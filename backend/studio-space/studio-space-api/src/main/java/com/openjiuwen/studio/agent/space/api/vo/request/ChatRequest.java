/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.api.vo.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 对话请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 发起对话的内容
     */
    @NotBlank
    private String query;

    /**
     * agent类型，当前只支持工作流/单/多智能体
     */
    @NotNull
    @Max(5)
    @Min(2)
    private Integer agentType;

    /**
     * 对应的agentId
     */
    @NotBlank
    private String agentId;

    /**
     * 对话中引用的文件Id
     */
    @Size(max = 5)
    private List<String> fileId;

    private String conversationId;
}
