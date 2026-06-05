/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models.knowledge;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.common.dto.knowledge.ParseConf;
import com.openjiuwen.studio.agent.common.dto.knowledge.SplitConf;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 功能描述
 *
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
