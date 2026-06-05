package com.openjiuwen.studio.agent.space.app.util.simple;

import com.openjiuwen.studio.agent.space.app.constant.SimpleConstants;
import com.openjiuwen.studio.agent.space.app.service.simple.PocConfig;
import com.openjiuwen.studio.agent.space.app.util.SpringBeanUtils;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class SimpleAuthUtils {
    /**
     * 用户信息转成token
     *
     * @param httpRequest
     * @return
     */
    public static String paramToToken(HttpServletRequest httpRequest, String existToken) {
        // 获取userId忽略大小写
        Map<String, String[]> params = httpRequest.getParameterMap();
        String userId = "";
        String projectId = "";
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, String[]> entry : params.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(SimpleConstants.UserInfo.USER_FIELD)) {
                    if (entry.getValue() != null && entry.getValue().length > 0) {
                        userId = entry.getValue()[0];
                    }
                }
                if (entry.getKey().equalsIgnoreCase(SimpleConstants.UserInfo.PROJECT_FIELD)) {
                    if (entry.getValue() != null && entry.getValue().length > 0) {
                        projectId = entry.getValue()[0];
                    }
                }
            }
        }
        if (StringUtils.isEmpty(userId)) {
            userId = httpRequest.getHeader(SimpleConstants.UserInfo.USER_FIELD);
        }
        if (StringUtils.isEmpty(projectId)) {
            projectId = httpRequest.getHeader(SimpleConstants.UserInfo.PROJECT_FIELD);
        }

        String token = "";
        PocConfig pocConfig = SpringBeanUtils.getBean(PocConfig.class);
        // userId和projectId都为空则认为没有传输登陆信息,使用默认登录信息
        if (StringUtils.isEmpty(userId) && StringUtils.isEmpty(projectId)) {
            // 如果没有历史token才用默认值
            if (StringUtils.isEmpty(existToken)) {
                userId = pocConfig.getDefaultUserId();
                projectId = pocConfig.getDefaultProject();
            } else {
                // 否则返回空
                return token;
            }
        }
        return getToken(userId, projectId);
    }

    /**
     * mock token构造
     *
     * @param userId    String
     * @param projectId String
     * @return token
     */
    public static String getToken(String userId, String projectId) {
        PocConfig pocConfig = SpringBeanUtils.getBean(PocConfig.class);
        return String.format("%s|%s", StringUtils.isEmpty(userId) ? "" : userId,
            StringUtils.isEmpty(projectId) ? pocConfig.getDefaultProject() : projectId);
    }
}
