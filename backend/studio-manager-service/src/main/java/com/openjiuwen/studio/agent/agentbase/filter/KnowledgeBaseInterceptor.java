/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.filter;

import com.openjiuwen.studio.agent.agentbase.service.KnowledgeBaseRuleServiceImpl;
import com.openjiuwen.studio.agent.agentbase.service.KnowledgeBaseServiceImpl;
import com.openjiuwen.studio.agent.common.enums.KnowledgeSourceEnum;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;
import com.openjiuwen.studio.agent.foundation.base.exception.ErrorCode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 校验空间权限
 */
@Component
public class KnowledgeBaseInterceptor implements HandlerInterceptor {

    @Value("${knowledge.source}")
    private String knowledgeSource;

    @Autowired
    private KnowledgeBaseServiceImpl knowledgeBaseService;

    @Autowired
    private KnowledgeBaseRuleServiceImpl knowledgeBaseRuleService;

    // AntPathMatcher实例，用于路径匹配
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final Set<String> WHITE_LIST = new HashSet<>(
        Arrays.asList("/v1/agent-builder/**", "/private/v1/agent-base/data-clean-task"));

    private final static Logger logger = LoggerFactory.getLogger(KnowledgeBaseInterceptor.class);

    /**
     * 处理请求前的验证逻辑，包括验证项目ID和工作区ID的格式、存在性等。
     *
     * @param request HttpServletRequest对象，包含请求信息
     * @param response HttpServletResponse对象，用于发送错误响应
     * @param handler 处理请求的对象
     * @return 如果验证通过，返回true；如果验证失败，返回false
     * @throws Exception 可能抛出的异常
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws Exception {
        if (KnowledgeSourceEnum.CUSTOM.toString().equalsIgnoreCase(knowledgeSource)) {
            return true;
        }
        // 白名单校验
        String requestUri = request.getRequestURI();
        if (isInWhiteList(requestUri)) {
            return true;
        }

        @SuppressWarnings("unchecked") Map<String, String> pathVariables = (Map<String, String>) request.getAttribute(
            HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        String knowledgeBaseId = pathVariables.get("knowledge_base_id");
        if (StringUtils.isNotEmpty(knowledgeBaseId) && !knowledgeBaseService.isExistedKnowledgeBase(knowledgeBaseId,
            RequestContextUtils.getRequestUserDomainId())) {
            logger.error("The knowledge base [{}] does not belong to the domainId [{}]. ", knowledgeBaseId,
                RequestContextUtils.getRequestUserDomainId());
            throw new AgentBaseException(ErrorCode.KNOWLEDGE_BASE_NOT_EXIST, knowledgeBaseId);
        }
        String id = pathVariables.get("id");
        // 校验分段规则id属于该空间
        if (StringUtils.isNotEmpty(id) && !knowledgeBaseRuleService.isExistedSegmentRule(id,
            RequestContextUtils.getRequestUserDomainId())) {
            logger.error("The segmentation rule [{}] does not belong to the domainId [{}].", id,
                RequestContextUtils.getRequestUserDomainId());
            throw new AgentBaseException(ErrorCode.KNOWLEDGE_SEGMENT_RULE_NOT_EXIST, id);
        }

        return true;
    }

    /**
     * 检查给定的请求URI是否在白名单中。
     *
     * @param requestUri 要检查的请求URI
     * @return 如果请求URI匹配白名单中的某个模式，则返回true，否则返回false
     */
    private boolean isInWhiteList(String requestUri) {
        // 遍历白名单中的每个模式
        for (String pattern : WHITE_LIST) {
            // 如果请求URI与当前模式匹配，则返回true
            if (pathMatcher.match(pattern, requestUri)) {
                return true;
            }
        }
        // 如果没有匹配的模式，返回false
        return false;
    }
}
