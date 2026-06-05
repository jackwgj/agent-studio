package com.openjiuwen.studio.agent.space.api.vo.response.resource;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ManagerTenantInfo {
    @JsonProperty("domain_id")
    private String domainId;

    @JsonProperty("domain_name")
    private String domainName;

    @JsonProperty("tenant_type")
    private String tenantType;

    @JsonProperty("description")
    private String description;

    @JsonProperty("ext_info")
    private String extInfo;
}
