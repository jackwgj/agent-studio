/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.rce.client;

import com.openjiuwen.studio.agent.common.constant.Constants;
import com.openjiuwen.studio.agent.common.entity.AuthRequestWrapper;
import com.openjiuwen.studio.agent.common.entity.TokenResponse;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "iamManager", url = "${feign.client.config.iamManager.url:}")
public interface IamClient {
    @PostMapping(value = "/v3/auth/tokens", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<TokenResponse> getTokenByPassword(@RequestBody AuthRequestWrapper request);

    @PostMapping(value = "/v3/auth/tokens", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<TokenResponse> getTokenByAssumeRole(@RequestHeader(Constants.Header.X_AUTH_TOKEN) String authToken,
        @RequestBody AuthRequestWrapper request);

    @PostMapping(value = "/v3/auth/tokens", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<TokenResponse> getTokenWithInvocation(@RequestBody AuthRequestWrapper request);
}
