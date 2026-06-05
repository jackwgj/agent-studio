/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.utils;

import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * HttpUtils
 *
 */
@Slf4j
public class HttpUtils {
    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    public final static int MAX_IDLE_CONNECTIONS = 20;

    public final static long KEEP_ALIVE_DURATION = 30L;

    public final static int CONNECT_TIME_OUT = 6;

    public final static int WRITE_TIME_OUT = 10;

    public final static int READ_TIME_OUT = 40;

    /**
     * client
     * 配置重试
     */
    private final static OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder().connectTimeout(CONNECT_TIME_OUT,
            TimeUnit.SECONDS)
        .writeTimeout(WRITE_TIME_OUT, TimeUnit.SECONDS)
        .readTimeout(READ_TIME_OUT, TimeUnit.SECONDS)
        .build();

    public static boolean stream(String url, Map<String, String> headers, String json,
        EventSourceListener eventSourceListener) {
        try {
            RequestBody body = RequestBody.Companion.create(json, MEDIA_TYPE_JSON);
            Request.Builder builder = new Request.Builder();
            buildHeader(builder, headers);
            Request request = builder.url(url).post(body).build();
            EventSource.Factory factory = EventSources.createFactory(HTTP_CLIENT);
            // 创建事件
            log.info("http stream request,url: {}, parameters: {}", url, json);
            factory.newEventSource(request, eventSourceListener);
            return true;
        } catch (Exception e) {
            log.error("http stream request, url: {} failed, parameters: {}", url, json, e);
        }
        return false;
    }

    /**
     * 设置请求头
     *
     * @param builder .
     * @param headers 请求头
     */
    private static void buildHeader(Request.Builder builder, Map<String, String> headers) {
        if (Objects.nonNull(headers) && !headers.isEmpty()) {
            headers.forEach((k, v) -> {
                if (Objects.nonNull(k) && Objects.nonNull(v)) {
                    builder.addHeader(k, v);
                }
            });
        }
    }
}
