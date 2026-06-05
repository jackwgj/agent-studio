/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.utils;


import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;
import com.openjiuwen.studio.agent.foundation.base.exception.ErrorCode;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.text.TextStringBuilder;

import java.util.regex.Pattern;

/**
 * 功能描述
 */
@Slf4j
public class IconNameCheckUtils {
    private static final String UUID_FILE_NAME_PATTERN
        = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}\\.[a-zA-Z]+$";

    private static final Pattern UUID_PATTERN = Pattern.compile(UUID_FILE_NAME_PATTERN);

    /**
     * 图标校验
     *
     * @param iconName 图标名称
     */
    public static void validaIconName(String iconName) {
        if (iconName.isEmpty()) {
            log.error("Icon name can not be empty");
            throw new AgentBaseException(ErrorCode.INVALID_ICON_NAME);
        }
        if (!UUID_PATTERN.matcher(new TextStringBuilder(iconName)).matches()) {
            log.error("Icon name [{}] is illegal!");
            throw new AgentBaseException(ErrorCode.INVALID_ICON_NAME);
        }
    }
}
