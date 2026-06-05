/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto.md;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.common.dto.md.ProviderAuth;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.AgentStringUtils;
import com.openjiuwen.studio.agent.common.utils.CryptoUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class ApiKeyAuthInfo implements ProviderAuth {
    @JsonProperty("API Key")
    private String apiKey;

    private String authId;

    @Override
    public String type() {
        return "API_KEY";
    }

    @Override
    public void validCheck() {
        if (StringUtils.isAnyBlank(apiKey)) {
            log.error("API_KEY auth config is invalid.");
            throw new AgentStudioException(StudioError.MD_AUTH_INFO_API_INVALID);
        }
    }

    @Override
    public String authInfoTemplate() {
        return "{\"API Key\":\"\"}";
    }

    @Override
    public String convertToEncryptStr() {
        return StringUtils.join("{\"API Key\":\"", CryptoUtils.encrypt(apiKey), "\"}");
    }

    @Override
    public String convertToMaskStr(boolean decrypt) {
        if (decrypt) {
            return StringUtils.join("{\"API Key\":\"", AgentStringUtils.mask(CryptoUtils.decrypt(apiKey)), "\"}");
        } else {
            return StringUtils.join("{\"API Key\":\"", AgentStringUtils.mask(apiKey), "\"}");
        }
    }
}
