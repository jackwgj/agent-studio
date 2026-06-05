package com.openjiuwen.studio.agent.space.app.util.iam;

import com.alibaba.fastjson.JSON;
import com.openjiuwen.studio.agent.space.app.model.iam.Role;
import com.openjiuwen.studio.agent.space.common.context.AuthorizationContextHolder;
import com.openjiuwen.studio.agent.space.common.utils.StringUtil;

import java.util.List;

public class IamRoleUtil {
    /**
     * 管理员角色名称
     */
    public static final String ROLE_ADMIN = "te_admin";

    /**
     * 判断是否存在指定的iam角色
     * @param roleName
     */
    public static boolean checkRole(String roleName) {
        String rolesStr = AuthorizationContextHolder.roles();
        if (StringUtil.isEmptyString(rolesStr) || StringUtil.isEmptyString(roleName)) {
            return false;
        }
        List<Role> roleList = JSON.parseArray(rolesStr, Role.class);
        return roleList.stream().anyMatch(role -> roleName.equals(role.getName()));
    }

    /**
     * 判断是否有管理员角色权限
     */
    public static boolean checkAdminRole() {
        return checkRole(ROLE_ADMIN);
    }
}
