/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.common.service.service;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.CopyUtils;
import com.openjiuwen.studio.agent.common.utils.DateUtils;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.common.utils.UuidUtils;
import com.openjiuwen.studio.agent.common.utils.ValidateUtils;
import com.openjiuwen.studio.common.service.constant.CommonConstant;
import com.openjiuwen.studio.common.service.entity.LicenseEntity;
import com.openjiuwen.studio.common.service.entity.LicenseRecordEntity;
import com.openjiuwen.studio.common.service.entity.ResourceInstEntity;
import com.openjiuwen.studio.common.service.model.skuinst.LicenseInst;
import com.openjiuwen.studio.common.service.model.skuinst.LicenseReport;
import com.openjiuwen.studio.common.service.model.skuinst.LicenseReportReq;
import com.openjiuwen.studio.common.service.model.skuinst.ResourceInst;
import com.openjiuwen.studio.common.service.repository.LicenseRecordRepository;
import com.openjiuwen.studio.common.service.repository.LicenseRepository;
import com.openjiuwen.studio.common.service.repository.ResourceInstRepository;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ResourceInstService {

    @Autowired
    private ResourceInstRepository resourceInstRepository;

    @Autowired
    private LicenseRepository licenseRepository;

    @Autowired
    private LicenseRecordRepository licenseRecordRepository;

    private static final String OPERTYPE_ADD = "add";

    private static final String OPERTYPE_DELETE = "delete";

    /**
     * 查询租户订购实例
     *
     * @return 资源订购实例列表
     */
    public List<ResourceInst> queryResourceInsts(String domainId, Boolean isLicenseDetail) {
        List<ResourceInstEntity> resourceInstEntityList = resourceInstRepository.selectActiveResourcesByDomainId(domainId);
        if (ValidateUtils.isEmptyCollection(resourceInstEntityList)) {
            return new ArrayList<>();
        }
        List<ResourceInst> resourceInsts = CopyUtils.copyList(resourceInstEntityList, ResourceInst.class);
        if (isLicenseDetail == null || !isLicenseDetail) {
            return resourceInsts;
        }
        List<LicenseEntity> licenseEntityList = licenseRepository.selectByConditions(null, null, null, domainId, CommonConstant.ResourceStatus.SUBSCRIBED);
        if (ValidateUtils.isEmptyCollection(licenseEntityList)) {
            return resourceInsts;
        }

        List<LicenseInst> licenseInsts = CopyUtils.copyList(licenseEntityList, LicenseInst.class);
        Map<String, List<LicenseInst>> licenseInstMap = licenseInsts.stream().collect(Collectors.groupingBy(LicenseInst::getResourceId));
        resourceInsts.forEach(it -> {
            it.setAttrList(licenseInstMap.getOrDefault(it.getResourceId(), new ArrayList<>()));
        });
        return resourceInsts;
    }

    /**
     * 根据条件查询租户订购实例
     *
     * @param resId    资源ID
     * @param attrCode 属性编码
     * @param skuCode  sku编码
     * @param domainId  sku编码
     * @return license控制项列表
     */
    public List<LicenseInst> queryLicense(String resId, String attrCode, String skuCode, String domainId) {
        List<LicenseEntity> licenseEntityList = licenseRepository.selectByConditions(resId, attrCode, skuCode, domainId, CommonConstant.ResourceStatus.SUBSCRIBED);
        if (ValidateUtils.isEmptyCollection(licenseEntityList)) {
            return new ArrayList<>();
        }
        return CopyUtils.copyList(licenseEntityList, LicenseInst.class);
    }

    /**
     * license消费
     *
     * @param req 上报请求
     * @param domainId 租户ID
     */
    public void reportLicense(LicenseReportReq req, String domainId) {
        if (ValidateUtils.isEmptyCollection(req.getList())) {
            return;
        }
        req.getList().forEach(licenseReport -> {
            if (CommonConstant.CommonOperType.DELETE.equals(licenseReport.getOperType())) {
                // 释放license资源场景
                deleteLicense(licenseReport, domainId);
            } else {
                // 消费license资源场景
                addLicense(licenseReport, domainId);
            }
        });
    }

    private void addLicense(LicenseReport licenseReport, String domainId) {
        List<LicenseEntity> licenseEntityList = licenseRepository.selectByConditions(licenseReport.getResourceId(), licenseReport.getAttrCode(), null, domainId, CommonConstant.ResourceStatus.SUBSCRIBED);
        if (ValidateUtils.isEmptyCollection(licenseEntityList)) {
            throw new AgentStudioException(StudioError.INVALID_LICENSE);
        } else {
            // 逻辑优化，当前值超过maxValue时，不做控制，具体控制逻辑由下游业务根据实际场景做控制，纯记录
            LicenseEntity usageEntity = licenseEntityList.get(0);
            BigDecimal currentValue = new BigDecimal(usageEntity.getCurrentValue());
            BigDecimal consumedValue = new BigDecimal(licenseReport.getQuantity());
            BigDecimal now = currentValue.add(consumedValue);
            usageEntity.setCurrentValue(now.toString());
            usageEntity.setLastUpdatedDate(DateUtils.getNow());
            licenseRepository.updateById(usageEntity);

            // 添加license消费记录
            licenseRecord(usageEntity, licenseReport, domainId, OPERTYPE_ADD);
        }
    }

    private void licenseRecord(LicenseEntity usageEntity, LicenseReport licenseReport, String domainId, String operType) {
        LicenseRecordEntity entity = (LicenseRecordEntity) CopyUtils.copyProperties(licenseReport, new LicenseRecordEntity());
        entity.setId(UuidUtils.getUUID());
        entity.setSkuCode(usageEntity.getSkuCode());
        entity.setOperType(operType);
        entity.setQuantity(licenseReport.getQuantity());
        entity.setCreatedDate(DateUtils.getNow());
        entity.setLastUpdatedDate(DateUtils.getNow());
        entity.setCreatedByUserId(RequestContextUtils.getRequestUserId());
        entity.setLastUpdatedByUserId(RequestContextUtils.getRequestUserId());
        entity.setDomainId(domainId);
        licenseRecordRepository.insert(entity);
    }


    /**
     * 校验可用余量
     *
     * @param usageEntity 可用余量
     */
    private void validAvailableLicense(LicenseEntity usageEntity) {
        if (CommonConstant.AttrType.COUNT.equals(usageEntity.getType())) {
            // 无上限token消耗
            if (CommonConstant.INFINITY.equals(usageEntity.getMaxValue())) {
                return;
            }
            BigDecimal currentValue = new BigDecimal(usageEntity.getCurrentValue());
            BigDecimal maxValue = new BigDecimal(usageEntity.getMaxValue());
            if (currentValue.compareTo(maxValue) >= 0) {
                throw new AgentStudioException(StudioError.NO_LICENSE_CAN_BE_CONSUMED);
            }
        }
    }

    private void deleteLicense(LicenseReport licenseReport, String domainId) {
        List<LicenseEntity> licenseEntityList = licenseRepository.selectByConditions(licenseReport.getResourceId(), licenseReport.getAttrCode(), null, domainId, CommonConstant.ResourceStatus.SUBSCRIBED);
        if (ValidateUtils.isEmptyCollection(licenseEntityList)) {
            throw new AgentStudioException(StudioError.INVALID_LICENSE);
        } else {
            LicenseEntity usageEntity = licenseEntityList.get(0);
            BigDecimal currentValue = new BigDecimal(usageEntity.getCurrentValue());
            BigDecimal consumedValue = new BigDecimal(licenseReport.getQuantity());
            BigDecimal now = currentValue.subtract(consumedValue);
            if (currentValue.compareTo(BigDecimal.ZERO) <= 0) {
                throw new AgentStudioException(StudioError.NO_LICENSE_CAN_BE_RELEASED);
            }
            if (now.compareTo(BigDecimal.ZERO) < 0) {
                throw new AgentStudioException(StudioError.NO_ENOUGH_LICENSE_CAN_BE_RELEASED);
            }
            usageEntity.setCurrentValue(now.toString());
            usageEntity.setLastUpdatedDate(DateUtils.getNow());
            licenseRepository.updateById(usageEntity);

            // 添加license消费记录
            licenseRecord(usageEntity, licenseReport, domainId, OPERTYPE_DELETE);
        }
    }
}