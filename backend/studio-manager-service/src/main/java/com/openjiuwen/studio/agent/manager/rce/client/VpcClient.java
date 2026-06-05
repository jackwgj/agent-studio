/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.client;

import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.huaweicloud.sdk.vpc.v2.model.ListSubnetsResponse;
import com.huaweicloud.sdk.vpc.v2.model.ShowSubnetResponse;
import com.huaweicloud.sdk.vpc.v3.model.ListSecurityGroupsResponse;
import com.huaweicloud.sdk.vpc.v3.model.ListVpcsResponse;
import com.huaweicloud.sdk.vpc.v3.model.ShowSecurityGroupResponse;
import com.huaweicloud.sdk.vpc.v3.model.ShowVpcResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * VPC服务接口调用类
 *
 */
@FeignClient(name = "vpcClient", url = "${feign.client.config.vpcClient.url:}")
public interface VpcClient {
    @GetMapping(value = "/v3/{project_id}/vpc/vpcs", name = "查询VPC列表")
    ResponseEntity<ListVpcsResponse> queryVpcs(
        @RequestHeader(CommonConstant.X_AUTH_TOKEN) String authToken,
        @PathVariable(name = "project_id") String projectId,
        @RequestParam(value = "limit") int limit,
        @RequestParam(value = "marker") String marker,
        @RequestParam(value = "id") @Valid @Size(max = 10) List<String> id,
        @RequestParam(value = "name") @Valid @Size(max = 10) List<String> name,
        @RequestParam(value = "description") @Valid @Size(max = 10) List<String> description,
        @RequestParam(value = "cidr") @Valid @Size(max = 10) List<String> cidr);

    @GetMapping(value = "/v3/{project_id}/vpc/vpcs/{vpc_id}", name = "查询VPC")
    ResponseEntity<ShowVpcResponse> showVpc(
        @RequestHeader(CommonConstant.X_AUTH_TOKEN) String authToken,
        @PathVariable(name = "project_id") String projectId,
        @PathVariable(value = "vpc_id") String vpcId);

    @GetMapping(value = "/v1/{project_id}/subnets", name = "查询子网列表")
    ResponseEntity<ListSubnetsResponse> listSubnets(
        @RequestHeader(CommonConstant.X_AUTH_TOKEN) String authToken,
        @PathVariable(name = "project_id") String projectId,
        @RequestParam(value = "limit") int limit,
        @RequestParam(value = "marker") String marker,
        @RequestParam(value = "vpc_id") String vpcId);

    @GetMapping(value = "/v1/{project_id}/subnets/{subnet_id}", name = "查询子网")
    ResponseEntity<ShowSubnetResponse> showSubnet(
        @RequestHeader(CommonConstant.X_AUTH_TOKEN) String authToken,
        @PathVariable(name = "project_id") String projectId,
        @PathVariable(value = "subnet_id") String subnetId);

    @GetMapping(value = "/v3/{project_id}/vpc/security-groups", name = "查询安全组列表")
    ResponseEntity<ListSecurityGroupsResponse> listSecurityGroups(
        @RequestHeader(CommonConstant.X_AUTH_TOKEN) String authToken,
        @PathVariable(name = "project_id") String projectId,
        @RequestParam(value = "limit") int limit,
        @RequestParam(value = "marker") String marker,
        @RequestParam(value = "id") @Valid @Size(max = 10) List<String> id,
        @RequestParam(value = "name") @Valid @Size(max = 10) List<String> name,
        @RequestParam(value = "description") @Valid @Size(max = 10) List<String> description,
        @RequestParam(value = "enterprise_project_id") @Valid @Size(max = 10) String enterpriseProjectId);

    @GetMapping(value = "/v3/{project_id}/vpc/security-groups/{security_group_id}", name = "查询安全组信息")
    ResponseEntity<ShowSecurityGroupResponse> showSecurityGroup(
        @RequestHeader(CommonConstant.X_AUTH_TOKEN) String authToken,
        @PathVariable(name = "project_id") String projectId,
        @PathVariable(name = "security_group_id") String securityGroupId);
}