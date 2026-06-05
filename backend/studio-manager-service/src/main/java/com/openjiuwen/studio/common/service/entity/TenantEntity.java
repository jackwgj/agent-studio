/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.common.service.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@TableName("t_common_tenant")
public class TenantEntity {

    /**
     * IAM账号ID
     */
    @TableId
    private String domainId;

    /**
     * 租户名称
     */
    private String domainName;

    /**
     * 描述
     */
    private String description;

    /**
     * 创建时间
     */
    private Timestamp createdDate;

    /**
     * 最后修改时间
     */
    private Timestamp lastUpdatedDate;

    /**
     * 租户状态，枚举值：激活（Active）、退订（Deleted）
     */
    private String status;

    private String extInfo;

    /**
     * 租户类型 2B：B/2C：C
     */
    private String tenantType;
}
