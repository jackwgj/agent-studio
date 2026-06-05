/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * t_wisestudio_oper_log
 *
 */
@Data
@TableName(value = "t_agent_space_oper_log", autoResultMap = true)
public class OperLogEntity {
    /**
     * 主键，log_id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 操作对象类型：产品product，服务service，标签tag，公告notice
     */
    private String objType;

    /**
     * 记录的操作对象ID字段名
     */
    private String objIdField;

    /**
     * 记录的操作对象ID值
     */
    private String objIdValue;

    /**
     * 操作类型：add新增，modify修改，delete删除，active上线，inactive下线
     */
    private String operType;

    /**
     * 操作IP
     */
    private String operIp;

    /**
     * 1:成功，0:失败
     */
    private String responseCode;

    /**
     * 操作内容
     */
    private String content;

    /**
     * 失败信息
     */
    private String responseMsg;

    /**
     * 操作人
     */
    private String operUser;

    /**
     * 操作时间
     */
    private LocalDateTime operTime;

    /**
     * 租户（组织）标识
     */
    private String domainId;

    /**
     * 1:成功，0:失败
     */
    private Integer successFlag;
}