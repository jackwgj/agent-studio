/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.client;

import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.license.models.ItepCheckResourceReq;
import com.openjiuwen.studio.agent.manager.license.models.LcsResourceDto;
import com.openjiuwen.studio.agent.manager.license.models.LcsResourceDtoWrapper;
import com.openjiuwen.studio.agent.manager.license.models.LcsResourceInfo;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * ITEP接口调用类
 *
 */
@FeignClient(name = "itepIamClient", url = "${feign.client.config.itepIamClient.url:}")
public interface ItepClient {
    /**
     * 资源项状态检查
     *
     */
    @PostMapping(value = "/v1/itep/license/check-resource", name = "资源项状态检查")
    ResponseEntity<LcsResourceInfo> checkResource(@RequestHeader(CommonConstant.X_AUTH_TOKEN) String authToken,
                                                  @RequestBody ItepCheckResourceReq body);

    /**
     * 从itep获取资源项总量和已使用量
     *
     */
    @GetMapping(value = "/v1/itep/license/resources", name = "从itep获取资源项总量和已使用量")
    ResponseEntity<LcsResourceDtoWrapper> getResourceLicneseInfo(@RequestHeader(CommonConstant.X_AUTH_TOKEN) String authToken,
                                                                 @RequestHeader(CommonConstant.CONTENT_TYPE) String contentType);

    /**
     * 消耗资源
     *
     */
    @PostMapping(value = "/v1/itep/license/add-resource", name = "消耗资源")
    ResponseEntity<LcsResourceDto> addResource(@RequestHeader(CommonConstant.X_AUTH_TOKEN) String authToken,
                                               @RequestHeader(CommonConstant.CONTENT_TYPE) String contentType,
                                               @RequestBody LcsResourceDto body);

    /**
     * 释放资源
     *
     */
    @PostMapping(value = "/v1/itep/license/delete-resource", name = "释放资源")
    ResponseEntity<LcsResourceDto> deleteResource(@RequestHeader(CommonConstant.X_AUTH_TOKEN) String authToken,
                                                  @RequestHeader(CommonConstant.CONTENT_TYPE) String contentType,
                                                  @RequestBody LcsResourceDto body);
}