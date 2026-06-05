/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.dto.iam;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Data;

@Data
public class DeveloperUserInfo {
    @JSONField(name ="domain_id")
    private String domainId;

    @JSONField(name ="domain_name")
    private String domainName;

    @JSONField(name ="user_id")
    private String userId;

    @JSONField(name ="user_id_type")
    private String userIdType;

    @JSONField(name ="user_name")
    private String userName;

    @JSONField(name ="project_id")
    private String projectId;

    @JSONField(name ="project_name")
    private String projectName;

}
