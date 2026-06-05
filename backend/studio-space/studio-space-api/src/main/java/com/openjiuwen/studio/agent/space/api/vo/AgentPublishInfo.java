package com.openjiuwen.studio.agent.space.api.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * agent 发布信息基本信息
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Accessors(chain = true)
public class AgentPublishInfo {

    @JsonProperty("agent_id")
    private String agentId;

    /**
     * 是否发布
     */
    @JsonProperty("publish_status")
    private boolean publishStatus;

    /**
     * agentStudio 前台快速访问地址
     */
    @JsonProperty("request_url")
    private String requestUrl;

    @JsonProperty("created_date")
    private Date createdDate;

}
