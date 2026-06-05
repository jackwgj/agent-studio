/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.ros.service;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class CleanResource {
    private String resource;

    @JsonProperty("table_name")
    private String tableName;

    @JsonProperty("id_column")
    private String idColumn = "id";

    @JsonProperty("domain_column")
    private String domainColumn = "domain_id";
}
