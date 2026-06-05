/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.openjiuwen.studio.agent.space.dao.handler.ArrayListTypeHandler;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import org.springframework.data.annotation.Id;

import java.util.List;

/**
 * 任务表
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Accessors(chain = true)
@TableName(value = "ws_agent_builder_task_def", autoResultMap = true)
@EqualsAndHashCode(callSuper = true)
public class AgentBuilderTaskEntity extends BaseProperties {
    /**
     * 任务id
     */
    @Id
    @TableField("id")
    private String id;

    /**
     * 任务名称
     */
    @TableField("name")
    private String name;

    /**
     * agent类型
     * 1:内置智能体,2:工作流智能体;3单智能体;4多智能体;5:任务型工作流;6:DR
     */
    @TableField("agent_type")
    private int agentType;

    /**
     * 用户agent_id列表
     */
    @TableField(value = "agent_list", typeHandler = ArrayListTypeHandler.class)
    private List<String> agentList;

    /**
     * 运行类型
     * 0：探索模式；1：规划模式
     */
    @TableField("run_mode")
    private int runMode;

    /**
     * 任务状态
     * 0：未开始；1：运行中；2：等待用户输入；3：触发暂停；4：暂停，等待用户输入；5：任务正常结束，等待用户输入；6：任务删除；7：任务完成，寿终正寝；8：运行失败，意外身亡；9：触发手动终止；10：手动终止完成；
     */
    @TableField("status")
    private Integer status;

    /**
     * 任务在前端展示的附加信息
     * [json结构]is_pin：是否置顶任务（布尔值，false 表示未置顶，true 表示置顶显示）is_dnd：是否开启免打扰（布尔值，false 表示正常通知，true 表示关闭任务通知）
     */
    @TableField("display_info")
    private String displayInfo;

    /**
     * agent的额外配置
     * 兼容自定agent场景的额外配置
     */
    @TableField("extra_agent_config")
    private String extraAgentConfig;

    /**
     * 是否为定时任务
     */
    @TableField("scheduled")
    private boolean scheduled;

    /**
     * 存储类型
     * 0：DB；1：ES
     */
    @TableField("storage_type")
    private int storageType;

    /**
     * 运行方式
     * 0：AgentSpace；1：AgentWeb
     */
    @TableField("run_type")
    private int runType = 0;

    /**
     * 运行的agent
     * 由AgentWeb发起的请求，存储的是shortCode
     */
    @TableField("agent_id")
    private String agentId;
}
