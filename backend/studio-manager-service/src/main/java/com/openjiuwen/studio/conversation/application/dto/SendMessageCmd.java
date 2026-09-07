/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.conversation.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 发送消息命令（多轮对话入口，触发一轮运行）
 */
@Data
public class SendMessageCmd {
    /**
     * 用户输入
     */
    @JsonProperty("query")
    private String query;

    /**
     * 模型部署id（=t_model_service.ID），模型选择驱动 IR 选取
     */
    @JsonProperty("model_deployment_id")
    private String modelDeploymentId;
}
