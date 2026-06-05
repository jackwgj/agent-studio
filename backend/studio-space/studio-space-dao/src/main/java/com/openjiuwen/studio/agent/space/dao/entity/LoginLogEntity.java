/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import org.springframework.data.annotation.Id;

@NoArgsConstructor
@AllArgsConstructor
@Data
@TableName(value = "t_agent_space_login_log", autoResultMap = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Builder
public class LoginLogEntity {
    /**
     * 主键
     */
    @Id
    @TableField("id")
    private String id;

    /**
     * 登录域名
     */
    @TableField("login_domain")
    private String loginDomain;

    /**
     * 用户sn
     */
    @TableField("user_id")
    private String userId;

    /**
     * 登录IP地址
     */
    @TableField("oper_ip")
    private String operIp;

    /**
     * 操作类型：登录login，登出logout
     */
    @TableField("oper_type")
    private String operType;

    /**
     * 登录/登出时间
     */
    @TableField("oper_time")
    private String operTime;

    /**
     * 会话标识，加密存储
     */
    @TableField("session_id")
    private String sessionId;

    /**
     * 登录的服务器IP
     */
    @TableField("service_ip")
    private String serviceIp;

    /**
     * 从哪个中心登录
     */
    @TableField("login_center")
    private String loginCenter;

    /**
     * 登出类型，包括：用户从应用平台主动登出，会话过期登出
     */
    @TableField("logout_type")
    private String logoutType;

    /**
     * 0：失败，1：成功
     */
    @TableField("success_flag")
    private Integer successFlag;

    /**
     * 失败场景原因
     */
    @TableField("content")
    private String content;

    /**
     * 租户标识
     */
    @TableField("domain_id")
    private String domainId;

}
