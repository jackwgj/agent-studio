/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.service;

import com.openjiuwen.studio.prompt.engineering.dto.ListTagsQo;
import com.openjiuwen.studio.prompt.engineering.dto.TagListVo;

/**
 * TagManager service
 */

public interface ITagManagerService {

    /**
     * listTags
     *
     * @param projectId projectId
     * @param listTagsQo listTagsQo
     */
    TagListVo listTags(String projectId, ListTagsQo listTagsQo);
}
