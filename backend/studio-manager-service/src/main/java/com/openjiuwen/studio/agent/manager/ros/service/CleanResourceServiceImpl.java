/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.ros.service;

import com.alibaba.fastjson.JSONObject;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.dto.NotifyReq;
import com.openjiuwen.studio.agent.manager.dto.NotifyResp;
import com.openjiuwen.studio.agent.manager.ros.ResourceCleanMaster;
import com.openjiuwen.studio.agent.manager.ros.RosConstants;
import com.openjiuwen.studio.agent.manager.service.IRosCleanResourceService;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CleanResourceServiceImpl implements IRosCleanResourceService {

    @Autowired
    private ResourceCleanMaster resourceCleanMaster;

    @Value("${tenant.resourceclean.tenant}")
    private String cleanDomainName;

    @Autowired
    private RedisClient redisClient;

    // 单线程，等待队列5，超过丢弃，等管理面重新通知
    private ExecutorService executor = new ThreadPoolExecutor(1, 1,  // 核心0，最大100
        0L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(5));

    @Override
    public NotifyResp cleanNotify(String moduleName, NotifyReq body) {
        log.info("receive tenant dataclean task, module:{}, domainId:{}, taskId:{}", moduleName, body.getDomainId(),
            body.getTaskId());
        // 如果是承载租户才允许调用
        String actualDomain = RequestContextUtils.getRequestUserDomainName();
        if (StringUtils.isEmpty(cleanDomainName) || !cleanDomainName.equals(actualDomain)) {
            log.error("Tenant data clean forbidden, cause exceptDomain:{}, actualDomain:{}", cleanDomainName,
                actualDomain);
            throw new AgentStudioException(StudioError.TENANT_DATA_CLEAN_FORBIDDEN, cleanDomainName, actualDomain);
        }

        // 先查看有没有清理结果，有结果直接返回
        String result = redisClient.get(String.format(RosConstants.DOMAIN_CLEAN_KEY, body.getDomainId()));
        if (StringUtils.isNotEmpty(result)) {
            try {
                log.info("Tenant:{} data clean result:{}.", body.getDomainId(), result);
                return JSONObject.parseObject(result, NotifyResp.class);
            } catch (Exception exception) {
                log.error("parse clean result failed:{}.", result, exception);
            }
        }

        NotifyResp resp = new NotifyResp();
        resp.setModuleName(moduleName);
        resp.setResCode(RosConstants.SUCCESS);
        resp.setResMsg(CommonConstant.SUCCESS);
        resp.setResult(CommonConstant.SUCCESS);
        resp.setStatus(RosConstants.STATUS_DOING);

        // 异步执行
        try {
            executor.submit(() -> resourceCleanMaster.clean(body));
        } catch (RejectedExecutionException e) {
            // 请求数量超过处理能力
            log.error("request exceed processing capacity.", e);
            resp.setResCode(RosConstants.FAILED);
            resp.setResMsg(CommonConstant.FAILED);
            resp.setResult("request exceed processing capacity.");
            resp.setStatus(RosConstants.STATUS_FAILED);
        }
        return resp;
    }
}
