package com.openjiuwen.studio.agent.manager.service;   

import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.bo.SkillDetails;
import com.openjiuwen.studio.agent.manager.dto.GetStudioSkillDetailsResponseBody;
import com.openjiuwen.studio.agent.manager.dto.ListStudioSkillsQo;
import com.openjiuwen.studio.agent.manager.dto.ListStudioSkillsResponseBody;
import com.openjiuwen.studio.agent.manager.dto.SkillSource;
import com.openjiuwen.studio.agent.manager.dto.SkillStatus;
import com.openjiuwen.studio.agent.manager.dto.StudioSkillInfo;
import com.openjiuwen.studio.agent.manager.entity.SkillEntity;
import com.openjiuwen.studio.agent.manager.mapper.SIESkillMapper;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * SIE Skill 查询服务（仅用于组件库与资产广场的 Skill 展示）
 */
@Slf4j
@Service
public class SIESkillManagementService implements ISIESkillManagementService {

    private final SIESkillMapper sieskillMapper;

    public SIESkillManagementService(SIESkillMapper sieskillMapper) {
        this.sieskillMapper = sieskillMapper;
    }

    /**
     * 查询 Skill 列表（支持分页、过滤）
     */
    public ListStudioSkillsResponseBody listStudioSkills(String projectId, ListStudioSkillsQo listStudioSkillsQo) {
        SkillEntity selectCondition;
        Integer publishedAsset = listStudioSkillsQo.getPublishedAsset();
        if (Integer.valueOf(1).equals(publishedAsset)) {
            selectCondition = new SkillEntity()
                .setName(escapeSqlSpecialChars(listStudioSkillsQo.getName()))
                .setDescription(escapeSqlSpecialChars(listStudioSkillsQo.getDescription()))
                .setStatus(listStudioSkillsQo.getStatus())
                .setSource(listStudioSkillsQo.getSource())
                .setWorkspaceId(listStudioSkillsQo.getWorkspaceId())
                .setProjectId(projectId)
                .setPublishedAsset(publishedAsset);
        } else {
            selectCondition = new SkillEntity()
                .setDomainId(RequestContextUtils.getRequestUserDomainId())
                .setName(escapeSqlSpecialChars(listStudioSkillsQo.getName()))
                .setDescription(escapeSqlSpecialChars(listStudioSkillsQo.getDescription()))
                .setStatus(listStudioSkillsQo.getStatus())
                .setSource(listStudioSkillsQo.getSource())
                .setWorkspaceId(listStudioSkillsQo.getWorkspaceId())
                .setProjectId(projectId);
        }

        List<SkillDetails> selectedSkills = sieskillMapper.search(
            selectCondition,
            listStudioSkillsQo.getOffset(),
            listStudioSkillsQo.getLimit(),
            listStudioSkillsQo.getPriorityStatus(),
            listStudioSkillsQo.getTagId(),
            publishedAsset
        );

        ListStudioSkillsResponseBody responseBody = new ListStudioSkillsResponseBody()
            .setTotal(sieskillMapper.countSelectedSkills(selectCondition));
        responseBody.setItems(buildRsqEntity(selectedSkills));
        return responseBody;
    }

    /**
     * 查询 Skill 详情（默认最新版本）
     */
    public GetStudioSkillDetailsResponseBody showStudioSkillDetail(String skillId, String workspaceId,
        String projectId) {
        SkillDetails skill = sieskillMapper.searchBySkillId(skillId);
        if (skill == null) {
            return new GetStudioSkillDetailsResponseBody().setSkillInfo(new StudioSkillInfo());
        }

        // 只返回 obsPath，不生成临时下载 URL
        StudioSkillInfo skillInfo = new StudioSkillInfo()
            .setSkillId(skill.getSkillId())
            .setDomainId(skill.getDomainId())
            .setSkillName(skill.getName())
            .setStatus(SkillStatus.fromValue(skill.getStatus()))
            .setSource(SkillSource.fromValue(skill.getSource()))
            .setIcon(skill.getIcon())
            .setDescription(skill.getDescription())
            .setCreatorName(skill.getCreatorName())
            .setCreatedTime(skill.getCreatedAt())
            .setUpdatedTime(skill.getUpdatedAt())
            .setLatestVersion(skill.getLatestVersion())
            .setUsedVersion(skill.getUsedVersion())
            .setUsedVersionName(skill.getVersionName())
            .setObsPath(skill.getObsPath());  

        return new GetStudioSkillDetailsResponseBody().setSkillInfo(skillInfo);
    }

    private List<StudioSkillInfo> buildRsqEntity(List<SkillDetails> skills) {
        List<StudioSkillInfo> items = new ArrayList<>();
        skills.forEach(skill -> {
            items.add(new StudioSkillInfo()
                .setSkillId(skill.getSkillId())
                .setDomainId(skill.getDomainId())
                .setSkillName(skill.getName())
                .setStatus(SkillStatus.fromValue(skill.getStatus()))
                .setSource(SkillSource.fromValue(skill.getSource()))
                .setIcon(skill.getIcon())
                .setDescription(skill.getDescription())
                .setCreatorName(skill.getCreatorName())
                .setCreatedTime(skill.getCreatedAt() == null ? null : skill.getCreatedAt())
                .setUpdatedTime(skill.getUpdatedAt() == null ? null : skill.getUpdatedAt())
                .setLatestVersion(skill.getLatestVersion())
                .setObsPath(skill.getObsPath())
                .setUsedVersion(skill.getUsedVersion())
                .setUsedVersionName(skill.getVersionName()));
        });
        return items;
    }

    /**
     * 转换 SQL 特殊字符（防止 SQL 注入）
     */
    private String escapeSqlSpecialChars(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        return content.replace("!", "!!")
            .replace("%", "!%")
            .replace("_", "!_");
    }
}