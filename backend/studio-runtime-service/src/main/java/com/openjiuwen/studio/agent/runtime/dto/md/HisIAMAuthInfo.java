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
public class HisIAMAuthInfo implements ProviderAuth {
    private String authId;

    private String iamAccount;

    private String iamSecret;

    private String iamProject;

    private String iamEnterprise;

    private String iamUrl;

    private String userId;

    private String scenarioUuid;

    @Override
    public String type() {
        return "HIS_IAM";
    }

    public HisIAMAuthInfo(String authInfo) {
        JSONObject obj = JSONObject.parseObject(authInfo);
        iamSecret = obj.getString("iamSecret");
        iamAccount = obj.getString("iamAccount");
        iamEnterprise = obj.getString("iamEnterprise");
        iamUrl = obj.getString("iamUrl");
        userId = obj.getString("userId");
        scenarioUuid = obj.getString("scenarioUuid");
        iamProject = obj.getString("iamProject");
    }

    @Override
    public void validCheck() {
        if (StringUtils.isAnyEmpty(iamEnterprise, iamProject, iamUrl)) {
            log.error("Required iam auth param is empty.Enterprise: {}, Project: {}, Url: {}", iamEnterprise,
                iamProject, iamUrl);
            throw new AgentStudioException(StudioError.MD_AUTH_INFO_INVALID);
        }

        if (StringUtils.isAnyEmpty(scenarioUuid, userId)) {
            log.error("Required scenarioUuid or userId auth param is empty.");
            throw new AgentStudioException(StudioError.MD_AUTH_INFO_INVALID);
        }

        if (StringUtils.isAnyEmpty(iamAccount, iamSecret)) {
            log.error("Required iam auth param is empty. account or  is empty.");
            throw new AgentStudioException(StudioError.MD_AUTH_INFO_INVALID);
        }
    }

    @Override
    public String convertToEncryptStr() {
        JSONObject obj = new JSONObject();
        obj.put("iamAccount", iamAccount);
        obj.put("iamSecret", CryptoUtils.encrypt(iamSecret));
        obj.put("iamProject", iamProject);
        obj.put("iamEnterprise", CryptoUtils.encrypt(iamEnterprise));
        obj.put("iamUrl", iamUrl);
        obj.put("userId", userId);
        obj.put("scenarioUuid", scenarioUuid);
        return obj.toJSONString();
    }

    @Override
    public String authInfoTemplate() {
        return "{\"iamAccount\":\"\",\"iamSecret\":\"\",\"iamProject\":\"\","
            + "\"iamEnterprise\":\"\",\"iamUrl\":\"\",\"userId\":\"\",\"scenarioUuid\":\"\"}";
    }

    @Override
    public String convertToMaskStr(boolean decrypt) {
        JSONObject obj = new JSONObject();
        String sec = decrypt ? CryptoUtils.decrypt(iamSecret) : iamSecret;
        obj.put("iamSecret", AgentStringUtils.mask(sec));
        obj.put("iamAccount", iamAccount);
        obj.put("iamProject", iamProject);

        String enterprise = decrypt ? CryptoUtils.decrypt(iamEnterprise) : iamEnterprise;
        obj.put("iamEnterprise", AgentStringUtils.mask(enterprise));
        obj.put("iamUrl", iamUrl);
        return obj.toJSONString();
    }
}
