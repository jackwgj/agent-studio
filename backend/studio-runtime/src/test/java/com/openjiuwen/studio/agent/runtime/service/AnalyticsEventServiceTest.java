/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service;

import static com.openjiuwen.studio.agent.runtime.constant.ConstantTest.TEST_PROJECT_ID;
import static com.openjiuwen.studio.agent.runtime.constant.ConstantTest.TEST_TOKEN;
import static com.openjiuwen.studio.agent.runtime.constant.ConstantTest.TEST_USER_ID;

import com.openjiuwen.studio.agent.runtime.constant.Constant;
import com.openjiuwen.studio.agent.common.dto.analytics.AnalyticsEventReq;
import com.openjiuwen.studio.agent.runtime.entity.analytics.AnalyticsEventEntity;
import com.openjiuwen.studio.agent.runtime.entity.analytics.AnalyticsEventType;
import com.openjiuwen.studio.agent.runtime.mapper.AnalyticsEventMapper;
import com.openjiuwen.studio.agent.runtime.task.AnalyticsEventTaskScheduler;
import com.openjiuwen.studio.agent.runtime.utils.AnalyticsEventQueue;
import com.openjiuwen.studio.agent.runtime.utils.BaseTest;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

/**
 * 事件测试
 *
 */
@Transactional
@MockitoSettings(strictness = Strictness.LENIENT)
public class AnalyticsEventServiceTest extends BaseTest {
    private static MockedStatic<RequestContextUtils> mockedStatic;

    @MockitoBean
    RedissonClient redissonClientMock;

    @Autowired
    AnalyticsEventService analyticsEventService;

    @Autowired
    AnalyticsEventMapper analyticsEventMapper;

    @Autowired
    AnalyticsEventTaskScheduler analyticsEventTaskScheduler;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);

        RequestContextUtils.setRequestAuthTokenAndProjectId(TEST_TOKEN, TEST_PROJECT_ID);
        mockedStatic = Mockito.mockStatic(RequestContextUtils.class);
        mockedStatic.when(RequestContextUtils::getRequestUserId).thenReturn(TEST_USER_ID);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
        mockedStatic.close();
    }

    @Test
    void testService() {
        String appId = UUID.randomUUID().toString();
        AnalyticsEventEntity testData = AnalyticsEventEntity.builder()
            .eventType(AnalyticsEventType.COMPLETE.toString())
            .appType(Constant.AppType.AGENT)
            .appId(appId)
            .userId("test_user")
            .projectId("test_project")
            .channel("wechat")
            .build();

        AnalyticsEventEntity test2nd = AnalyticsEventEntity.builder()
            .eventType(AnalyticsEventType.COMPLETE.toString())
            .appType(Constant.AppType.AGENT)
            .appId(appId)
            .userId("test_user")
            .projectId("test_project")
            .channel("wechat")
            .build();

        analyticsEventService.addEvent(testData);
        analyticsEventService.addEvent(test2nd);
        Assertions.assertNull(analyticsEventMapper.getByPrimaryKey(testData.getEventId(), testData.getProjectId()));

        analyticsEventService.flushEvent(new Date(System.currentTimeMillis() + 5));
        Assertions.assertNotNull(analyticsEventMapper.getByPrimaryKey(testData.getEventId(), testData.getProjectId()));
        Assertions.assertTrue(
            analyticsEventMapper.getByPrimaryKey(testData.getEventId(), testData.getProjectId()).getIsNewAppUser());
        AnalyticsEventEntity entity =
            analyticsEventMapper.getByPrimaryKey(test2nd.getEventId(), test2nd.getProjectId());
        if (entity == null) {
            throw new RuntimeException(
                "Entity not found with eventId=" + test2nd.getEventId() + " and projectId=" + test2nd.getProjectId());
        }
        Assertions.assertFalse(entity.getIsNewAppUser());

        analyticsEventService.clearEvent(new Date(0), new Date());
        Assertions.assertNull(analyticsEventMapper.getByPrimaryKey(testData.getEventId(), testData.getProjectId()));
    }

    @Test
    void testTask() {
        AnalyticsEventEntity testData = AnalyticsEventEntity.builder()
            .eventType(AnalyticsEventType.COMPLETE.toString())
            .appType(Constant.AppType.AGENT)
            .eventTime(
                Date.from(LocalDateTime.now().minusMinutes(60 * 24 * 181).atZone(ZoneId.systemDefault()).toInstant()))
            .appId(UUID.randomUUID().toString())
            .userId("test_user")
            .projectId("test_project")
            .channel("wechat")
            .build();

        analyticsEventService.addEvent(testData);
        analyticsEventTaskScheduler.flushTask();
        analyticsEventTaskScheduler.clearTask();
        Assertions.assertNull(analyticsEventMapper.getByPrimaryKey(testData.getEventId(), testData.getProjectId()));
    }

    @Test
    void testEvent() {
        AnalyticsEventReq req = new AnalyticsEventReq();
        req.setAppType(Constant.AppType.AGENT);
        req.setChannel("xx");
        req.setEventType("like");

        analyticsEventService.analyticsEvent("project", "agentid", req);
        Assertions.assertNotNull(AnalyticsEventQueue.popBatchByTime(new Date()));
    }
}
