package com.openjiuwen.studio.agent.space.dao.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.sql.Timestamp;

/**
 * 基础表字段
 */
@Data
public class BaseProperties {
    @JsonProperty("created_date")
    private Timestamp createdDate;

    @JsonProperty("created_by_user_id")
    private String createdByUserId;

    @JsonProperty("last_updated_date")
    private Timestamp lastUpdatedDate;

    @JsonProperty("last_updated_by_user_id")
    private String lastUpdatedByUserId;

    @JsonProperty("domain_id")
    private String domainId;

    private Boolean deleted = false;

    @JsonProperty("dept_code")
    private String deptCode;
}
