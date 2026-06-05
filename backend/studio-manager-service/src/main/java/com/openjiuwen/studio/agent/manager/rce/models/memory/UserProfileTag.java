/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models.memory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户画像标签
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileTag {

    private String name;

    private String description;
}
