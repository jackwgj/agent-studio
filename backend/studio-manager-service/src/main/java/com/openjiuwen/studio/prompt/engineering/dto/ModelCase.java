/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用例，每个都是完整的消息列表
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelCase{

    private List<Case> messages;

}
