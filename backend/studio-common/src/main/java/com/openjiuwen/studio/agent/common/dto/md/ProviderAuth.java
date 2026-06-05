/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.md;

public interface ProviderAuth {
    String type();

    void validCheck();

    /**
     * 获取加密后的JSON字符串格式数据
     *
     * @return {}
     */
    String convertToEncryptStr();

    String authInfoTemplate();

    /**
     * 获取掩码的JSON字符串
     *
     * @param decrypt 是否需要解密
     * @return {}
     */
    String convertToMaskStr(boolean decrypt);

    default void setAuthId(String id) {
    }

    default String getAuthId() {
        return "";
    }
}
