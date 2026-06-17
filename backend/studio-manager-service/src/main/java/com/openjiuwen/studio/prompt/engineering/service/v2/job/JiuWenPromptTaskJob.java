/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.service.v2.job;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.manager.entity.md.ModelServiceBase;
import com.openjiuwen.studio.agent.manager.mapper.md.ModelServiceMapper;
import com.openjiuwen.studio.agent.manager.utils.JsonUtils;
import com.openjiuwen.studio.prompt.engineering.constant.CommonConstant;
import com.openjiuwen.studio.prompt.engineering.dto.Case;
import com.openjiuwen.studio.prompt.engineering.dto.ModelCase;
import com.openjiuwen.studio.prompt.engineering.entity.v2.PromptBuildRequest;
import com.openjiuwen.studio.prompt.engineering.entity.v2.PromptBuildResponse;
import com.openjiuwen.studio.prompt.engineering.entity.v2.PromptFeedbackRequest;
import com.openjiuwen.studio.prompt.engineering.entity.v2.PromptTaskDetailVo;
import com.openjiuwen.studio.prompt.engineering.entity.v2.PromptVar;
import com.openjiuwen.studio.prompt.engineering.enums.ResponseCode;
import com.openjiuwen.studio.prompt.engineering.enums.v2.PromptTaskStatusEnum;
import com.openjiuwen.studio.prompt.engineering.enums.v2.PtTypeEnum;
import com.openjiuwen.studio.prompt.engineering.mapper.PeTaskMapper;
import com.openjiuwen.studio.prompt.engineering.mapper.v2.PromptTaskMapper;
import com.openjiuwen.studio.prompt.engineering.remote.ClientTemplate;
import com.openjiuwen.studio.prompt.engineering.service.model.v2.jiuwen.JIuWenPromptBaseRes;
import com.openjiuwen.studio.prompt.engineering.service.model.v2.jiuwen.JiuWenCreTaskReq;
import com.openjiuwen.studio.prompt.engineering.service.model.v2.jiuwen.JiuWenCreTaskRes;
import com.openjiuwen.studio.prompt.engineering.service.model.v2.jiuwen.JiuWenJobDeatails;
import com.openjiuwen.studio.prompt.engineering.service.model.v2.jiuwen.JiuWenJobInfo;
import com.openjiuwen.studio.prompt.engineering.service.model.v2.jiuwen.JiuWenOptimizeInfo;
import com.openjiuwen.studio.prompt.engineering.service.model.v2.jiuwen.JiuWenPromptBatchReq;
import com.openjiuwen.studio.prompt.engineering.service.model.v2.jiuwen.JiuWenPromptBatchRes;
import com.openjiuwen.studio.prompt.engineering.service.model.v2.jiuwen.JiuWenPromptDeatilRes;
import com.openjiuwen.studio.prompt.engineering.service.v2.PromptEngineerDataSetService;
import com.openjiuwen.studio.prompt.engineering.utils.JsonUtil;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JiuWenPromptTaskJob implements Job {

    private static final String TEMPLATES_OPTIMIZATION_CREATE_MULTI_API
        = "/flask/v1/MMprompt/templates_optimization/jobs";

    private static final String TEMPLATES_OPTIMIZATION_PROGRESS_MULTI_API
        = "/flask/v1/MMprompt/templates_optimization/jobs/%s";

    private static final String TEMPLATES_OPTIMIZATION_BATCH_PROGRESS_MULTI_API
        = "/flask/v1/MMprompt/templates_optimization/jobs/get_infos";

    private static final String TEMPLATES_OPTIMIZATION_STOP_MULTI_API
        = "/flask/v1/MMprompt/templates_optimization/jobs/%s/stop";

    private static final String TEMPLATES_OPTIMIZATION_RESTART_MULTI_API
        = "/flask/v1/MMprompt/templates_optimization/jobs/%s/restart";

    private static final String TEMPLATES_OPTIMIZATION_DELETE_MULTI_API
        = "/flask/v1/MMprompt/templates_optimization/jobs/%s";

    private static final String TEMPLATES_OPTIMIZATION_CREATE_API = "/flask/v1/prompt/templates_optimization/jobs";

    private static final String TEMPLATES_OPTIMIZATION_PROGRESS_API = "/flask/v1/prompt/templates_optimization/jobs/%s";

    private static final String TEMPLATES_OPTIMIZATION_BATCH_PROGRESS_API
        = "/flask/v1/prompt/templates_optimization/jobs/get_infos";

    private static final String TEMPLATES_OPTIMIZATION_STOP_API
        = "/flask/v1/prompt/templates_optimization/jobs/%s/stop";

    private static final String TEMPLATES_OPTIMIZATION_RESTART_API
        = "/flask/v1/prompt/templates_optimization/jobs/%s/restart";

    private static final String TEMPLATES_OPTIMIZATION_DELETE_API = "/flask/v1/prompt/templates_optimization/jobs/%s";

    private static final String PROMPT_GENERATE_API = "/flask/v1/prompt/build";

    private static final String PROMPT_FEEDBACK_API = "/flask/v1/prompt/optimize_feedback";

    @Value("${jiuwen.base-url:https://jiuwen-runtime.default.svc.cluster.local:31024}")
    private String jiuwenBaseUrl;

    @Resource(name = "remoteClientTemplate")
    private ClientTemplate clientTemplate;

    @Autowired
    private WebClient webClient;

    @Autowired
    private PromptTaskMapper promptTaskMapper;

    @Autowired
    private PromptEngineerDataSetService promptEngineerDataSetService;

    @Autowired
    private ModelServiceMapper modelServiceMapper;

    @Autowired
    private PeTaskMapper peTaskMapper;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            String taskParam = jobExecutionContext.getJobDetail().getJobDataMap().getString("taskParam");
            String token = jobExecutionContext.getJobDetail().getJobDataMap().getString("token");
            PromptTaskDetailVo promptTaskDetailVo = JsonUtils.json2ObjQuietly(taskParam, PromptTaskDetailVo.class);
            execTask(promptTaskDetailVo, token);
        } catch (Exception e) {
            log.error("schedule create prompt task error open, {}", e.getMessage());
            throw new JobExecutionException(e);
        }

    }

    public JiuWenJobInfo execTask(PromptTaskDetailVo promptTaskDetailVo, String token) {
        log.info("request jiuwen for create task");
        // 保存原始模型 UUID（buildJiuWenCreTaskReq 会把 model 替换为 modelName）
        String deploymentId = promptTaskDetailVo.getPtModel() != null
            ? promptTaskDetailVo.getPtModel().getModel() : null;
        JiuWenCreTaskReq req = buildJiuWenCreTaskReq(promptTaskDetailVo);
        try {
            String jiuwenPath = promptTaskDetailVo.getPtType() == PtTypeEnum.TEXT
                ? TEMPLATES_OPTIMIZATION_CREATE_API
                : TEMPLATES_OPTIMIZATION_CREATE_MULTI_API;
            HttpHeaders headers = new HttpHeaders();
            if (Objects.nonNull(token)) {
                headers.set(CommonConstant.X_AUTH_TOKEN, token);
            }
            headers.set(CommonConstant.X_WORKSPACE_ID, promptTaskDetailVo.getWorkspaceId());
            // 补充 X-Auth-Id 和 X-Deployment-Id
            enrichAuthHeaders(headers, promptTaskDetailVo.getProjectId(),
                promptTaskDetailVo.getWorkspaceId(), deploymentId);
            ResponseEntity<JiuWenCreTaskRes> response = clientTemplate.postForEntity(jiuwenBaseUrl + jiuwenPath,
                headers, JsonUtil.object2Json(req), JiuWenCreTaskRes.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                if (response.getBody() != null) {
                    log.error("Create optimization task from jiuwen server error! response:{}",
                        response.getBody().getMessage());
                }
                throw new AgentStudioException(StudioError.OPTIMIZATION_TASK_FROM_JIUWEN_SERVICE_ERROR);
            }
            JiuWenCreTaskRes jiuWenCreTaskRes = response.getBody();
            if (Objects.isNull(jiuWenCreTaskRes)) {
                log.error("Create optimization task resp is null");
                throw new AgentStudioException(StudioError.CREATE_OPTIMIZATION_TASK_RESP_NULL);
            }
            if (jiuWenCreTaskRes.getCode() != ResponseCode.OK.getCode()) {
                log.error("Create optimization task from jiuwen server error! response:{}",
                    jiuWenCreTaskRes.getMessage());
                promptTaskMapper.updateJobIdStatusByPrimaryKey(promptTaskDetailVo.getId(),
                    jiuWenCreTaskRes.getJobInfo().getId(), PromptTaskStatusEnum.FAILED.getCode(),
                    promptTaskDetailVo.getProjectId(), promptTaskDetailVo.getWorkspaceId());
                throw new AgentStudioException(StudioError.OPTIMIZE_TEMPLATE_FAILED,
                    Collections.singletonList(jiuWenCreTaskRes.getMessage()));
            }
            log.info("jiuwen create task successfully");
            promptTaskMapper.updateJobIdStatusByPrimaryKey(promptTaskDetailVo.getId(),
                jiuWenCreTaskRes.getJobInfo().getId(), PromptTaskStatusEnum.RUNNING.getCode(),
                promptTaskDetailVo.getProjectId(), promptTaskDetailVo.getWorkspaceId());
            return jiuWenCreTaskRes.getJobInfo();
        } catch (Exception e) {
            log.error("Create optimization task from jiuwen server error! ", e);
            promptTaskMapper.updateStatusByPrimaryKey(promptTaskDetailVo.getId(), PromptTaskStatusEnum.FAILED.getCode(),
                promptTaskDetailVo.getProjectId(), promptTaskDetailVo.getWorkspaceId());
            throw new AgentStudioException(StudioError.OPTIMIZATION_TASK_FROM_JIUWEN_SERVICE_ERROR);
        }
    }

    public void deleteTask(PromptTaskDetailVo promptTaskDetailVo, String token) {
        try {
            String jiuwenTaskId = promptTaskDetailVo.getJiuwenTaskId();
            log.info("delete jiuwen optimization taskId: {}", jiuwenTaskId);
            String jiuwenPath = promptTaskDetailVo.getPtType() == PtTypeEnum.TEXT
                ? TEMPLATES_OPTIMIZATION_DELETE_API
                : TEMPLATES_OPTIMIZATION_DELETE_MULTI_API;
            ResponseEntity<JIuWenPromptBaseRes> response = clientTemplate.deleteForEntity(
                jiuwenBaseUrl + String.format(jiuwenPath, jiuwenTaskId), token, JIuWenPromptBaseRes.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                if (response.getBody() != null) {
                    log.error("Delete optimization task from jiuwen server error! response:{}",
                        response.getBody().getMessage());
                }
                throw new AgentStudioException(StudioError.DELETE_OPTIMIZATION_TASK, response);
            }
            JIuWenPromptBaseRes jIuWenPromptBaseRes = response.getBody();
            if (Objects.isNull(jIuWenPromptBaseRes)) {
                log.error("Create optimization task resp is null");
                throw new AgentStudioException(StudioError.CREATE_OPTIMIZATION_TASK_RESP_NULL);
            }
            if (jIuWenPromptBaseRes.getCode() != ResponseCode.OK.getCode()) {
                log.warn("optimize template delete failed, response code: {}", jIuWenPromptBaseRes.getCode());
                throw new AgentStudioException(StudioError.OPTIMIZE_TEMPLATE_DELETE_FAILED);
            }
        } catch (ResourceAccessException e) {
            log.error("optimization template service access failed, error: {}", e.getMessage());
            throw new AgentStudioException(StudioError.OPTIMIZATION_TEMPLATE_SERVICE_ACCESS_FAILED);
        }
    }

    public JiuWenPromptDeatilRes getTaskDetail(PromptTaskDetailVo promptTaskDetailVo, String token) {
        // 后续调用九问接口查询任务详情
        try {
            String jiuwenTaskId = promptTaskDetailVo.getJiuwenTaskId();
            log.info("getOptimizationProgress jiuwenTaskId: {}", jiuwenTaskId);
            String jiuwenPath = promptTaskDetailVo.getPtType() == PtTypeEnum.TEXT
                ? TEMPLATES_OPTIMIZATION_PROGRESS_API
                : TEMPLATES_OPTIMIZATION_PROGRESS_MULTI_API;
            ResponseEntity<JiuWenPromptDeatilRes> response = clientTemplate.getForEntity(
                jiuwenBaseUrl + String.format(jiuwenPath, jiuwenTaskId), token, JiuWenPromptDeatilRes.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                if (response.getBody() != null) {
                    log.error("Get optimization task progress from jiuwen server error! response:{}",
                        response.getBody().getMessage());
                }
                throw new AgentStudioException(StudioError.GET_OPTIMIZATION_TASK);
            }
            if (Objects.isNull(response.getBody())) {
                log.info("get optimization task progress is null");
                throw new AgentStudioException(StudioError.GET_OPTIMIZATION_TASK);
            }
            return response.getBody();
        } catch (ResourceAccessException e) {
            log.error("optimization template service access failed, error: {}", e.getMessage());
            throw new AgentStudioException(StudioError.OPTIMIZATION_TEMPLATE_SERVICE_ACCESS_FAILED);
        }
    }

    /**
     * 批量查询多模态任务
     *
     * @param jiuwenTaskIds
     * @param token
     * @return
     */
    public JiuWenJobDeatails queryMultiTaskDetailsByIds(List<String> jiuwenTaskIds, String token) {
        // 后续调用九问批量任务查询接口
        try {
            log.info("getOptimizationProgress jiuwenTaskIds: {}", jiuwenTaskIds);
            ResponseEntity<JiuWenPromptBatchRes> response = clientTemplate.postForEntity(
                jiuwenBaseUrl + TEMPLATES_OPTIMIZATION_BATCH_PROGRESS_MULTI_API, token,
                JsonUtil.object2Json(JiuWenPromptBatchReq.builder().idList(jiuwenTaskIds).build()),
                JiuWenPromptBatchRes.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                if (response.getBody() != null) {
                    log.error("Get optimization task progresses from jiuwen server error! response:{}",
                        response.getBody().getMessage());
                }
                throw new AgentStudioException(StudioError.GET_OPTIMIZATION_TASK);
            }
            if (Objects.isNull(response.getBody())) {
                log.info("get optimization task progresses is null");
                throw new AgentStudioException(StudioError.GET_OPTIMIZATION_TASK);
            }
            return response.getBody().getJobDetails();
        } catch (ResourceAccessException e) {
            log.error("optimization template service access failed, error: {}", e.getMessage(), e);
            throw new AgentStudioException(StudioError.OPTIMIZATION_TEMPLATE_SERVICE_ACCESS_FAILED);
        }
    }

    /**
     * 批量查询纯文本任务
     *
     * @param jiuwenTaskIds
     * @param token
     * @return
     */
    public JiuWenJobDeatails queryTaskDetailsByIds(List<String> jiuwenTaskIds, String token) {
        // 后续调用九问批量任务查询接口
        try {
            log.info("getOptimizationProgress jiuwenTaskIds: {}", jiuwenTaskIds);
            ResponseEntity<JiuWenPromptBatchRes> response = clientTemplate.postForEntity(
                jiuwenBaseUrl + TEMPLATES_OPTIMIZATION_BATCH_PROGRESS_API, token,
                JsonUtil.object2Json(JiuWenPromptBatchReq.builder().idList(jiuwenTaskIds).build()),
                JiuWenPromptBatchRes.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                if (response.getBody() != null) {
                    log.error("Get optimization task progresses from jiuwen server error! response:{}",
                        response.getBody().getMessage());
                }
                throw new AgentStudioException(StudioError.GET_OPTIMIZATION_TASK);
            }
            if (Objects.isNull(response.getBody())) {
                log.info("get optimization task progresses is null");
                throw new AgentStudioException(StudioError.GET_OPTIMIZATION_TASK);
            }
            return response.getBody().getJobDetails();
        } catch (ResourceAccessException e) {
            log.error("optimization template service access failed, error: {}", e.getMessage(), e);
            throw new AgentStudioException(StudioError.OPTIMIZATION_TEMPLATE_SERVICE_ACCESS_FAILED);
        }
    }

    public void pauseTask(PromptTaskDetailVo promptTaskDetailVo, String token) {
        // 后续调用九问任务暂停接口
        try {
            String jiuwenTaskId = promptTaskDetailVo.getJiuwenTaskId();
            log.info("Pause OptimizationProgress jiuwenTaskId: {}", jiuwenTaskId);
            String jiuwenPath = promptTaskDetailVo.getPtType() == PtTypeEnum.TEXT
                ? TEMPLATES_OPTIMIZATION_STOP_API
                : TEMPLATES_OPTIMIZATION_STOP_MULTI_API;
            ResponseEntity<JIuWenPromptBaseRes> response = clientTemplate.postForEntity(
                jiuwenBaseUrl + String.format(jiuwenPath, jiuwenTaskId), token, null, JIuWenPromptBaseRes.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                if (response.getBody() != null) {
                    log.error("Get optimization task progresses from jiuwen server error! response:{}",
                        response.getBody().getMessage());
                }
                throw new AgentStudioException(StudioError.GET_OPTIMIZATION_TASK);
            }
        } catch (ResourceAccessException e) {
            log.error("optimization template service access failed, error: {}", e.getMessage(), e);
            throw new AgentStudioException(StudioError.OPTIMIZATION_TEMPLATE_SERVICE_ACCESS_FAILED);
        }
    }

    public void resumeTask(PromptTaskDetailVo promptTaskDetailVo, String token) {
        // 后续调用九问任务重启任务接口
        try {
            String jiuwenTaskId = promptTaskDetailVo.getJiuwenTaskId();
            log.info("resume OptimizationProgress jiuwenTaskId: {}", jiuwenTaskId);
            String jiuwenPath = promptTaskDetailVo.getPtType() == PtTypeEnum.TEXT
                ? TEMPLATES_OPTIMIZATION_RESTART_API
                : TEMPLATES_OPTIMIZATION_RESTART_MULTI_API;
            HttpHeaders headers = new HttpHeaders();
            if (Objects.nonNull(token)) {
                headers.set(CommonConstant.X_AUTH_TOKEN, token);
            }
            headers.set(CommonConstant.X_WORKSPACE_ID, promptTaskDetailVo.getWorkspaceId());
            if (promptTaskDetailVo.getPtModel() != null) {
                enrichAuthHeaders(headers, promptTaskDetailVo.getProjectId(),
                    promptTaskDetailVo.getWorkspaceId(), promptTaskDetailVo.getPtModel().getModel());
            }
            ResponseEntity<JIuWenPromptBaseRes> response = clientTemplate.postForEntity(
                jiuwenBaseUrl + String.format(jiuwenPath, jiuwenTaskId), headers, null, JIuWenPromptBaseRes.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                if (response.getBody() != null) {
                    log.error("resume optimization task progresses from jiuwen server error! response:{}",
                        response.getBody().getMessage());
                }
                throw new AgentStudioException(StudioError.GET_OPTIMIZATION_TASK);
            }
        } catch (ResourceAccessException e) {
            log.error("optimization template service access failed, error: {}", e.getMessage(), e);
            throw new AgentStudioException(StudioError.OPTIMIZATION_TEMPLATE_SERVICE_ACCESS_FAILED);
        }
    }

    public Flux<String> generatePrompt(String projectId, String workspaceId, PromptBuildRequest body, String token) {
        log.info("start to generate prompt, projectId: {}, workspaceId: {}", projectId, workspaceId);

        // 保存原始模型 UUID 作为 deployment_id（enrichment 会替换 model 为 modelName）
        String deploymentId = body.getModelInfo() != null ? body.getModelInfo().getModel() : null;

        // 补充 modelInfo 的 model name 等字段
        if (body.getModelInfo() != null) {
            enrichExecConfig(body.getModelInfo(), projectId, workspaceId);
        }

        // 查询 auth_id
        String authId = null;
        if (StringUtils.isNotEmpty(deploymentId)) {
            try {
                authId = peTaskMapper.selectModelAuthId(projectId, workspaceId, deploymentId);
            } catch (Exception e) {
                log.warn("Failed to query auth_id for model {}: {}", deploymentId, e.getMessage());
            }
        }

        String finalAuthId = authId;
        String finalDeploymentId = deploymentId != null ? deploymentId : "";
        Flux<String> dataFlux = webClient.post()
            .uri(jiuwenBaseUrl + PROMPT_GENERATE_API)
            .contentType(MediaType.APPLICATION_JSON)
            .header(CommonConstant.X_AUTH_TOKEN, token)
            .header(CommonConstant.X_WORKSPACE_ID, workspaceId)
            .header("X-Deployment-Id", finalDeploymentId)
            .header("X-Auth-Id", finalAuthId != null ? finalAuthId : "")
            .bodyValue(body)
            .retrieve()
            // 处理HTTP非2xx状态码：直接抛异常
            .onStatus(status -> !status.is2xxSuccessful(),
                response -> response.bodyToMono(String.class).flatMap(errorBody -> {
                    String errorMsg = String.format("Prompt generate failed:  status=%s, error=%s",
                        response.statusCode(), errorBody);
                    log.error(errorMsg);
                    // 抛出异常，终止流（不返回任何数据）
                    return Mono.error(
                        new AgentStudioException(StudioError.OPTIMIZATION_TEMPLATE_SERVICE_ACCESS_FAILED));
                }))
            .bodyToFlux(String.class)
            .mapNotNull(chunk -> {
                PromptBuildResponse response = JsonUtils.json2ObjQuietly(chunk, PromptBuildResponse.class);
                return response != null ? chunk : ""; // 过滤无效 JSON
            })
            .filter(chunk -> chunk != null && !chunk.trim().isEmpty())
            .onErrorResume(Exception.class, e -> {
                // 直接抛出异常，流终止，不会执行后续的[Done]拼接
                return Flux.error(new AgentStudioException(StudioError.OPTIMIZATION_TEMPLATE_SERVICE_ACCESS_FAILED));
            });

        Flux<String> doneFlux = Flux.just("[Done]");

        log.info("prompt generation completed, projectId: {}, workspaceId: {}", projectId, workspaceId);
        return Flux.concat(dataFlux, doneFlux);
    }

    public Flux<String> optimizeFeedback(String projectId, String workspaceId, PromptFeedbackRequest body,
        String token) {
        // 判断是一键生成还是反馈优化
        if (StringUtils.isEmpty(body.getPrompt())) {
            // 一键生成

            if (ObjectUtils.isEmpty(body.getModelInfo())) {
                log.error("generate prompt doesn't have extension info");
                throw new AgentStudioException(StudioError.PROMPT_FEEDBACK_PARAM_ERROR);
            }
            String instruct = "";
            for (Map.Entry<String, String> entry : body.getExtension_info().entrySet()) {
                instruct += entry.getKey() + ":" + entry.getValue() + "\n;";
            }

            PromptBuildRequest buildRequest = PromptBuildRequest.builder()
                .instruct(instruct)
                .tools(new ArrayList<>())
                .modelInfo(body.getModelInfo())
                .stream(true)
                .build();

            return generatePrompt(projectId, workspaceId, buildRequest, token);

        } else {
            // 反馈优化，补充反馈信息

            String feeback = "";
            for (Map.Entry<String, String> entry : body.getExtension_info().entrySet()) {
                feeback += entry.getKey() + ":" + entry.getValue() + "\n;";
            }
            if (StringUtils.isEmpty(body.getFeedback())) {
                body.setFeedback(feeback + "对提示词进一步丰富一些");
            } else {
                body.setFeedback(body.getFeedback() + "\n" + feeback);
            }

            // 查询 auth_id 和 deployment_id
            String deploymentId = body.getModelInfo() != null ? body.getModelInfo().getModel() : null;
            String authId = null;
            if (StringUtils.isNotEmpty(deploymentId)) {
                try {
                    authId = peTaskMapper.selectModelAuthId(projectId, workspaceId, deploymentId);
                } catch (Exception e) {
                    log.warn("Failed to query auth_id for model {}: {}", deploymentId, e.getMessage());
                }
            }

            String finalAuthId = authId;
            String finalDeploymentId = deploymentId != null ? deploymentId : "";
            Flux<String> dataFlux = webClient.post()
                .uri(jiuwenBaseUrl + PROMPT_FEEDBACK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .header(CommonConstant.X_AUTH_TOKEN, token)
                .header("X-Deployment-Id", finalDeploymentId)
                .header("X-Auth-Id", finalAuthId != null ? finalAuthId : "")
                .bodyValue(body)
                .retrieve()
                // 处理HTTP非2xx状态码：直接抛异常
                .onStatus(status -> !status.is2xxSuccessful(),
                    response -> response.bodyToMono(String.class).flatMap(errorBody -> {
                        String errorMsg = String.format("Prompt generate failed:  status=%s, error=%s",
                            response.statusCode(), errorBody);
                        log.error(errorMsg);
                        // 抛出异常，终止流（不返回任何数据）
                        return Mono.error(
                            new AgentStudioException(StudioError.OPTIMIZATION_TEMPLATE_SERVICE_ACCESS_FAILED));
                    }))
                .bodyToFlux(String.class)
                .mapNotNull(chunk -> {
                    PromptBuildResponse response = JsonUtils.json2ObjQuietly(chunk, PromptBuildResponse.class);
                    return response != null ? chunk : ""; // 过滤无效 JSON
                })
                .filter(chunk -> chunk != null && !chunk.trim().isEmpty())
                .onErrorResume(Exception.class, e -> {
                    // 直接抛出异常，流终止，不会执行后续的[Done]拼接
                    return Flux.error(
                        new AgentStudioException(StudioError.OPTIMIZATION_TEMPLATE_SERVICE_ACCESS_FAILED));
                });
            Flux<String> doneFlux = Flux.just("[Done]");

            return Flux.concat(dataFlux, doneFlux);
        }
    }

    private JiuWenCreTaskReq buildJiuWenCreTaskReq(PromptTaskDetailVo promptTaskDetailVo) {
        log.info("start to build JiuWenCreTaskReq, task name: {}", promptTaskDetailVo.getName());

        List<PromptVar> varsConfig = promptTaskDetailVo.getPtVars();
        Map<String, PromptVar> varConfigMap = varsConfig.stream()
            .collect(Collectors.toMap(PromptVar::getName, var -> var));

        List<List<PromptVar>> evalDataList = promptEngineerDataSetService.getAllEvalDataSet(promptTaskDetailVo.getId(),
            promptTaskDetailVo.getProjectId(), promptTaskDetailVo.getWorkspaceId());

        List<ModelCase> cases = new ArrayList<>();
        evalDataList.forEach(promptVars -> {
            Map<String, String> varMap = new HashMap<>();
            Map<String, String> varTypeMap = new HashMap<>();

            for (PromptVar var : promptVars) {
                if (varConfigMap.containsKey(var.getName())) {
                    varMap.put(var.getName(), var.getContent());
                    varTypeMap.put(var.getName(),
                        varConfigMap.get(var.getName()).getType() == PtTypeEnum.MULTI ? "image" : "text");
                }
            }

            String outPut = varMap.get(CommonConstant.AGENT_BUILDER_PROMPT_OUTPUT);
            varMap.remove(CommonConstant.AGENT_BUILDER_PROMPT_OUTPUT);
            varTypeMap.remove(CommonConstant.AGENT_BUILDER_PROMPT_OUTPUT);

            Case userMessage = Case.builder()
                .role("user")
                .content("<RAW_PROMPT>")
                .variable(varMap)
                .variableType(varTypeMap)
                .build();
            Case assistantMessage = Case.builder()
                .role("assistant")
                .content(outPut)
                .variable(new HashMap<>())
                .variableType(new HashMap<>())
                .build();
            ModelCase modelCase = ModelCase.builder().messages(List.of(userMessage, assistantMessage)).build();
            cases.add(modelCase);
        });

        JiuWenOptimizeInfo jiuWenOptimizeInfo = JiuWenOptimizeInfo.builder()
            .cases(cases)
            .numIter(promptTaskDetailVo.getMaxIterNum())
            .earlyStopScore(promptTaskDetailVo.getTargetAcc())
            .exampleNum(promptTaskDetailVo.getShowCaseNum())
            .placeholder(new ArrayList<>())
            .llmParallel(2)
            .cotExampleNum(0)
            .optimizeMethod("JOINT")
            .evaluationMethod("LLM")
            .userCompareOptions(promptTaskDetailVo.getTargetType().toString())
            .userCompareRules(promptTaskDetailVo.getScoreStandard())
            .externalKnowledge(promptTaskDetailVo.getBackKnowledge())
            .optimizeAlgorithm("beamsearch")
            .build();

        JiuWenCreTaskReq jiuWenCreTaskReq = JiuWenCreTaskReq.builder()
            .name(promptTaskDetailVo.getName())
            .desc(promptTaskDetailVo.getDesc())
            .rawTemplates(promptTaskDetailVo.getPtText())
            .modelInfo(enrichExecConfig(promptTaskDetailVo.getPtModel(),
                promptTaskDetailVo.getProjectId(), promptTaskDetailVo.getWorkspaceId()))
            .assistantInfo(enrichExecConfig(promptTaskDetailVo.getExecObject(),
                promptTaskDetailVo.getProjectId(), promptTaskDetailVo.getWorkspaceId()))
            .optimizeInfo(jiuWenOptimizeInfo)
            .build();

        log.info("JiuWenCreTaskReq built successfully, task name: {}", promptTaskDetailVo.getName());
        return jiuWenCreTaskReq;
    }

    /**
     * Enrich ExecConfig with model name from the model service database.
     * api_key、url、auth_id、deployment_id 不通过请求体传输：
     * - api_key/url 由 runtime 端从环境变量读取
     * - auth_id/deployment_id 通过 HTTP 请求头透传（见 enrichAuthHeaders）
     */
    private com.openjiuwen.studio.prompt.engineering.entity.v2.ExecConfig enrichExecConfig(
            com.openjiuwen.studio.prompt.engineering.entity.v2.ExecConfig config,
            String projectId, String workspaceId) {
        if (config == null || StringUtils.isEmpty(config.getModel())) {
            return config;
        }
        try {
            ModelServiceBase modelService = modelServiceMapper.queryById(config.getModel());
            if (modelService != null) {
                String originalId = config.getModel();
                config.setModel(modelService.getModelName());
                log.info("Enriched model config: id={}, modelName={}", originalId, modelService.getModelName());

                // 设置 top_p 和 temperature 默认值（前端可能不传）
                if (config.getTopP() == null) {
                    config.setTopP(0.1);
                }
                if (config.getTemperature() == null) {
                    config.setTemperature(0.1);
                }
            } else {
                log.warn("Model service not found for id: {}", config.getModel());
            }
        } catch (Exception e) {
            log.warn("Failed to enrich model config for model {}: {}", config.getModel(), e.getMessage());
        }
        return config;
    }

    /**
     * 查询模型的 auth_id 和 deployment_id，写入 HTTP 请求头。
     * Flask 端会将 HTTP 请求头合并到 modelInfo.headers，最终由 PromptOptimizeModelProvider 提取。
     */
    private void enrichAuthHeaders(HttpHeaders headers, String projectId, String workspaceId, String modelId) {
        if (StringUtils.isEmpty(modelId)) {
            return;
        }
        // deployment_id: 模型 UUID，模型路由服务用于定位目标模型
        headers.set("X-Deployment-Id", modelId);
        // auth_id: 从 t_provider_auth_info 查询
        try {
            String authId = peTaskMapper.selectModelAuthId(projectId, workspaceId, modelId);
            if (StringUtils.isNotEmpty(authId)) {
                headers.set("X-Auth-Id", authId);
                log.info("Auth ID set in HTTP header: {}", authId);
            }
        } catch (Exception e) {
            log.warn("Failed to query auth_id for model {}: {}", modelId, e.getMessage());
        }
    }

}
