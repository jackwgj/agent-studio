/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.foundation.base.http;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Set;

@Slf4j
@Component
public class SmartRestTemplate {

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Autowired
    @Qualifier("restTemplateForCloudService")
    private RestTemplate restTemplateDirect;

    @Autowired(required = false)
    @Qualifier("restTemplateForCloudServiceWithProxy")
    private RestTemplate restTemplateProxy;

    @Autowired
    private UserProxyConfig proxyConfig;

    public <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity,
        Class<T> responseType) {
        if (!proxyConfig.isEnabled()) {
            log.info("No proxy set, using direct rest template");
            return restTemplateDirect.exchange(url, method, requestEntity, responseType);
        }
        URI uri;
        try {
            uri = URI.create(url);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid URI: {}, using direct rest template", url);
            return restTemplateDirect.exchange(url, method, requestEntity, responseType);
        }
        String host = uri.getHost();
        Set<String> noProxy = proxyConfig.getNoProxy();
        boolean useDirect = false;
        if (!CollectionUtils.isEmpty(noProxy) && host != null) {
            for (String pattern : noProxy) {
                if (antPathMatcher.match(pattern.trim(), host)) {
                    useDirect = true;
                    log.debug("No proxy match: pattern={}, host={}, url={}", pattern, host, url);
                    break;
                }
            }
        }
        if (useDirect || host == null) {
            log.info("Using direct rest template: host={}, url={}", host, url);
            return restTemplateDirect.exchange(url, method, requestEntity, responseType);
        } else {
            log.info("Using proxy rest template: host={}, url={}", host, url);
            return restTemplateProxy.exchange(url, method, requestEntity, responseType);
        }
    }

}