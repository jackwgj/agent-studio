/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.entity.security;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.runtime.enums.IdentifyType;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * ContentIdentifyReq
 *
 */
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
public class ContentIdentifyReq {

    /**
     * domainId op账号付费时不需要传
     */
    @JsonProperty("domain_id")
    @Size(max = 100)
    private String domainId;

    @JsonProperty("model_id")
    private String modelId;

    /**
     * op账号付费时不需要传
     */
    @JsonProperty("project_id")
    @Size(max = 100)
    private String projectId;

    /**
     * 智能体id op账号付费时不需要传
     */
    @JsonProperty("resource_id")
    @Size(max = 100)
    private String resourceId;

    @JsonProperty("glossary_names")
    @Size(max = 100)
    private List<String> glossaryNames;

    @JsonProperty("white_glossary_names")
    @Size(max = 100)
    private List<String> whiteGlossaryNames;

    /**
     * 默认策略有：chat chat_response 还有自定义策略
     */
    @JsonProperty("scene_type")
    private String sceneType;

    /**
     * 0：表示文本审核
     * 1：表示图像审核
     */
    @JsonProperty("identify_type")
    private IdentifyType identifyType = IdentifyType.TEXT;

    /**
     * 文本内容
     */
    private Message message;

    /**
     * 图片信息
     */
    @JsonProperty("image_info")
    private ImageInfo imageInfo;

    /**
     * 音频信息
     */
    @JsonProperty("audio_info")
    private AudioInfo audioInfo;

    public ContentIdentifyReq(String domainId, String modelId, String text) {
        this.domainId = domainId;
        this.modelId = modelId;
        this.identifyType = IdentifyType.TEXT;
        this.message = new Message(text);
    }

    public ContentIdentifyReq(String domainId, String modelId, IdentifyType type, String content) {
        this.domainId = domainId;
        this.modelId = modelId;
        this.identifyType = type;
        switch (type) {
            case TEXT:
                this.message = new Message(content);
                break;
            case IMAGE:
                this.imageInfo = new ImageInfo().setImageUrl(content);
                break;
            case AUDIO:
                this.audioInfo = new AudioInfo().setAudioUrl(content);
                break;
        }
    }
}
