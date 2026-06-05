/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.mapper;

import com.openjiuwen.studio.prompt.engineering.dto.QueryTemplateCondition;
import com.openjiuwen.studio.prompt.engineering.entity.PePromptTemplate;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PePromptTemplateMapper {

    List<String> selectAllIds();

    void bindingTemplateIdAndTagId(@Param("templateId") String templateId, @Param("tagIds") List<String> tagIds, @Param("workspaceId") String workspaceId);

    void insertOneTemplate(@Param("template") PePromptTemplate pePromptTemplate,
                           @Param("projectId") String projectId,
                           @Param("industryId")String industryId);

    void unbindTemplateIdAndTagId(String templateId, String workspaceId);

    void unbindTemplateIdAndTagIdByTemplateIds(List<String> templateIds);

    void deleteTemplateByTemplateId(@Param("templateId") String templateId, @Param("workspaceId") String workspaceId);

    int copyToHistory(@Param("templateId") String templateId, @Param("workspaceId") String workspaceId, @Param("tagId") String tagId);

    List<String> findTemplatesToBeDeleted(LocalDateTime twoYearsAgo);

    void deleteTemplatesPermanently(List<String> templateIds);

    void batchUnbindTemplateIdAndTagId(List<String> templateIds, String workspaceId);

    void batchDeleteTemplateByTemplateId(List<String> templateIds, String workspaceId);

    List<String> queryTemplateIdsByCondition(@Param("body") QueryTemplateCondition body, @Param("projectId") String projectId, @Param("workspaceId") String workspaceId, @Param("category") String category);

    List<PePromptTemplate> queryTemplateDetailByTemplateIds(List<String> resultIdsByCondition, @Param("workspaceId") String workspaceId);

    List<PePromptTemplate> queryTemplateDetailByPersonTemplateIds(List<String> resultIdsByCondition, @Param("workspaceId") String workspaceId);

    List<PePromptTemplate> queryTemplateDetailByCreatorTemplateIds(List<String> resultIdsByCondition);

    List<PePromptTemplate> queryTemplateDetailByTemplateId(List<String> resultIdsByCondition, @Param("workspaceId") String workspaceId);

    Integer updateTemplate(@Param("pePromptTemplate") PePromptTemplate pePromptTemplate, @Param("industryId") String industryId);

    Boolean isNameExist(String name, String id, String projectId, String workspaceId);

    Boolean isNameExist2(String name, String projectId, String workspaceId);

    int countPresetTemplateByProjectId(String projectId, String workspaceId);

    int countTemplateByProjectId(String projectId, String workspaceId);

    int countTemplateByIndustryId(String industryId, String workspaceId);

    List<String> queryTemplateNameByName(@Param("templateNames") List<String> templateNames, String workspaceId);

    List<String> queryTemplateIdsByCondition2(@Param("projectId")String projectId, @Param("workspaceId") String workspaceId, @Param("name") String name);

    PePromptTemplate queryTemplateIds(@Param("id")String promptId, @Param("workspaceId") String workspaceId);

    PePromptTemplate queryTemplate(@Param("id")String promptId);

    List<String> queryTemplateNameByNameV2(@Param("templateNames") List<String> templateNames, @Param("projectId") String projectId, @Param("workspaceId") String workspaceId);

    PePromptTemplate queryTemplateDetailById(@Param("id") String promptId, @Param("projectId") String projectId, @Param("workspaceId") String workspaceId);

    void updatePromptShareStatus(@Param("id") String id, @Param("status") int status);

    List<PePromptTemplate> batchQueryTemplateByIds(@Param("ids") List<String> ids, @Param("projectId") String projectId);
}
