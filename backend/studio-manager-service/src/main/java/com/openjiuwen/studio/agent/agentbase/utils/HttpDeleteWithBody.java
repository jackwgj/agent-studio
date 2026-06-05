/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.utils;

import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpEntity;

/**
 * Http DELETE请求，带请求体（Apache HttpClient中的HttpDelete不带请求体）
 *
 * @since 2025-06-14
 */
public class HttpDeleteWithBody extends HttpDelete implements ClassicHttpRequest {

    private HttpEntity entity;

    public HttpDeleteWithBody(String uri) {
        super(uri);
    }

    @Override
    public void setEntity(HttpEntity entity) {
        this.entity = entity;
    }

    @Override
    public HttpEntity getEntity() {
        return entity;
    }
}

