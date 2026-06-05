/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.common.dto.ErrorDetail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KmsDatakeyResponse {

    @JsonProperty("kms_info_list")
    private List<KmsInfoItem> kmsInfoList;

    private ErrorDetail error;

    @JsonProperty("key_id")
    private String keyId;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KmsInfoItem {
        /**
         * 密钥ID
         */
        @JsonProperty("id")
        private String id;

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
    }
}
