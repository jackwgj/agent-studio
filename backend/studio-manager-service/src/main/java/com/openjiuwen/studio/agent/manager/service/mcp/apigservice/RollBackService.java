/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2028. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.mcp.apigservice;

import com.openjiuwen.studio.agent.manager.mapper.ApigApisMapper;
import com.openjiuwen.studio.agent.manager.service.mcp.apigservice.bean.ApigApisDto;
import com.openjiuwen.studio.agent.manager.utils.CommonUtil;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

/**
 * @ClassName : RollBackService
 * @Description :回滚服务，与@EatException注解搭配使用
 * @Date : Created in 2024/7/2 15:36
 * @Version : V1.0
 */
@Slf4j
@Service
public class RollBackService {


    @Autowired
    private ApigApisMapper apigApisMapper;

    /**
     * 修改API-回滚-API表回滚
     *
     * @param oldApigApisDto oldApicApisDto
     */
    public void modifyApiTableRollBack(ApigApisDto oldApigApisDto) {
        oldApigApisDto.setLastUpdatedByUserId(CommonUtil.getUserId());
        oldApigApisDto.setLastUpdatedDate(new Timestamp(System.currentTimeMillis()));
        apigApisMapper.update(oldApigApisDto);
    }
}
