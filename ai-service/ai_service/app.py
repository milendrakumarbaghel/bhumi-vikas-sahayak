from __future__ import annotations

from hmac import compare_digest
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
import json
import logging
from typing import Any, Type

from ai_service.config import Settings
from ai_service.providers.anthropic import AnthropicProvider
from ai_service.providers.base import ProviderError
from ai_service.services.query_intent import QueryIntentService


LOGGER = logging.getLogger(__name__)
MAX_BODY_BYTES = 16_384


def create_handler(
    service: QueryIntentService,
    internal_token: str,
) -> Type[BaseHTTPRequestHandler]:
    class Handler(BaseHTTPRequestHandler):
        server_version = "BhumiVikasAiService/0.1"

        def do_GET(self) -> None:  # noqa: N802 - standard library callback name
            if self.path == "/internal/v1/health":
                self._json(200, {
                    "status": "ok",
                    "providerConfigured": service.provider_configured,
                })
                return
            self._json(404, {"code": "NOT_FOUND", "message": "Endpoint not found"})

        def do_POST(self) -> None:  # noqa: N802 - standard library callback name
            if self.path != "/internal/v1/query-intents":
                self._json(404, {"code": "NOT_FOUND", "message": "Endpoint not found"})
                return
            if internal_token and not compare_digest(
                self.headers.get("x-internal-token", ""), internal_token
            ):
                self._json(401, {"code": "UNAUTHORIZED", "message": "Invalid internal token"})
                return
            try:
                body = self._request_json()
                question = body.get("question") if isinstance(body, dict) else None
                if not isinstance(question, str):
                    raise ValueError("question must be a string")
                self._json(200, service.analyse(question).to_contract())
            except ValueError as exc:
                self._json(422, {"code": "VALIDATION_ERROR", "message": str(exc)})
            except ProviderError:
                self._json(503, {
                    "code": "AI_PROVIDER_UNAVAILABLE",
                    "message": "The AI provider is unavailable or returned an invalid response",
                })
            except Exception:
                LOGGER.exception("Unexpected AI-service request failure")
                self._json(500, {"code": "INTERNAL_ERROR", "message": "Internal service error"})

        def _request_json(self) -> Any:
            try:
                length = int(self.headers.get("content-length", "0"))
            except ValueError as exc:
                raise ValueError("invalid content length") from exc
            if length <= 0 or length > MAX_BODY_BYTES:
                raise ValueError("request body size is invalid")
            try:
                return json.loads(self.rfile.read(length).decode("utf-8"))
            except (UnicodeDecodeError, json.JSONDecodeError) as exc:
                raise ValueError("request body must be UTF-8 JSON") from exc

        def _json(self, status: int, payload: dict[str, Any]) -> None:
            encoded = json.dumps(payload, ensure_ascii=False).encode("utf-8")
            self.send_response(status)
            self.send_header("content-type", "application/json; charset=utf-8")
            self.send_header("content-length", str(len(encoded)))
            self.send_header("cache-control", "no-store")
            self.send_header("x-content-type-options", "nosniff")
            self.end_headers()
            self.wfile.write(encoded)

        def log_message(self, format: str, *args: object) -> None:
            LOGGER.info("%s - %s", self.client_address[0], format % args)

    return Handler


def build_server(settings: Settings) -> ThreadingHTTPServer:
    provider = AnthropicProvider(
        settings.anthropic_api_key,
        settings.anthropic_model,
        settings.provider_timeout_seconds,
    )
    service = QueryIntentService(provider)
    return ThreadingHTTPServer(
        (settings.host, settings.port),
        create_handler(service, settings.internal_token),
    )


def run() -> None:
    logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(name)s %(message)s")
    settings = Settings.from_environment()
    server = build_server(settings)
    LOGGER.info("AI service listening on %s:%s", settings.host, settings.port)
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        LOGGER.info("AI service stopping")
    finally:
        server.server_close()
