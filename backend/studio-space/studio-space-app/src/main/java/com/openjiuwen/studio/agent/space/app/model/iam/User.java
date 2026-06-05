package com.openjiuwen.studio.agent.space.app.model.iam;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class User {
    private String name;

    private String id;

    @JsonProperty("password_expires_at")
    private String passwordExpiresAt;

    private Domain domain;
}
