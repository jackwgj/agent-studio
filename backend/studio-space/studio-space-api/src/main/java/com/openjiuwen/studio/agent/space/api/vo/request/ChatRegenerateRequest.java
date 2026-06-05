/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.api.vo.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 重新生成结果请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatRegenerateRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank
    private String conversationId;

    @NotBlank
    private String requestMsgId;
}
