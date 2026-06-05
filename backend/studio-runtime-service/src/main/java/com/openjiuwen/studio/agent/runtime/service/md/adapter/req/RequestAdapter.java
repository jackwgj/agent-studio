/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.md.adapter.req;

import com.alibaba.fastjson.JSONObject;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.runtime.http.HttpResponse;

import java.util.Map;

public interface RequestAdapter {
    String getName();

    /**
     * 请求参数转换
     *
     * @param headers 请求头
     * @param body 请求体
     * @param stream 是否流式请求
     * @return 转换之后请求体
     */
    Object requestBodyConvert(Map<String, String> headers, Object body, boolean stream);

    /**
     * 认证之后的处理
     *
     * @param originHeaders 原始请求头
     * @param authHeaders 认证后请求头
     * @param body 请求体
     * @param stream 是否流式调用
     */
    default void postAuthHandler(Map<String, String> originHeaders, Map<String, String> authHeaders, Object body,
        boolean stream) {
    }

    /**
     * 流式请求 响应体转换
     *
     * @param url 请求URL
     * @param response 响应
     * @return 响应结果
     */
    Object streamResBodyConvert(String url, HttpResponse<String> response);

    /**
     * 非流式请求 响应体转换
     *
     * @param url url
     * @param response response
     * @param request request
     * @return JSONObject
     */
    JSONObject resBodyConvert(String url, HttpResponse<String> response, Object request);

    /**
     * 非流式请求 响应体转换
     *
     * @param url 请求URL
     * @param response 响应
     * @return 响应结果
     */
    JSONObject resBodyConvert(String url, HttpResponse<String> response);

    /**
     * 异常处理
     *
     * @param exp 异常
     */
    AgentStudioException requestExceptionHandler(Exception exp);
}
