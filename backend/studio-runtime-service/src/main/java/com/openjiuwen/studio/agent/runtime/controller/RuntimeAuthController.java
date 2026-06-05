/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.controller;

import static com.openjiuwen.studio.agent.common.constant.Constants.Header.X_SUBJECT_TOKEN;

import com.openjiuwen.studio.agent.common.dto.simple.SimpleUser;
import com.openjiuwen.studio.agent.common.utils.simple.SimpleAuthUtils;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * AuthController
 *
 */
@RestController
@Validated
@Slf4j
public class RuntimeAuthController {

    @ApiOperation(value = "token校验", nickname = "validateToken", notes = "校验token并返回对应用户信息",
        response = SimpleUser.class, tags = {"validateToken"})
    @RequestMapping(value = "/v3/auth/tokens", produces = {"application/json"}, method = RequestMethod.GET)
    public ResponseEntity<SimpleUser> validateToken(
        @RequestHeader("X-Subject-Token") String token) {
        SimpleUser user = SimpleAuthUtils.tokenToUser(token);
        return buildResp(user, token);
    }

    private ResponseEntity<SimpleUser> buildResp(SimpleUser simpleUser,
        String tokenStr) {
        return ResponseEntity.ok().header(X_SUBJECT_TOKEN, tokenStr).body(simpleUser);
    }
}
