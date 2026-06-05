/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.foundation.connection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 连接器的定义。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConnectorDefinition {

    /**
     * 连接器编码，必须唯一。
     */
    private String code;

    private String connectionId;

    private String domainId;

    private String name;

    private String desc;

    private List<ConnectorParamDefinition> paramDefinitions;

}
