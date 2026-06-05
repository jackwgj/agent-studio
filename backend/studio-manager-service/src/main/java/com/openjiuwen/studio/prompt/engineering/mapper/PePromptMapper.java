
/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.mapper;

import com.openjiuwen.studio.prompt.engineering.dto.QueryCondition;
import com.openjiuwen.studio.prompt.engineering.entity.PePrompt;
import com.openjiuwen.studio.prompt.engineering.entity.SmallPrompt;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PePromptMapper {
    void deleteRecordsByTaskId(String taskId, String workspaceId);

    void deleteByTaskId(String taskId, String workspaceId);

    List<String> queryVariablesByTaskId(String taskId, String workspaceId);

    void savePrompt(PePrompt pePrompt);

    void deleteByPromptId(String promptId, String workspaceId);

    PePrompt queryByPromptId(String promptId, String workspaceId);

    List<PePrompt> queryByCondition(@Param("body") QueryCondition body, @Param("workspaceId") String workspaceId);

    int countCandidateTemplateByTaskId(String taskId, String workspaceId);

    void updatePrompt(PePrompt pePrompt);

    Integer updateSmallPrompt(@Param("smallPrompt") SmallPrompt smallPrompt, @Param("workspaceId") String workspaceId);

    List<PePrompt> queryPromptsByIds(List<String> promptIds, String workspaceId);

    Boolean isPromptIdExist(String promptId, String taskId, String workspaceId);

    Boolean isNameExist(String name, String taskId, String id, String workspaceId);

    Integer checkDuplicateName(@Param("name") String name, @Param("id") String id, @Param("workspaceId") String workspaceId);
}
