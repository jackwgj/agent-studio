package com.openjiuwen.studio.agent.space.common.context;

import com.openjiuwen.studio.agent.space.common.constant.CommonConstant;
import com.openjiuwen.studio.agent.space.common.utils.StringUtil;

import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户上下文基本信息
 */
@Data
@ToString()
@Slf4j
public class ContextBean {
    private String userId;

    private String sessionId;

    /**
     * 账号名称
     */
    private String userName;

    /**
     * 用户名（昵称）
     */
    private String name;

    private String role;

    private String mobile;

    private String email;

    private String tenantId;

    private String tenantName;

    private String tenantType;

    /**
     * 前台选择语言
     */
    private String locale;

    private String loginIp;

    private String url;

    /**
     * 用户归属部门编码
     */
    private String deptCode;

    private long threadId;

    public String getLocale() {
        return StringUtil.isNotEmptyString(this.locale) ? this.locale : CommonConstant.LOCALE_CN;
    }
}
