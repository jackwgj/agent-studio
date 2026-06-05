package com.openjiuwen.studio.agent.space.app.model.iam;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.util.List;

@Data
public class Token {
    private List<String> methods;

    @JsonProperty("expires_at")
    private String expiresAt;

    private String signature;

    @JsonProperty("issued_at")
    private String issuedAt;

    @JsonProperty("user")
    private User user;

    @JsonProperty("scope")
    private Scope scope;

    @JsonProperty("roles")
    private List<Role> roles;

    @JsonProperty("catalog")
    private Object catalog;

    private Project project;
}
