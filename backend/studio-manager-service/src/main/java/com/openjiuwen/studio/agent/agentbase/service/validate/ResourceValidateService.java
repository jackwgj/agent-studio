/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.validate;

import static com.openjiuwen.studio.common.service.constant.CommonConstant.TenantType.TENANT_TYPE_2B;
import static com.openjiuwen.studio.common.service.constant.CommonConstant.TenantType.TENANT_TYPE_2C;

import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeBaseEntity;
import com.openjiuwen.studio.agent.agentbase.enums.ConfigType;
import com.openjiuwen.studio.agent.agentbase.enums.LicenseAttrCode;
import com.openjiuwen.studio.agent.agentbase.enums.ResourceType;
import com.openjiuwen.studio.agent.agentbase.service.config.KnowledgeBaseLicenseService;
import com.openjiuwen.studio.agent.agentbase.service.config.KnowledgeBaseSystemConfigService;
import com.openjiuwen.studio.agent.agentbase.service.resource.ResourceUsageFactory;
import com.openjiuwen.studio.agent.agentbase.utils.TenantTypeUtil;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.foundation.base.constants.DeploymentModeEnum;
import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;
import com.openjiuwen.studio.agent.foundation.base.exception.ErrorCode;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class ResourceValidateService {

    private static final Map<ResourceType, LicenseAttrCode> LICENSE_CONTROL_RESOURCES = Map.of(
        ResourceType.KNOWLEDGE_BASE_DATESET_STORAGE, LicenseAttrCode.OBS_CAPACITY);

    public static final long MB = 1024 * 1024L;

    private final KnowledgeBaseSystemConfigService configService;

    private final KnowledgeBaseLicenseService licenseService;

    private final ResourceUsageFactory resourceUsageFactory;

    private final MultipartProperties multipartProperties;

    @Value("${env.type}")
    private String envType;

    public ResourceValidateService(KnowledgeBaseSystemConfigService configService,
        KnowledgeBaseLicenseService licenseService, ResourceUsageFactory resourceUsageFactory,
        MultipartProperties multipartProperties) {
        this.configService = configService;
        this.licenseService = licenseService;
        this.resourceUsageFactory = resourceUsageFactory;
        this.multipartProperties = multipartProperties;
    }

    // 全局检验
    public void resourceQuotaCheck(String domainId, ResourceType resourceType, long increase) {
        // HCS环境暂时不做规格校验
        if (DeploymentModeEnum.isNotHc(envType)) {
            return;
        }
        Long usedCount = resourceUsageFactory.chooseResourceUsageQueryServiceMap(resourceType)
            .queryResourceUsageCount(domainId);
        Long quotaLimit = queryQuotaLimit(resourceType);
        if (increase + usedCount > quotaLimit) {
            log.warn("Current tenant resource {} count is out of limit {}, please check.", resourceType.getName(),
                quotaLimit);
            throw new AgentBaseException(ErrorCode.RESOURCE_CAPACITY_LIMIT_ERROR,
                ResourceType.getNameByLanguage(resourceType));
        }
    }

    // 知识库内校验
    public void resourceQuotaCheckInKnowledgeBase(String domainId, ResourceType resourceType, String knowledgeBaseId,
        long increase) {
        Long usedCount = resourceUsageFactory.chooseResourceUsageQueryServiceMap(resourceType)
            .queryResourceUsageCountInKnowledgeBase(domainId, knowledgeBaseId);
        Long quotaLimit = queryQuotaLimit(resourceType);
        if (increase + usedCount > quotaLimit) {
            log.warn("Current tenant resource {} count is out of limit {}, please check.", resourceType.getName(),
                quotaLimit);
            throw new AgentBaseException(ErrorCode.RESOURCE_CAPACITY_LIMIT_ERROR,
                ResourceType.getNameByLanguage(resourceType));
        }
    }

    public Long queryQuotaLimit(ResourceType resourceType) {
        String id = ConfigType.RESOURCE_LIMIT.getValue() + "." + resourceType.getName();
        return licenseControl(resourceType)
            ? licenseService.getLimitValue(id, LICENSE_CONTROL_RESOURCES.get(resourceType))
            : configService.getSystemConfigValue(id, RequestContextUtils.getRequestUserDomainId());
    }

    private boolean licenseControl(ResourceType resourceType) {
        return LICENSE_CONTROL_RESOURCES.get(resourceType) != null;
    }

    /**
     * 校验上传文件分为以下几步：
     * 1.校验单个文件大小，免费版为数据库限制，企业版为配置项配置
     * 2.校验文件数量
     * 3.校验存储容量
     * 3.1 单个租户的全局免费版存储总容量为1G
     * 3.2 对于升级为企业版的租户，先前开通的免费版知识库总容量为1G
     * 3.3 对于自己购买的KooSearch上开通的知识库，总容量不限制
     */
    public void validateUploadFile(String knowledgeBaseId, KnowledgeBaseEntity knowledgeBaseEntity, MultipartFile file,
        String[] supportedFileTypes) {
        long maxFileSize = getKnowledgeBaseFileLimitSize();
        // 校验容量存储
        validateKnowledgeBaseStorage(knowledgeBaseId, knowledgeBaseEntity, file.getSize());
    }

    public void validateUploadFileStream(Resource resource, String knowledgeBaseId,
        KnowledgeBaseEntity knowledgeBaseEntity, String[] supportedFileTypes) {
        long fileSize;
        try {
            fileSize = resource.contentLength();
        } catch (IOException e) {
            log.error("Fail to get inputStream from file [{}]", resource.getFilename());
            throw new AgentBaseException(ErrorCode.FILE_READ_ERROR);
        }
        // 校验容量存储
        validateKnowledgeBaseStorage(knowledgeBaseId, knowledgeBaseEntity, fileSize);
    }

    private void validateKnowledgeBaseStorage(String knowledgeBaseId, KnowledgeBaseEntity knowledgeBaseEntity,
        long fileSize) {
        String domainId = RequestContextUtils.getRequestUserDomainId();
        String tenantType = TenantTypeUtil.getTenantType();
        // 校验文件数量
        resourceQuotaCheckInKnowledgeBase(domainId, ResourceType.KNOWLEDGE_BASE_DATESET, knowledgeBaseId, 1L);
        // 校验存储容量
        if (TENANT_TYPE_2C.equals(tenantType) || (TENANT_TYPE_2B.equals(tenantType)
            && knowledgeBaseEntity.getKnowledgeBaseTenantType().equals(TENANT_TYPE_2C))) {
            resourceQuotaCheck(domainId, ResourceType.KNOWLEDGE_BASE_DATESET_STORAGE, fileSize);
        } else {
            log.info("enterprise kooSearch do not validate storage!");
        }
    }

    private long getKnowledgeBaseFileLimitSize() {
        return TENANT_TYPE_2C.equals(TenantTypeUtil.getTenantType())
            ? queryQuotaLimit(ResourceType.SINGLE_FILE_SIZE_FREE_KNOWLEDGE_BASE)
            : Optional.ofNullable(multipartProperties)
                .map(MultipartProperties::getMaxFileSize)
                .map(DataSize::toBytes)
                .orElse(10 * MB);
    }
}
