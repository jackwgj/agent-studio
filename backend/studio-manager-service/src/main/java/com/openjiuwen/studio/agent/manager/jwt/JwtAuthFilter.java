/*
 * 企业数字人智能平台 · SSO 集成
 * 校验本平台 IdP 签发的 HS256 JWT（Authorization: Bearer），
 * 通过后写入 studio-manager 的 RequestContextUtils(SimpleUser) 上下文。
 * 与 SAML/华为 IAM 互斥，由 auth.provider=jwt 开关启用（见 JwtAuthConfig）。
 */

package com.openjiuwen.studio.agent.manager.jwt;

import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.openjiuwen.studio.agent.common.dto.simple.SimpleUser;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * 平台 IdP 融合过滤器：校验本平台签发的 HS256 JWT，写入 SimpleUser 上下文供业务读取。
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private final String jwtSecret;

    private final String jwtIssuer;

    private final boolean enforce;

    private final String fallbackUserId;

    private final String fallbackProjectId;

    private final String fallbackDomainId;

    private static final List<String> EXCLUDED = List.of(
            "/health", "/actuator", "/error", "/saml", "/login", "/logout",
            "/modelartsstudio-agent", "/v3/api-docs", "/swagger", "/static"
    );

    public JwtAuthFilter(String jwtSecret, String jwtIssuer, boolean enforce,
                         String fallbackUserId, String fallbackProjectId, String fallbackDomainId) {
        this.jwtSecret = jwtSecret;
        this.jwtIssuer = jwtIssuer;
        this.enforce = enforce;
        this.fallbackUserId = fallbackUserId;
        this.fallbackProjectId = fallbackProjectId;
        this.fallbackDomainId = fallbackDomainId;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        for (String e : EXCLUDED) {
            if (uri.equals(e) || uri.startsWith(e + "/") || uri.startsWith(e)) {
                return true;
            }
        }
        if (uri.endsWith(".js") || uri.endsWith(".css") || uri.endsWith(".html")
                || uri.endsWith(".ico") || uri.endsWith(".png") || uri.endsWith(".svg")
                || uri.endsWith(".json") || uri.endsWith(".woff2") || uri.endsWith(".map")) {
            return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            String auth = request.getHeader("Authorization");
            if (StringUtils.hasText(auth) && auth.startsWith("Bearer ")) {
                String token = auth.substring(7).trim();
                SimpleUser user = verify(token);
                if (user != null) {
                    RequestContextUtils.setContext(user);
                    chain.doFilter(request, response);
                    return;
                }
                if (enforce) {
                    sendUnauthorized(response, "Invalid JWT");
                    return;
                }
            } else {
                if (enforce) {
                    sendUnauthorized(response, "Missing Authorization Bearer");
                    return;
                }
            }
            // 兜底：内部/服务间调用或网关未注入 token 时，使用默认上下文，避免击穿现有调用链
            SimpleUser anon = new SimpleUser();
            anon.setUserId(fallbackUserId);
            anon.setToken("");
            anon.setProjectId(fallbackProjectId);
            anon.setDomainId(fallbackDomainId);
            anon.setDomainName(fallbackDomainId);
            RequestContextUtils.setContext(anon);
            chain.doFilter(request, response);
        } finally {
            RequestContextUtils.remove();
        }
    }

    private SimpleUser verify(String token) {
        if (!StringUtils.hasText(jwtSecret)) {
            return null;
        }
        try {
            SignedJWT signed = SignedJWT.parse(token);
            JWSHeader header = signed.getHeader();
            if (!"HS256".equals(header.getAlgorithm() != null ? header.getAlgorithm().getName() : "")) {
                return null;
            }
            JWSVerifier verifier = new MACVerifier(jwtSecret.getBytes(StandardCharsets.UTF_8));
            if (!signed.verify(verifier)) {
                return null;
            }
            JWTClaimsSet claims = signed.getJWTClaimsSet();
            Date exp = claims.getExpirationTime();
            if (exp != null && exp.before(new Date())) {
                return null;
            }
            if (StringUtils.hasText(jwtIssuer) && !jwtIssuer.equals(claims.getIssuer())) {
                return null;
            }
            SimpleUser user = new SimpleUser();
            user.setUserId(claims.getSubject());
            String name = claims.getStringClaim("name");
            if (!StringUtils.hasText(name)) {
                name = claims.getStringClaim("username");
            }
            user.setUserName(name);
            user.setToken(token);
            Object domainId = claims.getClaim("domainId");
            user.setDomainId(domainId != null ? domainId.toString() : fallbackDomainId);
            Object projectId = claims.getClaim("projectId");
            user.setProjectId(projectId != null ? projectId.toString() : fallbackProjectId);
            Object domainName = claims.getClaim("domainName");
            user.setDomainName(domainName != null ? domainName.toString() : fallbackDomainId);
            return user;
        } catch (Exception e) {
            return null;
        }
    }

    private void sendUnauthorized(HttpServletResponse response, String msg) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":4010,\"message\":\"" + msg + "\"}");
    }
}
