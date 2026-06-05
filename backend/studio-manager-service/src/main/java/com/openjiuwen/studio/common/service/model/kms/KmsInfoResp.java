/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.common.service.model.kms;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class KmsInfoResp {

    @JsonProperty("kms_info_list")
    private List<KmsInfoItem> kmsInfoList;

    @JsonProperty("key_id")
    private String keyId;

    private ErrorDetail error;

    @Data
    @Builder
    public static class KmsInfoItem {
        /**
         * 密钥ID
         */
        @JsonProperty("id")
        private String id;

        /**
         * 密钥keyID
         */
        @JsonProperty("main_key_id")
        private String mainKeyId;

        /**
         * 明文
         */
        @JsonProperty("plain_text")
        private String plainText;

        /**
         * 密文
         */
        @JsonProperty("cipher_text")
        private String cipherText;

        // 加密时, 找到active=Y AND latest=Y的一条记录
        @JsonProperty("active")
        private String active;

        // 解密时, 要求active=Y
        @JsonProperty("latest")
        private String latest;

    }
}
