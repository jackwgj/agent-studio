/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.jiuwen.adapt;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openjiuwen.studio.agent.common.constant.Constants;
import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.common.utils.SpringBeanUtils;
import com.openjiuwen.studio.agent.manager.dto.KnowledgeRetrievePolicy;
import com.openjiuwen.studio.agent.manager.dto.WorkflowBranchVO;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;
import com.openjiuwen.studio.agent.manager.entity.KnowledgeRepoEntity;
import com.openjiuwen.studio.agent.manager.service.IrAdapterService;
import com.openjiuwen.studio.agent.manager.utils.IRUtils;
import com.openjiuwen.studio.agent.manager.utils.JsonUtils;

import lombok.extern.slf4j.Slf4j;

import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 意图节点转换IR
 *
 */
@Slf4j
public class IntentDetectionNodeAdapter extends AbstractIRNodeAdapter {
    private static final int DEFAULT_TURN = 0;

    /**
     * 意图prompt字段
     */
    private static final String PROMPT = "prompt";

    /**
     * 意图大模型相关配置字段
     */
    private static final String LLM = "llm";

    /**
     * 意图分支配置字段
     */
    private static final String BRANCHES = "branches";

    /**
     * 意图分类配置字段
     */
    private static final String CATALOG = "catalog";

    /**
     * 意图类别配置字段
     */
    private static final String CATEGORY = "category";

    /**
     * 意图是否开启历史会话
     */
    private static final String ENABLE_HISTORY = "enable_history";

    /**
     * 意图是否开启辅助识别
     */
    private static final String ENABLE_KNOWLEDGE = "enable_knowledge";

    /**
     * 意图知识库搜索阈值
     */
    private static final String RECALL_THRESHOLD = "recall_threshold";

    /**
     * 意图知识库相关配置
     */
    private static final String KG = "kg";

    /**
     * 意图识别样例内容
     */
    private static final String EXAMPLE_CONTENT = "example_content";

    /**
     * 意图识别样例配置
     */
    private static final String EXAMPLE_CONFIG = "example_config";

    /**
     * 意图识别允许输入
     */
    private static final String ENABLE_INPUT = "enable_input";

    /**
     * 意图识别最大轮数
     */
    private static final String CHAT_HISTORY_MAX_TURN = "chat_history_max_turn";

    /**
     * 意图识别默认知识库搜索阈值
     */
    private static final float DEFAULT_RECALL_THRESHOLD = 0.8f;

    /**
     * 意图识别节点文件id列表
     */
    private static final String FILE_IDS = "file_ids";

    /**
     * 知识库相关信息键值
     */
    private static final String REPOS = "repos";

    /**
     * 其他意图是否可被控制器全局意图覆盖
     */
    private static final String OVERRIDABLE = "overridable";

    /**
     * 知识库tags
     */
    private static final String TAGS = "tags";

    /**
     * ID
     */
    private static final String ID = "id";

    /**
     * 知识库最低得分环境变量key
     */
    private static final String MIN_SCORE_ENV_KEY = "knowledge.recall-threshold.min";

    /**
     * 意图辅助知识库检索默认topk
     */
    private static final int DEFAULT_TOP_K = 10;

    @Override
    public Map<String, Object> adaptConfig(WorkflowNodeVO workflowNodeVo) {
        Map<String, Object> configs = new HashMap<>();
        Map<String, Object> nodeConfigs = workflowNodeVo.getConfigs();
        configs.put(PROMPT, nodeConfigs.get(PROMPT));
        boolean isKnowledgeEnable =
            nodeConfigs.get(ENABLE_KNOWLEDGE) != null ? (Boolean) nodeConfigs.get(ENABLE_KNOWLEDGE) : false;
        configs.put(IRUtils.adaptKey(ENABLE_KNOWLEDGE), isKnowledgeEnable);
        if (isKnowledgeEnable && nodeConfigs.get(REPOS) != null) {
            IrAdapterService irAdapterService = SpringBeanUtils.getBean(IrAdapterService.class);

            // 获取意图识别节点中配置的知识库列表
            List<KnowledgeRepoEntity> knowledgeRepoEntities =
                irAdapterService.getReposFromWorkflowNode(nodeConfigs, RequestContextUtils.getRequestProjectId());
            Map<String, Object> pluginConfig = new HashMap<>();
            float minScore = IRUtils.toFloat(SpringBeanUtils.getProperty(MIN_SCORE_ENV_KEY, null), 0f);
            if (!CollectionUtils.isEmpty(knowledgeRepoEntities)) {
                pluginConfig = irAdapterService.parseKnowledgeRepoPlugin(knowledgeRepoEntities,
                    new KnowledgeRetrievePolicy().setFaqThreshold(minScore)
                        .setRecallThreshold(minScore)
                        .setTopK(DEFAULT_TOP_K),
                    false, null);
                pluginConfig.put(FILE_IDS, nodeConfigs.get(FILE_IDS));
            }
            configs.put(KG, pluginConfig);
        } else {
            configs.put(KG, new HashMap<>());
        }
        configs.put(IRUtils.adaptKey(RECALL_THRESHOLD),
            nodeConfigs.get(RECALL_THRESHOLD) != null ? nodeConfigs.get(RECALL_THRESHOLD) : DEFAULT_RECALL_THRESHOLD);

        configs.put(IRUtils.adaptKey(ENABLE_INPUT),
            nodeConfigs.get(ENABLE_INPUT) != null ? nodeConfigs.get(ENABLE_INPUT) : true);

        // 预留字段，九问IR示例及自动生成模型配置
        if (nodeConfigs.get(EXAMPLE_CONTENT) != null) {
            configs.put(IRUtils.adaptKey(EXAMPLE_CONTENT), nodeConfigs.get(EXAMPLE_CONTENT));
        }
        if (nodeConfigs.get(EXAMPLE_CONFIG) != null) {
            configs.put(IRUtils.adaptKey(EXAMPLE_CONFIG), nodeConfigs.get(EXAMPLE_CONFIG));
        }

        // 意图节点历史记录最大轮次，默认3轮，历史开关绑定轮次，轮次大于0打开
        if (nodeConfigs.get(CHAT_HISTORY_MAX_TURN) instanceof Integer maxTurn) {
            configs.put(IRUtils.adaptKey(CHAT_HISTORY_MAX_TURN), maxTurn > 0 ? maxTurn + maxTurn + 1 : 0);
            configs.put(IRUtils.adaptKey(ENABLE_HISTORY), maxTurn != 0);
        } else {
            configs.put(IRUtils.adaptKey(CHAT_HISTORY_MAX_TURN), DEFAULT_TURN);
            configs.put(IRUtils.adaptKey(ENABLE_HISTORY), false);
        }

        // 意图节点llm map需要进行驼峰转换
        Map<String, Object> llmConfig = JsonUtils.objectToClass(nodeConfigs.get(LLM));
        if (llmConfig != null) {
            Map<String, Object> llmMap = new HashMap<>();
            configs.put(LLM, llmMap);

            // 适配模型
            adaptModel(llmMap, llmConfig);
        }
        configs.put(IRUtils.adaptKey(BRANCHES),
            workflowNodeVo.getBranches().stream().map(this::adaptBranch).collect(Collectors.toList()));
        if (nodeConfigs.get(OVERRIDABLE) != null) {
            configs.put(OVERRIDABLE, nodeConfigs.get(OVERRIDABLE));
        } else {
            configs.put(OVERRIDABLE, false);
        }

        // 节点是否启用记忆的配置
        Object enableMemory = nodeConfigs.getOrDefault(Constants.Workflow.ENABLE_MEMORY, false);
        boolean enableMemoryBool = Boolean.parseBoolean(enableMemory.toString());
        configs.put(IRUtils.adaptKey(MEMORY), Map.of("enable", enableMemoryBool));
        return configs;
    }

    private Map<String, Object> adaptBranch(WorkflowBranchVO workflowBranchVo) {
        Map<String, Object> workflowBranch = new HashMap<>();
        workflowBranch.put(Constants.ID, workflowBranchVo.getId());

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> intentBranchConfig =
            objectMapper.convertValue(workflowBranchVo.getConfigs(), new TypeReference<>() {});
        workflowBranch.put(CATALOG, intentBranchConfig.get(CATEGORY));
        return workflowBranch;
    }

    @Override
    public String getNodeType() {
        return NodeType.INTENT_DETECTION.getIrType();
    }
}
