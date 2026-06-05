/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.manager.dto.TagInfo;

import lombok.Data;

import java.util.Date;

/**
 * 功能描述 标签数据库实体
 *
 */
@Data
public class Tag {
    /**
     * 标签唯一标识
     */
    @JsonProperty("tag_id")
    private String tagId;

    /**
     * 标签名称
     */
    private String name;

    /**
     * 标签英文名称
     */
    @JsonProperty("name_en")
    private String nameEn;

    /**
     * 标签创建时间
     */
    @JsonProperty("created_on")
    private Date createdOn;

    /**
     * 标签更新时间
     */
    @JsonProperty("updated_on")
    private Date updatedOn;

    /**
     * 转换为Dto
     */
    public TagInfo convertToDto() {
        TagInfo tagInfo = new TagInfo();
        tagInfo.setTagId(this.getTagId());
        tagInfo.setName(this.getName());
        tagInfo.setNameEn(this.getNameEn());
        return tagInfo;
    }
}
