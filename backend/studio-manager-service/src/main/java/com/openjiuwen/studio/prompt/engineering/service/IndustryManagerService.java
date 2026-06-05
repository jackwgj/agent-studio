/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.service;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.prompt.engineering.dto.IndustryDto;
import com.openjiuwen.studio.prompt.engineering.dto.IndustryVo;
import com.openjiuwen.studio.prompt.engineering.entity.Industry;
import com.openjiuwen.studio.prompt.engineering.mapper.PeIndustryMapper;
import com.openjiuwen.studio.prompt.engineering.mapper.PePromptTemplateMapper;
import com.openjiuwen.studio.prompt.engineering.mapper.PeTaskMapper;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class IndustryManagerService implements IIndustryManagerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(IndustryManagerService.class);

    @Resource
    private PeIndustryMapper industryMapper;

    @Resource
    private PePromptTemplateMapper templateMapper;

    @Resource
    private PeTaskMapper taskMapper;

    private static final String PROMPT_LIBRARY_TYPE = "prompt";


    public String createIndustry(String projectId, String workspaceId, IndustryDto body) {
        log.info("operation log {} : create industry.", projectId);
        if (Boolean.TRUE.equals(industryMapper.isExist(body, workspaceId))) {
            throw new AgentStudioException(StudioError.INDUSTRY_NAME_EXIST);
        }
        try {
            industryMapper.insertOne(Industry.convertToIndustry(body, workspaceId));
        } catch (RuntimeException e) {
            LOGGER.error("Failed to insert industry {} into database", body.getName(), e);
            throw new AgentStudioException(StudioError.CREATE_INDUSTRY_FAILED, body.getName());
        }
        return HttpStatus.OK.toString();
    }


    public String deletaIndustry(String projectId, String industryId, String workspaceId) {
        log.info("operation log {} : delete industry.", projectId);
        int templates = templateMapper.countTemplateByIndustryId(industryId, workspaceId);
        int tasks = taskMapper.countByIndustryId(industryId, workspaceId);
        if (tasks > 0 || templates > 0) {
            throw new AgentStudioException(StudioError.INDUSTRY_IN_USE);
        }
        Integer cnt = industryMapper.deleteById(industryId, workspaceId);
        if(cnt < 1) {
            throw new AgentStudioException(StudioError.INDUSTRY_ID_NOT_EXIST);
        }
        return HttpStatus.OK.toString();
    }

    @Override
    public List<IndustryVo> listIndustry(String projectId, String workspaceId, String libraryType) {
        log.info("operation log {} : list industry.", projectId);
        try {
            List<Industry> industries;

            if (StringUtils.isBlank(libraryType)) {
                libraryType = PROMPT_LIBRARY_TYPE;
            }
            industries = industryMapper.queryIndustryByLibraryType(libraryType);
            ArrayList<IndustryVo> industryVos = new ArrayList<>();
            industries.forEach(e -> industryVos.add(e.convertToIndustryVo()));
            return industryVos;
        } catch (RuntimeException e) {
            throw new AgentStudioException(StudioError.QUERY_INDUSTRY_FAILED);
        }
    }


    public String updateIndustry(String projectId, String workspaceId, IndustryDto body) {
        log.info("operation log {} : update industry.", projectId);
        if (Boolean.TRUE.equals(industryMapper.isExist(body, workspaceId))) {
            throw new AgentStudioException(StudioError.INDUSTRY_NAME_EXIST);
        }
        try {
            Integer cnt = industryMapper.updateOne(Industry.convertToIndustry(body, workspaceId));
            if(cnt < 1) {
                throw new AgentStudioException(StudioError.INDUSTRY_ID_NOT_EXIST);
            }
            return HttpStatus.OK.toString();
        } catch (RuntimeException e) {
            LOGGER.error("Failed to update industry, id: {} ", body.getId(), e);
            throw new AgentStudioException(StudioError.UPDATE_INDUSTRY_FAILED, body.getId());
        }
    }
}
