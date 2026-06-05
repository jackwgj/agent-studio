package com.openjiuwen.studio.agent.space.dao.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.sql.Timestamp;

@EqualsAndHashCode(callSuper = false)
@Data
@TableName(value = "ws_agent_task_resource", autoResultMap = true)
public class AgentTaskUrlEntity {
    @TableId
    private String id;

    @JsonProperty("agent_id")
    private String agentId;

    @JsonProperty("task_id")
    private String taskId;

    private Boolean isEnable = false;

    @JsonProperty("agent_uri")
    private String agentUri;

    @JsonProperty("web_uri")
    private String webUri;

    @JsonProperty("created_date")
    private Timestamp createdDate;

    @JsonProperty("last_updated_date")
    private Timestamp lastUpdatedDate;

    @JsonProperty("resource_id")
    private String resourceId;

}
