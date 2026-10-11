from __future__ import annotations

import json
import logging
import time
from typing import Any
from urllib import error, request

from ai_service.providers.base import ProviderError


LOGGER = logging.getLogger(__name__)
MESSAGES_URL = "https://api.anthropic.com/v1/messages"


class AnthropicProvider:
    """Minimal standard-library adapter for Anthropic structured messages."""

    def __init__(self, api_key: str, model: str, timeout_seconds: float = 90.0) -> None:
        self._api_key = api_key.strip()
        self._model = model.strip()
        self._timeout_seconds = timeout_seconds

    @property
    def configured(self) -> bool:
        return bool(self._api_key and self._model)

    @property
    def model_name(self) -> str:
        return self._model

    def complete_json(
        self,
        system_prompt: str,
        user_message: str,
        json_schema: dict[str, Any],
    ) -> str:
        if not self.configured:
            raise ProviderError("Anthropic provider is not configured")

        payload = {
            "model": self._model,
            "max_tokens": 4096,
            "system": system_prompt,
            "messages": [{"role": "user", "content": user_message}],
            "output_config": {
                "effort": "low",
                "format": {"type": "json_schema", "schema": json_schema},
            },
        }
        encoded = json.dumps(payload, ensure_ascii=False).encode("utf-8")
        api_request = request.Request(
            MESSAGES_URL,
            data=encoded,
            method="POST",
            headers={
                "content-type": "application/json",
                "x-api-key": self._api_key,
                "anthropic-version": "2023-06-01",
            },
        )
        response = self._send_with_one_retry(api_request)
        try:
            body = json.loads(response)
        except json.JSONDecodeError as exc:
            raise ProviderError("Provider returned non-JSON content") from exc

        stop_reason = body.get("stop_reason", "")
        if stop_reason in {"refusal", "max_tokens"}:
            raise ProviderError("Provider did not produce a complete structured response")

        text = "".join(
            block.get("text", "")
            for block in body.get("content", [])
            if block.get("type") == "text"
        )
        if not text:
            raise ProviderError("Provider returned no structured text")
        return text

    def _send_with_one_retry(self, api_request: request.Request) -> str:
        for attempt in (1, 2):
            try:
                with request.urlopen(api_request, timeout=self._timeout_seconds) as response:
                    return response.read().decode("utf-8")
            except error.HTTPError as exc:
                retryable = exc.code == 429 or exc.code == 529 or exc.code >= 500
                if not retryable or attempt == 2:
                    raise ProviderError(f"Provider returned HTTP {exc.code}") from exc
                LOGGER.warning("LLM provider returned a retryable HTTP status; retrying once")
            except (error.URLError, TimeoutError) as exc:
                if attempt == 2:
                    raise ProviderError("Could not reach LLM provider") from exc
                LOGGER.warning("Network error calling LLM provider; retrying once")
            time.sleep(1.5)
        raise ProviderError("Could not reach LLM provider")
