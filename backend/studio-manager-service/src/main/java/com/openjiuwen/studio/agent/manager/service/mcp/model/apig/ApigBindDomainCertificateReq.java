/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.mcp.model.apig;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * APIG 查询专享版实例列表响应类
 *
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApigBindDomainCertificateReq {
    /**
     * 证书内容
     */
    @JsonProperty("certificate_ids")
    @Size(max = 1000)
    private List<String> certificateIds;
}
