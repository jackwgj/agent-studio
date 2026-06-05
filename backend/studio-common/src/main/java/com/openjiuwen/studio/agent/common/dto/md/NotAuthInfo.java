/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.md;

public class NotAuthInfo implements ProviderAuth {
    @Override
    public void validCheck() {
    }

    @Override
    public String type() {
        return "NO_AUTH";
    }

    @Override
    public String authInfoTemplate() {
        return "{}";
    }

    @Override
    public String convertToEncryptStr() {
        return "{}";
    }

    @Override
    public String convertToMaskStr(boolean decrypt) {
        return "{}";
    }
}
