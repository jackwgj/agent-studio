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
 * ControllerConfigIRGlobalIntents
 */

@Validated

public class ControllerConfigIRGlobalIntents implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("intent_id")
    private String intentId = null;

    @JsonProperty("description")
    private String description = null;

    @JsonProperty("handler_type")
    private String handlerType = null;

    @JsonProperty("handler")
    @Valid
    private Object handler = null;

    @JsonProperty("action_after_completion")
    private String actionAfterCompletion = null;

    public String getName() {
        return name;
    }

    public ControllerConfigIRGlobalIntents setName(String name) {
        this.name = name;
        return this;
    }

    public String getIntentId() {
        return intentId;
    }

    public ControllerConfigIRGlobalIntents setIntentId(String intentId) {
        this.intentId = intentId;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ControllerConfigIRGlobalIntents setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getHandlerType() {
        return handlerType;
    }

    public ControllerConfigIRGlobalIntents setHandlerType(String handlerType) {
        this.handlerType = handlerType;
        return this;
    }

    public Object getHandler() {
        return handler;
    }

    public ControllerConfigIRGlobalIntents setHandler(Object handler) {
        this.handler = handler;
        return this;
    }

    public String getActionAfterCompletion() {
        return actionAfterCompletion;
    }

    public ControllerConfigIRGlobalIntents setActionAfterCompletion(String actionAfterCompletion) {
        this.actionAfterCompletion = actionAfterCompletion;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ControllerConfigIRGlobalIntents {\n");

        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    intentId: ").append(toIndentedString(intentId)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    handlerType: ").append(toIndentedString(handlerType)).append("\n");
        sb.append("    handler: ").append(toIndentedString(handler)).append("\n");
        sb.append("    actionAfterCompletion: ").append(toIndentedString(actionAfterCompletion)).append("\n");
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
        ControllerConfigIRGlobalIntents controllerConfigIRGlobalIntents = (ControllerConfigIRGlobalIntents) o;
        return Objects.equals(this.name, controllerConfigIRGlobalIntents.name) && Objects.equals(this.intentId,
            controllerConfigIRGlobalIntents.intentId) && Objects.equals(this.description,
            controllerConfigIRGlobalIntents.description) && Objects.equals(this.handlerType,
            controllerConfigIRGlobalIntents.handlerType) && Objects.equals(this.handler,
            controllerConfigIRGlobalIntents.handler) && Objects.equals(this.actionAfterCompletion,
            controllerConfigIRGlobalIntents.actionAfterCompletion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, intentId, description, handlerType, handler, actionAfterCompletion);
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
