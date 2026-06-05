/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.service;

/**
 * OBS公共服务，后续Manager和Runtime的OBSService要归一
 *
 */
public interface CommonObsService {

    /**
     * 上传OBS文件
     *
     * @param objectKey 文件路径
     * @param content 文件内容
     */
    void putObject(String objectKey, String content);

    /**
     * 删除OBS文件
     *
     * @param objectKey 文件路径
     */
    void deleteObject(String objectKey);

    /**
     * 下载OBS文件
     *
     * @param objectKey 文件路径
     * @return 文件内容
     */
    String getObject(String objectKey);
}
