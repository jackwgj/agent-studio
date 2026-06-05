/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.ros.relation;

import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.entity.McpServiceEntity;
import com.openjiuwen.studio.agent.manager.enums.EnumMcpStatus;
import com.openjiuwen.studio.agent.manager.enums.EnumOrgType;
import com.openjiuwen.studio.agent.manager.mapper.MappingMapper;
import com.openjiuwen.studio.agent.manager.mapper.McpServiceMapper;
import com.openjiuwen.studio.agent.manager.service.mcp.apigservice.ApiMcpService;
import com.openjiuwen.studio.agent.manager.service.mcp.apigservice.bean.ApigRequsetDto;
import com.openjiuwen.studio.agent.manager.service.mcp.model.dao.McpServiceDao;
import com.openjiuwen.studio.agent.manager.utils.McpUtil;
import com.huaweicloud.sdk.apig.v2.model.DeleteApiV2Response;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service("MCP_RelateResourceCleaner")
public class MCPRelationCleaner implements IRelateSourceCleaner {

    @Value("${spring.is-soft-delete}")
    private Boolean isSoftDelete;

    @Autowired
    private McpServiceMapper serviceMapper;

    @Autowired
    private MappingMapper mappingMapper;

    @Autowired
    private McpServiceDao serviceDao;

    @Resource
    private ApiMcpService apigMcpService;

    @Resource
    private McpUtil mcpUtil;

    @Override
    public void clean(List<String> ids, String domainId) {
        List<McpServiceEntity> serviceEntities = serviceMapper.selectByIds(ids);

        for (McpServiceEntity serviceEntity : serviceEntities) {
            if (Strings.CI.equals(serviceEntity.getOrgType(), EnumOrgType.SSE.toString()) ||
                    Strings.CI.equals(serviceEntity.getOrgType(), EnumOrgType.STREAMABLE_HTTP.toString())
                    || StringUtils.isBlank(serviceEntity.getOrgType())) {
                if (isSoftDelete) {
                    serviceDao.markAsDeleted(serviceEntity.getId(), serviceEntity.getTenantId(), serviceEntity.getDeptCode());
                } else {
                    serviceDao.hardDeleteSingleMcpServiceById(serviceEntity.getId(), serviceEntity.getTenantId(), serviceEntity.getDeptCode());
                }
                mappingMapper.updateValidByResourceId(serviceEntity.getId());
                return;
            }
            mappingMapper.updateValidByResourceId(serviceEntity.getId());
            ApigRequsetDto apigRequsetDto = new ApigRequsetDto();
            apigRequsetDto.setId(serviceEntity.getId());
            apigRequsetDto.setApiServiceName(serviceEntity.getFunctionApplicationName());
            // functiongraph由functiongraph服务自己删，只删apig
            ResponseEntity<DeleteApiV2Response> deleteApiV2Response = apigMcpService.deleteApi(apigRequsetDto);
            // 轮询查询结果
            if (deleteApiV2Response.getStatusCode().is2xxSuccessful()) {
                log.info("app {} delete complete at FG.", serviceEntity.getFunctionApplicationName());
                mcpUtil.updateServiceDeletedInfoFromFG(serviceEntity.getId(), serviceEntity.getTenantId(),
                        serviceEntity.getDeptCode());
            } else {
                log.info("app {} delete complete at FG.", serviceEntity.getFunctionApplicationName());
                mcpUtil.updateServiceStatus(serviceEntity.getId(), EnumMcpStatus.DELETION_FAIL.getValue(),
                        RequestContextUtils.getRequestUserDomainId(), serviceEntity.getDeptCode());
            }
        }
    }
}
