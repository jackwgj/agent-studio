/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.jiuwen.adapt;

import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.common.utils.SpringBeanUtils;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.dto.ParamExtractionDomainObject;
import com.openjiuwen.studio.agent.manager.dto.ParamExtractionExceptionConfig;
import com.openjiuwen.studio.agent.manager.dto.ParamExtractionWorkflow;
import com.openjiuwen.studio.agent.manager.dto.WorkflowBranchConfigVO;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;
import com.openjiuwen.studio.agent.manager.service.WorkflowManagementService;
import com.openjiuwen.studio.agent.manager.utils.IRUtils;
import com.openjiuwen.studio.agent.manager.utils.JsonUtils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 参数提取节点转换IR
 *
 */
public class ParamExtractionAdapter extends AbstractIRNodeAdapter {
    /**
     * ID
     */
    private static final String ID = "id";

    /**
     * obs路径
     */
    private static final String PATH = "path";

    /**
     * 输入引用
     */
    private static final String INPUTS = "inputs";

    /**
     * 领域对象
     */
    private static final String DOMAIN_OBJECTS = "domain_objects";

    /**
     * prompt
     */
    private static final String PROMPT = "prompt";

    /**
     * 意图退出开关
     */
    private static final String ENABLE_INTENT_BREAK = "enable_intent_break";

    /**
     * 意图退出prompt
     */
    private static final String INTENT_BREAK_PROMPT = "intent_break_prompt";

    /**
     * 执行时机
     */
    private static final String EXECUTION_TIMING = "execution_timing";

    /**
     * 处理工作流
     */
    private static final String PROCESSING_WORKFLOWS = "processing_workflows";

    /**
     * 拓展工作流
     */
    private static final String EXTENSION_WORKFLOWS = "extension_workflows";

    /**
     * 异常配置
     */
    private static final String EXCEPTION_CONFIG = "exception_config";

    /**
     * 最大迭代轮数
     */
    private static final String MAX_ITERATION = "max_iteration";

    /**
     * 跳出条件
     */
    private static final String BREAK_CONDITION = "break_condition";

    /**
     * 进入条件
     */
    private static final String ENTRY_CONDITION = "entry_condition";

    /**
     * 异常条件
     */
    private static final String EXCEPTION_CONDITION = "exception_condition";

    @Override
    public Map<String, Object> adaptConfig(WorkflowNodeVO workflowNodeVo) {
        Map<String, Object> configs = new HashMap<>();
        Map<String, Object> nodeConfigs = workflowNodeVo.getConfigs();
        WorkflowManagementService wmService = SpringBeanUtils.getBean(WorkflowManagementService.class);

        // 模型相关配置
        adaptModel(configs, nodeConfigs);
        configs.put(PROMPT, nodeConfigs.get(PROMPT));
        configs.put(IRUtils.adaptKey(ENABLE_INTENT_BREAK), nodeConfigs.get(ENABLE_INTENT_BREAK));
        if (Boolean.TRUE.equals(nodeConfigs.get(ENABLE_INTENT_BREAK))) {
            configs.put(IRUtils.adaptKey(INTENT_BREAK_PROMPT), nodeConfigs.get(INTENT_BREAK_PROMPT));
        }
        if (nodeConfigs.get(BREAK_CONDITION) != null) {
            configs.put(IRUtils.adaptKey(BREAK_CONDITION), adaptCondition(nodeConfigs.get(BREAK_CONDITION)));
        }
        if (nodeConfigs.get(ENTRY_CONDITION) != null) {
            configs.put(IRUtils.adaptKey(ENTRY_CONDITION), adaptCondition(nodeConfigs.get(ENTRY_CONDITION)));
        }

        // 领域对象相关配置
        List<Object> domainObjects = JsonUtils.objectToClass(nodeConfigs.get(DOMAIN_OBJECTS));
        List<Object> domainObjectsIr = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(domainObjects)) {
            domainObjects.forEach(item -> {
                ParamExtractionDomainObject domainObject =
                    JsonUtils.objectToClassType(item, ParamExtractionDomainObject.class);
                if (domainObject != null) {
                    Map<String, Object> domainObjectIr = new HashMap<>();
                    domainObjectIr.put(ID, domainObject.getName());
                    List<Map<String, Object>> processingWorkflowsIr = new ArrayList<>();

                    // 领域对象子工作流处理
                    domainObject.getProcessingWorkflows()
                        .forEach(processingWorkflow -> processingWorkflowsIr
                            .add(adaptProcessingWorkflow(processingWorkflow, wmService)));
                    domainObjectIr.put(IRUtils.adaptKey(PROCESSING_WORKFLOWS), processingWorkflowsIr);
                    domainObjectsIr.add(domainObjectIr);
                }
            });
        }
        configs.put(IRUtils.adaptKey(DOMAIN_OBJECTS), domainObjectsIr);

        // 拓展工作量相关配置
        List<Object> extensionWorkflows = JsonUtils.objectToClass(nodeConfigs.get(EXTENSION_WORKFLOWS));
        List<Object> extensionWorkflowsIr = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(extensionWorkflows)) {
            extensionWorkflows.forEach(item -> {
                ParamExtractionWorkflow extensionWorkflow =
                    JsonUtils.objectToClassType(item, ParamExtractionWorkflow.class);
                if (extensionWorkflow != null) {
                    Map<String, Object> extensionWorkflowIr = adaptProcessingWorkflow(extensionWorkflow, wmService);
                    extensionWorkflowIr.put(IRUtils.adaptKey(EXECUTION_TIMING), extensionWorkflow.getExecutionTiming());
                    extensionWorkflowsIr.add(extensionWorkflowIr);
                }
            });
        }
        configs.put(IRUtils.adaptKey(EXTENSION_WORKFLOWS), extensionWorkflowsIr);

        // 异常配置
        if (nodeConfigs.get(EXCEPTION_CONFIG) != null) {
            ParamExtractionExceptionConfig exceptionConfig =
                JsonUtils.objectToClassType(nodeConfigs.get(EXCEPTION_CONFIG), ParamExtractionExceptionConfig.class);
            Map<String, Object> exceptionConfigIr = new HashMap<>();
            if (exceptionConfig != null) {
                exceptionConfigIr.put(IRUtils.adaptKey(MAX_ITERATION), exceptionConfig.getMaxIteration());
                if (exceptionConfig.getExceptionCondition() != null) {
                    exceptionConfigIr.put(IRUtils.adaptKey(EXCEPTION_CONDITION),
                        adaptCondition(exceptionConfig.getExceptionCondition()));
                }
            }
            configs.put(IRUtils.adaptKey(EXCEPTION_CONFIG), exceptionConfigIr);
        }

        return configs;
    }

    private Map<String, Object> adaptProcessingWorkflow(ParamExtractionWorkflow processingWorkflow,
        WorkflowManagementService wmService) {
        Map<String, Object> processingWorkflowIr = new HashMap<>();
        processingWorkflowIr.put(ID, processingWorkflow.getId());
        processingWorkflowIr.put(PATH, wmService.getWorkflowObsPath(processingWorkflow.getId(),
            CommonConstant.Workflow.IR, processingWorkflow.getVersionId()));
        Map<String, Object> inputsIr = new HashMap<>();
        processingWorkflow.getInputs().forEach(workflowField -> {
            inputsIr.put(workflowField.getName(), adaptFieldValue(workflowField, NodeType.PARAM_EXTRACTION, false));
        });
        processingWorkflowIr.put(INPUTS, inputsIr);
        if (processingWorkflow.getEntryCondition() != null) {
            processingWorkflowIr.put(IRUtils.adaptKey(ENTRY_CONDITION),
                adaptCondition(processingWorkflow.getEntryCondition()));
        }
        return processingWorkflowIr;
    }

    private String adaptCondition(Object conditionObj) {
        WorkflowBranchConfigVO condition = JsonUtils.objectToClassType(conditionObj, WorkflowBranchConfigVO.class);
        if (condition != null) {
            return new com.openjiuwen.studio.agent.manager.workflow.jiuwen.adapt.BranchNodeAdapter().adaptExpression(condition);
        } else {
            return StringUtils.EMPTY;
        }
    }

    @Override
    public String getNodeType() {
        return NodeType.PARAM_EXTRACTION.getIrType();
    }
}
