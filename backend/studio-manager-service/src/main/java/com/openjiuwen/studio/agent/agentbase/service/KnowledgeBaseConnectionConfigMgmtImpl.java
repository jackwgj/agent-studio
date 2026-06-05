/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service;

import static com.openjiuwen.studio.agent.agentbase.converter.KbConnectionConverter.DEFAULT_CONNECTION_ID;
import static com.openjiuwen.studio.agent.foundation.connection.constants.ConnectorTypeEnum.LAKE_SEARCH_INSIDE;

import com.openjiuwen.studio.agent.common.enums.RagAuthMode;
import com.openjiuwen.studio.agent.agentbase.converter.KbConnectionConverter;
import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeBaseConnectionEntity;
import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeBaseConnectorEntity;
import com.openjiuwen.studio.agent.agentbase.mapper.KnowledgeBaseConnectionMapper;
import com.openjiuwen.studio.agent.agentbase.mapper.KnowledgeBaseConnectorMapper;
import com.openjiuwen.studio.agent.agentbase.mapper.KnowledgeBaseMapper;
import com.openjiuwen.studio.agent.agentbase.model.KnowledgeBaseConnectionParam;
import com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.LakeSearchService;
import com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.connection.LakeSearchConnection;
import com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.knowledgesourceprovider.LakeSearchConnectionProvider;
import com.openjiuwen.studio.agent.agentbase.service.validate.KnowledgeBaseConnectionValidateService;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.foundation.base.constants.DeploymentModeEnum;
import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;
import com.openjiuwen.studio.agent.foundation.base.exception.ErrorCode;
import com.openjiuwen.studio.agent.manager.dto.ConnectionParamInfo;
import com.openjiuwen.studio.agent.manager.dto.CreateDefaultKnowledgeBaseConnectionRequestBody;
import com.openjiuwen.studio.agent.manager.dto.CreateDefaultKnowledgeBaseConnectionResponse;
import com.openjiuwen.studio.agent.manager.dto.DefaultKnowledgeBaseConnectionDetail;
import com.openjiuwen.studio.agent.manager.dto.KnowledgeBaseConnectorDetail;
import com.openjiuwen.studio.agent.manager.dto.ListDefaultKnowledgeBaseConnectorsResponseBody;
import com.openjiuwen.studio.agent.manager.dto.ParamDefinitionInfo;
import com.openjiuwen.studio.agent.manager.dto.ShowDefaultKnowledgeBaseConnectionDetailResponseBody;
import com.openjiuwen.studio.agent.manager.dto.TestDefaultKnowledgeBaseConnectionRequestBody;
import com.openjiuwen.studio.agent.manager.dto.TestDefaultKnowledgeBaseResponseBody;
import com.openjiuwen.studio.agent.manager.dto.UpdateDefaultKnowledgeBaseConnectionRequestBody;
import com.openjiuwen.studio.agent.manager.dto.UpdateDefaultKnowledgeBaseConnectionResponse;
import com.openjiuwen.studio.agent.manager.service.IKnowledgeBaseConnectionConfigManagementService;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class KnowledgeBaseConnectionConfigMgmtImpl implements IKnowledgeBaseConnectionConfigManagementService {

    private final KnowledgeBaseConnectionValidateService knowledgeBaseConnectionValidateService;

    private final KbConnectionConverter kbConnectionConverter;

    private final KnowledgeBaseConnectionMapper connectionMapper;

    private final KnowledgeBaseConnectorMapper connectorMapper;

    private final KnowledgeBaseMapper knowledgeBaseMapper;

    private final LakeSearchService lakeSearchService;

    private final LakeSearchConnectionProvider lakeSearchConnectionProvider;

    @Value("${knowledge.default-icon}")
    private String defaultIcon;

    @Value("${env.type}")
    private String envType;

    @Value("${knowledge.lakeSearch.auth-mode}")
    private String authMode;

    private static final String ENDPOINT = "endpoint";

    private static final String OCR_ENABLE = "ocr_enable";

    private static final String AUTH_MODE = "auth_mode";

    private static final String AUTHORIZATION = "authorization";

    public static final String KERBEROS = "Kerberos";

    private static final Set<String> REQUIRED_FIELDS = Set.of(AUTH_MODE, ENDPOINT, OCR_ENABLE);

    public KnowledgeBaseConnectionConfigMgmtImpl(
        KnowledgeBaseConnectionValidateService knowledgeBaseConnectionValidateService,
        KbConnectionConverter kbConnectionConverter, KnowledgeBaseConnectionMapper connectionMapper,
        KnowledgeBaseConnectorMapper connectorMapper, KnowledgeBaseMapper knowledgeBaseMapper,
        LakeSearchService lakeSearchService, LakeSearchConnectionProvider lakeSearchConnectionProvider) {
        this.knowledgeBaseConnectionValidateService = knowledgeBaseConnectionValidateService;
        this.kbConnectionConverter = kbConnectionConverter;
        this.connectionMapper = connectionMapper;
        this.connectorMapper = connectorMapper;
        this.knowledgeBaseMapper = knowledgeBaseMapper;
        this.lakeSearchService = lakeSearchService;
        this.lakeSearchConnectionProvider = lakeSearchConnectionProvider;
    }

    @Override
    public CreateDefaultKnowledgeBaseConnectionResponse createDefaultKbConnection(String projectId,
        CreateDefaultKnowledgeBaseConnectionRequestBody body) {
        log.info("operation log projectId: {}, userId:{}, createDefaultKbConnection", projectId,
            RequestContextUtils.getRequestUserId());
        checkPermission();
        if (KERBEROS.equalsIgnoreCase(authMode)) {
            log.error("do not support to config default knowledge base connection with kerberos");
            throw new AgentBaseException(ErrorCode.INVALID_KNOWLEDGE_REPO_REQUEST, KERBEROS);
        }
        if (!Strings.CS.equals(body.getConnectorId(), LAKE_SEARCH_INSIDE.getValue())) {
            log.error("do not support to config default knowledge base connection with connectorId [{}]",
                body.getConnectorId());
            throw new AgentBaseException(ErrorCode.INVALID_PARAMETER, body.getConnectorId());
        }
        // 全局只有1个默认知识库校验
        if (knowledgeBaseConnectionValidateService.defaultConnectionExisted(body.getConnectorId())) {
            log.error("default connection already exist");
            throw new AgentBaseException(ErrorCode.INVALID_PARAMETER, body.getConnectorId());
        }
        // 参数校验，当前值进行了判空检验
        checkDefaultKbConnectionParams(body.getParams());
        // 校验参数与连接器中定义的参数是否一致并加密
        List<ConnectionParamInfo> connectionParamInfos = knowledgeBaseConnectionValidateService.checkAndEncryptedParams(
            body.getConnectorId(), body.getParams());
        body.setParams(connectionParamInfos);
        // 入库
        KnowledgeBaseConnectionEntity entity = kbConnectionConverter.toKnowledgeBaseConnectionEntity(projectId, body);
        entity.setIcon(defaultIcon);
        try {
            connectionMapper.insert(entity);
            // 当首次配置并配置成功后，原先的知识库的connectionId要刷新成默认知识库
            knowledgeBaseMapper.updatePreviewsKnowledgeBaseConnectionId(entity.getId());
        } catch (RuntimeException e) {
            log.error("Create default knowledge base connection failed", e);
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, e);
        }
        return new CreateDefaultKnowledgeBaseConnectionResponse().setId(entity.getId());
    }

    @Override
    public ListDefaultKnowledgeBaseConnectorsResponseBody listDefaultKnowledgeBaseConnectors(String projectId,
        Integer offset, Integer limit) {
        checkPermission();
        // 当前只有LakeSearch允许页面配置
        final KnowledgeBaseConnectorEntity knowledgeBaseConnectorEntity = connectorMapper.find(
            LAKE_SEARCH_INSIDE.getValue());
        ListDefaultKnowledgeBaseConnectorsResponseBody listDefaultKnowledgeBaseConnectorsResponseBody
            = new ListDefaultKnowledgeBaseConnectorsResponseBody();
        listDefaultKnowledgeBaseConnectorsResponseBody.setTotal(1L);
        KnowledgeBaseConnectorDetail knowledgeBaseConnectorDetail = toKnowledgeBaseConnectorDetail(
            knowledgeBaseConnectorEntity);
        listDefaultKnowledgeBaseConnectorsResponseBody.setConnectors(List.of(knowledgeBaseConnectorDetail));
        return listDefaultKnowledgeBaseConnectorsResponseBody;
    }

    private KnowledgeBaseConnectorDetail toKnowledgeBaseConnectorDetail(
        KnowledgeBaseConnectorEntity knowledgeBaseConnectorEntity) {
        KnowledgeBaseConnectorDetail knowledgeBaseConnectorDetail
            = kbConnectionConverter.toKnowledgeBaseConnectorDetail(knowledgeBaseConnectorEntity);
        List<ParamDefinitionInfo> paramDefinitionInfos = kbConnectionConverter.connectorParams2ParamDefinitionInfos(
            knowledgeBaseConnectorEntity.getParams());
        return knowledgeBaseConnectorDetail.setParamDefinition(paramDefinitionInfos);
    }

    @Override
    public ShowDefaultKnowledgeBaseConnectionDetailResponseBody showDefaultKnowledgeBaseConnection(String projectId,
        String kbConnectionId) {
        checkPermission();
        ShowDefaultKnowledgeBaseConnectionDetailResponseBody showDefaultKnowledgeBaseConnectionDetailResponseBody
            = new ShowDefaultKnowledgeBaseConnectionDetailResponseBody();
        List<KnowledgeBaseConnectionEntity> defaultConnections = connectionMapper.findDefaultConnection(
            LAKE_SEARCH_INSIDE.getValue());
        if (CollectionUtils.isEmpty(defaultConnections)) {
            // 若未配置，则为null
            showDefaultKnowledgeBaseConnectionDetailResponseBody.setKnowledgeBaseConnectionDetail(null);
            return showDefaultKnowledgeBaseConnectionDetailResponseBody;
        }
        KnowledgeBaseConnectionEntity knowledgeBaseConnectionEntity = Optional.ofNullable(defaultConnections.get(0))
            .orElse(new KnowledgeBaseConnectionEntity());

        DefaultKnowledgeBaseConnectionDetail defaultKnowledgeBaseConnectionDetail
            = kbConnectionConverter.toDefaultKnowledgeBaseConnectionDetail(knowledgeBaseConnectionEntity);
        List<ConnectionParamInfo> connectionParamInfos = kbConnectionConverter.kbConnectionParams2ConnectionParamInfos(
            knowledgeBaseConnectionEntity.getParams());
        defaultKnowledgeBaseConnectionDetail.setParams(connectionParamInfos);
        showDefaultKnowledgeBaseConnectionDetailResponseBody.setKnowledgeBaseConnectionDetail(
            defaultKnowledgeBaseConnectionDetail);
        return showDefaultKnowledgeBaseConnectionDetailResponseBody;
    }

    @Override
    public TestDefaultKnowledgeBaseResponseBody testDefaultKnowledgeBaseConnection(String projectId,
        TestDefaultKnowledgeBaseConnectionRequestBody body) {
        log.info("operation log projectId: {}, userId:{}, testDefaultKnowledgeBaseConnection", projectId,
            RequestContextUtils.getRequestUserId());
        checkPermission();
        try {
            boolean result = false;
            if (Strings.CS.equals(body.getConnectorId(), LAKE_SEARCH_INSIDE.getValue())) {
                // 校验参数与连接器中定义的参数是否一致并加密
                List<ConnectionParamInfo> connectionParamInfos
                    = knowledgeBaseConnectionValidateService.checkAndEncryptedParams(body.getConnectorId(),
                    body.getParams());
                body.setParams(connectionParamInfos);
                LakeSearchConnection lakeSearchConnection = convertToLakeSearchConnection(body);
                result = lakeSearchService.testConnection(lakeSearchConnection);
            }
            return new TestDefaultKnowledgeBaseResponseBody().setResult(result);
        } catch (Exception e) {
            log.error("Connect to {} failed,error message:{}", body.getConnectorId(), e.getMessage());
            throw new AgentBaseException(ErrorCode.KNOWLEDGE_BASE_DEFAULT_CONNECTION_FAILED, e);
        }
    }

    @Override
    public TestDefaultKnowledgeBaseResponseBody testDefaultKnowledgeBaseConnectionId(String projectId,
        String connectionId) {
        log.info("operation log projectId: {}, userId:{}, testDefaultKnowledgeBaseConnection, connectorId: {}",
            projectId, RequestContextUtils.getRequestUserId(), connectionId);
        checkPermission();
        try {
            // 当前只支持lakesearch
            LakeSearchConnection lakeSearchConnection = lakeSearchConnectionProvider.getKnowledgeSourceConnection(
                connectionId);
            boolean result = lakeSearchService.testConnection(lakeSearchConnection);
            return new TestDefaultKnowledgeBaseResponseBody().setResult(result);
        } catch (Exception e) {
            log.error("Connect to {} failed,error message:{}", connectionId, e.getMessage());
            throw new AgentBaseException(ErrorCode.KNOWLEDGE_BASE_DEFAULT_CONNECTION_FAILED, e);
        }
    }

    @Override
    public UpdateDefaultKnowledgeBaseConnectionResponse updateDefaultKnowledgeBaseConnection(String projectId,
        String kbConnectionId, UpdateDefaultKnowledgeBaseConnectionRequestBody body) {
        log.info("operation log projectId: {}, userId:{}, UpdateDefaultKnowledgeBaseConnection", projectId,
            RequestContextUtils.getRequestUserId());
        checkPermission();
        if (KERBEROS.equalsIgnoreCase(authMode)) {
            log.error("do not support to config default knowledge base connection with kerberos");
            throw new AgentBaseException(ErrorCode.INVALID_KNOWLEDGE_REPO_REQUEST, KERBEROS);
        }
        if (!body.isChanged()) {
            // 若未改变参数，则直接返回
            return new UpdateDefaultKnowledgeBaseConnectionResponse().setId(kbConnectionId);
        }
        if (!Strings.CS.equals(kbConnectionId, DEFAULT_CONNECTION_ID)) {
            // 当前先只支持更新默认LakeSearch，其余不支持
            log.error("do not support to update config default knowledge base connection with connectionId [{}]",
                body.getConnectorId());
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR);
        }
        // 校验参数与连接器中定义的参数是否一致并加密
        List<ConnectionParamInfo> connectionParamInfos = knowledgeBaseConnectionValidateService.checkAndEncryptedParams(
            body.getConnectorId(), body.getParams());
        body.setParams(connectionParamInfos);
        // 入库
        KnowledgeBaseConnectionEntity entity = kbConnectionConverter.updateBodytoKnowledgeBaseConnectionEntity(body);
        entity.setId(kbConnectionId);
        connectionMapper.update(entity);
        return new UpdateDefaultKnowledgeBaseConnectionResponse().setId(kbConnectionId);
    }

    private LakeSearchConnection convertToLakeSearchConnection(TestDefaultKnowledgeBaseConnectionRequestBody body) {
        KnowledgeBaseConnectionEntity knowledgeBaseConnectionEntity
            = kbConnectionConverter.requestBodyToKbConnectionEntity(body);
        List<KnowledgeBaseConnectionParam> knowledgeBaseConnectionParams
            = kbConnectionConverter.connectionParamInfos2kbConnectionParams(body.getParams());
        knowledgeBaseConnectionEntity.setParams(knowledgeBaseConnectionParams);
        return lakeSearchConnectionProvider.convertToLakeSearchConnection(knowledgeBaseConnectionEntity);
    }

    private void checkPermission() {
        if (DeploymentModeEnum.isHc(envType)) {
            // hc场景下，任何用户禁止改变默认知识库配置
            log.error("user can not change default config");
            throw new AgentBaseException(ErrorCode.NO_PERMISSION);
        }
    }

    public void checkDefaultKbConnectionParams(List<ConnectionParamInfo> params) {
        if (params == null || params.isEmpty()) {
            return;
        }
        params.forEach(this::validateSingleItemAndThrow);
        String authModeParamValue = params.stream()
            .filter(item -> AUTH_MODE.equals(item.getCode()))
            .map(ConnectionParamInfo::getValue)
            .findFirst()
            .orElse(null);
        if (RagAuthMode.BASIC.toString().equals(authModeParamValue)) {
            boolean isAuthorizationValueInvalid = params.stream()
                .filter(item -> AUTHORIZATION.equals(item.getCode()))
                .findFirst()
                .map(ConnectionParamInfo::getValue)
                .filter(org.springframework.util.StringUtils::hasLength)
                .isEmpty();
            if (isAuthorizationValueInvalid) {
                log.error("When the authentication mode is BASIC, the authorization field ({}) cannot be empty.",
                    AUTHORIZATION);
                throw new AgentBaseException(ErrorCode.INVALID_PARAMETER, AUTHORIZATION);
            }
        }
    }

    private void validateSingleItemAndThrow(ConnectionParamInfo item) {
        if (item.getCode() == null || item.getCode().trim().isEmpty()) {
            log.error("code is null");
            throw new AgentBaseException(ErrorCode.INVALID_PARAMETER, "code");
        }

        if (REQUIRED_FIELDS.contains(item.getCode())) {
            if (!org.springframework.util.StringUtils.hasLength(item.getValue())) {
                log.error("{} is null", item.getCode());
                throw new AgentBaseException(ErrorCode.INVALID_PARAMETER, item.getCode());
            }
        }
    }

}
