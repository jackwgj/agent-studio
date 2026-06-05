/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.service.knowledgerepo;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.openjiuwen.studio.agent.runtime.entity.KnowledgeBaseEntity;
import com.openjiuwen.studio.agent.runtime.mapper.KnowledgeBaseMapper;

import org.springframework.stereotype.Service;

@Service
public class KnowledgeBaseServiceImpl extends ServiceImpl<KnowledgeBaseMapper, KnowledgeBaseEntity>
    implements KnowledgeBaseService {

}