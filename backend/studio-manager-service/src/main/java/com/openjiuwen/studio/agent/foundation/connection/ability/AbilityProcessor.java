/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.foundation.connection.ability;

import com.openjiuwen.studio.agent.foundation.connection.constants.AbilityTypeEnum;

public interface AbilityProcessor {

    /**
     * 返回能力名称
     *
     * @return
     */
    AbilityTypeEnum ability();

    /**
     * 返回处理子能力的结果
     *
     * @return
     */
    String processSubAbility(String userSubAbility, String metaSubAbility);
}
