package com.openjiuwen.studio.agent.space.app.service.session;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Slf4j
@Component
public class AccessUrlService {
    @Value("${agent.space.login.loginUrl}")
    private String loginUrl;

    @Value("${agent.space.login.logoutUrl}")
    private String logoutUrl;
}
