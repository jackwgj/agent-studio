/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * WorkflowDetails
 */

@Validated

public class WorkflowDetails implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    @NotBlank
    @Length(max = 64)
    private String id = null;

    @JsonProperty("name")
    @NotBlank
    @Length(max = 64)
    private String name = null;

    @JsonProperty("description")
    @Length(max = 256)
    private String description = null;

    @JsonProperty("components")
    @Valid
    @NotNull
    @Size()
    private List<WorkflowComponent> components = new ArrayList<WorkflowComponent>();

    @JsonProperty("connections")
    @Valid
    @NotNull
    @Size()
    private List<WorkflowConnection> connections = new ArrayList<WorkflowConnection>();

    @JsonProperty("metadata")
    @Valid
    private Object metadata = null;

    public String getId() {
        return id;
    }

    public WorkflowDetails setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public WorkflowDetails setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public WorkflowDetails setDescription(String description) {
        this.description = description;
        return this;
    }

    public List<WorkflowComponent> getComponents() {
        return components;
    }

    public WorkflowDetails setComponents(List<WorkflowComponent> components) {
        this.components = components;
        return this;
    }

    public List<WorkflowConnection> getConnections() {
        return connections;
    }

    public WorkflowDetails setConnections(List<WorkflowConnection> connections) {
        this.connections = connections;
        return this;
    }

    public Object getMetadata() {
        return metadata;
    }

    public WorkflowDetails setMetadata(Object metadata) {
        this.metadata = metadata;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class WorkflowDetails {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    components: ").append(toIndentedString(components)).append("\n");
        sb.append("    connections: ").append(toIndentedString(connections)).append("\n");
        sb.append("    metadata: ").append(toIndentedString(metadata)).append("\n");
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
        WorkflowDetails workflowDetails = (WorkflowDetails) o;
        return Objects.equals(this.id, workflowDetails.id) && Objects.equals(this.name, workflowDetails.name)
            && Objects.equals(this.description, workflowDetails.description) && Objects.equals(this.components,
            workflowDetails.components) && Objects.equals(this.connections, workflowDetails.connections)
            && Objects.equals(this.metadata, workflowDetails.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, components, connections, metadata);
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
