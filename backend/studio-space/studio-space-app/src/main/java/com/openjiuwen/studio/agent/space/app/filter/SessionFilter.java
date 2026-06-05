/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.filter;

import com.openjiuwen.studio.agent.space.app.model.session.SessionInfoDetail;
import com.openjiuwen.studio.agent.space.app.model.web.CookieInfo;
import com.openjiuwen.studio.agent.space.app.model.web.UrlPattern;
import com.openjiuwen.studio.agent.space.app.model.web.WebContextInfo;
import com.openjiuwen.studio.agent.space.app.service.session.SessionsManageService;
import com.openjiuwen.studio.agent.space.app.util.BuildResponseUtil;
import com.openjiuwen.studio.agent.space.app.util.CookieUtil;
import com.openjiuwen.studio.agent.space.common.constant.ContextConstants;
import com.openjiuwen.studio.agent.space.common.constant.HeaderConstant;
import com.openjiuwen.studio.agent.space.common.context.AddAuthorizationContextHolder;
import com.openjiuwen.studio.agent.space.common.context.ContextUtils;
import com.openjiuwen.studio.agent.space.common.context.LoggerContext;
import com.openjiuwen.studio.agent.space.common.exception.AgentSpaceErrorCodes;
import com.openjiuwen.studio.agent.space.common.exception.AgentSpaceException;
import com.openjiuwen.studio.agent.space.common.utils.CollectionUtil;
import com.openjiuwen.studio.agent.space.common.utils.SpringContextUtils;
import com.openjiuwen.studio.agent.space.common.utils.StringUtil;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Session校验过滤器
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
@WebFilter(filterName = "sessionFilter", urlPatterns = {"/agentspace/health/check"}, asyncSupported = true)
public class SessionFilter implements Filter, EnvironmentAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionFilter.class);

    private static final String METHOD_ALL = "all";

    /**
     * 不同URL直接的分隔符
     */
    private static final String URL_SPLIT_CHAR = ",";

    /**
     * URL中Method与Path的分隔符
     */
    private static final String PATH_SPLIT_CHAR = ":";

    /**
     * PATH中模糊匹配符号
     */
    private static final String MATCH_SPLIT_CHAR = "*";

    /**
     * 是否已完成初始化
     */
    private boolean isInited = false;

    private static final ThreadLocal<String> threadLocalLanguage = ThreadLocal.withInitial(() -> null);

    /**
     * 需拦截校验的URL
     */
    private final Map<String, List<UrlPattern>> matchedUrlMap = new HashMap<>();

    /**
     * 需拦截校验URL中的例外URL
     */
    private final Map<String, List<UrlPattern>> excludedUrlMap = new HashMap<>();

    /**
     * CSRFToken校验中的例外URL
     */
    private final Map<String, List<UrlPattern>> csrfTokenExcludedUrlMap = new HashMap<>();

    private Environment environment;

    private SessionsManageService getSessionServiceManager() {
        return SpringContextUtils.getBean(SessionsManageService.class);
    }

    /**
     * 读取配置进行初始化
     */
    private void init() {
        if (isInited) {
            return;
        }

        synchronized (SessionFilter.class) {
            if (isInited) {
                return;
            }

            String matchedUrlPattern = environment.getProperty("agent.space.sessionFilter.matchedUrlPattern");
            String urlPatternStrings = StringUtil.trimWhitespace(matchedUrlPattern);
            if (StringUtil.isEmptyString(urlPatternStrings) || urlPatternStrings.equals(URL_SPLIT_CHAR)) {
                // appstage.platform.login.sessionFilter.matchedUrlPattern参数为空
                throw new AgentSpaceException(AgentSpaceErrorCodes.CONFIG_VALUE_IS_EMPTY,
                    "agent.space.sessionFilter.matchedUrlPattern");
            }
            initUrlMap(matchedUrlMap, urlPatternStrings);
            if (CollectionUtil.isEmpty(matchedUrlMap)) {
                throw new AgentSpaceException(AgentSpaceErrorCodes.CONFIG_VALUE_IS_EMPTY,
                    "agent.space.sessionFilter.matchedUrlPattern");
            }

            String excludeUrlPattern = environment.getProperty("agent.space.sessionFilter.excludeUrlPattern");
            String excludeUrlPatternStrings = StringUtil.trimWhitespace(excludeUrlPattern);
            if (StringUtil.isNotEmptyString(excludeUrlPatternStrings)) {
                initUrlMap(excludedUrlMap, excludeUrlPatternStrings);
            }

            if (environment.getProperty("agent.space.sessionFilter.csrfToken.enabled", Boolean.class, true)) {
                String csrfExcludeUrlPattern = environment.getProperty(
                    "agent.space.sessionFilter.csrfToken.excludeUrlPattern");
                String csrfExcludeUrlPatternStrings = StringUtil.trimWhitespace(csrfExcludeUrlPattern);
                if (StringUtil.isNotEmptyString(csrfExcludeUrlPatternStrings)) {
                    initUrlMap(csrfTokenExcludedUrlMap, csrfExcludeUrlPatternStrings);
                }
            }

            isInited = true;
        }
    }

    /**
     * 解析urlPatternStrings进行urlMap的初始化
     *
     * @param urlMmap           urlMap
     * @param urlPatternStrings url匹配参数
     */
    private void initUrlMap(Map<String, List<UrlPattern>> urlMmap, String urlPatternStrings) {
        for (String urlPatternString : StringUtil.split(urlPatternStrings, URL_SPLIT_CHAR)) {
            String[] urlArray = split(urlPatternString, PATH_SPLIT_CHAR);
            if (urlArray.length == 0) {
                return;
            }

            String method = METHOD_ALL;
            String path = StringUtil.trimWhitespace(urlArray[0]);
            if (urlArray.length > 1) {
                method = StringUtil.trimWhitespace(urlArray[0].toLowerCase(Locale.ENGLISH));
                path = StringUtil.trimWhitespace(urlArray[1]);
            }
            checkPath(path);

            List<UrlPattern> urlPatternList = urlMmap.computeIfAbsent(method, k -> new ArrayList<>());

            UrlPattern urlPattern = new UrlPattern();
            if (!path.contains(MATCH_SPLIT_CHAR)) {
                //配置的path中无*
                urlPattern.setFullMatchModel(true);
                urlPattern.setFullPath(path);
            } else {
                String[] pathArray = split(path, MATCH_SPLIT_CHAR);
                if (pathArray.length > 0) {
                    //length>0，说明path不是*，否则不做处理
                    if (StringUtils.startsWithIgnoreCase(path, MATCH_SPLIT_CHAR)) {
                        //以*开头场景
                        urlPattern.setSuffix(StringUtil.trimWhitespace(pathArray[0]));
                    } else if (StringUtils.endsWithIgnoreCase(path, MATCH_SPLIT_CHAR)) {
                        //以*结尾场景
                        urlPattern.setPrefix(StringUtil.trimWhitespace(pathArray[0]));
                    } else {
                        //*在中间位置场景
                        urlPattern.setPrefix(StringUtil.trimWhitespace(pathArray[0]));
                        urlPattern.setSuffix(StringUtil.trimWhitespace(pathArray[1]));
                    }
                }
            }

            urlPatternList.add(urlPattern);
        }
    }

    private void checkPath(String path) {
        int idx = path.indexOf(MATCH_SPLIT_CHAR);
        if (idx != -1) {
            idx = path.indexOf(MATCH_SPLIT_CHAR, idx + 1);
            if (idx != -1) {
                // path中存在多个*，解析失败
                LOGGER.error("SessionFilter init failed.");
                throw new AgentSpaceException(AgentSpaceErrorCodes.CONFIG_VALUE_IS_EMPTY,
                    "agent.space.sessionFilter.matchedUrlPattern");
            }
        }
    }

    private String[] split(String toSplit, String delimiter) {
        String[] a = StringUtil.split(toSplit, delimiter);
        if (a.length > 2) {
            LOGGER.error("SessionFilter init failed.");
            throw new AgentSpaceException(AgentSpaceErrorCodes.CONFIG_VALUE_IS_EMPTY,
                "agent.space.sessionFilter.matchedUrlPattern");
        }
        return a;
    }

    private boolean isMatched(String method, String path) {
        return isMatchMap(method, path, matchedUrlMap);
    }

    private boolean isMatchMap(String method, String path, Map<String, List<UrlPattern>> matchedUrlMap) {
        String lowerMethod = method.toLowerCase(Locale.ENGLISH);
        List<UrlPattern> urlPatterns = matchedUrlMap.get(lowerMethod);
        if (isMatched(urlPatterns, path)) {
            return isNotExcluded(lowerMethod, path);
        }

        urlPatterns = matchedUrlMap.get(METHOD_ALL);
        if (isMatched(urlPatterns, path)) {
            return isNotExcluded(lowerMethod, path);
        }

        return false;
    }

    private boolean isInCSRFTokenExcludedMap(HttpServletRequest requestEx) {
        String method = requestEx.getMethod();
        String path = requestEx.getServletPath();
        return isMatchMap(method, path, csrfTokenExcludedUrlMap);
    }

    private boolean isNotExcluded(String method, String path) {
        List<UrlPattern> urlPatterns = excludedUrlMap.get(method);
        if (isMatched(urlPatterns, path)) {
            return false;
        }

        urlPatterns = excludedUrlMap.get(METHOD_ALL);
        return !isMatched(urlPatterns, path);
    }

    private boolean isMatched(List<UrlPattern> urlPatterns, String path) {
        if (CollectionUtils.isEmpty(urlPatterns)) {
            return false;
        }

        for (UrlPattern urlPattern : urlPatterns) {
            if (urlPattern.isFullMatchModel()) {
                if (path.equalsIgnoreCase(urlPattern.getFullPath())) {
                    return true;
                }
            } else if (StringUtils.startsWithIgnoreCase(path, urlPattern.getPrefix()) && StringUtils.endsWithIgnoreCase(
                path, urlPattern.getSuffix())) {
                return true;
            }
        }

        return false;
    }

    public boolean enabled() {
        return environment.getProperty("agent.space.sessionFilter.enabled", Boolean.class, true);
    }

    private boolean checkCSRFToken(HttpServletRequest request, HttpServletResponse response,
        SessionInfoDetail sessionInfo) {

        if (!environment.getProperty("agent.space.sessionFilter.csrfToken.enabled", Boolean.class, true)) {
            //若关闭csrfToken校验，直接放行
            return true;
        }

        if (isInCSRFTokenExcludedMap(request)) {
            //例外请求，不校验
            return true;
        }

        //校验CSRF-Token
        String csrfToken = request.getHeader(HeaderConstant.HEADER_X_CSRF_TOKEN);
        if (StringUtil.isEmptyString(csrfToken)) {
            csrfToken = request.getParameter(HeaderConstant.HEADER_X_CSRF_TOKEN);
        }
        if (StringUtil.isEmptyString(csrfToken) || !csrfToken.equals(sessionInfo.getCsrfToken())) {
            BuildResponseUtil.createNotLoginResponse(response);
            return false;
        }

        return true;
    }

    private void buildContext(SessionInfoDetail sessionInfo, WebContextInfo webContextInfo) {
        LoggerContext.put(ContextConstants.AGENT_SPACE_USER_ID, sessionInfo.getUserId());

        AddAuthorizationContextHolder.sessionId(webContextInfo.getCookieInfo().getWiseSessionId());
        AddAuthorizationContextHolder.userId(sessionInfo.getUserId());
        AddAuthorizationContextHolder.clientIp(sessionInfo.getSessionAttr(ContextConstants.AGENT_SPACE_CLIENT_IP));
        AddAuthorizationContextHolder.language(webContextInfo.getCookieInfo().getLanguage());

        AddAuthorizationContextHolder.userName(sessionInfo.getSessionAttr(ContextConstants.AGENT_SPACE_USER_NAME));
        AddAuthorizationContextHolder.userRoles(sessionInfo.getSessionAttr(ContextConstants.AGENT_SPACE_USER_ROLES));
        AddAuthorizationContextHolder.userEmail(sessionInfo.getSessionAttr(ContextConstants.AGENT_SPACE_USER_EMAIL));
        AddAuthorizationContextHolder.projectId(sessionInfo.getSessionAttr(ContextConstants.AGENT_SPACE_PROJECT_ID));
        AddAuthorizationContextHolder.iamToken(sessionInfo.getSessionAttr(ContextConstants.AGENT_SPACE_IAM_TOKEN));
        AddAuthorizationContextHolder.domainName(sessionInfo.getSessionAttr(ContextConstants.AGENT_SPACE_DOMAIN_NAME));
        AddAuthorizationContextHolder.ticket(sessionInfo.getTicket());

        AddAuthorizationContextHolder.domainId(sessionInfo.getDomainId());
    }

    private boolean receiveResponse(HttpServletRequest request, HttpServletResponse response) {
        String cookieLanguage = "zh-CN";  // 默认中文
        if (StringUtils.hasText(CookieUtil.getCookieValueByName(request, ContextConstants.AGENT_SPACE_LANGUAGE))) {
            cookieLanguage = CookieUtil.getCookieValueByName(request, ContextConstants.AGENT_SPACE_LANGUAGE);
        }
        threadLocalLanguage.set(cookieLanguage);

        try {
            init();
        } catch (Exception e) {
            LOGGER.error("LoginInterceptor end, LoginInterceptor.init failed.", e);
            BuildResponseUtil.createSystemErrResponse(response);
            return false;
        }

        String method = request.getMethod();
        String path = request.getServletPath();
        if (!isMatched(method, path)) {
            LOGGER.error("LoginInterceptor end, this request({}:{}) is not need check.", method, path);
            return true;
        }

        WebContextInfo webContextInfo;
        try {
            webContextInfo = WebContextInfo.buildWebContextInfo(request);
            webContextInfo.getCookieInfo().setLanguage(cookieLanguage);
        } catch (Exception e) {
            LOGGER.error("SessionFilter end, buildWebContextInfo failed.", e);
            BuildResponseUtil.createSystemErrResponse(response);
            return false;
        }
        CookieInfo cookieInfo = webContextInfo.getCookieInfo();
        // sid为空，即请求中无sessionId，校验失败
        if (StringUtil.isEmptyString(cookieInfo.getWiseSessionId())) {
            LOGGER.error("SessionFilter end, sid is null.");
            BuildResponseUtil.createNotLoginResponse(response);
            return false;
        }

        //查询Session对象
        SessionInfoDetail sessionInfo;

        try {
            sessionInfo = getSessionServiceManager().visitSession(cookieInfo.getWiseSessionId());
        } catch (Exception e) {
            LOGGER.error("SessionFilter end, querySessionInfo failed.", e);
            BuildResponseUtil.createSystemErrResponse(response);
            return false;
        }

        if (sessionInfo == null || sessionInfo.getExpiredTime() == null || sessionInfo.getUserId() == null
            || sessionInfo.getExpiredTime().before(new Date()) || sessionInfo.getMaxExpiredTime().before(new Date())) {
            BuildResponseUtil.createNotLoginResponse(response);
            LOGGER.error("SessionFilter end, session invalid.");
            return false;
        }

        if (!checkCSRFToken(request, response, sessionInfo)) {
            LOGGER.error("SessionFilter end, csrf token invalid.");
            BuildResponseUtil.createNotLoginResponse(response);
            return false;
        }

        //设置上下文
        buildContext(sessionInfo, webContextInfo);

        LOGGER.info("SessionFilter end, check session successfully.");
        return true;
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
        throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        try {
            if (!enabled()) {
                log.info("Skip SessionFilter");
                filterChain.doFilter(request, response);
                return;
            }

            String traceId = request.getHeader(ContextConstants.AGENT_SPACE_TRANCE_ID);
            if (traceId == null) {
                traceId = UUID.randomUUID().toString().replaceAll("-", "");
            }

            LoggerContext.clear();
            LoggerContext.put(ContextConstants.AGENT_SPACE_TRANCE_ID, traceId);
            LOGGER.info("SessionFilter start, request({}:{}).", request.getMethod(), request.getServletPath());
            if (receiveResponse(request, response)) {
                filterChain.doFilter(request, response);
            }
        } finally {
            ContextUtils.removeInvocationContext();
        }
    }

    @Override
    public void destroy() {
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
