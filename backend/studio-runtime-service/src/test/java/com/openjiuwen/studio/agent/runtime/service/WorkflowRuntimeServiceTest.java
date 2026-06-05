/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.alibaba.fastjson2.JSONObject;
import com.openjiuwen.studio.agent.common.dto.agent.ExecutionInfo;
import com.openjiuwen.studio.agent.common.dto.run.GetExecutionInsightQo;
import com.openjiuwen.studio.agent.runtime.dto.JiuwenEvent;
import com.openjiuwen.studio.agent.runtime.dto.JiuwenEventData;
import com.openjiuwen.studio.agent.runtime.dto.JiuwenEventDataError;
import com.openjiuwen.studio.agent.runtime.entity.WorkflowInstanceEntity;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class WorkflowRuntimeServiceTest {
    private static final Logger log = LoggerFactory.getLogger(WorkflowRuntimeServiceTest.class);

    private List<String> readDataLines(String urlString) throws IOException {
        URL url = new URL(urlString);
        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
            return reader.lines()
                    .filter(line -> line.startsWith("data: "))
                    .map(line -> line.substring(6)) // 移除"data: "前缀
                    .collect(Collectors.toList());
        }
    }

    @Test
    public void getExecutionInsight_test_by_file() throws IOException {
        List<String> data = new ArrayList<>();
        try {
            // 优先使用文件数据，读取出错则使用mock数据
            data = readDataLines("https://xxxxx:443" +
                    "/file/params/dataTest1.txt");
        } catch (Exception e) {
            log.info("use mock data");
        }

        // Given
        String projectId = "b691749589164e26a276ad287100cc79";
        String workflowId = "071b89e2-a3fa-4d06-a3bf-77864d9bd3ee";
        String executionId = "df773ce0-ec2b-4775-ad68-863859283be3";
        GetExecutionInsightQo getExecutionInsightQo = new GetExecutionInsightQo();
        getExecutionInsightQo.setVersion("1");

        // 创建 mock 的 workflowInstanceService
        WorkflowInstanceService workflowInstanceService = Mockito.mock(WorkflowInstanceService.class);
        WorkflowInstanceEntity workflowInstanceEntity = new WorkflowInstanceEntity();
        workflowInstanceEntity.setEventList(new ArrayList<>());

        when(workflowInstanceService.get(anyString(), anyString())).thenReturn(workflowInstanceEntity);

        ExecutionInfo executionOriInfo = new ExecutionInfo();
        executionOriInfo.setEventList(new ArrayList<>());
        when(workflowInstanceService.convertExecutionInfo(workflowInstanceEntity)).thenReturn(executionOriInfo);

        // 测试数据
        for (String eventData : data) {
            JiuwenEvent eventObj = JSONObject.parseObject(eventData, JiuwenEvent.class);
            if (eventObj.getEvent().equals("workflow_node_message")) {
                workflowInstanceEntity.getEventList().add(eventObj);
            }
        }

        // 创建被测对象并注入 mock
        WorkflowRuntimeService workflowRuntimeService = new WorkflowRuntimeService();
        ReflectionTestUtils.setField(workflowRuntimeService, "workflowInstanceService", workflowInstanceService);

        // When
        ExecutionInfo executionInfo =
                workflowRuntimeService.getExecutionInsight(projectId, workflowId, executionId, getExecutionInsightQo);
    }

    @Test
    public void getExecutionInsight_test_by_file_qa_before() throws IOException {
        List<String> data = new ArrayList<>();
        try {
            // 优先使用文件数据，读取出错则使用mock数据
            data = readDataLines("https://xxxxx:443/" +
                    "file/params/dataParam-qa.txt");
        } catch (Exception e) {
            log.info("use mock data");
        }

        // Given
        String projectId = "b691749589164e26a276ad287100cc79";
        String workflowId = "071b89e2-a3fa-4d06-a3bf-77864d9bd3ee";
        String executionId = "df773ce0-ec2b-4775-ad68-863859283be3";
        GetExecutionInsightQo getExecutionInsightQo = new GetExecutionInsightQo();
        getExecutionInsightQo.setVersion("1");

        // 创建 mock 的 workflowInstanceService
        WorkflowInstanceService workflowInstanceService = Mockito.mock(WorkflowInstanceService.class);
        WorkflowInstanceEntity workflowInstanceEntity = new WorkflowInstanceEntity();
        workflowInstanceEntity.setEventList(new ArrayList<>());

        when(workflowInstanceService.get(anyString(), anyString())).thenReturn(workflowInstanceEntity);

        ExecutionInfo executionOriInfo = new ExecutionInfo();
        executionOriInfo.setEventList(new ArrayList<>());
        when(workflowInstanceService.convertExecutionInfo(workflowInstanceEntity)).thenReturn(executionOriInfo);

        // 测试数据
        for (String eventData : data) {
            JiuwenEvent eventObj = JSONObject.parseObject(eventData, JiuwenEvent.class);
            if (eventObj.getEvent().equals("workflow_node_message")) {
                workflowInstanceEntity.getEventList().add(eventObj);
            }
        }

        // 创建被测对象并注入 mock
        WorkflowRuntimeService workflowRuntimeService = new WorkflowRuntimeService();
        ReflectionTestUtils.setField(workflowRuntimeService, "workflowInstanceService", workflowInstanceService);

        // When
        ExecutionInfo executionInfo =
                workflowRuntimeService.getExecutionInsight(projectId, workflowId, executionId, getExecutionInsightQo);
    }

    @Test
    public void getExecutionInsight_test_exception_event() {
        String projectId = "b691749589164e26a276ad287100cc79";
        String workflowId = "071b89e2-a3fa-4d06-a3bf-77864d9bd3ee";
        String executionId = "df773ce0-ec2b-4775-ad68-863859283be3";
        GetExecutionInsightQo getExecutionInsightQo = new GetExecutionInsightQo();
        getExecutionInsightQo.setVersion("1");

        // 创建 mock 的 workflowInstanceService
        WorkflowInstanceService workflowInstanceService = Mockito.mock(WorkflowInstanceService.class);

        // 创建一个默认的 WorkflowInstanceEntity 对象
        WorkflowInstanceEntity workflowInstanceEntity = new WorkflowInstanceEntity();
        workflowInstanceEntity.setEventList(new ArrayList<>()); // 设置一个空的事件列表

        when(workflowInstanceService.get(anyString(), anyString())).thenReturn(workflowInstanceEntity);

        ExecutionInfo executionOriInfo = new ExecutionInfo();
        executionOriInfo.setEventList(new ArrayList<>());
        when(workflowInstanceService.convertExecutionInfo(workflowInstanceEntity)).thenReturn(executionOriInfo);

        // 测试数据
        JiuwenEvent event1 = new JiuwenEvent();
        event1.setEvent("exception");
        JiuwenEventData data1 = new JiuwenEventData();
        data1.setNodeId("node");
        event1.setData(data1);
        String dataException = "{\"code\": \"test_code\"}";
        event1.setDataException(dataException);

        JiuwenEvent event2 = new JiuwenEvent();
        event2.setEvent("workflow_node_message");
        JiuwenEventData data2 = new JiuwenEventData();
        JiuwenEventDataError error = new JiuwenEventDataError();
        error.setErrorCode(1);
        data2.setComponentId("node");
        data2.setStatus(JiuwenEventData.StatusEnum.ERROR);
        data2.setError(error);
        event2.setData(data2);

        workflowInstanceEntity.getEventList().add(event1);
        workflowInstanceEntity.getEventList().add(event2);

        // 创建被测对象并注入 mock
        WorkflowRuntimeService workflowRuntimeService = new WorkflowRuntimeService();
        ReflectionTestUtils.setField(workflowRuntimeService, "workflowInstanceService",
                Mockito.mock(WorkflowInstanceService.class));
        ReflectionTestUtils.setField(workflowRuntimeService, "workflowInstanceService", workflowInstanceService);

        // When
        ExecutionInfo executionInfo = workflowRuntimeService.getExecutionInsight(projectId,
                workflowId, executionId, getExecutionInsightQo);
    }
}
