
/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.entity;

import com.openjiuwen.studio.prompt.engineering.dto.TagVo;
import com.openjiuwen.studio.prompt.engineering.dto.TaskVo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 工程任务表
 *
 * @TableName t_pe_task
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeTask implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 唯一标识一个工程任务
     */
    private String id;

    /**
     * 任务名称
     */
    private String name;

    /**
     * 任务描述
     */
    private String description;

    /**
     * 创建人
     */
    private String creator;

    /**
     * 修改人
     */
    private String updater;

    /**
     * 创建时间
     */
    private Date createdOn;

    /**
     * 修改时间
     */
    private Date updatedOn;

    /**
     * 绑定的标签信息
     */
    private List<PeTag> tagList;

    /**
     * 候选模板迭代次数
     */
    private Integer iterationNum;

    /**
     * 行业
     */
    private Industry industry;

    private String workspaceId;

    public TaskVo convertToTaskVo() {
        PeTaskToTaskVoConverter toPeConversionVoConverter = new PeTaskToTaskVoConverter();
        return toPeConversionVoConverter.convert(this);
    }

    private static class PeTaskToTaskVoConverter implements FormConvert<PeTask, TaskVo> {
        @Override
        public TaskVo convert(PeTask peTask) {
            TaskVo taskVo = new TaskVo();
            BeanUtils.copyProperties(peTask, taskVo);
            taskVo.setIndustry(Objects.nonNull(peTask.getIndustry()) ? peTask.getIndustry().convertToIndustryVo() : null);
            List<TagVo> peTagList = new ArrayList<>();
            if (!peTask.getTagList().isEmpty() && !peTask.tagList.isEmpty()) {
                peTask.getTagList().forEach(tag -> {
                    TagVo tagVo = new TagVo();
                    BeanUtils.copyProperties(tag, tagVo);
                    peTagList.add(tagVo);
                });
            }
            taskVo.setTagList(peTagList);
            return taskVo;
        }
    }
}
