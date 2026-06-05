/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.nodes;

import static com.openjiuwen.studio.agent.manager.constant.CommonConstant.MEMORY;
import static com.openjiuwen.studio.agent.manager.constant.CommonConstant.NAME;
import static com.openjiuwen.studio.agent.manager.constant.CommonConstant.STREAM;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.enums.TypeEnum;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.UuidUtils;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.dto.ParamExtractionDomainObject;
import com.openjiuwen.studio.agent.manager.dto.ParamExtractionExceptionConfig;
import com.openjiuwen.studio.agent.manager.dto.ParamExtractionWorkflow;
import com.openjiuwen.studio.agent.manager.dto.WorkflowBranchConfigVO;
import com.openjiuwen.studio.agent.manager.dto.WorkflowBranchConfigVOConditions;
import com.openjiuwen.studio.agent.manager.dto.WorkflowBranchVO;
import com.openjiuwen.studio.agent.manager.dto.WorkflowEdgeVO;
import com.openjiuwen.studio.agent.manager.dto.WorkflowFieldVO;
import com.openjiuwen.studio.agent.manager.dto.WorkflowFieldVOValue;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;
import com.openjiuwen.studio.agent.manager.dto.WorkflowVO;
import com.openjiuwen.studio.agent.manager.enums.EnumExecutionTiming;
import com.openjiuwen.studio.agent.manager.utils.JsonUtils;
import com.openjiuwen.studio.agent.manager.workflow.jiuwen.models.SetVariableConfig;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 参数抽取节点转换
 *
 */
@Slf4j
@Service
public class ParamExtractionNodeService {
    /**
     * 中间变量
     */
    private static final String INTERMEDIATE_LOOP_VAR = "intermediate_loop_var";

    /**
     * 中间变量prefix
     */
    private static final String GLOBAL_PREFIX = "intermediate_loop_var.";

    /**
     * 记忆变量前缀
     */
    private static final String MEMORY_FORMAT = "memory.%s";

    /**
     * 开始
     */
    private static final String INPUT = "input";

    /**
     * source
     */
    private static final String SOURCE = "source";

    /**
     * 值
     */
    private static final String VALUE = "value";

    /**
     * 记忆变量时长
     */
    private static final String AGING_LEVEL = "aging_level";

    /**
     * 会话级记忆时长
     */
    private static final String SESSION = "session";

    /**
     * 记忆变量存储方式
     */
    private static final String STORAGE_METHOD = "storage_method";

    /**
     * 记忆存储方式
     */
    private static final String ASSIGNMENT = "assignment";

    /**
     * 变量赋值节点配置
     */
    private static final String SETTINGS = "settings";

    /**
     * ID
     */
    private static final String ID = "id";

    /**
     * version ID
     */
    private static final String VERSION_ID = "version_id";

    /**
     * 输入引用
     */
    private static final String INPUTS = "inputs";

    /**
     * 领域对象
     */
    private static final String DOMAIN_OBJECTS = "domain_objects";

    /**
     * 领域对象前缀
     */
    private static final String DOMAIN_PREFIX = "domain_objects.";

    /**
     * 模型输出
     */
    private static final String MODEL_OUTPUT_VAR = "model_output_var";

    /**
     * 模型前綴
     */
    private static final String MODEL_PREFIX = "model_output_var.";

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
     * 最大迭代轮数
     */
    private static final String MAX_ITERATION = "max_iteration";

    /**
     * 拓展工作流
     */
    private static final String EXTENSION_WORKFLOWS = "extension_workflows";

    /**
     * 异常配置
     */
    private static final String EXCEPTION_CONFIG = "exception_config";

    /**
     * 跳出条件
     */
    private static final String BREAK_CONDITION = "break_condition";

    /**
     * 温度参数
     */
    private static final String TEMPERATURE = "temperature";

    /**
     * top p参数
     */
    private static final String TOP_P = "top_p";

    /**
     * 頻次
     */
    private static final String FREQUENCY_PENALTY = "frequency_penalty";

    /**
     * 历史轮数
     */
    private static final String HISTORY_SIZE = "history_size";

    /**
     * 回答格式
     */
    private static final String RESPONSE_FORMAT = "response_format";

    /**
     * 最大token数
     */
    private static final String MAX_TOKENS = "max_tokens";

    /**
     * format_instruction
     */
    private static final String FORMAT_INSTRUCTION = "format_instruction";

    /**
     * prompt
     */
    private static final String TEMPLATE_CONTENT = "template_content";

    /**
     * model
     */
    private static final String MODEL = "model";

    /**
     * SYSTEM_PROMPT
     */
    private static final String SYSTEM_PROMPT = "system_prompt";

    /**
     * SYSTEM_PROMPT
     */
    private static final String ENABLE_HISTORY = "enable_history";

    /**
     * 分支节点IF
     */
    private static final String IF = "if";

    /**
     * 分支节点else
     */
    private static final String DEFAULT = "default";

    /**
     * 进入条件
     */
    private static final String ENTRY_CONDITION = "entry_condition";

    /**
     * 开始节点
     */
    private static final String NODE_START = "node_start";

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 获取起始节点并处理其输出字段
     *
     * @param nodes 节点列表
     * @param start 起始节点类型
     * @return 包含输出字段的起始节点
     */
    public WorkflowNodeVO getStartNodeWithOutputs(List<WorkflowNodeVO> nodes, NodeType start) {
        // 获取start节点
        WorkflowNodeVO startNode =
            nodes.stream().filter(node -> Strings.CS.equals(start.getType(), node.getType())).findFirst().orElse(null);

        if (startNode == null) {
            throw new AgentStudioException(StudioError.UNEXPECTED_ERROR);
        }

        return startNode;
    }

    /**
     * 参数提取节点转换
     *
     * @param originWorkflow 工作流原始对象
     * @return 修改后的工作流对象
     */
    public WorkflowVO adaptParamExtractionNode(WorkflowVO originWorkflow) {
        List<WorkflowNodeVO> paramExtractionNodes = new ArrayList<>();
        originWorkflow.getNodes().forEach(node -> {
            if (NodeType.PARAM_EXTRACTION.getType().equals(node.getType())) {
                paramExtractionNodes.add(node);
            }
        });

        // 如不存在参数提取节点，不处理返回
        if (paramExtractionNodes.isEmpty()) {
            return originWorkflow;
        }
        // 深拷贝一个对象，对对象进行修改
        WorkflowVO workflow = JsonUtils.json2ObjQuietly(JsonUtils.toJson(originWorkflow), WorkflowVO.class);
        if (workflow == null) {
            return originWorkflow;
        }

        for (WorkflowNodeVO paramExtractionNode : paramExtractionNodes) {
            // 修改全局记忆变量，替换所有参数提取节点引用输出
            modifyGlobalVariables(workflow, paramExtractionNode);

            // 拆分节点，并处理节点内部连接关系
            NodePartitionParam nodePartitionParam = partitionParamExtractionNode(paramExtractionNode);

            // 处理原有节点跟边，修正边连接关系
            modifyOriginConnections(workflow, paramExtractionNode, nodePartitionParam);
        }
        return workflow;
    }

    /**
     * 修改全局记忆变量
     *
     * @param workflow 工作流原始对象
     * @param paramExtractionNode 参数提取节点
     */
    private void modifyGlobalVariables(WorkflowVO workflow, WorkflowNodeVO paramExtractionNode) {
        // 输出节点为空说明未定义中间变量，无需新增全局变量
        if (CollectionUtils.isEmpty(paramExtractionNode.getOutputs())) {
            return;
        }
        // 调用公共方法获取start节点并处理outputs
        WorkflowNodeVO start = getStartNodeWithOutputs(workflow.getNodes(), NodeType.START);

        // 找到已有记忆变量添加
        List<Object> adaptedSchema = new ArrayList<>();
        WorkflowFieldVO memoryField = null;
        for (WorkflowFieldVO startField : start.getOutputs()) {
            if (Strings.CS.equals(startField.getName(), MEMORY)
                && WorkflowFieldVO.SourceEnum.PRE_DEFINED.equals(startField.getSource())) {
                List<Object> schemaList = JsonUtils.objectToClass(startField.getSchema());
                if (!CollectionUtils.isEmpty(schemaList)) {
                    memoryField = startField;
                    adaptedSchema.addAll(schemaList);
                    break;
                }
            }
        }
        Set<String> existGlobalVariables = filterExistGlobalVariables(memoryField);

        // 提取节点的上下文和领域对象，添加到全局变量
        extractInputGlobalVars(paramExtractionNode, existGlobalVariables, adaptedSchema);

        // 如开始节点无记忆变量，新增记忆变量
        if (memoryField == null) {
            // 创建memory
            WorkflowFieldVOValue value = new WorkflowFieldVOValue().setType(WorkflowFieldVOValue.TypeEnum.GENERATED)
                .setContent(StringUtils.EMPTY)
                .setHint(StringUtils.EMPTY);
            memoryField = new WorkflowFieldVO().setName(MEMORY)
                .setType(TypeEnum.OBJECT.toString())
                .setRequired(true)
                .setFieldType(INPUT)
                .setReflection(false)
                .setSchema(adaptedSchema)
                .setSource(WorkflowFieldVO.SourceEnum.PRE_DEFINED)
                .setValue(value);
            start.getOutputs().add(memoryField);
        } else {
            // 融合新老记忆变量
            List<Object> originSchema = JsonUtils.objectToClass(memoryField.getSchema());
            if (originSchema != null) {
                originSchema.addAll(adaptedSchema);
            }
            memoryField.setSchema(originSchema);
        }
        // 记忆变量类型统一刷新
        adaptFieldSource(memoryField, WorkflowFieldVO.SourceEnum.PRE_DEFINED,
            new WorkflowFieldVOValue().setType(WorkflowFieldVOValue.TypeEnum.GENERATED));

        // 处理节点引用
        adaptGlobalVariablesReference(workflow, paramExtractionNode);
    }

    private void extractInputGlobalVars(WorkflowNodeVO paramExtractionNode, Set<String> existGlobalVariables,
        List<Object> adaptedSchema) {
        for (WorkflowFieldVO field : paramExtractionNode.getInputs()) {
            // 只有INTERMEDIATE_LOOP_VAR和DOMAIN_OBJECTS且为PRE_DEFINED才是全局变量
            if (!WorkflowFieldVO.SourceEnum.PRE_DEFINED.equals(field.getSource())
                || (!Strings.CS.equals(field.getName(), INTERMEDIATE_LOOP_VAR)
                && !Strings.CS.equals(field.getName(), DOMAIN_OBJECTS))) {
                continue;
            }
            List<Object> schemaList = JsonUtils.objectToClass(field.getSchema());
            if (CollectionUtils.isEmpty(schemaList)) {
                continue;
            }
            for (Object schemaObj : schemaList) {
                Map<String, Object> schemaMap = JsonUtils.objectToClass(schemaObj);

                // 重名不重复添加
                if (schemaMap == null || existGlobalVariables.contains(schemaMap.get(NAME).toString())) {
                    continue;
                }
                schemaMap.put(AGING_LEVEL, SESSION);
                schemaMap.put(STORAGE_METHOD, ASSIGNMENT);

                // 参数新增，避免重复提取
                existGlobalVariables.add(schemaMap.get(NAME).toString());
                adaptedSchema.add(schemaMap);
            }
        }
    }

    private Set<String> filterExistGlobalVariables(WorkflowFieldVO memoryField) {
        Set<String> existVariables = new HashSet<>();
        if (memoryField != null) {
            List<Object> schemaList = JsonUtils.objectToClass(memoryField.getSchema());
            if (CollectionUtils.isNotEmpty(schemaList)) {
                for (Object schemaObj : schemaList) {
                    Map<String, Object> schemaMap = JsonUtils.objectToClass(schemaObj);
                    if (schemaMap == null) {
                        continue;
                    }
                    existVariables.add(schemaMap.get(NAME).toString());
                }
            }
        }
        return existVariables;
    }

    /**
     * 处理节点引用
     *
     * @param workflow 待处理工作流
     * @param paramExtractionNode 参数提取工作流
     */
    private void adaptGlobalVariablesReference(WorkflowVO workflow, WorkflowNodeVO paramExtractionNode) {
        // 引用类型变量，如引用参数提取节点输出，替换为全局变量输出
        workflow.getNodes().forEach(node -> {
            // 替换输入输出
            if (CollectionUtils.isNotEmpty(node.getInputs())) {
                node.getInputs()
                    .forEach(workflowField -> adaptWorkflowFieldReference(workflowField, paramExtractionNode, null, false));
            }
            if (CollectionUtils.isNotEmpty(node.getOutputs())) {
                node.getOutputs()
                    .forEach(workflowField -> adaptWorkflowFieldReference(workflowField, paramExtractionNode, null, false));
            }
            // 特殊节点进行替换条件
            if (NodeType.BRANCH.getType().equals(node.getType())) {
                node.getBranches().forEach(workflowBranch -> {
                    WorkflowBranchConfigVO workflowBranchConfig =
                        JsonUtils.objectToClassType(workflowBranch.getConfigs(), WorkflowBranchConfigVO.class);
                    if (workflowBranchConfig != null) {
                        adaptWorkflowBranchConfig(workflowBranchConfig, paramExtractionNode, null);
                        workflowBranch.getConfigs().put("conditions", workflowBranchConfig.getConditions());
                    }
                });
            } else if (NodeType.LOOP.getType().equals(node.getType())) {
                if (node.getConfigs().get(BREAK_CONDITION) != null) {
                    WorkflowBranchConfigVO breakCondition = JsonUtils
                        .objectToClassType(node.getConfigs().get(BREAK_CONDITION), WorkflowBranchConfigVO.class);
                    if (breakCondition != null) {
                        adaptWorkflowBranchConfig(breakCondition, paramExtractionNode, null);
                        node.getConfigs().put(BREAK_CONDITION, breakCondition);
                    }
                }
            } else if (NodeType.SET_VARIABLE.getType().equals(node.getType())) {
                if (node.getConfigs().get(SETTINGS) != null) {
                    List<SetVariableConfig> variables =
                        JsonUtils.objectToClassRef(node.getConfigs().get(SETTINGS), new TypeReference<>() {});
                    if (CollectionUtils.isNotEmpty(variables)) {
                        variables.forEach(variable -> {
                            adaptWorkflowFieldReference(variable.getLeft(), paramExtractionNode, null, false);
                            adaptWorkflowFieldReference(variable.getRight(), paramExtractionNode, null, false);
                        });
                        node.getConfigs().put(SETTINGS, variables);
                    }
                }
            }
        });
    }

    /**
     * 处理分支引用，替换上下文变量
     *
     * @param workflowBranchConfig 分支配置
     * @param paramExtractionNode 参数提取节点
     */
    private void adaptWorkflowBranchConfig(WorkflowBranchConfigVO workflowBranchConfig,
        WorkflowNodeVO paramExtractionNode, NodePartitionParam param) {
        if (CollectionUtils.isNotEmpty(workflowBranchConfig.getConditions())) {
            workflowBranchConfig.getConditions().forEach(condition -> {
                adaptWorkflowFieldReference(condition.getLeft(), paramExtractionNode, param, false);
                adaptWorkflowFieldReference(condition.getRight(), paramExtractionNode, param, false);
            });
        }
    }

    /**
     * 处理工作流输入输出引用，替换上下文变量
     *
     * @param workflowField 工作流参数
     * @param paramExtractionNode 参数提取节点
     * @param param 是否为null代表内部节点，外部节点当前仅需要转换全局变量，内部节点需额外转换大模型节点输出以及输入变量
     * @param isGenerateWorkflow 是否用于生成工作流，针对生成工作流的引用对象节点输入需做特殊处理
     */
    private void adaptWorkflowFieldReference(WorkflowFieldVO workflowField, WorkflowNodeVO paramExtractionNode,
        NodePartitionParam param, boolean isGenerateWorkflow) {
        if (workflowField == null) {
            return;
        }
        if (Strings.CS.equals(workflowField.getType(), TypeEnum.ARRAY.toString())) {
            WorkflowFieldVO schemaField = JsonUtils.objectToClassType(workflowField.getSchema(), WorkflowFieldVO.class);
            if (schemaField != null) {
                adaptWorkflowFieldReference(schemaField, paramExtractionNode, param, isGenerateWorkflow);
                workflowField.setSchema(schemaField);
            }
        } else if (Strings.CS.equals(workflowField.getType(), TypeEnum.OBJECT.toString())) {
            List<Object> adaptedSchema = new ArrayList<>();
            List<Object> schemaList = JsonUtils.objectToClass(workflowField.getSchema());
            if (CollectionUtils.isNotEmpty(schemaList)) {
                for (Object schemaObj : schemaList) {
                    WorkflowFieldVO schemaField = JsonUtils.objectToClassType(schemaObj, WorkflowFieldVO.class);
                    if (schemaField != null) {
                        adaptWorkflowFieldReference(schemaField, paramExtractionNode, param, isGenerateWorkflow);
                        adaptedSchema.add(schemaField);
                    }
                }
                workflowField.setSchema(adaptedSchema);
            }
        }

        // 引用类型变量，如引用参数提取节点输出，替换为全局变量输出
        Map<String, WorkflowFieldVO> fieldVOMap = paramExtractionNode.getInputs()
            .stream()
            .collect(Collectors.toMap(WorkflowFieldVO::getName, Function.identity()));
        WorkflowFieldVOValue value = workflowField.getValue();
        if (value != null && WorkflowFieldVOValue.TypeEnum.REF.equals(value.getType())) {
            Map<String, String> refMap = JsonUtils.objectToClassRef(value.getContent(), new TypeReference<>() {});
            if (refMap == null
                || !Strings.CS.equals(paramExtractionNode.getId(), refMap.get(CommonConstant.Workflow.REF_NODE_ID))) {
                return;
            }
            adaptRefMap(refMap, param, fieldVOMap, isGenerateWorkflow);
            value.setContent(refMap);
        }
    }

    private void adaptRefMap(Map<String, String> refMap, NodePartitionParam param,
        Map<String, WorkflowFieldVO> fieldVOMap, boolean isGenerateWorkflow) {
        String refVarName = refMap.get(CommonConstant.Workflow.REF_VAR_NAME);

        // 外部节点引用的参数提取节点输出都为全局变量，内部节点按输入类型区分
        if (param == null) {
            refMap.put(CommonConstant.Workflow.REF_NODE_ID, NODE_START);
            refMap.put(SOURCE, WorkflowFieldVO.SourceEnum.PRE_DEFINED.toString());
            refMap.put(CommonConstant.Workflow.REF_VAR_NAME, String.format(MEMORY_FORMAT, refVarName));
        } else {
            if (Strings.CS.startsWith(refVarName, GLOBAL_PREFIX)) {
                // 内部节点引用上下文变量转换为全局变量
                refMap.put(CommonConstant.Workflow.REF_NODE_ID, NODE_START);
                refMap.put(SOURCE, WorkflowFieldVO.SourceEnum.PRE_DEFINED.toString());
                refMap.put(CommonConstant.Workflow.REF_VAR_NAME,
                    String.format(MEMORY_FORMAT, StringUtils.substringAfter(refVarName, GLOBAL_PREFIX)));
            } else if (Strings.CS.startsWith(refVarName, DOMAIN_PREFIX)) {
                // 内部节点引用领域对象转换为记忆变量
                refMap.put(CommonConstant.Workflow.REF_NODE_ID, NODE_START);
                refMap.put(SOURCE, WorkflowFieldVO.SourceEnum.PRE_DEFINED.toString());
                refMap.put(CommonConstant.Workflow.REF_VAR_NAME,
                    String.format(MEMORY_FORMAT, StringUtils.substringAfter(refVarName, DOMAIN_PREFIX)));
            } else if (Strings.CS.startsWith(refVarName, MODEL_PREFIX)) {
                // 内部节点引用模型参数转换为大模型节点输出
                refMap.put(CommonConstant.Workflow.REF_NODE_ID, param.getModelNode().getId());
                refMap.put(SOURCE, WorkflowFieldVO.SourceEnum.USER.toString());
                refMap.put(CommonConstant.Workflow.REF_VAR_NAME, StringUtils.substringAfterLast(refVarName, "."));
            } else {
                // 内部节点输入转换
                if (fieldVOMap.containsKey(refVarName) && isGenerateWorkflow
                    && WorkflowFieldVOValue.TypeEnum.REF.equals(fieldVOMap.get(refVarName).getValue().getType())) {
                    // 针对工作流引用对象提取节点输入参数，转换为原始引用
                    Map<String, String> refMapOriginal = objectMapper.convertValue(
                        fieldVOMap.get(refVarName).getValue().getContent(), new TypeReference<>() { });
                    refMap.putAll(refMapOriginal);
                } else {
                    // 内部节点输入转换为输入节点输出
                    refMap.put(CommonConstant.Workflow.REF_NODE_ID, param.getStartNode().getId());
                    refMap.put(SOURCE, WorkflowFieldVO.SourceEnum.USER.toString());
                    refMap.put(CommonConstant.Workflow.REF_VAR_NAME, refVarName);
                }
            }
        }
    }

    /**
     * 拆分参数提取节点
     *
     * @param paramExtractionNode 待拆分节点
     * @return 拆分后的结果
     */
    private NodePartitionParam partitionParamExtractionNode(WorkflowNodeVO paramExtractionNode) {
        NodePartitionParam nodePartitionParam = NodePartitionParam.builder()
            .nodes(new ArrayList<>())
            .edges(new ArrayList<>())
            .elseBranchId(null)
            .exceptionBranchIds(new ArrayList<>())
            .loopNodeId(null)
            .build();

        // 生成起始节点
        generateStartNode(paramExtractionNode, nodePartitionParam);

        // 生成变量赋值节点
        generateSetVariableNode(paramExtractionNode, nodePartitionParam);

        // 解析拓展工作流
        List<ParamExtractionWorkflow> extensionWorkflows = new ArrayList<>();
        List<Object> extensionWorkflowObj =
            JsonUtils.objectToClass(paramExtractionNode.getConfigs().get(EXTENSION_WORKFLOWS));
        if (CollectionUtils.isNotEmpty(extensionWorkflowObj)) {
            extensionWorkflowObj.forEach(item -> {
                ParamExtractionWorkflow extensionWorkflow =
                    JsonUtils.objectToClassType(item, ParamExtractionWorkflow.class);
                if (extensionWorkflow != null) {
                    extensionWorkflows.add(extensionWorkflow);
                }
            });
        }
        // 预处理工作流
        generateExtensionWorkflows(paramExtractionNode, nodePartitionParam, extensionWorkflows,
            EnumExecutionTiming.BEFORE_ENTRY, null);

        // 生成大模型开始节点
        generateLoopStartNode(paramExtractionNode, nodePartitionParam);
        List<WorkflowFieldVO> outputs = new ArrayList<>();
        for (WorkflowFieldVO field : paramExtractionNode.getInputs()) {
            if (Strings.CS.equals(field.getName(), MODEL_OUTPUT_VAR)) {
                outputs = handleModelOutput(field);
            }
        }
        // 大模型提参工作流
        if (CollectionUtils.isNotEmpty(outputs)) {
            generateModelExtraction(paramExtractionNode, nodePartitionParam, outputs);
        }

        // 提参后处理工作流
        generateExtensionWorkflows(paramExtractionNode, nodePartitionParam, extensionWorkflows,
            EnumExecutionTiming.AFTER_EXTRACTION, null);

        // 领域对象后处理工作流
        generateDomainObjectExtraction(paramExtractionNode, nodePartitionParam);

        // 退出前处理
        generateExtensionWorkflows(paramExtractionNode, nodePartitionParam, extensionWorkflows,
            EnumExecutionTiming.AFTER_QUIT, null);

        // 跳出条件节点
        generateBreakNode(paramExtractionNode, nodePartitionParam);

        // 生成结束节点
        generateEndNode(paramExtractionNode, nodePartitionParam);

        return nodePartitionParam;
    }

    /**
     * 生成变量赋值节点
     * @param paramExtractionNode 参数提取节点
     * @param nodePartitionParam 拆分出的节点列表
     */
    private void generateSetVariableNode(WorkflowNodeVO paramExtractionNode, NodePartitionParam nodePartitionParam) {
        // 过滤所有输入参数
        List<WorkflowFieldVO> inputs = paramExtractionNode.getInputs()
            .stream()
            .filter(field -> !WorkflowFieldVO.SourceEnum.PRE_DEFINED.equals(field.getSource()))
            .toList();

        //
        for (WorkflowFieldVO field : paramExtractionNode.getInputs()) {
            // 针对设置初始值的上下文变量生成变量赋值节点
            if (!WorkflowFieldVO.SourceEnum.PRE_DEFINED.equals(field.getSource()) || !Strings.CS.equals(
                field.getName(), INTERMEDIATE_LOOP_VAR)) {
                continue;
            }

            List<WorkflowFieldVO> schemaList = JsonUtils.objectToClassRef(field.getSchema(), new TypeReference<>() { });
            if (CollectionUtils.isEmpty(schemaList)) {
                continue;
            }

            // 过滤有初始值上下文变量，ref和literal
            List<WorkflowFieldVO> needHandlerFieldVOList = schemaList.stream()
                .filter(schema -> schema.getValue().getType().equals(WorkflowFieldVOValue.TypeEnum.LITERAL)
                    || schema.getValue().getType().equals(WorkflowFieldVOValue.TypeEnum.REF))
                .toList();
            if (CollectionUtils.isEmpty(needHandlerFieldVOList)) {
                break;
            }

            // 生成变量赋值节点
            String nodeId = generateParamExtractionNodeId(paramExtractionNode.getId());
            WorkflowNodeVO setVariableNode = new WorkflowNodeVO().setId(nodeId)
                .setName(nodeId)
                .setConfigs(generateSetVariableConfigs(needHandlerFieldVOList))
                .setType(NodeType.SET_VARIABLE.getType())
                .setInputs(needHandlerFieldVOList);

            connectNodes(nodePartitionParam, setVariableNode);
            nodePartitionParam.getNodes().add(setVariableNode);
            break;
        }
    }

    /**
     * 生成变量赋值节点config配置和input配置
     * @param needHandlerFieldVOList 存在需要处理的上下文初始值
     * @return 变量赋值节点config配置
     */
    private Map<String, Object> generateSetVariableConfigs(List<WorkflowFieldVO> needHandlerFieldVOList) {
        Map<String, Object> configs = new HashMap<>();
        List<Object> settings = new ArrayList<>();
        needHandlerFieldVOList.forEach(field -> {
            WorkflowBranchConfigVOConditions workflowBranchConfigVOConditions = new WorkflowBranchConfigVOConditions();
            WorkflowFieldVO fieldVORight = new WorkflowFieldVO().setType(field.getType())
                .setValue(JsonUtils.json2ObjQuietly(JsonUtils.toJson(field.getValue()), WorkflowFieldVOValue.class))
                .setSchema(field.getSchema());
            workflowBranchConfigVOConditions.setRight(fieldVORight);

            WorkflowFieldVO fieldVOLeft = JsonUtils.json2ObjQuietly(JsonUtils.toJson(fieldVORight),
                WorkflowFieldVO.class);

            field.getValue().setType(WorkflowFieldVOValue.TypeEnum.REF);
            Objects.requireNonNull(fieldVOLeft).getValue().setType(WorkflowFieldVOValue.TypeEnum.REF);
            Map<String, String> contentMap = new HashMap<>();
            contentMap.put(CommonConstant.Workflow.REF_NODE_ID, NODE_START);
            contentMap.put(CommonConstant.Workflow.REF_VAR_NAME, String.format(MEMORY_FORMAT, field.getName()));
            contentMap.put("source", WorkflowFieldVO.SourceEnum.PRE_DEFINED.toString());
            field.getValue().setContent(contentMap);
            fieldVOLeft.getValue().setContent(contentMap);

            workflowBranchConfigVOConditions.setLeft(fieldVOLeft);

            field.setSource(WorkflowFieldVO.SourceEnum.USER);
            settings.add(workflowBranchConfigVOConditions);
        });
        configs.put("settings", settings);
        return configs;
    }

    /**
     * 领域对象后处理工作流
     *
     * @param paramExtractionNode 待拆分节点
     * @param param 参数拆分时传参
     */
    private void generateDomainObjectExtraction(WorkflowNodeVO paramExtractionNode, NodePartitionParam param) {
        // 领域对象相关配置
        List<Object> domainObjects = JsonUtils.objectToClass(paramExtractionNode.getConfigs().get(DOMAIN_OBJECTS));
        if (CollectionUtils.isNotEmpty(domainObjects)) {
            domainObjects.forEach(item -> {
                ParamExtractionDomainObject domainObject =
                    JsonUtils.objectToClassType(item, ParamExtractionDomainObject.class);
                if (domainObject != null) {
                    generateExtensionWorkflows(paramExtractionNode, param, domainObject.getProcessingWorkflows(), null,
                        domainObject.getName());
                }
            });
        }
    }

    private void generateStartNode(WorkflowNodeVO paramExtractionNode, NodePartitionParam param) {
        String nodeId = generateParamExtractionNodeId(paramExtractionNode.getId());
        nodeId = String.format(ParamExtractionIdFormatEnum.START.toString(), nodeId);

        // 过滤所有输入参数
        List<WorkflowFieldVO> inputs = paramExtractionNode.getInputs()
            .stream()
            .filter(field -> !WorkflowFieldVO.SourceEnum.PRE_DEFINED.equals(field.getSource()))
            .toList();
        List<WorkflowFieldVO> outputs = new ArrayList<>();
        for (WorkflowFieldVO field : inputs) {
            WorkflowFieldVOValue value = new WorkflowFieldVOValue().setType(WorkflowFieldVOValue.TypeEnum.GENERATED);
            outputs.add(new WorkflowFieldVO().setName(field.getName())
                .setType(field.getType())
                .setFieldType(field.getFieldType())
                .setSource(field.getSource())
                .setDescription(field.getDescription())
                .setSchema(field.getSchema())
                .setRequired(field.isRequired())
                .setValue(value));
        }
        WorkflowNodeVO startNode = new WorkflowNodeVO().setId(nodeId)
            .setName(paramExtractionNode.getName())
            .setType(NodeType.PARAM_OUT.getType())
            .setConfigs(new HashMap<>())
            .setInputs(inputs)
            .setOutputs(outputs);

        // 标记开始节点
        param.setStartNode(startNode);
        param.getNodes().add(startNode);
    }

    /**
     * 生成意图跳出节点
     *
     * @param paramExtractionNode 待拆分节点
     * @param param 参数拆分时传参
     */
    private void generateBreakNode(WorkflowNodeVO paramExtractionNode, NodePartitionParam param) {
        Map<String, Object> nodeConfigs = paramExtractionNode.getConfigs();
        if (nodeConfigs.get(BREAK_CONDITION) != null) {
            Map<String, Object> condition = JsonUtils.objectToClass(nodeConfigs.get(BREAK_CONDITION));
            if (condition == null) {
                return;
            }
            WorkflowNodeVO branchNode = generateBranchNode(condition, paramExtractionNode, param);

            // 循环回连
            WorkflowEdgeVO edge =
                new WorkflowEdgeVO().setSource(branchNode.getId()).setTarget(param.getLoopNodeId()).setBranch(DEFAULT);
            param.getEdges().add(edge);

            // 新增上一个节点连接到此的边
            connectNodes(param, branchNode);
            connectElseBranch(param, branchNode);
            param.getNodes().add(branchNode);
        }
    }

    /**
     * 生成结束节点
     *
     * @param paramExtractionNode 待拆分节点
     * @param param 参数拆分时传参
     */
    private void generateEndNode(WorkflowNodeVO paramExtractionNode, NodePartitionParam param) {
        String nodeId = generateParamExtractionNodeId(paramExtractionNode.getId());
        nodeId = String.format(ParamExtractionIdFormatEnum.END.toString(), nodeId);
        WorkflowNodeVO endNode = new WorkflowNodeVO().setId(nodeId)
            .setName(nodeId)
            .setType(NodeType.PARAM_OUT.getType())
            .setConfigs(new HashMap<>())
            .setInputs(new ArrayList<>())
            .setOutputs(new ArrayList<>());
        connectNodes(param, endNode);
        connectElseBranch(param, endNode);
        param.setEndNode(endNode);
        param.getNodes().add(endNode);
    }

    /**
     * 生成异常结束节点
     *
     * @param paramExtractionNode 待拆分节点
     * @param param 参数拆分时传参
     */
    private void generateExceptionNode(WorkflowNodeVO paramExtractionNode, NodePartitionParam param) {
        String nodeId = generateParamExtractionNodeId(paramExtractionNode.getId());
        nodeId = String.format(ParamExtractionIdFormatEnum.EXCEPTION.toString(), nodeId);
        WorkflowNodeVO exceptionNode = new WorkflowNodeVO().setId(nodeId)
            .setName(nodeId)
            .setType(NodeType.PARAM_OUT.getType())
            .setConfigs(new HashMap<>())
            .setInputs(new ArrayList<>())
            .setOutputs(new ArrayList<>());
        param.setExceptionNode(exceptionNode);
        param.getNodes().add(exceptionNode);

        // 连接所有异常分支到节点
        param.getExceptionBranchIds()
            .forEach(exceptionNodeId -> param.getEdges()
                .add(new WorkflowEdgeVO().setSource(exceptionNodeId).setTarget(exceptionNode.getId()).setBranch(IF)));
    }

    /**
     * 生成循环开始节点
     *
     * @param paramExtractionNode 待拆分节点
     * @param param 参数拆分时传参
     */
    private void generateLoopStartNode(WorkflowNodeVO paramExtractionNode, NodePartitionParam param) {
        String nodeId = generateParamExtractionNodeId(paramExtractionNode.getId());
        nodeId = String.format(ParamExtractionIdFormatEnum.CYCLE_BEGIN.toString(), nodeId);
        WorkflowNodeVO loopStartNode = new WorkflowNodeVO().setId(nodeId)
            .setName(nodeId)
            .setType(NodeType.PARAM_OUT.getType())
            .setConfigs(new HashMap<>())
            .setInputs(new ArrayList<>())
            .setOutputs(new ArrayList<>());
        connectNodes(param, loopStartNode);
        connectElseBranch(param, loopStartNode);
        param.setLoopNodeId(nodeId);
        param.getNodes().add(loopStartNode);
    }

    /**
     * 生成参数提取节点
     *
     * @param paramExtractionNode 待拆分节点
     * @param param 参数拆分时传参
     * @param outputs 待提取参数
     */
    private void generateModelExtraction(WorkflowNodeVO paramExtractionNode, NodePartitionParam param,
        List<WorkflowFieldVO> outputs) {
        Map<String, Object> nodeConfigs = paramExtractionNode.getConfigs();
        if (nodeConfigs.get(ENTRY_CONDITION) != null) {
            Map<String, Object> condition = JsonUtils.objectToClass(nodeConfigs.get(ENTRY_CONDITION));
            if (condition == null) {
                return;
            }
            WorkflowNodeVO branchNode = generateBranchNode(condition, paramExtractionNode, param);

            // 新增上一个节点连接到此的边
            connectNodes(param, branchNode);
            param.getNodes().add(branchNode);
            param.setLoopNodeId(branchNode.getId());

            // 设定下一个else跳过分支
            param.setElseBranchId(branchNode.getId());
        }
        WorkflowNodeVO modelNode = generateModelNode(paramExtractionNode, param, outputs);

        connectNodes(param, modelNode);
        param.getNodes().add(modelNode);
        param.setModelNode(modelNode);
    }

    /**
     * 根据拓展工作流生成节点
     *
     * @param paramExtractionNode 待拆分节点
     * @param param 参数拆分时传参
     * @param extensionWorkflows 拓展工作流
     * @param executionTiming 执行时机
     * @param domainName 领域对象名称
     */
    private void generateExtensionWorkflows(WorkflowNodeVO paramExtractionNode, NodePartitionParam param,
        List<ParamExtractionWorkflow> extensionWorkflows, EnumExecutionTiming executionTiming, String domainName) {
        List<ParamExtractionWorkflow> handleWorkflows;
        if (executionTiming != null) {
            handleWorkflows = new ArrayList<>();
            extensionWorkflows.forEach(extensionWorkflow -> {
                if (executionTiming.toString().equals(extensionWorkflow.getExecutionTiming())) {
                    handleWorkflows.add(extensionWorkflow);
                }
            });
        } else {
            handleWorkflows = extensionWorkflows;
        }
        if (CollectionUtils.isEmpty(handleWorkflows)) {
            return;
        }
        handleWorkflows.forEach(handleWorkflow -> {
            boolean hasEntryCondition = handleWorkflow.getEntryCondition() != null;

            // 如配置进入条件，新增一个判断节点
            if (hasEntryCondition) {
                WorkflowNodeVO branchNode =
                    generateBranchNode(handleWorkflow.getEntryCondition(), paramExtractionNode, param);

                // 新增上一个节点连接到此的边
                connectNodes(param, branchNode);
                connectElseBranch(param, branchNode);
                param.getNodes().add(branchNode);

                // 设定下一个else跳过分支
                param.setElseBranchId(branchNode.getId());
            }
            WorkflowNodeVO workflowNode =
                generateWorkflowNode(handleWorkflow, paramExtractionNode, param, executionTiming, domainName);

            connectNodes(param, workflowNode);
            if (!hasEntryCondition) {
                connectElseBranch(param, workflowNode);
            }
            param.getNodes().add(workflowNode);

            // 异常配置
            if (paramExtractionNode.getConfigs().get(EXCEPTION_CONFIG) != null) {
                ParamExtractionExceptionConfig exceptionConfig = JsonUtils.objectToClassType(
                    paramExtractionNode.getConfigs().get(EXCEPTION_CONFIG), ParamExtractionExceptionConfig.class);
                if (exceptionConfig != null && exceptionConfig.getExceptionCondition() != null) {
                    WorkflowNodeVO exceptionBranchNode =
                        generateBranchNode(exceptionConfig.getExceptionCondition(), paramExtractionNode, param);

                    // 新增上一个节点连接到此的边，添加异常分支和ID
                    connectNodes(param, exceptionBranchNode);
                    param.getNodes().add(exceptionBranchNode);
                    param.getExceptionBranchIds().add(exceptionBranchNode.getId());
                }
            }
        });
    }

    private WorkflowNodeVO generateBranchNode(Map<String, Object> condition, WorkflowNodeVO paramExtractionNode,
        NodePartitionParam param) {
        String branchNodeId = generateParamExtractionNodeId(paramExtractionNode.getId());
        List<WorkflowBranchVO> branches = new ArrayList<>();
        branches.add(new WorkflowBranchVO().setId(IF).setConfigs(condition));
        branches.add(new WorkflowBranchVO().setId(DEFAULT));
        WorkflowNodeVO branchNode = new WorkflowNodeVO().setId(branchNodeId)
            .setName(branchNodeId)
            .setType(NodeType.BRANCH.getType())
            .setBranches(branches);
        branches.forEach(workflowBranch -> {
            WorkflowBranchConfigVO workflowBranchConfig =
                JsonUtils.objectToClassType(workflowBranch.getConfigs(), WorkflowBranchConfigVO.class);
            if (workflowBranchConfig != null) {
                adaptWorkflowBranchConfig(workflowBranchConfig, paramExtractionNode, param);
                workflowBranch.getConfigs().put("conditions", workflowBranchConfig.getConditions());
            }
        });
        return branchNode;
    }

    private WorkflowNodeVO generateModelNode(WorkflowNodeVO paramExtractionNode, NodePartitionParam param,
        List<WorkflowFieldVO> outputs) {
        Map<String, Object> nodeConfigs = paramExtractionNode.getConfigs();

        List<WorkflowFieldVO> inputs = param.getStartNode().getInputs();

        Map<String, Object> configs = new HashMap<>();
        configs.put(TOP_P, nodeConfigs.get(TOP_P));
        configs.put(TEMPERATURE, nodeConfigs.get(TEMPERATURE));
        configs.put(HISTORY_SIZE, nodeConfigs.get(HISTORY_SIZE));
        configs.put(FREQUENCY_PENALTY, nodeConfigs.get(FREQUENCY_PENALTY));
        configs.put(MAX_TOKENS, nodeConfigs.get(MAX_TOKENS));
        configs.put(TEMPLATE_CONTENT, nodeConfigs.get(PROMPT));
        configs.put(MODEL, nodeConfigs.get(MODEL));
        configs.put(FORMAT_INSTRUCTION, StringUtils.EMPTY);
        configs.put(SYSTEM_PROMPT, nodeConfigs.get(StringUtils.EMPTY));
        configs.put(RESPONSE_FORMAT, "json");
        configs.put(STREAM, false);
        configs.put(ENABLE_HISTORY, true);
        String nodeId = generateParamExtractionNodeId(paramExtractionNode.getId());
        nodeId = String.format(ParamExtractionIdFormatEnum.LLM.toString(), nodeId);
        return new WorkflowNodeVO().setId(nodeId)
            .setName(nodeId)
            .setType(NodeType.LLM.getType())
            .setInputs(inputs)
            .setOutputs(outputs)
            .setConfigs(configs);
    }

    private List<WorkflowFieldVO> handleModelOutput(WorkflowFieldVO workflowField) {
        List<WorkflowFieldVO> outputs = new ArrayList<>();
        List<Object> modelObjects = JsonUtils.objectToClass(workflowField.getSchema());
        if (CollectionUtils.isEmpty(modelObjects)) {
            return outputs;
        }
        for (Object modelObj : modelObjects) {
            WorkflowFieldVO modelField = JsonUtils.objectToClassType(modelObj, WorkflowFieldVO.class);
            if (modelField == null) {
                continue;
            }
            adaptFieldSource(modelField, WorkflowFieldVO.SourceEnum.USER,
                new WorkflowFieldVOValue().setType(WorkflowFieldVOValue.TypeEnum.GENERATED));
            outputs.add(modelField);
        }
        return outputs;
    }

    private WorkflowNodeVO generateWorkflowNode(ParamExtractionWorkflow workflow, WorkflowNodeVO paramExtractionNode,
        NodePartitionParam param, EnumExecutionTiming executionTiming, String domainName) {
        String workflowNodeId = generateParamExtractionNodeId(paramExtractionNode.getId());

        // 调测事件需新增根据模板区分拓展工作流或领域对象后处理工作流
        if (executionTiming != null) {
            switch (executionTiming) {
                case BEFORE_ENTRY ->
                    workflowNodeId = String.format(ParamExtractionIdFormatEnum.BEFORE_ENTRY.toString(), workflowNodeId);
                case AFTER_QUIT ->
                    workflowNodeId = String.format(ParamExtractionIdFormatEnum.AFTER_QUIT.toString(), workflowNodeId);
                case AFTER_EXTRACTION -> workflowNodeId =
                    String.format(ParamExtractionIdFormatEnum.AFTER_EXTRACTION.toString(), workflowNodeId);
                default -> log.error("unsupported type: {}", executionTiming);
            }
        } else {
            workflowNodeId =
                String.format(ParamExtractionIdFormatEnum.DOMAIN_OBJECTS.toString(), workflowNodeId, domainName);
        }
        workflow.getInputs().forEach(field -> adaptWorkflowFieldReference(field, paramExtractionNode, param, true));
        Map<String, Object> configs = new HashMap<>();
        configs.put(ID, workflow.getId());
        configs.put(VERSION_ID, workflow.getVersionId());
        return new WorkflowNodeVO().setId(workflowNodeId)
            .setName(workflowNodeId)
            .setType(NodeType.WORKFLOW.getType())
            .setInputs(workflow.getInputs())
            .setOutputs(workflow.getOutputs())
            .setConfigs(configs);
    }

    /**
     * 连接上一个节点到此节点
     *
     * @param param 参数拆分时传参
     * @param targetNode 目标节点
     */
    private void connectNodes(NodePartitionParam param, WorkflowNodeVO targetNode) {
        WorkflowNodeVO sourceNode = param.getNodes().get(param.getNodes().size() - 1);
        WorkflowEdgeVO edge = new WorkflowEdgeVO().setSource(sourceNode.getId()).setTarget(targetNode.getId());
        if (NodeType.BRANCH.getType().equals(sourceNode.getType())) {
            // 异常分支处理逻辑与跳出和进入逻辑相反，else分支为正常连接
            if (param.getExceptionBranchIds().contains(sourceNode.getId())) {
                edge.setBranch(DEFAULT);
            } else {
                edge.setBranch(IF);
            }
        }
        param.getEdges().add(edge);
    }

    /**
     * 连接else分支节点到此节点
     *
     * @param param 参数拆分时传参
     * @param targetNode 目标节点
     */
    private void connectElseBranch(NodePartitionParam param, WorkflowNodeVO targetNode) {
        if (StringUtils.isEmpty(param.getElseBranchId())) {
            return;
        }
        WorkflowEdgeVO edge =
            new WorkflowEdgeVO().setSource(param.getElseBranchId()).setTarget(targetNode.getId()).setBranch(DEFAULT);
        param.getEdges().add(edge);
        param.setElseBranchId(null);
    }

    /**
     * 处理原有节点跟边，修正边连接关系
     *
     * @param nodeId 原始节点ID
     */
    private String generateParamExtractionNodeId(String nodeId) {
        return String.format(ParamExtractionIdFormatEnum.COMPOSITE_FORMAT.toString(), nodeId, UuidUtils.getUUID());
    }

    /**
     * 此函数用于递归修改工作流参数的field source value
     *
     * @param workflowField 待修改字段
     * @param sourceEnum 需要修改的属性
     * @param value 需要修改的value
     */
    private void adaptFieldSource(WorkflowFieldVO workflowField, WorkflowFieldVO.SourceEnum sourceEnum,
        WorkflowFieldVOValue value) {
        if (Strings.CS.equals(workflowField.getType(), TypeEnum.ARRAY.toString())) {
            WorkflowFieldVO schemaField = JsonUtils.objectToClassType(workflowField.getSchema(), WorkflowFieldVO.class);
            if (schemaField != null) {
                adaptFieldSource(schemaField, sourceEnum, value);
                workflowField.setSchema(schemaField);
            }
        } else if (Strings.CS.equals(workflowField.getType(), TypeEnum.OBJECT.toString())) {
            List<Object> adaptedSchema = new ArrayList<>();
            List<Object> schemaList = JsonUtils.objectToClass(workflowField.getSchema());
            if (CollectionUtils.isNotEmpty(schemaList)) {
                for (Object schemaObj : schemaList) {
                    WorkflowFieldVO schemaField = JsonUtils.objectToClassType(schemaObj, WorkflowFieldVO.class);
                    if (schemaField != null) {
                        adaptFieldSource(schemaField, sourceEnum, value);
                        adaptedSchema.add(schemaField);
                    }
                }
                workflowField.setSchema(adaptedSchema);
            }
        }
        if (sourceEnum != null) {
            workflowField.setSource(sourceEnum);
        }
        if (value != null) {
            workflowField.setValue(value);
        }
    }

    /**
     * 处理原有节点跟边，修正边连接关系
     *
     * @param workflow 工作流原始对象
     * @param paramExtractionNode 待拆分节点
     * @param param 过程传参
     */
    private void modifyOriginConnections(WorkflowVO workflow, WorkflowNodeVO paramExtractionNode,
        NodePartitionParam param) {
        List<WorkflowNodeVO> nodes = new ArrayList<>();
        List<WorkflowEdgeVO> edges = new ArrayList<>();
        for (WorkflowEdgeVO edge : workflow.getEdges()) {
            // 目标节点直接连接，修改target为起始节点ID
            if (Strings.CS.equals(edge.getTarget(), paramExtractionNode.getId())) {
                edge.setTarget(param.getStartNode().getId());
                edges.add(edge);
                continue;
            }
            // 起始节点需分情况处理
            if (Strings.CS.equals(edge.getSource(), paramExtractionNode.getId())) {
                // 正常出去的边修改ID，异常节点的边连接到异常结束节点
                if (!Boolean.TRUE.equals(edge.isExceptionBranch())) {
                    edge.setSource(param.getEndNode().getId());
                    edges.add(edge);
                    continue;
                }
                // 异常边使用异常节点连接
                if (param.getExceptionNode() == null) {
                    generateExceptionNode(paramExtractionNode, param);
                }
                edge.setExceptionBranch(false);
                edge.setSource(param.getExceptionNode().getId());
                edges.add(edge);
                continue;
            }
            edges.add(edge);
        }
        for (WorkflowNodeVO node : workflow.getNodes()) {
            if (Strings.CS.equals(node.getId(), paramExtractionNode.getId())) {
                continue;
            }
            nodes.add(node);
        }
        edges.addAll(param.getEdges());
        nodes.addAll(param.getNodes());
        workflow.setNodes(nodes);
        workflow.setEdges(edges);
    }
}
