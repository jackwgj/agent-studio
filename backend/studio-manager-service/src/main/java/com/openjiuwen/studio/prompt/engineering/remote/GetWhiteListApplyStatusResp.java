/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.remote;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 功能描述 获取服务白名单状态响应体
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetWhiteListApplyStatusResp {
    private String status;
}
