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
@TableName("t_common_license_record")
public class LicenseRecordEntity {

    @TableId
    private String id;

    private String skuCode;

    private String attrCode;

    private String resourceId;

    private String operType;

    private String quantity;

    private Timestamp createdDate;

    private String createdByUserId;

    private Timestamp lastUpdatedDate;

    private String lastUpdatedByUserId;

    private String domainId;
}
