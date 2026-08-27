/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.conversation.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.InputStream;

/** 当前用户已获授权的对话正式产物文件流。 */
@Getter
@AllArgsConstructor
public class ConversationArtifactDownload {
    private final InputStream content;

    private final String fileName;

    private final String mediaType;

    private final Long size;
}
