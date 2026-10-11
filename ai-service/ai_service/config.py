from __future__ import annotations

from dataclasses import dataclass
import os


@dataclass(frozen=True)
class Settings:
    host: str
    port: int
    internal_token: str
    anthropic_api_key: str
    anthropic_model: str
    provider_timeout_seconds: float

    @classmethod
    def from_environment(cls) -> "Settings":
        return cls(
            host=os.getenv("AI_SERVICE_HOST", "127.0.0.1"),
            port=_integer("AI_SERVICE_PORT", 8081),
            internal_token=os.getenv("BVS_AI_SERVICE_TOKEN", "").strip(),
            anthropic_api_key=os.getenv("ANTHROPIC_API_KEY", "").strip(),
            anthropic_model=os.getenv("ANTHROPIC_MODEL", "").strip() or "claude-opus-5-5",
            provider_timeout_seconds=_number("AI_PROVIDER_TIMEOUT_SECONDS", 90.0),
        )


def _integer(name: str, default: int) -> int:
    try:
        return int(os.getenv(name, str(default)))
    except ValueError as exc:
        raise ValueError(f"{name} must be an integer") from exc


def _number(name: str, default: float) -> float:
    try:
        value = float(os.getenv(name, str(default)))
    except ValueError as exc:
        raise ValueError(f"{name} must be a number") from exc
    if value <= 0:
        raise ValueError(f"{name} must be positive")
    return value
