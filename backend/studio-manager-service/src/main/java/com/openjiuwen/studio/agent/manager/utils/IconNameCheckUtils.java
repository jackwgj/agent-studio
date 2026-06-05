/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.utils;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;

import java.util.regex.Pattern;

/**
 * 功能描述
 *
 */
public class IconNameCheckUtils {
    private static final String UUID_FILE_NAME_PATTERN =
        "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}\\.[a-zA-Z]+$";

    private static final Pattern UUID_PATTERN = Pattern.compile(UUID_FILE_NAME_PATTERN);

    /**
     * 图标校验
     *
     * @param iconName 图标名称
     */
    public static void validaIconName(String iconName) {
        if (iconName.isEmpty()) {
            throw new AgentStudioException(StudioError.INVALID_ICON_NAME);
        }
        if (!UUID_PATTERN.matcher(iconName).matches()) {
            throw new AgentStudioException(StudioError.INVALID_ICON_NAME);
        }
    }
}
