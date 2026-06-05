/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service.auth.apikey;

import com.alibaba.fastjson.JSON;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.common.utils.CopyUtils;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.common.utils.ValidateUtils;
import com.openjiuwen.studio.agent.manager.dto.ApiKeyRequest;
import com.openjiuwen.studio.agent.manager.dto.ApiKeys;
import com.openjiuwen.studio.agent.manager.dto.ApiKeysListResp;
import com.openjiuwen.studio.agent.manager.dto.ApikeyResponse;
import com.openjiuwen.studio.agent.manager.dto.QueryApiKeysQo;
import com.openjiuwen.studio.agent.manager.entity.ApiKeysEntity;
import com.openjiuwen.studio.agent.manager.mapper.ApiKeysMapper;
import com.openjiuwen.studio.agent.manager.service.IApiKeyMgmtService;
import com.openjiuwen.studio.agent.common.utils.CryptoUtils;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class ApiKeysService implements IApiKeyMgmtService {

    @Autowired
    private ApiKeysMapper apiKeysMapper;

    @Autowired
    private RedisClient redisClient;

    @Value("${apikey.enable:false}")
    private boolean apiKeyEnable;

    public static final String APIKEY_PREFIX = "sk-";

    public ApikeyResponse createApiKey(String projectId, String workspaceId, ApiKeyRequest body) {
        if (!apiKeyEnable) {
            throw new AgentStudioException(StudioError.API_KEY_UNABLE);
        }
        validateApiKey(projectId, workspaceId,body.getApikeyName());
        // 生成 API Key
        try {
            byte[] apiKey = genSecureRandomByte(32);
            String apiKeyHex = APIKEY_PREFIX + byte2hex(apiKey);
            String apiKeyComment = CryptoUtils.encrypt(apiKeyHex);
            String apikeyId = UUID.randomUUID().toString();
            ApiKeysEntity apiKeysEntity = new ApiKeysEntity().setApiKeyValue(apiKeyComment)
                .setApiKeyId(apikeyId)
                .setApiKeyName(body.getApikeyName())
                .setApiKeyValue(apiKeyComment)
                .setDescription(body.getApikeyDescription())
                .setCreatedDate(System.currentTimeMillis())
                .setProjectId(projectId)
                .setWorkspaceId(workspaceId)
                .setUserId(RequestContextUtils.getRequestUserId())
                .setDomainId(RequestContextUtils.getRequestUserDomainId())
                .setCreatedByUserName(RequestContextUtils.getRequestUserName())
                .setLastUpdatedByUserName(RequestContextUtils.getRequestUserName());
            apiKeysMapper.createApiKey(apiKeysEntity);

            Map<String, String> redisData = new HashMap<>();
            redisData.put("projectId", projectId);
            redisData.put("workspaceId", workspaceId);
            redisData.put("domainId", RequestContextUtils.getRequestUserDomainId());
            redisData.put("userId", RequestContextUtils.getRequestUserId());

            redisClient.set(apiKeyHex, JSON.toJSONString(redisData));

            return new ApikeyResponse().setId(String.valueOf(apiKeysEntity.getApiKeyId()))
                .setApikey(apiKeyHex);
        } catch (Exception e) {
            log.error("api key create fail{}", e.getMessage());
            throw new AgentStudioException(StudioError.API_KEY_CREATE_ERROR);
        }
    }


    @Scheduled(fixedRateString = "${apikey.sync.interval}")
    public void syncApiKeysToRedis() {
        if (!apiKeyEnable) {
            return;
        }
        List<ApiKeysEntity> allApiKeys = apiKeysMapper.selectList(null, null, 0, 0, null); // 获取所有 API Key
        try {
            for (ApiKeysEntity key : allApiKeys) {
                String redisKey = CryptoUtils.decrypt(key.getApiKeyValue());
                if (!redisClient.exists(redisKey)) {
                    Map<String, String> redisData = new HashMap<>();
                    redisData.put("projectId", key.getProjectId());
                    redisData.put("workspaceId", key.getWorkspaceId());
                    redisData.put("domainId", key.getDomainId());
                    redisData.put("userId", key.getUserId());
                    redisClient.set(redisKey, JSON.toJSONString(redisData));
                }
            }
        } catch (Exception e) {
            log.error("api key sync fail:{}",e.getMessage());
        }
        log.info("api key sync complete");
    }

    public byte[] genSecureRandomByte(int length) {
        byte[] randomBytes = new byte[length];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(randomBytes);
        return randomBytes;
    }

    public String byte2hex(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();
        String stmp;
        for (byte aByte : bytes) {
            stmp = Integer.toHexString(aByte & 0xFF);
            if (stmp.length() == 1) {
                stringBuilder.append("0").append(stmp);
            } else {
                stringBuilder.append(stmp);
            }
        }
        return stringBuilder.toString();
    }

    public ApiKeysListResp queryApiKeys(String projectId, QueryApiKeysQo queryApiKeysQo) {
        if (!apiKeyEnable) {
            throw new AgentStudioException(StudioError.API_KEY_UNABLE);
        }
        int totalSize = apiKeysMapper.count(null, projectId, queryApiKeysQo.getWorkspaceId(),RequestContextUtils.getRequestUserId());
        int offset = (queryApiKeysQo.getPageNum() - 1) * queryApiKeysQo.getPageSize();
        List<ApiKeysEntity> apiKeys = apiKeysMapper.selectList(projectId, queryApiKeysQo.getWorkspaceId(),
            queryApiKeysQo.getPageSize(), offset, RequestContextUtils.getRequestUserId());

        List<ApiKeys> data = ValidateUtils.isEmptyCollection(apiKeys)
            ? new ArrayList<>()
            : CopyUtils.copyList(apiKeys, ApiKeys.class);

        return new ApiKeysListResp().setTotal(totalSize).setData(data);
    }

    public Void deleteApiKey(String apiKeyId, String projectId, String workspaceId) {
        if (!apiKeyEnable) {
            throw new AgentStudioException(StudioError.API_KEY_UNABLE);
        }
        ApiKeysEntity apiKeys = apiKeysMapper.selectById(apiKeyId);
        if(apiKeys == null || !apiKeys.getUserId().equals(RequestContextUtils.getRequestUserId())) {
            throw new AgentStudioException(StudioError.API_KEY_AUTH_ERROR);
        }
        apiKeysMapper.deleteById(apiKeyId);
        redisClient.delete(apiKeys.getApiKeyValue());
        return null;
    }

    public void validateApiKey(String projectId, String workspaceId, String name) {
        if (apiKeysMapper.count(name, projectId, workspaceId, RequestContextUtils.getRequestUserId()) > 0) {
            throw new AgentStudioException(StudioError.API_KEY_NAME_EXIST);
        }
        if (apiKeysMapper.count(null, projectId, null, RequestContextUtils.getRequestUserId()) >= 30) {
            throw new AgentStudioException(StudioError.API_KEY_NUM_LIMIT);
        }
    }
}
