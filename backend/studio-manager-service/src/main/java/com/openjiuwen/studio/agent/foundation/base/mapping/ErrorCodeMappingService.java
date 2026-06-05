/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.foundation.base.mapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;
import com.openjiuwen.studio.agent.foundation.base.exception.ErrorCode;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class ErrorCodeMappingService {

    @Value("${knowledge.source}")
    private String knowledgeSource;

    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    private final Map<String, ErrorCode> mappingCache = new HashMap<>();

    private ErrorCode defaultAgentBaseErrorCode;

    @PostConstruct
    public void loadMappingConfig() {
        ClassPathResource resource = new ClassPathResource("kooSearch_errorCode_mapping.yml");
        try (InputStream inputStream = resource.getInputStream()) {
            MappingConfig config = yamlMapper.readValue(inputStream, MappingConfig.class);
            for (MappingRule rule : config.getMappingRules()) {
                String normalizedCode = normalizeKooSearchCode(rule.getKooSearchErrorCode());
                if (normalizedCode != null) {
                    mappingCache.put(normalizedCode, ErrorCode.fromCode(rule.getAgentBaseErrorCode()));
                }
            }
            if ("KooSearch".equals(knowledgeSource)) {
                this.defaultAgentBaseErrorCode = ErrorCode.KOO_SEARCH_SERVICE_EXCEPTION;
            } else if ("LakeSearch".equals(knowledgeSource)) {
                this.defaultAgentBaseErrorCode = ErrorCode.LAKE_SEARCH_SERVICE_EXCEPTION;
            } else {
                this.defaultAgentBaseErrorCode = ErrorCode.SERVER_INTERNAL_ERROR;
            }
        } catch (Exception e) {
            log.error("load error code mapping config error", e.getMessage());
            throw new AgentBaseException(ErrorCode.SYSTEM_PARAMS_CONFIG_ERROR, e);
        }
    }

    /**
     * @param kooSearchErrorCode kooSearch服务返回的错误码
     * @return 对应的AgentBase服务错误码
     */
    public ErrorCode getAgentBaseErrorCode(String kooSearchErrorCode) {
        if (kooSearchErrorCode == null) {
            return defaultAgentBaseErrorCode;
        }
        String normalizedCode = normalizeKooSearchCode(kooSearchErrorCode);
        ErrorCode errorCode = mappingCache.get(normalizedCode);
        if (errorCode != null) {
            log.error("kooSearch service return error code: {}, AgentBase service return error code: {}",
                kooSearchErrorCode, errorCode.getCode());
            return errorCode;
        }
        return defaultAgentBaseErrorCode;
    }

    private String normalizeKooSearchCode(String code) {
        return Objects.isNull(code) ? null : code.toLowerCase(Locale.ROOT);
    }
}
