/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.mapper;

import com.openjiuwen.studio.agent.runtime.constant.Constant;
import com.openjiuwen.studio.agent.runtime.entity.analytics.AnalyticsEventEntity;
import com.openjiuwen.studio.agent.runtime.entity.analytics.AnalyticsEventType;
import com.openjiuwen.studio.agent.runtime.utils.BaseTest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * mapper类
 *
 */
@MockitoSettings(strictness = Strictness.LENIENT)
public class AnalyticsEventMapperTest extends BaseTest {
    @MockitoBean
    RedissonClient redissonClientMock;

    @Autowired
    AnalyticsEventMapper analyticsEventMapper;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void testMapper() {
        AnalyticsEventEntity testData = AnalyticsEventEntity.builder()
            .eventId(UUID.randomUUID().toString())
            .eventType(AnalyticsEventType.COMPLETE.toString())
            .eventTime(new Date())
            .appType(Constant.AppType.AGENT)
            .appId(UUID.randomUUID().toString())
            .userId("test_user")
            .projectId("test_project")
            .channel("wechat")
            .isNewAppUser(false)
            .build();

        Assertions.assertEquals(analyticsEventMapper.createEntity(testData), 1);
        Assertions.assertNotNull(analyticsEventMapper.getByPrimaryKey(testData.getEventId(), testData.getProjectId()));

        Assertions.assertEquals(analyticsEventMapper.getEntity(testData).size(), 1);

        Assertions.assertEquals(analyticsEventMapper.deleteByPrimaryKey(testData.getEventId(), testData.getProjectId()),
            1);

        Assertions.assertNull(analyticsEventMapper.getByPrimaryKey(testData.getEventId(), testData.getProjectId()));

        List<AnalyticsEventEntity> datas = new ArrayList<>();
        datas.add(testData);

        analyticsEventMapper.insertBatch(datas);
        Assertions.assertNotNull(analyticsEventMapper.getByPrimaryKey(testData.getEventId(), testData.getProjectId()));

        analyticsEventMapper.clear(new Date(0), new Date());
        Assertions.assertNull(analyticsEventMapper.getByPrimaryKey(testData.getEventId(), testData.getProjectId()));
    }
}
