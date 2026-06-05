/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
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
public class CustomIAMAuthInfo implements ProviderAuth {
    private String iamProject;

    private String iamPassword;

    private String iamDomain;

    private String iamUrl;

    private String iamUser;

    private String iamAK;

    private String iamSK;

    private String authId;

    public CustomIAMAuthInfo(String authInfo) {
        JSONObject obj = JSONObject.parseObject(authInfo);
        iamAK = obj.getString("iamAK");
        iamSK = obj.getString("iamSK");

        iamProject = obj.getString("iamProject");
        iamPassword = obj.getString("iamPassword");
        iamDomain = obj.getString("iamDomain");
        iamUrl = obj.getString("iamUrl");
        iamUser = obj.getString("iamUser");
    }

    @Override
    public String type() {
        return "CUSTOM_IAM";
    }

    @Override
    public void validCheck() {
        if (StringUtils.isAnyEmpty(iamProject, iamDomain, iamUrl)) {
            log.error("Required iam auth param is empty.");
            throw new AgentStudioException(StudioError.MD_AUTH_INFO_INVALID);
        }
        if (StringUtils.isAnyEmpty(iamAK, iamSK) && StringUtils.isAnyEmpty(iamUser, iamPassword)) {
            log.error("Required iam auth param is empty. user or ak/sk is empty.");
            throw new AgentStudioException(StudioError.MD_AUTH_INFO_INVALID);
        }
    }

    @Override
    public String authInfoTemplate() {
        return "{\"iamProject\":\"\",\"iamPassword\":\"\",\"iamDomain\":\"\",\"iamUrl\":\"\",\"iamUser\":\"\"}";
    }

    @Override
    public String convertToEncryptStr() {
        JSONObject obj = new JSONObject();
        obj.put("iamProject", iamProject);
        obj.put("iamDomain", iamDomain);
        obj.put("iamUrl", iamUrl);
        if (StringUtils.isEmpty(iamPassword)) {
            obj.put("iamAK", iamAK);
            obj.put("iamSK", CryptoUtils.encrypt(iamSK));
        } else {
            obj.put("iamUser", iamUser);
            obj.put("iamPassword", CryptoUtils.encrypt(iamPassword));
        }
        return obj.toJSONString();
    }

    @Override
    public String convertToMaskStr(boolean decrypt) {
        JSONObject obj = new JSONObject();
        obj.put("iamProject", iamProject);
        obj.put("iamDomain", iamDomain);
        obj.put("iamUrl", iamUrl);
        if (StringUtils.isEmpty(iamSK)) {
            obj.put("iamUser", iamUser);
            String pwd = decrypt ? iamPassword : CryptoUtils.decrypt(iamPassword);
            obj.put("iamPassword", AgentStringUtils.mask(pwd));
        } else {
            obj.put("iamAK", iamAK);
            String sk = decrypt ? iamSK : CryptoUtils.decrypt(iamSK);
            obj.put("iamSK", AgentStringUtils.mask(sk));
        }
        return obj.toJSONString();
    }
}
