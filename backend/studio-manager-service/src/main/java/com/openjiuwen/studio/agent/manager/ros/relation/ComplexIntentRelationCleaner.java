/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.ros.relation;

import com.openjiuwen.studio.agent.manager.mapper.ros.ResourceCleanMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 复杂意图相关表清理类
 *
 */
@Service("ComplexIntent_RelateResourceCleaner")
public class ComplexIntentRelationCleaner implements IRelateSourceCleaner {

    @Autowired
    private ResourceCleanMapper resourceCleanMapper;

    @Override
    public void clean(List<String> ids, String domainId) {
        if (ids.size() > 0) {
            // 删除 t_complex_intent_branch
            resourceCleanMapper.batchDeleteByIds("t_complex_intent_branch", "intent_id", ids);
        }
    }
}
