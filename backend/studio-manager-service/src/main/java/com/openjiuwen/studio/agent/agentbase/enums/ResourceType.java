/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.enums;

import com.openjiuwen.studio.agent.foundation.base.constants.DeploymentModeEnum;
import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;
import com.openjiuwen.studio.agent.foundation.base.exception.ErrorCode;
import com.openjiuwen.studio.agent.foundation.i18n.I18nUtils;

import lombok.Getter;

import java.util.Map;
import java.util.Set;

@Getter
public enum ResourceType {

    KNOWLEDGE_BASE("knowledgeBase", "知识库数量", false),

    KNOWLEDGE_BASE_DATESET("knowledgeBaseDateset", "数据集数量", true),

    KNOWLEDGE_BASE_DATESET_STORAGE("knowledgeBaseDatesetStorage", "数据集容量", false),

    THIRD_PARTY_KNOWLEDGE_BASE("thirdPartyKnowledgeBase", "第三方知识库数量", false),

    THIRD_PARTY_KNOWLEDGE_BASE_CONNECTION("thirdPartyKnowledgeBaseConnection", "第三方知识库平台数量", false),

    SINGLE_FILE_SIZE_FREE_KNOWLEDGE_BASE("freeKnowledgeBaseSingleFileSize", "免费版知识库单个文件最大容量", false);

    private final String name;

    private final String nameCN;

    /**
     * 是否是知识库内限制，如数据集数量就是知识库内限制，需要根据知识库knowledgeId获取
     */
    private final boolean isInKnowledgeBase;

    ResourceType(String name, String nameCN, boolean isInKnowledgeBase) {
        this.name = name;
        this.nameCN = nameCN;
        this.isInKnowledgeBase = isInKnowledgeBase;
    }

    public static String getNameByLanguage(ResourceType type) {
        return I18nUtils.isCn() ? type.getNameCN() : type.name;
    }

    public static ResourceType fromValue(String type) {
        for (ResourceType resourceType : ResourceType.values()) {
            if (resourceType.name.equals(type)) {
                return resourceType;
            }
        }
        throw new AgentBaseException(ErrorCode.INVALID_PARAMETER, "resourceType");
    }

    public static final Set<ResourceType> HC_RESOURCE_TYPE = Set.of(KNOWLEDGE_BASE, KNOWLEDGE_BASE_DATESET,
        KNOWLEDGE_BASE_DATESET_STORAGE, THIRD_PARTY_KNOWLEDGE_BASE, THIRD_PARTY_KNOWLEDGE_BASE_CONNECTION,
        SINGLE_FILE_SIZE_FREE_KNOWLEDGE_BASE);

    public static final Set<ResourceType> HCS_RESOURCE_TYPE = Set.of(KNOWLEDGE_BASE_DATESET);

    public static final Set<ResourceType> SIMPLE_RESOURCE_TYPE = Set.of(KNOWLEDGE_BASE_DATESET);

    public static final Map<String, Set<ResourceType>> ENV_TYPE_QUERY_RESOURCE_TYPE_MAP = Map.of(
        DeploymentModeEnum.HC.getValue(), HC_RESOURCE_TYPE, DeploymentModeEnum.HCS.getValue(), HCS_RESOURCE_TYPE,
        DeploymentModeEnum.SIMPLE.getValue(), SIMPLE_RESOURCE_TYPE);

}
