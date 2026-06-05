/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.mcp.model.apig;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.util.List;

/**
 * APIG 查询专享版实例列表响应类
 *
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApigApiGroupDetailResp {
    /**
     * API分组名称
     */
    @JsonProperty("name")
    private String name;

    /**
     * 编号
     */
    @JsonProperty("id")
    private String id;

    /**
     * 状态 1： 有效
     */
    @JsonProperty("status")
    private int status;


    /**
     * 系统默认分配的子域名
     */
    @JsonProperty("sl_domain")
    private String slDomain;

    /**
     * 创建时间
     */
    @JsonProperty("register_time")
    private String registerTime;

    /**
     * 最近修改时间
     */
    @JsonProperty("update_time")
    private String updateTime;

    /**
     * 是否已上架云商店：
     * 1：已上架
     * 2：未上架
     * 3：审核中
     */
    @JsonProperty("on_sell_status")
    private int onSellStatus;

    /**
     * 分组上绑定的独立域名列表
     */
    @JsonProperty("url_domains")
    private List<UrlDomain> urlDomains;

    /**
     * 调试域名是否可以访问，true表示可以访问，false表示禁止访问 缺省值：true
     */
    @JsonProperty("sl_domain_access_enabled")
    private boolean slDomainAccessEnabled;

    /**
     * 系统默认分配的子域名列表
     */
    @JsonProperty("sl_domains")
    private List<String> slDomains;

    /**
     * 描述
     */
    @JsonProperty("remark")
    private String remark;

    /**
     * 流控时长内分组下的API的总访问次数限制，默认不限，请根据服务的负载能力自行设置
     */
    @JsonProperty("call_limits")
    private int callLimits;

    /**
     * 流控时长
     */
    @JsonProperty("time_interval")
    private int timeInterval;

    /**
     * 是否为默认分组
     */
    @JsonProperty("is_default")
    private int isDefault;

    /**
     * 流控的时间单位
     */
    @JsonProperty("time_unit")
    private String timeUnit;

    /**
     * 分组版本
     */
    @JsonProperty("version")
    private String version;

    /**
     * 分组归属的集成应用编号。
     * 分组版本V2时必填。
     */
    @JsonProperty("roma_app_id")
    private String romaAppId;

    /**
     * 分组归属的集成应用名称
     */
    @JsonProperty("roma_app_name")
    private String romaAppName;
}
