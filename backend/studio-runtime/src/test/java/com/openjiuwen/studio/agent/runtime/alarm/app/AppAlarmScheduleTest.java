/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.alarm.app;

import com.openjiuwen.studio.agent.runtime.utils.BaseTest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.redisson.api.RedissonClient;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * 功能描述
 *
 */
public class AppAlarmScheduleTest extends BaseTest {
    @MockitoBean
    RedissonClient redissonClientMock;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AppAlarmSchedule appAlarmSchedule;

    @BeforeEach
    public void setUp() throws Exception {
        ReflectionTestUtils.setField(appAlarmSchedule, "appFailureCountLimit", 0);
        ReflectionTestUtils.setField(appAlarmSchedule, "appSuccessCountLimit", 0);
    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    @Test
    public void test_check_api_failure_count_should_void_when_objects_is_null() throws Exception {
        // run the test
        appAlarmSchedule.checkApiFailureCount();
    }
}
