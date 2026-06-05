/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.dto.knowledge;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ListFaqFileReq {

    /**
     * 文件名称（支持模糊查询或精确匹配）
     */
    private String fileName;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 文件状态
     */
    private String fileStatus;

    /**
     * 当前页码（通常从 1 开始，但 JSON 中为 0，此处保持 Long 或 Integer 均可，建议用 Integer）
     */
    private Integer pageNum;

    /**
     * 每页大小
     */
    private Integer pageSize;

    /**
     * 文件 ID 列表
     */
    private List<String> ids;
}
