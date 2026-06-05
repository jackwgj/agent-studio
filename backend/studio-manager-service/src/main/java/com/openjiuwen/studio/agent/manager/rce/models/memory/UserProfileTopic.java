/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models.memory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用户画像主题
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileTopic {

    private String topic;

    private List<UserProfileTag> tags;
}
