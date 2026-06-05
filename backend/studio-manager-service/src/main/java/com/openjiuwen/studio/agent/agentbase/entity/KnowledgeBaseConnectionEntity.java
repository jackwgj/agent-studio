/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.agentbase.enums.KnowledgeBaseConnectionStatus;
import com.openjiuwen.studio.agent.agentbase.model.AbilityDetail;
import com.openjiuwen.studio.agent.agentbase.model.KnowledgeBaseConnectionParam;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Setter
public class KnowledgeBaseConnectionEntity extends BaseEntity {

    @JsonProperty("id")
    private String id;

    @JsonProperty("connector_id")
    private String connectorId;

    @JsonProperty("connector_name")
    private String connectorName;

    @JsonProperty("connector_type")
    private String connectorType;

    @JsonProperty("name")
    private String name;

    @JsonProperty("status")
    private KnowledgeBaseConnectionStatus status;

    @JsonProperty("icon")
    private String icon;

    @JsonProperty("description")
    private String description;

    @JsonProperty("params")
    private List<KnowledgeBaseConnectionParam> params;

    @JsonProperty("krb5_file")
    private String krb5File;

    @JsonProperty("keytab_file")
    private String keytabFile;

    @JsonProperty("knowledge_base_capacity")
    private Integer knowledgeBaseCapacity = 1000000;

    @JsonProperty("domain_id")
    private String domainId;

    @JsonProperty("used_abilities")
    private List<AbilityDetail> usedAbilities;
}
