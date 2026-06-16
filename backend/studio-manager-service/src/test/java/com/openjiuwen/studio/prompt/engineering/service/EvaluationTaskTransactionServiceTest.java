package com.openjiuwen.studio.prompt.engineering.service;

import com.openjiuwen.studio.prompt.engineering.entity.PeEvaluationTask;
import com.openjiuwen.studio.prompt.engineering.enums.EvaluationTaskStatus;
import com.openjiuwen.studio.prompt.engineering.mapper.PeEvaluationTaskMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvaluationTaskTransactionServiceTest {

    @Mock
    private PeEvaluationTaskMapper peEvaluationTaskMapper;

    @InjectMocks
    private EvaluationTaskTransactionService evaluationTaskTransactionService;

    @Test
    void createEvaluationTask_success() {
        PeEvaluationTask evaluationTask = PeEvaluationTask.builder()
                .id("task-id-1")
                .name("test-task")
                .status(EvaluationTaskStatus.pending)
                .workspaceId("ws-1")
                .build();

        evaluationTaskTransactionService.createEvaluationTask(evaluationTask);

        verify(peEvaluationTaskMapper).insertOneEvaluationTask(evaluationTask);
    }

    @Test
    void createEvaluationTask_withTaskObject() {
        PeEvaluationTask evaluationTask = PeEvaluationTask.builder()
                .id("task-id-2")
                .name("detailed-task")
                .description("task description")
                .status(EvaluationTaskStatus.running)
                .workspaceId("ws-2")
                .build();

        evaluationTaskTransactionService.createEvaluationTask(evaluationTask);

        verify(peEvaluationTaskMapper).insertOneEvaluationTask(evaluationTask);
    }

    @Test
    void createEvaluationTask_nullTask() {
        evaluationTaskTransactionService.createEvaluationTask(null);

        verify(peEvaluationTaskMapper).insertOneEvaluationTask(null);
    }

    @Test
    void deleteEvaluationTaskAndResult_success() {
        String id = "eval-task-id";
        String workspaceId = "ws-1";

        evaluationTaskTransactionService.deleteEvaluationTaskAndResult(id, workspaceId);

        verify(peEvaluationTaskMapper).deleteEvaluationResultByEvaluationTaskId(id, workspaceId);
        verify(peEvaluationTaskMapper).deleteEvaluationTaskById(id, workspaceId);
    }

    @Test
    void deleteEvaluationTaskAndResult_verifyCallOrder() {
        String id = "eval-task-id";
        String workspaceId = "ws-1";

        evaluationTaskTransactionService.deleteEvaluationTaskAndResult(id, workspaceId);

        InOrder inOrder = inOrder(peEvaluationTaskMapper);
        inOrder.verify(peEvaluationTaskMapper).deleteEvaluationResultByEvaluationTaskId(id, workspaceId);
        inOrder.verify(peEvaluationTaskMapper).deleteEvaluationTaskById(id, workspaceId);
    }

    @Test
    void deleteEvaluationTaskAndResult_nullId() {
        String workspaceId = "ws-1";

        evaluationTaskTransactionService.deleteEvaluationTaskAndResult(null, workspaceId);

        verify(peEvaluationTaskMapper).deleteEvaluationResultByEvaluationTaskId(null, workspaceId);
        verify(peEvaluationTaskMapper).deleteEvaluationTaskById(null, workspaceId);
    }

    @Test
    void deleteEvaluationTaskResult_success() {
        String id = "eval-task-id";
        String workspaceId = "ws-1";

        evaluationTaskTransactionService.deleteEvaluationTaskResult(id, workspaceId);

        verify(peEvaluationTaskMapper).deleteEvaluationResultByEvaluationTaskId(id, workspaceId);
    }

    @Test
    void deleteEvaluationTaskResult_nullId() {
        String workspaceId = "ws-1";

        evaluationTaskTransactionService.deleteEvaluationTaskResult(null, workspaceId);

        verify(peEvaluationTaskMapper).deleteEvaluationResultByEvaluationTaskId(null, workspaceId);
    }

    @Test
    void modifyEvaluationTaskInfo_returnsUpdateCount() {
        when(peEvaluationTaskMapper.updateEvaluationTaskInfoById("id-1", "name", "desc", EvaluationTaskStatus.success, "ws-1"))
                .thenReturn(1);

        Integer result = evaluationTaskTransactionService.modifyEvaluationTaskInfo("id-1", "name", "desc", EvaluationTaskStatus.success, "ws-1");

        assertEquals(1, result);
    }

    @Test
    void modifyEvaluationTaskInfo_returnsNull() {
        when(peEvaluationTaskMapper.updateEvaluationTaskInfoById("id-2", "name", "desc", EvaluationTaskStatus.failed, "ws-2"))
                .thenReturn(null);

        Integer result = evaluationTaskTransactionService.modifyEvaluationTaskInfo("id-2", "name", "desc", EvaluationTaskStatus.failed, "ws-2");

        assertNull(result);
    }

    @Test
    void modifyEvaluationTaskInfo_returnsZero() {
        when(peEvaluationTaskMapper.updateEvaluationTaskInfoById("id-3", "name", "desc", EvaluationTaskStatus.paused, "ws-3"))
                .thenReturn(0);

        Integer result = evaluationTaskTransactionService.modifyEvaluationTaskInfo("id-3", "name", "desc", EvaluationTaskStatus.paused, "ws-3");

        assertEquals(0, result);
    }
}
