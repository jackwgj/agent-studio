/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.ros.relation;

import java.util.List;

/**
 * 相关资源清理接口
 *
 */
public interface IRelateSourceCleaner {
    void clean(List<String> ids, String domainId);
}
