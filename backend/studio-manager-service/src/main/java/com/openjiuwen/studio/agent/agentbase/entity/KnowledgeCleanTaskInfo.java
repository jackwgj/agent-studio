/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.io.Serializable;

/**
 * 清理租户任务信息表
 *
 * @TableName t_knowledge_clean_task_info
 */
@TableName(value = "t_knowledge_clean_task_info")
@Data
public class KnowledgeCleanTaskInfo implements Serializable {
    /**
     * 任务ID
     */
    @TableId
    private String taskId;

    /**
     * DOMAINID
     */
    private String domainId;

    /**
     * 任务状态
     */
    private String status;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 任务完成时间
     */
    private Long finishTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}