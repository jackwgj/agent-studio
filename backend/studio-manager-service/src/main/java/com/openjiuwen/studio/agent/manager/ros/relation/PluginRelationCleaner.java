/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.ros.relation;

import com.openjiuwen.studio.agent.manager.dto.ToolCredential;
import com.openjiuwen.studio.agent.manager.mapper.ShareResourceMapper;
import com.openjiuwen.studio.agent.manager.mapper.ShareScopeMapper;
import com.openjiuwen.studio.agent.manager.mapper.ToolCredentialMapper;
import com.openjiuwen.studio.agent.manager.mapper.ros.ResourceCleanMapper;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * PluginRelationCleanner
 *
 */
@Service("Plugin_RelateResourceCleaner")
public class PluginRelationCleaner implements IRelateSourceCleaner {
    @Autowired
    MgObsService obsService;

    @Autowired
    private ResourceCleanMapper dataCleanMapper;

    @Autowired
    private ShareScopeMapper shareScopeMapper;

    @Autowired
    private ShareResourceMapper shareResourceMapper;

    @Autowired
    private ToolCredentialMapper toolCredentialMapper;

    @Override
    public void clean(List<String> ids, String domainId) {
        if (ids != null && ids.size() > 0) {
            // 删除obs
            for (String id : ids) {
                if (!StringUtils.isEmpty(id)) {
                    obsService.deleteByPrefix(String.format("Plugin/dsl/%s/", id));
                    obsService.deleteByPrefix(String.format("tool/dsl/%s/", id));
                    obsService.deleteByPrefix(String.format("temp/tools/openapi/%s", id));
                    shareResourceMapper.deleteByPrimaryKey(id);
                    shareScopeMapper.deleteByResourceId(id);
                }
            }

            List<ToolCredential> credentials = toolCredentialMapper.selectUserIdByDomainId(domainId);
            toolCredentialMapper.deleteByDomainId(domainId);
            for (ToolCredential credential : credentials) {
                obsService.deleteByPrefix(String.format("credential/ir/%s", credential.getCreatorId()));
            }
            // 删除 t_release_version
            dataCleanMapper.batchDeleteByIds("t_release_version", "app_id", ids);
        }
    }
}
