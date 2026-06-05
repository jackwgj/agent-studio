/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service.mcp.apigservice;

import com.openjiuwen.studio.agent.manager.service.mcp.apigservice.bean.ApigRequsetDto;
import com.huaweicloud.sdk.apig.v2.model.CreateApiV2Response;
import com.huaweicloud.sdk.apig.v2.model.DeleteApiV2Response;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

public interface ApiMcpService {
    @Transactional
    CreateApiV2Response createApi(ApigRequsetDto functionDto);

    @Transactional
    ResponseEntity<DeleteApiV2Response> deleteApi(ApigRequsetDto routerVo);
}
