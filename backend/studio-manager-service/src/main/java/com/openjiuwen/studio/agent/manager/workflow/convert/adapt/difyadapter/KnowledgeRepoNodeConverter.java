/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.workflow.convert.adapt.difyadapter;

import com.openjiuwen.studio.agent.common.constant.Constants;
import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.common.enums.VariableEnum;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.dto.KnowledgeRetrievePolicy;
import com.openjiuwen.studio.agent.manager.dto.WorkflowFieldVO;
import com.openjiuwen.studio.agent.manager.dto.WorkflowFieldVOValue;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;
import com.openjiuwen.studio.agent.manager.workflow.convert.adapt.enums.OutputParamsDiffType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class KnowledgeRepoNodeConverter extends AbstractSDSLNodeConverter {

    // 判断是否是支持的类型
    @Override
    public Boolean supportNodeType(NodeType nodeType) {
        return NodeType.KNOWLEDGE_REPO.equals(nodeType);
    }

    // 节点转换
    @Override
    public WorkflowNodeVO adapt(Map<String, Object> data, WorkflowNodeVO workflowNodeVO, Map<String, WorkflowNodeVO> workflowNodeMap) {
        workflowNodeVO.setType(NodeType.KNOWLEDGE_REPO.getType());
        workflowNodeVO.setInputs(adaptInputs(data, workflowNodeMap, workflowNodeVO));
        workflowNodeVO.setOutputs(adaptOutputs(data));
        workflowNodeVO.setConfigs(adaptConfigs(data));
        return workflowNodeVO;
    }

    // 配置适配
    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> adaptConfigs(Map<String, Object> data) {
        Map<String, Object> configs = new HashMap<>();
        configs.put("show_source", false);
        configs.put(CommonConstant.DIFY.CONFIG_DEFAULT_NAME, false);
        configs.put(CommonConstant.REPOS, List.of());
        configs.put(Constants.KnowledgeBase.NEED_EXTRAS_FAQ_SEARCH, false);
        configs.put(Constants.KnowledgeBase.FAQ_THRESHOLD, Constants.KnowledgeBase.DEFAULT_FAQ_THRESHOLD);
        configs.put(Constants.KnowledgeBase.RETRIEVE_IMAGE, false);

        // 设置top_k
        configs.put(Constants.KnowledgeBase.TOP_K, Optional.ofNullable((Map<String, Object>) data.get("multiple_retrieval_config"))
                .map(multipleRetrievalConfig -> multipleRetrievalConfig.get("top_k"))
                .orElse(Constants.KnowledgeBase.DEFAULT_TOP_K));

        // 设置recall_threshold
        configs.put(Constants.KnowledgeBase.RECALL_THRESHOLD, Optional.ofNullable((Map<String, Object>) data.get("multiple_retrieval_config"))
                .map(multipleRetrievalConfig -> multipleRetrievalConfig.get("score_threshold"))
                .orElse(Constants.KnowledgeBase.DEFAULT_RECALL_THRESHOLD));

        // 设置search_mode
        configs.put(Constants.KnowledgeBase.SEARCH_MODE, Optional.ofNullable((Map<String, Object>) data.get("multiple_retrieval_config"))
                .map(multipleRetrievalConfig -> (Map<String, Object>) multipleRetrievalConfig.get("weights"))
                .map(weights -> (Map<String, Number>) weights.get("keyword_setting"))
                .map(keywordSetting -> keywordSetting.get("keyword_weight").doubleValue())
                .map(this::determineSearchMode)
                .orElse(KnowledgeRetrievePolicy.SearchModeEnum.KEYWORD));

        return configs;
    }

    private KnowledgeRetrievePolicy.SearchModeEnum determineSearchMode(double keywordWeight) {
        if (keywordWeight == 1.0d) {
            return KnowledgeRetrievePolicy.SearchModeEnum.KEYWORD;
        }
        if (keywordWeight == 0.0d) {
            return KnowledgeRetrievePolicy.SearchModeEnum.DOC;
        }
        return KnowledgeRetrievePolicy.SearchModeEnum.MIX;
    }

    // 输入适配
    @SuppressWarnings("unchecked")
    public List<WorkflowFieldVO> adaptInputs(Map<String, Object> data, Map<String, WorkflowNodeVO> workflowNodeMap, WorkflowNodeVO workflowNodeVO) {

        WorkflowFieldVO workflowFieldVO = new WorkflowFieldVO()
                .setSource(WorkflowFieldVO.SourceEnum.USER)
                .setName("query")
                .setType("string")
                .setDescription("检索query")
                .setReflection(false)
                .setRequired(true)
                .setValue(new WorkflowFieldVOValue()
                        .setType(WorkflowFieldVOValue.TypeEnum.GENERATED)
                        .setContent(null));

        // 检查是否配置变量
        Optional.ofNullable((List<String>) data.get("query_variable_selector"))
                .filter(variable -> !variable.isEmpty())
                .ifPresent(variable -> workflowFieldVO.setValue(
                        adaptWorkflowFieldVoValue(variable, workflowNodeMap, workflowNodeVO)
                ));

        return List.of(workflowFieldVO);
    }

    // 输出适配
    @Override
    public List<WorkflowFieldVO> adaptOutputs(Map<String, Object> data) {

        // 准备数据
        WorkflowFieldVO documentName = new WorkflowFieldVO()
                .setName("document_name")
                .setDescription("文件名")
                .setRequired(true)
                .setSource(WorkflowFieldVO.SourceEnum.USER)
                .setType(VariableEnum.STRING.getDslType())
                .setValue(new WorkflowFieldVOValue()
                        .setType(WorkflowFieldVOValue.TypeEnum.GENERATED)
                        .setContent(""));
        WorkflowFieldVO subTitle = new WorkflowFieldVO()
                .setName("subtitle")
                .setDescription("子标题，文档段落信息")
                .setRequired(true)
                .setSource(WorkflowFieldVO.SourceEnum.USER)
                .setType(VariableEnum.STRING.getDslType())
                .setValue(new WorkflowFieldVOValue()
                        .setType(WorkflowFieldVOValue.TypeEnum.GENERATED)
                        .setContent(""));
        WorkflowFieldVO content = new WorkflowFieldVO()
                .setName("content")
                .setDescription("检索内容")
                .setRequired(true)
                .setSource(WorkflowFieldVO.SourceEnum.USER)
                .setType(VariableEnum.STRING.getDslType())
                .setValue(new WorkflowFieldVOValue()
                        .setType(WorkflowFieldVOValue.TypeEnum.GENERATED)
                        .setContent(""));
        WorkflowFieldVO score = new WorkflowFieldVO()
                .setName("score")
                .setDescription("匹配度得分")
                .setRequired(true)
                .setSource(WorkflowFieldVO.SourceEnum.USER)
                .setType(VariableEnum.NUMBER.getDslType())
                .setValue(new WorkflowFieldVOValue()
                        .setType(WorkflowFieldVOValue.TypeEnum.GENERATED)
                        .setContent(""));
        WorkflowFieldVO knowledgeBaseId = new WorkflowFieldVO()
                .setName("knowledge_base_id")
                .setDescription("知识库ID")
                .setRequired(true)
                .setSource(WorkflowFieldVO.SourceEnum.USER)
                .setType(VariableEnum.STRING.getDslType())
                .setValue(new WorkflowFieldVOValue()
                        .setType(WorkflowFieldVOValue.TypeEnum.GENERATED)
                        .setContent(""));
        WorkflowFieldVO fileId = new WorkflowFieldVO()
                .setName("file_id")
                .setDescription("文件ID")
                .setRequired(true)
                .setSource(WorkflowFieldVO.SourceEnum.USER)
                .setType(VariableEnum.STRING.getDslType())
                .setValue(new WorkflowFieldVOValue()
                        .setType(WorkflowFieldVOValue.TypeEnum.GENERATED)
                        .setContent(""));
        WorkflowFieldVO knowledgeBaseType = new WorkflowFieldVO()
                .setName("knowledge_base_type")
                .setDescription("知识库类型")
                .setRequired(true)
                .setSource(WorkflowFieldVO.SourceEnum.USER)
                .setType(VariableEnum.STRING.getDslType())
                .setValue(new WorkflowFieldVOValue()
                        .setType(WorkflowFieldVOValue.TypeEnum.GENERATED)
                        .setContent(""));

        // 设置返回的格式
        List<WorkflowFieldVO> schemaList = List.of(documentName, subTitle, content,
                score, knowledgeBaseId, fileId, knowledgeBaseType);

        // 设置schema
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("name", "");
        schema.put("schema", schemaList);

        // 返回
        return List.of(new WorkflowFieldVO()
                .setName(OutputParamsDiffType.DIFY_KNOWLEDGE_REPO_RESULT.getConvertName())
                .setDescription("查询结果列表")
                .setReflection(false)
                .setRequired(true)
                .setSchema(schema)
                .setType("array")
                .setSource(WorkflowFieldVO.SourceEnum.USER)
                .setValue(new WorkflowFieldVOValue()
                        .setType(WorkflowFieldVOValue.TypeEnum.GENERATED)
                        .setContent("")));
    }
}
