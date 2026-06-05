/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.runtime.dto.AgentVariableRsp;
import com.openjiuwen.studio.agent.runtime.dto.GetAgentVariableQo;
import com.openjiuwen.studio.agent.runtime.dto.UpdateVariableReq;
import com.openjiuwen.studio.agent.runtime.dto.VariableInfo;

import lombok.extern.slf4j.Slf4j;

import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Agent 全局变量管理
 *
 */
@Slf4j
@Service
public class AgentVariableService implements IAgentVariableService {

    private static final String GLOBAL_VALS_KEY = "global.vals.%s.%s";

    @Autowired
    private RedisClient redisClient;

    /**
     * 获取会话变量信息。
     *
     * @param projectId 项目ID
     * @param agentId 智能体或工作流ID
     * @param conversationId 会话ID
     * @param getAgentVariableQo 获取会话变量的查询对象
     * @return 包含变量信息的响应对象
     * @throws AgentStudioException 如果解析变量失败时抛出
     */
    @Override
    public AgentVariableRsp getAgentVariable(String projectId, String agentId, String conversationId,
        GetAgentVariableQo getAgentVariableQo) {

        // 从redis中获取全局会话变量
        String key = String.format(GLOBAL_VALS_KEY, agentId, conversationId);
        String globalVals = redisClient.get(key, StringCodec.INSTANCE);

        // 解析变量
        List<VariableInfo> variableInfos = jsonToVariableInfoList(globalVals);

        // 创建并返回包含变量信息的响应对象
        return new AgentVariableRsp().setStatus("success").setData(variableInfos);
    }

    /**
     * 更新代理变量信息。
     *
     * @param projectId 项目ID
     * @param agentId 智能体或工作流ID
     * @param conversationId 会话ID
     * @param varId 变量名称
     * @param body 更新变量请求体
     * @return 更新后的变量信息
     */
    @Override
    public VariableInfo updateAgentVariable(String projectId, String agentId, String conversationId, String varId,
        UpdateVariableReq body) {

        // 从redis中获取全局会话变量
        String key = String.format(GLOBAL_VALS_KEY, agentId, conversationId);
        String globalVals = redisClient.get(key, StringCodec.INSTANCE);

        // 更新body体中的变量名
        body.setName(varId);

        // 根据变量信息更新全局会话变量
        Object updatedGlobalVals = updateJsonByVariableInfo(globalVals, body);
        // 将更新后的全局会话变量存入redis
        redisClient.setObj(key, updatedGlobalVals);

        // 返回更新后的变量信息
        return new VariableInfo().setName(varId).setValue(body.getValue());
    }

    /**
     * JSON 字符串转 VariableInfoList
     *
     * @param jsonStr 输入 JSON 字符串（如 {"aaa": "", "bbb": {"ccc": "你好"}, ...}）
     * @return List<VariableInfo> 转换后的List
     * @throws IllegalArgumentException 当 JSON 解析失败或输入为 null/空时抛出
     */
    public List<VariableInfo> jsonToVariableInfoList(String jsonStr) {
        // 校验输入参数
        if (jsonStr == null || jsonStr.trim().isEmpty()) {
            log.error("jsonStr is null.");
            return new ArrayList<>();
        }

        // 解析 JSON 字符串为 JSONObject
        JSONObject jsonObject;
        try {
            jsonObject = JSON.parseObject(jsonStr);
        } catch (Exception e) {
            log.error("Invalid JSON string format, parsing failed!", e);
            throw new AgentStudioException(StudioError.AGENT_PARSE_VARIABLE_FAILED);
        }

        // 遍历 JSONObject 的所有 key-value，封装为 VariableInfo
        Set<String> keySet = jsonObject.keySet();
        List<VariableInfo> variableInfoList = new ArrayList<>(keySet.size());

        for (String key : keySet) {
            Object value = jsonObject.get(key);
            variableInfoList.add(new VariableInfo().setName(key).setValue(value));
        }

        return variableInfoList;
    }

    /**
     * 根据UpdateVariableReq 更新 JSON 字符串（仅更新第一层 key）
     *
     * @param originalJson 原始 JSON 字符串
     * @param updateVariableReq 要更新的单个 UpdateVariableReq 对象（name 匹配顶层 key）
     * @return 更新后的 JSON 字符串
     */
    public Object updateJsonByVariableInfo(String originalJson, UpdateVariableReq updateVariableReq) {
        // 校验入参
        if (originalJson == null || originalJson.trim().isEmpty()) {
            log.error("The original JSON string cannot be empty.");
            throw new AgentStudioException(StudioError.AGENT_GET_VARIABLE_FAILED);
        }
        if (updateVariableReq == null || updateVariableReq.getName() == null) {
            log.error("UpdateVariableReq cannot be null, and name cannot be null.");
            throw new AgentStudioException(StudioError.AGENT_GET_VARIABLE_FAILED);
        }

        // 解析原始 JSON 为 JSONObject（保留层级结构）
        JSONObject jsonObject = JSON.parseObject(originalJson);
        if (jsonObject == null) {
            throw new AgentStudioException(StudioError.AGENT_PARSE_VARIABLE_FAILED);
        }

        // 仅更新第一层 key（核心：name 匹配顶层 key 则覆盖，不匹配则新增）
        String targetKey = updateVariableReq.getName();
        Object newValue = updateVariableReq.getValue();
        jsonObject.put(targetKey, newValue);

        // 转回 JSON 字符串（可选格式化输出）
        return jsonObject.toJavaObject(Object.class);
    }

}
