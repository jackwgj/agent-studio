/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.md;

import static org.junit.jupiter.api.Assertions.fail;

import okhttp3.Headers;

import org.apache.hc.core5.http.Header;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Map;

public class RuntimeModelHttpClientServiceTest {
    private RuntimeModelHttpClientService clientService;

    @BeforeEach
    public void beforeEach() {
        clientService = new RuntimeModelHttpClientService();

        ReflectionTestUtils.setField(clientService, "modelRequestTimeout", 300000);
        ReflectionTestUtils.setField(clientService, "connectionTimeOut", 5000);
        ReflectionTestUtils.setField(clientService, "proxyOpen", true);
        ReflectionTestUtils.setField(clientService, "proxyUser", "uuu");
        ReflectionTestUtils.setField(clientService, "proxyPassword", "ppp");
        ReflectionTestUtils.setField(clientService, "proxyPort", 8080);
        ReflectionTestUtils.setField(clientService, "proxyHost", "127.0.0.1");
        ReflectionTestUtils.setField(clientService, "noProxyHost", Collections.singleton("aaa.com"));
        ReflectionTestUtils.setField(clientService, "maxTotal", 200);
        ReflectionTestUtils.setField(clientService, "defaultMaxPerRoute", 200);
    }

    @Test
    public void afterPropertiesSetTest() {
        try {
            clientService.afterPropertiesSet();
            clientService.getModelRequestHttpClient("127.0.0.1");
            clientService.getModelRequestHttpClient("aaa.com");
        } catch (Exception e) {
            fail("afterPropertiesSetTest Exception", e);
        }

        try {
            ReflectionTestUtils.setField(clientService, "proxyOpen", true);
            clientService.afterPropertiesSet();
            clientService.getModelRequestHttpClient("aaa.com");
        } catch (Exception e) {
            fail("afterPropertiesSetTest Exception", e);
        }
    }

    @Test
    public void header2MapTest() {
        Header header = new Header() {
            @Override
            public boolean isSensitive() {
                return false;
            }

            @Override
            public String getName() {
                return "aa";
            }

            @Override
            public String getValue() {
                return "bb";
            }
        };

        Header[] headers = new Header[] {header};
        Map<String, String> map = clientService.header2Map(headers);
        Assertions.assertEquals("{aa=bb}", map.toString());
    }

    @Test
    void testHeadersToMap() {
        Headers.Builder builder = new Headers.Builder();
        builder.set("aa", "bb");
        Map<String, String> map = clientService.header2Map(builder.build());
        Assertions.assertEquals("{aa=bb}", map.toString());
    }
}
