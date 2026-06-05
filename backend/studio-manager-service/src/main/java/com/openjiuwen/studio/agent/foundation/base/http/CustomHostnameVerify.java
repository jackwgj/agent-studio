/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.foundation.base.http;

import org.apache.hc.client5.http.ssl.DefaultHostnameVerifier;
import org.springframework.util.CollectionUtils;

import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public class CustomHostnameVerify {
    public HostnameVerifier CustomVerifyHostname(List<String> hostnames) {
        return new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                DefaultHostnameVerifier defaultHostnameVerifier = new DefaultHostnameVerifier();
                if (!defaultHostnameVerifier.verify(hostname, session)) {
                    if (!CollectionUtils.isEmpty(hostnames)) {
                        return hostnames.contains(hostname);
                    }
                    return true;
                }
                return true;
            }
        };

    }
}