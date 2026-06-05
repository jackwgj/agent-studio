/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.common.service.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@TableName("t_ops_tenant_config")
public class TenantConfigEntity {

    /**
     * IAM账号ID
     */
    @TableId
    private String domainId;

    /**
     * 项目ID
     */
    private String projectId;
    
    /**
     * 日志组ID
     */
    private String logGroupId;
    
    /**
     * 日志流ID
     */
    private String logStreamId;
    
}
