/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.jiuwen.adapt;

import static com.openjiuwen.studio.agent.common.constant.Constants.KnowledgeBase.DEFAULT_FAQ_THRESHOLD;
import static com.openjiuwen.studio.agent.common.constant.Constants.KnowledgeBase.DEFAULT_RECALL_THRESHOLD;
import static com.openjiuwen.studio.agent.common.constant.Constants.KnowledgeBase.DEFAULT_TOP_K;
import static com.openjiuwen.studio.agent.common.constant.Constants.KnowledgeBase.FAQ_THRESHOLD;
import static com.openjiuwen.studio.agent.common.constant.Constants.KnowledgeBase.NEED_EXTRAS_FAQ_SEARCH;
import static com.openjiuwen.studio.agent.common.constant.Constants.KnowledgeBase.RECALL_THRESHOLD;
import static com.openjiuwen.studio.agent.common.constant.Constants.KnowledgeBase.RETRIEVE_IMAGE;
import static com.openjiuwen.studio.agent.common.constant.Constants.KnowledgeBase.SEARCH_MODE;
import static com.openjiuwen.studio.agent.common.constant.Constants.KnowledgeBase.TOP_K;
import static com.openjiuwen.studio.agent.manager.constant.CommonConstant.EXTRA_PARAMS;

import com.fasterxml.jackson.core.type.TypeReference;
import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.common.utils.SpringBeanUtils;
import com.openjiuwen.studio.agent.manager.dto.KnowledgeRetrievePolicy;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;
import com.openjiuwen.studio.agent.manager.entity.KnowledgeRepoEntity;
import com.openjiuwen.studio.agent.manager.service.IrAdapterService;
import com.openjiuwen.studio.agent.manager.utils.JsonUtils;

import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 知识库节点转换IR
 *
 */
public class KnowledgeRepoNodeAdapter extends AbstractIRNodeAdapter {

    @Override
    public Map<String, Object> adaptConfig(WorkflowNodeVO workflowNodeVo) {
        Map<String, Object> nodeConfigs = workflowNodeVo.getConfigs();
        IrAdapterService irAdapterService = SpringBeanUtils.getBean(IrAdapterService.class);

        // 从DSL节点中获取知识库列表
        List<KnowledgeRepoEntity> knowledgeRepoEntities =
            irAdapterService.getReposFromWorkflowNode(nodeConfigs, RequestContextUtils.getRequestProjectId());
        if (!CollectionUtils.isEmpty(knowledgeRepoEntities)) {
            return irAdapterService.parseKnowledgeRepoPlugin(knowledgeRepoEntities,
                getPolicyFromWorkflowNode(nodeConfigs), false);
        }

        return new HashMap<>();
    }

    private KnowledgeRetrievePolicy getPolicyFromWorkflowNode(Map<String, Object> configs) {
        return new KnowledgeRetrievePolicy().setTopK(Objects.isNull(configs.get(TOP_K)) ? DEFAULT_TOP_K : Integer.parseInt(configs.get(TOP_K).toString()))
                .setRecallThreshold(Objects.isNull(configs.get(RECALL_THRESHOLD)) ? DEFAULT_RECALL_THRESHOLD : Float.parseFloat(configs.get(RECALL_THRESHOLD).toString()))
                .setFaqThreshold(Objects.isNull(configs.get(FAQ_THRESHOLD)) ? DEFAULT_FAQ_THRESHOLD : Float.parseFloat(configs.get(FAQ_THRESHOLD).toString()))
                .setExtraParams(Objects.isNull(configs.get(EXTRA_PARAMS)) ? null : JsonUtils.json2Obj(JsonUtils.toJson(configs.get(EXTRA_PARAMS)), new TypeReference<>() {
        }))
                .setSearchMode(Objects.isNull(configs.get(SEARCH_MODE)) ? KnowledgeRetrievePolicy.SearchModeEnum.DOC : KnowledgeRetrievePolicy.SearchModeEnum.fromValue(configs.get(SEARCH_MODE).toString()))
                .setNeedExtrasFaqSearch((Boolean) configs.get(NEED_EXTRAS_FAQ_SEARCH))
                .setRetrieveImage((Boolean) configs.get(RETRIEVE_IMAGE));
    }

    @Override
    public String getNodeType() {
        return NodeType.KNOWLEDGE_REPO.getIrType();
    }
}
