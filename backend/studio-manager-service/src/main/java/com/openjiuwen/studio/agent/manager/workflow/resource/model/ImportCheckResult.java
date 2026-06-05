/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.workflow.resource.model;

import com.openjiuwen.studio.agent.common.enums.ImportDescEnum;

import lombok.Data;

@Data
public class ImportCheckResult {

    private String id;

    private String name;

    private String type;

    private String description;

    private Boolean status;

    private Boolean prohibited = false;

    private ImportDescEnum importDesc;

}
