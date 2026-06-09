/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 工作流和前端交互对象定义。
 */
@ApiModel(description = "工作流和前端交互对象定义。")

@Validated

public class WorkflowVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    @NotBlank
    private String id = null;

    @JsonProperty("name")
    @NotBlank
    private String name = null;

    @JsonProperty("description")
    private String description = null;

    @JsonProperty("icon")
    private String icon = null;

    @JsonProperty("nodes")
    @Valid
    @NotNull
    @Size()
    private List<WorkflowNodeVO> nodes = new ArrayList<WorkflowNodeVO>();

    @JsonProperty("edges")
    @Valid
    @Size()
    private List<WorkflowEdgeVO> edges = null;

    @JsonProperty("layouts")
    @Valid
    @Size()
    private Map<String, Object> layouts = null;

    @JsonProperty("configs")
    @Valid
    @Size()
    private Map<String, Object> configs = null;

    @JsonProperty("comments")
    @Valid
    @Size()
    private List<Object> comments = null;

    public String getId() {
        return id;
    }

    public WorkflowVO setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public WorkflowVO setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public WorkflowVO setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getIcon() {
        return icon;
    }

    public WorkflowVO setIcon(String icon) {
        this.icon = icon;
        return this;
    }

    public List<WorkflowNodeVO> getNodes() {
        return nodes;
    }

    public WorkflowVO setNodes(List<WorkflowNodeVO> nodes) {
        this.nodes = nodes;
        return this;
    }

    public List<WorkflowEdgeVO> getEdges() {
        return edges;
    }

    public WorkflowVO setEdges(List<WorkflowEdgeVO> edges) {
        this.edges = edges;
        return this;
    }

    public Map<String, Object> getLayouts() {
        return layouts;
    }

    public WorkflowVO setLayouts(Map<String, Object> layouts) {
        this.layouts = layouts;
        return this;
    }

    public Map<String, Object> getConfigs() {
        return configs;
    }

    public WorkflowVO setConfigs(Map<String, Object> configs) {
        this.configs = configs;
        return this;
    }

    public List<Object> getComments() {
        return comments;
    }

    public WorkflowVO setComments(List<Object> comments) {
        this.comments = comments;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class WorkflowVO {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    icon: ").append(toIndentedString(icon)).append("\n");
        sb.append("    nodes: ").append(toIndentedString(nodes)).append("\n");
        sb.append("    edges: ").append(toIndentedString(edges)).append("\n");
        sb.append("    layouts: ").append(toIndentedString(layouts)).append("\n");
        sb.append("    configs: ").append(toIndentedString(configs)).append("\n");
        sb.append("    comments: ").append(toIndentedString(comments)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WorkflowVO workflowVO = (WorkflowVO) o;
        return Objects.equals(this.id, workflowVO.id) && Objects.equals(this.name, workflowVO.name) && Objects.equals(
            this.description, workflowVO.description) && Objects.equals(this.icon, workflowVO.icon) && Objects.equals(
            this.nodes, workflowVO.nodes) && Objects.equals(this.edges, workflowVO.edges) && Objects.equals(
            this.layouts, workflowVO.layouts) && Objects.equals(this.configs, workflowVO.configs) && Objects.equals(
            this.comments, workflowVO.comments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, icon, nodes, edges, layouts, configs, comments);
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
