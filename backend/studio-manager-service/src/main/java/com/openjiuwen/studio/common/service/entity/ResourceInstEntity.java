/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.common.service.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * 资源订购实例
 */
@Data
@NoArgsConstructor
@TableName("t_common_reource_inst")
public class ResourceInstEntity {

    @TableId
    private String resourceId;

    private String parentId;

    private String domainId;

    private String instanceId;

    private String subproductCode;

    private String skuCode;

    private String cbcOrderId;

    private String subproductId;

    private String cloudServiceType;

    private String regionId;

    private String projectId;

    private String status;

    private Timestamp createdDate;

    private String createdByUserId;

    private Timestamp lastUpdatedDate;

    private String lastUpdatedByUserId;
}
