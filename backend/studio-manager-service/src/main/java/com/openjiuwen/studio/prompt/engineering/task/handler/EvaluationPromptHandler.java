/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.task.handler;

import static com.openjiuwen.studio.prompt.engineering.constant.CommonConstant.FOLDER_SEPARATOR;
import static com.openjiuwen.studio.prompt.engineering.constant.CommonConstant.NUM_MINUS_ONE;
import static com.openjiuwen.studio.prompt.engineering.constant.CommonConstant.NUM_ONE;
import static com.openjiuwen.studio.prompt.engineering.constant.CommonConstant.NUM_ZERO;

import com.openjiuwen.studio.prompt.engineering.constant.CommonConstant;
import com.openjiuwen.studio.prompt.engineering.dto.InvokeResp;
import com.openjiuwen.studio.prompt.engineering.dto.PeEvalResult;
import com.openjiuwen.studio.prompt.engineering.dto.PromptBody;
import com.openjiuwen.studio.prompt.engineering.dto.VariableDto;
import com.openjiuwen.studio.prompt.engineering.entity.PeEvaluationResult;
import com.openjiuwen.studio.prompt.engineering.entity.PeEvaluationTask;
import com.openjiuwen.studio.prompt.engineering.entity.PePrompt;
import com.openjiuwen.studio.prompt.engineering.enums.EvaluationTaskStatus;
import com.openjiuwen.studio.prompt.engineering.mapper.DatasetMapper;
import com.openjiuwen.studio.prompt.engineering.mapper.PeEvaluationTaskMapper;
import com.openjiuwen.studio.prompt.engineering.service.EvaluationMethodService;
import com.openjiuwen.studio.prompt.engineering.service.model.ModelServiceCollection;
import com.openjiuwen.studio.prompt.engineering.task.constant.JobParamKeys;
import com.openjiuwen.studio.prompt.engineering.task.dao.JobInstance;
import com.openjiuwen.studio.prompt.engineering.task.domain.JobSpecName;
import com.openjiuwen.studio.prompt.engineering.task.domain.JobStatus;
import com.openjiuwen.studio.prompt.engineering.utils.ExcelUtil;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 异步调度 调度实现
 *
 */
@Slf4j
@Component
public class EvaluationPromptHandler extends AbstractCanPartlySuccessJobHandler {
    @Resource
    private PeEvaluationTaskMapper peEvaluationTaskMapper;

    @Resource
    private DatasetMapper datasetMapper;

    @Resource
    private ModelServiceCollection modelServiceCollection;

    @Resource
    private EvaluationMethodService evaluationMethodService;

    @Autowired
    private ExcelUtil excelUtil;

    @Override
    public String supportJobSpecName() {
        return JobSpecName.EVALUATION_PROMPT.name();
    }

    @Override
    protected void doExecuteInternal(JobInstance jobInstance, String token) {
        String projectId = jobInstance.getContext().getValue(JobParamKeys.PROJECT_ID);
        String evaluationTaskId = jobInstance.getContext().getValue(JobParamKeys.COMMON_TASK_ID);
        PeEvaluationTask peEvaluationTask = peEvaluationTaskMapper.queryEvaluationTaskDetailsById(evaluationTaskId,
            jobInstance.getWorkspaceId()).get(NUM_ZERO);
        updateContext(jobInstance, JobParamKeys.PE_PROMPT_ID, CollectionUtils.isEmpty(peEvaluationTask.getPrompts())
            ? ""
            : peEvaluationTask.getPrompts().get(NUM_ZERO).getId());
        String path = datasetMapper.getDatasetById(projectId, peEvaluationTask.getTestSetId(),
            jobInstance.getWorkspaceId()).getObsPath();
        String bucket = path.split(FOLDER_SEPARATOR)[NUM_ZERO];
        String filePath = path.substring(path.indexOf(FOLDER_SEPARATOR) + NUM_ONE);
        List<Map<String, String>> caseList = excelUtil.obtainCasesByObsFile(token, bucket, filePath);

        // 更新context信息
        updateContext(jobInstance, JobParamKeys.TEST_NUM, String.valueOf(caseList.size()));

        List<PePrompt> prompts = peEvaluationTask.getPrompts();
        for (int testNum = NUM_ONE; testNum <= caseList.size(); testNum++) {
            updateContext(jobInstance, JobParamKeys.TEST_NUM_CURRENT, String.valueOf(testNum));
            if (checkTestNum(jobInstance.getContext().getValue(JobParamKeys.COMMON_TASK_ID), testNum,
                jobInstance.getWorkspaceId())) {
                continue;
            }
            checkPaused(jobInstance);
            List<VariableDto> variableDtos = new ArrayList<>();
            Map<String, String> variables = caseList.get(testNum - NUM_ONE);
            for (Map.Entry<String, String> variable : variables.entrySet()) {
                if (!"result".equals(variable.getKey())) {
                    VariableDto variableDto = new VariableDto();
                    variableDto.setKey(variable.getKey());
                    variableDto.setValue(variable.getValue());
                    variableDtos.add(variableDto);
                }
            }

            List<PeEvaluationResult> results = new ArrayList<>();
            for (PePrompt prompt : prompts) {
                updateContext(jobInstance, JobParamKeys.PE_PROMPT_ID, prompt.getId());
                PromptBody promptBody = new PromptBody();
                promptBody.setQuestion(prompt.getQuestion());
                promptBody.setModelType(CommonConstant.AGENT_BUILDER_MODEL_TYPE);
                promptBody.setModel(prompt.getModel());
                promptBody.setVariables(variableDtos);
                promptBody.setModelConfig(prompt.getModelConfig());
                // 调用大模型接口
                InvokeResp invokeResp = modelServiceCollection.callModel(jobInstance.getWorkspaceId(), promptBody,
                    token, jobInstance.getProjectId());
                Integer score = evaluationMethodService.evaluate(jobInstance, invokeResp.getResult(),
                    variables.get("result"), peEvaluationTask);
                PeEvalResult evalResult = new PeEvalResult();
                evalResult.setScore(score);
                evalResult.setTime(invokeResp.getTime());
                evalResult.setToken(invokeResp.getToken());
                results.add(PeEvaluationResult.builder()
                    .id(UUID.randomUUID().toString())
                    .evaluationTaskId(peEvaluationTask.getId())
                    .evalResult(evalResult)
                    .generateResult(invokeResp.getResult())
                    .promptId(prompt.getId())
                    .testNum(testNum)
                    .workspaceId(jobInstance.getWorkspaceId())
                    .build());
            }
            // 结果存入数据库
            if (!CollectionUtils.isEmpty(results)) {
                peEvaluationTaskMapper.batchInsertEvalResult(results);
            }
        }
    }

    @Override
    protected boolean ifPartlySuccessRetryEnabled(JobInstance jobInstance) {
        int testNum = Integer.parseInt(jobInstance.getContext().getValue(JobParamKeys.TEST_NUM));
        String taskId = jobInstance.getContext().getValue(JobParamKeys.COMMON_TASK_ID);
        return testNum != NUM_MINUS_ONE && checkTestNum(taskId, NUM_ONE, jobInstance.getWorkspaceId()) && !checkTestNum(
            taskId, testNum, jobInstance.getWorkspaceId());
    }

    @Override
    protected boolean ifAllFailed(JobInstance jobInstance) {
        return checkTestNum(jobInstance.getContext().getValue(JobParamKeys.COMMON_TASK_ID), NUM_ONE,
            jobInstance.getWorkspaceId());
    }

    @Override
    protected boolean ifAllSuccess(JobInstance jobInstance) {
        int testNum = Integer.parseInt(jobInstance.getContext().getValue(JobParamKeys.TEST_NUM));
        return testNum != NUM_MINUS_ONE && checkTestNum(jobInstance.getContext().getValue(JobParamKeys.COMMON_TASK_ID),
            testNum, jobInstance.getWorkspaceId());
    }

    private boolean checkTestNum(String taskId, int testNum, String workspaceId) {
        return peEvaluationTaskMapper.countThisTestNumById(taskId, testNum, workspaceId) > NUM_ZERO;
    }

    @Override
    protected void processError(JobInstance jobInstance, String errorMsg) {
        super.onError(jobInstance, errorMsg);
        peEvaluationTaskMapper.updateEvaluationTaskStatusById(
            jobInstance.getContext().getValue(JobParamKeys.COMMON_TASK_ID), EvaluationTaskStatus.failed,
            jobInstance.getWorkspaceId());
    }

    @Override
    protected void onStart(JobInstance jobInstance) {
        super.onStart(jobInstance);
        peEvaluationTaskMapper.updateEvaluationTaskStatusById(
            jobInstance.getContext().getValue(JobParamKeys.COMMON_TASK_ID), EvaluationTaskStatus.running,
            jobInstance.getWorkspaceId());
    }

    @Override
    protected boolean ifNeedBePausedById(JobInstance jobInstance) {
        return jobInstanceMapper.isThisStatusById(jobInstance.getId(), JobStatus.pausing, jobInstance.getWorkspaceId())
            == 1 ||
            peEvaluationTaskMapper.isThisStatusById(jobInstance.getContext().getValue(JobParamKeys.COMMON_TASK_ID),
                EvaluationTaskStatus.pausing, jobInstance.getWorkspaceId()) == 1;
    }

    @Override
    protected void processSuccess(JobInstance jobInstance) {
        jobInstanceRepository.success(jobInstance);
        peEvaluationTaskMapper.updateEvaluationTaskStatusById(
            jobInstance.getContext().getValue(JobParamKeys.COMMON_TASK_ID), EvaluationTaskStatus.success,
            jobInstance.getWorkspaceId());
    }

    @Override
    protected void onRetry(JobInstance jobInstance) {
        jobInstanceRepository.retry(jobInstance);
        peEvaluationTaskMapper.updateEvaluationTaskStatusById(
            jobInstance.getContext().getValue(JobParamKeys.COMMON_TASK_ID), EvaluationTaskStatus.retrying,
            jobInstance.getWorkspaceId());
    }

    @Override
    protected void onPartlySuccess(JobInstance jobInstance, String errorMsg) {
        jobInstanceRepository.partlySuccess(jobInstance, "Partly success " + failedErrorMsg(jobInstance));
        peEvaluationTaskMapper.updateEvaluationTaskStatusById(
            jobInstance.getContext().getValue(JobParamKeys.COMMON_TASK_ID), EvaluationTaskStatus.partly_success,
            jobInstance.getWorkspaceId());
    }

    @Override
    protected void onNeedPaused(JobInstance jobInstance) {
        jobInstanceRepository.updateStatus(jobInstance, JobStatus.paused);
        peEvaluationTaskMapper.updateEvaluationTaskStatusById(
            jobInstance.getContext().getValue(JobParamKeys.COMMON_TASK_ID), EvaluationTaskStatus.paused,
            jobInstance.getWorkspaceId());
    }

    @Override
    protected boolean ifPausedById(JobInstance jobInstance) {
        return
            jobInstanceMapper.isThisStatusById(jobInstance.getId(), JobStatus.paused, jobInstance.getWorkspaceId()) == 1
                ||
                peEvaluationTaskMapper.isThisStatusById(jobInstance.getContext().getValue(JobParamKeys.COMMON_TASK_ID),
                    EvaluationTaskStatus.paused, jobInstance.getWorkspaceId()) == 1;
    }

    @Override
    protected void onErrorExtra(JobInstance jobInstance, String errorMsg) {
        peEvaluationTaskMapper.updateEvaluationTaskStatusById(
            jobInstance.getContext().getValue(JobParamKeys.COMMON_TASK_ID), EvaluationTaskStatus.failed,
            jobInstance.getWorkspaceId());
        peEvaluationTaskMapper.batchInsertEvalResult(Collections.singletonList(PeEvaluationResult.builder()
            .id(UUID.randomUUID().toString())
            .evaluationTaskId(jobInstance.getContext().getValue(JobParamKeys.COMMON_TASK_ID))
            .evalFailedReason(errorMsg + " Please retry later.")
            .promptId(jobInstance.getContext().getValue(JobParamKeys.PE_PROMPT_ID))
            .testNum(Integer.parseInt(jobInstance.getContext().getValue(JobParamKeys.TEST_NUM_CURRENT)))
            .generateResult("")
            .evalResult(new PeEvalResult())
            .workspaceId(jobInstance.getWorkspaceId())
            .build()));
    }
}
