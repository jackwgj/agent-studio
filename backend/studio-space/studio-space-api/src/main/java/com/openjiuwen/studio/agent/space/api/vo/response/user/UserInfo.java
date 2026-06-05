package com.openjiuwen.studio.agent.space.api.vo.response.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {
    private String userId;

    private String domainId;

    private String userName;

    private String domainName;

    private String roles;

    private String email;
}
