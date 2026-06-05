/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.filter;

import com.openjiuwen.studio.agent.common.constant.Constants;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.common.utils.CryptoUtils;
import com.openjiuwen.studio.agent.common.utils.I18nUtil;
import com.openjiuwen.studio.agent.runtime.service.ObsService;
import com.openjiuwen.studio.agent.runtime.utils.JsonUtils;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Lite 版本 api key 校验
 *
 */
@Component
@Order(0)
@Slf4j
@ConditionalOnProperty(name = "env_type", havingValue = "simple")
public class OpenApiValidatorFilter extends OncePerRequestFilter implements InitializingBean {

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    private final RedisClient redisClient;

    private final I18nUtil i18nUtil;

    private final ObsService obsService;

    private final List<Api> openApiList = new ArrayList<>();

    @Value("${lite.api-code.enabled:false}")
    private boolean isApiKeyCheckEnabled;

    private volatile String licenseKey = "";

    private static final Set<String> WHITE_LIST =
        new HashSet<>(List.of(
            "/health"));

    @Override
    public void afterPropertiesSet() {
        openApiList.add(Api.builder().url("/v1/*/agents/*/conversations/*").method("POST").build());
        openApiList.add(Api.builder().url("/v1/*/agents/*/conversations").method("POST").build());
        openApiList.add(Api.builder().url("/v1/*/workflows/*/conversations/*").method("POST").build());
        openApiList.add(Api.builder().url("/v1/*/workflows/*/conversations").method("POST").build());
        openApiList.add(Api.builder().url("/v1/*/agent-runtime/upload-file").method("POST").build());
        openApiList.add(Api.builder().url("/v1/*/knowledge-bases/*/retrieve").method("POST").build());
        openApiList.add(Api.builder().url("/v2/*/knowledge-bases/retrieve").method("POST").build());
        openApiList.add(Api.builder().url("/v2/*/knowledge-bases/images/*").method("GET").build());
        openApiList.add(Api.builder().url("/v2/*/knowledge-bases/*/files/*").method("GET").build());
    }

    public OpenApiValidatorFilter(RedisClient redisClient, I18nUtil i18nUtil, ObsService obsService) {
        this.redisClient = redisClient;
        this.i18nUtil = i18nUtil;
        this.obsService = obsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {
        if (!isOpenApi(request.getRequestURI(), request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }
        if (isApiKeyChecked(request, response)) {
            chain.doFilter(request, response);
        }
    }

    /**
     * 校验 api key 是否有效
     */
    private boolean isApiKeyChecked(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!isApiKeyCheckEnabled) {
            return true;
        }
        String authHeader = request.getHeader(Constants.Header.AUTHORIZATION);
        if (authHeader == null) {
            log.error("missing api key for reques:{}", request.getRequestURI());
            sendUnauthorized(response, true);
            return false;
        }
        if (!authHeader.startsWith(Constants.Header.AUTHORIZATION_PREFIX)) {
            log.error("api key format error, key: {}", authHeader);
            sendUnauthorized(response, false);
            return false;
        }

        String credentials = authHeader.substring(Constants.Header.AUTHORIZATION_PREFIX.length()).trim();
        String apikeyObject = redisClient.get(credentials);
        if (StringUtils.isBlank(apikeyObject)) {
            log.error("api key not find in redis");
            sendUnauthorized(response, false);
            return false;
        }
        Map<String, Object> map = JsonUtils.json2Obj(apikeyObject, Constants.MAP_TYPE_REFERENCE);
        if (map == null) {
            sendUnauthorized(response, false);
            return false;
        }
        String userId = (String) map.get("userId");
        String requestUserId = RequestContextUtils.getRequestUserId();
        if (!Objects.equals(userId, requestUserId)) {
            log.error("the api key not match current user");
            sendUnauthorized(response, false);
            return false;
        }
        return true;
    }

    /**
     * 校验 License 是否有效
     */
    private boolean isLicenseChecked(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        String key = getLicenseKey();
        if ("GET".equals(method) || isInWhiteList(uri)) {
            return true;
        }

        if (StringUtils.isBlank(key)) {
            loadLicenseKey();
            key = getLicenseKey();
        }
        String licenseStateCode = redisClient.get(key);
        if (licenseStateCode == null) {
            log.error("Fail get license from cache. key:{}", key);
            sendLicenseError(response, StudioError.LICENSE_MISSING);
            return false;
        }
        int code = Integer.parseInt(licenseStateCode);
        // 0:有效, 1:宽限, -1:试用
        if (code != 0 && code != 1 && code != -1) {
            log.error("[isLicenseChecked]: license is invalid: {}", licenseStateCode);
            sendLicenseError(response, StudioError.LICENSE_INVALID);
            return false;
        }
        return true;
    }

    private boolean isInWhiteList(String requestUri) {
        for (String pattern : WHITE_LIST) {
            if (antPathMatcher.match(pattern, requestUri)) {
                return true;
            }
        }
        return false;
    }

    private boolean isOpenApi(String requestUri, String method) {
        // 遍历白名单中的每个模式
        for (Api api : openApiList) {
            // 如果请求URI与当前模式匹配，则返回true
            if (antPathMatcher.match(api.getUrl(), requestUri) && method.equalsIgnoreCase(api.getMethod())) {
                return true;
            }
        }
        // 如果没有匹配的模式，返回false
        return false;
    }

    private void sendUnauthorized(HttpServletResponse response, boolean isMissing) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        StudioError error = isMissing ? StudioError.POC_API_KEY_MISSING : StudioError.POC_API_KEY_INCORRECT;
        sendErrorResponse(response, error);
    }

    private void sendLicenseError(HttpServletResponse response, StudioError error) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        sendErrorResponse(response, error);
    }

    private void sendErrorResponse(HttpServletResponse response, StudioError error) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        String code = "openjiuwen." + error.getModule().getSubCode() + error.getCode();
        String message = i18nUtil.getMessage(code);
        String quota = "\"";
        response.getWriter()
            .write("{\"error_code\":" + quota + code + quota + ",\"error_msg\":" + quota + message + quota + "}");
        response.getWriter().flush();
    }

    private void loadLicenseKey() {
        try {
            String esn = obsService.getObject(Constants.ESN_OBS_PATH);
            if (esn != null) {
                licenseKey = CryptoUtils.decrypt(esn);
            } else {
                log.error("[isLicenseChecked]: load license key error");
            }
            log.info("filter load: {}", licenseKey);
        } catch (AgentStudioException exception) {
            log.error("[isLicenseChecked]: failed to load license key from obs");
        }
    }

    private String getLicenseKey() {
        return licenseKey;
    }
}
