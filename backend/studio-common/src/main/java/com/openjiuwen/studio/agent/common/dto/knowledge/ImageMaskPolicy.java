/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.knowledge;

import org.apache.commons.lang3.StringUtils;

/**
 * 图片信息在文本中的替换类型
 */
public enum ImageMaskPolicy {
    /**
     * 1. 保留图片ID，格式：{KI-imageId}
     */
    RETAIN_IMAGE_ID,

    /**
     * 2. 保留占位符，格式：{KI-N}，N为序号
     */
    RETAIN_PLACEHOLDER,

    /**
     * 3. 移除图片标签（替换为空字符串）
     */
    REMOVE_IMAGE;

    /**
     * 辅助方法：安全地将字符串转换为枚举，如果转换失败或输入为空，返回REMOVE_IMAGE
     *
     * @param policyName
     * @return
     */
    public static ImageMaskPolicy fromString(String policyName) {
        if (StringUtils.isBlank(policyName)) {
            return REMOVE_IMAGE;
        }
        try {
            return ImageMaskPolicy.valueOf(policyName);
        } catch (IllegalArgumentException e) {
            return REMOVE_IMAGE;
        }
    }
}