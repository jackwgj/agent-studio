/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.mapper;

import com.openjiuwen.studio.prompt.engineering.entity.PeEvaluationResult;
import com.openjiuwen.studio.prompt.engineering.entity.PeEvaluationTask;
import com.openjiuwen.studio.prompt.engineering.enums.EvaluationTaskStatus;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 针对表【t_pe_evaluation_task】的数据库操作Mapper
 *
 */
@Mapper
public interface PeEvaluationTaskMapper {
    List<String> queryEvaluationTaskIdsByTestSetId(String testSetId, String workspaceId);

    Boolean isIdExist(String id, String workspaceId);

    Boolean isNameExist(String taskId, String name, String workspaceId);

    Boolean isNameDuplicate(String id, String taskId, String name, String workspaceId);

    void insertOneEvaluationTask(PeEvaluationTask evaluationTask);

    Boolean isEvaluationTaskExist(String id, String workspaceId);

    void deleteEvaluationTaskById(String id, String workspaceId);

    void deleteEvaluationResultByEvaluationTaskId(String evaluationTaskId, String workspaceId);

    List<PeEvaluationTask> queryEvaluationTaskDetailsById(String id, String workspaceId);

    List<PeEvaluationTask> queryEvaluationTaskDetailsByIds(List<String> ids, String workspaceId);

    Integer updateEvaluationTaskInfoById(@Param("id") String id,  @Param("name") String name,  @Param("description") String description,
                                         @Param("status") EvaluationTaskStatus status,  @Param("workspaceId") String workspaceId);

    List<EvaluationTaskStatus> queryEvaluationTaskStatusByTaskId(String taskId, String workspaceId);

    List<PeEvaluationResult> queryEvalResultsByEvalTaskId(String taskId, String workspaceId);

    List<PeEvaluationTask> queryEvaluationTaskByTaskId(String taskId, String workspaceId);

    void deleteByTaskId(String taskId, String workspaceId);

    void deleteEvaluationResultByEvaluationTaskIds(List<String> evaluationTaskIds, String workspaceId);

    String queryLatestSuccessEvaluationTaskIdByTaskId(String taskId, String workspaceId);

    String queryLatestEvaluationTaskIdByTaskId(String taskId, String workspaceId);

    List<PeEvaluationTask> queryByTaskIdAndStatus(String taskId, String workspaceId);

    void batchInsertEvalResult(List<PeEvaluationResult> results);

    EvaluationTaskStatus queryEvaluationTaskStatusById(String id, String workspaceId);

    void updateEvaluationTaskStatusById(String id, EvaluationTaskStatus status, String workspaceId);

    @Select("SELECT COUNT(1) FROM t_pe_evaluation_task WHERE id = #{id} AND status = #{status} AND workspace_id = #{workspaceId}")
    int isThisStatusById(String id, EvaluationTaskStatus status, String workspaceId);

    @Select("SELECT COUNT(1) FROM t_pe_evaluation_result WHERE evaluation_task_id = #{taskId} AND test_num = #{testNum} AND workspace_id = #{workspaceId}")
    int countThisTestNumById(String taskId, int testNum, String workspaceId);

    @Select("SELECT COUNT(1) FROM t_pe_evaluation_task WHERE task_id = #{taskId} AND workspace_id = #{workspaceId}")
    int countByTaskId(String taskId, String workspaceId);
}
