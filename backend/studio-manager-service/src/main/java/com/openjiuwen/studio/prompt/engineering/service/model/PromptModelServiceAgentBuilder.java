/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.service.model;

import com.alibaba.fastjson.JSONObject;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.prompt.engineering.dto.InvokeResp;
import com.openjiuwen.studio.prompt.engineering.dto.PromptBody;
import com.openjiuwen.studio.prompt.engineering.utils.PromptUtil;
import com.openjiuwen.studio.prompt.engineering.vo.PromptResultVo;
import com.openjiuwen.studio.prompt.engineering.vo.PromptVo;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;


/**
 * 对接盘古大模型调优接口
 *
 */
@Component
public class PromptModelServiceAgentBuilder extends PromptModelService<PromptResultVo> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PromptModelServiceAgentBuilder.class);

    @Autowired
    @Qualifier("customRestTemplate")
    private  RestTemplate restTemplate;

    @Value("${model.third-type}")
    private String thirdModelType;

    @Override
    protected ResponseEntity<PromptResultVo> call(String xAuthToken, String url, PromptBody promptBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Auth-Token", xAuthToken);
        promptBody.setThirdModelType(thirdModelType);
        HttpEntity<PromptVo> request = new HttpEntity<>(PromptVo.convertToPromptVo(promptBody), headers);

        long startTime = System.currentTimeMillis();
        ResponseEntity<PromptResultVo> response = restTemplate.postForEntity(url, request, PromptResultVo.class);
        LOGGER.info("ModelService.queryModel get request from model {} response = {}", promptBody.getModelType(),
            JSONObject.toJSONString(response));
        Objects.requireNonNull(response.getBody())
            .setTime(PromptUtil.formatDouble((System.currentTimeMillis() - startTime) / 1000.0));
        return response;
    }

    @Override
    protected InvokeResp getRespFromResultBody(PromptResultVo body, String modelUrl) {
        InvokeResp invokeResp = new InvokeResp();

        if (body == null || CollectionUtils.isEmpty(body.getChoices())
            || StringUtils.isEmpty(body.getChoices().get(0).getMessage().getContent())) {
            LOGGER.error("call model {} failed, get no result.", modelUrl);
            throw new AgentStudioException(StudioError.GET_NO_RESULT_FROM_LLM, modelUrl);
        }

        LOGGER.info("call llm success, {}", JSONObject.toJSONString(body));
        invokeResp.setResult(body.getChoices().get(0).getMessage().getContent());
        invokeResp.setToken(ObjectUtils.isEmpty(body.getUsage()) ? 0 : body.getUsage().getTotalTokens());
        invokeResp.setTime(body.getTime());
        return invokeResp;
    }
}
