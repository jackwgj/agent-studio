/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.common.service.model.tenant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
public class Tenant {

    /**
     * IAM账号ID
     */
    @JsonProperty("domain_id")
    @Size(max = 100)
    private String domainId;

    /**
     * 租户名称
     */
    @JsonProperty("domain_name")
    @Size(max = 100)
    private String domainName;

    /**
     * 租户名称
     */
    @JsonProperty("tenant_type")
    @Size(max = 1)
    private String tenantType;

    /**
     * 租户状态
     */
    @JsonProperty("status")
    @Size(max = 100)
    private String status;

    /**
     * 描述
     */
    @JsonProperty("description")
    @Size(max = 1000)
    private String description;

    /**
     * 扩展字段
     */
    @JsonProperty("ext_info")
    @Size(max = 1000)
    private String extInfo;

}
