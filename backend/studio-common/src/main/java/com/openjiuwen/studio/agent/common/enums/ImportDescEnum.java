/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ImportDescEnum {

    NEW_RESOURCE("newResource", "新增资源"),
    UPDATE_RESOURCE("updateResource", "更新资源"),
    NEW_RESOURCE_VERSION("newResourceVersion", "新增资源版本"),
    RESOURCE_EXISTS("resourceExists", "资源已存在"),
    RESOURCE_MATCH("resourceMatch", "匹配到相关资源"),
    RESOURCE_NOT_MATCH("resourceNotMatch", "未匹配到相关资源"),
    ERROR("error", "异常错误");

    private final String code;

    private final String desc;
}
