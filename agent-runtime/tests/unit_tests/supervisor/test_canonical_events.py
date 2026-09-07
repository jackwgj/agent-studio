from agent_runtime.supervisor.event import canonical
from agent_runtime.supervisor.event.conversation_adapter import adapt_runner_event
from agent_runtime.supervisor.event.types import ConversationEventType


def test_canonical_events_bind_root_and_child_runs_to_the_same_conversation():
    root = canonical.build_run_start("conversation-1", "run-root")
    child = canonical.build_message(
        "conversation-1",
        "run-child",
        "hello",
        parent_run_id="run-root",
        execution_type="agent",
        agent_id="agent-1",
    )

    assert root["runId"] == root["data"]["runId"] == "run-root"
    assert root["parentRunId"] is root["data"]["parentRunId"] is None
    assert child["conversationId"] == "conversation-1"
    assert child["runId"] == child["data"]["runId"] == "run-child"
    assert child["parentRunId"] == child["data"]["parentRunId"] == "run-root"
    assert child["data"]["agentId"] == "agent-1"


def test_skill_error_and_run_end_have_canonical_contracts():
    skill = canonical.build_skill_activated(
        "conversation-1",
        "run-child",
        skill_id="meeting-minutes",
        name="Meeting Minutes",
        version_id="v1",
        parent_run_id="run-root",
    )
    error = canonical.build_error(
        "conversation-1",
        "run-root",
        code="runtime_failed",
        message="boom",
    )
    run_end = canonical.build_run_end(
        "conversation-1",
        "run-root",
        status="success",
        text="done",
    )

    assert skill["event"] == ConversationEventType.SKILL_ACTIVATED.value
    assert skill["data"] == {
        "runId": "run-child",
        "parentRunId": "run-root",
        "executionType": "agent",
        "skillId": "meeting-minutes",
        "name": "Meeting Minutes",
        "versionId": "v1",
    }
    assert error["event"] == ConversationEventType.ERROR.value
    assert error["data"]["code"] == "runtime_failed"
    assert error["data"]["message"] == "boom"
    assert run_end["event"] == "run_end"
    assert run_end["data"]["status"] == "success"
    assert run_end["data"]["text"] == "done"


def test_artifact_contract_contains_storage_metadata_and_execution_ownership():
    artifact = canonical.build_artifact(
        "conversation-1",
        "run-root",
        execution_id="execution-1",
        object_key="conversation-artifacts/answer.pdf",
        file_name="answer.pdf",
        size=1024,
        media_type="application/pdf",
        checksum="a" * 64,
    )

    assert artifact["event"] == ConversationEventType.ARTIFACT.value
    assert artifact["runId"] == "run-root"
    assert artifact["data"] == {
        "runId": "run-root",
        "parentRunId": None,
        "executionType": "agent",
        "executionId": "execution-1",
        "objectKey": "conversation-artifacts/answer.pdf",
        "fileName": "answer.pdf",
        "size": 1024,
        "mediaType": "application/pdf",
        "checksum": "a" * 64,
    }


def test_adapter_accepts_run_end_but_rejects_legacy_run_done():
    canonical_end = adapt_runner_event(
        {"event": "run_end", "data": {"status": "success"}},
        conversation_id="conversation-1",
        run_id="run-root",
    )
    legacy_end = adapt_runner_event(
        {"event": "run_done", "data": {"text": "legacy"}},
        conversation_id="conversation-1",
        run_id="run-root",
    )

    assert canonical_end is not None
    assert canonical_end["event"] == "run_end"
    assert legacy_end is None


def test_root_success_is_released_after_artifacts_while_child_end_passes_through():
    sequencer = canonical.CanonicalEventSequencer(root_run_id="run-root")
    child_end = canonical.build_run_end(
        "conversation-1",
        "run-child",
        parent_run_id="run-root",
    )
    root_end = canonical.build_run_end("conversation-1", "run-root")
    artifact = canonical.build_artifact(
        "conversation-1",
        "run-root",
        execution_id="execution-1",
        object_key="conversation-artifacts/answer.txt",
        file_name="answer.txt",
        size=4,
        media_type="text/plain",
        checksum="b" * 64,
    )

    assert sequencer.accept(child_end) == [child_end]
    assert sequencer.accept(root_end) == []
    assert sequencer.accept(artifact) == [artifact]
    assert sequencer.release_root_end() == [root_end]


def test_root_error_discards_staged_success():
    sequencer = canonical.CanonicalEventSequencer(root_run_id="run-root")
    root_end = canonical.build_run_end("conversation-1", "run-root")
    error = canonical.build_error(
        "conversation-1",
        "run-root",
        code="runtime_failed",
        message="boom",
    )

    assert sequencer.accept(root_end) == []
    assert sequencer.accept(error) == [error]
    assert sequencer.release_root_end() == []


def test_cancelled_run_terminates_immediately_and_suppresses_later_success():
    sequencer = canonical.CanonicalEventSequencer(root_run_id="run-root")
    cancelled = canonical.build_run_end(
        "conversation-1",
        "run-root",
        status="cancelled",
    )
    success = canonical.build_run_end("conversation-1", "run-root")

    assert sequencer.accept(cancelled) == [cancelled]
    assert sequencer.accept(success) == []
    assert sequencer.release_root_end() == []


def test_each_run_emits_at_most_one_terminal_result():
    sequencer = canonical.CanonicalEventSequencer(root_run_id="run-root")
    first_child_end = canonical.build_run_end(
        "conversation-1",
        "run-child",
        parent_run_id="run-root",
    )
    duplicate_child_end = canonical.build_run_end(
        "conversation-1",
        "run-child",
        status="cancelled",
        parent_run_id="run-root",
    )

    assert sequencer.accept(first_child_end) == [first_child_end]
    assert sequencer.accept(duplicate_child_end) == []


def test_controller_task_events_form_a_child_run_under_business_execution():
    start = adapt_runner_event(
        {"event": "task_start", "executionId": "controller-task", "data": {}},
        conversation_id="conversation-1",
        run_id="execution-1",
    )
    end = adapt_runner_event(
        {"event": "task_end", "executionId": "controller-task", "data": {}},
        conversation_id="conversation-1",
        run_id="execution-1",
    )

    assert start["event"] == "run_start"
    assert start["runId"] == "controller-task"
    assert start["parentRunId"] == "execution-1"
    assert start["data"]["controllerEvent"] == "task_start"
    assert end["event"] == "run_end"
    assert end["runId"] == "controller-task"
    assert end["parentRunId"] == "execution-1"


def test_controller_specific_events_are_not_silently_dropped():
    cases = {
        "agent_handoff": "workflow_node",
        "agent_interrupted": "error",
        "waiting_user_input": "message",
        "intermediate_message": "message",
        "workflow_start": "workflow_node",
        "workflow_end": "workflow_node",
    }

    for raw_event, expected in cases.items():
        event = adapt_runner_event(
            {
                "event": raw_event,
                "executionId": "controller-task",
                "data": {
                    "task_id": "handoff-task",
                    "message": "controller update",
                    "workflow_id": "workflow-1",
                    "target_agent": {"id": "agent-child", "name": "Child"},
                },
            },
            conversation_id="conversation-1",
            run_id="execution-1",
        )

        assert event is not None
        assert event["event"] == expected
        assert event["runId"] == "controller-task"
        assert event["parentRunId"] == "execution-1"
        assert event["data"]["controllerEvent"] == raw_event
