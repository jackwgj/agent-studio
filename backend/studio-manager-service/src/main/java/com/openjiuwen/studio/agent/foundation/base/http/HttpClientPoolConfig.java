/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.foundation.base.http;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * httpClient连接池设置
 */
@Component
@ConfigurationProperties(prefix = "spring.http-client.pool")
@Data
public class HttpClientPoolConfig {
    /**
     * 连接池的最大连接数
     */
    private int maxTotalConnect;

    /**
     * 同路由的并发数
     */
    private int maxConnectPerRoute;

    /**
     * 客户端和服务器建立连接超时，默认2s
     */
    private int connectTimeout = 2000;

    /**
     * 指客户端从服务器读取数据包的间隔超时时间,不是总读取时间，默认120s
     */
    private int readTimeout = 120 * 1000;

    /**
     * 默认的编码格式
     */
    private String charset = "UTF-8";

    /**
     * 重试次数,默认3次
     */
    private int retryTimes = 3;

    /**
     * 从连接池获取连接的超时时间,不宜过长,单位ms
     */
    private int connectionRequestTimout = 200;

    /**
     * 针对不同的地址,特别设置不同的长连接保持时间
     */
    private Map<String, Integer> keepAliveTargetHost;

    /**
     * 针对不同的地址,特别设置不同的长连接保持时间,单位 s
     */
    private int keepAliveTime = 60;
}