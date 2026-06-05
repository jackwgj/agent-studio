/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.foundation.base.http;

import lombok.Builder;
import lombok.Data;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;

import java.util.Map;

/**
 * 通用请求参数
 *
 * @since 2025-08-23
 */
@Data
@Builder
public class RequestEntity {
    /**
     * 请求的连接地址，如https://ip:port
     */
    private String host;

    /**
     * 请求的接口地址
     */
    private String uri;

    /**
     * 请求方法
     */
    private HttpMethod method;

    /**
     * 请求参数
     */
    Map<String, Object> requestParams;

    /**
     * 路径参数
     */
    Map<String, Object> pathVariables;

    /**
     * headers
     */
    HttpHeaders headers;

    /**
     * form-data参数
     */
    MultiValueMap<String, Object> multiValueParams;

    /**
     * 请求消息体
     */
    String body;

}
