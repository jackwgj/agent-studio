/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2023. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.mcp.model.vo;

import lombok.Data;

/**
 * 传递调用FG APP相关接口的相关参数
 *
 */
@Data
public class McpPollingTaskInfo {

    private String appId;

    private boolean isCreate;

    private boolean isDelete;
}

