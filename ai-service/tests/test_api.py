from __future__ import annotations

from http.server import ThreadingHTTPServer
import json
from threading import Thread
import unittest
from urllib import error, request

from ai_service.app import create_handler
from tests.test_query_intent import FixedProvider
from ai_service.services.query_intent import QueryIntentService


class ApiTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls) -> None:
        service = QueryIntentService(FixedProvider())
        cls.server = ThreadingHTTPServer(("127.0.0.1", 0), create_handler(service, "secret"))
        cls.thread = Thread(target=cls.server.serve_forever, daemon=True)
        cls.thread.start()
        cls.base_url = f"http://127.0.0.1:{cls.server.server_port}"

    @classmethod
    def tearDownClass(cls) -> None:
        cls.server.shutdown()
        cls.server.server_close()
        cls.thread.join(timeout=2)

    def test_contract_matches_backend_and_requires_token(self) -> None:
        body = json.dumps({"question": "My plot is 200 sqm on a 9 m road"}).encode()
        api_request = request.Request(
            self.base_url + "/internal/v1/query-intents",
            data=body,
            method="POST",
            headers={"content-type": "application/json", "x-internal-token": "secret"},
        )
        with request.urlopen(api_request) as response:
            payload = json.load(response)

        self.assertEqual("en", payload["language"])
        self.assertEqual({"value": 200.0, "unit": "sqm"}, payload["plotArea"])
        self.assertIsNone(payload["buildingHeight"])
        self.assertIn("englishQuestion", payload)
        self.assertIn("ruleRefs", payload)

        unauthorized = request.Request(
            self.base_url + "/internal/v1/query-intents",
            data=body,
            method="POST",
            headers={"content-type": "application/json"},
        )
        with self.assertRaises(error.HTTPError) as raised:
            request.urlopen(unauthorized)
        try:
            self.assertEqual(401, raised.exception.code)
        finally:
            raised.exception.close()


if __name__ == "__main__":
    unittest.main()
