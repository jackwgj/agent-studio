/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * prompt标签表
 *
 * @TableName t_pe_tag
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeTag implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键UUID
     */
    private String id;

    /**
     * 名称/值，唯一
     */
    private String name;

    /**
     * 英文标签名称
     */
    private String nameEn;

    /**
     * 创建时间
     */
    private Date createdOn;

    /**
     * 更新时间
     */
    private Date updatedOn;

    private String workspaceId;
}
