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
 * 消息表
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Accessors(chain = true)
@TableName(value = "ws_agent_builder_message_def", autoResultMap = true)
@EqualsAndHashCode(callSuper = true)
public class AgentBuilderMessageEntity extends BaseProperties {
    /**
     * 消息id
     */
    @Id
    @TableField("id")
    private String id;

    /**
     * 父消息ID，提问的消息id
     */
    @TableField("parent_id")
    private String parentId;

    /**
     * 关联的任务id
     */
    @TableField("task_id")
    private String taskId;

    /**
     * 消息类型
     * 1：用户；2：系统
     */
    @TableField("type")
    private int type;

    /**
     * 消息内容
     */
    @TableField("content")
    private String content;

    /**
     * 文件id列表
     */
    @TableField(value = "file_list", typeHandler = ArrayListTypeHandler.class)
    private List<String> fileList;

    /**
     * mcp的id列表
     */
    @TableField(value = "mcp_list", typeHandler = ArrayListTypeHandler.class)
    private List<String> mcpList;

    /**
     * 用户评分
     * 1-10
     */
    @TableField("rating")
    private int rating;
}