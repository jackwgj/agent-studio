/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.workflow.resource.model;

import com.openjiuwen.studio.agent.manager.enums.ResourceTypeEnum;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.apache.commons.lang3.StringUtils;

@Data
@EqualsAndHashCode(callSuper = false)
public class ImportInfo extends ExportInfo {

    private String targetProjectId;

    private String targetWorkspaceId;

    private String targetWorkspaceName;

    private String targetDomainId;

    private String creator;

    private String creatorId;

    private int order;

    public int getOrder() {
        return StringUtils.isNotEmpty(this.getResourceType()) ? ResourceTypeEnum.fromValue(this.getResourceType())
            .getOrder() : 0;
    }

    public Integer getLevel() {
        return this.getResourceLevel() != null ? this.getResourceLevel() : 1;
    }

}
