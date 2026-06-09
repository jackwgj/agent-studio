/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class StructuredMessageEntity {
    /**
     * 消息ID（主键）
     */
    @JsonProperty("id")
    private String id;

    /**
     * 消息名称
     */
    @JsonProperty("name")
    private String name;

    /**
     * 消息分类（如：异常）
     */
    @JsonProperty("category")
    private String category;

    /**
     * JSON格式的消息体
     */
    @JsonProperty("content")
    private JsonMap content;

    public Map<String, Object> getContentMap() {
        return content != null ? content.toMap() : null;
    }

    public void setContentMap(Map<String, Object> map) {
        this.content = JsonMap.from(map);
    }

    /**
     * 状态
     */
    @JsonProperty("status")
    private String status;

    /**
     * 导入方式（PAGE/UPLOAD/OTHER）
     */
    @JsonProperty("import_method")
    private String importMethod;

    /**
     * 可见范围（默认WORKSPACE）
     */
    @JsonProperty("visibility")
    private String visibility;

    /**
     * 工作空间ID
     */
    @JsonProperty("workspace_id")
    private String workspaceId;

    /**
     * 域ID
     */
    @JsonProperty("domain_id")
    private String domainId;

    /**
     * 用户ID
     */
    @JsonProperty("user_id")
    private String userId;

    /**
     * 项目ID（非空）
     */
    @JsonProperty("project_id")
    private String projectId;

    /**
     * 创建时间
     */
    @JsonProperty("created_on")
    private Date createdOn;

    /**
     * 创建者ID
     */
    @JsonProperty("creator_id")
    private String creatorId;

    /**
     * 更新时间
     */
    @JsonProperty("updated_on")
    private Date updatedOn;

    /**
     * 更新者ID
     */
    @JsonProperty("updater_id")
    private String updaterId;
}
