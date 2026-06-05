/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
public class ApiKeysEntity {
    /**
     * 唯一标识
     */
    private String apiKeyId;

    /**
     * 名称
     */
    private String apiKeyName;

    /**
     * APIKey的值
     */
    private String apiKeyValue;

    private String description;

    /**
     * 创建人ID
     */
    private String createdByUserName;

    /**
     * 最近使用人
     */
    private String lastUpdatedByUserName;

    private String projectId;

    private String workspaceId;

    /**
     * DOMAIN_ID
     */
    private String domainId;

    private String userId;

    /**
     * 创建时间
     */
    private long createdDate;


    /**
     * 用户名
     */
    private String userName;

}
