/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.dto.knowledge;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class FileInfo {

    /**
     * 文件 ID
     */
    private String fileId;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 文件大小（字节）
     * 注意：JSON 中为 0，Java 中通常用 Long 或 BigInteger 以防溢出，这里根据上下文保持 Long
     */
    private Long fileSize;

    /**
     * 文件状态
     */
    private String fileStatus;

    /**
     * 失败原因（仅在状态为失败时可能有值）
     */
    private String failureReason;

    /**
     * 文件标签列表
     */
    private List<String> fileTags;

    /**
     * 元数据（JSON 字符串或对象，此处按 JSON 字符串处理）
     */
    private String metadata;

    /**
     * 创建时间（Unix 时间戳）
     */
    private Long createTime;

    /**
     * 更新时间（Unix 时间戳）
     */
    private Long updateTime;
}
