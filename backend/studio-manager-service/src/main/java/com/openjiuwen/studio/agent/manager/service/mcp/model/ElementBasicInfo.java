/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2023. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.mcp.model;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @description 模型参数
 * @since 2025/7/3
 **/
@Data
@Accessors(chain = true)
public class ElementBasicInfo {

    private String id;

    private String ownerType;

    private String ownerId;

    public String name;

    private String description;

    private String status;


    private String originalOwnerId;
}
