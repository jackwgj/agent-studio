/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.config;

import com.openjiuwen.studio.prompt.engineering.task.ClusterTaskExecutor;
import com.openjiuwen.studio.prompt.engineering.task.SingleNodeJobManager;
import com.openjiuwen.studio.prompt.engineering.task.domain.JobHandlerRegistry;
import com.openjiuwen.studio.prompt.engineering.task.handler.IJobHandler;
import com.openjiuwen.studio.prompt.engineering.task.repository.JobInstanceRepository;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 功能描述
 *
 */
@Configuration
@Slf4j
@EnableAsync
public class JobBeanConfiguration {
    private static InetAddress getLocalHostExactAddress() throws UnknownHostException, SocketException {
        InetAddress candidateAddress = null;
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            for (Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses(); inetAddresses.hasMoreElements(); ) {
                InetAddress inetAddr = inetAddresses.nextElement();
                if (!inetAddr.isLoopbackAddress()) {
                    if (inetAddr.isSiteLocalAddress()) {
                        return inetAddr;
                    }
                    if (Objects.isNull(candidateAddress)) {
                        candidateAddress = inetAddr;
                    }
                }
            }
        }
        return candidateAddress == null ? InetAddress.getLocalHost() : candidateAddress;
    }

    @Bean
    public JobHandlerRegistry jobHandlerRegistry(List<IJobHandler> jobHandlers,
                                                 @Value("${server.port}") int localServerPort, @Value("${spring.application.name}") String applicationName,
                                                 @Value("${server.address:}") String serverAddr) {
        Map<String, IJobHandler> lookup = new HashMap<>();
        jobHandlers.forEach(jh -> lookup.put(jh.supportJobSpecName(), jh));

        if (StringUtils.isEmpty(serverAddr)) {
            serverAddr = getServerHost();
            log.warn("No server.address config!!!, use Java api get localhost: {}", serverAddr);
        }

        String handlerId = applicationName + "@" + serverAddr + ":" + localServerPort;

        return new JobHandlerRegistry(lookup, handlerId);
    }

    private String getServerHost() {
        String addr = "XXX.XXX.XXX.XXX";
        try {
            addr = getLocalHostExactAddress().getHostAddress();
        } catch (UnknownHostException | SocketException e) {
            log.error("Can't get local host", e);
        }
        return addr;
    }

    @Bean
    public ClusterTaskExecutor clusterTaskExecutor(JobHandlerRegistry jobHandlerRegistry) {
        return new ClusterTaskExecutor(jobHandlerRegistry);
    }

    @Bean
    public SingleNodeJobManager singleNodeJobManager(JobInstanceRepository repository,
                                                     JobHandlerRegistry jobHandlerRegistry, ClusterTaskExecutor clusterTaskExecutor) {
        return new SingleNodeJobManager(repository, jobHandlerRegistry, clusterTaskExecutor);
    }
}
