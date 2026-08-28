/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.conversation.application.dto;

import lombok.Builder;
import lombok.Data;

/** 对话专用上传返回值，仅包含可持久化的对象元数据。 */
@Data
@Builder
public class ConversationInputUploadVo {
    private String objectKey;
    private String fileName;
    private long size;
    private String checksum;
}
