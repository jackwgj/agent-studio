/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.jiuwen.adapt;

import com.fasterxml.jackson.core.type.TypeReference;
import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.enums.TypeEnum;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.manager.dto.WorkflowFieldVO;
import com.openjiuwen.studio.agent.manager.dto.WorkflowFieldVOValidator;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;
import com.openjiuwen.studio.agent.manager.enums.QuestionerNodeMode;
import com.openjiuwen.studio.agent.manager.utils.IRUtils;
import com.openjiuwen.studio.agent.manager.utils.JsonUtils;
import com.openjiuwen.studio.agent.manager.utils.WorkflowUtils;
import com.openjiuwen.studio.agent.manager.workflow.jiuwen.models.RailsConfig;
import com.openjiuwen.studio.agent.manager.workflow.jiuwen.models.RewriteConfig;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

/**
 * 提问器节点转换IR
 *
 */
@Slf4j
public class QuestionerNodeAdapter extends AbstractIRNodeAdapter {
    private static final int PAIR = 2;

    /**
     * 提问器用户配置问题
     */
    private static final String QUESTION_CONTENT = "question_content";

    /**
     * 提问器用户输入配置
     */
    private static final String INPUT_COMPLEMENT = "input_complement";

    /**
     * 提问器开启历史
     */
    private static final String WITH_CHAT_HISTORY = "with_chat_history";

    /**
     * 提问器高级指令
     */
    private static final String EXTRA_PROMPT_FOR_FIELD_EXTRACTION = "extra_prompt_for_fields_extraction";

    private static final String EXTRACT_FIELDS = "extract_fields_from_response";

    /**
     * 提问器最大回复轮数
     */
    private static final String MAX_RESPONSE = "max_response";

    private static final String TYPE_COMPLEMENT = "type_complement";

    /**
     * 提问器参数信息
     */
    private static final String FIELD_NAMES = "field_names";

    /**
     * 提问器护栏配置
     */
    private static final String RAILS_CONFIG = "rails_config";

    /**
     * 提问器数字范围校验
     */
    private static final String NUMBER_RANGE_VALIDATE = "number_range_validate";

    /**
     * 提问器长度校验
     */
    private static final String LENGTH_LIMIT_VALIDATE = "length_limit_validate";

    /**
     * 提问器枚举校验
     */
    private static final String ENUM_LEGALITY_VALIDATE = "enum_legality_validate";

    /**
     * 提问器日期校验
     */
    private static final String DATE_TIME_FORMAT = "date_time_format";

    /**
     * 通用格式校验
     */
    private static final String COMMON_VALIDATE = "common_data_format_check";

    /**
     * 默认值校验
     */
    private static final String UPDATE_DEFAULT_VALUES = "update_default_values";

    /**
     * 提问器输入护栏
     */
    private static final String REWRITE_CHAIN = "rewrite_chain";

    /**
     * 提问器示例
     */
    private static final String EXAMPLE_CONTENT = "example_content";

    /**
     * 默认用户最后一轮输入（系统参数）
     */
    private static final String USER_RESPONSE = "USER_RESPONSE";

    /**
     * 提问器时间改写（开启日期校验自动添加）
     */
    private static final String REWRITE_TIME_INFO_IN_QUERY = "rewrite_time_info_in_query";

    /**
     * 提问器允许节点确认
     */
    private static final String ALLOW_NODE_CONFIRM = "allow_node_confirm";

    /**
     * 提问器允许节点跳出
     */
    private static final String ALLOW_NODE_BREAK = "allow_node_break";

    /**
     * 提问器可选择两种模式：效果优先（effect）、速度优先（speed）
     */
    private static final String MODE = "mode";

    /**
     * 提问器运行状态码
     */
    private static final String STATUS = "STATUS";

    /**
     * 智能追问开关
     */
    private static final String TEMPLATE_ANTHROPOMORPHIC = "template_anthropomorphic";

    /**
     * 自定义追问模板
     */
    private static final String AUTO_ASK_TEMPLATE = "auto_ask_template";

    /**
     * 辅助识别开关
     */
    private static final String ASSIST_RECOGNIZE = "assist_recognize";

    /**
     * 开启辅助识别后，样例内容键值
     */
    private static final String ASSIST_EXAMPLE = "assist_example";

    /**
     * 追问枚举值是否显示
     */
    private static final String ENUM_VISIBLE = "enum_visible";

    @Override
    public Map<String, Object> adaptConfig(WorkflowNodeVO workflowNodeVo) {
        Map<String, Object> config = new HashMap<>();
        Map<String, Object> nodeConfigs = workflowNodeVo.getConfigs();

        // 适配模型
        adaptModel(config, nodeConfigs);

        config.put(IRUtils.adaptKey(ENUM_VISIBLE), nodeConfigs.get(ENUM_VISIBLE));
        config.put(IRUtils.adaptKey(QUESTION_CONTENT), nodeConfigs.get(QUESTION_CONTENT));
        config.put(IRUtils.adaptKey(INPUT_COMPLEMENT),
            nodeConfigs.get(INPUT_COMPLEMENT) == null ? false : nodeConfigs.get(INPUT_COMPLEMENT));

        // 默认开启历史会话，无需用户选择
        config.put(IRUtils.adaptKey(WITH_CHAT_HISTORY), true);
        Optional.ofNullable(nodeConfigs.get(MODE)).ifPresent(mode -> config.put(MODE, mode));
        config.put(IRUtils.adaptKey(TEMPLATE_ANTHROPOMORPHIC),
            nodeConfigs.get(TEMPLATE_ANTHROPOMORPHIC) == null ? false : nodeConfigs.get(TEMPLATE_ANTHROPOMORPHIC));
        config.put(IRUtils.adaptKey(AUTO_ASK_TEMPLATE),
            nodeConfigs.get(AUTO_ASK_TEMPLATE) == null ? "" : nodeConfigs.get(AUTO_ASK_TEMPLATE));
        config.put(IRUtils.adaptKey(ASSIST_RECOGNIZE),
            nodeConfigs.get(ASSIST_RECOGNIZE) == null ? false : nodeConfigs.get(ASSIST_RECOGNIZE));
        config.put(IRUtils.adaptKey(ASSIST_EXAMPLE),
            nodeConfigs.get(ASSIST_EXAMPLE) == null ? "" : nodeConfigs.get(ASSIST_EXAMPLE));

        config.put(IRUtils.adaptKey(EXTRACT_FIELDS),
            nodeConfigs.get(EXTRACT_FIELDS) == null ? false : nodeConfigs.get(EXTRACT_FIELDS));
        config.put(IRUtils.adaptKey(MAX_RESPONSE), nodeConfigs.get(MAX_RESPONSE));
        config.put(IRUtils.adaptKey(TYPE_COMPLEMENT),
            nodeConfigs.get(TYPE_COMPLEMENT) == null ? "" : nodeConfigs.get(TYPE_COMPLEMENT));
        config.put(IRUtils.adaptKey(EXTRA_PROMPT_FOR_FIELD_EXTRACTION),
            nodeConfigs.get(EXTRA_PROMPT_FOR_FIELD_EXTRACTION));
        config.put(IRUtils.adaptKey(ALLOW_NODE_CONFIRM),
            nodeConfigs.get(ALLOW_NODE_CONFIRM) != null ? nodeConfigs.get(ALLOW_NODE_CONFIRM) : false);
        config.put(IRUtils.adaptKey(ALLOW_NODE_BREAK),
            nodeConfigs.get(ALLOW_NODE_BREAK) != null ? nodeConfigs.get(ALLOW_NODE_BREAK) : false);

        // 适配用户示例内容，如用户传值为空字符串，九问提供默认示例
        config.put(IRUtils.adaptKey(EXAMPLE_CONTENT),
            nodeConfigs.get(EXAMPLE_CONTENT) == null ? "" : nodeConfigs.get(EXAMPLE_CONTENT));

        RailsConfig railsConfig = adaptExecutionRails(config, workflowNodeVo.getOutputs());

        adaptRailsConfig(railsConfig, workflowNodeVo.getOutputs(), nodeConfigs);

        if (railsConfig.getRails() == null || railsConfig.getActionsConfig().isEmpty()) {
            config.put(IRUtils.adaptKey(RAILS_CONFIG), new HashMap<>());
        } else {
            config.put(IRUtils.adaptKey(RAILS_CONFIG), railsConfig);
        }
        return config;
    }

    private void adaptRailsConfig(RailsConfig railsConfig, List<WorkflowFieldVO> params,
        Map<String, Object> nodeConfigs) {
        AtomicBoolean needRewriteTimeInfo = new AtomicBoolean(false);

        // 历史存量提问器和效果优先提问器，需要判断是否包含时间日期格式转换
        if (Objects.isNull(nodeConfigs.get(MODE))
            || QuestionerNodeMode.EFFECT.toString().equalsIgnoreCase(nodeConfigs.get(MODE).toString())) {
            // 遍历输出参数，判断是否包含时间日期格式转换
            for (WorkflowFieldVO param : params) {
                if (CollectionUtils.isEmpty(param.getValidator()) || isPresetParam(param.getName())
                    || !WorkflowFieldVO.SourceEnum.USER.equals(param.getSource())) {
                    continue;
                }
                param.getValidator().forEach(m -> {
                    if (Strings.CS.equals(m.getType(), DATE_TIME_FORMAT)) {
                        needRewriteTimeInfo.set(true);
                    }
                });
            }
        }
        // 前端传入的改写护栏处理，如已包含时间改写护栏，无需额外新增
        List<RewriteConfig> rewriteConfigs = new ArrayList<>();
        if (nodeConfigs.get(REWRITE_CHAIN) != null) {
            List<Map<String, Object>> rewriteConfigList = JsonUtils.objectToClass(nodeConfigs.get(REWRITE_CHAIN));
            if (rewriteConfigList != null) {
                for (Map<String, Object> conf : rewriteConfigList) {
                    TypeReference<RewriteConfig> type = new TypeReference<>() {};
                    RewriteConfig rewriteConfig = JsonUtils.json2Obj(JsonUtils.toJson(conf), type);
                    if (Strings.CS.equals(rewriteConfig.getType(), REWRITE_TIME_INFO_IN_QUERY)) {
                        needRewriteTimeInfo.set(false);
                    }
                    rewriteConfigs.add(rewriteConfig);
                }
            }
        }
        // 如需要进行时间改写，新增时间改写护栏
        if (needRewriteTimeInfo.get()) {
            RewriteConfig timeRewrites = new RewriteConfig();
            timeRewrites.setType(REWRITE_TIME_INFO_IN_QUERY);
            timeRewrites.setEnabled(true);
            timeRewrites.setIndex(0);
            timeRewrites.setArgs(new HashMap<>());
            rewriteConfigs.add(timeRewrites);
        }
        if (!rewriteConfigs.isEmpty()) {
            adaptInputRails(railsConfig, rewriteConfigs);
        }
    }

    private void adaptInputRails(RailsConfig railsConfig, List<RewriteConfig> rewriteConfigs) {
        if (railsConfig.getRails() == null) {
            railsConfig.setRails(new HashMap<>());
        }
        if (railsConfig.getActionsConfig() == null) {
            railsConfig.setActionsConfig(new ArrayList<>());
        }

        List<Map<String, String>> input = new ArrayList<>();
        if (rewriteConfigs != null) {
            // 对改写按index进行排序
            List<RewriteConfig> sortedConfigs =
                rewriteConfigs.stream().sorted(Comparator.comparing(RewriteConfig::getIndex)).toList();
            for (RewriteConfig rewriteConfig : sortedConfigs) {
                if (rewriteConfig.isEnabled()) {
                    input.add(Map.of("action", rewriteConfig.getType()));
                    RailsConfig.ActionConfig actionConfigs = new RailsConfig.ActionConfig();
                    actionConfigs.setAction(rewriteConfig.getType());
                    actionConfigs.setActionExtraArgs(rewriteConfig.getArgs());
                    railsConfig.getActionsConfig().add(actionConfigs);
                }
            }
        }
        if (!input.isEmpty()) {
            railsConfig.getRails().put("input", input);
        }
    }

    private RailsConfig adaptExecutionRails(Map<String, Object> configs, List<WorkflowFieldVO> params) {
        List<Map<String, Object>> fieldNames = new ArrayList<>();
        Map<String, Map<String, Object>> railsDetails = new HashMap<>();
        for (WorkflowFieldVO param : params) {
            if (param.getSource() == WorkflowFieldVO.SourceEnum.USER && !isPresetParam(param.getName())) {
                Map<String, Object> fieldName = new HashMap<>();
                fieldName.put("cn_field_name", param.getCnName() != null ? param.getCnName() : "");
                fieldName.put("field_name", param.getName() != null ? param.getName() : "");
                fieldName.put("description", param.getDescription() != null ? param.getDescription() : "");
                fieldName.put("required", param.isRequired() != null ? param.isRequired() : false);
                fieldName.put("reflection",
                    isNeedReflection(Objects.toString(configs.get(MODE), StringUtils.EMPTY), param));
                if (param.getValue() != null && param.getValue().getDefault() != null
                    && StringUtils.isNotEmpty(param.getValue().getDefault().toString())) {
                    // 配置默认值且默认值不为空字符串，新增默认值校验护栏
                    fieldName.put("default_value", param.getValue().getDefault());
                    if (railsDetails.get(UPDATE_DEFAULT_VALUES) != null) {
                        railsDetails.get(UPDATE_DEFAULT_VALUES)
                            .putAll(new HashMap<>(Map.of(param.getName(), param.getValue().getDefault())));
                    } else {
                        railsDetails.put(UPDATE_DEFAULT_VALUES,
                            new HashMap<>(Map.of(param.getName(), param.getValue().getDefault())));
                    }
                } else {
                    fieldName.put("default_value", "");
                }
                String paramType = WorkflowUtils.formatWorkflowType(param.getType());
                fieldNames.add(fieldName);
                if (param.getValidator() != null) {
                    for (WorkflowFieldVOValidator validator : param.getValidator()) {
                        if (railsDetails.get(validator.getType()) != null) {
                            railsDetails.get(validator.getType())
                                .putAll(adaptExtraArgs(validator, param.getName(), paramType));
                        } else {
                            railsDetails.put(validator.getType(),
                                adaptExtraArgs(validator, param.getName(), paramType));
                        }
                    }
                }
            }
        }
        configs.put(IRUtils.adaptKey(FIELD_NAMES), fieldNames);
        if (railsDetails.isEmpty()) {
            return new RailsConfig();
        } else {
            RailsConfig railsConfigs = new RailsConfig();
            railsConfigs.setActionsConfig(new ArrayList<>());
            railsConfigs.setRails(new HashMap<>());
            List<Map<String, String>> executions = new ArrayList<>();
            railsDetails.keySet().forEach(action -> executions.add(Map.of("action", action)));
            railsConfigs.getRails().put("execution", executions);
            railsDetails.forEach((key, args) -> {
                RailsConfig.ActionConfig actionConfig = new RailsConfig.ActionConfig();
                actionConfig.setAction(key);
                actionConfig.setActionExtraArgs(args);
                railsConfigs.getActionsConfig().add(actionConfig);
            });
            return railsConfigs;
        }
    }

    private Map<String, Object> adaptExtraArgs(WorkflowFieldVOValidator validator, String fieldName, String fieldType) {
        if (Strings.CS.equals(validator.getType(), LENGTH_LIMIT_VALIDATE)) {
            return new HashMap<>(Map.of(fieldName, parseLengthLimitConfig(validator.getParams())));
        } else if (Strings.CS.equals(validator.getType(), NUMBER_RANGE_VALIDATE)) {
            return new HashMap<>(Map.of(fieldName, parseNumberRangeConfig(validator.getParams(), fieldType)));
        } else if (Strings.CS.equals(validator.getType(), ENUM_LEGALITY_VALIDATE)) {
            return new HashMap<>(Map.of(fieldName, parseEnumLimitConfig(validator.getParams())));
        } else if (Strings.CS.equals(validator.getType(), DATE_TIME_FORMAT)) {
            return new HashMap<>(Map.of(fieldName, parseDateTimeFormatConfig(validator.getParams())));
        } else if (Strings.CS.equals(validator.getType(), COMMON_VALIDATE)) {
            return new HashMap<>(Map.of(fieldName, validator.getParams().get(0)));
        } else {
            throw new AgentStudioException(StudioError.CONVERT_NODE_FAILED_VALIDATOR, validator.getType());
        }
    }

    private String parseDateTimeFormatConfig(List<String> dateTimeFormat) {
        try {
            return dateTimeFormat.get(0).strip();
        } catch (Exception e) {
            throw new AgentStudioException(StudioError.CONVERT_NODE_FAILED_PARSE);
        }
    }

    private Integer parseLengthLimitConfig(List<String> lengthLimit) {
        try {
            return Integer.parseInt(lengthLimit.get(0).strip());
        } catch (NumberFormatException e) {
            throw new AgentStudioException(StudioError.CONVERT_NODE_FAILED_PARSE_LENGTH);
        }
    }

    private Float parseNumber(String numStr) {
        try {
            return Float.parseFloat(numStr.strip());
        } catch (NumberFormatException e) {
            throw new AgentStudioException(StudioError.CONVERT_NODE_FAILED_NUM_FORMAT);
        }
    }

    private Integer parseInt(String numStr) {
        try {
            return Integer.parseInt(numStr.strip());
        } catch (NumberFormatException e) {
            throw new AgentStudioException(StudioError.CONVERT_NODE_FAILED_INT_LENGTH);
        }
    }

    private List<String> parseEnumLimitConfig(List<String> enumLimit) {
        try {
            String enumStr = enumLimit.get(0);
            return Stream.of(enumStr.split(",")).map(String::strip).toList();
        } catch (Exception e) {
            throw new AgentStudioException(StudioError.CONVERT_NODE_FAILED_ENUM_LENGTH);
        }
    }

    private List<Object> parseNumberRangeConfig(List<String> numberRange, String type) {
        try {
            List<Object> result = new ArrayList<>();
            if (Strings.CS.equals(type, TypeEnum.INTEGER.toString())) {
                numberRange.forEach(m -> result.add(parseInt(m)));
            } else {
                numberRange.forEach(m -> result.add(parseNumber(m)));
            }
            if (result.size() != PAIR) {
                throw new AgentStudioException(StudioError.WORKFLOW_NODE_EXECUTE_FAILED);
            }
            return result;
        } catch (Exception e) {
            throw new AgentStudioException(StudioError.CONVERT_NODE_FAILED_PARAM_LENGTH);
        }
    }

    @Override
    public String getNodeType() {
        return NodeType.QUESTIONER.getIrType();
    }

    /**
     * 系统预置的输出参数
     *
     * @param paramName 参数名
     * @return true：非系统预置参数，false：是系统预置参数
     */
    private boolean isPresetParam(String paramName) {
        return Strings.CS.equals(paramName, USER_RESPONSE) || Strings.CS.equals(paramName, STATUS);
    }

    /**
     * 参数是否需要反思
     *
     * @param mode 提问器模式
     * @param param 参数
     * @return boolean 是否需要反思
     */
    private boolean isNeedReflection(String mode, WorkflowFieldVO param) {
        // 历史存量提问器，mode为空，以前端传值为准
        if (StringUtils.isBlank(mode)) {
            return param.isReflection() != null ? param.isReflection() : false;
        }
        // 新提问器参数的反思配置，效果优先时默认打开，速度优先时默认关闭
        return QuestionerNodeMode.EFFECT.toString().equalsIgnoreCase(mode);
    }
}
