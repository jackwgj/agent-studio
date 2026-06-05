/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto.md;

import com.openjiuwen.studio.agent.common.dto.md.ModelInvokeBase;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class VideoGenerationReq extends ModelInvokeBase {
    private VideoGenerationInput input;

    private VideoGenerationParameter parameters;
}
