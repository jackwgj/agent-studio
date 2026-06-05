/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.dto.md;

import com.alibaba.fastjson.JSONObject;
import com.openjiuwen.studio.agent.common.dto.md.ProviderAuth;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.AgentStringUtils;
import com.openjiuwen.studio.agent.common.utils.CryptoUtils;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

@Slf4j
@Getter
@Setter
@NoArgsConstructor
public class SgovAuthInfo implements ProviderAuth {
    private String authId;

    private String appId;

    private String credential;

    private String sgovUrl;

    private String userId;

    private String scenarioUuid;

    @Override
    public String type() {
        return "SGOV";
    }

    public SgovAuthInfo(String authInfo) {
        JSONObject obj = JSONObject.parseObject(authInfo);
        sgovUrl = obj.getString("sgovUrl");
        credential = obj.getString("credential");
        scenarioUuid = obj.getString("scenarioUuid");
        userId = obj.getString("userId");
        appId = obj.getString("appId");
    }

    @Override
    public void validCheck() {
        if (StringUtils.isAnyEmpty(appId, sgovUrl)) {
            log.error("Required iam auth param is empty. AppId: {}, SgovUrl: {}", appId, sgovUrl);
            throw new AgentStudioException(StudioError.MD_AUTH_INFO_INVALID);
        }

        if (StringUtils.isAnyEmpty(scenarioUuid)) {
            log.error("Required scenarioUuid auth param is empty.");
            throw new AgentStudioException(StudioError.MD_AUTH_INFO_INVALID);
        }

        if (StringUtils.isEmpty(credential)) {
            log.error("Required iam auth param: credential is empty.");
            throw new AgentStudioException(StudioError.MD_AUTH_INFO_INVALID);
        }
    }

    @Override
    public String convertToEncryptStr() {
        return String.format(
            "{\"appId\":\"%s\",\"credential\":\"%s\",\"sgovUrl\":\"%s\",\"userId\":\"%s\",\"scenarioUuid\":\"%s\"}",
            appId, StringUtils.defaultString(CryptoUtils.encrypt(credential)), sgovUrl, userId == null ? "" : userId,
            scenarioUuid == null ? "" : scenarioUuid);
    }

    @Override
    public String authInfoTemplate() {
        return "{\"appId\":\"\",\"credential\":\"\",\"sgovUrl\":\"\",\"userId\":\"\",\"scenarioUuid\":\"\"}";
    }

    @Override
    public String convertToMaskStr(boolean decrypt) {
        String cre = decrypt ? credential : CryptoUtils.decrypt(credential);
        return String.format(
            "{\"appId\":\"%s\",\"credential\":\"%s\",\"sgovUrl\":\"%s\",\"userId\":\"%s\",\"scenarioUuid\":\"%s\"}",
            appId, StringUtils.defaultString(AgentStringUtils.mask(cre)), sgovUrl, userId == null ? "" : userId,
            scenarioUuid == null ? "" : scenarioUuid);
    }
}
