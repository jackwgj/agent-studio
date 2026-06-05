/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.mcp;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TemporalCredential
 *
 * @Date: 2024/7/24 14:26
 * @Description: Iam临时ak/sk凭证
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel("Iam临时ak/sk凭证")
public class TemporalCredential {
    @JsonProperty("expires_at")
    @JSONField(name = "expires_at")
    @ApiModelProperty("ak/sk与security token的过期时间")
    private String expiresAt;

    @JsonProperty("access")
    @JSONField(name = "access")
    @ApiModelProperty("临时的ak")
    private String access;

    @JsonProperty("secret")
    @JSONField(name = "secret")
    @ApiModelProperty("临时的sk，缓存中的sk需要进行加密")
    private String secret;

    @JsonProperty("securitytoken")
    @JSONField(name = "securitytoken")
    @ApiModelProperty("加密后的临时ak/sk")
    private String securityToken;
}
