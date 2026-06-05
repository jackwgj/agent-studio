/* * Copyright (c) Huawei Technologies Co., Ltd. 2021-2025. All rights reserved. */

package com.openjiuwen.studio.agent.manager.dto.iam;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 添加类的描述
 *
 */
@Data
@AllArgsConstructor
public class IamUserInfo {

    @JSONField(name ="domain_id")
    private String domainId;

    @JSONField(name ="name")
    private String name;

    @JSONField(name ="description")
    private String description;

    @JSONField(name ="id")
    private String id;

    public IamUserInfo() {
    }
}
