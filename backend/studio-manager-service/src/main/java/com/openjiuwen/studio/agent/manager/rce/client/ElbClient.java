/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.client;

import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.huaweicloud.sdk.elb.v3.model.ListLoadBalancersResponse;
import com.huaweicloud.sdk.elb.v3.model.ShowLoadBalancerResponse;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * APIG接口调用类
 *
 */
@FeignClient(name = "elbClient", url = "${feign.client.config.elbClient.url:}")
public interface ElbClient {
    @GetMapping(value = "/v3/{project_id}/elb/loadbalancers", name = "查询负载均衡器列表")
    ResponseEntity<ListLoadBalancersResponse> listLoadBalancers(
        @RequestHeader(CommonConstant.X_AUTH_TOKEN) String authToken,
        @PathVariable("project_id") String projectId,
        @RequestParam("vpc_id") String vpcId);

    @GetMapping(value = "/v3/{project_id}/elb/loadbalancers/{loadbalancer_id}", name = "查询负载均衡器详情")
    ResponseEntity<ShowLoadBalancerResponse> showLoadBalancer(
        @RequestHeader(CommonConstant.X_AUTH_TOKEN) String authToken,
        @PathVariable("project_id") String projectId,
        @PathVariable("loadbalancer_id") String loadbalancerId);
}