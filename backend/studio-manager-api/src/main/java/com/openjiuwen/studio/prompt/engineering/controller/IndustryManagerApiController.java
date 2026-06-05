/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.controller;

import com.openjiuwen.studio.agent.common.utils.ResponseModel;
import com.openjiuwen.studio.prompt.engineering.dto.IndustryVo;
import com.openjiuwen.studio.prompt.engineering.service.IIndustryManagerService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * IndustryManager controller
 */
@RestController

public class IndustryManagerApiController implements IndustryManagerApi {

    private static final Logger log = LoggerFactory.getLogger(IndustryManagerApiController.class);

    @Autowired
    private IIndustryManagerService industryManagerService;

    @Override
    public ResponseEntity<List<IndustryVo>> listIndustry(String projectId, String workspaceId, String libraryType) {
        return ResponseModel.success(industryManagerService.listIndustry(projectId, workspaceId, libraryType));
    }
}
