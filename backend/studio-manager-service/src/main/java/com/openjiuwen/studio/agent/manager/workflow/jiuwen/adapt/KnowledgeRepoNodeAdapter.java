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
import static com.openjiuwen.studio.agent.common.constant.Constants.KnowledgeBase.SHOW_SOURCE;
import static com.openjiuwen.studio.agent.common.constant.Constants.KnowledgeBase.TOP_K;

import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.common.utils.SpringBeanUtils;
import com.openjiuwen.studio.agent.manager.dto.KnowledgeRetrievePolicy;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;
import com.openjiuwen.studio.agent.manager.entity.KnowledgeRepoEntity;
import com.openjiuwen.studio.agent.manager.service.IrAdapterService;

import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 知识库节点转换IR
 * <p>
 * 生成的IR结构：
 * <pre>
 * configs: {
 *   "connectionId": "conn_xxx",
 *   "knowledgeBaseIds": ["kb_1", "kb_2"],
 *   "retrievalConfig": {
 *     "topK": 5,
 *     "scoreThreshold": 0.5,
 *     "searchMode": "hybrid"
 *   }
 * }
 * </pre>
 * 消费者通过 connectionId 从 OBS 获取连接信息，
 * 通过 knowledgeBaseIds 从 OBS 知识库文件获取 externalId（即 LakeSearch repo_id）。
 * </p>
 */
public class KnowledgeRepoNodeAdapter extends AbstractIRNodeAdapter {

    @Override
    public Map<String, Object> adaptConfig(WorkflowNodeVO workflowNodeVo) {
        Map<String, Object> nodeConfigs = workflowNodeVo.getConfigs();
        IrAdapterService irAdapterService = SpringBeanUtils.getBean(IrAdapterService.class);

        // 从DSL节点中获取知识库列表
        List<KnowledgeRepoEntity> knowledgeRepoEntities =
            irAdapterService.getReposFromWorkflowNode(nodeConfigs, RequestContextUtils.getRequestProjectId());

        Map<String, Object> configMap = new HashMap<>();

        if (!CollectionUtils.isEmpty(knowledgeRepoEntities)) {
            // 提取connectionId（同一节点下的知识库共享同一个连接）
            String connectionId = knowledgeRepoEntities.get(0).getConnectionId();
            configMap.put("connectionId", connectionId);

            // 提取knowledgeBaseIds列表
            List<String> knowledgeBaseIds = knowledgeRepoEntities.stream()
                .map(KnowledgeRepoEntity::getKnowledgeRepoId)
                .collect(Collectors.toList());
            configMap.put("knowledgeBaseIds", knowledgeBaseIds);

            // 构建retrievalConfig（全量透传，键名为 camelCase，与 Python 消费端对齐）
            KnowledgeRetrievePolicy policy = getPolicyFromWorkflowNode(nodeConfigs);
            Map<String, Object> retrievalConfig = new HashMap<>();
            retrievalConfig.put("topK", policy.getTopK());
            // scoreThreshold 供适配器发起检索请求使用，取召回阈值
            retrievalConfig.put("scoreThreshold", policy.getRecallThreshold());
            // recallThreshold 供节点层结果后过滤使用（Python 优先读此键，避免回退到 scoreThreshold）
            retrievalConfig.put("recallThreshold", policy.getRecallThreshold());
            retrievalConfig.put("searchMode", policy.getSearchMode() != null
                ? policy.getSearchMode().toString() : KnowledgeRetrievePolicy.SearchModeEnum.DOC.toString());
            // FAQ 优先检索开关及其直出阈值
            retrievalConfig.put("needExtrasFaqSearch", policy.isNeedExtrasFaqSearch());
            retrievalConfig.put("faqThreshold", policy.getFaqThreshold());
            // 图片检索 / 来源展示开关，透传给消费端
            retrievalConfig.put("retrieveImage", policy.isRetrieveImage());
            retrievalConfig.put("showSource", policy.isShowSource());
            List<String> tags = knowledgeRepoEntities.stream()
                .filter(item -> !CollectionUtils.isEmpty(item.getTag()))
                .flatMap(item -> item.getTag().stream())
                .collect(Collectors.toCollection(ArrayList::new));
            if (!CollectionUtils.isEmpty(tags)) {
                retrievalConfig.put("tags", tags);
            }
            configMap.put("retrievalConfig", retrievalConfig);
        }

        return configMap;
    }

    private KnowledgeRetrievePolicy getPolicyFromWorkflowNode(Map<String, Object> configs) {
        return new KnowledgeRetrievePolicy().setTopK(Objects.isNull(configs.get(TOP_K)) ? DEFAULT_TOP_K : Integer.parseInt(configs.get(TOP_K).toString()))
                .setRecallThreshold(Objects.isNull(configs.get(RECALL_THRESHOLD)) ? DEFAULT_RECALL_THRESHOLD : Float.parseFloat(configs.get(RECALL_THRESHOLD).toString()))
                .setFaqThreshold(Objects.isNull(configs.get(FAQ_THRESHOLD)) ? DEFAULT_FAQ_THRESHOLD : Float.parseFloat(configs.get(FAQ_THRESHOLD).toString()))
                .setSearchMode(Objects.isNull(configs.get(SEARCH_MODE)) ? KnowledgeRetrievePolicy.SearchModeEnum.DOC : KnowledgeRetrievePolicy.SearchModeEnum.fromValue(configs.get(SEARCH_MODE).toString()))
                .setNeedExtrasFaqSearch(parseBooleanConfig(configs.get(NEED_EXTRAS_FAQ_SEARCH), false))
                .setRetrieveImage(parseBooleanConfig(configs.get(RETRIEVE_IMAGE), false))
                .setShowSource(parseBooleanConfig(configs.get(SHOW_SOURCE), false));
    }

    /**
     * 安全解析 DSL 中的布尔配置项，缺省或非法时返回默认值，避免直接强转 (Boolean) 造成的 NPE。
     */
    private boolean parseBooleanConfig(Object value, boolean defaultValue) {
        if (Objects.isNull(value)) {
            return defaultValue;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(value.toString());
    }

    @Override
    public String getNodeType() {
        return NodeType.KNOWLEDGE_REPO.getIrType();
    }
}
