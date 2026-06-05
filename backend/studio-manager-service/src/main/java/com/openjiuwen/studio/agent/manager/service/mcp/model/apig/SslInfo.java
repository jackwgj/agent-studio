/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.mcp.model.apig;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * @description 查询api分组信息响应结构
 * @since 2025/6/4
 **/
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SslInfo {
    /**
     * SSL证书编号。
     */
    @JsonProperty("ssl_id")
    private String sslId;

    /**
     * SSL证书名称。
     */
    @JsonProperty("ssl_name")
    private String sslName;

    /**
     * 证书算法类型：
     * RSA
     * ECC
     * SM2
     */
    @JsonProperty("algorithm_type")
    private String algorithmType;

    /**
     * 最小ssl协议版本号。支持TLSv1.1或TLSv1.2
     */
    @JsonProperty("min_ssl_version")
    private String minSslVersion;

    /**
     * 证书可见范围： instance：当前实例  global：全局  缺省值：global
     */
    @JsonProperty("type")
    private String type;
}
