/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.ros.relation;

import com.openjiuwen.studio.agent.manager.mapper.ros.ResourceCleanMapper;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("Workflow_RelateResourceCleaner")
public class WorkflowRelationCleaner implements IRelateSourceCleaner {

    @Autowired
    MgObsService obsService;

    @Autowired
    private ResourceCleanMapper dataCleanMapper;

    @Override
    public void clean(List<String> ids, String domainId) {
        if (ids.size() > 0) {
            // 删除obs
            for (String id : ids) {
                if (!StringUtils.isEmpty(id)) {
                    obsService.deleteByPrefix(String.format("workflow/flow/%s/", id));
                    obsService.deleteByPrefix(String.format("workflow/ir/%s/", id));
                }
            }
            // 删除 t_release_version
            dataCleanMapper.batchDeleteByIds("t_release_version", "app_id", ids);

            // 删除 t_mapping
            dataCleanMapper.batchDeleteByIds("t_mapping", "app_id", ids);

            // 删除 t_release_channel
            dataCleanMapper.batchDeleteByIds("t_release_channel", "app_id", ids);
        }
    }
}
