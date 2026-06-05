/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.foundation.connection.httpclient;

import com.openjiuwen.studio.agent.foundation.base.http.RequestEntity;

import lombok.Builder;
import lombok.Data;

import org.springframework.http.HttpMethod;

/**
 * 向第三方知识库发送请求时，基础的请求信息
 *
 * @since 2025-08-23
 */
@Data
@Builder
public class BasicRequest {

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
     * 请求中的实体信息
     */
    private RequestEntity requestEntity;
}
