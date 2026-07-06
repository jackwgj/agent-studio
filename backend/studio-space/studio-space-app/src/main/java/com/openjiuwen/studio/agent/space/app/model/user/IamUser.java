package com.openjiuwen.studio.agent.space.app.model.user;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Builder
@Accessors(chain = true)
public class IamUser implements Serializable {

    // 静态代码检查G.SER.02：实现Serializable的类应显式声明serialVersionUID，避免类变更后反序列化兼容性问题
    private static final long serialVersionUID = 1L;

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
