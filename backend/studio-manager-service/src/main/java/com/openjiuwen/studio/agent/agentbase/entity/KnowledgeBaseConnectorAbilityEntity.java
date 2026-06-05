/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.entity;

import lombok.Data;

/**
 * 知识库连接器的能力
 *
 * @since 2025-09-01
 */
@Data
public class KnowledgeBaseConnectorAbilityEntity {

    private String id;

    private String code;

    private String name;

    private String description;

    private String connectorId;

    private String subAbility;

}
