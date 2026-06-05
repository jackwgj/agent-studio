/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.api.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.experimental.Accessors;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * 任务信息传输类
 */
@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgentBuilderTaskInfoVo {
    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    /**
     * 任务类型
     * 1：默认问答；2：用户agent
     */
    @JsonProperty("type")
    private Integer type;

    /**
     * 用户agent_id列表
     */
    @JsonProperty("agent_list")
    private List<String> agentList;

    /**
     * 运行类型
     * 0：探索模式；1：规划模式
     */
    @JsonProperty("run_mode")
    private Integer runMode;

    /**
     * 任务状态
     * 0：未开始；1：运行中；2：等待用户输入；3：触发暂停；4：暂停，等待用户输入；5：任务正常结束，等待用户输入；6：任务删除；7：任务完成，寿终正寝；8：运行失败，意外身亡；9：触发手动终止；10：手动终止完成；
     */
    @JsonProperty("status")
    private Integer status;

    @JsonProperty("create_time")
    private Timestamp createTime;

    @JsonProperty("update_time")
    private Timestamp updateTime;

    @JsonProperty("display_info")
    private AgentBuilderTaskDisplayVo displayInfo;

    @JsonProperty("extra_agent_config")
    private Map<String, Object> extraAgentConfig;

    @JsonProperty("scheduled")
    private Boolean scheduled;
}