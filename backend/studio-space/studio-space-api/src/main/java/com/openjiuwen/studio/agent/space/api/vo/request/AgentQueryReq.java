package com.openjiuwen.studio.agent.space.api.vo.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.space.common.validator.StringEnumValue;
import com.openjiuwen.studio.agent.space.common.validator.ValidNormalString;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 前端查询Agent接口入参模型
 */
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Accessors(chain = true)
public class AgentQueryReq extends PageInfo {

    @JsonProperty("agent_type")
    @Max(5)
    @Min(2)
    private Integer agentType;

    @ValidNormalString
    private String name;

    /**
     * agent 状态 active上线，inactive下线
     */
    @StringEnumValue(values = {"active", "inactive"})
    private String status = "active";

    @JsonProperty("workspace_id")
    @ValidNormalString
    private String workspaceId;

    /**
     * 查询agentStudio发布的agent, 不传此参数。
     * 传 system 查询系统预制的agent(平台推荐)
     * 传 recent 查询最近使用的agent(最近使用)
     */
    @JsonProperty("query_type")
    @ValidNormalString
    private String queryType;

}
