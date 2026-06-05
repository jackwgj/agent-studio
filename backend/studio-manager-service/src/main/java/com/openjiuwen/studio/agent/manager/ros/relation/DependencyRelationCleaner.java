/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.ros.relation;

import com.openjiuwen.studio.agent.manager.obs.MgObsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("Dependency_RelateResourceCleaner")
public class DependencyRelationCleaner implements IRelateSourceCleaner {

    @Autowired
    MgObsService obsService;

    @Override
    public void clean(List<String> ids, String domainId) {
        // 删除obs
        obsService.deleteByPrefix(String.format("function/dependency/%s/", domainId));
    }
}
