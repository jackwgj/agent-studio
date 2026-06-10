import logging

from agent_runtime.context.request_context import _request_ctx
from openjiuwen.core.common.logging import get_session_id

REQUEST_ID_LOG_FORMAT = (
    "%(asctime)s | %(log_type)s | %(trace_id)s | %(request_id)s | "
    "%(levelname)s | %(message)s"
)

_INSTALLED = False


def install_request_id_log_record_factory() -> None:
    """Add request_id to stdlib LogRecords used by openjiuwen formatters."""
    global _INSTALLED
    if _INSTALLED:
        return

    previous_factory = logging.getLogRecordFactory()

    def record_factory(*args, **kwargs):
        record = previous_factory(*args, **kwargs)
        if not hasattr(record, "trace_id"):
            record.trace_id = get_session_id()
        if not hasattr(record, "request_id"):
            record.request_id = _request_ctx.get().request_id
        return record

    logging.setLogRecordFactory(record_factory)
    _INSTALLED = True
