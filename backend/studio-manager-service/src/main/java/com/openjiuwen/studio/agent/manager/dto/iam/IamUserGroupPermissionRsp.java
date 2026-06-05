/* * Copyright (c) Huawei Technologies Co., Ltd. 2021-2025. All rights reserved. */

package com.openjiuwen.studio.agent.manager.dto.iam;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Data;

import java.util.List;

/**
 * @description 查询iam用户组对应权限响应
 * @since 2025/9/11
 **/
@Data
public class IamUserGroupPermissionRsp {

    @JSONField(name = "roles")
    private List<IamRole> roles;

    @JSONField(name = "links")
    private IamLinks links;
}
