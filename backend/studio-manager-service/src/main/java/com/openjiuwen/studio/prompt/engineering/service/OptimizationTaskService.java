/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.service;

import static com.openjiuwen.studio.prompt.engineering.constant.CommonConstant.NUM_ZERO;
import static com.openjiuwen.studio.prompt.engineering.constant.CommonConstant.USE_RANGE_SELFTRAINING;

import com.openjiuwen.studio.agent.common.crypt.Ciphers;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.prompt.engineering.constant.CommonConstant;
import com.openjiuwen.studio.prompt.engineering.dto.InvokeResp;
import com.openjiuwen.studio.prompt.engineering.dto.ModelConfig;
import com.openjiuwen.studio.prompt.engineering.dto.ModelListVo;
import com.openjiuwen.studio.prompt.engineering.dto.ModelServiceInfo;
import com.openjiuwen.studio.prompt.engineering.dto.PeOptimizationTaskDto;
import com.openjiuwen.studio.prompt.engineering.dto.PeOptimizationTaskVo;
import com.openjiuwen.studio.prompt.engineering.dto.PromptBody;
import com.openjiuwen.studio.prompt.engineering.entity.PeOptimizationResult;
import com.openjiuwen.studio.prompt.engineering.entity.PeOptimizationTask;
import com.openjiuwen.studio.prompt.engineering.entity.PePrompt;
import com.openjiuwen.studio.prompt.engineering.enums.OptimizationTaskStatus;
import com.openjiuwen.studio.prompt.engineering.mapper.DatasetMapper;
import com.openjiuwen.studio.prompt.engineering.mapper.PeOptimizationTaskMapper;
import com.openjiuwen.studio.prompt.engineering.mapper.PePromptMapper;
import com.openjiuwen.studio.prompt.engineering.mapper.PeTaskMapper;
import com.openjiuwen.studio.prompt.engineering.service.model.ModelServiceCollection;
import com.openjiuwen.studio.prompt.engineering.task.SingleNodeJobManager;
import com.openjiuwen.studio.prompt.engineering.task.constant.JobParamKeys;
import com.openjiuwen.studio.prompt.engineering.task.dao.JobInstance;
import com.openjiuwen.studio.prompt.engineering.task.domain.Context;
import com.openjiuwen.studio.prompt.engineering.task.domain.JobSpecName;
import com.openjiuwen.studio.prompt.engineering.task.service.TransactionalService;
import com.openjiuwen.studio.prompt.engineering.utils.DateUtil;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 创建优化任务
 *
 */
@Slf4j
@Component
public class OptimizationTaskService {
    private static final int PROMPT_MAX_SIZE = 9;

    private static final String TASK_2_RESTART = "restart";

    private static final String TASK_2_STOP = "stop";

    private static final String REFINE_PROMPT = "请将以下输入精炼为更简洁、清晰的形式，不要丢失{{}}内容\n" + "原始输入：\n"
        + "{{prompt}}";

    @Resource
    private TransactionalService transactionalService;

    @Resource
    private SingleNodeJobManager jobManager;

    @Resource
    private OptimizationTaskTransactionService optimizationTaskTransactionService;

    @Resource
    private DatasetMapper datasetMapper;

    @Resource
    private ModelManagerService modelManagerService;

    @Resource
    private OptimizationTemplateService optimizationTemplateService;

    @Resource
    private PePromptMapper pePromptMapper;

    @Resource
    private PeOptimizationTaskMapper peOptimizationTaskMapper;

    @Resource
    private PeTaskMapper peTaskMapper;

    @Resource
    private ModelServiceCollection modelServiceCollection;

    @Resource
    private Ciphers ciphers;

    /**
     * 创建评估任务
     *
     * @param projectId projectId
     * @param body body
     * @return 创建成功
     */
    public PeOptimizationTaskVo createOptimization(String projectId, String workspaceId, PeOptimizationTaskDto body) {
        // 进行校验
        String taskId = body.getTaskId();
        if (!Boolean.TRUE.equals(peTaskMapper.isIdExist(taskId, workspaceId))) {
            throw new AgentStudioException(StudioError.TASK_ID_IS_NOT_EXIST, taskId);
        }

        String testSetId = body.getTestSetId();
        if (!Boolean.TRUE.equals(datasetMapper.isIdExist(testSetId, workspaceId))) {
            throw new AgentStudioException(StudioError.DATASET_FILE_DOES_NOT_EXIST);
        }

        String id = UUID.randomUUID().toString();
        String name = body.getName();
        if (Boolean.TRUE.equals(peOptimizationTaskMapper.isNameExist(taskId, name, workspaceId))) {
            throw new AgentStudioException(StudioError.OPTIMIZATION_TASK_IS_EXIST);
        }
        String description = body.getDescription();

        List<String> promptIds = body.getPromptIds();
        List<PePrompt> prompts = new ArrayList<>();
        Iterator<String> iterator = promptIds.iterator();
        while (iterator.hasNext()) {
            String promptId = iterator.next();
            if (!Boolean.TRUE.equals(pePromptMapper.isPromptIdExist(promptId, taskId, workspaceId))) {
                iterator.remove();
                continue;
            }
            PePrompt prompt = pePromptMapper.queryPromptsByIds(Collections.singletonList(promptId), workspaceId)
                .get(NUM_ZERO);
            prompts.add(prompt);
        }
        if (prompts.isEmpty()) {
            throw new AgentStudioException(StudioError.PROMPT_TEMPLATE_EMPTY);
        }
        if (prompts.size() > PROMPT_MAX_SIZE) {
            throw new AgentStudioException(StudioError.CANDIDATE_TEMPLATE_EXCEEDS_QUOTA,
                String.valueOf(PROMPT_MAX_SIZE));
        }

        ModelListVo modelListVo = modelManagerService.listModels(projectId, USE_RANGE_SELFTRAINING, workspaceId);
        if (modelListVo == null) {
            throw new AgentStudioException(StudioError.PROMPT_MODEL_NOT_EXIST);
        }
        List<ModelServiceInfo> optimizationModelList = modelListVo.getModelServiceInfoList();
        List<String> optimizationModelDeploymentList = optimizationModelList.stream()
            .map(ModelServiceInfo::getDeploymentId)
            .collect(Collectors.toList());
        ModelConfig modelConfig = body.getModelConfig();
        modelConfig.setToken(ciphers.encrypt(RequestContextUtils.getRequestAuthToken()));
        if (!optimizationModelDeploymentList.contains(modelConfig.getDeploymentId())) {
            throw new AgentStudioException(StudioError.PROMPT_MODEL_NOT_EXIST);
        }
        ModelConfig assistantConfig = body.getAssistantConfig();
        if (assistantConfig != null) {
            if (!optimizationModelDeploymentList.contains(assistantConfig.getDeploymentId())) {
                throw new AgentStudioException(StudioError.PROMPT_MODEL_NOT_EXIST);
            }
            assistantConfig.setToken(ciphers.encrypt(RequestContextUtils.getRequestAuthToken()));
        }
        Integer numIter = body.getNumIter();
        OptimizationTaskStatus status = OptimizationTaskStatus.PENDING;
        String creator = RequestContextUtils.getRequestUserName();
        PeOptimizationTask optimizationTask = new PeOptimizationTask(id, projectId, taskId, name, description,
            body.getType(), null, prompts, testSetId, modelConfig, assistantConfig, numIter, null, null, 0.0, status,
            creator, new Date(), new Date(), new Date(), workspaceId);

        optimizationTaskTransactionService.createOptimizationTask(optimizationTask);

        // 如果是精炼模式，调用模型获取精炼prompt
        if (optimizationTask.getType().equals("refine")) {
            PePrompt prompt = optimizationTask.getPrompts().get(0);
            String refinePrompt = getRefinePrompt(optimizationTask.getModelConfig(), prompt.getQuestion(), workspaceId);
            PeOptimizationResult peOptimizationResult = PeOptimizationResult.builder()
                .id(optimizationTask.getId())
                .progressRate(1.0f)
                .bestPrompt(refinePrompt)
                .status(OptimizationTaskStatus.SUCCESS.getCode())
                .build();
            peOptimizationTaskMapper.updateOptimizationTaskResultById(peOptimizationResult);
            return optimizationTask.convertToPeOptimizationTaskVo();
        }
        String token = RequestContextUtils.getRequestAuthToken();
        if (peOptimizationTaskMapper.getOptimizationTask(projectId, taskId, OptimizationTaskStatus.executing(),
            workspaceId) == null) {
            Context context = Context.factory();
            context.put(JobParamKeys.PROJECT_ID, projectId);
            context.put(JobParamKeys.COMMON_TASK_ID, id);
            context.put(JobParamKeys.JIUWEN_TASK_ID, "");
            context.put(JobParamKeys.X_AUTH_TOKEN, ciphers.encrypt(RequestContextUtils.getRequestAuthToken()));
            JobInstance jobInstance = jobManager.createWithoutPersist(JobSpecName.TEMPLATE_OPTIMIZATION.name(), context,
                workspaceId);
            jobInstance.setProjectId(projectId);

            transactionalService.execute(() -> {
                jobManager.createAndDispatch(jobInstance, token);
            });
        }
        PeOptimizationTask peOptimizationTask = peOptimizationTaskMapper.queryOptimizationTaskDetailsById(id,
            workspaceId).get(NUM_ZERO);
        return peOptimizationTask.convertToPeOptimizationTaskVo();
    }

    public PeOptimizationTaskVo changeOptimization(String projectId, String id, String name, String description,
        String status, String workspaceId) {
        boolean ifRestart = TASK_2_RESTART.equals(status);
        List<PeOptimizationTask> peOptimizationTaskList = peOptimizationTaskMapper.queryOptimizationTaskDetailsById(id,
            workspaceId);
        if (CollectionUtils.isEmpty(peOptimizationTaskList)) {
            throw new AgentStudioException(StudioError.OPTIMIZATION_TASK_ID_IS_NOT_EXIST);
        }
        PeOptimizationTask peOptimizationTask = peOptimizationTaskList.get(NUM_ZERO);
        String optimizationTaskName = StringUtils.isEmpty(name) ? peOptimizationTask.getName() : name;
        description = description == null ? peOptimizationTask.getDescription() : description;

        OptimizationTaskStatus optimizationTaskStatus = peOptimizationTask.getStatus();
        if (StringUtils.isBlank(name)) {
            if (!TASK_2_RESTART.equals(status) && !TASK_2_STOP.equals(status)) {
                throw new AgentStudioException(StudioError.OPTIMIZATION_TASK_STATUS_IS_INVALIDATE);
            }
            if (TASK_2_RESTART.equals(status)) {
                PeOptimizationTask peOptTask = peOptimizationTaskMapper.getOptimizationTask(
                    peOptimizationTask.getProjectId(), peOptimizationTask.getTaskId(),
                    OptimizationTaskStatus.executing(), workspaceId);
                if (peOptTask != null) {
                    String taskName = peTaskMapper.queryTaskNameById(peOptTask.getTaskId(), workspaceId);
                    throw new AgentStudioException(StudioError.OPTIMIZATION_TASK_RUNNING_IS_EXIST,
                        Collections.singletonList(taskName));
                }
            }

            String jiuwenTaskId = peOptimizationTask.getJiuwenTaskId();
            optimizationTemplateService.changeOptimization(jiuwenTaskId, ifRestart);
            optimizationTaskStatus = ifRestart ? OptimizationTaskStatus.RUNNING : OptimizationTaskStatus.PAUSED;
        }
        peOptimizationTaskMapper.modifyOptimizationTaskInfo(id, optimizationTaskName, description,
            optimizationTaskStatus, workspaceId);

        peOptimizationTask = peOptimizationTaskMapper.queryOptimizationTaskDetailsById(id, workspaceId).get(NUM_ZERO);
        return peOptimizationTask.convertToPeOptimizationTaskVo();
    }

    /**
     * 展示评估任务
     *
     * @param projectId projectId
     * @param id id
     * @return PeOptimizationTaskVo
     */
    public PeOptimizationTaskVo showOptimization(String projectId, String id, String workspaceId) {
        if (!Boolean.TRUE.equals(peOptimizationTaskMapper.isOptimizationTaskExist(id, workspaceId))) {
            throw new AgentStudioException(StudioError.OPTIMIZATION_TASK_ID_IS_NOT_EXIST);
        }
        List<PeOptimizationTask> optimizationTaskList = peOptimizationTaskMapper.queryOptimizationTaskDetailsById(id,
            workspaceId);
        PeOptimizationTask optimizationTask = optimizationTaskList.get(NUM_ZERO);
        PeOptimizationTaskVo optimizationTaskVo = optimizationTask.convertToPeOptimizationTaskVo();
        optimizationTaskVo.setCreatedOn(DateUtil.GMTFormat(optimizationTask.getCreatedOn()));
        return optimizationTaskVo;
    }

    public String deleteOptimization(String projectId, String id, String workspaceId) {
        List<PeOptimizationTask> optimizationTaskList = peOptimizationTaskMapper.queryOptimizationTaskDetailsById(id,
            workspaceId);
        if (optimizationTaskList.isEmpty()) {
            throw new AgentStudioException(StudioError.OPTIMIZATION_TASK_ID_IS_NOT_EXIST);
        }
        PeOptimizationTask peOptimizationTask = optimizationTaskList.get(NUM_ZERO);
        String name = peOptimizationTask.getName();
        OptimizationTaskStatus status = peOptimizationTask.getStatus();
        if (OptimizationTaskStatus.RUNNING == status) {
            throw new AgentStudioException(StudioError.OPTIMIZATION_TASK_RUNNING, Collections.singletonList(name));
        }
        String jiuwenTaskId = peOptimizationTask.getJiuwenTaskId();
        if (OptimizationTaskStatus.PENDING != peOptimizationTask.getStatus() && StringUtils.isNotEmpty(jiuwenTaskId)) {
            optimizationTemplateService.deleteOptimization(jiuwenTaskId);
        }
        optimizationTaskTransactionService.deleteOptimizationTaskAndResult(id, workspaceId);

        return HttpStatus.OK.toString();
    }

    /**
     * 获取精炼优化prompt
     */
    private String getRefinePrompt(ModelConfig modelConfig, String prompt, String workspaceId) {
        PromptBody promptBody = new PromptBody();
        promptBody.setQuestion(REFINE_PROMPT.replace("{{prompt}}", prompt));
        promptBody.setModelType(StringUtils.isEmpty(modelConfig.getModelSource())
            ? CommonConstant.AGENT_BUILDER_MODEL_TYPE
            : modelConfig.getModelSource());
        promptBody.setVariables(new ArrayList<>());

        // 构建模型优化参数配置
        modelConfig.setTemperature(0.1);
        modelConfig.setPresencePenalty(0.0);
        modelConfig.setMaxTokens(2048);
        promptBody.setModelConfig(modelConfig);

        // 调用大模型接口
        InvokeResp invokeResp = modelServiceCollection.callModel(workspaceId, promptBody,
            RequestContextUtils.getRequestAuthToken(), RequestContextUtils.getRequestProjectId());
        return invokeResp.getResult();
    }

    @Scheduled(cron = "0/30 * * * * ?")
    public void optimizationModelTemplate() {
        OptimizationTaskStatus runningStatus = OptimizationTaskStatus.PENDING;
        List<PeOptimizationTask> list = peOptimizationTaskMapper.queryOptimizationTaskListByStatus(runningStatus);

        for (PeOptimizationTask peOptimizationTask : list) {
            try {
                log.info("Pending optimization task id is {}", peOptimizationTask.getId());
                if (validateToken(peOptimizationTask.getCreatedOn())) {
                    PeOptimizationResult peOptimizationResult = PeOptimizationResult.builder()
                        .id(peOptimizationTask.getId())
                        .errorMsg("模板优化任务启动超时，请重新创建！")
                        .status(OptimizationTaskStatus.FAILED.getCode())
                        .build();
                    peOptimizationTaskMapper.updateOptimizationTaskResultById(peOptimizationResult);
                    return;
                }
                if (peOptimizationTaskMapper.getOptimizationTask(peOptimizationTask.getProjectId(),
                    peOptimizationTask.getTaskId(), OptimizationTaskStatus.executing(),
                    peOptimizationTask.getWorkspaceId()) == null) {
                    String projectId = peOptimizationTask.getProjectId();
                    String id = peOptimizationTask.getId();
                    String testSetId = peOptimizationTask.getTestSetId();
                    if (!Boolean.TRUE.equals(datasetMapper.isIdExist(testSetId, peOptimizationTask.getWorkspaceId()))) {
                        throw new AgentStudioException(StudioError.DATASET_FILE_DOES_NOT_EXIST);
                    }

                    Context context = Context.factory();
                    context.put(JobParamKeys.PROJECT_ID, projectId);
                    context.put(JobParamKeys.COMMON_TASK_ID, id);
                    context.put(JobParamKeys.JIUWEN_TASK_ID, "");
                    JobInstance jobInstance = jobManager.createWithoutPersist(JobSpecName.TEMPLATE_OPTIMIZATION.name(),
                        context, peOptimizationTask.getWorkspaceId());
                    String token = RequestContextUtils.getRequestAuthToken();
                    jobInstance.setProjectId(RequestContextUtils.getRequestProjectId());
                    transactionalService.execute(() -> {
                        jobManager.createAndDispatch(jobInstance, token);
                    });
                    break;
                }
            } catch (Exception exception) {
                PeOptimizationResult peOptimizationResult = PeOptimizationResult.builder()
                    .id(peOptimizationTask.getId())
                    .errorMsg("模板优化任务启动失败，请联系系统管理员！")
                    .status(OptimizationTaskStatus.FAILED.getCode())
                    .build();
                peOptimizationTaskMapper.updateOptimizationTaskResultById(peOptimizationResult);
                log.error("optimization model template {} error: {}", peOptimizationTask.getName(),
                    exception.getMessage());
            }
        }
    }

    /**
     * 九问任务状态转换为优化任务状态
     *
     * @param jiuwenStatus
     * @return
     */
    private String convertJiuwenStatus(String jiuwenStatus) {
        String status = jiuwenStatus;
        switch (jiuwenStatus) {
            case "success":
                status = "running";
                break;
            case "finished":
                status = "success";
                break;
            case "stopped":
                status = "paused";
                break;
            default:
                break;
        }
        return status;
    }

    /**
     * 验证token时效，是否在24小时内
     *
     * @param createdOn
     * @return
     */
    private boolean validateToken(Date createdOn) {
        long dayDiff = DateUtil.getDayDiff(createdOn, new Date());
        return dayDiff >= CommonConstant.NUM_ONE;
    }
}
