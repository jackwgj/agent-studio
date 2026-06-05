/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.enums;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;

import lombok.Getter;

import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

/**
 * agent类型枚举
 *
 */
public enum AgentType {
    AGENT("agent"),
    CONTROLLER("controller"),
    DEEPRESEARCH("deepresearch"),
    PLANEXECUTE("planexecute");

    @Getter
    private final String value;

    AgentType(String value) {
        this.value = value;
    }

    /**
     * 根据type获取枚举对象，如果为null则返回Agent
     *
     * @param type
     * @return
     */
    public static AgentType naValueOf(String type) {
        try {
            if (!StringUtils.isEmpty(type)) {
                return AgentType.valueOf(type.toUpperCase(Locale.ROOT));
            }
        } catch (Exception e) {
            throw new AgentStudioException(StudioError.UNSUPPORTED_AGENT_TYPE);
        }
        return AGENT;
    }
}
