/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.foundation.connection.utils;

import jakarta.validation.constraints.NotNull;

/**
 * 连接器参数处理类
 *
 * @since 2025-08-25
 */
public class ConnectorParamUtil {

    /**
     * 第三方知识库中知识库详情页中的id占位符
     */
    public static final String EXTERNAL_ID_PLACE_HOLDER = "{{id}}";

    public static String getDetailPageUrl(@NotNull String externalId, String detailPagePath) {
        return detailPagePath.replace(EXTERNAL_ID_PLACE_HOLDER, externalId);
    }

}
