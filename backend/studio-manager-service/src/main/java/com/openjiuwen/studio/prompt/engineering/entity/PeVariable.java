
/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.entity;

import com.openjiuwen.studio.prompt.engineering.dto.VariableVo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * 工程任务变量表
 *
 * @TableName t_pe_variable
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeVariable implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 唯一标识一个变量
     */
    private String id;

    /**
     * 工程任务id
     */
    private String peTaskId;

    /**
     * 参数Key
     */
    private String key;

    /**
     * 参数名称
     */
    private String name;

    /**
     * 引用该变量的模板数
     */
    private Integer relationCount;

    private long createTime;

    private String workspaceId;

    public VariableVo convertToVariableVo() {
        PeVariableToVariableVoConverter peVariableToVariableVoConverter = new PeVariableToVariableVoConverter();
        return peVariableToVariableVoConverter.convert(this);
    }

    private static class PeVariableToVariableVoConverter implements FormConvert<PeVariable, VariableVo> {
        @Override
        public VariableVo convert(PeVariable variable) {
            VariableVo variableVo = new VariableVo();
            variableVo.setId(variable.getId());
            variableVo.setName(variable.getName());
            variableVo.setKey(variable.getKey());
            variableVo.setPeTaskId(variable.getPeTaskId());
            return variableVo;
        }
    }
}
