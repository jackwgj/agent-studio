/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.service.v2.fileparser;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.manager.utils.JsonUtils;
import com.openjiuwen.studio.prompt.engineering.entity.v2.PromptVar;
import com.openjiuwen.studio.prompt.engineering.enums.v2.PtTypeEnum;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@MockitoSettings(strictness = Strictness.LENIENT)
public class JsonFileParserTest {

    // 待测试的目标类（注入@Value配置）
    @InjectMocks
    private JsonFileParser jsonFileParser;

    // Mock依赖（若有需要，此处可Mock父类方法/其他依赖）
    @Mock
    private MultipartFile mockMultipartFile;

    // 测试用常量
    private static final String PROJECT_ID = "proj_123456";

    private static final String WORKSPACE_ID = "ws_789012";

    private static final String TASK_ID = "task_345678";

    private static final int REST_NUM = 50;

    // 初始化测试数据
    private List<List<PromptVar>> validPromptVarsList;

    private String validJsonContent;

    private Map<String, PromptVar> mockVarsConfig;

    @BeforeEach
    void setUp() {
        // 1. 构造合法的二维PromptVar列表
        PromptVar var1 = new PromptVar();
        var1.setName("validName1");
        var1.setContent("validContent1");
        var1.setType(PtTypeEnum.TEXT);
        PromptVar var2 = new PromptVar();
        var2.setName("validName2");
        var2.setContent("validContent2");
        var2.setType(PtTypeEnum.TEXT);
        List<PromptVar> varList1 = Collections.singletonList(var1);
        List<PromptVar> varList2 = Collections.singletonList(var2);
        validPromptVarsList = new ArrayList<>();
        validPromptVarsList.add(varList1);
        validPromptVarsList.add(varList2);

        // 2. 序列化为JSON字符串
        validJsonContent = JsonUtils.toJson(validPromptVarsList);

        // 3. 构造varsConfig（仅包含validName1，用于测试清理无效变量）
        mockVarsConfig = new HashMap<>();
        mockVarsConfig.put("validName1", var1);
        ReflectionTestUtils.setField(jsonFileParser, "varsNum", 0);
        ReflectionTestUtils.setField(jsonFileParser, "varsNameLength", 0);
        ReflectionTestUtils.setField(jsonFileParser, "varsContentLength", 0);
    }

    @Test
    void type_ReturnsJson() {
        Assertions.assertEquals("json", jsonFileParser.type());
    }


    @Test
    void parseData_EmptyJsonContent_ThrowsAgentStudioException() {
        // 1. 构造空内容的文件
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.json", "application/json",
            "   ".getBytes(StandardCharsets.UTF_8) // 空白字符串
        );

        // 2. 执行并断言异常
        AgentStudioException exception = Assertions.assertThrows(AgentStudioException.class,
            () -> jsonFileParser.parseData(emptyFile, mockVarsConfig, PROJECT_ID, WORKSPACE_ID, TASK_ID, REST_NUM));
        Assertions.assertEquals(StudioError.PROMPT_EVAL_JSON_FILE_ERROR, exception.getErrorCode());
    }

    @Test
    void parseData_InvalidJsonStructure_ThrowsAgentStudioException() {
        // 1. 构造一维数组的JSON文件（结构错误）
        String invalidJson = "[{\"name\":\"var1\",\"content\":\"content1\"}]";
        MockMultipartFile invalidFile = new MockMultipartFile("file", "invalid.json", "application/json",
            invalidJson.getBytes(StandardCharsets.UTF_8));

        // 2. 执行并断言异常
        AgentStudioException exception = Assertions.assertThrows(AgentStudioException.class,
            () -> jsonFileParser.parseData(invalidFile, mockVarsConfig, PROJECT_ID, WORKSPACE_ID, TASK_ID, REST_NUM));
        Assertions.assertEquals(StudioError.PROMPT_EVAL_JSON_FILE_ERROR, exception.getErrorCode());
    }

    @Test
    void parseData_EmptyListAfterDeserialize_ThrowsAgentStudioException() {
        // 1. 构造空二维数组的JSON文件
        String emptyListJson = "[]";
        MockMultipartFile emptyListFile = new MockMultipartFile("file", "empty_list.json", "application/json",
            emptyListJson.getBytes(StandardCharsets.UTF_8));

        // 2. 执行并断言异常
        AgentStudioException exception = Assertions.assertThrows(AgentStudioException.class,
            () -> jsonFileParser.parseData(emptyListFile, mockVarsConfig, PROJECT_ID, WORKSPACE_ID, TASK_ID, REST_NUM));
        Assertions.assertEquals(StudioError.PROMPT_EVAL_JSON_FILE_ERROR, exception.getErrorCode());
    }

    @Test
    void parseData_FileReadIOException_ThrowsAgentStudioException() throws Exception {
        // 1. Mock文件读取抛出IO异常
        Mockito.when(mockMultipartFile.getBytes()).thenThrow(new java.io.IOException("文件读取失败"));

        // 2. 执行并断言异常
        AgentStudioException exception = Assertions.assertThrows(AgentStudioException.class,
            () -> jsonFileParser.parseData(mockMultipartFile, mockVarsConfig, PROJECT_ID, WORKSPACE_ID, TASK_ID,
                REST_NUM));
        Assertions.assertEquals(StudioError.PROMPT_EVAL_JSON_FILE_ERROR, exception.getErrorCode());
    }

    @Test
    void parseData_ValidPromptVarsFailed_ThrowsAgentStudioException() throws Exception {
        // 1. 构造合法文件，但Mock校验方法抛出异常
        MockMultipartFile validFile = new MockMultipartFile("file", "test.json", "application/json",
            validJsonContent.getBytes(StandardCharsets.UTF_8));
        JsonFileParser spyParser = Mockito.spy(jsonFileParser);
        Mockito.doThrow(new AgentStudioException(StudioError.PROMPT_EVAL_DATA_FILE_ERROR))
            .when(spyParser)
            .validPromptVarsList(Mockito.anyList(), Mockito.anyMap(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.anyInt());

        // 2. 执行并断言异常
        AgentStudioException exception = Assertions.assertThrows(AgentStudioException.class,
            () -> spyParser.parseData(validFile, mockVarsConfig, PROJECT_ID, WORKSPACE_ID, TASK_ID, REST_NUM));
        Assertions.assertEquals(StudioError.PROMPT_EVAL_DATA_FILE_ERROR, exception.getErrorCode());
    }

    @Test
    void parseData_NullJsonContent_ThrowsAgentStudioException() throws Exception {
        // 1. 构造null内容的文件（getBytes返回null）
        Mockito.when(mockMultipartFile.getBytes()).thenReturn(null);

        // 2. 执行并断言异常
        AgentStudioException exception = Assertions.assertThrows(AgentStudioException.class,
            () -> jsonFileParser.parseData(mockMultipartFile, mockVarsConfig, PROJECT_ID, WORKSPACE_ID, TASK_ID,
                REST_NUM));
        Assertions.assertEquals(StudioError.PROMPT_EVAL_JSON_FILE_ERROR, exception.getErrorCode());
    }
}
