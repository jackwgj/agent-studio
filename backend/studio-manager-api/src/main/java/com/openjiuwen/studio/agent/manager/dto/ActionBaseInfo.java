/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * ActionBaseInfo
 */

@Validated

public class ActionBaseInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    @Length(max = 64)
    private String id = null;

    @JsonProperty("name")
    @Length(max = 128)
    private String name = null;

    @JsonProperty("description")
    @Length(max = 512)
    private String description = null;

    @JsonProperty("connector_id")
    @Length(max = 64)
    private String connectorId = null;

    @JsonProperty("connector_version")
    private String connectorVersion = null;

    @JsonProperty("created_time")
    private Date createdTime = null;

    @JsonProperty("updated_time")
    private Date updatedTime = null;

    @JsonProperty("test_result")
    private String testResult = null;

    @JsonProperty("category")
    private String category = null;

    @JsonProperty("swagger")
    @Valid
    private Object swagger = null;

    @JsonProperty("definition")
    @Valid
    private Object definition = null;

    @JsonProperty("connector_created_type")
    private String connectorCreatedType = null;

    @JsonProperty("connector_action_html")
    private String connectorActionHtml = null;

    @JsonProperty("operation_id")
    private String operationId = null;

    public String getId() {
        return id;
    }

    public ActionBaseInfo setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public ActionBaseInfo setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ActionBaseInfo setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getConnectorId() {
        return connectorId;
    }

    public ActionBaseInfo setConnectorId(String connectorId) {
        this.connectorId = connectorId;
        return this;
    }

    public String getConnectorVersion() {
        return connectorVersion;
    }

    public ActionBaseInfo setConnectorVersion(String connectorVersion) {
        this.connectorVersion = connectorVersion;
        return this;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public ActionBaseInfo setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
        return this;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public ActionBaseInfo setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
        return this;
    }

    public String getTestResult() {
        return testResult;
    }

    public ActionBaseInfo setTestResult(String testResult) {
        this.testResult = testResult;
        return this;
    }

    public String getCategory() {
        return category;
    }

    public ActionBaseInfo setCategory(String category) {
        this.category = category;
        return this;
    }

    public Object getSwagger() {
        return swagger;
    }

    public ActionBaseInfo setSwagger(Object swagger) {
        this.swagger = swagger;
        return this;
    }

    public Object getDefinition() {
        return definition;
    }

    public ActionBaseInfo setDefinition(Object definition) {
        this.definition = definition;
        return this;
    }

    public String getConnectorCreatedType() {
        return connectorCreatedType;
    }

    public ActionBaseInfo setConnectorCreatedType(String connectorCreatedType) {
        this.connectorCreatedType = connectorCreatedType;
        return this;
    }

    public String getConnectorActionHtml() {
        return connectorActionHtml;
    }

    public ActionBaseInfo setConnectorActionHtml(String connectorActionHtml) {
        this.connectorActionHtml = connectorActionHtml;
        return this;
    }

    public String getOperationId() {
        return operationId;
    }

    public ActionBaseInfo setOperationId(String operationId) {
        this.operationId = operationId;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ActionBaseInfo {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    connectorId: ").append(toIndentedString(connectorId)).append("\n");
        sb.append("    connectorVersion: ").append(toIndentedString(connectorVersion)).append("\n");
        sb.append("    createdTime: ").append(toIndentedString(createdTime)).append("\n");
        sb.append("    updatedTime: ").append(toIndentedString(updatedTime)).append("\n");
        sb.append("    testResult: ").append(toIndentedString(testResult)).append("\n");
        sb.append("    category: ").append(toIndentedString(category)).append("\n");
        sb.append("    swagger: ").append(toIndentedString(swagger)).append("\n");
        sb.append("    definition: ").append(toIndentedString(definition)).append("\n");
        sb.append("    connectorCreatedType: ").append(toIndentedString(connectorCreatedType)).append("\n");
        sb.append("    connectorActionHtml: ").append(toIndentedString(connectorActionHtml)).append("\n");
        sb.append("    operationId: ").append(toIndentedString(operationId)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ActionBaseInfo actionBaseInfo = (ActionBaseInfo) o;
        return Objects.equals(this.id, actionBaseInfo.id) && Objects.equals(this.name, actionBaseInfo.name)
            && Objects.equals(this.description, actionBaseInfo.description) && Objects.equals(this.connectorId,
            actionBaseInfo.connectorId) && Objects.equals(this.connectorVersion, actionBaseInfo.connectorVersion)
            && Objects.equals(this.createdTime, actionBaseInfo.createdTime) && Objects.equals(this.updatedTime,
            actionBaseInfo.updatedTime) && Objects.equals(this.testResult, actionBaseInfo.testResult) && Objects.equals(
            this.category, actionBaseInfo.category) && Objects.equals(this.swagger, actionBaseInfo.swagger)
            && Objects.equals(this.definition, actionBaseInfo.definition) && Objects.equals(this.connectorCreatedType,
            actionBaseInfo.connectorCreatedType) && Objects.equals(this.connectorActionHtml,
            actionBaseInfo.connectorActionHtml) && Objects.equals(this.operationId, actionBaseInfo.operationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, connectorId, connectorVersion, createdTime, updatedTime, testResult,
            category, swagger, definition, connectorCreatedType, connectorActionHtml, operationId);
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
