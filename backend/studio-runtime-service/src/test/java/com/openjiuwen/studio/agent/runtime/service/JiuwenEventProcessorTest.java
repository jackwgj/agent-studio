/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

import com.fasterxml.jackson.core.type.TypeReference;
import com.openjiuwen.studio.agent.common.utils.JsonUtils;
import com.openjiuwen.studio.agent.runtime.dto.JiuwenEvent;
import com.openjiuwen.studio.agent.runtime.dto.JiuwenEventData;
import com.openjiuwen.studio.agent.runtime.dto.WorkflowRunInfo;
import com.openjiuwen.studio.agent.runtime.entity.WorkflowInstanceEntity;
import com.openjiuwen.studio.agent.runtime.enums.JiuwenEventType;
import com.openjiuwen.studio.agent.runtime.model.ExecuteParams;
import com.openjiuwen.studio.agent.runtime.model.WorkflowIrWrapper;
import com.openjiuwen.studio.agent.runtime.model.WorkflowRunResult;
import com.openjiuwen.studio.agent.runtime.service.memory.UserMemoryCacheMgmtService;

import okhttp3.sse.EventSource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class JiuwenEventProcessorTest {
    @Test
    public void exception_process_test() {
        String eventStr
            = "{\"event\":\"exception\",\"data\":{\"name\":null,\"jiuwen_exception_node_id\":\"node_1760433595385\"},\"createdTime\":1760448600762,\"executionId\":\"15b7389a-91e5-40ad-91ea-95fc49ca51d1\",\"index\":0,\"isStructMessage\":false}";
        ExecuteParams params = ExecuteParams.builder().build();
        WorkflowRunResult result = new WorkflowRunResult();
        result.setInstance(new WorkflowInstanceEntity());
        result.getInstance().setStatus("error");

        EventSource eventSource = Mockito.mock(EventSource.class);

        JiuwenEventProcessor eventProcessor = new JiuwenEventProcessor();

        ReflectionTestUtils.setField(eventProcessor, "logEvents", new ArrayList<>());

        eventProcessor.process(eventStr, params, result, eventSource);
    }

    @Test
    public void intermediateMessage_process_test() {
        String eventStr
            = "{\"event\":\"intermediate_message\",\"data\":{\"answer\":[{\"role\":\"user\",\"content\":\"test1\",\"function_call\":[],\"enable_history\":true},{\"role\":\"assistant\",\"content\":\"test2\",\"enable_history\":true}]},\"createdTime\":1760324072161,\"executionId\":\"2645f4a1-3c8c-4402-a66b-f5331c56870f\",\"index\":0,\"isStructMessage\":false}";
        ExecuteParams params = ExecuteParams.builder().build();
        WorkflowRunResult result = Mockito.mock(WorkflowRunResult.class);
        EventSource eventSource = Mockito.mock(EventSource.class);

        JiuwenEventProcessor eventProcessor = new JiuwenEventProcessor();
        UserMemoryCacheMgmtService userMemoryCacheMgmtService = Mockito.mock(UserMemoryCacheMgmtService.class);

        ReflectionTestUtils.setField(eventProcessor, "logEvents", new ArrayList<>());
        ReflectionTestUtils.setField(eventProcessor, "userMemoryCacheMgmtService", userMemoryCacheMgmtService);

        eventProcessor.process(eventStr, params, result, eventSource);
    }

    @Test
    public void extractOriginAnswerTest() {
        JiuwenEventData data = new JiuwenEventData().setOriginAnswer("ans");
        Assertions.assertEquals("ans", new JiuwenEventProcessor().extractOriginAnswer(data));

        data = new JiuwenEventData().setOriginAnswer(new HashMap<>());
        Assertions.assertEquals("{}", new JiuwenEventProcessor().extractOriginAnswer(data));
    }

    @Test
    public void processMessageEndTest() {
        JiuwenEventProcessor processor = new JiuwenEventProcessor();

        WorkflowIrCacheService workflowIrService = Mockito.mock(WorkflowIrCacheService.class);

        WorkflowIrWrapper wrapper = new WorkflowIrWrapper();
        wrapper.setIdNameMap(new HashMap<>());

        Mockito.when(workflowIrService.getWorkflowIr(Mockito.any(), Mockito.any())).thenReturn(wrapper);

        ReflectionTestUtils.setField(processor, "workflowIrService", workflowIrService);
        ReflectionTestUtils.setField(processor, "logEvents", new ArrayList<>());

        ExecuteParams params = ExecuteParams.builder().workflowId("workflow_id").build();

        EventSource eventSource = Mockito.mock(EventSource.class);

        JiuwenEventData eventData = new JiuwenEventData();
        JiuwenEvent event = new JiuwenEvent().setIsStructMessage(true).setData(eventData).setEvent("message_end");
        WorkflowRunResult result = new WorkflowRunResult();

        processor.process(JsonUtils.encode(event), params, result, eventSource);
    }

    @Test
    public void processDoneTest() {
        JiuwenEventProcessor processor = new JiuwenEventProcessor();

        WorkflowIrCacheService workflowIrService = Mockito.mock(WorkflowIrCacheService.class);

        WorkflowIrWrapper wrapper = new WorkflowIrWrapper();
        wrapper.setIdNameMap(new HashMap<>());

        Mockito.when(workflowIrService.getWorkflowIr(Mockito.any(), Mockito.any())).thenReturn(wrapper);

        ReflectionTestUtils.setField(processor, "workflowIrService", workflowIrService);
        ReflectionTestUtils.setField(processor, "logEvents", new ArrayList<>());

        ExecuteParams params = ExecuteParams.builder().workflowId("workflow_id").build();

        EventSource eventSource = Mockito.mock(EventSource.class);

        JiuwenEventData eventData = new JiuwenEventData();
        JiuwenEvent event = new JiuwenEvent().setIsStructMessage(true).setData(eventData).setEvent("done");
        WorkflowRunResult result = new WorkflowRunResult();
        WorkflowInstanceEntity instance = Mockito.mock(WorkflowInstanceEntity.class);
        WorkflowRunInfo workflowRunInfo = Mockito.mock(WorkflowRunInfo.class);
        result.setInstance(instance);
        result.setWorkflowRunInfo(workflowRunInfo);

        WorkflowInstanceService instanceService = Mockito.mock(WorkflowInstanceService.class);
        ConversationManagementService conversationManagementService = Mockito.mock(ConversationManagementService.class);
        ReflectionTestUtils.setField(processor, "instanceService", instanceService);
        ReflectionTestUtils.setField(processor, "conversationManagementService", conversationManagementService);
        doNothing().when(instanceService).save(any(), any());
        doNothing().when(conversationManagementService).saveTaskId(any(), any(), any());

        processor.process(JsonUtils.encode(event), params, result, eventSource);
    }

    @Test
    public void valueClippingTest() {
        JiuwenEventProcessor processor = new JiuwenEventProcessor();
        ReflectionTestUtils.setField(processor, "maxDebugMsgLength", 5);
        processor.postConstruct();

        List<Map<String, Object>> data = JsonUtils.decode(
            "[{\"maxValue\":\"12345678911111\",\"int\":10,\"bool\":true},{\"maxObj\":{\"maxValue\":"
                + "\"12345678911111\",\"int\":10,\"maxList\":[{\"maxValue\":\"12345678911111\"},"
                + "{\"maxValue\":\"12345678911111\"}]},\"maxListStr\":[\"12345678\",\"12345678\"]}]",
            new TypeReference<>() { });

        JiuwenEventProcessor.valueClipping(data);
        Assertions.assertEquals("[{\"maxValue\":\"12...\",\"int\":10,\"bool\":true},{\"maxObj\":"
            + "{\"maxValue\":\"12...\",\"int\":10,\"maxList\":[{\"maxValue\":\"12...\"},{\"maxValue\":\"12...\"}]}"
            + ",\"maxListStr\":[\"12...\",\"12...\"]}]", JsonUtils.encode(data));
    }

    @Test
    public void recordEventTest() {
        JiuwenEventType eventType = JiuwenEventType.WORKFLOW_NODE_MESSAGE;
        JiuwenEvent jiuwenEvent = new JiuwenEvent();
        jiuwenEvent.setData(new JiuwenEventData());
        jiuwenEvent.getData().setComponentType("jiuwen.setVariable");
        jiuwenEvent.getData().setInputs(new HashMap<>());

        Map<String, Object> userFields = new HashMap<>();
        userFields.put("null", null);
        jiuwenEvent.getData().getInputs().put("userFields", userFields);

        WorkflowInstanceEntity instance = new WorkflowInstanceEntity();
        instance.setId("workflow_instance_id");
        instance.setEventList(new ArrayList<>());

        JiuwenEventProcessor processor = new JiuwenEventProcessor();
        ReflectionTestUtils.setField(processor, "logEvents", new ArrayList<>());

        processor.recordEvent(jiuwenEvent, eventType, instance);

        jiuwenEvent.getData().getInputs().put("userFields", "");
        processor.recordEvent(jiuwenEvent, eventType, instance);
    }

    @Test
    public void saveInstanceTest() {
        JiuwenEventProcessor processor = new JiuwenEventProcessor();
        WorkflowRunInfo workflowRunInfo = new WorkflowRunInfo();
        WorkflowInstanceEntity instance = new WorkflowInstanceEntity();
        ExecuteParams executeParams = ExecuteParams.builder().workflowId("workflow_id").build();
        WorkflowInstanceService instanceService = Mockito.mock(WorkflowInstanceService.class);
        ConversationManagementService conversationManagementService = Mockito.mock(ConversationManagementService.class);

        ReflectionTestUtils.setField(processor, "instanceService", instanceService);
        ReflectionTestUtils.setField(processor, "conversationManagementService", conversationManagementService);

        doNothing().when(instanceService).save(any(), any());
        doNothing().when(conversationManagementService).saveTaskId(any(), any(), any());
        processor.saveInstance(workflowRunInfo, instance, executeParams, false);
    }
}
