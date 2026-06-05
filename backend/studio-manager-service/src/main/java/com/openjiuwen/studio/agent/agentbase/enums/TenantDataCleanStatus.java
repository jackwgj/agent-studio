/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.enums;

import lombok.Getter;

@Getter
public enum TenantDataCleanStatus {
    SUCCESS("1", "成功"),
    FAILED("0", "失败"),
    PROCESSING("2", "清理中"); // 最后一个用分号结束

    private final String code; // 存 "1", "0", "2"

    private final String desc; // 存 "成功", "失败", "清理中"

    TenantDataCleanStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
