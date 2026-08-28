/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.conversation.domain.model.valueobject;

import com.alibaba.fastjson2.annotation.JSONField;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件引用值对象
 */
@Data
@NoArgsConstructor
public class FileRef {
    /**
     * OBS key/url
     */
    @JSONField(name = "objectKey", alternateNames = {"key"})
    private String objectKey;

    /**
     * 上传时的原始文件名
     */
    private String fileName;

    /** 文件字节数。 */
    private Long size;

    /** 文件 MIME 类型。 */
    private String mediaType;

    /** 文件内容 SHA-256。 */
    private String checksum;

    /** 生成该正式产物的主执行 ID。 */
    private String executionId;

    public FileRef(String objectKey) {
        this(objectKey, null);
    }

    public FileRef(String objectKey, String fileName) {
        this(objectKey, fileName, null, null, null, null);
    }

    public FileRef(String objectKey, String fileName, Long size, String mediaType, String checksum,
                   String executionId) {
        this.objectKey = objectKey;
        this.fileName = fileName;
        this.size = size;
        this.mediaType = mediaType;
        this.checksum = checksum;
        this.executionId = executionId;
    }

}
