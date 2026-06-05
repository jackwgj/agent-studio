/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.openjiuwen.studio.agent.runtime.entity.KnowledgeBaseConnectionEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 知识库连接 Mapper
 *
 * @since 2026-03-22
 */
@Mapper
public interface KnowledgeBaseConnectionMapper extends BaseMapper<KnowledgeBaseConnectionEntity> {

    KnowledgeBaseConnectionEntity findById(@Param("knowledge_base_id") String knowledgeBaseId);

}