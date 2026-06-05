/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.knowledgesourceprovider;

import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeBaseConnectionEntity;
import com.openjiuwen.studio.agent.agentbase.mapper.KnowledgeBaseConnectionMapper;
import com.openjiuwen.studio.agent.agentbase.model.KnowledgeBaseConnectionParam;
import com.openjiuwen.studio.agent.common.dto.knowledge.KerberosAuth;
import com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.connection.LakeSearchConnection;
import com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.connection.LsBasicAuth;
import com.openjiuwen.studio.agent.common.enums.RagAuthMode;
import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;
import com.openjiuwen.studio.agent.foundation.base.exception.ErrorCode;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Slf4j
@Service
public class LakeSearchConnectionProvider extends KnowledgeSourceConnectionProvider {

    private static final String ENDPOINT = "endpoint";

    private static final String OCR_ENABLE = "ocr_enable";

    private static final String AUTH_MODE = "auth_mode";

    private static final String AUTHORIZATION = "authorization";

    private static final String HOST_NAMES = "host_names";

    private static final String CLUSTER_IPS = "cluster_ips";

    private static final String PORT = "port";

    private static final String PROTOCOL = "protocol";

    private static final String USER_KEYTAB_FILE = "user_keytab_file";

    private static final String KRB5_FILE = "krb5_file";

    protected LakeSearchConnectionProvider(KnowledgeBaseConnectionMapper knowledgeConnectionMapper) {
        super(knowledgeConnectionMapper);
    }

    @Override
    public LakeSearchConnection getKnowledgeSourceConnection(String connectionId) {
        try {

            KnowledgeBaseConnectionEntity knowledgeBaseConnectionEntity = queryKnowledgeConnectionFromDb(connectionId);
            return convertToLakeSearchConnection(knowledgeBaseConnectionEntity);
        } catch (Exception exception) {
            log.error("Fail to get KnowledgeBase Connection Info", exception.getMessage());
            throw new AgentBaseException(ErrorCode.FAILED_TO_FIND_DEFAULT_CONNECTION_INFO);
        }
    }

    public LakeSearchConnection convertToLakeSearchConnection(
        KnowledgeBaseConnectionEntity knowledgeBaseConnectionEntity) {
        List<KnowledgeBaseConnectionParam> knowledgeBaseConnectionParams = knowledgeBaseConnectionEntity.getParams();
        LakeSearchConnection lakeSearchConnection = new LakeSearchConnection();
        lakeSearchConnection.setConnectionId(knowledgeBaseConnectionEntity.getId());
        RagAuthMode ragAuthMode = null;
        // 识别auth_mode和通用配置项
        for (KnowledgeBaseConnectionParam knowledgeBaseConnectionParam : knowledgeBaseConnectionParams) {
            switch (knowledgeBaseConnectionParam.getCode()) {
                case ENDPOINT -> lakeSearchConnection.setEndpoint(knowledgeBaseConnectionParam.getValue());
                case OCR_ENABLE ->
                    lakeSearchConnection.setOcrEnable(Boolean.parseBoolean(knowledgeBaseConnectionParam.getValue()));
                case AUTH_MODE -> {
                    ragAuthMode = RagAuthMode.valueOf(
                        knowledgeBaseConnectionParam.getValue().toUpperCase(Locale.ENGLISH));
                    lakeSearchConnection.setAuthMode(ragAuthMode.toString());
                }

            }
        }
        switch (Objects.requireNonNull(ragAuthMode)) {
            case NONE -> {
            }
            case BASIC -> {
                LsBasicAuth lsBasicAuth = new LsBasicAuth();
                for (KnowledgeBaseConnectionParam knowledgeBaseConnectionParam : knowledgeBaseConnectionParams) {
                    switch (knowledgeBaseConnectionParam.getCode()) {
                        case AUTHORIZATION ->
                            lsBasicAuth.setLakeSearchAuthorization(knowledgeBaseConnectionParam.getValue());
                    }
                }
                lakeSearchConnection.setLsBasicAuth(lsBasicAuth);
            }
            case KERBEROS -> {
                KerberosAuth kerberosAuth = new KerberosAuth();
                for (KnowledgeBaseConnectionParam knowledgeBaseConnectionParam : knowledgeBaseConnectionParams) {
                    switch (knowledgeBaseConnectionParam.getCode()) {
                        case HOST_NAMES -> kerberosAuth.setHostNames(knowledgeBaseConnectionParam.getValue());
                        case CLUSTER_IPS -> kerberosAuth.setClusterIps(knowledgeBaseConnectionParam.getValue());
                        case PORT -> kerberosAuth.setPort(knowledgeBaseConnectionParam.getValue());
                        case PROTOCOL -> kerberosAuth.setProtocol(knowledgeBaseConnectionParam.getValue());
                        case USER_KEYTAB_FILE ->
                            kerberosAuth.setUserKeytabFile(knowledgeBaseConnectionParam.getValue());
                        case KRB5_FILE -> kerberosAuth.setKrb5File(knowledgeBaseConnectionParam.getValue());
                    }
                    lakeSearchConnection.setKerberosAuth(kerberosAuth);
                }
            }
            default -> {
                log.error("unknown lakesearch auth mode[{0}]", ragAuthMode);
                throw new AgentBaseException(ErrorCode.LAKE_SEARCH_AUTH_MODE_ERROR);
            }
        }
        lakeSearchConnection.isValid();
        return lakeSearchConnection;
    }
}
