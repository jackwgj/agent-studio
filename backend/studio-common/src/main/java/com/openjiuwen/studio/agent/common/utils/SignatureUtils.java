/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.utils;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.util.Base64;

@Slf4j
@Service
public class SignatureUtils {
    @Value("${export.signature.enable:true}")
    private boolean signatureEnable;

    @Value("${export.signature.key:}")
    private String secretKey;

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    public String signature(String data) {
        if (!signatureEnable) {
            return null;
        }
        return HmacSignature.generateHmac(data, secretKey, HMAC_ALGORITHM);
    }

    public void verifySignature(String data, String signature) {
        if (!signatureEnable) {
            log.info("Signature verification is disabled");
            return;
        }
        if (StringUtils.isEmpty(signature)) {
            log.error("The signature is empty.");
            throw new AgentStudioException(StudioError.VERIFY_SIGNATURE_FAILED);
        }
        boolean isVerifySuccess = MessageDigest.isEqual(Base64.getDecoder().decode(signature), Base64.getDecoder()
            .decode(signature(data)));
        if (!isVerifySuccess) {
            log.error("The signature verification failed");
            throw new AgentStudioException(StudioError.VERIFY_SIGNATURE_FAILED);
        }
    }

}
