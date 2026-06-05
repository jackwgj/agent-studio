/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.jiuwen.adapt;

import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.common.dto.auth.AuthInfo;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;
import com.openjiuwen.studio.agent.manager.utils.IRUtils;
import com.openjiuwen.studio.agent.manager.utils.JsonUtils;
import com.openjiuwen.studio.agent.manager.utils.WorkflowUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * http节点转换IR
 *
 */
public class HttpNodeAdapter extends AbstractIRNodeAdapter {
    /**
     * 请求方法，支持POST，GET
     */
    private static final String METHOD = "method";

    /**
     * url
     */
    private static final String URL = "url";

    /**
     * 请求方式，支持JSON和NONE
     */
    private static final String REQUEST_TYPE = "request_type";

    /**
     * 请求体
     */
    private static final String REQUEST_BODY = "request_body";

    /**
     * ei鉴权方式
     */
    private static final String AUTH_INFO = "auth_info";

    /**
     * 九问鉴权方式
     */
    private static final String AUTH = "auth";

    @Override
    public Map<String, Object> adaptConfig(WorkflowNodeVO workflowNodeVo) {
        Map<String, Object> nodeConfigs = workflowNodeVo.getConfigs();
        Map<String, Object> configs = new HashMap<>();

        String path = nodeConfigs.get(CommonConstant.Http.PATH) != null
            ? nodeConfigs.get(CommonConstant.Http.PATH).toString() : StringUtils.EMPTY;
        configs.put(URL, WorkflowUtils.parseHttpEndpoint(nodeConfigs) + path);
        configs.put(METHOD, nodeConfigs.get(METHOD));
        if (nodeConfigs.get(AUTH_INFO) != null) {
            AuthInfo authInfo = JsonUtils.objectToClassType(nodeConfigs.get(AUTH_INFO), AuthInfo.class);
            configs.put(AUTH, WorkflowUtils.parseAuthInfo(authInfo));
        } else {
            configs.put(AUTH, new HashMap<>());
        }
        configs.put(IRUtils.adaptKey(REQUEST_TYPE), nodeConfigs.get(REQUEST_TYPE));
        configs.put(IRUtils.adaptKey(REQUEST_BODY), nodeConfigs.get(REQUEST_BODY));

        if (nodeConfigs.get(CommonConstant.Http.EXCEPTION_ENABLE) != null) {
            configs.put(IRUtils.adaptKey(CommonConstant.Http.EXCEPTION_ENABLE),
                nodeConfigs.get(CommonConstant.Http.EXCEPTION_ENABLE));
            configs.put(IRUtils.adaptKey(CommonConstant.Http.EXCEPTION_SUPPRESSION),
                nodeConfigs.get(CommonConstant.Http.EXCEPTION_SUPPRESSION));
        }
        return configs;
    }

    @Override
    public String getNodeType() {
        return NodeType.HTTP.getIrType();
    }
}
