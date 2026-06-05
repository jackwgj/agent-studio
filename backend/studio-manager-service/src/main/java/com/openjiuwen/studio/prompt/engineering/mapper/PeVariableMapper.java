/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.mapper;

import com.openjiuwen.studio.prompt.engineering.entity.PeVariable;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 针对表【t_pe_task(工程任务表)】的数据库操作Mapper
 *
 */
@Mapper
public interface PeVariableMapper {
    /**
     * 根据变量名和任务id查询变量数
     *
     * @return int 变量数
     */
    int countVariableNameByNameAndTaskId(String taskId, List<String> variables, String workspaceId);

    /**
     * 批量插入变量
     *
     * @param variables variables
     */
    void batchInsertVariable(@Param("variables") List<PeVariable> variables, @Param("workspaceId") String workspaceId);

    /**
     * 根据任务id查询变量
     *
     * @return List<PeVariable> 变量
     */
    List<PeVariable> queryVariablesByTaskId(String taskId, String workspaceId);

    /**
     * 更新变量
     *
     * @param needUpdateVariable needUpdateVariable
     */
    void updateVariable(@Param("needUpdateVariable") PeVariable needUpdateVariable, @Param("workspaceId") String workspaceId);

    /**
     * 查询变量
     *
     * @param variableIdList variableIdList
     * @return List<PeVariable>
     */
    List<PeVariable> queryVariablesById(List<String> variableIdList, String workspaceId);

    /**
     * 根据taskID查询变量key
     *
     * @param taskId
     * @return
     */
    List<String> queryVariableKeysByTaskId(String taskId, String workspaceId);

    List<PeVariable> selectVariablesByIds(List<String> variableIds, String workspaceId);

    void updateVariableRelationCount(String id, String workspaceId);

    void deleteVariablesByTaskId(String taskId, String workspaceId);

    List<PeVariable> queryVariablesByTaskIdAndKey(List<String> variableKeys, String taskId, String workspaceId);

    void batchRaiseVariableRelationCount(List<String> needRaise, String taskId, String workspaceId);

    void batchReduceVariableRelationCount(List<String> needReduce, String workspaceId);

    void deleteAllVariablesByTaskId(String taskId, String workspaceId);
}
