/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.mybatis;

import com.alibaba.fastjson2.JSONObject;
import com.openjiuwen.studio.agent.manager.dto.ComplexIntentBranchInfoReq;
import com.openjiuwen.studio.agent.manager.entity.ComplexIntentBranchEntity;
import com.openjiuwen.studio.agent.manager.entity.ComplexIntentEntity;
import com.openjiuwen.studio.agent.manager.mapper.ComplexIntentBranchMapper;
import com.openjiuwen.studio.agent.manager.mapper.ComplexIntentMapper;
import com.openjiuwen.studio.agent.manager.utils.BaseTest;

import lombok.SneakyThrows;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;

/**
 * ComplexIntent 测试类
 *
 */
@Transactional
public class ComplexIntentMapperTest extends BaseTest {

    @Autowired
    private ComplexIntentMapper complexIntentMapper;

    @Autowired
    private ComplexIntentBranchMapper complexIntentBranchMapper;

    @SneakyThrows
    @Test
    void testCrud() {
        ComplexIntentEntity entity = initEntity();
        Assertions.assertEquals(complexIntentMapper.createEntity(entity), 1);
        Assertions.assertEquals(complexIntentMapper.getEntities(entity).size(), 1);
        Assertions.assertNotNull(complexIntentMapper.getByKey(entity.getIntentId(), entity.getProjectId()));
        entity.setBranchesCnt(1);
        complexIntentMapper.updateByKey(entity);
        Assertions.assertEquals(
            complexIntentMapper.getByKey(entity.getIntentId(), entity.getProjectId()).getBranchesCnt(), 1);
        complexIntentMapper.deleteByKey(entity.getIntentId(), entity.getProjectId());
        Assertions.assertNull(complexIntentMapper.getByKey(entity.getIntentId(), entity.getProjectId()));
    }

    @SneakyThrows
    @Test
    void testBranchCrud() {
        ComplexIntentBranchEntity entity = initBranchEntity();
        Assertions.assertEquals(complexIntentBranchMapper.createEntity(entity), 1);
        Assertions.assertEquals(complexIntentBranchMapper.getEntities(entity).size(), 1);
        Assertions.assertNotNull(
            complexIntentBranchMapper.getByKey(entity.getBranchId(), entity.getIntentId(), entity.getProjectId()));
        entity.setBranchName("new");
        complexIntentBranchMapper.updateByKey(entity);
        Assertions.assertEquals(
            complexIntentBranchMapper.getByKey(entity.getBranchId(), entity.getIntentId(), entity.getProjectId())
                .getBranchName(),
            "new");
        complexIntentBranchMapper.deleteByKey(entity.getBranchId(), entity.getIntentId(), entity.getProjectId());
        Assertions.assertNull(
            complexIntentBranchMapper.getByKey(entity.getBranchId(), entity.getIntentId(), entity.getProjectId()));
    }

    private ComplexIntentEntity initEntity() {
        ComplexIntentEntity entity = new ComplexIntentEntity();
        entity.setIntentId("intent_id");
        entity.setProjectId("project_id");
        entity.setName("name");
        entity.setDescription("description");
        entity.setBranchesCnt(0);
        entity.setBranches("branches");
        entity.setDomainId("domain_id");
        return entity;
    }

    private ComplexIntentBranchEntity initBranchEntity() {
        ComplexIntentBranchEntity entity = new ComplexIntentBranchEntity();
        entity.setIntentId("intent_id");
        entity.setProjectId("project_id");
        entity.setBranchId("branch_id");
        entity.setBranchIndex(0);
        entity.setBranchName("name");

        ComplexIntentBranchInfoReq req =
            new ComplexIntentBranchInfoReq().setName("name").setDescription("聊天").setExamples(Arrays.asList("你好"));
        entity.setContent(JSONObject.toJSONString(req));
        entity.setCreatedOn(new Date());
        entity.setUpdatedOn(new Date());

        return entity;
    }
}
