/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.openjiuwen.studio.agent.common.utils.JsonUtils;
import com.openjiuwen.studio.agent.manager.utils.BaseTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@MockitoSettings(strictness = Strictness.LENIENT)
public class JiuwenRuntimeI18nServiceTest extends BaseTest {
    @Autowired
    private JiuwenRuntimeI18nService jiuwenRuntimeI18nService;

    @Test
    public void createWorkflowErrorEventTest() {
        Map<String, Object> jsonObject = JsonUtils.decode("{\"code\":\"999999\",\"message\":\"We're\"}",
            new TypeReference<Map<String, Object>>() {});
        Map<String, Object> ans = jiuwenRuntimeI18nService.createRuntimeErrorEvent(jsonObject);
        Assertions.assertEquals(
            "{code=999999, message=We're, error_code=openjiuwen.999999, error_msg=系统内部错误, error_suggestion=请联系技术支持进行定位, error_reason=系统内部错误}",
            ans.toString());

        ans = jiuwenRuntimeI18nService.createRuntimeErrorEvent(
            JsonUtils.decode("{\"code\":\"0\",\"message\":\"\"}", new TypeReference<Map<String, Object>>() {}));
        Assertions.assertEquals(
            "{code=0, message=, error_code=openjiuwen.0, error_msg=, error_suggestion=, error_reason=}", ans.toString());

        ans = jiuwenRuntimeI18nService.createRuntimeErrorEvent(JsonUtils.decode(
            "{\"code\":\"0\",\"message\":\"ause=[100025]Model request error: {\\\"error_code\\\":\\\"openjiuwen.02501051\\\"\"}",
            new TypeReference<Map<String, Object>>() {}));
        Assertions.assertEquals(
            "{code=0, message=ause=[100025]Model request error: {\"error_code\":\"openjiuwen.02501051\", error_code=openjiuwen.02501051, error_msg=模型免费Token额度已用完, error_suggestion=请升级额度, error_reason=模型免费Token额度已用完}",
            ans.toString());
    }
}
