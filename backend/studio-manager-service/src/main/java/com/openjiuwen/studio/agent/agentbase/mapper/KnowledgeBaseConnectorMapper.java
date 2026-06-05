/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.mapper;

import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeBaseConnectorEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface KnowledgeBaseConnectorMapper {

    KnowledgeBaseConnectorEntity find(@Param("id") String id);

    List<KnowledgeBaseConnectorEntity> listKnowledgeBaseConnectors(@Param("deploy_mode") String deployMode);

}
