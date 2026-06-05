/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.mcp.model.apig;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.util.List;

/**
 * @description 查询api分组信息响应结构
 * @since 2025/6/4
 **/
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UrlDomain {
    /**
     * 域名编号
     */
    @JsonProperty("id")
    private String id;

    /**
     * 访问域名
     */
    @JsonProperty("domain")
    private String domain;

    /**
     * 域名cname状态：
     * 1：未解析
     * 2：解析中
     * 3：解析成功
     * 4：解析失败
     */
    @JsonProperty("cname_status")
    private int cnameStatus;

    /**
     * SSL证书编号
     */
    @JsonProperty("ssl_id")
    private String sslId;

    /**
     * SSL证书名称
     */
    @JsonProperty("ssl_name")
    private String sslName;

    /**
     * 最小ssl协议版本号。支持TLSv1.1或TLSv1.2
     */
    @JsonProperty("min_ssl_version")
    private String minSslVersion;

    /**
     * 是否开启客户端证书校验。只有绑定证书时，该参数才生效。当绑定证书存在trusted_root_ca时，默认开启；当绑定证书不存在trusted_root_ca时，默认关闭。
     */
    @JsonProperty("verified_client_certificate_enabled")
    private boolean verifiedClientCertificateEnabled;

    /**
     * 是否存在信任的根证书CA。当绑定证书存在trusted_root_ca时为true。
     */
    @JsonProperty("is_has_trusted_root_ca")
    private boolean isHasTrustedRootCa;

    /**
     * 访问该域名绑定的http协议入方向端口，-1表示无端口且协议不支持，可使用80默认端口，其他有效端口允许的取值范围为1024~49151，需为实例已开放的HTTP协议的自定义入方向端口。
     */
    @JsonProperty("ingress_http_port")
    private int ingressHttpPort;

    /**
     * 访问该域名绑定的https协议入方向端口，-1表示无端口且协议不支持，可使用443默认端口，其他有效端口允许的取值范围为1024~49151，需为实例已开放的HTTPS协议的自定义入方向端口。
     */
    @JsonProperty("ingress_https_port")
    private int ingressHttpsPort;

    /**
     * SSL证书列表。
     */
    @JsonProperty("ssl_infos")
    private List<SslInfo> sslInfos;
}
