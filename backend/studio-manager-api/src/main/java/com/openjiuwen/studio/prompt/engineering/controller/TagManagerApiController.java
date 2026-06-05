/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.controller;

import com.openjiuwen.studio.agent.common.utils.ResponseModel;
import com.openjiuwen.studio.prompt.engineering.dto.ListTagsQo;
import com.openjiuwen.studio.prompt.engineering.dto.TagListVo;
import com.openjiuwen.studio.prompt.engineering.service.ITagManagerService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * TagManager controller
 */
@RestController

public class TagManagerApiController implements TagManagerApi {

    private static final Logger log = LoggerFactory.getLogger(TagManagerApiController.class);

    @Autowired
    private ITagManagerService tagManagerService;

    @Override
    public ResponseEntity<TagListVo> listTags(String projectId, ListTagsQo listTagsQo) {
        return ResponseModel.success(tagManagerService.listTags(projectId, listTagsQo));
    }
}
