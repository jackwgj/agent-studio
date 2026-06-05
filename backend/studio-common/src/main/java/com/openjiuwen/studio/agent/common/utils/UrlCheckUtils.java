/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.utils;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 功能描述
 *
 */
@Component
@Slf4j
public class UrlCheckUtils {
    @Value("${tool.url.enable-check}")
    private boolean enableUrlCheck;

    @Value("${tool.url.black-list}")
    private String toolUrlBlackListStr;

    @Value("${tool.url.white-list}")
    private String toolUrlWhiteListStr;

    private List<String> toolUrlBlackList;

    private List<String> toolUrlWhiteList;

    @PostConstruct
    private void init() {
        toolUrlBlackList = StringUtils.isBlank(toolUrlBlackListStr) ? new ArrayList<>()
                : Arrays.asList(toolUrlBlackListStr.split(","));
        toolUrlWhiteList = StringUtils.isBlank(toolUrlWhiteListStr) ? new ArrayList<>()
                : Arrays.asList(toolUrlWhiteListStr.split(","));
    }

    public void checkUrl(String projectId, String url) {
        log.info("start to checkUrl {}", url);
        // 未开启检查（默认开启），返回
        if (!enableUrlCheck || StringUtils.isEmpty(url)) {
            log.info("not check url");
            return;
        }
        if (url.contains("{")) {
            // 含有占位符，放通处理
            return;
        }

        URI parsedUri;
        try {
            parsedUri = new URI(url);
        } catch (URISyntaxException e) {
            log.error("Fail to parse url [{}]", url, e);
            throw new AgentStudioException(StudioError.INVALID_URL);
        }
        String host = parsedUri.getHost();

        // 1. 检查黑名单
        if (toolUrlBlackList.contains(host)) {
            log.error("URL is blocked by blacklist [{}]", url);
            throw new AgentStudioException(StudioError.INVALID_URL);
        }

        // 2. 检查白名单（白名单优先级高于其他规则）
        if (toolUrlWhiteList.contains(host)) {
            log.info("URL is in whitelist [{}]", url);
            return;
        }

        // 3. 解析关联的IP地址
        InetAddress[] addresses;
        try {
            addresses = InetAddress.getAllByName(host);
        } catch (UnknownHostException e) {
            // 如果无法解析域名，放通处理
            log.warn("can not get host [{}], url check interrupt", url);
            return;
        }
        for (InetAddress addr : addresses) {
            String ip = addr.getHostAddress();

            // 4. 检查IP是否在黑名单
            if (toolUrlBlackList.contains(ip)) {
                log.error("IP is blocked by blacklist [{}]", ip);
                throw new AgentStudioException(StudioError.URL_BLOCKED_BLOCK_LIST);
            }

            // 5. 检查是否为内网IP
            if (isInternalIp(addr, projectId)) {
                log.error("Access to internal network is forbidden [{}]", ip);
                throw new AgentStudioException(StudioError.ACCESS_INTER_NETWORK_FORBIDDEN);
            }
        }
    }

    /**
     * 检查给定的URL是否在白名单中
     *
     * @param url 需要检查的URL
     * @throws AgentStudioException 如果URL不在白名单中，或者URL格式错误，或者无法解析主机名
     */
    public void checkUrlInWhiteList(String url) {
        log.info("Starting URL check for: {}", url);

        if (!enableUrlCheck || StringUtils.isEmpty(url)) {
            log.info("URL check is disabled or URL is empty");
            return;
        }

        try {
            URI parsedUri = new URI(url);
            String host = parsedUri.getHost();

            // 判断白名单中是否存在该URL
            if (toolUrlWhiteList.contains(host)) {
                log.info("URL is in whitelist: {}", url);
                return;
            }

            // 解析URL关联的IP地址，判断IP地址是否在白名单中
            InetAddress[] addresses = InetAddress.getAllByName(host);
            for (InetAddress addr : addresses) {
                String ip = addr.getHostAddress();
                if (toolUrlWhiteList.contains(ip)) {
                    log.info("IP is in whitelist: {}", url);
                    return;
                }
            }
            throw new AgentStudioException(StudioError.URL_WHITELIST_CHECK_ERROR);
        } catch (URISyntaxException e) {
            log.error("Invalid URL: {}", url, e);
            throw new AgentStudioException(StudioError.INVALID_URL);
        } catch (UnknownHostException e) {
            log.warn("can not get host [{}], url check interrupt", url);
            throw new AgentStudioException(StudioError.URL_WHITELIST_CHECK_ERROR);
        }
    }


    /**
     * 判断IP是否为内网地址
     */
    public boolean isInternalIp(InetAddress addr, String projectId) {
        // 本地回环地址
        if (addr.isAnyLocalAddress() || addr.isLoopbackAddress()) {
            return true;
        }
        // 内网地址
        return addr.isLinkLocalAddress() || addr.isSiteLocalAddress();
    }
}
