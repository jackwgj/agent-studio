/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.api.vo;

import com.alibaba.fastjson2.JSONObject;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * Created by Fang Zhen on 2024/3/18.
 */
@Data
public class VariableSettings {
    @Size(max = 100)
    List<JSONObject> content;
}
