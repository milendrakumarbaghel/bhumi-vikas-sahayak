from __future__ import annotations

from http.server import ThreadingHTTPServer
import json
from threading import Thread
import unittest
from typing import Any
from urllib import request

from ai_service.app import create_handler
from ai_service.providers.base import ProviderError
from ai_service.services.answer import AnswerService
from ai_service.services.query_intent import QueryIntentService
from tests.test_query_intent import FixedProvider


CHUNK = {
    "id": "56#0",
    "label": "Rule 56 (1)",
    "title": "Residential Buildings",
    "pages": "77-77",
    "text": "2.    More than 9.0 meters and up to 12 meters                    3.6 meters",
}


class ReplyProvider:
    """Returns a fixed reply and remembers what it was asked."""

    configured = True
    model_name = "test-model"

    def __init__(self, reply: dict[str, Any]) -> None:
        self.reply = reply
        self.last_user_message = ""
        self.last_effort = ""

    def complete_json(self, system_prompt: str, user_message: str,
                      json_schema: dict[str, Any], effort: str = "low") -> str:
        self.last_user_message = user_message
        self.last_effort = effort
        return json.dumps(self.reply)


def answered(citations: list[dict[str, str]]) -> dict[str, Any]:
    return {"status": "answered", "answer": "3.6 m under Rule 56(1).",
            "citations": citations, "missingInputs": []}


class AnswerServiceTest(unittest.TestCase):
    def test_keeps_citations_only_for_supplied_chunks(self) -> None:
        provider = ReplyProvider(answered([
            {"chunkId": "56#0", "quote": "More than 9.0 meters and up to 12 meters 3.6 meters"},
            {"chunkId": "99#9", "quote": "invented"},
        ]))
        result = AnswerService(provider).answer(
            {"question": "10 m road, front gap?", "language": "en", "chunks": [CHUNK]})

        self.assertEqual("answered", result.status)
        self.assertEqual(["56#0"], [c.chunk_id for c in result.citations])
        self.assertEqual("medium", provider.last_effort)
        self.assertIn('chunkId="56#0"', provider.last_user_message)
        self.assertIn("ANSWER_LANGUAGE: en", provider.last_user_message)

    def test_passes_hindi_and_calculation_to_the_model(self) -> None:
        provider = ReplyProvider(answered([]))
        AnswerService(provider).answer({
            "question": "आगे कितनी जगह?", "language": "hi", "chunks": [CHUNK],
            "calculation": {"result": "3.6 m"},
        })
        self.assertIn("ANSWER_LANGUAGE: hi", provider.last_user_message)
        self.assertIn("CALCULATION", provider.last_user_message)

    def test_rejects_unknown_status(self) -> None:
        provider = ReplyProvider({"status": "maybe", "answer": "", "citations": [], "missingInputs": []})
        with self.assertRaises(ProviderError):
            AnswerService(provider).answer({"question": "q?", "chunks": [CHUNK]})

    def test_validates_request(self) -> None:
        service = AnswerService(ReplyProvider(answered([])))
        with self.assertRaises(ValueError):
            service.answer({"question": "", "chunks": [CHUNK]})
        with self.assertRaises(ValueError):
            service.answer({"question": "q?", "chunks": [{"id": "1"}]})
        with self.assertRaises(ValueError):
            service.answer({"question": "q?", "chunks": [CHUNK] * 11})


class AnswerApiTest(unittest.TestCase):
    def test_answers_endpoint(self) -> None:
        provider = ReplyProvider(answered([{"chunkId": "56#0", "quote": "3.6 meters"}]))
        handler = create_handler(QueryIntentService(FixedProvider()), "secret", AnswerService(provider))
        server = ThreadingHTTPServer(("127.0.0.1", 0), handler)
        thread = Thread(target=server.serve_forever, daemon=True)
        thread.start()
        try:
            body = json.dumps({"question": "front gap on 10 m road?", "language": "en",
                               "chunks": [CHUNK]}).encode()
            api_request = request.Request(
                f"http://127.0.0.1:{server.server_port}/internal/v1/answers",
                data=body, method="POST",
                headers={"content-type": "application/json", "x-internal-token": "secret"})
            with request.urlopen(api_request) as response:
                payload = json.load(response)
            self.assertEqual("answered", payload["status"])
            self.assertEqual([{"chunkId": "56#0", "quote": "3.6 meters"}], payload["citations"])
            self.assertEqual("test-model", payload["model"])
        finally:
            server.shutdown()
            server.server_close()
            thread.join(timeout=2)


if __name__ == "__main__":
    unittest.main()
