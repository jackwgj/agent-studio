/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.common.exception.model;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Data;

@Data
public class ErrorRsp {
    @JsonAlias({"code", "errorCode", "error_code"})
    private String errorCode;

    @JsonAlias({"message", "massage", "msg", "massages", "data", "result", "reason", "error_msg"})
    private String errorMsg;
}
