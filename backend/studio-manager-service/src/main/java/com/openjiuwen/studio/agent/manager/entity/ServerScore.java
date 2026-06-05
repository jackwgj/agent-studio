/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2023. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.entity;

import lombok.Data;

@Data
public class ServerScore {

    private long times;

    private long totalScore;

    private String serverId;

}

