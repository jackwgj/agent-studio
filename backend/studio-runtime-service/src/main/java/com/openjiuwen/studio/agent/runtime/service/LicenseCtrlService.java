/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service;

import com.google.common.collect.Lists;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.common.utils.JsonUtils;
import com.openjiuwen.studio.agent.runtime.dto.skuinst.LicenseInst;
import com.openjiuwen.studio.agent.runtime.dto.skuinst.LicenseRecord;
import com.openjiuwen.studio.agent.runtime.dto.skuinst.LicenseReport;
import com.openjiuwen.studio.agent.runtime.dto.skuinst.LicenseReportReq;
import com.openjiuwen.studio.agent.runtime.rce.client.AgentManagerClient;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class LicenseCtrlService {
    private static final Object LOCK = new Object();

    private static final int ONE_DAY = 24 * 60 * 60 * 1000;

    private static final int NO_LIMIT_VALUE = -1;

    private static final String FORMAT = "yyyyMMdd";

    private static final String RECORD_KEY = "AGENT_BUILDER_AGENT_RUN_%s_%s";

    private static final String CACHE_KEY = "AGENT_BUILDER_LICENSE_INSTS__%s_%s";

    @Value("${license.ctrl.enable:true}")
    private boolean licenseCtrlEnable;

    @Value("${license.apiInvocation.ctrl.enable:false}")
    private boolean invocationCtrlEnable;

    @Value("${license.apiInvocation.attrCode:agent_api_invocation_count}")
    private String invocationAttrCode;

    @Value("${license.cache.expireTimeS:600}")
    private int cacheExpireTime;

    @Autowired
    private RedisClient redisClient;

    @Autowired
    private AgentManagerClient managerClient;

    private Map<String, List<LicenseRecord>> cacheRecords = new HashMap<>();

    public LicenseCtrlService() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            try {
                reportRecordToManager();
            } catch (Exception e) {
                log.warn("Fail report license record to manager. {}", e.getMessage());
            }
        }, 1, 1, TimeUnit.MINUTES);
    }

    /**
     * 上报到agent-manager
     */
    protected void reportRecordToManager() {
        Map<String, List<LicenseRecord>> records;
        synchronized (LOCK) {
            records = cacheRecords;
            cacheRecords = new HashMap<>();
        }
        if (records.isEmpty()) {
            return;
        }
        log.info("Start to report to manager. num:{}", records.size());
        for (Map.Entry<String, List<LicenseRecord>> entry : records.entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue;
            }
            reportRecordToManager(entry.getKey(), entry.getValue());
        }

        log.info("Success report to manager.");
    }

    private void reportRecordToManager(String domainId, List<LicenseRecord> records) {
        Map<String, LicenseRecord> reportMap = new HashMap<>();
        for (LicenseRecord record : records) {
            String key = record.getResourceId() + "_" + record.getAttrCode();
            if (reportMap.containsKey(key)) {
                reportMap.get(key).incremental(record.getIncrementalValue());
            } else {
                reportMap.put(key, record);
            }
        }
        List<LicenseReport> reports = new ArrayList<>(reportMap.size());
        for (Map.Entry<String, LicenseRecord> entry : reportMap.entrySet()) {
            reports.add(entry.getValue().createLicenseReport());
        }

        LicenseReportReq req = new LicenseReportReq(reports);
        managerClient.report(req, null, domainId);
        for (LicenseRecord record : records) {
            String cacheKey = String.format(Locale.ROOT, CACHE_KEY, record.getDomainId(), record.getAttrCode());
            redisClient.delete(cacheKey);
        }
        log.info("Success report record to manager. domainId:{}", domainId);
    }

    /**
     * 数据上报
     *
     * @param record 上报
     */
    public void agentInvocationReport(LicenseRecord record, String domainId) {
        if (!licenseCtrlEnable) {
            return;
        }
        String key = String.format(Locale.ROOT, RECORD_KEY, domainId,
            new SimpleDateFormat(FORMAT).format(new Date(System.currentTimeMillis())));
        redisClient.getAndIncrement(key, ONE_DAY);
        redisClient.expire(key, Duration.ofSeconds(ONE_DAY));
    }

    /**
     * 检查是否允许调用Agent/工作流
     *
     * @return License
     */
    public LicenseRecord agentInvocationCheck(String domainId) {
        if (!licenseCtrlEnable) {
            return new LicenseRecord();
        }
        LicenseRecord record = getLicenseRecord(domainId, null, invocationAttrCode);

        if (NO_LIMIT_VALUE == record.getMaxValue()) {
            return record;
        }

        if (invocationCtrlEnable) {
            long curValue = getCurrentValue(domainId);
            if (curValue >= record.getMaxValue()) {
                log.error("The number of API calls has exceeded the quota. curValue:{} maxValue:{}", curValue,
                    record.getMaxValue());
                throw new AgentStudioException(StudioError.AGENT_API_INVOKE_EXCEEDED_QUOTA);
            }
        }
        return record;
    }

    private long getCurrentValue(String domainId) {
        String key = String.format(Locale.ROOT, RECORD_KEY, domainId,
            new SimpleDateFormat(FORMAT).format(new Date(System.currentTimeMillis())));

        try {
            return redisClient.addAndGet(key, 0, ONE_DAY);
        } catch (Exception e) {
            log.warn("Get agent invoke value fail. key:{}  msg:{}", key, e.getMessage());
            return 0L;
        }
    }

    /**
     * 获取License控制项
     *
     * @param domainId domainId
     * @param token 用户Token
     * @param attrCode 控制箱
     * @return 当前记录
     */
    public LicenseRecord getLicenseRecord(String domainId, String token, String attrCode) {
        String cacheKey = String.format(Locale.ROOT, CACHE_KEY, domainId, attrCode);
        String cacheStr = redisClient.get(cacheKey);
        if (StringUtils.isEmpty(cacheStr)) {
            return getLicenseRecordSync(domainId, token, attrCode, cacheKey);
        }

        return JsonUtils.decode(cacheStr, LicenseRecord.class);
    }

    /**
     * 上报 license用量
     *
     * @param domainId domainId
     * @param attrCode 属性
     * @param maxValue 最大值
     * @param value 使用值
     */
    public void licenseRecordReport(String domainId, String attrCode, int maxValue, int value) {
        if (NO_LIMIT_VALUE == maxValue) {
            return;
        }
        CompletableFuture.runAsync(() -> {
            try {
                LicenseRecord newRecord = getLicenseRecord(domainId, null, attrCode);
                newRecord.setCurValue(newRecord.getCurValue() + value);
                newRecord.setIncrementalValue(value);

                String cacheKey = String.format(Locale.ROOT, CACHE_KEY, domainId, attrCode);
                redisClient.set(cacheKey, JsonUtils.encode(newRecord), Duration.ofSeconds(cacheExpireTime));

                synchronized (LOCK) {
                    cacheRecords.computeIfAbsent(domainId, k -> Lists.newArrayList()).add(newRecord);
                }
            } catch (Exception e) {
                log.error("License record report error. domainId:{} attrCode:{}", domainId, attrCode, e);
            }
        });
    }

    private LicenseRecord getLicenseRecordSync(String domainId, String token, String attrCode, String cacheKey) {
        ResponseEntity<List<LicenseInst>> responseEntity = managerClient.queryLicense(null, null, attrCode, token,
            domainId);
        LicenseRecord rd = null;
        List<LicenseInst> body = responseEntity.getBody();
        if (body != null && !body.isEmpty()) {
            for (LicenseInst licenseInst : body) {
                LicenseRecord record = new LicenseRecord(domainId, licenseInst);
                rd = record;
                if (record.isAvailable()) {
                    break;
                }
            }
        }

        if (rd == null) {
            log.warn("Fail query license instance. domainId:{} attrCode:{}", domainId, attrCode);
            rd = new LicenseRecord().setDomainId(domainId).setMaxValue(0).setCurValue(0);
        }
        redisClient.set(cacheKey, JsonUtils.encode(rd), Duration.ofSeconds(cacheExpireTime));
        return rd;
    }
}
