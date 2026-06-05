/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.service;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.dto.run.TaskStatus;
import com.openjiuwen.studio.agent.runtime.enums.MessageRole;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Date;

@MockitoSettings(strictness = Strictness.LENIENT)
public class TaskManagementServiceTest {
    @InjectMocks
    TaskManagementService taskManagementService;

    @Mock
    ConversationManagementService conversationManagementService;

    @Test
    void test_checkStatus() {
        TaskManagementService taskManagementService = new TaskManagementService();
        Assertions.assertThrows(AgentStudioException.class, () -> taskManagementService.checkStatus(TaskStatus.RUNNING, null));
        Assertions.assertThrows(AgentStudioException.class, () -> taskManagementService.checkStatus(TaskStatus.INIT, null));
        Assertions.assertThrows(AgentStudioException.class, () -> taskManagementService.checkStatus(TaskStatus.COMPLETED, "TASK"));
        Assertions.assertThrows(AgentStudioException.class, () -> taskManagementService.checkStatus(TaskStatus.FAILED, "TASK"));

        boolean success;
        try {
            taskManagementService.checkStatus(TaskStatus.COMPLETED, "chat");
            taskManagementService.checkStatus(TaskStatus.FAILED, "chat");
            taskManagementService.checkStatus(TaskStatus.PENDING, null);
            success = true;
        } catch (Exception ignore) {
            success = false;
        }
        Assertions.assertTrue(success);

    }

    @Test
    public void test_setUserQueryToHistory() {
        String workflowId = "workflowId";
        String conversationId = "conversationId";
        String query = "{\"query\":\"测试\"}";
        Date currentTime = new Date(System.currentTimeMillis());
        boolean success = true;
        try {
            taskManagementService.setUserQueryToHistory(workflowId, conversationId, currentTime, query, MessageRole.USER.name());
        } catch (Exception e) {
            success = false;
        }
        Assertions.assertTrue(success);
    }
}
