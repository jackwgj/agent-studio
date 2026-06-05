/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import org.springframework.data.annotation.Id;

/**
 * 任务相关文件表实体类
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@TableName(value = "ws_agent_builder_file_def", autoResultMap = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class AgentBuilderFileEntity extends BaseProperties {
    /**
     * id
     */
    @Id
    @TableField("id")
    private String id;

    /**
     * 来源文件id,针对重复引用的文件
     */
    @TableField("source_id")
    private String sourceId;

    /**
     * kooPageId存出对应的ID
     */
    @TableField("koo_page_id")
    private String kooPageId;

    /**
     * 文件名称
     */
    @TableField("name")
    private String name;

    /**
     * 所属任务id
     */
    @TableField("task_id")
    private String taskId;

    /**
     * 文件相对路径
     */
    @TableField("uri")
    private String uri;

    /**
     * obs路径
     */
    @TableField("url")
    private String url;

    /**
     * 文件类型
     * 0：用户上传；1：流程生成; 2:运行时日志 3: 重复引用
     */
    @TableField("type")
    private int type;

    /**
     * 文件内容 Base64编码
     */
    @TableField("content")
    private String content;

    /**
     * 文件存储类型 0：数据库；1：obs
     */
    @TableField("storage_type")
    private int storageType;

    /**
     * 所属任务id
     */
    @TableField("message_id")
    private String messageId;

    /**
     * 额外的信息
     */
    @TableField("extra_config")
    private String extraConfig;
}
