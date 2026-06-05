/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.http;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import okhttp3.Response;

import java.io.InputStream;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HttpResponse<T> {
    private int statusCode;

    private Map<String, String> headers;

    private T responseBody;

    /**
     * 异常响应体，统一字符串返回
     */
    private String errorResponseBody;

    /**
     * 正常流式响应
     */
    private InputStream streamResponse;

    /**
     * okhttp 原始响应体
     */
    private Response rawResponse;

    public void close() {
        if (rawResponse != null) {
            rawResponse.close();
            rawResponse = null;
        }
    }
}
