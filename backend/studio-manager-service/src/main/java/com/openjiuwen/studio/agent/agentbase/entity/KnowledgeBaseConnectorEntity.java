/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.agentbase.model.KnowledgeBaseConnectorParam;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * 第三方知识库连接器实体
 *
 * @since 2025-08-15
 */

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class KnowledgeBaseConnectorEntity extends BaseEntity {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("icon")
    private String icon;

    @JsonProperty("description")
    private String description;

    @JsonProperty("deploy_mode")
    private String deployMode;

    @JsonProperty("type")
    private String type;

    @JsonProperty("param_definition")
    private List<KnowledgeBaseConnectorParam> params;

    @JsonProperty("help_text")
    private String helpText;

}
