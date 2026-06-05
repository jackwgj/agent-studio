/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.common.service.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 资源订购实例
 */
@Data
@NoArgsConstructor
@TableName("t_common_agreement")
public class AgreementEntity {

    private String id;

    private String domainId;

    private String version;

    private String privacyStatement;

    private String agreeFlag;

    private Date createdDate;

    private String createdByUserId;

    private Date lastUpdatedDate;

    private String lastUpdatedByUserId;
}
