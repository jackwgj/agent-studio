package com.openjiuwen.studio.agent.space.app.model.user;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Builder
@Accessors(chain = true)
public class IamUser implements Serializable {
    private String userId;

    private String userName;

    private String name;

    private String mobile;

    private String email;

    private String tenant;

    private String role;

    private String tenantName;

    private String deptCode;

    private String deptName;

    private String tenantType;

    private String domainId;
}
