/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.entity;

import lombok.Data;

@Data
public class KnowledgeBaseInfo {

    private String id;

    private String connectorId;

    private String knowledgeBaseConnectionId;

    private String params;

    private String externalId;

    private String status;

}
