/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.knowledge;

import lombok.Getter;

@Getter
public enum LakeSearchQueryScopeEnum {
    DOC("doc"),

    KEYWORD("keyword"),

    MIX("mix"),

    FAQ("faq");

    private final String name;

    LakeSearchQueryScopeEnum(String name) {
        this.name = name;
    }
}
