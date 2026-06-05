/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.task.vo;

import com.openjiuwen.studio.prompt.engineering.task.domain.JobSpecName;
import com.openjiuwen.studio.prompt.engineering.task.domain.JobStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.Set;

/**
 * 任务列表的查询条件
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskQueryCondition extends BaseReq {
    private String handlerId;

    private Set<JobStatus> statuses;

    private Set<String> accountIds;

    private Set<JobSpecName> taskType;

    private String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        TaskQueryCondition that = (TaskQueryCondition) o;
        return Objects.equals(handlerId, that.handlerId) && Objects.equals(statuses, that.statuses)
                && Objects.equals(accountIds, that.accountIds) && Objects.equals(taskType, that.taskType)
                && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), handlerId, statuses, accountIds, taskType, name);
    }
}
