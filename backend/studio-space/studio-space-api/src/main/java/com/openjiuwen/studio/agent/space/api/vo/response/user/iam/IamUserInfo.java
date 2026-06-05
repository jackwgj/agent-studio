package com.openjiuwen.studio.agent.space.api.vo.response.user.iam;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class IamUserInfo {
    private Boolean enabled;

    private String id;

    @JsonProperty(value = "domain_id")
    private String domainId;

    private String name;

    private IamUserResourceLinks links;

    @JsonProperty(value = "xuser_id")
    private String xUserId;

    @JsonProperty(value = "xuser_type")
    private String xUserType;

    private String areacode;

    private String email;

    private String phone;

    @JsonProperty(value = "pwd_status")
    private Boolean pwdStatus;

    @JsonProperty(value = "pwd_create_time")
    private String pwdCreateTime;

    @JsonProperty(value = "modify_pwd_time")
    private String modifyPwdTime;

    @JsonProperty(value = "update_time")
    private String updateTime;

    @JsonProperty(value = "create_time")
    private String createTime;

    @JsonProperty(value = "last_login_time")
    private String lastLoginTime;

    @JsonProperty(value = "last_pwd_auth_time")
    private String lastPwdAuthTime;

    @JsonProperty(value = "pwd_strength")
    private String pwdStrength;

    @JsonProperty(value = "is_domain_owner")
    private Boolean isDomainOwner;

    @JsonProperty(value = "access_mode")
    private String accessMode;

    @JsonProperty(value = "description")
    private String description;
}
