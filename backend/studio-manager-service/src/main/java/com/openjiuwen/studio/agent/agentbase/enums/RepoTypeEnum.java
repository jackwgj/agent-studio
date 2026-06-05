/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.enums;

import lombok.Getter;

import org.apache.commons.lang3.Strings;

@Getter
public enum RepoTypeEnum {

    SHARE("share"),
    EXCLUSIVE("exclusive");

    private final String value;

    RepoTypeEnum(String value) {
        this.value = value;
    }

    public static RepoTypeEnum fromValue(String value) {
        for (RepoTypeEnum repoTypeEnum : RepoTypeEnum.values()) {
            if (Strings.CS.equals(repoTypeEnum.getValue(), value)) {
                return repoTypeEnum;
            }
        }
        return null;
    }
}
