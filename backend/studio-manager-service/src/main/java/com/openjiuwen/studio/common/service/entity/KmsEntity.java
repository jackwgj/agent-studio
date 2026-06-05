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
@TableName("t_common_kms_info")
public class KmsEntity {

    @TableId
    private String id;

    private String domainId;

    private String mainKeyId;

    private String mainKeyAlias;

    private String cipherText;

    private int version;

    private String active;

    private String latest;

    private Timestamp createdDate;

    private String createdByUserId;

    private Timestamp lastUpdatedDate;

    private String lastUpdatedByUserId;
}
