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
 * sku控制项
 */
@Data
@NoArgsConstructor
@TableName("t_common_license")
public class LicenseEntity {

    @TableId
    private String id;

    private String resourceId;

    private String skuCode;

    private String attrCode;

    private String status;

    /**
     * 当前值
     */
    private String currentValue;

    /**
     * 最大值
     */
    private String maxValue;

    /**
     * license控制类型：1（布尔型）、2（计数型）
     */
    private String type;

    private Timestamp createdDate;

    private String createdByUserId;

    private Timestamp lastUpdatedDate;

    private String lastUpdatedByUserId;

    private String domainId;
}
