/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2028. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSON;
import com.openjiuwen.studio.agent.common.entity.Text2AudioReq;
import com.openjiuwen.studio.agent.common.entity.TtsConfig;
import com.openjiuwen.studio.agent.runtime.constant.Constant;
import com.openjiuwen.studio.agent.runtime.model.RequestResult;
import com.openjiuwen.studio.agent.runtime.properties.SisProperties;
import com.openjiuwen.studio.agent.runtime.service.md.RuntimeModelHttpClientService;
import com.openjiuwen.studio.agent.runtime.utils.OkHttpUtils;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@Component
public class TextToSpeechCustomization {

    @Value("${agent.builder.sis.domain:}")
    private String sisClientUrl;

    @Value("${agent.builder.sis.iamEndpoint:}")
    private String iamUrl;

    private final SisProperties sisProperties;

    // 用于匹配各种Markdown特殊字符的正则表达式
    private static final Pattern[] MD_PATTERNS = {
        // 标题标记：# 及其可能的后续空格
        Pattern.compile("#{1,6}\\s*", Pattern.MULTILINE),
        // 强调标记：**、_、__
        Pattern.compile("\\*\\*|__|_"),
        // 链接和图片标记：![alt](url) 或 [text](url)
        Pattern.compile("!*\\[.*?\\]\\(.*?\\)"),
        // 代码块标记：`和```
        Pattern.compile("`{1,3}"),
        // 分隔线：---、***等
        Pattern.compile("^-{3,}|\\*{3,}$", Pattern.MULTILINE),
        // 知识库返回图片标签{KI|******}（需要往前放在“表格标记”之前，防止“表格标记”移除标签内的“|”导致匹配失败）
        Pattern.compile("\\{KI\\|([-A-Za-z0-9_]{1,100})}"),
        // 表格标记：| 和 :
        Pattern.compile("\\||:"),
        // 表格中的分隔线
        Pattern.compile("^\\s*-+\\s*$", Pattern.MULTILINE),
        // 引用标签
        Pattern.compile("(?is)<span\\s+id=\"reference\\d+\"\\s+class=\"agent-base-show-source\\s+retrieval-id-[^\"]*\"[^>]*>.*?</span>")
    };

    @Autowired
    private OkHttpUtils okHttpUtils;

    @Autowired
    private RuntimeModelHttpClientService httpClientService;

    public TextToSpeechCustomization(SisProperties sisProperties) {
        this.sisProperties = sisProperties;
    }

    /**
     * convertTextToSpeech
     *
     * @param req 请求体
     * @return String 语音数据，以Base64编码格式返回
     */
    public JSONObject convertTextToSpeech(Text2AudioReq req) {
        log.info("start to call tts, convert text to speech, text length = {}", req.getText().length());
        String httpResponse = null;
        JSONObject response = null;
        try {
            TtsConfig ttsConfig = new TtsConfig();
            ttsConfig.setProperty(req.getConfig().getProperty()); // 设置发音人
            ttsConfig.setSampleRate("16000");
            Text2AudioReq text2AudioReq = new Text2AudioReq();

            // 处理文本中的特殊字符
            String reqText = req.getText();
            // 依次应用所有正则表达式进行替换
            for (Pattern pattern : MD_PATTERNS) {
                reqText = pattern.matcher(reqText).replaceAll("");
            }

            text2AudioReq.setText(reqText);
            text2AudioReq.setConfig(ttsConfig);

            String url = (sisClientUrl + "/v1/{sis_project_id}/tts")
                    .replace("{sis_project_id}", sisProperties.getProjectId());
            String opToken = "";
            final String jsonRequest = JSON.toJSONString(text2AudioReq);

            Map<String, String> headers = new HashMap<>();
            headers.put(Constant.CONTENT_TYPE, Constant.APPLICATION_JSON);
            headers.put(Constant.Common.X_AUTH_TOKEN, opToken);
            RequestResult requestResult = okHttpUtils.call(url, OkHttpUtils.Method.POST, headers, jsonRequest);
            // 检查请求是否成功
            if (!requestResult.isSuccess()) {
                log.error("invoke tts failed! {}", JSONObject.parseObject(httpResponse));
            }
            httpResponse = requestResult.getResponse();
            response = JSONObject.parseObject(httpResponse);
        } catch (Exception e) {
            log.error("When start to call tts, convert text to speech exception.", e);
        }
        return response.getJSONObject("result");
    }
}
