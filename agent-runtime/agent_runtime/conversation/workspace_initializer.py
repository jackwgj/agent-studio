"""Idempotently prepare the current layout without replacing conversation files."""

from agent_runtime.conversation.execution_context import (
    ConversationExecutionContext, get_conversation_execution_context,
)
from agent_runtime.conversation.input_artifact_bridge import conversation_sandbox_operation
from agent_runtime.conversation.operation_result import operation_error_detail, operation_succeeded
from agent_runtime.conversation.sandbox import ConversationSandboxConfig, ConversationSysOperationFactory
from agent_runtime.conversation.sandbox.remote_directories import remote_directory_command


class ConversationWorkspaceInitializationError(RuntimeError):
    """The remote workspace could not be initialized safely."""


_ENSURE_DIRECTORIES = r'''
root_fd = open_absolute(arguments["sandbox_root"])
try:
    for relative in arguments["directories"]:
        fd = open_relative(root_fd, relative, create=True)
        os.close(fd)
finally:
    os.close(root_fd)
print("workspace ready")
'''


class ConversationWorkspaceInitializer:
    def __init__(self, operation):
        self._operation = operation

    async def ensure(self, context: ConversationExecutionContext) -> None:
        root = context.workspace.sandbox_root
        expected = ConversationExecutionContext.create(context.identity, root)
        if context != expected or str(root) == "/":
            raise ConversationWorkspaceInitializationError("workspace layout is outside its trusted boundary")
        targets = (
            context.workspace.input_dir, context.workspace.skills_dir,
            context.workspace.work_dir, context.output_dir, context.tmp_dir,
        )
        arguments = {
            "sandbox_root": str(root),
            "directories": [str(path.relative_to(root)) for path in targets],
        }
        try:
            result = await self._operation.shell().execute_cmd(
                remote_directory_command(_ENSURE_DIRECTORIES, arguments), cwd="/",
            )
        except Exception as error:
            raise ConversationWorkspaceInitializationError("remote workspace initialization failed") from error
        data = getattr(result, "data", None)
        if (
            not operation_succeeded(result) or data is None
            or getattr(data, "exit_code", None) != 0
            or (getattr(data, "stdout", "") or "").strip() != "workspace ready"
        ):
            detail = getattr(data, "stderr", "") or getattr(data, "stdout", "") or operation_error_detail(result)
            raise ConversationWorkspaceInitializationError(
                f"remote workspace initialization failed: {str(detail)[:1000]}"
            )


async def ensure_conversation_workspace() -> None:
    """Prepare before inputs/Agent execution; disabled mode performs no FS I/O."""
    from agent_runtime.common.config import settings

    config = ConversationSandboxConfig.from_security_sandbox_settings(settings.security_sandbox)
    if ConversationSysOperationFactory(config).create() is None:
        return
    context = get_conversation_execution_context()
    async with conversation_sandbox_operation("conversation_workspace_init") as operation:
        await ConversationWorkspaceInitializer(operation).ensure(context)
