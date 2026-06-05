/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.md.adapter.req;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.runtime.exception.OpenAiHttpException;
import com.openjiuwen.studio.agent.runtime.http.HttpResponse;
import com.openjiuwen.studio.agent.runtime.http.SseEmitterUTF8;
import com.openjiuwen.studio.agent.runtime.model.ModelApiLog;
import com.openjiuwen.studio.agent.runtime.service.md.RuntimeModelApiLogService;
import com.openjiuwen.studio.agent.runtime.util.TestUtil;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

class AbstractRequestAdapterTest {
    private AbstractRequestAdapter adapter;

    @BeforeEach
    public void beforeEach() {
        adapter = new OpenApiRequestAdapter();

        RuntimeModelApiLogService apiLogService = Mockito.mock(RuntimeModelApiLogService.class);
        ReflectionTestUtils.setField(adapter, "apiLogService", apiLogService);
        Mockito.doNothing().when(apiLogService).saveModelApiLog(Mockito.any(ModelApiLog.class));
    }

    @Test
    public void streamResBodyHandlerTest() {
        HttpResponse<String> response = createResponse();

        ModelApiLog apiLog = new ModelApiLog();
        adapter.streamResBodyHandler(new SseEmitterUTF8(1000L), apiLog, "", response);

    }

    private HttpResponse<String> createResponse() {
        HttpResponse<String> response = new HttpResponse<>();
        response.setStatusCode(200);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "123");
        response.setHeaders(headers);

        headers.put("Content-Type", "stream");

        String content = TestUtil.getStringFromFile("classpath:stream/hello.text");

        InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        response.setStreamResponse(inputStream);
        return response;
    }

    @Test
    public void requestExceptionHandlerTest() {
        AgentStudioException agentStudioException = new AgentStudioException(StudioError.MD_FREE_MODEL_TOKENS_USED_UP);
        AgentStudioException ans = adapter.requestExceptionHandler(agentStudioException);
        Assertions.assertEquals(agentStudioException.getErrorCode(), ans.getErrorCode());

        ans = adapter.requestExceptionHandler(new OpenAiHttpException(409, "error"));
        Assertions.assertEquals(StudioError.MD_THIRD_MODEL_FAIL, ans.getErrorCode());

        ans = adapter.requestExceptionHandler(new RuntimeException("error"));
        Assertions.assertEquals(StudioError.UNEXPECTED_ERROR, ans.getErrorCode());
    }
}
