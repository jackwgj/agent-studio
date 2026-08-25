/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.conversation.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/** 对话附件的服务端可信对象引用，不接受临时下载 URL。 */
@Data
public class ConversationInputFileRef {
    @JsonProperty("object_key")
    private String objectKey;

    @JsonProperty("file_name")
    private String fileName;

    private long size;

    private String checksum;
}
