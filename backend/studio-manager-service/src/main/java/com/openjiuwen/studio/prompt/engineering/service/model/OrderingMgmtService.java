/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.service.model;

import static com.openjiuwen.studio.prompt.engineering.constant.CommonConstant.RESOURCES_ORDERS_URL;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.prompt.engineering.dto.OrderResource;
import com.openjiuwen.studio.prompt.engineering.dto.OrderResourceList;
import com.openjiuwen.studio.prompt.engineering.dto.OrderingCapacityInfo;
import com.openjiuwen.studio.prompt.engineering.dto.OrderingCapacityResp;
import com.openjiuwen.studio.prompt.engineering.enums.ApplicationDevelopingCapacityEnum;
import com.openjiuwen.studio.prompt.engineering.remote.ClientTemplate;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * 功能描述
 *
 */
@Slf4j
@Service
public class OrderingMgmtService {

    @Value("${manager.server.endpoint}")
    private String managerServerEndpoint;

    @Resource(name = "remoteClientTemplate")
    private ClientTemplate clientTemplate;

    /**
     * 查询订单功能项信息
     *
     * @param projectId projectId 最终租户项目 id
     * @return
     */

    public OrderingCapacityResp listOrderingCapacity(String projectId, String workspaceId) {
        // 调用agentBuilder-manager接口查询订单信息
        OrderResourceList resourceList = getOrderResources(projectId);
        List<OrderingCapacityInfo> orderingCapacityInfoList = new ArrayList<>();

        // 返回的是全量订单信息，需要自己做筛选
        for (OrderResource ele : resourceList.getList()) {
            List<String> capacities = ApplicationDevelopingCapacityEnum.getNames(ele.getResourceSpecCode());
            if (CollectionUtils.isEmpty(capacities)) {
                continue;
            }
            orderingCapacityInfoList.add(
                new OrderingCapacityInfo().setCapacities(capacities).setResourceSpecCode(ele.getResourceSpecCode()));
        }
        return new OrderingCapacityResp().setOrderingCapacities(orderingCapacityInfoList);
    }

    /**
     * 调用agentBuilder-manager接口查询订单信息
     *
     * @param projectId projectId 最终租户项目 id
     * @return
     */
    private OrderResourceList getOrderResources(String projectId) {
        try {
            String url = String.format(RESOURCES_ORDERS_URL, managerServerEndpoint, projectId);
            ResponseEntity<JSONObject> responseEntity =
                clientTemplate.getForEntity(url, RequestContextUtils.getRequestAuthToken(), JSONObject.class);
            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                log.error("GetOrderResources from manager server failed. projectId {} ", projectId);
                throw new AgentStudioException(StudioError.QUERY_ORDER_RESOURCES_FAILED, projectId);
            }
            OrderResourceList orderResourceList = JSON.toJavaObject(responseEntity.getBody(), OrderResourceList.class);
            log.info("resource list:{}", orderResourceList);
            return orderResourceList;
        } catch (Exception e) {
            log.error("GetOrderResources Exception {} projectId {} ", e.getMessage(), projectId);
            throw new AgentStudioException(StudioError.QUERY_ORDER_RESOURCES_FAILED, projectId);
        }
    }
}
