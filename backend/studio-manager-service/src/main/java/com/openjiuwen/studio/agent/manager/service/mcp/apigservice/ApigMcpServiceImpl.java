/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.mcp.apigservice;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.constant.TableStatus;
import com.openjiuwen.studio.agent.manager.mapper.ApigApiGroupsMapper;
import com.openjiuwen.studio.agent.manager.mapper.ApigApisMapper;
import com.openjiuwen.studio.agent.manager.service.mcp.apigservice.apig.ApigApiService;
import com.openjiuwen.studio.agent.manager.service.mcp.apigservice.apig.ApigRequestService;
import com.openjiuwen.studio.agent.manager.service.mcp.apigservice.bean.ApigApiGroupsDto;
import com.openjiuwen.studio.agent.manager.service.mcp.apigservice.bean.ApigApisDto;
import com.openjiuwen.studio.agent.manager.service.mcp.apigservice.bean.ApigRequsetDto;
import com.openjiuwen.studio.agent.manager.service.mcp.model.apig.ApigGroupResp;
import com.openjiuwen.studio.agent.manager.utils.CommonUtil;
import com.openjiuwen.studio.agent.manager.utils.JsonUtils;
import com.huaweicloud.sdk.apig.v2.model.ApiActionInfo;
import com.huaweicloud.sdk.apig.v2.model.ApiAuthCreate;
import com.huaweicloud.sdk.apig.v2.model.ApiAuthRelations;
import com.huaweicloud.sdk.apig.v2.model.ApiCreate;
import com.huaweicloud.sdk.apig.v2.model.ApiGroupCreate;
import com.huaweicloud.sdk.apig.v2.model.CreateApiGroupV2Response;
import com.huaweicloud.sdk.apig.v2.model.CreateApiV2Response;
import com.huaweicloud.sdk.apig.v2.model.DeleteApiV2Response;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;

/**
 * @ClassName : ApigService
 * @Description :UniModel直接调用
 * @Date : Created in 2024/3/22 19:49
 * @Version : V1.0
 */
@Slf4j
@Service
public class ApigMcpServiceImpl implements ApiMcpService {
    @Autowired
    private ApigApisMapper apigApisMapper;

    @Autowired
    private ApigApiGroupsMapper apigApiGroupsMapper;

    @Autowired
    private ApigRequestService apigRequestService;

    @Autowired
    private ApigApiService apigApiService;

    @Autowired
    private RollBackService rollBackService;


    @Transactional
    public CreateApiV2Response createApi(ApigRequsetDto apigRequsetDto) {
        String groupName = apigRequestService.genGroupName();
        apigRequestService.modifyRouterVo(apigRequsetDto);

        // 2.调用成功，上APIG创建API
        // 查看分组是否存在
        CreateApiV2Response createApiV2Response = null;
        boolean needRollBack = true;
        try {
            String groupId;
            ApigApiGroupsDto apigApiGroupsDto = apigApiGroupsMapper.selectByName(groupName); // 根据分组名称查询分组id
            if (apigApiGroupsDto == null || !apigApiGroupsDto.getName().equals(groupName)) {
                // 分组不存在,创建分组
                log.info("Create Group '{}'!", groupName);
                ApigGroupResp resp = apigApiService.listApiGroup(groupName);
                groupId = getGruopId(resp) ;
                if (StringUtils.isEmpty(groupId)) {
                    ApiGroupCreate apiGroupCreate = apigRequestService.buildCreateApiGroupV2Request(apigRequsetDto);
                    CreateApiGroupV2Response createApiGroupV2Response = apigApiService.createApiGroup(apiGroupCreate);
                    groupId = createApiGroupV2Response.getId();
                }
                apigApiGroupsMapper.insert(ApigApiGroupsDto.builder()
                    .id(groupId)
                    .name(groupName)
                    .tenantId(RequestContextUtils.getRequestUserDomainId())
                    .createdByUserId(CommonUtil.getUserId())
                    .createdDate(new Timestamp(System.currentTimeMillis()))
                    .lastUpdatedByUserId(CommonUtil.getUserId())
                    .lastUpdatedDate(new Timestamp(System.currentTimeMillis()))
                    .status(TableStatus.CREATED.getValue())
                    .build());
            } else {
                log.info("Group '{}' exists!", groupName);
                groupId = apigApiGroupsDto.getId();
            }
            apigRequsetDto.setGroupId(groupId);

            // 创建api
            String apiName = apigRequsetDto.getApiServiceName();
            log.info("Create api '{}'!", apiName);
            ApiCreate createApiV2Request = apigRequestService.buildCreateApiV2Request(apigRequsetDto);
            createApiV2Response = apigApiService.createApi(createApiV2Request);
            log.info("response {}", JsonUtils.toJson(createApiV2Request));
            String apiId = createApiV2Response.getId();
            apigApisMapper.insert(ApigApisDto.builder()
                .id(apiId)
                .name(apiName)
                .groupId(groupId)
                .tenantId(RequestContextUtils.getRequestUserDomainId())
                .createdByUserId(CommonUtil.getUserId())
                .createdDate(new Timestamp(System.currentTimeMillis()))
                .lastUpdatedByUserId(CommonUtil.getUserId())
                .lastUpdatedDate(new Timestamp(System.currentTimeMillis()))
                .status(TableStatus.RELEASED.getValue()) // API创建出来立即发布
                .build());

            // 发布Api
            log.info("Publish api '{}'!", apiName);
            ApiActionInfo publishApiActionInfo = apigRequestService.buildPublishApiActionInfo(
                createApiV2Response.getId());
            apigApiService.publishOrOfflineApi(publishApiActionInfo);

            // App认证授权
            log.info("Create auth of api '{}'!", apiName);
            ApiAuthCreate apiAuthCreate = apigRequestService.buildApiAuthCreate(new ArrayList<>() {
                {
                    add(apiId);
                }
            });
            ApiAuthRelations apiAuthRelations = apigApiService.apiBindCredentials(apiAuthCreate);
            return createApiV2Response;
        } catch (Exception e) {
            log.error("Faile {}", e.getMessage());
            throw new AgentStudioException(StudioError.CREATE_MCP_API_FAILED, "create mcp api failed!");
        }
    }

    private String getGruopId(ApigGroupResp resp) {
        if (resp == null || resp.getGroups() == null || resp.getGroups().isEmpty()) {
            return StringUtils.EMPTY;
        }
        return resp.getGroups().get(0).getId();
    }
    @Transactional
    public ResponseEntity<DeleteApiV2Response> deleteApi(ApigRequsetDto apigRequsetDto) {
        String apiName = apigRequsetDto.getApiServiceName();

        // 2.删除APIG接口
        ResponseEntity<DeleteApiV2Response> deleteApiV2Response;
        try {
            // 根据api名称获取apiId
            ApigApisDto apigApisDto = apigApisMapper.selectByName(apiName);
            if (apigApisDto == null || !apigApisDto.getName().equals(apiName)) {
                log.error("Api '{}' not found in table!", (apiName));
                throw new AgentStudioException(StudioError.MCP_API_NOT_FOUND, apiName);
            }
            String apiId = apigApisDto.getId();

            // 下线api
            log.info("Offline api '{}'!", (apiName));
            ApiActionInfo offlineApiActionInfo = apigRequestService.buildOfflineApiActionInfo(apiId);
            apigApiService.publishOrOfflineApi(offlineApiActionInfo);

            // 删除api
            log.info("Delete api '{}'!", (apiName));
            deleteApiV2Response = apigApiService.deleteApi(apiId);
            apigApisMapper.deleteById(
                ApigApisDto.builder().id(apiId).lastUpdatedDate(new Timestamp(System.currentTimeMillis())).build());
            return deleteApiV2Response;
        } catch (Exception e) {
            // 删除APIC接口报错，前端用户不感知，且不影响后续其他API创建，忽略报错
            log.error("Delete api error, ignored");
            throw new AgentStudioException(StudioError.UNEXPECTED_ERROR);
        }
    }
}
