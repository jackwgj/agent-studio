/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.jiuwen.adapt;

import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.manager.dto.WorkflowEdgeVO;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 工作流节点连线转换IR
 *
 */
public class IREdgeAdapter {
    /**
     * 分支节点分支模板
     */
    private static final String BRANCH_FORMAT = "%s-%s";

    /**
     * 意图节点分支模板
     */
    private static final String INTENT_FORMAT = "%s_%s";

    /**
     * 异常节点分支模板
     */
    private static final String EXCEPTION_FORMAT = "%s%s";

    /**
     * 起始节点
     */
    private static final String SOURCE = "source";

    /**
     * 结束节点
     */
    private static final String TARGET = "target";

    /**
     * 节点id
     */
    private static final String COMPONENT_ID = "componentId";

    /**
     * 分支id
     */
    private static final String BRANCH_ID = "branchId";

    /**
     * 并行分支id
     */
    private static final String PARALLEL_BRANCH_ID = "parallelBranchId";

    Map<String, Object> adaptEdge(WorkflowEdgeVO workflowEdge, Map<String, String> reflection,
        Map<String, String> nodePairReverses) {
        Map<String, Object> connection = new HashMap<>();
        Map<String, Object> source = new HashMap<>();
        Map<String, Object> target = new HashMap<>();

        // 成对节点之间的连线去掉
        if (!Objects.isNull(nodePairReverses) && nodePairReverses.containsKey(workflowEdge.getTarget())
            && nodePairReverses.get(workflowEdge.getTarget()).equals(workflowEdge.getSource())) {
            return connection;
        }

        // 如果分支节点拼接ID, 此处新增逻辑，分支与意图节点拼接模板不同
        if (StringUtils.isNotEmpty(workflowEdge.getBranch())) {
            String branchFormat;
            if (Boolean.TRUE.equals(workflowEdge.isExceptionBranch())) {
                branchFormat = EXCEPTION_FORMAT;
            } else {
                branchFormat = Strings.CS.equals(reflection.get(workflowEdge.getSource()), NodeType.BRANCH.getType())
                    ? BRANCH_FORMAT : INTENT_FORMAT;
            }
            source.put(BRANCH_ID, String.format(branchFormat, workflowEdge.getSource(), workflowEdge.getBranch()));
        }
        if (StringUtils.isNotEmpty(workflowEdge.getSource())) {
            // 转移锚点
            source.put(COMPONENT_ID,
                !Objects.isNull(nodePairReverses) && nodePairReverses.containsKey(workflowEdge.getSource())
                    ? nodePairReverses.get(workflowEdge.getSource()) : workflowEdge.getSource());
            connection.put(SOURCE, source);
        }
        if (StringUtils.isNotEmpty(workflowEdge.getTarget())) {
            target.put(COMPONENT_ID, workflowEdge.getTarget());
            connection.put(TARGET, target);
        }

        // 添加parallelBranchId
        if (StringUtils.isNotEmpty(workflowEdge.getSourceBranchId())) {
            source.put(PARALLEL_BRANCH_ID, workflowEdge.getSourceBranchId());
        }
        if (StringUtils.isNotEmpty(workflowEdge.getTargetBranchId())) {
            target.put(PARALLEL_BRANCH_ID, workflowEdge.getTargetBranchId());
        }

        return connection;
    }
}
