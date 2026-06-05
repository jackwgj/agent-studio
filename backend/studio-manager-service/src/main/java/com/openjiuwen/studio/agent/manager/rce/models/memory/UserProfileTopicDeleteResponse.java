/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models.memory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户画像主题删除响应
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileTopicDeleteResponse {

    private boolean success;
}
