/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.rce.service;

import static com.openjiuwen.studio.agent.runtime.constant.ConstantTest.TEST_AGENT_ID;
import static com.openjiuwen.studio.agent.runtime.constant.ConstantTest.TEST_AGENT_NAME;
import static com.openjiuwen.studio.agent.runtime.constant.ConstantTest.TEST_MODEL_DEPLOYMENT_ID;
import static com.openjiuwen.studio.agent.runtime.constant.ConstantTest.TEST_PROJECT_ID;
import static com.openjiuwen.studio.agent.runtime.constant.ConstantTest.TEST_TOKEN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.openjiuwen.studio.agent.common.rce.client.ModelManagerClient;
import com.openjiuwen.studio.agent.common.rce.model.ModelServiceInfo;
import com.openjiuwen.studio.agent.runtime.rce.client.DataManagerClient;
import com.openjiuwen.studio.agent.runtime.rce.model.BackflowMessage;
import com.openjiuwen.studio.agent.runtime.rce.model.BackflowModel;
import com.openjiuwen.studio.agent.runtime.rce.model.BackflowSource;
import com.openjiuwen.studio.agent.runtime.rce.model.BackflowVo;
import com.openjiuwen.studio.agent.runtime.rce.model.CreateBackflowReq;
import com.openjiuwen.studio.agent.runtime.utils.JsonUtils;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.runtime.utils.TestUtil;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 功能描述
 *
 */
@MockitoSettings(strictness = Strictness.LENIENT)
class DataManagerServiceTest {
    private static MockedStatic<RequestContextUtils> mockedStatic;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DataManagerClient dataManagerClient;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ModelManagerClient modelManagerClient;

    @InjectMocks
    private DataManagerService dataManagerService;

    private AutoCloseable mockitoCloseable;

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
        ReflectionTestUtils.setField(dataManagerService, "dataManagerClient", dataManagerClient);
        ReflectionTestUtils.setField(dataManagerService, "modelManagerClient", modelManagerClient);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_createDataBackflowJob_succeed() {
        ModelServiceInfo modelServiceInfo =
            JsonUtils.json2Obj(TestUtil.getStringFromFile("classpath:response/query_model_info_success_result.json"),
                new TypeReference<>() {});
        ResponseEntity<ModelServiceInfo> modelResponse =
            new ResponseEntity<>(modelServiceInfo, HttpStatusCode.valueOf(200));
        when(modelManagerClient.queryModelInfo(any(), any())).thenReturn(modelResponse);
        ResponseEntity<JSONObject> creatJobResponse = new ResponseEntity<>(HttpStatusCode.valueOf(200));
        when(dataManagerClient.createBackflowJob(any(), any(), any(), any())).thenReturn(creatJobResponse);
        dataManagerService.createDataBackflowJob(TEST_PROJECT_ID, mockCreateBackflowReq());
    }

    @Test
    void test_createDataBackflowJob_when_result_is_null() {
        // test model deployment id is null
        CreateBackflowReq req1 = mockCreateBackflowReq();
        req1.getData().get(0).getModel().setDeploymentId(null);
        dataManagerService.createDataBackflowJob(TEST_PROJECT_ID, mockCreateBackflowReq());

        // test model response failed
        ModelServiceInfo modelServiceInfo =
            JsonUtils.json2Obj(TestUtil.getStringFromFile("classpath:response/query_model_info_success_result.json"),
                new TypeReference<>() {});
        ResponseEntity<ModelServiceInfo> modelResponse1 =
            new ResponseEntity<>(modelServiceInfo, HttpStatusCode.valueOf(401));
        when(modelManagerClient.queryModelInfo(any(), any())).thenReturn(modelResponse1);
        dataManagerService.createDataBackflowJob(TEST_PROJECT_ID, mockCreateBackflowReq());

        // test model response is null
        ResponseEntity<ModelServiceInfo> modelResponse2 = new ResponseEntity<>(null, HttpStatusCode.valueOf(200));
        when(modelManagerClient.queryModelInfo(any(), any())).thenReturn(modelResponse1);
        dataManagerService.createDataBackflowJob(TEST_PROJECT_ID, mockCreateBackflowReq());
    }

    @Test
    void test_createDataBackflowJob_should_catch_AgentRuntimeException() {
        // set up - mock throws RuntimeException which is unchecked
        when(modelManagerClient.queryModelInfo(any(), any())).thenThrow(new RuntimeException("test exception"));

        // run test - should not throw, just log error
        dataManagerService.createDataBackflowJob(TEST_PROJECT_ID, mockCreateBackflowReq());
    }

    @Test
    void test_createDataBackflowJob_should_catch_Exception() {
        CreateBackflowReq req = mockCreateBackflowReq();
        req.getData().get(0).setModel(null);
        dataManagerService.createDataBackflowJob(TEST_PROJECT_ID, mockCreateBackflowReq());
    }

    private CreateBackflowReq mockCreateBackflowReq() {
        BackflowVo backflowVo = new BackflowVo();
        List<BackflowMessage> messageList = new ArrayList<>();
        backflowVo.setMessages(messageList);
        backflowVo.setSource(new BackflowSource(TEST_AGENT_ID, "agent", TEST_AGENT_NAME));
        BackflowModel backflowModel = new BackflowModel();
        backflowModel.setDeploymentId(TEST_MODEL_DEPLOYMENT_ID);
        backflowVo.setModel(backflowModel);
        List<BackflowVo> data = new ArrayList<>();
        data.add(backflowVo);
        return new CreateBackflowReq(data);
    }
}
