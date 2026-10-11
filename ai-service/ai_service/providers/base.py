from __future__ import annotations

from typing import Any, Protocol


class ProviderError(RuntimeError):
    """Raised for unavailable, refused, truncated, or malformed provider responses."""


class StructuredLlmProvider(Protocol):
    @property
    def configured(self) -> bool: ...

    @property
    def model_name(self) -> str: ...

    def complete_json(
        self,
        system_prompt: str,
        user_message: str,
        json_schema: dict[str, Any],
        effort: str = "low",
    ) -> str: ...
