/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.workflow.convert.adapt.difyadapter;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import inet.ipaddr.IPAddressString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CheckUrlService {
    @Value("${workflow.enable-url-check:}")
    private Boolean enableUrlCheck;

    /**
     * host 合法性检查
     *
     * @param swaggerUrl    调用url
     * @param hostBlackList 连接器host黑名单
     */
    public void checkSwaggerUrlValid(String swaggerUrl, String hostBlackList) {
        log.info("Start: check url {} from black list.", swaggerUrl);
        if (hostBlackList.isEmpty()) {
            return;
        }
        Set<String> connectorHostBlackList = Arrays.stream(hostBlackList.split(","))
            .map(String::trim)
            .collect(Collectors.toSet());
        if (!enableUrlCheck) {
            log.info("not check from black list");
            return;
        }
        try {
            URL url = new URL(swaggerUrl);
            String host = url.getHost();
            checkHostValid(host, connectorHostBlackList);
            try {
                InetAddress[] addresses = InetAddress.getAllByName(host);
                for (InetAddress address : addresses) {
                    checkHostValid(address.getHostAddress(), connectorHostBlackList);
                }
            } catch (UnknownHostException e) {
                // 不做控制，运行态DNS一样，有问题会调不通，没有影响
                log.warn("swagger base url is Unknown Host: {}", host);
            }
        } catch (MalformedURLException e) {
            // URL 格式本身不对，报 1029 是对的，或者报 1146 也可以
            throw new AgentStudioException(StudioError.INVALID_URL);
        } catch (Exception e) {
            // 捕获其他异常，防止程序崩溃
            // 只要是黑名单校验不通过，就抛出 1151
            log.error("Error while checking swagger URL: {}", swaggerUrl, e);
            throw new AgentStudioException(StudioError.WORKFLOW_ADDRESS_RESTRICTED);
        }
        log.info("End: check url from black list.");
    }


    /**
     * host 合法性检查
     *
     * @param host host
     * @param connectorHostBlackList 连接器host黑名单
     */
    private static void checkHostValid(String host, Set<String> connectorHostBlackList) {
        // 判断host是否在黑名单中
        if (connectorHostBlackList.contains(host)) {
            log.warn("checkHostValid failed, host: {}", host);
            throw new AgentStudioException(StudioError.INVALID_URL);
        }

        // 判断网段
        try {
            IPAddressString hostAddress = new IPAddressString(host);
            for (String ips : connectorHostBlackList) {
                if (org.apache.commons.lang3.StringUtils.isEmpty(ips)) {
                    continue;
                }
                try {
                    IPAddressString ipAddress = new IPAddressString(ips);
                    if (ipAddress.contains(hostAddress) || host.contains(ips)) {
                        log.warn("checkHostValid failed, ipAddress:{}, hostAddress: {}, host: {}, ips:{}", ipAddress,
                            hostAddress, host, ips);
                        throw new AgentStudioException(StudioError.INVALID_URL);
                    }
                } catch (IllegalArgumentException e) {
                    // 忽略非法的IP格式
                    log.warn("Invalid IP format in blacklist: {}", ips);
                }
            }
        } catch (IllegalArgumentException e) {
            // host 不是合法的IP地址，可能是域名，不进行网段判断
            log.warn("Host is not a valid IP address, skip subnet check: {}", host);
        }
    }
}
