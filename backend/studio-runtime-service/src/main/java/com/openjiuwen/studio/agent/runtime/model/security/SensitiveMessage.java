/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.model.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 功能描述
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SensitiveMessage {

    private String cluster;

    private String topic;

    private String groupId;

    private int partition;

    private long offset;

    private String key;

    private String message;

    private String val;

    private long timestamp;

    public SensitiveMessage getNewSensitiveMessage() {
        SensitiveMessage sensitiveMessage = new SensitiveMessage();
        sensitiveMessage.setKey(new String(Base64.getDecoder().decode(getKey()), StandardCharsets.UTF_8));
        sensitiveMessage.setVal(new String(Base64.getDecoder().decode(getVal()), StandardCharsets.UTF_8));
        sensitiveMessage.setMessage(new String(Base64.getDecoder().decode(getMessage()), StandardCharsets.UTF_8));
        sensitiveMessage.setCluster(getCluster());
        sensitiveMessage.setGroupId(getCluster());
        sensitiveMessage.setOffset(getOffset());
        sensitiveMessage.setTopic(getTopic());
        sensitiveMessage.setPartition(getPartition());
        sensitiveMessage.setTimestamp(getTimestamp());
        return sensitiveMessage;
    }
}
