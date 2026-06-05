/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.api.vo.response;

import com.openjiuwen.studio.agent.space.api.vo.McpServerBaseInfoVo;
import com.openjiuwen.studio.agent.space.api.vo.request.PageInfo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * MCP server 列表信息
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class McpServerBaseInfoListResp extends PageInfo {
    private List<McpServerBaseInfoVo> mcps;

    private long total;
}
