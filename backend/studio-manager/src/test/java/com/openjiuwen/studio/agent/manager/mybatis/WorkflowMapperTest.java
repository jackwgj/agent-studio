/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.mybatis;

import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.constant.Constants;
import com.openjiuwen.studio.agent.manager.entity.WorkflowEntity;
import com.openjiuwen.studio.agent.manager.mapper.WorkflowMapper;
import com.openjiuwen.studio.agent.manager.utils.BaseTest;

import lombok.SneakyThrows;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 数据库查表测试，UT使用h2替换mysql
 *
 */
@Transactional
public class WorkflowMapperTest extends BaseTest {
    @Autowired
    private WorkflowMapper workflowMapper;

    @Sql(scripts = {"classpath:sql/workflow_setup_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @SneakyThrows
    @Test
    void testParseWorkflowTable() {
        WorkflowEntity workflowEntity = workflowMapper.getWorkflowEntityByStatus(CommonConstant.WORKFLOW_PUBLISHED,
            Constants.TEST_CODE_INTERPRETER_WORKFLOW_ID);
    }

    @Sql(scripts = {"classpath:sql/workflow_setup_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @SneakyThrows
    @Test
    void testParseWorkflowFilter() {
        WorkflowEntity filter = new WorkflowEntity();
        filter.setId(Constants.TEST_CODE_INTERPRETER_WORKFLOW_ID);
        filter.setStatus(CommonConstant.WORKFLOW_PUBLISHED);
        List<WorkflowEntity> workflowEntityList = workflowMapper.getWorkflowEntityByFilter(filter);
        Assertions.assertEquals(workflowEntityList.size(), 1);
        filter.setName("s");
        workflowEntityList = workflowMapper.getWorkflowEntityByFilter(filter);
        Assertions.assertEquals(workflowEntityList.size(), 0);
    }
}
