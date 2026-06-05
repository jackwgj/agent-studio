/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.entity;

import lombok.Builder;
import lombok.Data;

/**
 * AK/SK
 *
 */
@Data
public class AkSkCredential {
    AccessKey access;

    AccessKey secret;

    public AkSkCredential(String ak, String sk) {
        access = new AccessKey(ak);
        secret = new AccessKey(sk);
    }

    /**
     * key
     *
     */
    @Builder
    @Data
    public static class AccessKey {
        String key;

        AccessKey(String key) {
            this.key = key;
        }
    }
}
