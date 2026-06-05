/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.jiuwen.adapt;

import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.enums.TypeEnum;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.common.utils.SpringBeanUtils;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.dto.ExceptionProcess;
import com.openjiuwen.studio.agent.manager.dto.WorkFlowNodeMemoryIR;
import com.openjiuwen.studio.agent.manager.dto.WorkFlowNodeUserProfileIR;
import com.openjiuwen.studio.agent.manager.dto.WorkflowFieldVO;
import com.openjiuwen.studio.agent.manager.dto.WorkflowFieldVOMemory;
import com.openjiuwen.studio.agent.manager.dto.WorkflowFieldVOValue;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;
import com.openjiuwen.studio.agent.manager.service.IrAdapterService;
import com.openjiuwen.studio.agent.manager.service.WorkflowManagementService;
import com.openjiuwen.studio.agent.manager.service.md.ModelServiceManager;
import com.openjiuwen.studio.agent.manager.utils.IRUtils;
import com.openjiuwen.studio.agent.manager.utils.JsonUtils;
import com.openjiuwen.studio.agent.manager.utils.WorkflowUtils;
import com.openjiuwen.studio.agent.manager.workflow.models.JiuwenFields;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * 工作流对象转换IR
 *
 */
@Slf4j
public abstract class AbstractIRNodeAdapter implements Adapter {
    /**
     * ir预定义变量字段
     */
    private static final String PRE_DEFINED_FIELDS = "preDefinedFields";

    /**
     * ir系统变量字段
     */
    private static final String SYSTEM_FIELDS = "systemFields";

    /**
     * ir环境变量字段
     */
    private static final String ENVIRONMENT_FIELDS = "environmentFields";

    /**
     * ir用户变量字段
     */
    private static final String USER_FIELDS = "userFields";

    /**
     * 模型字段名
     */
    private static final String MODEL = "model";

    /**
     * 模型部署id
     */
    private static final String MODEL_DEPLOYMENT_ID = "model_deployment_id";

    /**
     * 大模型节点安全护栏开关配置
     */
    private static final String SAFETY_BARRIER = "safety_barrier";

    /**
     * 模型名称
     */
    private static final String MODEL_NAME = "model_name";

    /**
     * 温度参数
     */
    private static final String TEMPERATURE = "temperature";

    /**
     * top p参数
     */
    private static final String TOP_P = "top_p";

    /**
     * 模型类型
     */
    private static final String MODEL_TYPE = "model_type";

    /**
     * 模型超参
     */
    private static final String HYPER_PARAMETERS = "hyper_parameters";

    /**
     * id
     */
    private static final String ID = "id";

    /**
     * 类型
     */
    private static final String TYPE = "type";

    /**
     * 实际类型
     */
    private static final String ACTUAL_TYPE = "actualType";

    /**
     * 是否必须
     */
    private static final String REQUIRED = "required";

    /**
     * 描述
     */
    private static final String DESCRIPTION = "description";

    /**
     * schema
     */
    private static final String SCHEMA = "schema";

    /**
     * 中文名称
     */
    private static final String CN_NAME = "cn_name";

    /**
     * 值
     */
    private static final String VALUE = "value";

    /**
     * 输入参数
     */
    private static final String INPUTS = "inputs";

    /**
     * 输出参数
     */
    private static final String OUTPUTS = "outputs";

    /**
     * 名称
     */
    private static final String NAME = "name";

    /**
     * 源类型
     */
    private static final String SOURCE_TYPE = "sourceType";

    /**
     * 默认值
     */
    private static final String DEFAULT_VALUE = "default_value";

    /**
     * 最大深度
     */
    private static final int MAX_DEPTH = 5;

    /**
     * 记忆变量时长
     */
    private static final String AGING_LEVEL = "aging_level";

    /**
     * 记忆变量存储方式
     */
    private static final String STORAGE_METHOD = "storage_method";

    /**
     * array类型模板
     */
    private static final String ARRAY_PATTERN = "array<%s>";

    /**
     * 异常处理节点
     */
    private static final String EXCEPTION_PROCESS = "exception_process";

    /**
     * 异常处理节点超时最短时间
     */
    private static final double EXCEPTION_MIN_TIMEOUT = 0.1;

    /**
     * 异常处理节点超时最长时间
     */
    private static final double EXCEPTION_MAX_TIMEOUT = 900;

    /**
     * 结构化输出的内容，可按照模板进行变量填充
     */
    private static final String STRUCT_OUTPUT_TEMPLATE = "struct_output_template";

    /**
     * 是否写入历史
     */
    private static final String ENABLE_HISTORY = "enable_history";

    /**
     * 结构化输出是否生效
     */
    private static final String IS_STRUCT_MESSAGE = "isStructMessage";

    /**
     * 模型扩展参数
     */
    private static final String EXTENSION = "extension";

    /**
     *  大模型节点深度思考开关
     */
    private static final String ENABLE_THINKING = "enable_thinking";

    private static final String ENABLE_THINKING_ENABLED = "enabled";

    private static final String ENABLE_THINKING_DISABLED = "disabled";

    /**
     * 是否视觉模型
     */
    private static final String VL_ENABLE = "vl_enable";

    /**
     * 九问IR工作流节点中的记忆配置
     */
    protected static final String MEMORY = "memory";

    @Override
    public Map<String, Object> adapt(WorkflowNodeVO workflowNodeVo) {
        Map<String, Object> nodeIr = new HashMap<>();
        nodeIr.put(ID, workflowNodeVo.getId());
        nodeIr.put(TYPE, getNodeType());
        nodeIr.put(NAME, workflowNodeVo.getName());
        NodeType irNodeType = NodeType.fromType(workflowNodeVo.getType());

        // 开始节点EI测output作为jiuwen input和output
        boolean isStart = Strings.CS.equals(NodeType.START.getType(), workflowNodeVo.getType());
        List<WorkflowFieldVO> nodeInputs = isStart ? workflowNodeVo.getOutputs() : workflowNodeVo.getInputs();
        List<WorkflowFieldVO> nodeOutputs = workflowNodeVo.getOutputs();
        Map<String, JiuwenFields> confFieldsMap = new HashMap<>();
        if (nodeInputs != null) {
            nodeIr.put(INPUTS, adaptNodeParams(nodeInputs, INPUTS, NodeType.fromType(workflowNodeVo.getType())));
            adaptConfFields(nodeInputs, confFieldsMap, INPUTS, irNodeType);
        }
        if (nodeOutputs != null) {
            nodeIr.put(OUTPUTS, adaptNodeParams(nodeOutputs, OUTPUTS, NodeType.fromType(workflowNodeVo.getType())));
            adaptConfFields(nodeOutputs, confFieldsMap, OUTPUTS, irNodeType);
        }

        // 配置值去除空值
        Map<String, Object> configs = adaptConfig(workflowNodeVo);

        // 适配格式化输出，只有结束节点、消息节点和问答节点支持
        if (NodeType.END.equals(irNodeType) || NodeType.MESSAGE.equals(irNodeType) || NodeType.QA.equals(irNodeType)) {
            Map<String, Object> nodeConfigs = workflowNodeVo.getConfigs();
            configs.put(STRUCT_OUTPUT_TEMPLATE, nodeConfigs.get(STRUCT_OUTPUT_TEMPLATE));
            // 消息节点和结束节点默认开启历史会话写入
            configs.put(ENABLE_HISTORY, ObjectUtils.isNotEmpty(nodeConfigs.get(ENABLE_HISTORY)) ? nodeConfigs.get(ENABLE_HISTORY) :
                NodeType.END.equals(irNodeType) || NodeType.MESSAGE.equals(irNodeType));
            // 是否结构化输出，默认关闭
            configs.put(IS_STRUCT_MESSAGE, ObjectUtils.isNotEmpty(nodeConfigs.get(IS_STRUCT_MESSAGE)) ? nodeConfigs.get(IS_STRUCT_MESSAGE) : false);
        }
        adaptExceptionProcess(nodeIr, configs, workflowNodeVo);
        configs.entrySet().removeIf(entry -> Objects.isNull(entry.getValue()));
        nodeIr.put("configs", configs);

        // 设置config内fields信息
        configs.put(USER_FIELDS,
            confFieldsMap.get(USER_FIELDS) != null ? confFieldsMap.get(USER_FIELDS) : new HashMap<>());
        configs.put(SYSTEM_FIELDS,
            confFieldsMap.get(SYSTEM_FIELDS) != null ? confFieldsMap.get(SYSTEM_FIELDS) : new HashMap<>());

        // 实际配置了preDefinedFields才新增configs，不改动原有组件
        if (confFieldsMap.get(PRE_DEFINED_FIELDS) != null
            && (!confFieldsMap.get(PRE_DEFINED_FIELDS).getInputs().isEmpty()
                || !confFieldsMap.get(PRE_DEFINED_FIELDS).getOutputs().isEmpty())) {
            configs.put(PRE_DEFINED_FIELDS, confFieldsMap.get(PRE_DEFINED_FIELDS) != null
                ? confFieldsMap.get(PRE_DEFINED_FIELDS) : new HashMap<>());
        }

        return nodeIr;
    }

    /**
     * 适配配置字段
     *
     * @param fields 输入输出字段
     * @param confFieldsMap conf下字段map
     * @param source 来源inputs or outputs
     * @param nodeType 节点类型
     */
    private void adaptConfFields(List<WorkflowFieldVO> fields, Map<String, JiuwenFields> confFieldsMap, String source,
        NodeType nodeType) {
        JiuwenFields confUserFields = confFieldsMap.computeIfAbsent(USER_FIELDS, k -> new JiuwenFields());
        JiuwenFields confSystemFields = confFieldsMap.computeIfAbsent(SYSTEM_FIELDS, k -> new JiuwenFields());
        JiuwenFields confPreDefinedFields = confFieldsMap.computeIfAbsent(PRE_DEFINED_FIELDS, k -> new JiuwenFields());

        List<Map<String, Object>> userJiuwenFields;
        List<Map<String, Object>> systemJiuwenFields;
        List<Map<String, Object>> preDefinedJiuwenFields;
        if (source.equalsIgnoreCase(INPUTS)) {
            userJiuwenFields = confUserFields.getInputs();
            systemJiuwenFields = confSystemFields.getInputs();
            preDefinedJiuwenFields = confPreDefinedFields.getInputs();
        } else {
            userJiuwenFields = confUserFields.getOutputs();
            systemJiuwenFields = confSystemFields.getOutputs();
            preDefinedJiuwenFields = confPreDefinedFields.getOutputs();
        }

        for (WorkflowFieldVO workflowField : fields) {
            // 用户新增参数需放在userFields下
            if (workflowField.getSource().equals(WorkflowFieldVO.SourceEnum.USER)) {
                userJiuwenFields
                    .add(adaptConfigField(workflowField, false, source.equalsIgnoreCase(OUTPUTS), false, nodeType));
            } else if (workflowField.getSource().equals(WorkflowFieldVO.SourceEnum.PRE_DEFINED)) {
                preDefinedJiuwenFields.add(adaptConfigField(workflowField, true, false, false, nodeType));
            } else if (workflowField.getSource().equals(WorkflowFieldVO.SourceEnum.ENVIRONMENT)) {
                userJiuwenFields.add(adaptConfigField(workflowField, false, source.equalsIgnoreCase(OUTPUTS), false, nodeType));
            } else {
                // 系统参数开始节点在systemFields下，其他节点平铺
                if (NodeType.START.equals(nodeType) || NodeType.WORKFLOW.equals(nodeType) && source.equals(INPUTS)) {
                    systemJiuwenFields.add(adaptConfigField(workflowField, true, false, true, nodeType));
                }
            }
        }
    }

    /**
     * 适配conf下field字段
     *
     * @param workflowField 工作流输入输出字段
     * @param shouldConvert 是否为系统变量或预定义变量
     * @param isOutput 是否为输出节点，输出节点在循环节点需额外新增引用信息
     * @return 返回jiuwen字段
     */
    private Map<String, Object> adaptConfigField(WorkflowFieldVO workflowField, boolean shouldConvert, boolean isOutput,
        boolean shouldConvertSub, NodeType nodeType) {
        Map<String, Object> jiuwenField = new HashMap<>();
        String type = WorkflowUtils.formatWorkflowType(workflowField.getType());

        // 系统变量和预定义变量需下划线转驼峰
        jiuwenField.put(ID, shouldConvert ? IRUtils.adaptKey(workflowField.getName()) : workflowField.getName());
        jiuwenField.put(TYPE, type);
        jiuwenField.put(DESCRIPTION, workflowField.getDescription());
        jiuwenField.put(REQUIRED, workflowField.isRequired());
        if (NodeType.START.equals(nodeType) && !Objects.isNull(workflowField.getValue())
            && !Objects.isNull(workflowField.getValue().getDefault())) {
            jiuwenField.put(DEFAULT_VALUE, workflowField.getValue().getDefault());
        }

        // 输入节点参数需要加个actualType，适配file类型
        if (NodeType.INPUT.equals(nodeType)) {
            // 适配array<file>
            if (Strings.CS.equals(TypeEnum.ARRAY.toString(), type)) {
                Map<String, Object> schemaMap = JsonUtils.objectToClass(workflowField.getSchema());
                jiuwenField.put(ACTUAL_TYPE, String.format(ARRAY_PATTERN, schemaMap.get(TYPE)));
            } else {
                jiuwenField.put(ACTUAL_TYPE, WorkflowUtils.formatWorkflowActualType(workflowField.getType()));
            }
        }
        // 复杂类型新增schema参数，没有时需留空
        if (workflowField.getSchema() != null) {
            jiuwenField.put(SCHEMA, adaptSchema(type, workflowField.getSchema(), 0, shouldConvertSub));
        }
        WorkflowFieldVOValue value = workflowField.getValue();
        if (value != null && value.getType() != null) {
            switch (value.getType()) {
                case REF -> {
                    jiuwenField.put(SOURCE_TYPE, "ref");
                    if (isOutput) {
                        Map<String, String> refMap = JsonUtils.objectToClass(value.getContent());
                        if (refMap == null) {
                            throw new AgentStudioException(StudioError.WORKFLOW_IR_FAILED_OUTPUT_TYPE_INVALID, workflowField.getName());
                        }
                        jiuwenField.put(VALUE, adaptValue(refMap.get(CommonConstant.Workflow.REF_NODE_ID),
                            refMap.get(CommonConstant.Workflow.REF_VAR_NAME), refMap.get("source")));
                    }
                }
                case LITERAL -> jiuwenField.put(SOURCE_TYPE, "input");
                default -> jiuwenField.put(SOURCE_TYPE, "null");
            }
        }
        return jiuwenField;
    }

    /**
     * 工作流值与引用转换接口
     *
     * @param workflowField 节点值
     * @param nodeType NodeType
     * @param shouldConvertSub 子项是否需要驼峰转换（系统输出参数需要）
     * @return 节点值引用
     */
    public Object adaptFieldValue(WorkflowFieldVO workflowField, NodeType nodeType, boolean shouldConvertSub) {
        log.debug("Start adapting field value, workflowField={}, nodeType={}, shouldConvertSub={}", workflowField,
            nodeType, shouldConvertSub);

        WorkflowFieldVOValue workflowFieldValue = workflowField.getValue();
        Object fieldValue;
        switch (workflowFieldValue.getType()) {
            case LITERAL -> {
                fieldValue = workflowFieldValue.getContent();
            }
            case GENERATED -> {
                log.debug("Handling GENERATED type, start generating placeholders");

                String fieldType = WorkflowUtils.formatWorkflowType(workflowField.getType());
                fieldValue = generatePlaceholders(fieldType, workflowField.getSchema(), 0, nodeType, shouldConvertSub);
            }
            case REF -> {
                Map<String, String> refMap = JsonUtils.objectToClass(workflowFieldValue.getContent());
                if (refMap == null) {
                    return null;
                }
                fieldValue = adaptValue(refMap.get(CommonConstant.Workflow.REF_NODE_ID),
                    refMap.get(CommonConstant.Workflow.REF_VAR_NAME), refMap.get("source"));
            }
            case NESTED -> {
                Map<String, Object> referenceMap = new HashMap<>();
                List<Object> schemaList = JsonUtils.objectToClass(workflowField.getSchema());
                if (schemaList != null) {
                    for (Object subSchema : schemaList) {
                        WorkflowFieldVO itemField = adaptWorkflowField(subSchema, shouldConvertSub);
                        referenceMap.put(itemField.getName(), adaptFieldValue(itemField, nodeType, shouldConvertSub));
                    }
                }
                fieldValue = referenceMap;
            }
            default -> {
                log.error("unsupported workflow field value type");
                return null;
            }
        }
        return fieldValue;
    }

    private Object generatePlaceholders(String type, Object schema, int depth, NodeType nodeType,
        boolean shouldConvertSub) {
        log.debug("Start generating placeholders，type={}, schema={}, depth={}, nodeType={}, shouldConvertSub={}", type,
            schema, depth, nodeType, shouldConvertSub);

        // 占位符生成, json嵌套最大3层
        if (depth > MAX_DEPTH + 1) {
            log.error("json schema exceed max depth");
            throw new AgentStudioException(StudioError.CHECK_WORKFLOW_INFO_FAILED_DEPTH, MAX_DEPTH);
        }
        if (Strings.CS.equals(TypeEnum.ARRAY.toString(), type)) {
            return generateListPlaceholders(schema, depth, nodeType, shouldConvertSub);
        } else if (Strings.CS.equals(TypeEnum.OBJECT.toString(), type)) {
            return generateObjectPlaceholders(schema, depth + 1, nodeType, shouldConvertSub);
        } else {
            // 基础类型使用对应字符进行占位
            return generateStationSymbol(type, nodeType);
        }
    }

    private Object generateStationSymbol(String type, NodeType nodeType) {
        // 非输入节点，基础类型使用空字符进行占位
        if (!NodeType.INPUT.equals(nodeType)) {
            return StringUtils.EMPTY;
        }
        // 输入节点，类型不同占位字符不同
        return switch (TypeEnum.valueOf(type.toUpperCase(Locale.ROOT))) {
            case INTEGER, NUMBER -> 0;
            case BOOLEAN -> false;
            default -> StringUtils.EMPTY;
        };
    }

    private Map<String, Object> generateObjectPlaceholders(Object schema, int depth, NodeType nodeType,
        boolean shouldConvertSub) {
        // object类型placeHolder生成
        Map<String, Object> result = new HashMap<>();
        List<Object> schemaList = JsonUtils.objectToClass(schema);
        if (schemaList != null) {
            for (Object subSchema : schemaList) {
                WorkflowFieldVO workflowField = adaptWorkflowField(subSchema, shouldConvertSub);
                result.put(workflowField.getName(), generatePlaceholders(workflowField.getType(),
                    workflowField.getSchema(), depth, nodeType, shouldConvertSub));
            }
        }
        return result;
    }

    private List<Object> generateListPlaceholders(Object schema, int depth, NodeType nodeType,
        boolean shouldConvertSub) {
        // list类型placeHolder生成
        List<Object> result = new ArrayList<>();
        WorkflowFieldVO workflowField = adaptWorkflowField(schema, shouldConvertSub);
        result.add(generatePlaceholders(workflowField.getType(), workflowField.getSchema(), depth, nodeType,
            shouldConvertSub));
        return result;
    }

    /**
     * 工作流节点参数转换
     *
     * @param fieldVoList 节点信息
     * @param source 输入or输出
     * @param nodeType 节点类型
     * @return inputs or outputs
     */
    private Map<String, Object> adaptNodeParams(List<WorkflowFieldVO> fieldVoList, String source, NodeType nodeType) {
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> userFields = new HashMap<>();
        Map<String, Object> systemFields = new HashMap<>();
        Map<String, Object> environmentFields = new HashMap<>();

        for (WorkflowFieldVO workflowField : fieldVoList) {
            // 用户参数，放在userFields下
            if (Strings.CS.equals(workflowField.getSource().toString(), WorkflowFieldVO.SourceEnum.USER.toString())
                || Strings.CS.equals(workflowField.getSource().toString(),
                WorkflowFieldVO.SourceEnum.REQUEST.toString())) {
                userFields.put(workflowField.getName(), adaptFieldValue(workflowField, nodeType, false));
                continue;
            }
            // 预定义变量无需驼峰转换
            if (Strings.CS.equals(workflowField.getSource().toString(),
                WorkflowFieldVO.SourceEnum.PRE_DEFINED.toString())) {
                params.put(IRUtils.adaptKey(workflowField.getName()), adaptFieldValue(workflowField, nodeType, false));
                continue;
            }
            // 环境变量，入参在environmentFields下
            if (Strings.CS.equals(workflowField.getSource().toString(),
                WorkflowFieldVO.SourceEnum.ENVIRONMENT.toString())) {
                userFields.put(workflowField.getName(), adaptFieldValue(workflowField, nodeType, false));
                continue;
            }
            // 系统参数，开始节点、子workflow节点的入参在systemFields下
            if (NodeType.START.equals(nodeType) || NodeType.WORKFLOW.equals(nodeType) && source.equals(INPUTS)) {
                systemFields.put(IRUtils.adaptKey(workflowField.getName()),
                    adaptFieldValue(workflowField, nodeType, true));
                continue;
            }
            // 系统参数，子变量也需要驼峰转换
            params.put(IRUtils.adaptKey(workflowField.getName()), adaptFieldValue(workflowField, nodeType, true));
        }
        params.put(USER_FIELDS, userFields);
        if (!systemFields.isEmpty()) {
            params.put(SYSTEM_FIELDS, systemFields);
        }
        return params;
    }

    @Override
    public String adaptValue(String nodeId, String varName, String fieldType) {
        if (Strings.CS.equals(fieldType, "user")) {
            return String.format("${%s.%sFields.%s}", nodeId, fieldType, varName);
        } else if (Strings.CS.equals(fieldType, "system") && Strings.CS.equals(nodeId, "node_start")) {
            return String.format("${%s.%sFields.%s}", nodeId, fieldType, IRUtils.adaptKey(varName));
        } else if (Strings.CS.equals(fieldType, "pre_defined")) {
            return String.format("${%s.%s}", nodeId, IRUtils.adaptPreDefinedKey(varName));
        } else if (Strings.CS.equals(fieldType, "environment")) {
            return String.format("${%s.%s}", "_env.plugin_url_params", varName);
        } else if (Strings.CS.equals(fieldType, "request")) {
            return String.format("${%s.%s}", "_request", varName);
        } else {
            return String.format("${%s.%s}", nodeId, IRUtils.adaptKey(varName));
        }
    }

    @Override
    public void adaptModel(Map<String, Object> jiuwenConfigs, Map<String, Object> eiConfigs) {
        Object eiModel = eiConfigs.get(MODEL);
        if (eiModel instanceof Map) {
            Map<String, Object> jiuwenModel = new HashMap<>();
            Map<String, String> eiModelMap = JsonUtils.objectToClass(eiModel);
            if (eiModelMap == null) {
                throw new AgentStudioException(StudioError.UNEXPECTED_ERROR);
            }
            WorkflowManagementService wm = SpringBeanUtils.getBean(WorkflowManagementService.class);

            // 添加POC\HC共用参数
            adaptModelParams(jiuwenModel, eiConfigs);
            ModelServiceManager modelServiceManager = SpringBeanUtils.getBean(ModelServiceManager.class);
            Map<String,
                Object> extension = modelServiceManager.parseModelExtension(eiModelMap.get(MODEL_DEPLOYMENT_ID),
                    RequestContextUtils.getRequestProjectId(), RequestContextUtils.getRequestWorkspaceId(),
                    Boolean.TRUE.equals(eiConfigs.get(SAFETY_BARRIER)) || "true".equals(eiConfigs.get(SAFETY_BARRIER)));

            if (!Objects.isNull(extension)) {
                jiuwenModel.put("extension", extension);
            }
            // hc版本模型信息仍需要存储在ir内
            jiuwenModel.put(IRUtils.adaptKey(MODEL_NAME), eiModelMap.get(MODEL_DEPLOYMENT_ID)
                + (StringUtils.isEmpty(eiModelMap.get(MODEL)) ? "" : "|" + eiModelMap.get(MODEL)));
            jiuwenModel.put(IRUtils.adaptKey(MODEL_TYPE), eiModelMap.get(MODEL_TYPE));

            // 给一个默认modelType
            if (Objects.isNull(jiuwenModel.get(IRUtils.adaptKey(MODEL_TYPE)))
                || StringUtils.isEmpty(jiuwenModel.get(IRUtils.adaptKey(MODEL_TYPE)).toString())) {
                jiuwenModel.put(IRUtils.adaptKey(MODEL_TYPE), wm.getDefaultModelType());
            }
            jiuwenConfigs.put(MODEL, jiuwenModel);

            // 适配模型extension参数
            IRUtils.adaptModelExtension(jiuwenModel);
        } else {
            // 兼容下老版本
            jiuwenConfigs.put(MODEL, eiModel);
        }
    }

    /**
     * 适配模型超参
     *
     * @param jiuwenConfigs 九问配置
     * @param eiConfigs ei配置
     */
    private void adaptModelParams(Map<String, Object> jiuwenConfigs, Map<String, Object> eiConfigs) {
        Map<String, Object> modelParams = new HashMap<>();
        jiuwenConfigs.put(IRUtils.adaptKey(HYPER_PARAMETERS), modelParams);

        if (eiConfigs.get(TEMPERATURE) != null) {
            modelParams.put(TEMPERATURE, eiConfigs.get(TEMPERATURE));
        }

        if (eiConfigs.get(TOP_P) != null) {
            modelParams.put(TOP_P, eiConfigs.get(TOP_P));
        }

        if (eiConfigs.get(CommonConstant.ModelParam.FREQUENCY_PENALTY) != null) {
            modelParams.put(CommonConstant.ModelParam.FREQUENCY_PENALTY,
                eiConfigs.get(CommonConstant.ModelParam.FREQUENCY_PENALTY));
        }

        if (eiConfigs.get(CommonConstant.ModelParam.MAX_TOKENS) != null) {
            modelParams.put(CommonConstant.ModelParam.MAX_TOKENS, eiConfigs.get(CommonConstant.ModelParam.MAX_TOKENS));
        }

        // 适配大模型节点深度思考开关
        if (eiConfigs.get(ENABLE_THINKING) != null) {
            String type = Strings.CI.equals(eiConfigs.get(ENABLE_THINKING).toString(), Boolean.TRUE.toString()) ? ENABLE_THINKING_ENABLED : ENABLE_THINKING_DISABLED;
            HashMap<String, String> typeMap = new HashMap<>();
            typeMap.put(CommonConstant.ModelParam.TYPE, type);
            modelParams.put(CommonConstant.ModelParam.THINKING, typeMap);
        }
    }

    /**
     * dsl schema转换九问schema
     *
     * @param type 类型
     * @param schema schema
     * @param depth 深度，深度最大为3
     * @return 转换后schema
     */
    public Object adaptSchema(String type, Object schema, int depth, boolean shouldConvertSub) {
        log.debug("Entering adaptSchema with parameters: type={}, schema={}, depth={}, shouldConvertSub={}", type,
            schema, depth, shouldConvertSub);

        // 递归深度控制, json嵌套最大3层
        if (depth > MAX_DEPTH) {
            log.error("json schema exceed max depth");
            throw new AgentStudioException(StudioError.CHECK_WORKFLOW_INFO_FAILED_DEPTH, MAX_DEPTH);
        }
        if (Strings.CS.equals(TypeEnum.ARRAY.toString(), type)) {
            return adaptObjectSchema(schema, depth, shouldConvertSub);
        } else if (Strings.CS.equals(TypeEnum.OBJECT.toString(), type)) {
            return adaptListSchema(schema, depth + 1, shouldConvertSub);
        } else if (type.contains("|") && schema instanceof ArrayList<?>) {
            return adaptUnionTypeSchema(type, schema, depth, shouldConvertSub);
        } else {
            return Map.of(ID, StringUtils.EMPTY, TYPE, type);
        }
    }

    private Map<String, Object> adaptObjectSchema(Object schema, int depth, boolean shouldConvertSub) {
        // object类型schema转换
        Map<String, Object> result = new HashMap<>();
        WorkflowFieldVO workflowField = adaptWorkflowField(schema, shouldConvertSub);
        result.put(ID, workflowField.getName() != null ? workflowField.getName() : StringUtils.EMPTY);
        if (Strings.CI.equals(TypeEnum.TIME.toString(), workflowField.getType())) {
            result.put(TYPE, TypeEnum.STRING.toString());
        } else {
            result.put(TYPE, workflowField.getType());
        }
        if (workflowField.getDescription() != null) {
            result.put(DESCRIPTION, workflowField.getDescription());
        }
        if (workflowField.getSchema() != null) {
            Object subSchema = adaptSchema(workflowField.getType(), workflowField.getSchema(), depth, false);
            result.put(SCHEMA, subSchema);
        }
        return result;
    }

    private List<Object> adaptListSchema(Object schema, int depth, boolean shouldConvertSub) {
        // list类型schema转换
        List<Object> result = new ArrayList<>();
        List<Object> schemaList = JsonUtils.objectToClass(schema);
        if (schemaList != null) {
            for (Object subSchema : schemaList) {
                WorkflowFieldVO workflowField = adaptWorkflowField(subSchema, shouldConvertSub);
                WorkflowFieldVOMemory memory = adaptWorkflowFieldMemory(subSchema);
                Map<String, Object> subResult = new HashMap<>();
                subResult.put(ID, workflowField.getName());
                if (Strings.CI.equals(TypeEnum.TIME.toString(), workflowField.getType())) {
                    subResult.put(TYPE, TypeEnum.STRING.toString());
                } else {
                    subResult.put(TYPE, workflowField.getType());
                }
                if (workflowField.getDescription() != null) {
                    subResult.put(DESCRIPTION, workflowField.getDescription());
                }
                if (workflowField.getSchema() != null) {
                    subResult.put(SCHEMA,
                        adaptSchema(workflowField.getType(), workflowField.getSchema(), depth, shouldConvertSub));
                }
                if (memory.getAgingLevel() != null) {
                    subResult.put(AGING_LEVEL, memory.getAgingLevel());
                }
                if (memory.getStorageMethod() != null) {
                    subResult.put(STORAGE_METHOD, memory.getStorageMethod());
                }
                if (memory.getStorageMethod() != null && memory.getValue() != null) {
                    subResult.put(DEFAULT_VALUE, memory.getValue().getDefault());
                }
                result.add(subResult);
            }
        }
        return result;
    }

    /**
     * 适配联合类型，Mcp 服务工具参数存在 oneOf/anyOf 语法
     * 例如：oneOf/anyOf:[{type:string},{type:object},{type:number}]
     * 前端在 dsl 中会对此类语法 type 做拼接：type: string | object | number
     *
     * @param type 联合类型，demo：string | object | number
     * @param schema List 类型
     */
    private List<Object> adaptUnionTypeSchema(String type, Object schema, int depth, boolean shouldConvertSub) {
        String[] types = type.split("\\|");
        List<Object> objectList = new ArrayList<>();
        int len = Math.min(types.length, ((ArrayList<?>) schema).size());
        for (int i = 0; i < len; i++) {
            // 对每种 type 单独做 schema 适配
            String subType = types[i].trim();
            Object subSchema = ((ArrayList<?>) schema).get(i);
            Object result;
            if (Strings.CS.equals(TypeEnum.ARRAY.toString(), subType)) {
                result = adaptObjectSchema(subSchema, depth, shouldConvertSub);
            } else if (Strings.CS.equals(TypeEnum.OBJECT.toString(), subType)) {
                result = adaptListSchema(subSchema, depth + 1, shouldConvertSub);
            } else {
                result = Map.of(ID, StringUtils.EMPTY, TYPE, subType);
            }
            objectList.add(result);
        }
        return objectList;
    }

    private WorkflowFieldVO adaptWorkflowField(Object schema, boolean shouldConvertSub) {
        // workflowField类型从object构造
        Map<String, Object> schemaMap = JsonUtils.objectToClass(schema);
        if (schemaMap != null) {
            String name = schemaMap.get(NAME) != null ? schemaMap.get(NAME).toString() : StringUtils.EMPTY;
            WorkflowFieldVO workflowField =
                new WorkflowFieldVO().setName(shouldConvertSub ? IRUtils.adaptKey(name) : name);
            workflowField.setType(schemaMap.get(TYPE) != null
                ? WorkflowUtils.formatWorkflowType(schemaMap.get(TYPE).toString()) : TypeEnum.STRING.toString());
            workflowField.setSchema(schemaMap.get(SCHEMA));
            if (schemaMap.get(REQUIRED) instanceof Boolean b) {
                workflowField.setRequired(b);
            }
            if (schemaMap.get(DESCRIPTION) != null) {
                workflowField.setDescription(schemaMap.get(DESCRIPTION).toString());
            }
            if (schemaMap.get(CN_NAME) != null) {
                workflowField.setCnName(schemaMap.get(CN_NAME).toString());
            }
            if (schemaMap.get(VALUE) != null) {
                workflowField.setValue(JsonUtils.objectToClassType(schemaMap.get(VALUE), WorkflowFieldVOValue.class));
            }
            return workflowField;
        }
        return new WorkflowFieldVO();
    }

    private WorkflowFieldVOMemory adaptWorkflowFieldMemory(Object schema) {
        // 记忆变量中添加AGING_LEVEL和STORAGE_METHOD
        Map<String, Object> schemaMap = JsonUtils.objectToClass(schema);
        if (schemaMap != null) {
            WorkflowFieldVOMemory memory = new WorkflowFieldVOMemory();
            if (schemaMap.get(AGING_LEVEL) != null) {
                memory.setAgingLevel(schemaMap.get(AGING_LEVEL).toString());
            }
            if (schemaMap.get(STORAGE_METHOD) != null) {
                memory.setStorageMethod(schemaMap.get(STORAGE_METHOD).toString());
            }
            if (schemaMap.get(VALUE) != null && schemaMap.get(AGING_LEVEL) != null) {
                memory.setValue(JsonUtils.objectToClassType(schemaMap.get(VALUE), WorkflowFieldVOValue.class));
            }
            return memory;
        }
        return new WorkflowFieldVOMemory();
    }

    private void adaptExceptionProcess(Map<String, Object> nodeIr, Map<String, Object> config,
        WorkflowNodeVO workflowNodeVo) {
        Map<String, Object> nodeConfigs = workflowNodeVo.getConfigs();
        IrAdapterService irAdapterService = SpringBeanUtils.getBean(IrAdapterService.class);
        List<String> exceptionNodeTypes = irAdapterService.getExceptionNodeTypes();

        // 节点未配置异常或不支持异常配置节点直接return
        if (nodeConfigs == null || nodeConfigs.get(EXCEPTION_PROCESS) == null
            || !exceptionNodeTypes.contains(workflowNodeVo.getType())) {
            return;
        }

        ExceptionProcess exceptionProcess =
            JsonUtils.objectToClassType(nodeConfigs.get(EXCEPTION_PROCESS), ExceptionProcess.class);
        if (exceptionProcess == null) {
            throw new AgentStudioException(StudioError.WORKFLOW_EXCEPTION_CONFIG_INVALID, workflowNodeVo.getName());
        }

        validExceptionProcessParam(workflowNodeVo, exceptionProcess);

        Map<String, Object> output = JsonUtils.objectToClass(nodeIr.get(OUTPUTS));
        if (output == null) {
            output = new HashMap<>();
        }
        output.put("isSuccess", StringUtils.EMPTY);
        Map<String, String> errorBody = new HashMap<>();
        errorBody.put("errorMessage", StringUtils.EMPTY);
        errorBody.put("errorCode", StringUtils.EMPTY);
        output.put("errorBody", errorBody);
        nodeIr.put(OUTPUTS, output);
        Map<String, Object> exceptionIr = new HashMap<>();
        exceptionIr.put("timeout", exceptionProcess.getTimeout());
        if (ExceptionProcess.HandleTypeEnum.DEFAULTOUTPUTS.equals(exceptionProcess.getHandleType())) {
            exceptionIr.put("defaultOutputs",
                adaptExceptionDefaultOutputs(workflowNodeVo.getOutputs(), exceptionProcess.getDefaultOutputs()));
        }
        exceptionIr.put("handleType", exceptionProcess.getHandleType().toString());
        exceptionIr.put("retryTimes", exceptionProcess.getRetryTimes() != null ? exceptionProcess.getRetryTimes() : 0);
        config.put(IRUtils.adaptKey(EXCEPTION_PROCESS), exceptionIr);
    }

    /**
     * 校验异常处理参数
     *
     * @param node 节点
     * @param exceptionProcess 异常参数
     */
    public void validExceptionProcessParam(WorkflowNodeVO node, ExceptionProcess exceptionProcess) {
        NodeType nodeType = NodeType.fromType(node.getType());
        if (nodeType == null) {
            log.error("Node:{} nodeType:{} not support", node.getName(), node.getType());
            throw new AgentStudioException(StudioError.NODE_TYPE_NOT_SUPPORT, node.getType());
        }

        if (exceptionProcess.getHandleType() == null) {
            log.error("Node:{} exception:{} handleType not support.", node.getName(), exceptionProcess);
            throw new AgentStudioException(StudioError.WORKFLOW_EXCEPTION_NOT_SUPPORT_HANDLER, node.getName());
        }

        // 节点特殊校验
        Map<String, Object> map = node.getConfigs();
        switch (nodeType) {
            // 大模型流式输出不支持重试和超时设置
            case LLM -> {
                if (map.get("stream") != null) {
                    if (Boolean.parseBoolean(map.get("stream").toString())) {
                        validStreamOut(node, exceptionProcess);
                    } else {
                        // 非流式，非json格式 异常处理只支持中断
                        Object format = map.get("response_format");
                        if ((format == null || !format.toString().equalsIgnoreCase("json"))
                            && (exceptionProcess.getHandleType() != null
                                && exceptionProcess.getHandleType() != ExceptionProcess.HandleTypeEnum.INTERRUPT)) {
                            log.error(
                                "llm block output and response_format is not json only support interrupt, node:{}, handleType:{}",
                                node.getName(), exceptionProcess.getHandleType());
                            throw new AgentStudioException(StudioError.WORKFLOW_EXCEPTION_LLM_HANDLE_TYPE_INVALID,
                                node.getName());
                        }
                    }
                }
            }
            // 插件流式输出不支持重试和超时设置
            case PLUGIN -> {
                if (map.get("streaming") != null && Boolean.parseBoolean(map.get("streaming").toString())) {
                    validStreamOut(node, exceptionProcess);
                }
            }
            // 工作流节点不支持重试
            case WORKFLOW -> {
                if (exceptionProcess.getRetryTimes() != null && exceptionProcess.getRetryTimes() > 0) {
                    log.error("workflow node not support retry:, workflow node:{}, retry:{}.", node.getName(),
                        exceptionProcess.getRetryTimes());
                    throw new AgentStudioException(StudioError.WORKFLOW_EXCEPTION_WORKFLOW_NOT_SUPPORT_RETRY,
                        node.getName());
                }
            }
        }

        // 超时时间范围 0.1-900
        if (exceptionProcess.getTimeout() != null && (exceptionProcess.getTimeout() < EXCEPTION_MIN_TIMEOUT
            || exceptionProcess.getTimeout() > EXCEPTION_MAX_TIMEOUT)) {
            log.error("timeout setting should be: 0.1-900, node:{} timeout:{}", node.getName(),
                exceptionProcess.getTimeout());
            throw new AgentStudioException(StudioError.WORKFLOW_TIME_RANGE_INVALID, node.getName());
        }

        // 重试次数 0-3
        if (exceptionProcess.getRetryTimes() != null
            && (exceptionProcess.getRetryTimes() > 3 || exceptionProcess.getRetryTimes() < 0)) {
            log.error("max retry times is 3, node:{} retry:{}", node.getName(), exceptionProcess.getRetryTimes());
            throw new AgentStudioException(StudioError.WORKFLOW_EXCEPTION_RETRY_TIMES_INVALID, node.getName());
        }
    }

    private void validStreamOut(WorkflowNodeVO node, ExceptionProcess exceptionProcess) {
        // 不支持设置超时和重试次数.
        if ((exceptionProcess.getRetryTimes() != null && exceptionProcess.getRetryTimes() > 0)
            || (exceptionProcess.getHandleType() != null
                && exceptionProcess.getHandleType() != ExceptionProcess.HandleTypeEnum.INTERRUPT)) {
            log.error("stream output only support INTERRUPT, node:{}, exceptionProcess:{}.", node.getName(),
                exceptionProcess);
            throw new AgentStudioException(StudioError.WORKFLOW_EXCEPTION_STREAM_INVALID, node.getName());
        }
    }

    private Map<String, Object> adaptExceptionDefaultOutputs(List<WorkflowFieldVO> outputParams,
        Map<String, Object> defaultOutputs) {
        Map<String, Object> defaultOutputsIr = new HashMap<>();

        Map<String, Object> userFields = new HashMap<>();
        for (WorkflowFieldVO workflowField : outputParams) {
            // 配置的兜底输出中不包含节点输出字段，跳过
            if (defaultOutputs.get(workflowField.getName()) == null) {
                continue;
            }
            // 用户级参数需放在userFields下
            if (workflowField.getSource().equals(WorkflowFieldVO.SourceEnum.USER)
                && defaultOutputs.get(workflowField.getName()) != null) {
                userFields.put(workflowField.getName(), defaultOutputs.get(workflowField.getName()));
            } else {
                // 系统参数平铺，当前支持的节点输出系统参数只有一层，无需展开驼峰转换
                defaultOutputsIr.put(IRUtils.adaptKey(workflowField.getName()),
                    defaultOutputs.get(workflowField.getName()));
            }
        }
        if (!userFields.isEmpty()) {
            defaultOutputsIr.put("userFields", userFields);
        }
        return defaultOutputsIr;
    }

    /**
     * 当前在页面上只有一个记忆开关
     * 但是在九问的IR里，是按照不同记忆类型分开定义的开关
     *
     * @param enableMemory
     * @return
     */
    protected WorkFlowNodeMemoryIR adaptMemoryEnable(boolean enableMemory) {
        WorkFlowNodeMemoryIR memoryIR = new WorkFlowNodeMemoryIR();
        memoryIR.setUserProfile(new WorkFlowNodeUserProfileIR().setEnable(enableMemory));
        return memoryIR;
    }
}
