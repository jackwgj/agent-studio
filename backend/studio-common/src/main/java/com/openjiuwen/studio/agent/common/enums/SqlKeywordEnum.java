/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.enums;

import lombok.Getter;

@Getter
public enum SqlKeywordEnum {
    WHERE(" WHERE "),
    ORDER_BY(" ORDER BY "),
    ASC(" ASC "),
    DESC(" DESC "),
    LIMIT(" LIMIT "),
    AND(" AND "),
    OR(" OR "),
    NULL(" (NULL) "),
    OFFSET(" OFFSET "),
    LEFT("LEFT(%s, %s) AS %s");

    private final String value;

    SqlKeywordEnum(String value) {
        this.value = value;
    }

}
