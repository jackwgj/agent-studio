import asyncio

import pytest

from agent_runtime.conversation.execution_context import ConversationExecutionContext, ConversationIdentity
from agent_runtime.conversation.execution_coordinator import acquire_conversation_execution


def context(conversation):
    return ConversationExecutionContext.create(
        ConversationIdentity("p", "w", "u", conversation, "e"), "/workspace"
    )


@pytest.mark.asyncio
async def test_same_conversation_waits_until_owner_releases():
    first = await acquire_conversation_execution(context("same"))
    waiter = asyncio.create_task(acquire_conversation_execution(context("same")))
    await asyncio.sleep(0)
    assert not waiter.done()
    await first.release()
    second = await waiter
    await second.release()


@pytest.mark.asyncio
async def test_different_conversations_do_not_block_each_other():
    first = await acquire_conversation_execution(context("one"))
    second = await asyncio.wait_for(acquire_conversation_execution(context("two")), 0.2)
    await second.release()
    await first.release()


@pytest.mark.asyncio
async def test_cancelled_waiter_does_not_leak_registry_or_lock():
    first = await acquire_conversation_execution(context("same"))
    waiter = asyncio.create_task(acquire_conversation_execution(context("same")))
    await asyncio.sleep(0)
    waiter.cancel()
    with pytest.raises(asyncio.CancelledError):
        await waiter
    await first.release()
    recovered = await acquire_conversation_execution(context("same"))
    await recovered.release()
