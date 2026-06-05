/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.md;

import com.openjiuwen.studio.agent.common.dto.md.ProviderAuth;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ModelAuthStorageServiceTest {
    @Test
    public void getProviderAuthTest() {
        ModelAuthStorageService service = new ModelAuthStorageService();

        ProviderAuth auth = service.getProviderAuth("authId", "API_KEY", "{}");
        Assertions.assertEquals("API_KEY", auth.type());

        auth = service.getProviderAuth("authId", "CUSTOM_APIKEY", "{}");
        Assertions.assertEquals("CUSTOM_APIKEY", auth.type());

        auth = service.getProviderAuth("authId", "NO_AUTH", "{}");
        Assertions.assertEquals("NO_AUTH", auth.type());
    }

    @Test
    public void getProviderAuth_UnsupportedType_ThrowsException() {
        ModelAuthStorageService service = new ModelAuthStorageService();

        try {
            service.getProviderAuth("authId", "NOT_EXIST", "{}");
            Assertions.fail("Expected AgentStudioException");
        } catch (AgentStudioException e) {
            Assertions.assertEquals(StudioError.METHOD_ARGUMENT_NOT_VALID, e.getErrorCode());
        }
    }
}
