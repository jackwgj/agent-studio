/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.knowledge;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 功能描述
 *
 * @since 2024-04-23
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileExtract {
    @JsonProperty("parse_conf")
    private ParseConf parseConf;

    @JsonProperty("split_conf")
    private SplitConf splitConf;
}
