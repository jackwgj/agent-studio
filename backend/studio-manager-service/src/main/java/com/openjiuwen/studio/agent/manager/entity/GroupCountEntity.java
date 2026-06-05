/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 功能描述
 *
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupCountEntity {
    Integer num;

    String dimension;

    Date sequence;

    String type;
}
