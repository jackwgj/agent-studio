/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.api.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Created by Fang Zhen on 2024/3/18.
 */
@Data
public class FragmentMemorySettings {

    Boolean enable;

    @JsonProperty("auto_update")
    Boolean autoUpdate;

}
