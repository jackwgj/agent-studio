package com.openjiuwen.studio.agent.space.api.vo.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 调用AgentStudio查询agent接口入参模型
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Accessors(chain = true)
public class AgentStudioQueryReq {

    private List<String> type;

    private String name;

    private String status = "published";

    @JsonProperty("workspace_id")
    private String workspaceId;

    private List<String> ids;

    private Integer offset;

    private Integer limit;

}
