package com.openjiuwen.studio.agent.space.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.InputStream;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommonHttpResponse {
    /**
     * http状态码
     */
    private int statusCode;

    /**
     * 响应头
     */
    private Map<String, String> responseHeaders;

    /**
     * 正常响应体，可以直接转换成要求的类型
     */
    private String responseBody;

    /**
     * 正常流式响应
     */
    private InputStream streamResponse;
}
