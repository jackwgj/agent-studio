"""Opt-in real AIO checks, restricted to the explicitly supplied local service.

CONVERSATION_AIO_TEST_URL=http://127.0.0.1:8082
No database/model needed. Each test uses and removes its own random /workspace
subdirectory, never existing conversations or the volume root.
"""

import asyncio
import hashlib
import json
import os
import re
import uuid

import pytest
import pytest_asyncio
from openjiuwen.core.runner.resources_manager.resource_manager import ResourceMgr

from agent_runtime.conversation.execution_context import (
    ConversationExecutionContext, ConversationIdentity,
    set_conversation_execution_context, reset_conversation_execution_context,
)
from agent_runtime.conversation.output_artifact_collector import (
    ConversationOutputCollector, OutputArtifactCollectionError, RemoteSandboxOutputSource,
)
from agent_runtime.conversation.sandbox import ConversationSandboxConfig, ConversationSysOperationFactory
from agent_runtime.conversation.sandbox.registration import get_conversation_sandbox_operation
from agent_runtime.conversation.sandbox.remote_directories import remote_directory_command
from agent_runtime.conversation.workspace_initializer import (
    ConversationWorkspaceInitializer, ConversationWorkspaceInitializationError,
)

URL = os.environ.get("CONVERSATION_AIO_TEST_URL", "")
pytestmark = pytest.mark.skipif(not URL, reason="requires explicit local AIO test URL")


@pytest_asyncio.fixture
async def aio():
    assert URL.rstrip("/") in {"http://localhost:8082", "http://127.0.0.1:8082"}
    manager = ResourceMgr()
    card = ConversationSysOperationFactory(ConversationSandboxConfig(
        mode="sandbox", server=URL, ssl_verify=False, sandbox_type="aio",
        idle_ttl_seconds=600, timeout_seconds=30, scope="system",
    )).create()
    operation = get_conversation_sandbox_operation(manager, card)
    root = "/workspace/.ojw-init-test-" + uuid.uuid4().hex

    async def program(script, **arguments):
        result = await operation.shell().execute_cmd(remote_directory_command(script, arguments), cwd="/")
        assert result.code == 0, result
        assert result.data.exit_code == 0, result.data
        return result.data.stdout

    await program('os.mkdir(arguments["root"])', root=root)

    def context(execution="first", conversation="conversation"):
        return ConversationExecutionContext.create(ConversationIdentity(
            project_id="test-project", workspace_id="test-workspace", user_id="test-user",
            conversation_id=conversation, execution_id=execution,
        ), root)

    try:
        yield operation, context, program
    finally:
        # Only the exact random test root created above can be removed.
        assert re.fullmatch(r"/workspace/\.ojw-init-test-[0-9a-f]{32}", root)
        await program('import shutil\nshutil.rmtree(arguments["root"])', root=root)


@pytest.mark.asyncio
async def test_real_aio_ensure_is_idempotent_concurrent_and_preserves_multiturn_files(aio):
    operation, context, program = aio
    first, second = context(), context("second")
    initializer = ConversationWorkspaceInitializer(operation)
    await initializer.ensure(first)
    paths = [first.workspace.input_dir, first.workspace.skills_dir, first.workspace.work_dir, first.output_dir, first.tmp_dir]
    assert json.loads(await program('print(json.dumps([os.path.isdir(p) for p in arguments["paths"]]))', paths=list(map(str, paths)))) == [True] * 5
    marker = str(first.workspace.work_dir / "keep.txt")
    await program('with open(arguments["path"], "w") as f: f.write("preserve me")', path=marker)
    await asyncio.gather(*(initializer.ensure(ctx) for ctx in [first, second, first, second]))
    assert first.workspace.work_dir == second.workspace.work_dir
    assert await program('print(open(arguments["path"]).read())', path=marker) == "preserve me"


@pytest.mark.asyncio
async def test_real_aio_missing_empty_and_populated_output(aio):
    operation, context, program = aio
    ctx = context()
    token = set_conversation_execution_context(ctx)
    try:
        collector = ConversationOutputCollector(RemoteSandboxOutputSource(operation))
        assert await collector.collect() == []  # even the conversation does not exist
        await ConversationWorkspaceInitializer(operation).ensure(ctx)
        assert await collector.collect() == []
        await program('os.rmdir(arguments["path"])', path=str(ctx.output_dir))
        assert await collector.collect() == []
        await ConversationWorkspaceInitializer(operation).ensure(ctx)
        await program('with open(arguments["path"], "wb") as f: f.write(b"hello artifact")', path=str(ctx.output_dir / "report.txt"))
        artifacts = await collector.collect()
        assert len(artifacts) == 1
        assert artifacts[0].content == b"hello artifact"
        assert artifacts[0].checksum == hashlib.sha256(b"hello artifact").hexdigest()

        baseline = await collector.snapshot()
        assert await collector.collect(baseline=baseline) == []

        await program(
            'with open(arguments["path"], "wb") as f: f.write(b"updated artifact")',
            path=str(ctx.output_dir / "report.txt"),
        )
        changed = await collector.collect(baseline=baseline)
        assert len(changed) == 1
        assert changed[0].content == b"updated artifact"
        assert changed[0].checksum == hashlib.sha256(b"updated artifact").hexdigest()
    finally:
        reset_conversation_execution_context(token)


@pytest.mark.asyncio
@pytest.mark.parametrize("target_kind", ["work_symlink", "work_file", "ancestor_symlink"])
async def test_real_aio_initializer_rejects_symlinks_and_files(aio, target_kind):
    operation, context, program = aio
    ctx = context()
    await ConversationWorkspaceInitializer(operation).ensure(ctx)
    outside = str(ctx.workspace.sandbox_root / "outside")
    if target_kind == "ancestor_symlink":
        target = str(ctx.workspace.conversation_root)
        await program('os.rename(arguments["path"], arguments["outside"])\nos.symlink(arguments["outside"], arguments["path"])', path=target, outside=outside)
    else:
        target = str(ctx.workspace.work_dir)
        await program('os.rmdir(arguments["path"])', path=target)
        if target_kind == "work_symlink":
            await program('os.mkdir(arguments["outside"])\nos.symlink(arguments["outside"], arguments["path"])', path=target, outside=outside)
        else:
            await program('with open(arguments["path"], "w") as f: f.write("do not overwrite")', path=target)
    with pytest.raises(ConversationWorkspaceInitializationError):
        await ConversationWorkspaceInitializer(operation).ensure(ctx)
    if target_kind == "work_file":
        assert await program('print(open(arguments["path"]).read())', path=target) == "do not overwrite"


@pytest.mark.asyncio
@pytest.mark.parametrize("kind", ["root_symlink", "child_symlink", "permission", "nested_permission"])
async def test_real_aio_scan_does_not_hide_path_or_permission_errors(aio, kind):
    operation, context, program = aio
    ctx = context()
    await ConversationWorkspaceInitializer(operation).ensure(ctx)
    path = str(ctx.output_dir)
    denied = None
    if kind == "root_symlink":
        await program('os.rmdir(arguments["path"])\nos.symlink(arguments["other"], arguments["path"])', path=path, other=str(ctx.workspace.work_dir))
    elif kind == "child_symlink":
        await program('os.symlink(arguments["other"], arguments["path"])', path=str(ctx.output_dir / "link"), other=str(ctx.workspace.work_dir))
    else:
        if await program('print(os.geteuid())') == "0":
            pytest.skip("root bypasses directory permission checks")
        denied = path if kind == "permission" else str(ctx.output_dir / "locked")
        await program('os.makedirs(arguments["path"], exist_ok=True)\nos.chmod(arguments["path"], 0)', path=denied)
    token = set_conversation_execution_context(ctx)
    try:
        with pytest.raises(OutputArtifactCollectionError):
            await ConversationOutputCollector(RemoteSandboxOutputSource(operation)).collect()
    finally:
        reset_conversation_execution_context(token)
        if denied:
            await program('os.chmod(arguments["path"], 0o700)', path=denied)


@pytest.mark.asyncio
async def test_real_aio_scan_cannot_target_another_conversation(aio):
    operation, context, _ = aio
    token = set_conversation_execution_context(context())
    try:
        with pytest.raises(OutputArtifactCollectionError, match="boundary"):
            await RemoteSandboxOutputSource(operation).scan(str(context(conversation="other").output_dir))
    finally:
        reset_conversation_execution_context(token)
