/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TemplateDownloadVo {

    private String templateName;

    private String content;

    private String description;

    private String variables;

    private String tag;

    private String industry;

    private String creator;

    private String createdOn;

    private String updatedOn;
}
