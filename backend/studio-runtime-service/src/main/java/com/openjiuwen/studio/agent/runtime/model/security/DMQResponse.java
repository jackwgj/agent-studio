/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.model.security;

import lombok.Data;

import java.util.List;

/**
 * DMQResponse
 *
 */
@Data
public class DMQResponse {
    private String receipt;

    private List<DMQMessageVO> message;
}
