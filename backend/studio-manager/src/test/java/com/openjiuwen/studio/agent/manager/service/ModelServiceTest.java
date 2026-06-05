/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service;

import static com.openjiuwen.studio.agent.manager.constant.Constants.TEST_PROJECT_ID;

import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.constant.Constants;
import com.openjiuwen.studio.agent.manager.rce.client.CustomModelManagerClient;
import com.openjiuwen.studio.agent.manager.rce.models.knowledge.ModelInfo;
import com.openjiuwen.studio.agent.manager.utils.BaseTest;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class ModelServiceTest extends BaseTest {
    private static MockedStatic<RequestContextUtils> mockedStatic;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private CustomModelManagerClient customModelManagerClient;

    private AutoCloseable mockitoCloseable;

    @BeforeAll
    public static void init() {
        RequestContextUtils.setRequestAuthTokenAndProjectId("mock", TEST_PROJECT_ID);
        mockedStatic = Mockito.mockStatic(RequestContextUtils.class);
        mockedStatic.when(RequestContextUtils::getRequestUserId).thenReturn(Constants.TEST_USER_ID);
    }

    @AfterAll
    static void end() {
        mockedStatic.close();
    }

    @BeforeEach
    void setUp() {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    private ModelInfo mockModelInfo() {
        return ModelInfo.builder().name("fake_model").path("/agentBuilder/search/v1/vector").detail("fake_detail")
            .type("embedding").endpoint("fake_endpoint").url("fake_endpoint/agentBuilder/search/v1/vector").build();
    }
}
