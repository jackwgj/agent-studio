/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.api.vo.convert;

import com.openjiuwen.studio.agent.space.api.vo.response.AgentSummaryInfo;
import com.openjiuwen.studio.agent.space.api.vo.response.QueryManagerAgentResp;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface ManagerServiceAgentConvert {
    List<AgentSummaryInfo> convert(List<QueryManagerAgentResp.AgentListBean> agentList);

    @Mapping(target = "id", source = "agentId")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "icon", source = "icon")
    AgentSummaryInfo convert(QueryManagerAgentResp.AgentListBean agent);
}
