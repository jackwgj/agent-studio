/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.service;

import com.openjiuwen.studio.prompt.engineering.dto.IndustryVo;

import java.util.List;

/**
 * IndustryManager service
 */

public interface IIndustryManagerService {

    /**
     * listIndustry
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param libraryType libraryType
     */
    List<IndustryVo> listIndustry(String projectId, String workspaceId, String libraryType);
}
