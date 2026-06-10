import logging
import unittest

from starlette.requests import Request
from starlette.responses import Response

from agent_runtime.context.middleware import RequestContextMiddleware
from agent_runtime.common.logging_context import install_request_id_log_record_factory


class RequestContextLoggingTest(unittest.IsolatedAsyncioTestCase):
    async def test_request_id_is_available_to_log_records_inside_request(self):
        install_request_id_log_record_factory()
        middleware = RequestContextMiddleware(app=lambda scope, receive, send: None)
        captured = {}

        async def call_next(request):
            record = logging.getLogRecordFactory()(
                "workflow",
                logging.INFO,
                __file__,
                1,
                "message",
                (),
                None,
            )
            captured["trace_id"] = record.trace_id
            captured["request_id"] = record.request_id
            return Response("ok")

        request = Request(
            {
                "type": "http",
                "method": "POST",
                "path": "/v1/orchestration/ir/execute",
                "headers": [
                    (b"x-request-id", b"00957491"),
                    (b"x-execution-id", b"exec-1"),
                ],
            },
            receive=self._body_receiver(
                b'{"conversationId":"122212412c92-6543-4c40-ac19-2f678995e7e9"}'
            ),
        )

        await middleware.dispatch(request, call_next)

        self.assertEqual(
            captured["trace_id"], "122212412c92-6543-4c40-ac19-2f678995e7e9"
        )
        self.assertEqual(captured["request_id"], "00957491")

    @staticmethod
    def _body_receiver(body):
        received = False

        async def receive():
            nonlocal received
            if received:
                return {"type": "http.request", "body": b"", "more_body": False}
            received = True
            return {"type": "http.request", "body": body, "more_body": False}

        return receive
