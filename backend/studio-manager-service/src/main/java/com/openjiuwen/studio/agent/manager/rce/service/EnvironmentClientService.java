/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.service;

import com.huaweicloud.sdk.apig.v2.model.ListInstancesV2Response;
import com.huaweicloud.sdk.apig.v2.model.ShowDetailsOfInstanceV2Response;
import com.huaweicloud.sdk.eip.v3.model.ListPublicipsResponse;
import com.huaweicloud.sdk.elb.v3.model.ListLoadBalancersResponse;
import com.huaweicloud.sdk.elb.v3.model.ShowLoadBalancerResponse;
import com.huaweicloud.sdk.nat.v2.model.ListNatGatewaysResponse;
import com.huaweicloud.sdk.swr.v2.model.ListNamespacesResponse;
import com.huaweicloud.sdk.vpc.v2.model.ListSubnetsResponse;
import com.huaweicloud.sdk.vpc.v2.model.ShowSubnetResponse;
import com.huaweicloud.sdk.vpc.v3.model.ListSecurityGroupsResponse;
import com.huaweicloud.sdk.vpc.v3.model.ListVpcsResponse;
import com.huaweicloud.sdk.vpc.v3.model.ShowSecurityGroupResponse;
import com.huaweicloud.sdk.vpc.v3.model.ShowVpcResponse;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.manager.entity.CreateEnvOpsMetaData;
import com.openjiuwen.studio.agent.manager.enums.EnumEnvStatus;
import com.openjiuwen.studio.agent.manager.rce.client.ApigClient;
import com.openjiuwen.studio.agent.manager.rce.client.EipClient;
import com.openjiuwen.studio.agent.manager.rce.client.ElbClient;
import com.openjiuwen.studio.agent.manager.rce.client.NatClient;
import com.openjiuwen.studio.agent.manager.rce.client.SwrClient;
import com.openjiuwen.studio.agent.manager.rce.client.VpcClient;
import com.openjiuwen.studio.agent.manager.service.environment.model.OpsEnvironmentInfo;
import com.openjiuwen.studio.agent.manager.service.environment.model.OpsEnvironmentInstances;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 环境管理
 *
 */
@Slf4j
@Service
public class EnvironmentClientService {

    public static final int IPV4_VERSION = 4;

    @Autowired
    private VpcClient vpcClient;

    @Autowired
    private SwrClient swrClient;

    @Autowired
    private ElbClient elbClient;

    @Autowired
    private ApigClient apigClient;

    @Autowired
    private EipClient eipClient;

    @Autowired
    private NatClient natClient;

    @Value("${env-management.ops-call:true}")
    private boolean opsCallSwitch;

    /**
     * 默认分页大小
     */
    private static final int DEFAULT_LIMIT = 100;

    /**
     * 查询vpc列表
     *
     */
    public ListVpcsResponse listVpcs(String token, String projectId) {
        try {
            ResponseEntity<ListVpcsResponse> vpcResp = vpcClient.queryVpcs(token, projectId, DEFAULT_LIMIT, null,
                null, null, null, null);
            if (vpcResp.getStatusCode().is2xxSuccessful()) {
                return vpcResp.getBody();
            }
            throw new AgentStudioException(StudioError.ENVIRONMENT_WITH_QUERY_VPC_FAIL);
        } catch (feign.FeignException feignException) {
            log.error("queryVpcs error: ", feignException);
            throw new AgentStudioException(StudioError.ENVIRONMENT_WITH_QUERY_VPC_FAIL);
        }
    }

    /**
     * 查询vpc信息
     *
     */
    public Optional<ShowVpcResponse> showVpc(String token, String projectId, String vpcId) {
        try {
            ResponseEntity<ShowVpcResponse> vpcResp = vpcClient.showVpc(token, projectId, vpcId);
            if (vpcResp.getStatusCode().is2xxSuccessful()) {
                return Optional.ofNullable(vpcResp.getBody());
            }
            throw new AgentStudioException(StudioError.ENVIRONMENT_WITH_QUERY_VPC_FAIL);
        } catch (feign.FeignException feignException) {
            log.error("queryVpc error: ", feignException);
            if (feignException.status() >= 400 && feignException.status() < 500) {
                return Optional.empty();
            }
            throw new AgentStudioException(StudioError.ENVIRONMENT_WITH_QUERY_VPC_FAIL);
        }
    }

    /**
     * 查询vpc下的子网信息
     *
     */
    public ListSubnetsResponse listSubnets(String token, String projectId, String vpcId) {
        try {
            ResponseEntity<ListSubnetsResponse> subnetResp = vpcClient.listSubnets(token,
                projectId, DEFAULT_LIMIT, null, vpcId);
            if (subnetResp.getStatusCode().is2xxSuccessful()) {
                return subnetResp.getBody();
            }
            throw new AgentStudioException(StudioError.ENVIRONMENT_WITH_QUERY_SUBNET_FAIL);
        } catch (feign.FeignException feignException) {
            log.error("query subnets : ", feignException);
            throw new AgentStudioException(StudioError.ENVIRONMENT_WITH_QUERY_SUBNET_FAIL);
        }
    }

    /**
     * 查询子网信息
     *
     */
    public Optional<ShowSubnetResponse> showSubnet(String token, String projectId, String subnetId) {
        try {
            ResponseEntity<ShowSubnetResponse> subnetResp = vpcClient.showSubnet(token, projectId, subnetId);
            if (subnetResp.getStatusCode().is2xxSuccessful()) {
                return Optional.ofNullable(subnetResp.getBody());
            }
            throw new AgentStudioException(StudioError.ENVIRONMENT_WITH_QUERY_SUBNET_FAIL);
        } catch (feign.FeignException feignException) {
            log.error("query subnet: ", feignException);
            if (feignException.status() >= 400 && feignException.status() < 500) {
                return Optional.empty();
            }
            throw new AgentStudioException(StudioError.ENVIRONMENT_WITH_QUERY_SUBNET_FAIL);
        }
    }

    /**
     * 查询eip列表
     *
     */
    public ResponseEntity<ListPublicipsResponse> queryEipPublicIps(String token, String projectId) {
        try {
            return eipClient.ListEipPublicIps(token, projectId, null, 0, DEFAULT_LIMIT, null,
                Collections.singletonList(IPV4_VERSION), Collections.singletonList("EIP"), Collections.singletonList("DOWN"));
        } catch (feign.FeignException feignException) {
            log.error("query eip: ", feignException);
            throw new AgentStudioException(StudioError.DEPLOY_QUERY_EIP_FAIL);
        }
    }

    /**
     * 查询nat列表
     *
     */
    public ResponseEntity<ListNatGatewaysResponse> queryNatGatewayList(String token, String projectId) {
        try {
            return natClient.ListNatGateways(token, projectId, "ACTIVE");
        } catch (feign.FeignException feignException) {
            log.error("query nat: ", feignException);
            throw new AgentStudioException(StudioError.DEPLOY_QUERY_NAT_FAIL);
        }
    }

    /**
     * 查询镜像组织信息
     *
     */
    public ListNamespacesResponse listNamespaces(String token) {
        try {
            ResponseEntity<ListNamespacesResponse> nsResp = swrClient.listNamespaces(token, null, null);
            if (nsResp.getStatusCode().is2xxSuccessful()) {
                return nsResp.getBody();
            }
            throw new AgentStudioException(StudioError.ENVIRONMENT_WITH_QUERY_SWR_NAMESPACES_FAIL);
        } catch (feign.FeignException feignException) {
            log.error("query swr: ", feignException);
            throw new AgentStudioException(StudioError.ENVIRONMENT_WITH_QUERY_SWR_NAMESPACES_FAIL);
        }
    }

    /**
     * 查询负载均衡列表
     *
     */
    public ListLoadBalancersResponse listLoadBalancers(String token, String projectId, String vpcId) {
        try {
            ResponseEntity<ListLoadBalancersResponse> elbResp = elbClient.listLoadBalancers(token, projectId, vpcId);
            if (elbResp.getStatusCode().is2xxSuccessful()) {
                return elbResp.getBody();
            }
            throw new AgentStudioException(StudioError.ENVIRONMENT_WITH_QUERY_ELB_BALANCERS_FAIL);
        } catch (feign.FeignException feignException) {
            log.error("query elb: ", feignException);
            throw new AgentStudioException(StudioError.ENVIRONMENT_WITH_QUERY_ELB_BALANCERS_FAIL);
        }
    }

    /**
     * 查询负载均衡详情
     *
     */
    public Optional<ShowLoadBalancerResponse> showLoadBalancer(String token, String projectId, String loadbalancerId) {
        try {
            ResponseEntity<ShowLoadBalancerResponse> elbResp = elbClient.showLoadBalancer(token,
                projectId, loadbalancerId);
            if (elbResp.getStatusCode().is2xxSuccessful()) {
                return Optional.ofNullable(elbResp.getBody());
            }
            throw new AgentStudioException(StudioError.ENVIRONMENT_WITH_QUERY_ELB_BALANCERS_FAIL);
        } catch (feign.FeignException feignException) {
            if (feignException.status() >= 400 && feignException.status() < 500) {
                return Optional.empty();
            }
            log.error("query elb: ", feignException);
            throw new AgentStudioException(StudioError.ENVIRONMENT_WITH_QUERY_ELB_BALANCERS_FAIL);
        }
    }

    /**
     * 查询安全组列表
     *
     * @param limit 每页大小
     * @param marker 分页查询起始的资源ID，为空时查询第一页
     * @param name 安全组的名称
     */
    public ListSecurityGroupsResponse listSecurityGroups(String token,
        String projectId, int limit, String marker, List<String> name) {
        try {
            ResponseEntity<ListSecurityGroupsResponse> securityGroupsResp = vpcClient.listSecurityGroups(token,
                projectId, limit, marker, null, name, null, null);
            if (securityGroupsResp.getStatusCode().is2xxSuccessful()) {
                return securityGroupsResp.getBody();
            }
            throw new AgentStudioException(StudioError.ENVIRONMENT_WITH_QUERY_SECURITY_GROUPS_FAIL);
        } catch (feign.FeignException feignException) {
            log.error("query security groups: ", feignException);
            throw new AgentStudioException(StudioError.ENVIRONMENT_WITH_QUERY_SECURITY_GROUPS_FAIL);
        }
    }

    /**
     * 查询安全组信息
     *
     */
    public Optional<ShowSecurityGroupResponse> showSecurityGroup(String token,
        String projectId, String securityGroupId) {
        try {
            ResponseEntity<ShowSecurityGroupResponse> securityGroupsResp = vpcClient.showSecurityGroup(token,
                projectId, securityGroupId);
            if (securityGroupsResp.getStatusCode().is2xxSuccessful()) {
                return Optional.ofNullable(securityGroupsResp.getBody());
            }
            throw new AgentStudioException(StudioError.ENVIRONMENT_WITH_QUERY_SECURITY_GROUPS_FAIL);
        } catch (feign.FeignException feignException) {
            log.error("query security groups: ", feignException);
            if (feignException.status() >= 400 && feignException.status() < 500) {
                return Optional.empty();
            }
            throw new AgentStudioException(StudioError.ENVIRONMENT_WITH_QUERY_SECURITY_GROUPS_FAIL);
        }
    }

    /**
     * 查询apig实例列表
     *
     */
    public Optional<ListInstancesV2Response> listInstancesV2(String token, String projectId, int limit, int offset) {
        try {
            ResponseEntity<ListInstancesV2Response> securityGroupsResp = apigClient.listInstancesV2(token,
                projectId, null, offset, limit);
            if (securityGroupsResp.getStatusCode().is2xxSuccessful()) {
                return Optional.ofNullable(securityGroupsResp.getBody());
            }
            throw new AgentStudioException(StudioError.ENVIRONMENT_WITH_QUERY_APIG_FAIL);
        } catch (feign.FeignException feignException) {
            log.error("query apig instances: ", feignException);
            if (feignException.status() >= 400 && feignException.status() < 500) {
                return Optional.empty();
            }
            throw new AgentStudioException(StudioError.ENVIRONMENT_WITH_QUERY_APIG_FAIL);
        }
    }

    /**
     * 查询apig实例
     *
     */
    public Optional<ShowDetailsOfInstanceV2Response> showDetailsOfInstanceV2(String token,
        String projectId, String instanceId) {
        try {
            ResponseEntity<ShowDetailsOfInstanceV2Response> securityGroupsResp = apigClient.showDetailsOfInstanceV2(
                token, projectId, instanceId);
            if (securityGroupsResp.getStatusCode().is2xxSuccessful()) {
                return Optional.ofNullable(securityGroupsResp.getBody());
            }
            throw new AgentStudioException(StudioError.ENVIRONMENT_WITH_QUERY_APIG_FAIL);
        } catch (feign.FeignException feignException) {
            log.error("query apig instance: ", feignException);
            if (feignException.status() >= 400 && feignException.status() < 500) {
                return Optional.empty();
            }
            throw new AgentStudioException(StudioError.ENVIRONMENT_WITH_QUERY_APIG_FAIL);
        }
    }

    /**
     * 环境创建
     *
     */
    public String createEnvironment(String projectId, CreateEnvOpsMetaData body) {
        return UUID.randomUUID().toString();
    }

    /**
     * 删除环境
     *
     */
    public boolean hasDeleteEnvironment(String projectId, String environmentId) {
        return true;
    }

    /**
     * 查询当前环境实例信息
     *
     */
    public OpsEnvironmentInstances queryEnvironmentInstance(String projectId,
        String environmentId, int pageNo, int pageSize) {
        OpsEnvironmentInstances opsEnvironmentInstances = new OpsEnvironmentInstances();
        opsEnvironmentInstances.setItems(new ArrayList<>());
        return opsEnvironmentInstances;
    }

    /**
     * 查询环境详情
     *
     */
    public OpsEnvironmentInfo queryEnvironmentInfo(String projectId, String environmentId, String domainId, String status) {
        OpsEnvironmentInfo opsEnvironmentInfo = new OpsEnvironmentInfo();
        if (Strings.CI.equals(status, EnumEnvStatus.CREATING.getValue())) {
            opsEnvironmentInfo.setStatus(EnumEnvStatus.READY.getValue());
        } else if (Strings.CI.equals(status, EnumEnvStatus.DELETING.getValue())) {
            opsEnvironmentInfo.setStatus(EnumEnvStatus.DELETED.getValue());
        }
        return opsEnvironmentInfo;
    }
}

