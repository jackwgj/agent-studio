/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.md;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.AgentStringUtils;
import com.openjiuwen.studio.agent.common.utils.CryptoUtils;
import com.openjiuwen.studio.agent.common.utils.JsonUtils;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.Map;

@Slf4j
@Getter
@Setter
@NoArgsConstructor
public class CustomApiKeyAuthInfo implements ProviderAuth {
    private Map<String, String> authInfo;

    private String authId;

    public CustomApiKeyAuthInfo(String authInfo) {
        this.authInfo = JsonUtils.decode(authInfo, new TypeReference<>() {});
    }

    public CustomApiKeyAuthInfo(Map<String, String> authInfo) {
        this.authInfo = authInfo;
    }

    @Override
    public String type() {
        return "CUSTOM_APIKEY";
    }

    @Override
    public void validCheck() {
        if (CollectionUtils.isEmpty(authInfo)) {
            log.error("Required custom api key param is empty.");
            throw new AgentStudioException(StudioError.MD_AUTH_INFO_INVALID);
        }
        for (Map.Entry<String, String> entry : authInfo.entrySet()) {
            if (StringUtils.isAnyBlank(entry.getKey(), entry.getValue())) {
                log.error("Required custom api key param is empty.");
                throw new AgentStudioException(StudioError.MD_AUTH_INFO_INVALID);
            }
        }
    }

    @Override
    public String convertToEncryptStr() {
        if (authInfo == null || authInfo.isEmpty()) {
            return "{}";
        }
        JSONObject jsonObject = new JSONObject();
        for (Map.Entry<String, String> entry : authInfo.entrySet()) {
            jsonObject.put(entry.getKey(), CryptoUtils.encrypt(entry.getValue()));
        }
        return jsonObject.toJSONString();
    }

    @Override
    public String authInfoTemplate() {
        return "{\"\":\"\"}";
    }

    @Override
    public String convertToMaskStr(boolean decrypt) {
        if (authInfo == null || authInfo.isEmpty()) {
            return "{}";
        }
        JSONObject jsonObject = new JSONObject();
        for (Map.Entry<String, String> entry : authInfo.entrySet()) {
            String value = decrypt ? CryptoUtils.decrypt(entry.getValue()) : entry.getValue();
            jsonObject.put(entry.getKey(), AgentStringUtils.mask(value));
        }
        return jsonObject.toJSONString();
    }
}
