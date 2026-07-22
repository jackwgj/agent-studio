package com.openjiuwen.studio.agent.space.app.filter.authitem.impl;

import com.alibaba.fastjson2.JSON;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.openjiuwen.studio.agent.space.api.auth.IamTokenAuth;
import com.openjiuwen.studio.agent.space.app.filter.authitem.AuthItem;
import com.openjiuwen.studio.agent.space.app.util.BuildResponseUtil;
import com.openjiuwen.studio.agent.space.common.constant.ContextConstants;
import com.openjiuwen.studio.agent.space.common.constant.HeaderConstant;
import com.openjiuwen.studio.agent.space.common.context.ContextUtils;
import com.openjiuwen.studio.agent.space.common.utils.StringUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Date;
import java.util.List;

/**
 * 企业数字人平台自建 IdP 的 JWT 认证项。
 *
 * <p>复用与 {@link IamTokenAuthItem} 相同的 {@link IamTokenAuth} 注解匹配点，
 * 校验平台签发的 HS256 JWT，并向 {@code ContextUtils.getInvocationContext()} 注入
 * userId / userName / domainId / roles / email，业务层零改动。
 *
 * <p>灰度开关：{@code auth.provider=jwt} 时本项真正生效；否则 {@link #auth} 直接放行，
 * 由 {@link IamTokenAuthItem} 处理（华为 IAM 分支）。同一 {@code @IamTokenAuth} 方法在两种模式下仅一个认证项生效。
 */
@Slf4j
@Component
public class JwtTokenAuthItem implements AuthItem {

    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Value("${auth.provider:iam}")
    private String authProvider;

    @Value("${auth.jwt.secret:}")
    private String jwtSecret;

    @Value("${auth.jwt.issuer:enterprise-digital-human}")
    private String jwtIssuer;

    @Override
    public boolean isMatch(HttpServletRequest request, String urlPath) {
        try {
            HandlerExecutionChain handler = requestMappingHandlerMapping.getHandler(request);
            if (handler != null && handler.getHandler() instanceof HandlerMethod handlerMethod) {
                return handlerMethod.getMethod().isAnnotationPresent(IamTokenAuth.class);
            }
            return false;
        } catch (Exception e) {
            log.error("JwtTokenAuthItem::isMatch failed!", e);
            return false;
        }
    }

    @Override
    public boolean auth(HttpServletRequest request, HttpServletResponse response) {
        // 灰度：仅当 auth.provider=jwt 时本项生效，否则放行交给 IamTokenAuthItem
        if (!"jwt".equalsIgnoreCase(authProvider)) {
            return true;
        }
        if (StringUtil.isEmptyString(jwtSecret)) {
            log.error("JwtTokenAuthItem auth failed: auth.jwt.secret not configured.");
            BuildResponseUtil.createOpenApiErrResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "jwt secret not configured.");
            return false;
        }

        String token = extractToken(request);
        if (StringUtil.isEmptyString(token)) {
            BuildResponseUtil.createOpenApiErrResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "token not found.");
            return false;
        }

        try {
            SignedJWT signed = SignedJWT.parse(token);
            MACVerifier verifier = new MACVerifier(jwtSecret);
            if (!signed.verify(verifier)) {
                BuildResponseUtil.createOpenApiErrResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "token signature invalid.");
                return false;
            }

            JWTClaimsSet claims = signed.getJWTClaimsSet();
            if (!jwtIssuer.equals(claims.getIssuer())) {
                BuildResponseUtil.createOpenApiErrResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "token issuer invalid.");
                return false;
            }
            Date exp = claims.getExpirationTime();
            if (exp != null && exp.before(new Date())) {
                BuildResponseUtil.createOpenApiErrResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "token expired.");
                return false;
            }

            Object userId = claims.getClaim("user_id");
            Object username = claims.getClaim("username");
            Object email = claims.getClaim("email");
            Object tenantId = claims.getClaim("tenant_id");
            Object roles = claims.getClaim("roles");

            ContextUtils.getInvocationContext()
                    .addContext(ContextConstants.AGENT_SPACE_USER_ID, userId == null ? "" : userId.toString())
                    .addContext(ContextConstants.AGENT_SPACE_USER_NAME, username == null ? "" : username.toString())
                    .addContext(ContextConstants.AGENT_SPACE_DOMAIN_ID, tenantId == null ? "" : tenantId.toString())
                    .addContext(ContextConstants.AGENT_SPACE_IAM_TOKEN, token);

            if (email != null) {
                ContextUtils.getInvocationContext().addContext(ContextConstants.AGENT_SPACE_USER_EMAIL, email.toString());
            }
            if (roles != null) {
                ContextUtils.getInvocationContext().addContext(
                        ContextConstants.AGENT_SPACE_USER_ROLES,
                        roles instanceof List ? JSON.toJSONString(roles) : roles.toString()
                );
            }
            return true;
        } catch (Exception e) {
            log.error("JwtTokenAuthItem auth failed.", e);
            BuildResponseUtil.createOpenApiErrResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "token verify failed.");
            return false;
        }
    }

    private String extractToken(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7).trim();
        }
        return request.getHeader(HeaderConstant.HEADER_X_AUTH_TOKEN);
    }
}
