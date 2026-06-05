/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.license;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.rce.client.IamClient;
import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.common.service.IamAuthService;
import com.openjiuwen.studio.agent.common.utils.DateUtils;
import com.openjiuwen.studio.agent.common.utils.UuidUtils;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.license.models.LcsResourceDto;
import com.openjiuwen.studio.agent.manager.license.models.LcsResourceDtoWrapper;
import com.openjiuwen.studio.agent.manager.license.models.LcsResourceInfo;
import com.openjiuwen.studio.agent.manager.rce.client.ItepClient;
import com.openjiuwen.studio.common.service.entity.LicenseEntity;
import com.openjiuwen.studio.common.service.repository.LicenseRepository;

import lombok.extern.slf4j.Slf4j;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * License控制 定时任务
 *
 */
@Component
@Slf4j
@ConditionalOnProperty(name = "env_type", havingValue = "hcs")
public class LicenseControlSchedule {
    private static final String SYSTEM_MODE_ONE = "CLOUD_SERVICE";

    private static final String SYSTEM_MODE_THREE = "PRODUCT";

    private static final String MODE_ONE_LS_001 = "VAPENTSV001";

    private static final String MODE_ONE_LS_002 = "VAPENTSV002";

    private static final String MODE_THREE_LS_001 = "VAPENT001";

    private static final String MODE_THREE_LS_002 = "VAPENT002";

    public static final String RUNTIME_CPU_NUMS = "RUNTIME_CPU_NUMS";

    private static final int BASE_MULT = 44;

    private static final int INCREMENTAL_MULT = 4;

    private final List<String> modeLicenses =
        new ArrayList<>(Arrays.asList(MODE_ONE_LS_001, MODE_ONE_LS_002, MODE_THREE_LS_001, MODE_THREE_LS_002));

    private final K8sService k8sService;

    private final ItepClient itepClient;

    private final IamClient iamClient;

    @Value("${op.svc.user-id}")
    private String opUserId;

    @Value("${agent.builder.iam.ak}")
    private String ak;

    @Value("${agent.builder.iam.sk}")
    private String sk;

    @Value("${iam.project.name}")
    private String projectName;

    @Autowired
    private LicenseRepository licenseRepository;

    @Autowired
    private RedisClient redisClient;

    @Autowired
    private IamAuthService iamAuthService;

    public LicenseControlSchedule(K8sService k8sService, ItepClient itepClient, IamClient iamClient) {
        this.k8sService = k8sService;
        this.itepClient = itepClient;
        this.iamClient = iamClient;
    }

    @Scheduled(cron = "0 0/15 * * * ?")
    @Async
    public void syncLicenseTask() {
        log.info("license control schedule sync itep license start");
        List<LcsResourceInfo> resources = getLcsResourceInfos();

        // vCPU的个数
        long bbomValue = 0L;
        // itep基础包使用量
        int itepBasicUsedNum = 0;
        // itep增量包使用量
        int itepIncrementalUsedNum = 0;
        String currentSystemMode = SYSTEM_MODE_THREE;
        for (LcsResourceInfo resource : resources) {
            String itemName = resource.getItemName();
            Integer usedNum = resource.getItemUsedNum();
            // 只处理在 modeLicenses 中的项（如果有其他 itemName 会被忽略）
            if (!modeLicenses.contains(itemName)) {
                continue;
            }
            if (MODE_ONE_LS_001.equals(itemName) || MODE_THREE_LS_001.equals(itemName)) {
                bbomValue += usedNum.longValue() * BASE_MULT;
                itepBasicUsedNum = resource.getItemUsedNum();
                updateLicenseTotalNum(resource, itemName);
                currentSystemMode = MODE_ONE_LS_001.equals(itemName) ? SYSTEM_MODE_ONE : SYSTEM_MODE_THREE;
            }
            if (MODE_ONE_LS_002.equals(itemName) || MODE_THREE_LS_002.equals(itemName)) {
                bbomValue += usedNum.longValue() * INCREMENTAL_MULT;
                itepIncrementalUsedNum = resource.getItemUsedNum();
                updateLicenseTotalNum(resource, itemName);
                currentSystemMode = MODE_ONE_LS_002.equals(itemName) ? SYSTEM_MODE_ONE : SYSTEM_MODE_THREE;
            }
        }

        log.info("license control schedule sync itep license currentSystemMode: {}, bbomValue: {}, itepBasicUsedNum: {}, itepIncrementalUsedNum: {}",
            currentSystemMode, bbomValue, itepBasicUsedNum, itepIncrementalUsedNum);
        if (bbomValue <= 0 || itepBasicUsedNum < 0 || itepIncrementalUsedNum < 0) {
            log.error("license control schedule sync itep license get value failed");
            return;
        }

        Double cpus = getCpus(bbomValue);
        if (cpus == null || cpus <= 0) {
            log.error("license control schedule checkLicense: get cpu failed");
            return;
        }

        postItepService(bbomValue, cpus, itepBasicUsedNum, currentSystemMode, itepIncrementalUsedNum);
    }

    private void postItepService(long bbomValue, Double cpus, int itepBasicUsedNum, String currentSystemMode,
        int itepIncrementalUsedNum) {
        LcsResourceDto lcsResourceDto = new LcsResourceDto();
        String itemName = Strings.EMPTY;

        // itep用量小于k8s总数，调itep增加资源
        int resourceRequestNum;
        if (bbomValue < cpus) {
            if (itepBasicUsedNum == 0) {
                // 如果没有基础包, 增加一个基础的
                itemName = currentSystemMode.equals(SYSTEM_MODE_ONE) ? MODE_ONE_LS_001 : MODE_THREE_LS_001;
                resourceRequestNum = 1;
            } else {
                // 如果有基础包, 计算增加的个数
                log.info("license control schedule The itep vCPU usage < the actual total usage");
                itemName = currentSystemMode.equals(SYSTEM_MODE_ONE) ? MODE_ONE_LS_002 : MODE_THREE_LS_002;
                // 计算使用量, 向上取整, 保证资源足够
                resourceRequestNum = (int) Math.ceil((cpus - bbomValue) / INCREMENTAL_MULT);
                // 待办 需求暂停: 如果使用量>申请量*120%，ITEP会拒绝请求，按120%上报给ITEP。向下取整
                // 需求暂停:if (resourceRequestNum > itepIncrementalUsedNum * 1.2) {
                // 需求暂停:    resourceRequestNum = (int) Math.floor(itepIncrementalUsedNum * 1.2);
                // 需求暂停:}
            }
            log.info("license control schedule addResource {} usedNum {} to itep.", itemName, resourceRequestNum);
            lcsResourceDto.setResourceName(itemName);
            lcsResourceDto.setResourceRequestNum(resourceRequestNum);
            try {
                itepClient.addResource(getTokenByPassword(), CommonConstant.APPLICATION_JSON, lcsResourceDto);
                log.info("license control schedule addResource {} usedNum {} to itep success.", itemName, resourceRequestNum);
            } catch (Exception e) {
                log.error("license control schedule addResource from itep error, error: {}", e.getMessage());
            }

        } else if (bbomValue > cpus) {
            // itep用量大于基础版容量, 且大于k8s总数，调itep减少资源
            log.info("license control schedule The itep vCPU usage > the actual usage");
            // 若itep基础版个数大于1，优先减少基础的
            if (itepBasicUsedNum > 1) {
                log.info("license control schedule itep item_used_num > 1");
                itemName = currentSystemMode.equals(SYSTEM_MODE_ONE) ? MODE_ONE_LS_001 : MODE_THREE_LS_001;
                // 计算使用量, 向下取整, 保证资源不会多删, 防止 反复增减增量包的情况
                int decreaseNum = (int) Math.floor((bbomValue - cpus) / BASE_MULT);
                if (decreaseNum == 0) {
                    return;
                }
                // 防止计算异常, 最多删除不能超过总数-1
                resourceRequestNum = Math.min(decreaseNum, itepBasicUsedNum - 1);
            } else {
                // 若itep基础版个数小于等于1，减少增量的
                itemName = currentSystemMode.equals(SYSTEM_MODE_ONE) ? MODE_ONE_LS_002 : MODE_THREE_LS_002;
                // 计算使用量, 向下取整, 保证资源不会多删, 防止 反复增减增量包的情况
                int decreaseNum = (int) Math.floor((bbomValue - cpus) / INCREMENTAL_MULT);
                if (decreaseNum == 0) {
                    return;
                }
                // 防止计算异常, 最多删除不能超过总数
                resourceRequestNum = Math.min(decreaseNum, itepIncrementalUsedNum);
            }
            log.info("license control schedule deleteResource {} usedNum {} to itep.", itemName, resourceRequestNum);
            lcsResourceDto.setResourceName(itemName);
            lcsResourceDto.setResourceRequestNum(resourceRequestNum);
            try {
                itepClient.deleteResource(getTokenByPassword(), CommonConstant.APPLICATION_JSON, lcsResourceDto);
                log.info("license control schedule deleteResource {} usedNum {} to itep success.", itemName, resourceRequestNum);
            } catch (Exception e) {
                log.error("license control schedule deleteResource from itep error, {}", e.getMessage());
            }
        }
    }

    @Nullable
    private Double getCpus(long bbomValue) {
        double cpus;
        try {
            cpus = k8sService.getAgentResourceCpu();
        } catch (AgentStudioException e) {
            log.error("license control schedule checkLicense: get agent resource cpu failed", e);
            return null;
        }
        log.info("license control schedule manager cluster cpus is: {}", cpus);

        // 获取runtime所在集群的cpu
        if (!redisClient.exists(RUNTIME_CPU_NUMS)) {
            log.error("license control schedule get runtimeCluster vCPU error");
            return null;
        }
        cpus += Double.parseDouble(redisClient.get(RUNTIME_CPU_NUMS));
        log.info("license control schedule The itep vCPU bbomValue is: {}, total cce cpus is: {}", bbomValue, cpus);
        return cpus;
    }

    private List<LcsResourceInfo> getLcsResourceInfos() {
        ResponseEntity<LcsResourceDtoWrapper> responseEntity;
        try {
            String tokenByPassword = getTokenByPassword();
            responseEntity = itepClient.getResourceLicneseInfo(tokenByPassword, CommonConstant.APPLICATION_JSON);
        } catch (Exception e) {
            log.error("license control schedule failed to get license resources from itep, message: {}", e.getMessage());
            throw new AgentStudioException(StudioError.LICENSE_GET_RESOURCE_ERROR);
        }

        List<LcsResourceInfo> resources;
        if (null != responseEntity.getBody().getResources()) {
            resources = responseEntity.getBody().getResources();
            log.info("license control schedule The result of license resources from itep success");
        } else {
            log.error("license control schedule the result of license resources from itep is empty");
            throw new AgentStudioException(StudioError.LICENSE_GET_RESOURCE_ERROR);
        }
        return resources;
    }

    public void updateLicenseTotalNum(LcsResourceInfo lcsResourceInfo, String modeLicense) {
        List<LicenseEntity> licenseEntities = licenseRepository.selectByResourceId(modeLicense, opUserId);
        if (CollectionUtils.isEmpty(licenseEntities)) {
            LicenseEntity licenseEntity = new LicenseEntity();
            licenseEntity.setId(UuidUtils.getUUID());
            licenseEntity.setDomainId(opUserId);
            licenseEntity.setCurrentValue(String.valueOf(lcsResourceInfo.getItemUsedNum()));
            licenseEntity.setMaxValue(String.valueOf(lcsResourceInfo.getItemTotalNum()));
            licenseEntity.setResourceId(modeLicense);
            licenseEntity.setCreatedDate(DateUtils.getNow());
            licenseEntity.setLastUpdatedDate(DateUtils.getNow());
            licenseEntity.setCreatedByUserId(opUserId);
            licenseRepository.insert(licenseEntity);
        } else {
            LicenseEntity licenseEntity = licenseEntities.get(0);
            licenseEntity.setMaxValue(String.valueOf(lcsResourceInfo.getItemTotalNum()));
            licenseEntity.setLastUpdatedDate(DateUtils.getNow());
            licenseRepository.updateById(licenseEntity);
        }
        log.info("license control schedule update license total num success");
    }

    public String getTokenByPassword() {
        return iamAuthService.getProjectTokenByAkSk(ak, sk, projectName, null);
    }
}
