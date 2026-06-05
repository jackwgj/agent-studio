/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.foundation.base.mapping;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 表示整个错误码映射配置的根对象。
 * 对应 error-mapping.yml 文件的顶层结构。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MappingConfig {
    @JsonProperty("mapping_rules")
    private List<MappingRule> mappingRules;
}