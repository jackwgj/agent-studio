package com.openjiuwen.studio.agent.space.api.vo.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 发布Agent到AgentSpace的请求参数模型
 */
@Data
@Accessors(chain = true)
public class PublishAgentRequest {

    /**
     * agent 唯一标识
     */
    @NotNull
    private String id;

    /**
     * agent 类型
     */
    @NotNull
    @JsonProperty("app_type")
    private String appType;

    /**
     * 来源
     */
    @JsonProperty("workspace_id")
    @NotNull
    private String workspaceId;

    /**
     * 创建人
     */
    @NotNull
    private String creator;
}