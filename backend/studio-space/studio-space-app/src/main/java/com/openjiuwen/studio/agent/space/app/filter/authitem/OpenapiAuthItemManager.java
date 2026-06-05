/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.filter.authitem;

import com.openjiuwen.studio.agent.space.app.filter.authitem.impl.ApiGWAuthItem;
import com.openjiuwen.studio.agent.space.app.filter.authitem.impl.IamTokenAuthItem;
import com.openjiuwen.studio.agent.space.app.filter.authitem.impl.OpenApiAccessKeyAuthItem;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 管理各authitem
 */
@Slf4j
@Service
public class OpenapiAuthItemManager {
    @Value("agent.apace.openapi.trust.urls: ")
    private String trustUrls;

    @Value("tenant.freeze.filter.white_list: ")
    private String freezeFilterWhiteList;

    /**
     * 为保证执行顺序，不能自动注入
     */
    private List<AuthItem> authItemList;

    @Autowired
    private ApiGWAuthItem apiGWAuthItem;

    @Autowired
    private OpenApiAccessKeyAuthItem openApiAccessKeyAuthItem;

    @Autowired
    private IamTokenAuthItem iamtokenAuthItem;

    @PostConstruct
    public void init() {
        authItemList = Arrays.asList(apiGWAuthItem, openApiAccessKeyAuthItem, iamtokenAuthItem);
    }

    public boolean auth(HttpServletRequest request, HttpServletResponse response, String urlPath) {
        if (CollectionUtils.isEmpty(authItemList)) {
            return true;
        }
        // 1.遍历查找符合认证的item
        for (AuthItem item : authItemList) {
            if (!item.isMatch(request, urlPath)) {
                continue;
            }
            // 2.查找到鉴权item，进行鉴权
            if (!item.auth(request, response)) {
                return false;
            }
        }
        return true;
    }

    public boolean isTrustUrl(String urlPath) {
        String[] trustUrls = this.getTrustUrls();
        for (String api : trustUrls) {
            if (urlPath.equals(api)) {
                return true;
            }
        }
        return false;
    }

    private String[] getTrustUrls() {
        return Optional.ofNullable(trustUrls).map(s -> s.split(",")).orElse(new String[] {});
    }
}
