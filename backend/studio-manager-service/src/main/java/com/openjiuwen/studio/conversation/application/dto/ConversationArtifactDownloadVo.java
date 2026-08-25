/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.conversation.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 对话正式产物的临时下载信息。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationArtifactDownloadVo {
    @JsonProperty("download_url")
    private String downloadUrl;

    @JsonProperty("expires_in")
    private Long expiresIn;
}
