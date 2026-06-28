/* Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved. */
package com.openjiuwen.studio.agent.runtime.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.openjiuwen.studio.agent.runtime.dto.ApiExecDataEventAnswer;
import com.openjiuwen.studio.agent.runtime.dto.KnowledgeBaseFileInfo;

import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * 校验插件检索结果 -> 前端可下载文件信息（KnowledgeBaseFileInfo）的解析逻辑。
 * 这是知识库改造保留、供前端展示下载文件的关键数据通路。
 */
class KnowledgeBaseFileInfoServiceTest {

    private final KnowledgeBaseFileInfoService service = new KnowledgeBaseFileInfoService();

    private JSONObject pluginContent(String fileId, String kbId, String docName) {
        // 构造 {result: {output_list: [{knowledge_base_id, file_id, document_name, ...}]}}
        String json = "{\"result\":{\"output_list\":[{"
            + "\"knowledge_base_id\":\"" + kbId + "\","
            + "\"file_id\":\"" + fileId + "\","
            + "\"document_name\":\"" + docName + "\","
            + "\"knowledge_base_type\":\"INTERNAL\","
            + "\"type\":\"doc\"}]}}";
        return JSON.parseObject(json);
    }

    @Test
    void getKnowledgeBaseFileInfosFromPlugin_mapsFields() {
        ApiExecDataEventAnswer rsp = new ApiExecDataEventAnswer();
        rsp.setContent(pluginContent("f-1", "kb-1", "doc.pdf"));

        List<KnowledgeBaseFileInfo> infos = service.getKnowledgeBaseFileInfosFromPlugin(rsp);

        assertEquals(1, infos.size());
        KnowledgeBaseFileInfo info = infos.get(0);
        assertEquals("kb-1", info.getKnowledgeBaseId());
        assertEquals("f-1", info.getFileId());
        assertEquals("doc.pdf", info.getFileName());
        assertEquals("INTERNAL", info.getKnowledgeBaseType());
        assertEquals("doc", info.getType());
    }

    @Test
    void getKnowledgeBaseFileInfosFromPlugin_filtersEmptyFileId() {
        ApiExecDataEventAnswer rsp = new ApiExecDataEventAnswer();
        // file_id 为空的条目应被过滤
        String json = "{\"result\":{\"output_list\":["
            + "{\"knowledge_base_id\":\"kb-1\",\"file_id\":\"\",\"document_name\":\"a\"},"
            + "{\"knowledge_base_id\":\"kb-1\",\"file_id\":\"f-2\",\"document_name\":\"b\"}]}}";
        rsp.setContent(JSON.parseObject(json));

        List<KnowledgeBaseFileInfo> infos = service.getKnowledgeBaseFileInfosFromPlugin(rsp);
        assertEquals(1, infos.size());
        assertEquals("f-2", infos.get(0).getFileId());
    }

    @Test
    void getKnowledgeBaseFileInfosFromPlugin_nullContentReturnsEmpty() {
        ApiExecDataEventAnswer rsp = new ApiExecDataEventAnswer();
        rsp.setContent(null);
        assertTrue(service.getKnowledgeBaseFileInfosFromPlugin(rsp).isEmpty());
    }

    @Test
    void getKnowledgeBaseFileInfosFromPlugin_nullAnswerReturnsEmpty() {
        assertTrue(service.getKnowledgeBaseFileInfosFromPlugin(null).isEmpty());
    }

    @Test
    void getKnowledgeBaseFileInfosFromPlugin_emptyOutputListReturnsEmpty() {
        ApiExecDataEventAnswer rsp = new ApiExecDataEventAnswer();
        rsp.setContent(JSON.parseObject("{\"result\":{\"output_list\":[]}}"));
        assertTrue(service.getKnowledgeBaseFileInfosFromPlugin(rsp).isEmpty());
    }
}
