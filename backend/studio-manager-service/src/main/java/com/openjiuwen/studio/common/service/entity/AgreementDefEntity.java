/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.common.service.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 资源订购实例
 */
@Data
@NoArgsConstructor
@TableName("t_common_agreement_def")
public class AgreementDefEntity {

    @TableId
    private String id;

    private String version;

    private String privacyStatement;

    private String status;

    private Date createdDate;
}

