/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.rce.service;

import static com.openjiuwen.studio.agent.runtime.constant.ConstantTest.TEST_ASSISTANT_ANSWER;
import static com.openjiuwen.studio.agent.runtime.constant.ConstantTest.TEST_TOKEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.runtime.constant.ConstantTest;
import com.openjiuwen.studio.agent.runtime.rce.client.JiuWenClient;
import com.openjiuwen.studio.agent.runtime.rce.model.JiuWenDeleteIrRsp;
import com.openjiuwen.studio.agent.runtime.rce.model.JiuWenExecutionRsp;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * JiuWenService测试类
 *
 */
@MockitoSettings(strictness = Strictness.LENIENT)
class JiuWenServiceTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private JiuWenClient jiuWenClient;

    @InjectMocks
    private JiuWenService jiuWenService;

    private AutoCloseable mockitoCloseable;

    private static MockedStatic<RequestContextUtils> mockedStatic;

    @BeforeAll
    static void init() {
        mockedStatic = Mockito.mockStatic(RequestContextUtils.class);
        mockedStatic.when(RequestContextUtils::getRequestAuthToken).thenReturn(TEST_TOKEN);
    }

    @AfterAll
    static void end() {
        mockedStatic.close();
    }

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(jiuWenService, "jiuWenClient", jiuWenClient);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }


    @Test
    void test_deleteConversationIr_should_succeed_when_test_data_combination() {
        // set up
        when(jiuWenClient.deleteIr(any(), any())).thenReturn(mockJiuWenDeleteIrRsp());

        // run test
        JiuWenDeleteIrRsp rsp = jiuWenService.deleteConversationIr(ConstantTest.Conversation.TEST_CONVERSATION_ID);

        // verify result
        assertEquals(0, rsp.getCode());
    }

    @Test
    void test_deleteConversationIr_should_throw_AgentRuntimeException_when_fail_to_invoke_jiu_wen_service() {
        Assertions.assertThrows(AgentStudioException.class, () -> {
            // set up
            when(jiuWenClient.deleteIr(any(), any())).thenThrow(new RuntimeException("test exception"));

            // run test
            jiuWenService.deleteConversationIr(ConstantTest.Conversation.TEST_CONVERSATION_ID);
        });
    }

    private JiuWenExecutionRsp mockJiuWenExecutionRsp() {
        JiuWenExecutionRsp resp = new JiuWenExecutionRsp();
        resp.setConversationId(ConstantTest.Conversation.TEST_CONVERSATION_ID);
        resp.setData(JiuWenExecutionRsp.ExecutionData.builder().answer(TEST_ASSISTANT_ANSWER).build());
        return resp;
    }

    private JiuWenDeleteIrRsp mockJiuWenDeleteIrRsp() {
        JiuWenDeleteIrRsp rsp = new JiuWenDeleteIrRsp();
        rsp.setCode(0);
        rsp.setMessage("success");
        return rsp;
    }
}
