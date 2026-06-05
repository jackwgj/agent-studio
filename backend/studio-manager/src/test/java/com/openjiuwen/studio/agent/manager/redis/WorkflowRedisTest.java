/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.redis;

import com.openjiuwen.studio.agent.manager.constant.Constants;
import com.openjiuwen.studio.agent.manager.dto.Message;
import com.openjiuwen.studio.agent.manager.entity.SessionEntity;
import com.openjiuwen.studio.agent.manager.entity.WorkflowInstanceEntity;
import com.openjiuwen.studio.agent.manager.entity.WorkflowMessageEntity;
import com.openjiuwen.studio.agent.manager.utils.BaseTest;
import com.openjiuwen.studio.agent.manager.utils.WorkflowUtils;

import lombok.SneakyThrows;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * redis数据库mock
 *
 */
@Transactional
public class WorkflowRedisTest extends BaseTest {

    @Sql(scripts = {"classpath:sql/workflow_setup_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @SneakyThrows
    @Test
    void testWorkflowEntity() {
        WorkflowInstanceEntity workflowInstanceEntity = new WorkflowInstanceEntity();
        workflowInstanceEntity.setWorkflowId(Constants.TEST_WORKFLOW_ID);
        workflowInstanceEntity.setInstanceId("test");
        workflowInstanceEntity.setStatus("wait");
        workflowInstanceEntity.setRunInfo(new ArrayList<>());
        workflowInstanceEntity.setProjectId(Constants.TEST_PROJECT_ID);
        workflowInstanceEntity.setCreatedOn(new Date(System.currentTimeMillis()));
        workflowInstanceEntity.setUpdatedOn(new Date(System.currentTimeMillis()));
        workflowInstanceEntity.setSessionId("test");

        Assertions.assertNotNull(workflowInstanceEntity.getWorkflowId());
        Assertions.assertNotNull(workflowInstanceEntity.getInstanceId());
        Assertions.assertNotNull(workflowInstanceEntity.getStatus());
        Assertions.assertNotNull(workflowInstanceEntity.getRunInfo());
        Assertions.assertNotNull(workflowInstanceEntity.getProjectId());
        Assertions.assertNotNull(workflowInstanceEntity.getCreatedOn());
        Assertions.assertNotNull(workflowInstanceEntity.getUpdatedOn());
        Assertions.assertNotNull(workflowInstanceEntity.getSessionId());

        Message message = new Message();
        message.setRole("user");
        message.setContent("你好");
        List<WorkflowMessageEntity> workflowMessageEntity =
            WorkflowUtils.convertMessageToMessageEntity("test", Collections.singletonList(message));
        Assertions.assertEquals(workflowMessageEntity.size(), 1);
        WorkflowMessageEntity messageEntity = workflowMessageEntity.get(0);
        Assertions.assertNotNull(messageEntity.getMessageId());
        Assertions.assertNotNull(messageEntity.getRole());
        Assertions.assertNotNull(messageEntity.getContent());
        Assertions.assertNotNull(messageEntity.getCreatedOn());
        Assertions.assertNotNull(messageEntity.getUpdatedOn());
        Assertions.assertNotNull(messageEntity.getSessionId());

        SessionEntity session = new SessionEntity();
        session.setWorkflowId(Constants.TEST_LLM_WORKFLOW_ID);
        session.setSessionId("test");
        session.setInstanceId("instance_id");
        session.setProjectId(Constants.TEST_PROJECT_ID);
        session.setCreatedOn(new Date(System.currentTimeMillis()));
        session.setUpdatedOn(new Date(System.currentTimeMillis()));
        Assertions.assertNotNull(session.getInstanceId());
        Assertions.assertNotNull(session.getWorkflowId());
        Assertions.assertNotNull(session.getProjectId());
        Assertions.assertNotNull(session.getCreatedOn());
        Assertions.assertNotNull(session.getUpdatedOn());
        Assertions.assertNotNull(session.getSessionId());
    }
}
