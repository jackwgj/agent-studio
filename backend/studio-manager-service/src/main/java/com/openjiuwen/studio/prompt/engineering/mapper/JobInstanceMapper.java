/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.mapper;

import com.openjiuwen.studio.prompt.engineering.task.dao.JobInstance;
import com.openjiuwen.studio.prompt.engineering.task.dao.JobSpec;
import com.openjiuwen.studio.prompt.engineering.task.domain.JobStatus;
import com.openjiuwen.studio.prompt.engineering.task.vo.TaskQueryCondition;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务调度mapper
 *
 */
@Mapper
public interface JobInstanceMapper {
    @Select("SELECT COUNT(1) FROM t_job_instance WHERE id = #{id} AND status = #{jobStatus} AND workspace_id = #{workspaceId}")
    int isThisStatusById(String id, JobStatus jobStatus, String workspaceId);

    @Select("SELECT * FROM t_job_spec WHERE name = #{jobSpecName}")
    JobSpec queryJobSpecByName(String jobSpecName);

    @Insert("INSERT INTO t_job_instance VALUES(" + "#{id}" + ", #{name}" + ", #{createdOn}" + ", #{updatedOn}" + ", #{jobSpecId}"
        + ", #{context}" + ", #{retryTime}" + ", #{status}" + ", #{startedOn}" + ", #{endedOn}" + ", #{handlerId}"
        + ", #{accountId}, #{workspaceId})")
    void create(JobInstance jobInstance);

    @Update("UPDATE t_job_instance SET status = 'running', started_on = #{date} WHERE id = #{id} AND workspace_id = #{workspaceId}")
    void startAt(String id, LocalDateTime date, String workspaceId);

    @Update("UPDATE t_job_instance SET " + "created_on = #{createdOn}" + ", updated_on = #{updatedOn}"
        + ", job_spec_id = #{jobSpecId}" + ", context = #{context}" + ", retry_time = #{retryTime}"
        + ", status = #{status}" + ", started_on = #{startedOn}" + ", ended_on = #{endedOn}"
        + ", handler_id = #{handlerId}" + ", account_id = #{accountId}" + " WHERE id = #{id} and workspace_id = #{workspaceId}")
    void update(JobInstance jobInstance);

    @Update("UPDATE t_job_instance SET " + "updated_on = #{updatedOn}" + ", context = #{context}" + " WHERE id = #{id} AND workspace_id = #{workspaceId}")
    void updateCtx(JobInstance jobInstance);

    @Update("<script>UPDATE t_job_instance SET status = #{status} WHERE workspace_id = #{workspaceId} AND id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id} </foreach></script>")
    void batchUpdateStatusByIds(JobStatus status, List<String> ids);

    List<JobInstance> queryWithCondition2(TaskQueryCondition condition);
}

