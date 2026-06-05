/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.entity;

import lombok.Data;

@Data
public class TemplateOneRow {

    private String templateName;

    private String content;

    private String description;

    private String tag;

    private String industry;

    private String vars;
}
