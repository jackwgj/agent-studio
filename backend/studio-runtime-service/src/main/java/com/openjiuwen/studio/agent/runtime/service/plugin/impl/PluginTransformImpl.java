/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.plugin.impl;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.runtime.constant.Constant;
import com.openjiuwen.studio.agent.runtime.dto.md.CustomIAMAuthInfo;
import com.openjiuwen.studio.agent.runtime.dto.md.HisIAMAuthInfo;
import com.openjiuwen.studio.agent.runtime.dto.md.SgovAuthInfo;
import com.openjiuwen.studio.agent.runtime.service.plugin.PluginTransform;

import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@Service
public class PluginTransformImpl implements PluginTransform {
    // 定义 IAM 地址的正则表达式
    private static final Pattern IAM_URL_PATTERN = Pattern.compile("^https://[^/]+/v3/auth/tokens$");

    @Override
    public HisIAMAuthInfo transformAuthInfo2HisIAMAuthInfo(SecurityScheme securityScheme) {
        HisIAMAuthInfo hisIAMAuthInfo = new HisIAMAuthInfo();
        hisIAMAuthInfo.setIamUrl(getExtensionValue(securityScheme.getExtensions(), Constant.OpenAPI.IAM_URL));
        hisIAMAuthInfo.setIamAccount(getExtensionValue(securityScheme.getExtensions(), Constant.OpenAPI.IAM_ACCOUNT));
        hisIAMAuthInfo.setIamSecret(getExtensionValue(securityScheme.getExtensions(), Constant.OpenAPI.IAM_SECRET));
        hisIAMAuthInfo.setIamProject(getExtensionValue(securityScheme.getExtensions(), Constant.OpenAPI.IAM_PROJECT));
        hisIAMAuthInfo.setIamEnterprise(getExtensionValue(securityScheme.getExtensions(), Constant.OpenAPI.IAM_ENTERPRISE));
        return hisIAMAuthInfo;
    }

    @Override
    public SgovAuthInfo transformAuthInfo2SgovAuthInfo(SecurityScheme securityScheme) {
        SgovAuthInfo sgovAuthInfo = new SgovAuthInfo();
        sgovAuthInfo.setSgovUrl(getExtensionValue(securityScheme.getExtensions(), Constant.OpenAPI.SGOV_URL));
        sgovAuthInfo.setAppId(getExtensionValue(securityScheme.getExtensions(), Constant.OpenAPI.APP_ID));
        sgovAuthInfo.setCredential(getExtensionValue(securityScheme.getExtensions(), Constant.OpenAPI.CREDENTIAL));
        return sgovAuthInfo;
    }

    @Override
    public CustomIAMAuthInfo transformAuthInfo2CustomIAMAuthInfo(SecurityScheme securityScheme) {
        CustomIAMAuthInfo customIAMAuthInfo = new CustomIAMAuthInfo();
        customIAMAuthInfo
                .setIamDomain(getExtensionValue(securityScheme.getExtensions(), Constant.OpenAPI.CUSTOM_IAM_DOMAIN));
        customIAMAuthInfo
                .setIamProject(getExtensionValue(securityScheme.getExtensions(), Constant.OpenAPI.CUSTOM_IAM_PROJECT));
        customIAMAuthInfo
                .setIamUser(getExtensionValue(securityScheme.getExtensions(), Constant.OpenAPI.CUSTOM_IAM_USER));
        customIAMAuthInfo.setIamPassword(
                getExtensionValue(securityScheme.getExtensions(), Constant.OpenAPI.CUSTOM_IAM_PASSWORD));
        customIAMAuthInfo.setIamAK(getExtensionValue(securityScheme.getExtensions(), Constant.OpenAPI.CUSTOM_IAM_AK));
        customIAMAuthInfo.setIamSK(getExtensionValue(securityScheme.getExtensions(), Constant.OpenAPI.CUSTOM_IAM_SK));

        String url = getExtensionValue(securityScheme.getExtensions(), Constant.OpenAPI.CUSTOM_IAM_URL);
        validIAMAddress(url);
        customIAMAuthInfo.setIamUrl(url);

        return customIAMAuthInfo;
    }

    private void validIAMAddress(String url) throws IllegalArgumentException {
        if (url == null || !IAM_URL_PATTERN.matcher(url).matches()) {
            log.error("Failed to validate IAM URL: {}", url);
            throw new AgentStudioException(StudioError.PLUGIN_IAM_AUTNEHTICATION_URL_CONFIG_ERROR);
        }
    }

    private String getExtensionValue(Map<String, Object> extensions, String key) {
        if (extensions == null || extensions.isEmpty()) {
            return "";
        }
        return extensions.get(key).toString();
    }
}
