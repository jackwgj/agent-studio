package com.openjiuwen.studio.agent.space.common.model;

import lombok.Data;

@Data
public class Credential {

    private String access;

    private String expires_at;

    private String secret;

    private String securitytoken;
}
