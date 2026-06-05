/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.foundation.connection.model.ability;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.util.List;

@Data
public class RetrieveSubAbility {
    @JsonProperty("search_mode_list")
    private List<SearchMode> searchModeList;
}
