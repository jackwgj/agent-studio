/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

import com.openjiuwen.studio.agent.common.dto.knowledge.FileUploadRsp;
import com.openjiuwen.studio.agent.manager.dto.TagInfo;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * CommonManagement service
 */

public interface ICommonManagementService {

    /**
     * listTags
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     */
    List<TagInfo> listTags(String projectId, String workspaceId);

    /**
     * systemSettings
     *
     * @param projectId projectId
     * @param service service
     */
    Object systemSettings(String projectId, String service);

    /**
     * uploadAvatar
     *
     * @param projectId projectId
     * @param file file
     */
    FileUploadRsp uploadAvatar(String projectId, MultipartFile file);
}