/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.bo;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

/**
 * 上传文件校验包装类
 *
 */
@Data
@Builder
public class FileCheckWrapper {
    /**
     * 文件大小(KB)
     */
    private long size;

    /**
     * 文件类型
     */
    private Set<String> type;
}
