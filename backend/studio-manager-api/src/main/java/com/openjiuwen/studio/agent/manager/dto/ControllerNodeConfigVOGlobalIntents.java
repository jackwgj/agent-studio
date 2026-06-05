/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * ControllerNodeConfigVOGlobalIntents
 */

@Validated

public class ControllerNodeConfigVOGlobalIntents implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private String id = null;

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("description")
    private String description = null;

    @JsonProperty("handler_type")
    private String handlerType = null;

    @JsonProperty("handler")
    @Valid
    private Object handler = null;

    @JsonProperty("termination")
    private Boolean termination = null;

    @JsonProperty("action")
    private String action = null;

    public String getId() {
        return id;
    }

    public ControllerNodeConfigVOGlobalIntents setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public ControllerNodeConfigVOGlobalIntents setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ControllerNodeConfigVOGlobalIntents setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getHandlerType() {
        return handlerType;
    }

    public ControllerNodeConfigVOGlobalIntents setHandlerType(String handlerType) {
        this.handlerType = handlerType;
        return this;
    }

    public Object getHandler() {
        return handler;
    }

    public ControllerNodeConfigVOGlobalIntents setHandler(Object handler) {
        this.handler = handler;
        return this;
    }

    public ControllerNodeConfigVOGlobalIntents setTermination(Boolean termination) {
        this.termination = termination;
        return this;
    }

    public Boolean isTermination() {
        return termination;
    }

    public String getAction() {
        return action;
    }

    public ControllerNodeConfigVOGlobalIntents setAction(String action) {
        this.action = action;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ControllerNodeConfigVOGlobalIntents {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    handlerType: ").append(toIndentedString(handlerType)).append("\n");
        sb.append("    handler: ").append(toIndentedString(handler)).append("\n");
        sb.append("    termination: ").append(toIndentedString(termination)).append("\n");
        sb.append("    action: ").append(toIndentedString(action)).append("\n");
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
        ControllerNodeConfigVOGlobalIntents controllerNodeConfigVOGlobalIntents
            = (ControllerNodeConfigVOGlobalIntents) o;
        return Objects.equals(this.id, controllerNodeConfigVOGlobalIntents.id) && Objects.equals(this.name,
            controllerNodeConfigVOGlobalIntents.name) && Objects.equals(this.description,
            controllerNodeConfigVOGlobalIntents.description) && Objects.equals(this.handlerType,
            controllerNodeConfigVOGlobalIntents.handlerType) && Objects.equals(this.handler,
            controllerNodeConfigVOGlobalIntents.handler) && Objects.equals(this.termination,
            controllerNodeConfigVOGlobalIntents.termination) && Objects.equals(this.action,
            controllerNodeConfigVOGlobalIntents.action);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, handlerType, handler, termination, action);
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
