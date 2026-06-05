/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.mcp.infrastructure.apig;

import com.alibaba.fastjson.JSONObject;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.dto.CesMetricDataResp;
import com.openjiuwen.studio.agent.manager.rce.service.ClientService;
import com.openjiuwen.studio.agent.manager.service.mcp.properties.IamProperties;
import com.openjiuwen.studio.agent.manager.service.mcp.properties.IamSouthWestProperties;
import com.openjiuwen.studio.agent.manager.utils.CommonUtil;
import com.openjiuwen.studio.agent.manager.utils.McpServiceExceptionUtils;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * APIG service
 *
 */
@Slf4j
@Service
public class CesService {
    @Resource
    private ClientService clientService;


    @Value("${agent.builder.fg.southwest.switch: false}")
    private String southwestSwitch;

    @Resource
    private IamSouthWestProperties iamSouthWestProperties;

    private static final String NAME_SPACE = "SYS.FunctionGraph";


    private static final String METRIC_PREFIX = "package-functionname%2Cdefault-";

    @Value("${iam.hisAccount.rotate.enable:true}")
    private Boolean hisAccountRotate;


    @Resource
    private IamProperties iamProperties;

    /**
     * @param from   开始时间
     * @param to     结束时间
     * @param period 监控数据的粒度
     * @return com.huawei.apaas.apparts.wisepilot.workflow.adapter.vo.apig.CesMetricDataResp
     * @implNote 调用ces接口
     * @description 从ces获取函数的统计信息
     * @date 10:25 2025/5/12
     **/
    public CesMetricDataResp mcpServicesCesByCountMetric(String from, String to, int period, String fgName) {
        String filter = "sum";
        String dim = StringUtils.join(METRIC_PREFIX, fgName); // 拼接函数名称
        CommonUtil.validateTenantIdIsNotEmpty();
        HttpHeaders headers = new HttpHeaders();
        String token;
        ResponseEntity<JSONObject> responseEntity;
        String projectId = iamProperties.getProjectId();
        token = RequestContextUtils.getRequestAuthToken();
        headers.add("x-auth-token", token);
        responseEntity = clientService.queryMetricData(headers, projectId, NAME_SPACE, "count", from, to, period, filter, dim);
        if (null == responseEntity) {
            return null;
        }
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            if (ObjectUtils.isNotEmpty(responseEntity.getBody())) {
                return responseEntity.getBody().toJavaObject(CesMetricDataResp.class);
            } else {
                log.error("ces metric data is not success! body is empty.");
            }
        } else {
            log.error("ces metric data is not success! code is {}", responseEntity.getStatusCode());
        }
        return null;
    }


    /**
     * @param from   开始时间
     * @param to     结束时间
     * @param period 监控数据的粒度
     * @return com.huawei.apaas.apparts.wisepilot.workflow.adapter.vo.apig.CesMetricDataResp
     * @implNote 调用ces接口
     * @description 从ces中获取接口调用错误率指标数据
     * @date 10:25 2025/5/12
     **/
    public CesMetricDataResp mcpServicesCesByFailRateMetric(String from, String to, int period, String fgName) {
        String filter = "average";
        String dim = StringUtils.join(METRIC_PREFIX, fgName); // 拼接函数名称
        CommonUtil.validateTenantIdIsNotEmpty();
        HttpHeaders headers = new HttpHeaders();
        String token;
        ResponseEntity<JSONObject> responseEntity;
        String projectId = iamProperties.getProjectId();

        token = RequestContextUtils.getRequestAuthToken();
        headers.add("x-auth-token", token);
        responseEntity = clientService.queryMetricData(headers, projectId, NAME_SPACE, "failRate", from, to, period, filter, dim);
        if (null == responseEntity) {
            return null;
        }
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            if (ObjectUtils.isNotEmpty(responseEntity.getBody())) {
                return responseEntity.getBody().toJavaObject(CesMetricDataResp.class);
            } else {
                log.error("ces metric data is not success! body is empty.");
                McpServiceExceptionUtils.throwRemoteCallException("ces metric data query", "ces metric data is not success! body is empty.");
            }
        } else {
            log.error("ces metric data is not success! code is {}", responseEntity.getStatusCode());
            McpServiceExceptionUtils.throwRemoteCallException("ces metric data query", "ces metric data is not success!");
        }
        return null;
    }
}
