/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.task.handler;

import static com.openjiuwen.studio.prompt.engineering.constant.CommonConstant.FOLDER_SEPARATOR;
import static com.openjiuwen.studio.prompt.engineering.constant.CommonConstant.NUM_ONE;
import static com.openjiuwen.studio.prompt.engineering.constant.CommonConstant.NUM_ZERO;

import com.openjiuwen.studio.agent.common.crypt.Ciphers;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.prompt.engineering.dto.Case;
import com.openjiuwen.studio.prompt.engineering.dto.ModelCase;
import com.openjiuwen.studio.prompt.engineering.dto.ModelConfig;
import com.openjiuwen.studio.prompt.engineering.dto.ModelProperties;
import com.openjiuwen.studio.prompt.engineering.dto.OptimizeInfo;
import com.openjiuwen.studio.prompt.engineering.dto.TemplateOptimizeReq;
import com.openjiuwen.studio.prompt.engineering.dto.TemplateOptimizeResp;
import com.openjiuwen.studio.prompt.engineering.entity.PeOptimizationResult;
import com.openjiuwen.studio.prompt.engineering.entity.PeOptimizationTask;
import com.openjiuwen.studio.prompt.engineering.entity.PePrompt;
import com.openjiuwen.studio.prompt.engineering.enums.OptimizationTaskStatus;
import com.openjiuwen.studio.prompt.engineering.mapper.DatasetMapper;
import com.openjiuwen.studio.prompt.engineering.mapper.PeOptimizationTaskMapper;
import com.openjiuwen.studio.prompt.engineering.service.OptimizationTemplateService;
import com.openjiuwen.studio.prompt.engineering.task.constant.JobParamKeys;
import com.openjiuwen.studio.prompt.engineering.task.dao.JobInstance;
import com.openjiuwen.studio.prompt.engineering.task.domain.JobSpecName;
import com.openjiuwen.studio.prompt.engineering.utils.ExcelUtil;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 异步调度 调度实现
 *
 */
@Slf4j
@Component
public class OptimizationPromptHandler extends AbstractJobHandler {
    @Resource
    private OptimizationTemplateService optimizationTemplateService;

    @Autowired
    private ExcelUtil excelUtil;

    @Resource
    private PeOptimizationTaskMapper peOptimizationTaskMapper;

    @Resource
    private DatasetMapper datasetMapper;

    @Resource
    private Ciphers ciphers;

    @Override
    public String supportJobSpecName() {
        return JobSpecName.TEMPLATE_OPTIMIZATION.name();
    }

    @Override
    protected void doExecute(JobInstance jobInstance, String token) {
        String projectId = jobInstance.getContext().getValue(JobParamKeys.PROJECT_ID);
        String optimizationTaskId = jobInstance.getContext().getValue(JobParamKeys.COMMON_TASK_ID);
        PeOptimizationTask peOptimizationTask = peOptimizationTaskMapper.queryOptimizationTaskDetailsById(
            optimizationTaskId, jobInstance.getWorkspaceId()).get(NUM_ZERO);

        String testSetId = peOptimizationTask.getTestSetId();
        String path = datasetMapper.getDatasetById(projectId, testSetId, jobInstance.getWorkspaceId()).getObsPath();
        String bucket = path.split(FOLDER_SEPARATOR)[NUM_ZERO];
        String filePath = path.substring(path.indexOf(FOLDER_SEPARATOR) + NUM_ONE);
        String xAuthToken = ciphers.decrypt(jobInstance.getContext().getValue(JobParamKeys.X_AUTH_TOKEN));
        List<Map<String, String>> caseList = excelUtil.obtainCasesByObsFile(xAuthToken, bucket, filePath);

        final List<Case> messages = Arrays.asList(Case.builder()
            .role("system")
            .content("你是一个智能助手，你为用户生成综合质量（包括有用性，事实性，无害性）极好的回复。")
            .build(), Case.builder()
            .role("system")
            .content(
                "你的名字叫智子。当详尽回复更好时，你会生成全面且深入的回复。当简洁回复更好时，你会生成简明扼要的回复。")
            .build());
        try {
            List<ModelCase> cases = new ArrayList<>();
            List<ModelCase> finalCases = cases;
            peOptimizationTask.getPrompts().forEach(pePrompt -> {
                caseList.forEach(cas -> {
                    String content = pePrompt.getQuestion();
                    for (Map.Entry<String, String> variable : cas.entrySet()) {
                        content = content.replace("{{" + variable.getKey() + "}}", variable.getValue());
                    }

                    finalCases.add(ModelCase.builder()
                        .messages(Arrays.asList(messages.get(NUM_ZERO), messages.get(NUM_ONE),
                            Case.builder().role("user").content(content).build(),
                            Case.builder().role("assistant").content(cas.get("result")).build()))
                        .build());
                });
            });

            List<String> rawTemplates = peOptimizationTask.getPrompts()
                .stream()
                .map(PePrompt::getQuestion)
                .collect(Collectors.toList());

            int numIter = peOptimizationTask.getNumIter();
            ModelProperties modelInfo = getModelInfo(peOptimizationTask.getModelConfig());
            ModelProperties assistantInfo = getModelInfo(peOptimizationTask.getAssistantConfig());
            TemplateOptimizeReq req = TemplateOptimizeReq.builder()
                .name(peOptimizationTask.getName())
                .desc(peOptimizationTask.getDescription())
                .rawTemplates(rawTemplates)
                .optimizeInfo(OptimizeInfo.builder().cases(cases).numIter(numIter).build())
                .modelInfo(modelInfo)
                .assistantInfo(assistantInfo)
                .build();
            TemplateOptimizeResp response = optimizationTemplateService.createOptimization(req,
                ciphers.decrypt(peOptimizationTask.getModelConfig().getToken()));

            if (StringUtils.isNotBlank(response.getJobInfo().getId())) {
                updateContext(jobInstance, JobParamKeys.JIUWEN_TASK_ID, response.getJobInfo().getId());
            } else {
                throw new AgentStudioException(StudioError.OPTIMIZE_TEMPLATE_FAILED,
                    Collections.singletonList(response.getMessage()));
            }
        } catch (ResourceAccessException e) {
            throw new AgentStudioException(StudioError.OPTIMIZATION_TEMPLATE_SERVICE_ACCESS_FAILED);
        }
    }

    private ModelProperties getModelInfo(ModelConfig modelConfig) {
        ModelProperties modelInfo = new ModelProperties();
        if (modelConfig != null) {
            modelInfo.setModel(modelConfig.getDeploymentId());
            modelInfo.setModelSource(modelConfig.getModelSource());
        }
        return modelInfo;
    }

    @Override
    protected void onStart(JobInstance jobInstance) {
        super.onStart(jobInstance);
        peOptimizationTaskMapper.updateOptimizationTaskRuningStatusById(
            jobInstance.getContext().getValue(JobParamKeys.COMMON_TASK_ID), OptimizationTaskStatus.RUNNING,
            jobInstance.getWorkspaceId());
    }

    @Override
    protected void onSuccess(JobInstance jobInstance) {
        jobInstanceRepository.success(jobInstance);
        peOptimizationTaskMapper.updateOptimizationTaskById(
            jobInstance.getContext().getValue(JobParamKeys.COMMON_TASK_ID), OptimizationTaskStatus.RUNNING,
            jobInstance.getContext().getValue(JobParamKeys.JIUWEN_TASK_ID), jobInstance.getWorkspaceId());
    }

    @Override
    protected void onError(JobInstance jobInstance, String errorMsg) {
        super.onError(jobInstance, errorMsg);
        PeOptimizationResult peOptimizationResult = PeOptimizationResult.builder()
            .id(jobInstance.getContext().getValue(JobParamKeys.COMMON_TASK_ID))
            .errorMsg(errorMsg + " Please retry later.")
            .status(OptimizationTaskStatus.FAILED.getCode())
            .build();
        peOptimizationTaskMapper.updateOptimizationTaskResultById(peOptimizationResult);
    }

    @Override
    protected void onErrorExtra(JobInstance jobInstance, String errorMsg) {
        peOptimizationTaskMapper.updateOptimizationTaskRuningStatusById(
            jobInstance.getContext().getValue(JobParamKeys.COMMON_TASK_ID), OptimizationTaskStatus.FAILED,
            jobInstance.getWorkspaceId());
    }
}
