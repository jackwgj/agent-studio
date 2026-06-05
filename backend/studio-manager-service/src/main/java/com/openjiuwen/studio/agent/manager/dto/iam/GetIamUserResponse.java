/* * Copyright (c) Huawei Technologies Co., Ltd. 2021-2025. All rights reserved. */

package com.openjiuwen.studio.agent.manager.dto.iam;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Data;

import java.util.List;

/**
 * 添加类的描述
 *
 */
@Data
public class GetIamUserResponse {

    @JSONField(name = "users")
    private List<IamUserInfo> users;

    public GetIamUserResponse() {
    }
}
