/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.model.security;

import lombok.Data;

import java.util.List;

/**
 * DMQMessage
 *
 */
@Data
public class DMQMessage {
    private String taskID;

    private Integer resultCode;

    private String resultMessage;

    /**
     * 音频审核DMQ判断结果字段，图片和文本不涉及
     */
    private String securityResult;

    private List<IdentifyResult> result;
}
