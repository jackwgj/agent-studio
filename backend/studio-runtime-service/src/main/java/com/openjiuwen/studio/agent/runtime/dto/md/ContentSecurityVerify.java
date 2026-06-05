/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2028. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto.md;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @ClassName : ContentSecurityVerify
 * @Description :
 * @Date : Created in 2024/5/21 11:32
 * @Version : V1.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ContentSecurityVerify {

    /**
     * 请求体开启校验,默认校验
     */
    @JsonProperty("is_request_verify")
    @JSONField(name = "is_request_verify")
    private Boolean isRequestVerify;

    /**
     * 返回体开启校验,默认校验
     */
    @JsonProperty("is_response_verify")
    @JSONField(name = "is_response_verify")
    private Boolean isResponseVerify;
}
