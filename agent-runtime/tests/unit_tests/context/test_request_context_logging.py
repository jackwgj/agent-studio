import logging
import unittest

from starlette.requests import Request
from starlette.responses import Response

from agent_runtime.context.middleware import RequestContextMiddleware
from agent_runtime.common.logging_context import (
    COMMON_LOG_FORMAT,
    DEFAULT_LOG_FORMAT,
    PERFORMANCE_LOG_FORMAT,
    get_log_format,
    install_log_formatter_patch,
    install_request_id_log_record_factory,
)


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
            captured["execution_id"] = record.execution_id
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
        self.assertEqual(captured["execution_id"], "exec-1")
        self.assertEqual(captured["request_id"], "00957491")

    def test_log_formats_align_with_jiuwen_field_order(self):
        self.assertEqual(
            COMMON_LOG_FORMAT,
            "%(asctime)s|%(log_type)s|%(filename)s:%(lineno)d|%(funcName)s|"
            "%(trace_id)s|%(execution_id)s|%(request_id)s|%(levelname)s|"
            "%(message)s",
        )
        self.assertEqual(
            PERFORMANCE_LOG_FORMAT,
            "%(asctime)s|%(log_type)s|%(trace_id)s|%(execution_id)s|"
            "%(request_id)s|%(levelname)s|%(message)s",
        )
        self.assertEqual(
            DEFAULT_LOG_FORMAT,
            "%(asctime)s|%(log_type)s|%(trace_id)s|%(levelname)s|%(message)s",
        )

    def test_log_format_is_selected_by_log_type(self):
        self.assertEqual(get_log_format("common"), COMMON_LOG_FORMAT)
        self.assertEqual(get_log_format("performance"), PERFORMANCE_LOG_FORMAT)
        self.assertEqual(get_log_format("workflow"), COMMON_LOG_FORMAT)
        self.assertEqual(get_log_format("llm"), COMMON_LOG_FORMAT)

    def test_default_logger_uses_log_type_specific_format(self):
        install_log_formatter_patch()

        from openjiuwen.core.common.logging.default.default_impl import DefaultLogger

        config = {
            "format": COMMON_LOG_FORMAT,
            "level": "INFO",
            "log_file": "./logs/test.log",
            "output": [],
        }

        self.assertEqual(
            DefaultLogger("common", config)._get_formatter()._fmt,
            COMMON_LOG_FORMAT,
        )
        self.assertEqual(
            DefaultLogger("performance", config)._get_formatter()._fmt,
            PERFORMANCE_LOG_FORMAT,
        )
        self.assertEqual(
            DefaultLogger("workflow", config)._get_formatter()._fmt,
            COMMON_LOG_FORMAT,
        )
        self.assertEqual(
            DefaultLogger("llm", config)._get_formatter()._fmt,
            COMMON_LOG_FORMAT,
        )

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
