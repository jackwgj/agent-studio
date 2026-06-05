/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.models;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class JiuwenFields {
    private List<Map<String, Object>> inputs = new ArrayList<>();

    private List<Map<String, Object>> outputs = new ArrayList<>();
}
