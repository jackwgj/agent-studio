from types import SimpleNamespace

from agent_runtime.conversation.operation_result import operation_succeeded


def test_operation_result_accepts_aio_zero_code():
    assert operation_succeeded(SimpleNamespace(code=0, data=object())) is True


def test_operation_result_rejects_nonzero_command_exit_code():
    result = SimpleNamespace(
        code=0,
        data=SimpleNamespace(exit_code=13),
    )
    assert operation_succeeded(result) is False


def test_operation_result_rejects_nonzero_command_exit_code_even_when_is_ok():
    result = SimpleNamespace(
        is_ok=lambda: True,
        data=SimpleNamespace(exit_code=1),
    )
    assert operation_succeeded(result) is False


def test_operation_result_rejects_aio_nonzero_code():
    assert operation_succeeded(SimpleNamespace(code=199003, data=None)) is False


def test_operation_result_fails_closed_for_unknown_contract():
    assert operation_succeeded(SimpleNamespace(data=object())) is False
