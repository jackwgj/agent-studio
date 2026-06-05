/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.openjiuwen.studio.agent.runtime.entity.KnowledgeBaseInfo;
import com.openjiuwen.studio.agent.runtime.entity.KnowledgeBaseEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 知识库 Mapper
 *
 * @since 2026-03-22
 */
@Mapper
public interface KnowledgeBaseMapper extends BaseMapper<KnowledgeBaseEntity> {

    List<KnowledgeBaseInfo> batchQueryKnowledgeBases(@Param("ids") List<String> knowledgeBaseIds);

    void delete(@Param("knowledge_base_id") String knowledgeBaseId);

    KnowledgeBaseInfo find(@Param("id") String id);

}