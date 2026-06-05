/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.jiuwen.adapt;

import com.openjiuwen.studio.agent.common.constant.Constants;
import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.common.utils.SpringBeanUtils;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.dto.WorkflowBranchVO;
import com.openjiuwen.studio.agent.manager.dto.WorkflowFieldVO;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;
import com.openjiuwen.studio.agent.manager.service.WorkflowManagementService;
import com.openjiuwen.studio.agent.manager.utils.IRUtils;
import com.openjiuwen.studio.agent.manager.utils.JsonUtils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * EI 复杂类型
 *
 */
public class EIComplexIntentNodeAdapter extends IntentDetectionNodeAdapter {
    /**
     * 配置字段名
     */
    private static final String FIELD_CONFIGS = "configs";

    /**
     * 意图 ID
     */
    private static final String FIELD_ID = "id";

    /**
     * 意图 NAME
     */
    private static final String FIELD_NAME = "name";

    /**
     * 意图分类配置字段
     */
    private static final String CATALOG = "catalog";

    /**
     * 意图类别配置字段
     */
    private static final String CATEGORY = "category";

    /**
     * 聚合
     */
    private static final String GROUPS = "groups";

    /**
     * 聚合模式
     */
    private static final String GROUPS_MODEL = "model";

    /**
     * IR聚合模式
     */
    private static final String IR_GROUPS_MODEL = "agg_model";

    /**
     * 分支
     */
    private static final String BRANCHES = "branches";

    /**
     * 类型
     */
    private static final String TYPE = "type";

    /**
     * 工作流id
     */
    private static final String WORKFLOW_ID = "workflow_id";

    /**
     * 工作流version id
     */
    private static final String WORKFLOW_VERSION_ID = "workflow_version_id";

    /**
     * 工作流IR path
     */
    private static final String WORKFLOW_IR_PATH = "ir_path";

    /**
     * 知识库相关信息键值
     */
    private static final String REPOS = "repos";

    /**
     * 意图识别IN3
     */
    private static final String IN3 = "in3";

    /**
     * 意图是否开启辅助识别
     */
    private static final String ENABLE_KNOWLEDGE = "enable_knowledge";

    /**
     * 意图识别 - KG
     */
    private static final String KG = "kg";

    /**
     * 意图容器动作节点ID
     */
    private static final String INTENT_DETECTION_CONTAINER_NODE_ID = "intentDetectionContainerNodeId";

    public Map<String, Object> adapt(WorkflowNodeVO workflowNodeVO, WorkflowNodeVO pair) {
        // 合并输入输出
        if (CollectionUtils.isNotEmpty(pair.getInputs())) {
            workflowNodeVO.getInputs().addAll(pair.getInputs());
        }
        if (CollectionUtils.isNotEmpty(pair.getOutputs())) {
            workflowNodeVO.getOutputs().addAll(pair.getOutputs());
        }
        String intentId = getIntentId(workflowNodeVO);
        String intentRepoId = getIntentRepoId(workflowNodeVO);
        dealComplexIntentRepo(workflowNodeVO, intentId);
        Map<String, Object> intentIr = adapt(workflowNodeVO);

        // 配置group
        refreshConfigs(intentIr, pair);

        // 配置kg
        refreshKgConfigs(intentIr, intentId, intentRepoId);
        intentIr.put(TYPE, NodeType.INTENT_COMPLEX_INTENT.getIrType());
        return intentIr;
    }

    // config字段适配
    private void refreshConfigs(Map<String, Object> ir, WorkflowNodeVO pair) {
        if (ObjectUtils.isEmpty(pair.getConfigs()) || Objects.isNull(pair.getConfigs().get(GROUPS))) {
            return;
        }
        Map<String, Object> dslGroups = JsonUtils.objectToClass(pair.getConfigs().get(GROUPS));
        if (dslGroups.isEmpty()) {
            return;
        }

        Map<String, Object> irGroups = new HashMap<>();
        for (Map.Entry<String, Object> entity : dslGroups.entrySet()) {
            List<Object> fields = JsonUtils.objectToClass(entity.getValue());
            List<Object> irGroup = new ArrayList<>();
            for (Object fieldObj : fields) {
                WorkflowFieldVO field = JsonUtils.objectToClassType(fieldObj, WorkflowFieldVO.class);
                irGroup.add(adaptFieldValue(field, NodeType.INTENT_COMPLEX_INTENT, false));
            }

            // 和输出字段一致， 转驼峰形式
            irGroups.put(IRUtils.adaptKey(entity.getKey()), irGroup);
        }

        Map<String, Object> configs = JsonUtils.objectToClass(ir.get(FIELD_CONFIGS));
        configs.put(GROUPS, irGroups);
        configs.put(IR_GROUPS_MODEL, pair.getConfigs().get(GROUPS_MODEL));
        configs.put(BRANCHES, pair.getBranches().stream().map(this::adaptBranch).collect(Collectors.toList()));
        configs.put(IN3, false);
        configs.put(INTENT_DETECTION_CONTAINER_NODE_ID, pair.getId());

        ir.put(FIELD_CONFIGS, configs);
    }

    private void refreshKgConfigs(Map<String, Object> ir, String intentId, String intentRepoId) {
        if (StringUtils.isEmpty(intentId) || Objects.isNull(ir.get(FIELD_CONFIGS))
            || StringUtils.isEmpty(intentRepoId)) {
            return;
        }

        Map<String, Object> configs = JsonUtils.objectToClass(ir.get(FIELD_CONFIGS));
        if (Objects.isNull(configs.get(KG))) {
            return;
        }

        Map<String, Object> kgs = JsonUtils.objectToClass(configs.get(KG));
        if (Objects.isNull(kgs)) {
            return;
        }

        kgs.put(CATEGORY, intentId);
        kgs.put("scope", "faq");

        configs.put(KG, kgs);
        ir.put(FIELD_CONFIGS, configs);
    }

    // 转换分支
    private Map<String, Object> adaptBranch(WorkflowBranchVO branch) {
        Map<String, Object> workflowBranch = new HashMap<>();
        workflowBranch.put(Constants.ID, branch.getId());

        Map<String, Object> intentBranchConfig = JsonUtils.objectToClass(branch.getConfigs());
        workflowBranch.put(CATALOG, intentBranchConfig.get(CATEGORY));

        // 转换Work flow PATH
        Map<String, String> branchConfigs = new HashMap<>();
        if (!Objects.isNull(intentBranchConfig.get(WORKFLOW_ID))) {
            String workflowId = intentBranchConfig.get(WORKFLOW_ID).toString();
            String versionId = intentBranchConfig.get(WORKFLOW_VERSION_ID).toString();
            branchConfigs.put(WORKFLOW_ID, workflowId);
            WorkflowManagementService wmService = SpringBeanUtils.getBean(WorkflowManagementService.class);
            branchConfigs.put(WORKFLOW_IR_PATH,
                wmService.getWorkflowObsPath(workflowId, CommonConstant.Workflow.IR, versionId));
        }

        workflowBranch.put(FIELD_CONFIGS, branchConfigs);
        return workflowBranch;
    }

    // 复杂意图，转换为知识库模式
    private void dealComplexIntentRepo(WorkflowNodeVO node, String intentId) {
        if (StringUtils.isEmpty(intentId)) {
            return;
        }

        Map<String, String> intents = getIntents(node);
        if (Objects.isNull(intents) || StringUtils.isEmpty(intents.get("repo_id"))) {
            return;
        }
        Map<String, String> repo = new HashMap<>();
        repo.put(FIELD_ID, intents.get("repo_id"));
        repo.put(FIELD_NAME, "");
        repo.put(CATEGORY, intentId);
        node.getConfigs().put(REPOS, Arrays.asList(repo));
        node.getConfigs().put(ENABLE_KNOWLEDGE, true);
    }

    private String getIntentId(WorkflowNodeVO node) {
        Map<String, String> intents = getIntents(node);
        return Objects.isNull(intents) ? null : getIntents(node).get("id");
    }

    private String getIntentRepoId(WorkflowNodeVO node) {
        Map<String, String> intents = getIntents(node);
        return Objects.isNull(intents) ? null : getIntents(node).get("repo_id");
    }

    private Map<String, String> getIntents(WorkflowNodeVO node) {
        String field = "intent";
        if (Objects.isNull(node.getConfigs().get(field))) {
            return null;
        }

        Map<String, String> intents = JsonUtils.objectToClass(node.getConfigs().get(field));
        return intents;
    }
}
