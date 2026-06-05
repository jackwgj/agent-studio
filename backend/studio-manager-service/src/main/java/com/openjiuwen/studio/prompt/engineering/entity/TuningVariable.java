/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 调优模板记录时的变量 在数据库中只保存key,name,value
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TuningVariable {
    private String key;

    private String name;

    private String value;
}
