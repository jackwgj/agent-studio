package com.openjiuwen.studio.agent.space.app.handler;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.openjiuwen.studio.agent.space.api.vo.AgentBuilderDispatchVo;
import com.openjiuwen.studio.agent.space.api.vo.AgentBuilderFileVo;
import com.openjiuwen.studio.agent.space.api.vo.response.deepresearch.ChunkResp;
import com.openjiuwen.studio.agent.space.api.vo.response.deepresearch.EndContent;
import com.openjiuwen.studio.agent.space.api.vo.response.deepresearch.Latency;
import com.openjiuwen.studio.agent.space.api.vo.response.deepresearch.OutlineContent;
import com.openjiuwen.studio.agent.space.app.constant.Constant;
import com.openjiuwen.studio.agent.space.app.constant.RedisKeyConstant;
import com.openjiuwen.studio.agent.space.app.enums.DRTaskStatusType;
import com.openjiuwen.studio.agent.space.app.enums.AgentBuilderUploadScene;
import com.openjiuwen.studio.agent.space.app.task.DRTaskHandler;
import com.openjiuwen.studio.agent.space.app.util.AppCommonUtils;
import com.openjiuwen.studio.agent.space.app.util.FileTransferUtils;
import com.openjiuwen.studio.agent.space.app.util.AgentBuilderTransformUtil;
import com.openjiuwen.studio.agent.space.common.exception.AgentSpaceErrorCodes;
import com.openjiuwen.studio.agent.space.common.exception.AgentSpaceException;
import com.openjiuwen.studio.agent.space.common.utils.redis.RedisClient;
import com.openjiuwen.studio.agent.space.dao.application.AgentBuilderActionMapper;
import com.openjiuwen.studio.agent.space.dao.application.AgentBuilderFileMapper;
import com.openjiuwen.studio.agent.space.dao.application.AgentBuilderStepMapper;
import com.openjiuwen.studio.agent.space.dao.entity.AgentBuilderActionEntity;
import com.openjiuwen.studio.agent.space.dao.entity.AgentBuilderFileEntity;
import com.openjiuwen.studio.agent.space.dao.entity.AgentBuilderStepEntity;

import io.netty.util.internal.StringUtil;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class DREventSourceListener extends EventSourceListener {
    @Setter
    private AgentBuilderActionMapper AgentBuilderActionMapper;

    @Setter
    private AgentBuilderStepMapper AgentBuilderStepMapper;

    @Setter
    private FileTransferUtils fileTransferUtil;

    @Setter
    private AgentBuilderTransformUtil AgentBuilderTransformUtil;

    @Setter
    private AgentBuilderFileMapper AgentBuilderFileMapper;

    @Setter
    private DRTaskHandler drTaskHandler;

    @Setter
    private RedisClient redisClient;

    private static final Pattern JSON_CODE_BLOCK_PATTERN = Pattern.compile("```json\\s*(.*?)\\s*```", Pattern.DOTALL);

    private final String errorMsg = new AgentSpaceException(AgentSpaceErrorCodes.AGENT_BUILDER_SERVICE_INTERNAL_ERROR,
        "Request backend error!").getErrorDesc();

    private final Timestamp startTime;

    private Timestamp endTime;

    private final ConcurrentHashMap<String, String> stepMap = new ConcurrentHashMap<>();

    private final AgentBuilderDispatchVo AgentBuilderDispatchVo;

    // 生成大纲时会返回标题，最后上传文件时需要用到
    private String fileTitle = "";

    private final String taskId;

    private final Runnable onClosedCallback;

    public DREventSourceListener(AgentBuilderDispatchVo AgentBuilderDispatchVo, Runnable onClosedCallback) {
        this.AgentBuilderDispatchVo = AgentBuilderDispatchVo;
        taskId = AgentBuilderDispatchVo.getTaskId();
        this.onClosedCallback = onClosedCallback;
        startTime = AgentBuilderDispatchVo.getStartTime();
        endTime = startTime;
    }

    @Override
    public void onOpen(EventSource eventSource, Response response) {
        log.info("[EventSourceListener] {} on open", taskId);
    }

    @Override
    public void onEvent(EventSource eventSource, String id, String type, String data) {
        log.info("[EventSourceListener] {} on event", taskId);
        dealEvent(eventSource, data);
    }

    @Override
    public void onFailure(EventSource eventSource, Throwable t, Response response) {
        log.info("[EventSourceListener] {} on failure", taskId);
        cleanup();

        String msg = errorMsg;
        if (response != null && response.body() != null) {
            try {
                String body = response.body().string();
                JSONObject jsonObject = JSON.parseObject(body);
                msg = new AgentSpaceException(AgentSpaceErrorCodes.AGENT_BUILDER_SERVICE_INTERNAL_ERROR,
                    "Request backend error!" + (jsonObject != null
                        ? jsonObject.getString("error_msg")
                        : body)).getErrorDesc();
            } catch (Exception e) {
                log.error("[EventSourceListener] parse response error", e);
            }
        }

        String nowStatus = redisClient.get(String.format(RedisKeyConstant.DR_STATUS, taskId));
        if (!Objects.equals(nowStatus, String.valueOf(AgentBuilderDispatchVo.getTaskStatus()))) {
            return;
        }
        log.error("[EventSourceListener] err {} on failure {}", taskId, msg, t);
        createErrorAction(AgentBuilderDispatchVo, msg);
    }

    @Override
    public void onClosed(EventSource eventSource) {
        log.info("[EventSourceListener] {} on closed", taskId);
        cleanup();

        String nowStatus = redisClient.get(String.format(RedisKeyConstant.DR_STATUS, taskId));
        if (!Objects.equals(nowStatus, String.valueOf(AgentBuilderDispatchVo.getTaskStatus()))) {
            return;
        }
        log.error("[EventSourceListener] err {} on closed ", taskId);
        createErrorAction(AgentBuilderDispatchVo, errorMsg);
    }

    private void cleanup() {
        try {
            onClosedCallback.run();
        } catch (Exception e) {
            log.error("[EventSourceListener] Failed to execute onClosedCallback", e);
        }
    }

    /**
     * 处理事件源数据。
     * 该方法主要负责处理从事件源接收到的数据，并根据数据内容执行相应的操作，如创建步骤、处理大纲、推送数据到Redis等。
     *
     * @param eventSource 事件源对象，用于取消事件监听。
     * @param dataContent 事件数据内容，包含chunk响应信息。
     */
    private void dealEvent(EventSource eventSource, String dataContent) {
        // 获取当前任务的状态
        log.info("[dealEvent] deal event for task: {} status: {}.", taskId, AgentBuilderDispatchVo.getTaskStatus());
        String nowStatus = redisClient.get(String.format(RedisKeyConstant.DR_STATUS, taskId));
        // 检查当前任务状态是否与任务对象中的状态一致
        if (!Objects.equals(nowStatus, String.valueOf(AgentBuilderDispatchVo.getTaskStatus()))) {
            // 如果状态不一致，记录警告日志并取消事件源
            log.warn("[dealEvent] suspend task: {}  status: {} {}", taskId, AgentBuilderDispatchVo.getTaskStatus(),
                nowStatus);
            eventSource.cancel();
            return;
        }

        // 检查数据内容是否为空
        if (dataContent == null || dataContent.isEmpty()) {
            return;
        }

        try {
            // 将数据内容解析为ChunkResp对象
            ChunkResp chunkResp = JSONObject.parseObject(dataContent, ChunkResp.class);
            if (chunkResp == null) {
                // 如果解析失败，记录错误日志，创建错误动作并取消事件源
                log.error("[dealEvent] Failed to parse chunk response for task: {}", taskId);
                createErrorAction(AgentBuilderDispatchVo, "Invalid response format");
                eventSource.cancel();
                return;
            }
            if (Objects.equals(chunkResp.getEvent(), "heartbeat")) {
                // 忽略心跳包
                return;
            }

            // 获取sectionIdx
            String sectionIdx = chunkResp.getSectionIdx();
            if (sectionIdx == null) {
                // 如果sectionIdx为空，记录错误日志，创建错误动作并取消事件源
                log.error("[dealEvent] sectionIdx is null task: {} chunk {}", taskId, chunkResp);
                createErrorAction(AgentBuilderDispatchVo,
                    (chunkResp.getContent() == null || chunkResp.getContent().isEmpty())
                        ? "model error"
                        : chunkResp.getContent());
                eventSource.cancel();
                return;
            }

            // 记录接收到的数据信息
            log.info("[dealEvent] get data task: {} idx: {} agent: {} event: {}", taskId, chunkResp.getSectionIdx(),
                chunkResp.getAgent(), chunkResp.getEvent());

            // 如果是sectionIdx为0且stepMap中不包含该sectionIdx，创建主步骤
            if (sectionIdx.equals("0") && !stepMap.containsKey(chunkResp.getSectionIdx())) {
                creatMainStep(AgentBuilderDispatchVo, stepMap, chunkResp);
            }

            // 如果agent为"outline"且content不为空，处理大纲
            if (Objects.equals(chunkResp.getAgent(), "outline") && chunkResp.getContent() != null) {
                dealOutline(chunkResp);
            }

            // 处理chunk响应并进行归档操作
            log.info("[dealEvent] process chunk response for task: {}", taskId);
            processChunkResponse(AgentBuilderDispatchVo, chunkResp, stepMap, fileTitle);

            // 将数据推送到Redis
            log.info("[dealEvent] push chunk response to Redis: {}", taskId);
            pushToRedis(chunkResp);
        } catch (Exception e) {
            // 如果发生异常，记录错误日志，创建错误动作并取消事件源
            log.error("[dealEvent] JSON parsing error for task: {}, data: {}", taskId, dataContent, e);
            createErrorAction(AgentBuilderDispatchVo, dataContent);
            eventSource.cancel();
        }
    }

    private void pushToRedis(ChunkResp chunkResp) {
        // 由lPush修正为rPush，原因：修正前redisUtils.lPush内部实现用的rPush
        redisClient.rPush(String.format(RedisKeyConstant.DR_CHUNKS, taskId), JSON.toJSONString(chunkResp));
        redisClient.expire(String.format(RedisKeyConstant.DR_CHUNKS, taskId),
            Duration.ofSeconds(Constant.REDIS_EXPIRE_TIME));
    }

    /**
     * 处理大纲
     */
    private void dealOutline(ChunkResp chunkResp) {
        try {
            Matcher matcher = JSON_CODE_BLOCK_PATTERN.matcher(chunkResp.getContent());
            OutlineContent outlineContent;
            if (matcher.find()) {
                outlineContent = JSONObject.parseObject(matcher.group(1).trim(), OutlineContent.class);
            } else {
                outlineContent = JSONObject.parseObject(chunkResp.getContent(), OutlineContent.class);
            }
            if (outlineContent.getTitle() != null) {
                fileTitle = outlineContent.getTitle();
                drTaskHandler.updateTaskNameByAgent(taskId, fileTitle);
            }

            creatSubStep(AgentBuilderDispatchVo, outlineContent, stepMap);
        } catch (Exception e) {
            log.error("[dealOutline] Failed to parse outline content for task: {}", taskId, e);
            throw new AgentSpaceException(AgentSpaceErrorCodes.AGENT_BUILDER_ACCESS_INTERNAL_ERROR,
                "Outline parsing failed");
        }
    }

    /**
     * 归档step
     */
    private void creatMainStep(AgentBuilderDispatchVo AgentBuilderDispatchVo, ConcurrentHashMap<String, String> stepMap,
        ChunkResp chunkResp) {
        AgentBuilderStepEntity AgentBuilderStepEntity = new AgentBuilderStepEntity().setId(AppCommonUtils.getUUID())
            .setAgentName("deepResearch")
            .setMessageId(AgentBuilderDispatchVo.getAnswerId())
            .setFinished(false);
        AppCommonUtils.setCommonCreateProperties(AgentBuilderStepEntity);
        stepMap.put(chunkResp.getSectionIdx(), AgentBuilderStepEntity.getId());
        AgentBuilderStepMapper.insert(AgentBuilderStepEntity);
    }

    /**
     * 归档子step
     */
    private void creatSubStep(AgentBuilderDispatchVo AgentBuilderDispatchVo, OutlineContent outlineContent,
        ConcurrentHashMap<String, String> stepMap) {
        List<AgentBuilderStepEntity> stepEntities = new ArrayList<>();
        for (int i = 0; i < outlineContent.getSections().size(); i++) {
            AgentBuilderStepEntity AgentBuilderStepEntity = new AgentBuilderStepEntity().setId(AppCommonUtils.getUUID())
                .setAgentName("deepResearch")
                .setMessageId(AgentBuilderDispatchVo.getAnswerId())
                .setFinished(false);
            AppCommonUtils.setCommonCreateProperties(AgentBuilderStepEntity);
            stepEntities.add(AgentBuilderStepEntity);
            stepMap.put(String.valueOf(i + 1), AgentBuilderStepEntity.getId());
        }
        AgentBuilderStepMapper.batchInsert(stepEntities);
    }

    /**
     * 归档chunk
     */
    public void processChunkResponse(AgentBuilderDispatchVo dispatchVo, ChunkResp chunkResp,
        ConcurrentHashMap<String, String> stepMap, String fileTitle) {
        log.info("[processChunkResponse] start to process chunk resonse.");
        String sectionIdx = chunkResp.getSectionIdx();
        if (sectionIdx == null) {
            log.info("[processChunkResponse] SectionIdx is null, skipping processing.");
            return;
        }

        if (!stepMap.containsKey(sectionIdx)) { // 检查stepMap中是否包含当前sectionIdx对应的stepId
            log.error("[processChunkResponse] Failed to get step entity for task: {} section: {}",
                dispatchVo.getTaskId(), sectionIdx);
            return;
        }

        String stepId = stepMap.get(sectionIdx); // 从stepMap中获取当前sectionIdx对应的stepId

        String fileId = handleFileCreation(chunkResp, dispatchVo, fileTitle); // 处理文件创建
        log.info("[processChunkResponse] File creation result: {}", fileId);

        handleActionCreation(dispatchVo, chunkResp, stepId, fileId); // 处理动作创建

        updateStepCompletionStatus(stepId, chunkResp); // 更新步骤完成状态
        log.info("[processChunkResponse] Step completion status updated for stepId: {}", stepId);
    }

    /**
     * 获取研究报告，并上传到obs
     */
    String handleFileCreation(ChunkResp chunkResp, AgentBuilderDispatchVo dispatchVo, String fileTitle) {
        // 检查chunkResp是否满足条件
        if (!Objects.equals(chunkResp.getSectionIdx(), "0") || !Objects.equals(chunkResp.getAgent(), "end")
            || !Objects.equals(chunkResp.getEvent(), "summary_response") || Objects.equals(chunkResp.getContent(),
            "ALL END")) {
            return "";
        }

        // 解析内容为EndContent对象
        EndContent endContent = JSONObject.parseObject(chunkResp.getContent(), EndContent.class);
        String responseContent = endContent.getResponseContent();
        String exceptionInfo = endContent.getExceptionInfo();
        // 1. 核心判断：响应内容为空，直接抛出异常
        if (StringUtil.isNullOrEmpty(responseContent)) {
            changeTaskStatus(DRTaskStatusType.FAIL.getStatus());
            log.error("[handleFileCreation] Response content is null or empty! WarningInfo: {}, ExceptionInfo: {}",
                endContent.getWarningInfo(), exceptionInfo);
            return "";
        }

        // 创建AgentBuilderFileVo对象并设置内容
        AgentBuilderFileVo fileVo = new AgentBuilderFileVo();
        // 2. 响应内容不为空，但存在异常信息 → 打印日志，继续执行
        if (!StringUtil.isNullOrEmpty(exceptionInfo)) {
            log.warn("[handleFileCreation] Request has exception info. WarningInfo: {}, ExceptionInfo: {}",
                endContent.getWarningInfo(), exceptionInfo);
            fileVo.setContent("系统出现故障或错误，建议重新生成报告。\n\n" + responseContent);
        } else {
            fileVo.setContent(responseContent);
        }

        // 设置文件名
        String fileName = (fileTitle.isEmpty() ? "Final Report" : fileTitle) + ".md";
        log.info("[handleFileCreation] File name set to: {}", fileName);

        // 创建AgentBuilderFile对象
        FileTransferUtils.AgentBuilderFile file = new FileTransferUtils.AgentBuilderFile().setBytes(
            fileVo.getContent().getBytes(StandardCharsets.UTF_8)).setOriginalFilename(fileName);
        log.info("[handleFileCreation] Created AgentBuilderFile object with original filename: {}", fileName);

        // 上传文件并获取传输结果
        FileTransferUtils.FileTransferResult fileTransferResult = fileTransferUtil.uploadTaskFile(file,
            dispatchVo.getTaskId(), AgentBuilderUploadScene.RUNTIME_UPLOAD);
        log.info("[handleFileCreation] File upload result: {}", fileTransferResult);

        // 填充文件实体内容并转换为AgentBuilderFileEntity
        fileTransferResult.fillAgentBuilderFileEntityContent(fileVo);
        AgentBuilderFileEntity fileEntity = AgentBuilderTransformUtil.fileVo2FileEntity(fileVo);
        fileEntity.setName(fileName);
        fileEntity.setTaskId(dispatchVo.getTaskId());
        log.info("[handleFileCreation] Created AgentBuilderFileEntity with name: {} and task ID: {}", fileName,
            dispatchVo.getTaskId());

        // 将文件实体插入数据库
        int insertCount = AgentBuilderFileMapper.insert(fileEntity);
        if (insertCount == 0) {
            log.error("[handleFileCreation] Failed to create file entity for task: {}", dispatchVo.getTaskId());
            return "";
        }

        // 设置文件ID到endContent并打印详细信息
        endContent.setFileId(fileEntity.getId());
        log.info("[handleFileCreation] endContent details: {}", endContent);
        log.info("[handleFileCreation] fileEntity details: {}", fileEntity);

        // 清空响应内容并更新chunkResp
        endContent.setResponseContent("");
        chunkResp.setContent(JSONObject.toJSONString(endContent));
        log.info("[handleFileCreation] chunkResp details: {}", chunkResp);

        // 返回文件实体ID
        return fileEntity.getId();
    }

    /**
     * 将chunk归档为action
     */
    private void handleActionCreation(AgentBuilderDispatchVo dispatchVo, ChunkResp chunkResp, String stepId,
        String fileId) {
        AgentBuilderActionEntity actionEntity = new AgentBuilderActionEntity().setId(AppCommonUtils.getUUID())
            .setStepId(stepId)
            .setType(chunkResp.getAgent())
            .setFileList(fileId.isEmpty() ? Collections.emptyList() : Collections.singletonList(fileId))
            .setToolType(Objects.equals(chunkResp.getAgent(), "collector_info_retrieval") ? "search" : "")
            .setContent(JSON.toJSONString(chunkResp))
            .setFinished(1);
        AppCommonUtils.setCommonCreateProperties(actionEntity);
        log.info("[handleActionCreation] actionEntity: {}", actionEntity);
        AgentBuilderActionMapper.insert(actionEntity);
        endTime = actionEntity.getCreatedDate();
        log.debug("[handleActionCreation] Created action entity with task: {} id: {} for step: {}",
            dispatchVo.getTaskId(), actionEntity.getId(), stepId);
    }

    /**
     * 更新步骤完成状态
     */
    private void updateStepCompletionStatus(String stepId, ChunkResp chunkResp) {
        log.info("[updateStepCompletionStatus] start to update.");
        if (!Objects.equals(chunkResp.getAgent(), "end") && !Objects.equals(chunkResp.getAgent(), "feedback_handler")) {
            return;
        }
        if (chunkResp.getSectionIdx().equals("0")) {
            int endStatus = Objects.equals(chunkResp.getAgent(), "feedback_handler")
                ? DRTaskStatusType.WAITING_USER_INPUT.getStatus()
                : DRTaskStatusType.END.getStatus();
            changeTaskStatus(endStatus);
        }

        AgentBuilderStepEntity stepEntity = new AgentBuilderStepEntity().setId(stepId).setFinished(true);
        AppCommonUtils.setCommonCreateProperties(stepEntity);
        AgentBuilderStepMapper.updateStepFinishedById(stepEntity);
        log.info("[updateStepCompletionStatus] Marked step {} as completed for section: {} event: {}", stepId,
            chunkResp.getSectionIdx(), chunkResp.getEvent());
    }

    private void changeTaskStatus(int endStatus) {
        double runTime = (endTime.getTime() - startTime.getTime()) / 1_000.0;
        boolean flag = drTaskHandler.updateTaskStatus(taskId, endStatus);
        if (flag) {
            ChunkResp statisticChunk = new ChunkResp().setCreatedTime(new Timestamp(System.currentTimeMillis()))
                .setEvent("statistic_data")
                .setLatency(new Latency().setOverall(Math.round(runTime * 100.0) / 100.0));
            pushToRedis(statisticChunk);
        }
    }

    /**
     * 调用runtime异常时，归档相关信息
     *
     * @param AgentBuilderDispatchVo
     * @param msg                 异常信息
     */
    private void createErrorAction(AgentBuilderDispatchVo AgentBuilderDispatchVo, String msg) {
        ChunkResp chunkResp = new ChunkResp().setMessageId(AppCommonUtils.getUUID())
            .setContent(msg)
            .setAgent("error")
            .setEvent("error")
            .setConversationId(taskId)
            .setMessageType("message_chunk")
            .setSectionIdx("0");
        pushToRedis(chunkResp);

        AgentBuilderStepEntity AgentBuilderStepEntity = new AgentBuilderStepEntity().setId(AppCommonUtils.getUUID())
            .setAgentName("deepResearch")
            .setMessageId(AgentBuilderDispatchVo.getAnswerId())
            .setFinished(true);
        AppCommonUtils.setCommonCreateProperties(AgentBuilderStepEntity);
        AgentBuilderStepMapper.insert(AgentBuilderStepEntity);

        AgentBuilderActionEntity actionEntity = new AgentBuilderActionEntity().setId(AppCommonUtils.getUUID())
            .setStepId(AgentBuilderStepEntity.getId())
            .setType("error")
            .setFileList(Collections.emptyList())
            .setToolType("")
            .setContent(JSON.toJSONString(chunkResp))
            .setFinished(1);
        AppCommonUtils.setCommonCreateProperties(actionEntity);
        AgentBuilderActionMapper.insert(actionEntity);
        endTime = actionEntity.getCreatedDate();

        changeTaskStatus(DRTaskStatusType.FAIL.getStatus());
    }
}
