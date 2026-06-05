/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.service;

import static com.openjiuwen.studio.prompt.engineering.constant.CommonConstant.FOLDER_SEPARATOR;
import static com.openjiuwen.studio.prompt.engineering.constant.CommonConstant.NUM_MINUS_ONE;
import static com.openjiuwen.studio.prompt.engineering.constant.CommonConstant.NUM_ONE;
import static com.openjiuwen.studio.prompt.engineering.constant.CommonConstant.NUM_ZERO;
import static com.openjiuwen.studio.prompt.engineering.constant.CommonConstant.USE_RANGE_AVAILABLE;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import com.openjiuwen.studio.agent.common.crypt.Ciphers;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.prompt.engineering.dto.DatasetDto;
import com.openjiuwen.studio.prompt.engineering.dto.ModelListVo;
import com.openjiuwen.studio.prompt.engineering.dto.ModelServiceInfo;
import com.openjiuwen.studio.prompt.engineering.dto.PeEvaluationTaskDto;
import com.openjiuwen.studio.prompt.engineering.dto.PeEvaluationTaskVo;
import com.openjiuwen.studio.prompt.engineering.entity.PeEvaluationTask;
import com.openjiuwen.studio.prompt.engineering.entity.PePrompt;
import com.openjiuwen.studio.prompt.engineering.enums.EvaluationMethodType;
import com.openjiuwen.studio.prompt.engineering.enums.EvaluationTaskStatus;
import com.openjiuwen.studio.prompt.engineering.mapper.DatasetMapper;
import com.openjiuwen.studio.prompt.engineering.mapper.PeEvaluationTaskMapper;
import com.openjiuwen.studio.prompt.engineering.mapper.PePromptMapper;
import com.openjiuwen.studio.prompt.engineering.mapper.PeTaskMapper;
import com.openjiuwen.studio.prompt.engineering.task.SingleNodeJobManager;
import com.openjiuwen.studio.prompt.engineering.task.constant.JobParamKeys;
import com.openjiuwen.studio.prompt.engineering.task.dao.JobInstance;
import com.openjiuwen.studio.prompt.engineering.task.domain.Context;
import com.openjiuwen.studio.prompt.engineering.task.domain.JobSpecName;
import com.openjiuwen.studio.prompt.engineering.task.service.TransactionalService;
import com.openjiuwen.studio.prompt.engineering.utils.DateUtil;
import com.openjiuwen.studio.prompt.engineering.utils.ExcelUtil;

import jakarta.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 评估任务
 *
 */
@Component
public class EvaluationTaskService {
    private static final int PROMPT_MAX_SIZE = 9;

    @Resource
    private PeEvaluationTaskMapper peEvaluationTaskMapper;

    @Resource
    private PeTaskMapper peTaskMapper;

    @Resource
    private PePromptMapper pePromptMapper;

    @Resource
    private EvaluationTaskTransactionService evaluationTaskTransactionService;

    @Resource
    private ModelManagerService modelManagerService;

    @Resource
    private DatasetMapper datasetMapper;

    @Resource
    private TransactionalService transactionalService;

    @Resource
    private SingleNodeJobManager jobManager;

    @Autowired
    private ExcelUtil excelUtil;

    @Value("${agentBuilder.evaluation.quota:500}")
    private Integer evaluationQuota;

    @Resource
    private Ciphers ciphers;

    /**
     * 创建评估任务
     *
     * @param projectId projectId
     * @param body body
     * @return 创建成功
     */
    public PeEvaluationTaskVo createEvaluation(String projectId, String workspaceId, PeEvaluationTaskDto body) {
        // 进行校验
        String taskId = body.getTaskId();
        if (!Boolean.TRUE.equals(peTaskMapper.isIdExist(taskId, workspaceId))) {
            throw new AgentStudioException(StudioError.TASK_ID_IS_NOT_EXIST, taskId);
        }

        int evaluationNum = peEvaluationTaskMapper.countByTaskId(taskId, workspaceId);
        if (evaluationNum >= evaluationQuota) {
            throw new AgentStudioException(StudioError.EVALUATION_EXCEEDS_QUOTA, List.of(evaluationQuota.toString()));
        }
        String name = body.getName();
        if (Boolean.TRUE.equals(peEvaluationTaskMapper.isNameExist(taskId, name, workspaceId))) {
            throw new AgentStudioException(StudioError.EVALUATION_TASK_IS_EXIST);
        }
        String method = body.getMethod();
        if (!EvaluationMethodType.isInclude(method)) {
            throw new AgentStudioException(StudioError.EVALUATION_METHOD_ERROR);
        }

        // 查看是否有正在进行中的同一个工程任务下评估任务，如果有，则不可以新建评估任务
        List<PeEvaluationTask> evaluationTaskExist = peEvaluationTaskMapper.queryEvaluationTaskByTaskId(taskId,
            workspaceId);
        for (PeEvaluationTask evaluationTask : evaluationTaskExist) {
            if (EvaluationTaskStatus.isExecuting(evaluationTask.getStatus())) {
                throw new AgentStudioException(StudioError.EVALUATION_TASK_RUNNING,
                    Collections.singletonList(evaluationTask.getName()));
            }
        }
        String testSetId = body.getTestSetId();
        if (!Boolean.TRUE.equals(datasetMapper.isIdExist(testSetId, workspaceId))) {
            throw new AgentStudioException(StudioError.DATASET_FILE_DOES_NOT_EXIST);
        }
        List<String> promptIds = body.getPromptIds();
        List<PePrompt> prompts = new ArrayList<>();
        ModelListVo modelListVo = modelManagerService.listModels(projectId, USE_RANGE_AVAILABLE, workspaceId);
        if (modelListVo == null) {
            throw new AgentStudioException(StudioError.PROMPT_MODEL_NOT_EXIST);
        }
        List<ModelServiceInfo> modelList = modelListVo.getModelServiceInfoList();
        List<String> modelDeploymentList = new ArrayList<>();
        for (ModelServiceInfo modelServiceInfo : modelList) {
            String modelServiceInfoDeploymentId = modelServiceInfo.getDeploymentId();
            modelDeploymentList.add(modelServiceInfoDeploymentId);
        }
        Iterator<String> iterator = promptIds.iterator();
        while (iterator.hasNext()) {
            String promptId = iterator.next();
            if (!Boolean.TRUE.equals(pePromptMapper.isPromptIdExist(promptId, taskId, workspaceId))) {
                iterator.remove();
                continue;
            }
            PePrompt prompt = pePromptMapper.queryPromptsByIds(Collections.singletonList(promptId), workspaceId)
                .get(NUM_ZERO);
            String deploymentId = prompt.getModelConfig().getDeploymentId();
            String modelName = prompt.getModel();
            if (!modelDeploymentList.contains(deploymentId)) {
                throw new AgentStudioException(StudioError.PROMPT_MODEL_NOT_EXIST);
            }
            prompts.add(prompt);
        }
        if (prompts.isEmpty()) {
            throw new AgentStudioException(StudioError.PROMPT_TEMPLATE_EMPTY);
        }
        if (prompts.size() > PROMPT_MAX_SIZE) {
            throw new AgentStudioException(StudioError.CANDIDATE_TEMPLATE_EXCEEDS_QUOTA,
                String.valueOf(PROMPT_MAX_SIZE));
        }
        EvaluationTaskStatus status = EvaluationTaskStatus.pending;
        String id = UUID.randomUUID().toString();
        DatasetDto datasetDto = datasetMapper.getDatasetById(projectId, testSetId, workspaceId);
        String testSetName = datasetDto.getDatasetName();
        String path = datasetDto.getObsPath();
        String bucket = path.split(FOLDER_SEPARATOR)[NUM_ZERO];
        String filePath = path.substring(path.indexOf(FOLDER_SEPARATOR) + NUM_ONE);
        List<Map<String, String>> list = excelUtil.obtainCasesByObsFile(RequestContextUtils.getRequestAuthToken(),
            bucket, filePath);
        int caseNum = list.size();
        Context context = Context.factory();
        context.put(JobParamKeys.PROJECT_ID, projectId);
        context.put(JobParamKeys.COMMON_TASK_ID, id);
        context.put(JobParamKeys.TEST_NUM, Integer.toString(NUM_MINUS_ONE));
        context.put(JobParamKeys.TEST_NUM_CURRENT, Integer.toString(NUM_ONE));
        context.put(JobParamKeys.PE_PROMPT_ID, EMPTY);
        context.put(JobParamKeys.X_AUTH_TOKEN, ciphers.encrypt(RequestContextUtils.getRequestAuthToken()));
        String creator = RequestContextUtils.getRequestUserName();
        PeEvaluationTask evaluationTask = new PeEvaluationTask(id, name,
            EvaluationMethodType.getEvaluationMethodType(method), taskId, testSetId, testSetName, caseNum,
            body.getDescription(), body.getRegularExpression(), body.getEvalModelConfig(), null, null, status, prompts,
            null, null, creator, workspaceId);
        JobInstance jobInstance = jobManager.createWithoutPersist(JobSpecName.EVALUATION_PROMPT.name(), context,
            workspaceId);
        String token = RequestContextUtils.getRequestAuthToken();
        jobInstance.setProjectId(RequestContextUtils.getRequestProjectId());
        transactionalService.execute(() -> {
            evaluationTaskTransactionService.createEvaluationTask(evaluationTask);
            jobManager.createAndDispatch(jobInstance, token);
        });
        PeEvaluationTask peEvaluationTask = peEvaluationTaskMapper.queryEvaluationTaskDetailsById(id, workspaceId)
            .get(NUM_ZERO);
        peEvaluationTask.setTestSetName(testSetName);
        peEvaluationTask.setCaseNum(caseNum);
        return peEvaluationTask.convertToPeEvaluationTaskVo();
    }

    /**
     * 重试失败评估任务
     *
     * @param projectId projectId
     * @param id id
     * @return PeEvaluationTaskVo
     */
    public String retryEvaluationTask(String projectId, String id, String workspaceId) {
        List<PeEvaluationTask> evaluationTaskList = peEvaluationTaskMapper.queryEvaluationTaskDetailsById(id,
            workspaceId);
        if (evaluationTaskList.isEmpty()) {
            throw new AgentStudioException(StudioError.EVALUATION_TASK_IS_NOT_EXIST);
        }
        PeEvaluationTask evaluationTask = evaluationTaskList.get(NUM_ZERO);
        EvaluationTaskStatus status = evaluationTask.getStatus();
        if (!status.equals(EvaluationTaskStatus.failed)) {
            throw new AgentStudioException(StudioError.RETRY_EVALUATION_TASK_FAILED);
        }

        // 构造重试任务上下文
        Context context = Context.factory();
        context.put(JobParamKeys.PROJECT_ID, projectId);
        context.put(JobParamKeys.COMMON_TASK_ID, id);
        context.put(JobParamKeys.TEST_NUM, Integer.toString(NUM_MINUS_ONE));
        context.put(JobParamKeys.TEST_NUM_CURRENT, Integer.toString(NUM_ONE));
        context.put(JobParamKeys.PE_PROMPT_ID, EMPTY);
        context.put(JobParamKeys.X_AUTH_TOKEN, ciphers.encrypt(RequestContextUtils.getRequestAuthToken()));
        JobInstance jobInstance = jobManager.createWithoutPersist(JobSpecName.EVALUATION_PROMPT.name(), context,
            workspaceId);
        String token = RequestContextUtils.getRequestAuthToken();
        jobInstance.setProjectId(RequestContextUtils.getRequestProjectId());
        transactionalService.execute(() -> {
            evaluationTaskTransactionService.deleteEvaluationTaskResult(id, workspaceId);
            evaluationTaskTransactionService.modifyEvaluationTaskInfo(id, evaluationTask.getName(),
                evaluationTask.getDescription(), EvaluationTaskStatus.pending, workspaceId);
            jobManager.createAndDispatch(jobInstance, token);
        });

        return HttpStatus.OK.toString();
    }

    /**
     * 展示评估任务
     *
     * @param projectId projectId
     * @param id id
     * @return PeEvaluationTaskVo
     */
    public PeEvaluationTaskVo showEvaluation(String projectId, String id, String workspaceId) {
        if (!Boolean.TRUE.equals(peEvaluationTaskMapper.isEvaluationTaskExist(id, workspaceId))) {
            throw new AgentStudioException(StudioError.EVALUATION_TASK_IS_NOT_EXIST);
        }
        List<PeEvaluationTask> evaluationTaskList = peEvaluationTaskMapper.queryEvaluationTaskDetailsById(id,
            workspaceId);
        PeEvaluationTask evaluationTask = evaluationTaskList.get(NUM_ZERO);
        String testSetId = evaluationTask.getTestSetId();
        getDatasetInfo(projectId, testSetId, evaluationTask, workspaceId);
        PeEvaluationTaskVo evaluationTaskVo = evaluationTask.convertToPeEvaluationTaskVo();
        evaluationTaskVo.setCreatedOn(DateUtil.GMTFormat(evaluationTask.getCreatedOn()));
        return evaluationTaskVo;
    }

    public String deleteEvaluation(String projectId, String id, String workspaceId) {
        List<PeEvaluationTask> evaluationTaskList = peEvaluationTaskMapper.queryEvaluationTaskDetailsById(id,
            workspaceId);
        if (evaluationTaskList.isEmpty()) {
            throw new AgentStudioException(StudioError.EVALUATION_TASK_IS_NOT_EXIST);
        }
        PeEvaluationTask evaluationTask = evaluationTaskList.get(NUM_ZERO);
        String name = evaluationTask.getName();
        EvaluationTaskStatus status = evaluationTask.getStatus();
        if (EvaluationTaskStatus.isExecuting(status)) {
            throw new AgentStudioException(StudioError.EVALUATION_TASK_RUNNING, Collections.singletonList(name));
        }
        evaluationTaskTransactionService.deleteEvaluationTaskAndResult(id, workspaceId);
        return HttpStatus.OK.toString();
    }

    public PeEvaluationTaskVo updateEvaluation(String projectId, String id, String name, String description,
        String status, String workspaceId) {
        if (!Boolean.TRUE.equals(peEvaluationTaskMapper.isIdExist(id, workspaceId))) {
            throw new AgentStudioException(StudioError.EVALUATION_TASK_IS_NOT_EXIST);
        }
        if (!StringUtils.isEmpty(status) && !EvaluationTaskStatus.isInclude(status)) {
            throw new AgentStudioException(StudioError.STATUS_ERROR);
        }
        if (StringUtils.isEmpty(name) && StringUtils.isEmpty(description) && StringUtils.isEmpty(status)) {
            throw new AgentStudioException(StudioError.EVALUATION_TASK_UPDATE_FAILED);
        }
        PeEvaluationTask evaluationTask = peEvaluationTaskMapper.queryEvaluationTaskDetailsById(id, workspaceId)
            .get(NUM_ZERO);
        String taskId = evaluationTask.getTaskId();
        if (!StringUtils.isEmpty(name) && Boolean.TRUE.equals(
            peEvaluationTaskMapper.isNameDuplicate(id, taskId, name, workspaceId))) {
            throw new AgentStudioException(StudioError.EVALUATION_TASK_IS_EXIST);
        }
        String evaluationTaskName = StringUtils.isEmpty(name) ? evaluationTask.getName() : name;
        description = description == null ? evaluationTask.getDescription() : description;
        EvaluationTaskStatus evaluationTaskStatus = StringUtils.isEmpty(status)
            ? evaluationTask.getStatus()
            : EvaluationTaskStatus.getEvaluationTaskStatus(status);
        evaluationTaskTransactionService.modifyEvaluationTaskInfo(id, evaluationTaskName, description,
            evaluationTaskStatus, workspaceId);
        PeEvaluationTask peEvaluationTask = peEvaluationTaskMapper.queryEvaluationTaskDetailsById(id, workspaceId)
            .get(NUM_ZERO);
        String testSetId = peEvaluationTask.getTestSetId();
        getDatasetInfo(projectId, testSetId, peEvaluationTask, workspaceId);
        return peEvaluationTask.convertToPeEvaluationTaskVo();
    }

    public PeEvaluationTaskVo queryLatestEvaluationByTaskId(String projectId, String taskId, String workspaceId) {
        if (!Boolean.TRUE.equals(peTaskMapper.isIdExist(taskId, workspaceId))) {
            throw new AgentStudioException(StudioError.TASK_ID_IS_NOT_EXIST, taskId);
        }
        try {
            // 查询最新的评估任务ID
            String evaluationTaskId = peEvaluationTaskMapper.queryLatestEvaluationTaskIdByTaskId(taskId, workspaceId);
            if (StringUtils.isEmpty(evaluationTaskId)) {
                return null;
            }
            PeEvaluationTask peEvaluationTask = peEvaluationTaskMapper.queryEvaluationTaskDetailsById(evaluationTaskId,
                workspaceId).get(NUM_ZERO);
            return peEvaluationTask.convertToPeEvaluationTaskVo();
        } catch (Exception e) {
            throw new AgentStudioException(StudioError.FAILED_TO_QUERY_EVALUATION_TASK_INFO);
        }
    }

    private void getDatasetInfo(String projectId, String testSetId, PeEvaluationTask peEvaluationTask,
        String workspaceId) {
        String testSetName = datasetMapper.getDatasetById(projectId, testSetId, workspaceId).getDatasetName();
        String path = datasetMapper.getDatasetById(projectId, testSetId, workspaceId).getObsPath();
        String bucket = path.split(FOLDER_SEPARATOR)[NUM_ZERO];
        String filePath = path.substring(path.indexOf(FOLDER_SEPARATOR) + NUM_ONE);
        List<Map<String, String>> list = excelUtil.obtainCasesByObsFile(RequestContextUtils.getRequestAuthToken(),
            bucket, filePath);
        int caseNum = list.size();
        peEvaluationTask.setTestSetName(testSetName);
        peEvaluationTask.setCaseNum(caseNum);
    }
}
