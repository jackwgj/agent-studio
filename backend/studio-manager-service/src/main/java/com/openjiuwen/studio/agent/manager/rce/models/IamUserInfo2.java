/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 查询IAM用户详情
 *
 */
@Data
@Accessors(chain = true)
public class IamUserInfo2 {
    /**
     * IAM用户是否启用。true表示启用，false表示停用，默认为true。
     */
    @JsonProperty("enabled")
    private boolean enabled;

    /**
     * IAM用户ID。
     */
    @JsonProperty("id")
    private String id;

    /**
     * IAM用户所属账号ID。
     */
    @JsonProperty("domain_id")
    private String domainId;

    /**
     * IAM用户名。
     */
    @JsonProperty("name")
    private String name;

    /**
     * IAM用户的资源链接信息。
     */
    @JsonProperty("links")
    private IamUserInfoLink links;

    /**
     * IAM用户在外部系统中的ID。
     */
    @JsonProperty("xuser_id")
    private String xuserId;

    /**
     * IAM用户在外部系统中的类型。
     */
    @JsonProperty("xuser_type")
    private String xuserType;

    /**
     * IAM用户手机号的国家码。
     */
    @JsonProperty("areacode")
    private String areacode;

    /**
     * IAM用户邮箱。
     */
    @JsonProperty("email")
    private String email;

    /**
     * IAM用户手机号。
     */
    @JsonProperty("phone")
    private String phone;

    /**
     * IAM用户密码状态。true：需要修改密码，false：正常。如果密码未设置，此字段可能不返回。
     */
    @JsonProperty("pwd_status")
    private boolean pwdStatus;

    /**
     * IAM用户密码创建时间。
     */
    @JsonProperty("pwd_create_time")
    private String pwdCreateTime;

    /**
     * IAM用户密码更新时间。
     */
    @JsonProperty("modify_pwd_time")
    private String modifyPwdTime;

    /**
     * IAM用户更新时间。
     */
    @JsonProperty("update_time")
    private String updateTime;

    /**
     * IAM用户创建时间。
     */
    @JsonProperty("create_time")
    private String createTime;

    /**
     * IAM用户最后登录时间或认证访问时间。
     */
    @JsonProperty("last_login_time")
    private String lastLoginTime;

    /**
     * IAM用户最后使用密码认证时间。
     */
    @JsonProperty("last_pwd_auth_time")
    private String lastPwdAuthTime;

    /**
     * IAM用户密码强度。结果为Low/Medium/Strong/None，分别表示密码强度低/中/高/无。
     */
    @JsonProperty("pwd_strength")
    private String pwdStrength;

    /**
     * IAM用户是否为账号。
     */
    @JsonProperty("is_domain_owner")
    private boolean isDomainOwner;

    /**
     * IAM用户访问方式。
     * default：默认访问模式，编程访问和管理控制台访问。
     * programmatic：编程访问。
     * console：管理控制台访问。
     */
    @JsonProperty("access_mode")
    private String accessMode;

    /**
     * IAM用户描述信息
     */
    @JsonProperty("description")
    private String description;
}
