/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.dto.iam;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Data;

@Data
public class DeveloperHeaderUserInfo {
    @JSONField(name ="iam_user_info")
    private DeveloperUserInfo iamUserInfo;
}
