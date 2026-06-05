/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.mcp.model.apig;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * APIG 查询专享版实例列表响应类
 *
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApigBindDomainsResp {
    /**
     * 自定义域名。长度为0-255位的字符串，需要符合域名规范
     */
    @JsonProperty("url_domain")
    private String urlDomain;

    /**
     * 自定义域名的编号
     */
    @JsonProperty("id")
    private String id;


    /**
     * CNAME解析状态
     * 1: 未解析
     * 2: 解析中
     * 3: 解析成功
     * 4: 解析失败
     */
    @JsonProperty("status")
    private int status;

    /**
     * 最小ssl协议版本号。支持TLSv1.1或TLSv1.2
     */
    @JsonProperty("min_ssl_version")
    private String minSslVersion;

    /**
     * 是否开启http到https的重定向，false为关闭，true为开启，默认为false
     */
    @JsonProperty("is_http_redirect_to_https")
    private boolean isHttpRedirectToHttps;

    /**
     * 是否开启http到https的重定向，false为关闭，true为开启，默认为false
     */
    @JsonProperty("verified_client_certificate_enabled")
    private boolean verifiedClientCertificateEnabled;

    /**
     * 访问该域名绑定的http协议入方向端口
     */
    @JsonProperty("ingress_http_port")
    private int ingressHttpPort = 80;

    /**
     * 访问该域名绑定的https协议入方向端口
     */
    @JsonProperty("ingress_https_port")
    private int ingressHttpsPort = 443;

}
