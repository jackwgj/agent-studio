/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.service;

import static com.openjiuwen.studio.prompt.engineering.constant.CommonConstant.NUM_ONE;

import com.alibaba.fastjson.JSON;
import com.openjiuwen.studio.agent.common.dto.simple.SimpleUser;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.prompt.engineering.dto.FileInfoVo;
import com.openjiuwen.studio.prompt.engineering.dto.PePromptDto;
import com.openjiuwen.studio.prompt.engineering.dto.PePromptPageInfo;
import com.openjiuwen.studio.prompt.engineering.dto.PePromptVo;
import com.openjiuwen.studio.prompt.engineering.dto.QueryCondition;
import com.openjiuwen.studio.prompt.engineering.dto.SmallPromptDto;
import com.openjiuwen.studio.prompt.engineering.dto.UpdateOrDeletePromptCheckDto;
import com.openjiuwen.studio.prompt.engineering.dto.VariableDto;
import com.openjiuwen.studio.prompt.engineering.dto.VariableVo;
import com.openjiuwen.studio.prompt.engineering.entity.CandidateVariable;
import com.openjiuwen.studio.prompt.engineering.entity.PeEvaluationTask;
import com.openjiuwen.studio.prompt.engineering.entity.PePrompt;
import com.openjiuwen.studio.prompt.engineering.entity.PeVariable;
import com.openjiuwen.studio.prompt.engineering.entity.SmallPrompt;
import com.openjiuwen.studio.prompt.engineering.entity.TuningVariable;
import com.openjiuwen.studio.prompt.engineering.enums.SourceType;
import com.openjiuwen.studio.prompt.engineering.mapper.PeEvaluationTaskMapper;
import com.openjiuwen.studio.prompt.engineering.mapper.PePromptMapper;
import com.openjiuwen.studio.prompt.engineering.mapper.PeTaskMapper;
import com.openjiuwen.studio.prompt.engineering.mapper.PeVariableMapper;
import com.openjiuwen.studio.prompt.engineering.utils.DateUtil;
import com.openjiuwen.studio.prompt.engineering.utils.ParamBodyUtil;
import com.openjiuwen.studio.prompt.engineering.utils.PromptUtil;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;


@Component
@Slf4j
public class PromptService {
    private static final int VARIABLE_QUOTA = 20;

    private static final int MAX_CANDIDATE_TEMPLATE_NUM = 9;

    @Autowired
    private PeTaskMapper peTaskMapper;

    @Autowired
    private PePromptMapper pePromptMapper;

    @Autowired
    private PeVariableMapper peVariableMapper;

    @Autowired
    private EvaluationMethodService evaluationMethodService;

    @Autowired
    private PeEvaluationTaskMapper peEvaluationTaskMapper;

    @Resource
    private PromptObsService promptObsService;


    @Transactional
    public PePromptVo createOrUpdatePrompt(String projectId, String workspaceId, PePromptDto body) {
        log.info("operation log {} : createOrUpdatePrompt.", projectId);
        PromptUtil.validateModelConfig(body.getModelConfig());
        PromptService promptService = (PromptService) AopContext.currentProxy();

        SimpleUser userInfo = RequestContextUtils.getRequestUser();
        boolean noPromptId = StringUtils.isEmpty(body.getId());
        PePrompt pePrompt = PePrompt.convertToPePrompt(body);
        pePrompt.setWorkspaceId(workspaceId);
        List<VariableDto> variables = body.getVariables();
        ImmutableTriple<List<PeVariable>, List<PeVariable>, List<CandidateVariable>> triple = handleVariable(pePrompt,
            variables, noPromptId);

        if (noPromptId) {
            pePrompt = promptService.create(body, userInfo, triple, variables, workspaceId);
        } else {
            pePrompt = promptService.update(body, userInfo, triple, variables, workspaceId);
        }
        return pePrompt.convertToPePromptVO();
    }

    @Transactional
    public PePrompt create(PePromptDto body, SimpleUser userInfo,
        ImmutableTriple<List<PeVariable>, List<PeVariable>, List<CandidateVariable>> triple,
        List<VariableDto> variables, String workspaceId) {
        checkReq(body, workspaceId);
        checkTemplateNum(body.getTaskId(), workspaceId);
        try {
            PePrompt pePrompt = PePrompt.convertToPePrompt(body);
            pePrompt.setWorkspaceId(workspaceId);
            pePrompt.setCreator(Objects.nonNull(userInfo) ? userInfo.getUserName() : "");
            pePrompt.setUpdater(Objects.nonNull(userInfo) ? userInfo.getUserName() : "");
            if (SourceType.HISTORY.getType().equals(body.getType())) {
                List<TuningVariable> tuningVariables = new ArrayList<>();
                for (VariableDto variable : variables) {
                    tuningVariables.add(TuningVariable.builder()
                        .key(variable.getKey())
                        .name(variable.getName())
                        .value(variable.getValue())
                        .build());
                }
                pePrompt.setVariables(JSON.toJSONString(tuningVariables));
                String taskName = peTaskMapper.queryTaskNameById(pePrompt.getTaskId(), workspaceId);
                if (StringUtils.isEmpty(pePrompt.getName())) {
                    pePrompt.setName(taskName + "-" + DateUtil.timeStampFormat(System.currentTimeMillis()));
                }
                // 先删除统一任务下超多100条部分
                pePromptMapper.deleteRecordsByTaskId(pePrompt.getTaskId(), workspaceId);
                pePromptMapper.savePrompt(pePrompt);
            } else {
                // 保存或者更新变量,数量不超过20
                if (!CollectionUtils.isEmpty(triple.getLeft())) {
                    peVariableMapper.batchInsertVariable(triple.getLeft(), workspaceId);
                }
                for (PeVariable needUpdateVariable : triple.getMiddle()) {
                    peVariableMapper.updateVariable(needUpdateVariable, workspaceId);
                }
                // 保存候选模板
                pePrompt.setVariables(JSON.toJSONString(triple.getRight()));
                pePromptMapper.savePrompt(pePrompt);
                peTaskMapper.raiseIterationNum(pePrompt.getTaskId(), workspaceId);
            }
            return pePrompt;
        } catch (Exception e) {
            throw new AgentStudioException(StudioError.CREATE_TEMPLATE_ERROR);
        }
    }

    @Transactional
    public PePrompt update(PePromptDto body, SimpleUser userInfo,
        ImmutableTriple<List<PeVariable>, List<PeVariable>, List<CandidateVariable>> triple,
        List<VariableDto> variables, String workspaceId) {
        checkReq(body, workspaceId);
        try {
            List<PeVariable> oldPeVariables = Collections.emptyList();
            List<String> needDelete = Collections.emptyList();
            PePrompt pePrompt = PePrompt.convertToPePrompt(body);
            pePrompt.setWorkspaceId(workspaceId);
            pePrompt.setUpdater(Objects.nonNull(userInfo) ? userInfo.getUserName() : "");
            List<String> variableKeys = variables.stream().map(VariableDto::getKey).collect(Collectors.toList());
            // 查询修改前的变量
            PePrompt oldPePrompt = pePromptMapper.queryByPromptId(pePrompt.getId(), workspaceId);
            List<CandidateVariable> oldCandidateVariables = JSON.parseArray(oldPePrompt.getVariables(),
                CandidateVariable.class);
            List<String> collect = oldCandidateVariables.stream()
                .map(CandidateVariable::getId)
                .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(collect)) {
                oldPeVariables = peVariableMapper.queryVariablesById(collect, workspaceId);
                // 计算relationCount需要减一的变量
                needDelete = oldPeVariables.stream()
                    .filter(e -> !variableKeys.contains(e.getKey()))
                    .map(PeVariable::getId)
                    .collect(Collectors.toList());
            }
            // 计算relationCount需要加一的变量
            variableKeys.removeAll(oldPeVariables.stream().map(PeVariable::getKey).collect(Collectors.toList()));
            for (PeVariable needUpdateVariable : triple.getMiddle()) {
                peVariableMapper.updateVariable(needUpdateVariable, needUpdateVariable.getWorkspaceId());
            }
            if (!CollectionUtils.isEmpty(needDelete)) {
                peVariableMapper.batchReduceVariableRelationCount(needDelete, workspaceId);
            }
            peVariableMapper.deleteVariablesByTaskId(pePrompt.getTaskId(), workspaceId);
            if (!CollectionUtils.isEmpty(variableKeys)) {
                peVariableMapper.batchRaiseVariableRelationCount(variableKeys, pePrompt.getTaskId(), workspaceId);
            }
            if (!CollectionUtils.isEmpty(triple.getLeft())) {
                peVariableMapper.batchInsertVariable(triple.getLeft(), workspaceId);
            }
            // 更新候选模板
            pePrompt.setVariables(JSON.toJSONString(triple.getRight()));
            pePromptMapper.updatePrompt(pePrompt);
            return pePrompt;
        } catch (Exception e) {
            throw new AgentStudioException(StudioError.UPDATE_TEMPLATE_ERROR);
        }
    }

    private ImmutableTriple<List<PeVariable>, List<PeVariable>, List<CandidateVariable>> handleVariable(
        PePrompt pePrompt, List<VariableDto> variables, boolean noPromptId) {
        try {
            List<String> existingVariableKeys = peVariableMapper.queryVariableKeysByTaskId(pePrompt.getTaskId(), pePrompt.getWorkspaceId());
            List<String> variableKeys = variables.stream().map(VariableDto::getKey).collect(Collectors.toList());
            HashSet<String> set = new HashSet<>(existingVariableKeys);
            set.addAll(variableKeys);
            if (set.size() > VARIABLE_QUOTA) {
                throw new AgentStudioException(StudioError.VARIABLES_EXCEEDS_QUOTA, String.valueOf(VARIABLE_QUOTA));
            }
            List<VariableDto> needUpdateDTO = variables.stream()
                .filter(e -> existingVariableKeys.contains(e.getKey()))
                .collect(Collectors.toList());

            List<VariableDto> needAddDTO = variables.stream()
                .filter(e -> !existingVariableKeys.contains(e.getKey()))
                .collect(Collectors.toList());
            // 计算变量被多少个模板用到
            Map<String, PeVariable> variableAndKeyMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(variableKeys)) {
                List<PeVariable> peVariables = peVariableMapper.queryVariablesByTaskIdAndKey(variableKeys,
                    pePrompt.getTaskId(), pePrompt.getWorkspaceId());
                variableAndKeyMap = peVariables.stream()
                    .collect(Collectors.toMap(PeVariable::getKey, Function.identity()));
            }
            List<PeVariable> needAddVariables = new ArrayList<>();
            List<CandidateVariable> candidateVariables = new ArrayList<>();
            needAddDTO.forEach(e -> {
                PeVariable peVariable = new PeVariable();
                String variableId = UUID.randomUUID().toString();
                peVariable.setId(variableId);
                peVariable.setName(e.getName());
                peVariable.setKey(e.getKey());
                peVariable.setPeTaskId(pePrompt.getTaskId());
                peVariable.setRelationCount(1);
                peVariable.setCreateTime(System.currentTimeMillis());
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ex) {
                    log.error("sleep error!");
                }
                needAddVariables.add(peVariable);
                CandidateVariable candidateVariable = new CandidateVariable();
                candidateVariable.setId(variableId);
                candidateVariable.setValue(e.getValue());
                candidateVariables.add(candidateVariable);
            });

            List<PeVariable> needUpdateVariables = new ArrayList<>();
            for (VariableDto variableDto : needUpdateDTO) {
                PeVariable peVariable = new PeVariable();
                peVariable.setKey(variableDto.getKey());
                peVariable.setName(variableDto.getName());
                peVariable.setPeTaskId(pePrompt.getTaskId());
                if (noPromptId) {
                    peVariable.setRelationCount(variableAndKeyMap.get(variableDto.getKey()).getRelationCount() + 1);
                } else {
                    peVariable.setRelationCount(variableAndKeyMap.get(variableDto.getKey()).getRelationCount());
                }
                needUpdateVariables.add(peVariable);
                CandidateVariable candidateVariable = new CandidateVariable();
                candidateVariable.setId(variableAndKeyMap.get(variableDto.getKey()).getId());
                candidateVariable.setValue(variableDto.getValue());
                candidateVariables.add(candidateVariable);
            }
            return new ImmutableTriple<>(needAddVariables, needUpdateVariables, candidateVariables);
        } catch (Exception e) {
            throw new AgentStudioException(StudioError.HANDLE_VARIABLES_ERROR);
        }
    }


    @Transactional
    public String deletePrompt(String projectId, String promptId, String workspaceId) {
        log.info("operation log {} : deletePrompt.", projectId);
        // 删除候选模板后，需要重新计算工程任务下的变量绑定模板的数量，如果为0需要将变量也删除
        PePrompt pePrompt = pePromptMapper.queryByPromptId(promptId, workspaceId);
        if (pePrompt == null) {
            throw new AgentStudioException(StudioError.DATA_NOT_EXIST);
        }
        List<PeEvaluationTask> evaluationTasks = peEvaluationTaskMapper.queryByTaskIdAndStatus(pePrompt.getTaskId(), workspaceId);
        List<String> promptIds = evaluationTasks.stream()
            .map(PeEvaluationTask::getPrompts)
            .flatMap(Collection::stream)
            .map(PePrompt::getId)
            .collect(Collectors.toList());
        if (promptIds.contains(promptId)) {
            throw new AgentStudioException(StudioError.PROMPT_IN_USE);
        }
        try {
            String variables = pePrompt.getVariables();
            List<CandidateVariable> candidateVariables = JSON.parseArray(variables, CandidateVariable.class);
            for (CandidateVariable candidateVariable : candidateVariables) {
                peVariableMapper.updateVariableRelationCount(candidateVariable.getId(), workspaceId);
            }
            peVariableMapper.deleteVariablesByTaskId(pePrompt.getTaskId(), workspaceId);
            pePromptMapper.deleteByPromptId(promptId, workspaceId);
            return HttpStatus.OK.toString();
        } catch (Exception e) {
            throw new AgentStudioException(StudioError.DELETE_TEMPLATE_ERROR);
        }
    }


    public PePromptPageInfo listPrompts(String projectId, String workspaceId, QueryCondition body) {
        log.info("operation log {} : listPrompts.", projectId);
        try {
            // 校验taskId是否存在
            if (!Boolean.TRUE.equals(peTaskMapper.isIdExist(body.getTaskId(), workspaceId))) {
                throw new AgentStudioException(StudioError.TASK_ID_IS_NOT_EXIST, body.getTaskId());
            }
            List<PePrompt> pePrompts = pePromptMapper.queryByCondition(body, workspaceId);
            Map<String, Integer> promptIdAndScoreMap = promptIdAndScoreMap(body.getTaskId(), workspaceId);
            List<PePromptVo> pePromptVos = new ArrayList<>();

            if (SourceType.HISTORY.getType().equals(body.getType())) {
                for (PePrompt pePrompt : pePrompts) {
                    PePromptVo pePromptVO = pePrompt.convertToPePromptVO();
                    String variables = pePrompt.getVariables();
                    List<TuningVariable> tuningVariables = JSON.parseArray(variables, TuningVariable.class);
                    ArrayList<VariableVo> variableVos = new ArrayList<>();
                    if (!CollectionUtils.isEmpty(tuningVariables)) {
                        for (TuningVariable tuningVariable : tuningVariables) {
                            VariableVo variableVo = new VariableVo();
                            variableVo.setKey(tuningVariable.getKey());
                            variableVo.setValue(tuningVariable.getValue());
                            variableVo.setName(tuningVariable.getName());
                            variableVos.add(variableVo);
                        }
                    }
                    pePromptVO.setFileInfo(getFileInfoVoByFileId(pePrompt.getFileId()));
                    pePromptVO.setVariableVo(variableVos);
                    pePromptVos.add(pePromptVO);
                }
            } else {
                for (PePrompt pePrompt : pePrompts) {
                    PePromptVo pePromptVO = pePrompt.convertToPePromptVO();
                    String variables = pePrompt.getVariables();
                    List<CandidateVariable> candidateVariables = JSON.parseArray(variables, CandidateVariable.class);
                    List<String> variableIds = candidateVariables.stream()
                        .map(CandidateVariable::getId)
                        .collect(Collectors.toList());
                    pePromptVO.setVariableVo(Collections.emptyList());
                    if (!CollectionUtils.isEmpty(variableIds)) {
                        List<PeVariable> peVariables = peVariableMapper.selectVariablesByIds(variableIds, workspaceId);
                        List<VariableVo> VariableVOs = peVariables.stream()
                            .map(PeVariable::convertToVariableVo)
                            .collect(Collectors.toList());
                        pePromptVO.setVariableVo(VariableVOs);
                    }
                    pePromptVO.setEvaluationAvg(promptIdAndScoreMap.getOrDefault(pePrompt.getId(), -1));
                    pePromptVO.setFileInfo(getFileInfoVoByFileId(pePrompt.getFileId()));
                    pePromptVos.add(pePromptVO);
                }
            }

            PePromptPageInfo pePromptVOPager = new PePromptPageInfo();
            pePromptVOPager.setData(pePromptVos);
            pePromptVOPager.setTotalPage(1);
            pePromptVOPager.setCount(pePromptVos.size());
            pePromptVOPager.setHasNextPage(false);
            return pePromptVOPager;
        } catch (Exception e) {
            log.error("listPrompts error: {}", e.getMessage());
            throw new AgentStudioException(StudioError.QUERY_TEMPLATE_ERROR);
        }
    }


    @Transactional
    public String updatePrompt(String projectId, String workspaceId, PePromptDto body) {
        log.info("operation log {} : updatePrompt.", projectId);
        PromptUtil.validateModelConfig(body.getModelConfig());
        checkReq(body, workspaceId);

        SimpleUser userInfo = RequestContextUtils.getRequestUser();

        try {
            PePrompt pePrompt = PePrompt.convertToPePrompt(body);
            pePrompt.setWorkspaceId(workspaceId);
            pePrompt.setUpdater(Objects.nonNull(userInfo) ? userInfo.getUserName() : "");
            List<VariableDto> variables = body.getVariables();
            if (SourceType.HISTORY.getType().equals(body.getType())) {
                PromptService promptService = (PromptService) AopContext.currentProxy();
                promptService.saveHistory(pePrompt, variables);
            } else {
                ImmutableTriple<List<PeVariable>, List<PeVariable>, List<CandidateVariable>> triple = handleVariable(
                    pePrompt, variables, false);
                if (!CollectionUtils.isEmpty(triple.getLeft())) {
                    peVariableMapper.batchInsertVariable(triple.getLeft(), workspaceId);
                }
                for (PeVariable needUpdateVariable : triple.getMiddle()) {
                    peVariableMapper.updateVariable(needUpdateVariable, needUpdateVariable.getWorkspaceId());
                }
                // 更新候选模板
                pePrompt.setVariables(JSON.toJSONString(triple.getRight()));
                pePromptMapper.updatePrompt(pePrompt);
            }
            return HttpStatus.OK.toString();
        } catch (Exception e) {
            throw new AgentStudioException(StudioError.UPDATE_TEMPLATE_ERROR);
        }
    }


    @Transactional
    public String updateSamllPrompt(String projectId, String workspaceId, SmallPromptDto body) {
        log.info("operation log {} : updateSamllPrompt.", projectId);
        try {
            SmallPrompt prompt = new SmallPrompt();
            SmallPrompt smallPrompt = prompt.convertToSmallPrompt(body);
            if (StringUtils.isNotEmpty(smallPrompt.getName()) || smallPrompt.getManualScore() != null) {
                Integer cnt = pePromptMapper.updateSmallPrompt(smallPrompt, workspaceId);
                if (cnt < NUM_ONE) {
                    throw new AgentStudioException(StudioError.DATA_NOT_EXIST);
                }
                if (pePromptMapper.checkDuplicateName(smallPrompt.getName(), smallPrompt.getId(), workspaceId) > 1) {
                    throw new AgentStudioException(StudioError.DUPLICATE_PROMPT_NAME);
                }
            } else {
                throw new AgentStudioException(StudioError.UPDATE_TEMPLATE_ERROR_PARAM);
            }
            return HttpStatus.OK.toString();
        } catch (AgentStudioException e) {
            log.error("Failed to update the template. error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to update the template. error: {}", e.getMessage());
            throw new AgentStudioException(StudioError.UPDATE_TEMPLATE_ERROR);
        }
    }

    private void checkReq(PePromptDto body, String workspaceId) {
        // 检查请求体中变量key是否有重复
        ParamBodyUtil.checkUniqueVariable(body.getVariables());
        // 校验taskId是否存在
        if (!Boolean.TRUE.equals(peTaskMapper.isIdExist(body.getTaskId(), workspaceId))) {
            throw new AgentStudioException(StudioError.TASK_ID_IS_NOT_EXIST, body.getTaskId());
        }
        // 校验模板名称不可以重复
        if (Boolean.TRUE.equals(pePromptMapper.isNameExist(body.getName(), body.getTaskId(), body.getId(), workspaceId))) {
            throw new AgentStudioException(StudioError.NAME_IS_EXIST);
        }
    }

    private void checkTemplateNum(String taskId, String workspaceId) {
        // 校验当前工程任务下的候选模板数 最多保存9个
        int promptNum = pePromptMapper.countCandidateTemplateByTaskId(taskId, workspaceId);
        if (promptNum >= MAX_CANDIDATE_TEMPLATE_NUM) {
            throw new AgentStudioException(StudioError.CANDIDATE_TEMPLATE_EXCEEDS_QUOTA, String.valueOf(
                MAX_CANDIDATE_TEMPLATE_NUM));
        }
    }

    @Transactional
    public void saveHistory(PePrompt pePrompt, List<VariableDto> variables) {
        try {
            List<TuningVariable> tuningVariables = new ArrayList<>();
            for (VariableDto variable : variables) {
                tuningVariables.add(TuningVariable.builder()
                    .key(variable.getKey())
                    .name(variable.getName())
                    .value(variable.getValue())
                    .build());
            }
            pePrompt.setVariables(JSON.toJSONString(tuningVariables));
            String taskName = peTaskMapper.queryTaskNameById(pePrompt.getTaskId(), pePrompt.getWorkspaceId());
            if (StringUtils.isEmpty(pePrompt.getName())) {
                pePrompt.setName(taskName + "-" + DateUtil.timeStampFormat(System.currentTimeMillis()));
            }
            // 先删除统一任务下超多100条部分
            pePromptMapper.deleteRecordsByTaskId(pePrompt.getTaskId(), pePrompt.getWorkspaceId());
            pePromptMapper.savePrompt(pePrompt);
        } catch (Exception e) {
            throw new AgentStudioException(StudioError.SAVE_HISTORY_ERROR);
        }
    }

    /**
     * 将变量转为指定格式
     *
     * @param variables 变量字符串
     * @return list
     */
    public List<VariableVo> getVariableVos(String variables, String workspaceId) {
        List<VariableVo> variableVOs = new ArrayList<>();
        List<CandidateVariable> candidateVariables = JSON.parseArray(variables, CandidateVariable.class);
        List<String> variableIds = candidateVariables.stream()
            .map(CandidateVariable::getId)
            .collect(Collectors.toList());
        Map<String, String> lookup = candidateVariables.stream()
            .collect(HashMap::new, (map, item) -> map.put(item.getId(), item.getValue()), HashMap::putAll);
        if (!CollectionUtils.isEmpty(variableIds)) {
            List<PeVariable> peVariables = peVariableMapper.selectVariablesByIds(variableIds, workspaceId);
            peVariables.forEach(item -> {
                VariableVo variableVo = item.convertToVariableVo();
                variableVo.setValue(lookup.get(variableVo.getId()));
                variableVOs.add(variableVo);
            });
        }
        return variableVOs;
    }

    private Map<String, Integer> promptIdAndScoreMap(String taskId, String workspaceId) {
        // 查询最新的评估任务ID
        String evaluationTaskId = peEvaluationTaskMapper.queryLatestSuccessEvaluationTaskIdByTaskId(taskId, workspaceId);
        Map<String, Integer> promptScoreMap = new HashMap<>();
        if (StringUtils.isNotEmpty(evaluationTaskId)) {
            promptScoreMap = evaluationMethodService.graspPromptScore(
                peEvaluationTaskMapper.queryEvalResultsByEvalTaskId(evaluationTaskId, workspaceId));
        }
        return promptScoreMap;
    }


    public String checkUpdateOrDelete(String projectId, String workspaceId, UpdateOrDeletePromptCheckDto body) {
        log.info("operation log {} : checkUpdateOrDelete", projectId);
        if (!Boolean.TRUE.equals(peTaskMapper.isIdExist(body.getTaskId(), workspaceId))) {
            throw new AgentStudioException(StudioError.TASK_ID_IS_NOT_EXIST, body.getTaskId());
        }
        if (null == pePromptMapper.queryByPromptId(body.getPromptId(), workspaceId)) {
            throw new AgentStudioException(StudioError.PROMPT_ID_IS_NOT_EXISTED);
        }
        List<PeEvaluationTask> evaluationTasks = peEvaluationTaskMapper.queryByTaskIdAndStatus(body.getTaskId(), workspaceId);
        List<String> promptIds = evaluationTasks.stream()
            .map(PeEvaluationTask::getPrompts)
            .flatMap(Collection::stream)
            .map(PePrompt::getId)
            .collect(Collectors.toList());

        if (promptIds.contains(body.getPromptId())) {
            throw new AgentStudioException(StudioError.PROMPT_IN_USE);
        }
        return HttpStatus.OK.toString();
    }


    public PePromptVo queryPrompt(String projectId, String id, String workspaceId) {
        log.info("operation log {} : queryPrompt", projectId);
        try {
            PePrompt pePrompt = pePromptMapper.queryByPromptId(id, workspaceId);
            PePromptVo pePromptVO = pePrompt.convertToPePromptVO();
            List<VariableVo> variableVOs = getVariableVos(pePrompt.getVariables(), workspaceId);
            pePromptVO.setVariableVo(variableVOs);
            pePromptVO.setFileInfo(getFileInfoVoByFileId(pePrompt.getFileId()));
            return pePromptVO;
        } catch (Exception e) {
            throw new AgentStudioException(StudioError.QUERY_TEMPLATE_ERROR);
        }
    }

    /**根据图片id获取临时路径
     *
     * @param fileId 上传图片id
     * @return 文件包装类
     */
    public FileInfoVo getFileInfoVoByFileId(String fileId) {
        if(StringUtils.isEmpty(fileId))  {
            return new FileInfoVo();
        }
        String objectKey = String.format("%s/%s", "image", fileId);
        String tempUrl = promptObsService.getObsTempUrl(objectKey, 3600L);
        return new FileInfoVo().setFileId(fileId).setTempUrl(tempUrl);
    }

}
