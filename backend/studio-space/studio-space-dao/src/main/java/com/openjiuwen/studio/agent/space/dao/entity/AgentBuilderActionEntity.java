/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
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
 * 动作表
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Accessors(chain = true)
@TableName(value = "ws_agent_builder_action_def", autoResultMap = true)
@EqualsAndHashCode(callSuper = true)
public class AgentBuilderActionEntity extends BaseProperties {
    /**
     * 动作id
     */
    @Id
    @TableField("id")
    private String id;

    /**
     * 关联的步骤id
     */
    @TableField("step_id")
    private String stepId;

    /**
     * 动作类型
     * step_start、reply、tool_use、pause
     */
    @TableField("type")
    private String type;

    /**
     * 工具操作类型
     * 当type为tool_use时，详细的类型，如：search、file、terminal
     */
    @TableField("tool_type")
    private String toolType;

    /**
     * 对话中展示内容
     */
    @TableField("content")
    private String content;

    /**
     * 侧边栏展示内容
     */
    @TableField("display_content")
    private String displayContent;

    /**
     * 侧边栏展示内容
     */
    @TableField("reasoning_content")
    private String reasoningContent;

    /**
     * 修改的计划
     */
    @TableField("plan_content")
    private String planContent;

    /**
     * 文件id列表
     */
    @TableField(value = "file_list", typeHandler = ArrayListTypeHandler.class)
    private List<String> fileList;

    /**
     * 是否结束
     */
    @TableField("finished")
    private int finished;
}