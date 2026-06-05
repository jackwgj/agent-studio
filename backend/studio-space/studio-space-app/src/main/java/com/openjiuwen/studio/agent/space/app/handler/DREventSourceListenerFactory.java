package com.openjiuwen.studio.agent.space.app.handler;

import com.openjiuwen.studio.agent.space.api.vo.AgentBuilderDispatchVo;
import com.openjiuwen.studio.agent.space.app.task.DRTaskHandler;
import com.openjiuwen.studio.agent.space.app.util.FileTransferUtils;
import com.openjiuwen.studio.agent.space.app.util.AgentBuilderTransformUtil;
import com.openjiuwen.studio.agent.space.common.utils.redis.RedisClient;
import com.openjiuwen.studio.agent.space.dao.application.AgentBuilderActionMapper;
import com.openjiuwen.studio.agent.space.dao.application.AgentBuilderFileMapper;
import com.openjiuwen.studio.agent.space.dao.application.AgentBuilderStepMapper;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

@Component
public class DREventSourceListenerFactory {
    @Resource
    private AgentBuilderActionMapper AgentBuilderActionMapper;

    @Resource
    private AgentBuilderStepMapper AgentBuilderStepMapper;

    @Resource
    private FileTransferUtils fileTransferUtil;

    @Resource
    private AgentBuilderTransformUtil AgentBuilderTransformUtil;

    @Resource
    private AgentBuilderFileMapper AgentBuilderFileMapper;

    @Resource
    private DRTaskHandler drTaskHandler;

    @Resource
    private RedisClient redisClient;

    public DREventSourceListener create(AgentBuilderDispatchVo AgentBuilderDispatchVo, Runnable onClosedCallback) {
        DREventSourceListener listener = new DREventSourceListener(AgentBuilderDispatchVo, onClosedCallback);
        // 手动注入 Spring 管理的依赖
        listener.setAgentBuilderActionMapper(AgentBuilderActionMapper);
        listener.setAgentBuilderStepMapper(AgentBuilderStepMapper);
        listener.setFileTransferUtil(fileTransferUtil);
        listener.setAgentBuilderTransformUtil(AgentBuilderTransformUtil);
        listener.setAgentBuilderFileMapper(AgentBuilderFileMapper);
        listener.setDrTaskHandler(drTaskHandler);
        listener.setRedisClient(redisClient);
        return listener;
    }
}
