/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.alarm.app;

import com.openjiuwen.studio.agent.runtime.utils.BaseTest;

import lombok.SneakyThrows;

import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * 功能描述
 *
 */
public class AppApiRecoderTest extends BaseTest {

    @MockitoBean
    RedissonClient redissonClientMock;

    @Test
    @SneakyThrows
    public void test_record_failure_app_should_void_when_test_data_combination() {

        // run the test
        AppApiRecoder.recordFailureApp("123", "11", "11", "string");
    }

    @Test
    @SneakyThrows
    public void test_record_success_app_should_void_when_test_data_combination() {

        // run the test
        AppApiRecoder.recordSuccessApp("string");
    }

    @Test
    @SneakyThrows
    public void test_check_app_call_should_void_when_objects_is_null() {
        // run the test
        AppApiRecoder.recordFailureApp("123", "11", "11", "string");
        AppApiRecoder.checkAppCall(1, 1);
        AppApiRecoder.recordSuccessApp("11");
        AppApiRecoder.checkAppCall(1, 1);
    }
}