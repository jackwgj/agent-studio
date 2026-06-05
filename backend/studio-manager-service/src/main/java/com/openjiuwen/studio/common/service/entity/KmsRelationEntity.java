/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.common.service.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * 资源订购实例
 */
@Data
@NoArgsConstructor
@TableName("t_common_kms_relation")
public class KmsRelationEntity {

    private String domainId;

    private String mainKeyId;

    private String relatedPrimaryKey;

    private String type;

    private Timestamp createdDate;

    private String createdByUserId;

    private Timestamp lastUpdatedDate;

    private String lastUpdatedByUserId;
}
