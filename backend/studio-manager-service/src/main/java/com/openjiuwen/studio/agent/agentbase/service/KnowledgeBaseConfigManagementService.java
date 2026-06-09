/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.agentbase.service;

import static com.openjiuwen.studio.common.service.constant.CommonConstant.TenantType.TENANT_TYPE_2C;

import com.openjiuwen.studio.agent.agentbase.common.constant.Constants;
import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeBaseConnectionEntity;
import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeBaseEntity;
import com.openjiuwen.studio.agent.agentbase.enums.ResourceType;
import com.openjiuwen.studio.agent.agentbase.mapper.KnowledgeBaseConnectionMapper;
import com.openjiuwen.studio.agent.agentbase.mapper.KnowledgeBaseMapper;
import com.openjiuwen.studio.agent.agentbase.model.KnowledgeBaseConnectionParam;
import com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.KnowledgeConnectionRouterService;
import com.openjiuwen.studio.agent.agentbase.service.validate.ResourceValidateService;
import com.openjiuwen.studio.agent.agentbase.utils.TenantTypeUtil;
import com.openjiuwen.studio.agent.manager.dto.KnowledgeBaseConfig;
import com.openjiuwen.studio.agent.manager.dto.ListKnowledgeBaseConfigsResponse;
import com.openjiuwen.studio.agent.manager.service.IKnowledgeBaseConfigManagementService;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class KnowledgeBaseConfigManagementService implements IKnowledgeBaseConfigManagementService {

    private static final long BASE_UNIT = 1024L * 1024;

    private static final String KbModelManageUrlCode = "KB_MODEL_MANAGE_URL";

    private final ResourceValidateService validateService;

    private final MultipartProperties multipartProperties;

    private final KnowledgeBaseConnectionMapper knowledgeBaseConnectionMapper;

    private final KnowledgeBaseMapper knowledgeBaseMapper;

    private final KnowledgeConnectionRouterService knowledgeConnectionRouterService;

    private final KnowledgeBaseResourceManagementService
        resourceManagementService;

    public KnowledgeBaseConfigManagementService(ResourceValidateService validateService,
        MultipartProperties multipartProperties, KnowledgeBaseConnectionMapper knowledgeBaseConnectionMapper,
        KnowledgeBaseMapper knowledgeBaseMapper, KnowledgeConnectionRouterService knowledgeConnectionRouterService,
        KnowledgeBaseResourceManagementService resourceManagementService) {
        this.validateService = validateService;
        this.multipartProperties = multipartProperties;
        this.knowledgeBaseConnectionMapper = knowledgeBaseConnectionMapper;
        this.knowledgeBaseMapper = knowledgeBaseMapper;
        this.knowledgeConnectionRouterService = knowledgeConnectionRouterService;
        this.resourceManagementService = resourceManagementService;
    }

    private Long queryFileSize() {
        try {
            long fileSize = TENANT_TYPE_2C.equals(TenantTypeUtil.getTenantType())
                ? validateService.queryQuotaLimit(ResourceType.SINGLE_FILE_SIZE_FREE_KNOWLEDGE_BASE)
                : multipartProperties.getMaxFileSize().toBytes();
            return fileSize / BASE_UNIT;
        } catch (Exception e) {
            log.error("query file size failed, use default value 20M");
            return 20L;
        }
    }

    private String getKbModelManageUrl(String projectId, String knowledgeBaseId) {
        String kbModelManageUrl = "";
        String connectionId;

        // 1. 获取connection id
        if (StringUtils.isEmpty(knowledgeBaseId)) {
            connectionId = knowledgeConnectionRouterService.findConnectionIdByContext();
        } else {
            KnowledgeBaseEntity kbInfo = knowledgeBaseMapper.selectById(projectId, knowledgeBaseId);
            connectionId = kbInfo == null ? "" : kbInfo.getKnowledgeBaseConnectionId();
        }

        // 2. 通过connection id获取connection相关信息
        KnowledgeBaseConnectionEntity kbConnectionInfo = knowledgeBaseConnectionMapper.findById(connectionId);
        List<KnowledgeBaseConnectionParam> kbParams = kbConnectionInfo == null
            ? new ArrayList<>()
            : kbConnectionInfo.getParams();

        // 3. 在params中找到code=kb-model-manage-url 对应的value
        for (KnowledgeBaseConnectionParam param : kbParams) {
            if (KbModelManageUrlCode.equalsIgnoreCase(param.getCode())) {
                kbModelManageUrl = param.getValue();
                break;
            }
        }
        return kbModelManageUrl;
    }

    @Override
    public ListKnowledgeBaseConfigsResponse listKnowledgeBaseConfigs(String projectId, String knowledgeBaseId,
        String workspaceId) {
        List<KnowledgeBaseConfig> configs = new ArrayList<>();
        // 单个文件大小
        configs.add(new KnowledgeBaseConfig().setKey(Constants.SINGLE_FILE_SIZE).setValue(queryFileSize()));
        // 模型管理地址
        configs.add(new KnowledgeBaseConfig().setKey(KbModelManageUrlCode)
            .setValue(getKbModelManageUrl(projectId, knowledgeBaseId)));
        // 知识库容量限制,如果知识库id则不查询容量大小，为了兼容不传知识库id的场景。
        if (StringUtils.isNotEmpty(knowledgeBaseId)) {
            configs.add(new KnowledgeBaseConfig().setKey(Constants.OBS_CAPACITY)
                .setValue(resourceManagementService.queryResourceUsage(projectId,
                    ResourceType.KNOWLEDGE_BASE_DATESET_STORAGE.getName(), knowledgeBaseId).getMaxValue()));
        }
        return new ListKnowledgeBaseConfigsResponse().setConfigs(configs);
    }
}
