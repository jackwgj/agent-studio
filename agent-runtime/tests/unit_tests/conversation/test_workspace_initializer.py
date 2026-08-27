"""Workspace initialization must be remote-only and preserve existing state."""

import base64
import importlib
import zlib
from types import SimpleNamespace
from unittest.mock import AsyncMock

import pytest

from agent_runtime.conversation.execution_context import ConversationExecutionContext, ConversationIdentity


def context():
    return ConversationExecutionContext.create(ConversationIdentity(
        project_id="project", workspace_id="workspace", user_id="user",
        conversation_id="conversation", execution_id="execution",
    ), "/workspace")


def api():
    return importlib.import_module("agent_runtime.conversation.workspace_initializer")


@pytest.mark.asyncio
async def test_initializer_uses_existing_root_cwd_not_uncreated_conversation():
    module = api()
    shell = SimpleNamespace(execute_cmd=AsyncMock(return_value=SimpleNamespace(
        code=0, data=SimpleNamespace(exit_code=0, stdout="workspace ready", stderr=""),
    )))
    initializer = module.ConversationWorkspaceInitializer(SimpleNamespace(shell=lambda: shell))
    await initializer.ensure(context())
    await initializer.ensure(context())
    assert shell.execute_cmd.await_count == 2
    for call in shell.execute_cmd.await_args_list:
        assert call.kwargs.get("cwd") not in (
            str(context().workspace.conversation_root), str(context().output_dir),
        )
        assert len(call.args[0]) < 3000
        source = _decode_program(call.args[0])
        compile(source, "remote-initialize", "exec")
        assert str(context().output_dir.relative_to(context().workspace.sandbox_root)) in source


@pytest.mark.asyncio
@pytest.mark.parametrize("result", [
    SimpleNamespace(code=199004, message="transport failed", data=None),
    SimpleNamespace(code=0, data=SimpleNamespace(exit_code=1, stderr="Permission denied", stdout="")),
    SimpleNamespace(code=0, data=None),
    SimpleNamespace(code=0, data=SimpleNamespace(exit_code=0, stdout="", stderr="")),
])
async def test_initializer_does_not_treat_failed_shell_as_success(result):
    module = api()
    operation = SimpleNamespace(shell=lambda: SimpleNamespace(execute_cmd=AsyncMock(return_value=result)))
    with pytest.raises(module.ConversationWorkspaceInitializationError, match="workspace initialization failed"):
        await module.ConversationWorkspaceInitializer(operation).ensure(context())


@pytest.mark.asyncio
async def test_initializer_rejects_tampered_paths_before_remote_execution():
    module = api()
    shell = SimpleNamespace(execute_cmd=AsyncMock())
    ctx = context()
    object.__setattr__(ctx.workspace, "output_dir", ctx.workspace.sandbox_root / "other-conversation")
    with pytest.raises(module.ConversationWorkspaceInitializationError, match="layout"):
        await module.ConversationWorkspaceInitializer(SimpleNamespace(shell=lambda: shell)).ensure(ctx)
    shell.execute_cmd.assert_not_awaited()


@pytest.mark.asyncio
async def test_unconfigured_workspace_initialization_does_not_access_local_or_remote_fs(monkeypatch):
    module = api()
    monkeypatch.setenv("CONVERSATION_SANDBOX_MODE", "disabled")
    remote = AsyncMock(side_effect=AssertionError("must not request a remote operation"))
    monkeypatch.setattr(module, "conversation_sandbox_operation", remote)
    await module.ensure_conversation_workspace()
    remote.assert_not_called()


def test_remote_directory_program_stays_short_and_roundtrips_large_parameters():
    from agent_runtime.conversation.sandbox.remote_directories import remote_directory_command

    script = 'print("test result")'
    command = remote_directory_command(script, {"paths": ["/workspace/" + "a" * 300] * 5})
    assert len(command) < 3000  # leave room for the Provider's bash/cwd wrapper
    source = _decode_program(command)
    assert script in source
    assert "/workspace/" + "a" * 300 in source


def test_real_output_scan_program_stays_short_and_compiles():
    from agent_runtime.conversation.output_artifact_collector import _remote_scan_command

    command = _remote_scan_command()
    assert len(command) < 3000
    compile(_decode_program(command), "remote-scan", "exec")


def _decode_program(command):
    encoded = command.split("b64decode('")[1].split("')")[0]
    return zlib.decompress(base64.b64decode(encoded)).decode("utf-8")
