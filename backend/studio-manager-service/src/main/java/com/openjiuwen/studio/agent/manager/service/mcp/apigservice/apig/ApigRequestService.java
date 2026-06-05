/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.mcp.apigservice.apig;

import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.config.ApigConfiguration;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.dto.McpServiceDetailInfoDto;
import com.openjiuwen.studio.agent.manager.service.mcp.apigservice.bean.ApigRequsetDto;
import com.openjiuwen.studio.agent.manager.utils.IamServiceUtils;
import com.huaweicloud.sdk.apig.v2.model.ApiActionInfo;
import com.huaweicloud.sdk.apig.v2.model.ApiAuthCreate;
import com.huaweicloud.sdk.apig.v2.model.ApiCreate;
import com.huaweicloud.sdk.apig.v2.model.ApiFuncCreate;
import com.huaweicloud.sdk.apig.v2.model.ApiGroupCreate;
import com.huaweicloud.sdk.apig.v2.model.AuthOpt;
import com.huaweicloud.sdk.apig.v2.model.BackendApiCreate;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @ClassName : ApicRequestBuildService
 * @Description :转换工具，构造各种APIC请求
 * @Date : Created in 2024/4/9 9:45
 * @Version : V1.0
 */
@Service
@Slf4j
public class ApigRequestService {

    private static final String API_GROUP_PREFIX = "AgentBuilder_api_group";

    private static final String API_RELEASE_ENV_ID = "DEFAULT_ENVIRONMENT_RELEASE_ID";

    @Autowired
    private IamServiceUtils iamServiceUtils;

    private static final String CUSTOM_MCP_ENDPOINT_SUFFIX = "/mcp/%s";

    @Autowired
    private ApigConfiguration apigConfiguration;

    /**
     * 构造CreateApiV2Request
     *
     * @param apigRequsetDto
     * @return CreateApiV2Request
     */
    public ApiCreate buildCreateApiV2Request(ApigRequsetDto apigRequsetDto) {
        // 后端API配置
        ApiFuncCreate apiFuncCreate = new ApiFuncCreate().withFunctionUrn(apigRequsetDto.getFuncUrn())
            .withInvocationType(ApiFuncCreate.InvocationTypeEnum.SYNC)
            .withNetworkType(ApiFuncCreate.NetworkTypeEnum.V2)
            .withTimeout(60000);
        BackendApiCreate backendApiCreate = new BackendApiCreate().withReqMethod(BackendApiCreate.ReqMethodEnum.POST)
            .withReqProtocol(BackendApiCreate.ReqProtocolEnum.HTTP) // 后端调用协议
            .withTimeout(60000); // APIC连接最终后端服务的超时时间，非数据传输时间，连接不上不重试
        return new ApiCreate().withName(apigRequsetDto.getApiServiceName())
            .withVersion("latest")
            .withType(ApiCreate.TypeEnum.NUMBER_1) // 默认私有API
            .withReqMethod(ApiCreate.ReqMethodEnum.ANY)
            .withReqProtocol(ApiCreate.ReqProtocolEnum.BOTH) // 前端调用协议
            .withReqUri(apigRequsetDto.getApigPath()) // 前端调用路径
            .withAuthType(ApiCreate.AuthTypeEnum.APP) // APIG上APP认证
            .withAuthOpt(new AuthOpt().withAppCodeAuthType(AuthOpt.AppCodeAuthTypeEnum.HEADER)) // APIG上APP认证，开启简易版认证
            .withMatchMode(ApiCreate.MatchModeEnum.SWA)
            .withBackendType(ApiCreate.BackendTypeEnum.FUNCTION) // 后端服务类型:HTTP/函数流/Mock
            .withGroupId(apigRequsetDto.getGroupId())
            .withBackendApi(backendApiCreate)
            .withFuncInfo(apiFuncCreate); // 群组id
    }

    public ApiGroupCreate buildCreateApiGroupV2Request(ApigRequsetDto routerVo) {
        return new ApiGroupCreate().withName(genGroupName());
    }

    public ApiActionInfo buildPublishApiActionInfo(String apiId) {
        return new ApiActionInfo().withApiId(apiId)
            .withAction(ApiActionInfo.ActionEnum.ONLINE)
            .withEnvId(API_RELEASE_ENV_ID);
    }

    public ApiActionInfo buildOfflineApiActionInfo(String apiId) {
        return new ApiActionInfo().withApiId(apiId)
            .withAction(ApiActionInfo.ActionEnum.OFFLINE)
            .withEnvId(API_RELEASE_ENV_ID);
    }

    public ApiAuthCreate buildApiAuthCreate(List<String> apiIds) {
        return new ApiAuthCreate().withEnvId(apigConfiguration.getEnvId())
            .withApiIds(apiIds)
            .withAppIds(new ArrayList<>() {
                {
                    add(apigConfiguration.getCredentialsId());
                }
            });
    }

    public String buildRemoveThrottleApiBindingCreate(String throttleBindingId) {
        return throttleBindingId;
    }

    /**
     * 生成APIC群组名称
     *
     * @return String
     */
    public String genGroupName() {
        return String.format("%s_%s", API_GROUP_PREFIX, RequestContextUtils.getRequestUserDomainId());
    }

    /**
     * 生成在APIG上的api调用路径
     * APIC上的请求地址构造规则:service_id/XXX/sse
     * 模型服务名称可能包含中文或其他特殊字符，不放入路径中
     *
     * @param serviceDetailId
     * @return String
     */
    public String genApiPathInApic(String serviceDetailId) {
        return String.format(CUSTOM_MCP_ENDPOINT_SUFFIX, serviceDetailId);
    }

    /**
     * api命名规则:用户的server_id+fun_id
     *
     * @param apigRequsetDto
     * @return String
     */
    public String genApiName(ApigRequsetDto apigRequsetDto) {
        return apigRequsetDto.getApiServiceName();
    }

    /**
     * 构造可变请求头
     *
     * @return headers
     */
    public Map<String, String> getXAuthTokenHeaders() {
        String xAuthToken = iamServiceUtils.getOpAccountAuthToken();
        HashMap<String, String> headers = new HashMap<>();
        headers.put(StringUtils.lowerCase(CommonConstant.X_AUTH_TOKEN, Locale.ROOT), xAuthToken);
        return headers;
    }

    public HttpHeaders getXAuthTokenHeaders2() {
        HttpHeaders headers = new HttpHeaders();
        String xAuthToken = iamServiceUtils.getOpAccountAuthToken();
        headers.add("x-auth-token", xAuthToken);
        return headers;
    }

    private String genApicUrlDomain(String realUrl, int beginIndex) {
        String urlDomain = realUrl.substring(beginIndex);
        urlDomain = urlDomain.split("/", 2)[0];
        return urlDomain;
    }

    private String genApicReqUri(String realUrl) {
        String reqUri = realUrl.substring(8);
        reqUri = reqUri.split("/", 2)[1];
        return "/" + reqUri;
    }

    /**
     * 修改RouterVo，转化为ModelRouter接口可解析的内容
     *
     * @param apigRequsetDto routerVo
     */
    public void modifyRouterVo(ApigRequsetDto apigRequsetDto) {
        String groupName = genGroupName();
        apigRequsetDto.setGroupName(groupName);
        apigRequsetDto.setApigPath(genApiPathInApic(apigRequsetDto.getServiceId()));
        apigRequsetDto.setApiType(apigRequsetDto.getApiType());
    }
}
