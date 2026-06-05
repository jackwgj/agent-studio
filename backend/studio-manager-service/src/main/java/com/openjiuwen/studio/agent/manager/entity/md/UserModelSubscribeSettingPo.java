/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.entity.md;

import lombok.Data;

/**
 * UserModelSubscribeSetting
 *
 */
@Data
public class UserModelSubscribeSettingPo {
    private String id;

    private String domainId;

    private String projectId;

    private Integer isAutoSubscribeNewModel;

    private Long createdDate;

    private Long lastUpdatedDate;


    /**
     * isAutoSubscribeNewModel
     *
     * @return boolean
     */
    public boolean obtainIsAutoSubscribeNewModel() {
        return this.isAutoSubscribeNewModel != null && isAutoSubscribeNewModel == 1;
    }
}
