/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.agentbase.converter;

import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeBaseConnectionEntity;
import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeBaseConnectorEntity;
import com.openjiuwen.studio.agent.agentbase.enums.KnowledgeBaseConnectorParamType;
import com.openjiuwen.studio.agent.agentbase.model.KnowledgeBaseConnectionParam;
import com.openjiuwen.studio.agent.agentbase.model.KnowledgeBaseConnectorParam;
import com.openjiuwen.studio.agent.manager.dto.ConnectionParamInfo;
import com.openjiuwen.studio.agent.manager.dto.DefaultKnowledgeBaseConnectionDetail;
import com.openjiuwen.studio.agent.manager.dto.KnowledgeBaseConnectorDetail;
import com.openjiuwen.studio.agent.manager.dto.ParamDefinitionInfo;
import com.openjiuwen.studio.agent.manager.dto.TestDefaultKnowledgeBaseConnectionRequestBody;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class KbConnectionConverterImpl implements KbConnectionConverter {

    @Override
    public DefaultKnowledgeBaseConnectionDetail toDefaultKnowledgeBaseConnectionDetail(
        KnowledgeBaseConnectionEntity knowledgeBaseConnectionEntity) {
        if (knowledgeBaseConnectionEntity == null) {
            return null;
        }

        DefaultKnowledgeBaseConnectionDetail defaultKnowledgeBaseConnectionDetail
            = new DefaultKnowledgeBaseConnectionDetail();

        defaultKnowledgeBaseConnectionDetail.setCreateUserId(knowledgeBaseConnectionEntity.getCreatedUserId());
        defaultKnowledgeBaseConnectionDetail.setCreateUserName(knowledgeBaseConnectionEntity.getCreatedUserName());
        defaultKnowledgeBaseConnectionDetail.setId(knowledgeBaseConnectionEntity.getId());
        defaultKnowledgeBaseConnectionDetail.setConnectorId(knowledgeBaseConnectionEntity.getConnectorId());
        defaultKnowledgeBaseConnectionDetail.setConnectorName(knowledgeBaseConnectionEntity.getConnectorName());
        defaultKnowledgeBaseConnectionDetail.setWorkspaceId(knowledgeBaseConnectionEntity.getWorkspaceId());
        defaultKnowledgeBaseConnectionDetail.setDomainName(knowledgeBaseConnectionEntity.getDomainName());
        defaultKnowledgeBaseConnectionDetail.setCreateTime(knowledgeBaseConnectionEntity.getCreateTime());
        defaultKnowledgeBaseConnectionDetail.setUpdateTime(knowledgeBaseConnectionEntity.getUpdateTime());
        defaultKnowledgeBaseConnectionDetail.setUpdateUserId(knowledgeBaseConnectionEntity.getUpdateUserId());
        defaultKnowledgeBaseConnectionDetail.setUpdateUserName(knowledgeBaseConnectionEntity.getUpdateUserName());

        return defaultKnowledgeBaseConnectionDetail;
    }

    @Override
    public ConnectionParamInfo kbConnectionParam2ConnectionParamInfo(
        KnowledgeBaseConnectionParam knowledgeBaseConnectionParams) {
        if (knowledgeBaseConnectionParams == null) {
            return null;
        }

        ConnectionParamInfo connectionParamInfo = new ConnectionParamInfo();

        connectionParamInfo.setCode(knowledgeBaseConnectionParams.getCode());
        connectionParamInfo.setValue(knowledgeBaseConnectionParams.getValue());

        return connectionParamInfo;
    }

    @Override
    public KnowledgeBaseConnectorDetail toKnowledgeBaseConnectorDetail(
        KnowledgeBaseConnectorEntity knowledgeBaseConnectorEntity) {
        if (knowledgeBaseConnectorEntity == null) {
            return null;
        }

        KnowledgeBaseConnectorDetail knowledgeBaseConnectorDetail = new KnowledgeBaseConnectorDetail();

        knowledgeBaseConnectorDetail.setCreateUserId(knowledgeBaseConnectorEntity.getCreatedUserId());
        knowledgeBaseConnectorDetail.setCreateUserName(knowledgeBaseConnectorEntity.getCreatedUserName());
        knowledgeBaseConnectorDetail.setId(knowledgeBaseConnectorEntity.getId());
        knowledgeBaseConnectorDetail.setName(knowledgeBaseConnectorEntity.getName());
        knowledgeBaseConnectorDetail.setIcon(knowledgeBaseConnectorEntity.getIcon());
        knowledgeBaseConnectorDetail.setDescription(knowledgeBaseConnectorEntity.getDescription());
        knowledgeBaseConnectorDetail.setDeployMode(knowledgeBaseConnectorEntity.getDeployMode());
        knowledgeBaseConnectorDetail.setHelpText(knowledgeBaseConnectorEntity.getHelpText());
        knowledgeBaseConnectorDetail.setWorkspaceId(knowledgeBaseConnectorEntity.getWorkspaceId());
        knowledgeBaseConnectorDetail.setDomainName(knowledgeBaseConnectorEntity.getDomainName());
        knowledgeBaseConnectorDetail.setCreateTime(knowledgeBaseConnectorEntity.getCreateTime());
        knowledgeBaseConnectorDetail.setUpdateUserId(knowledgeBaseConnectorEntity.getUpdateUserId());
        knowledgeBaseConnectorDetail.setUpdateUserName(knowledgeBaseConnectorEntity.getUpdateUserName());
        knowledgeBaseConnectorDetail.setUpdateTime(knowledgeBaseConnectorEntity.getUpdateTime());

        return knowledgeBaseConnectorDetail;
    }

    @Override
    public List<ParamDefinitionInfo> connectorParams2ParamDefinitionInfos(
        List<KnowledgeBaseConnectorParam> knowledgeBaseConnectorParams) {
        if (knowledgeBaseConnectorParams == null) {
            return null;
        }

        List<ParamDefinitionInfo> list = new ArrayList<ParamDefinitionInfo>(knowledgeBaseConnectorParams.size());
        for (KnowledgeBaseConnectorParam knowledgeBaseConnectorParam : knowledgeBaseConnectorParams) {
            list.add(connectorParam2ParamDefinitionInfo(knowledgeBaseConnectorParam));
        }

        return list;
    }

    @Override
    public ParamDefinitionInfo connectorParam2ParamDefinitionInfo(
        KnowledgeBaseConnectorParam knowledgeBaseConnectorParam) {
        if (knowledgeBaseConnectorParam == null) {
            return null;
        }

        ParamDefinitionInfo paramDefinitionInfo = new ParamDefinitionInfo();

        paramDefinitionInfo.setCode(knowledgeBaseConnectorParam.getCode());
        paramDefinitionInfo.setName(knowledgeBaseConnectorParam.getName());
        paramDefinitionInfo.setNeed(knowledgeBaseConnectorParam.getNeed());
        paramDefinitionInfo.setType(knowledgeBaseConnectorParamTypeToTypeEnum(knowledgeBaseConnectorParam.getType()));
        paramDefinitionInfo.setRemark(knowledgeBaseConnectorParam.getRemark());
        paramDefinitionInfo.setPattern(knowledgeBaseConnectorParam.getPattern());

        return paramDefinitionInfo;
    }

    @Override
    public KnowledgeBaseConnectionEntity requestBodyToKbConnectionEntity(
        TestDefaultKnowledgeBaseConnectionRequestBody body) {
        if (body == null) {
            return null;
        }

        KnowledgeBaseConnectionEntity knowledgeBaseConnectionEntity = new KnowledgeBaseConnectionEntity();

        knowledgeBaseConnectionEntity.setConnectorId(body.getConnectorId());

        return knowledgeBaseConnectionEntity;
    }

    @Override
    public KnowledgeBaseConnectionParam connectionParamInfo2kbConnectionParam(ConnectionParamInfo connectionParamInfo) {
        if (connectionParamInfo == null) {
            return null;
        }

        KnowledgeBaseConnectionParam.KnowledgeBaseConnectionParamBuilder knowledgeBaseConnectionParam
            = KnowledgeBaseConnectionParam.builder();

        knowledgeBaseConnectionParam.code(connectionParamInfo.getCode());
        knowledgeBaseConnectionParam.value(connectionParamInfo.getValue());

        return knowledgeBaseConnectionParam.build();
    }

    @Override
    public List<KnowledgeBaseConnectionParam> connectionParamInfos2kbConnectionParams(
        List<ConnectionParamInfo> connectionParamInfos) {
        if (connectionParamInfos == null) {
            return null;
        }

        List<KnowledgeBaseConnectionParam> list = new ArrayList<KnowledgeBaseConnectionParam>(
            connectionParamInfos.size());
        for (ConnectionParamInfo connectionParamInfo : connectionParamInfos) {
            list.add(connectionParamInfo2kbConnectionParam(connectionParamInfo));
        }

        return list;
    }

    protected ParamDefinitionInfo.TypeEnum knowledgeBaseConnectorParamTypeToTypeEnum(
        KnowledgeBaseConnectorParamType knowledgeBaseConnectorParamType) {
        if (knowledgeBaseConnectorParamType == null) {
            return null;
        }

        ParamDefinitionInfo.TypeEnum typeEnum;

        switch (knowledgeBaseConnectorParamType) {
            case STRING:
                typeEnum = ParamDefinitionInfo.TypeEnum.STRING;
                break;
            case SECRET:
                typeEnum = ParamDefinitionInfo.TypeEnum.SECRET;
                break;
            case BOOLEAN:
                typeEnum = ParamDefinitionInfo.TypeEnum.BOOLEAN;
                break;
            case FILE:
                typeEnum = ParamDefinitionInfo.TypeEnum.FILE;
                break;
            default:
                throw new IllegalArgumentException("Unexpected enum constant: " + knowledgeBaseConnectorParamType);
        }

        return typeEnum;
    }
}
