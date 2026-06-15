/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.service.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.type.TypeReference;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.prompt.engineering.dto.BaseInfo;
import com.openjiuwen.studio.prompt.engineering.dto.ListEvalDataSetQo;
import com.openjiuwen.studio.prompt.engineering.dto.PromptBaseResp;
import com.openjiuwen.studio.prompt.engineering.dto.PromptEvalDataItem;
import com.openjiuwen.studio.prompt.engineering.entity.v2.PromptTaskEntity;
import com.openjiuwen.studio.prompt.engineering.entity.v2.PromptVar;
import com.openjiuwen.studio.prompt.engineering.mapper.v2.PromptTaskMapper;
import com.openjiuwen.studio.prompt.engineering.service.PromptObsService;
import com.openjiuwen.studio.prompt.engineering.service.v2.fileparser.DataSetFileParser;
import com.openjiuwen.studio.prompt.engineering.service.v2.fileparser.ExcelFileParser;
import com.openjiuwen.studio.prompt.engineering.service.v2.fileparser.JsonFileParser;
import com.openjiuwen.studio.prompt.engineering.service.v2.fileparser.ZipFileParser;
import com.openjiuwen.studio.agent.manager.utils.JsonUtils;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@MockitoSettings(strictness = Strictness.LENIENT)
class PromptEngineerDataSetServiceTest {

    @InjectMocks
    private PromptEngineerDataSetService promptEngineerDataSetService;

    @Mock
    private PromptTaskMapper promptTaskMapper;

    @Mock
    private PromptObsService obsService;

    @Mock
    private ZipFileParser zipFileParser;

    @Mock
    private JsonFileParser jsonFileParser;

    @Mock
    private ExcelFileParser excelFileParser;

    private static final String JSON_CONTENT = "[" + "  {" + "    \"id\": \"b577936f-b099-4e24-a251-4cb5eafa6d4d\","
        + "    \"content\": \"[{\\\"name\\\":\\\"rt\\\", \\\"content\\\":\\\"good\\\", \\\"type\\\":\\\"text\\\"}, {\\\"name\\\":\\\"drt\\\", \\\"content\\\":\\\"excellent\\\", \\\"type\\\":\\\"multi\\\"}]\""
        + "  }" + "]";

    private static final String ADD_JSON_CONTENT
        = "[{\"name\":\"qqq\",\"type\":\"text\",\"content\":\"asd\"},{\"name\":\"AGENT_BUILDER_PROMPT_OUTPUT\",\"type\":\"text\",\"content\":\"asd\"}]";

    private static final String PROJECT_ID = "project_id";

    private static final String WORKSPACE_ID = "workspace_id";

    private static final String TASK_ID = "task_id";

    private static final String ITEM_ID = "item_id";

    private AutoCloseable mockitoCloseable;

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @BeforeEach
    void setUp() {
        when(zipFileParser.type()).thenReturn("zip");
        when(jsonFileParser.type()).thenReturn("json");
        when(excelFileParser.type()).thenReturn("xls");

        Map<String, DataSetFileParser> map = new HashMap<>();
        map.put("zip", zipFileParser);
        map.put("json", jsonFileParser);
        map.put("xls", excelFileParser);

        MockitoAnnotations.openMocks(this);
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(promptEngineerDataSetService, "EXPIRE_TIME", 0L);
        ReflectionTestUtils.setField(promptEngineerDataSetService, "VARS_NUM", 20);
        ReflectionTestUtils.setField(promptEngineerDataSetService, "VARS_NAME_LENGTH", 50);
        ReflectionTestUtils.setField(promptEngineerDataSetService, "VARS_CONTENT_LENGTH", 1000);
        ReflectionTestUtils.setField(promptEngineerDataSetService, "PICTURE_SIZE", 10);
        ReflectionTestUtils.setField(promptEngineerDataSetService, "DATA_MAX_SIZE", 500);
        ReflectionTestUtils.setField(promptEngineerDataSetService, "strategyMap", map);
    }

    @Test
    void test_importEvalDataSetItems_success() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry = new ZipEntry("data.json");
            zos.putNextEntry(entry);
            zos.write("[]".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }
        byte[] zipBytes = baos.toByteArray();
        MockMultipartFile file = new MockMultipartFile("file", "template.zip", "application/zip", zipBytes);

        when(promptTaskMapper.selectByPrimaryKey(anyString(), anyString(), anyString())).thenReturn(
            new PromptTaskEntity());
        when(obsService.uploadObsFile(anyString(), anyString(), eq(-1))).thenReturn("file");

        BaseInfo result = promptEngineerDataSetService.importEvalDataSetItems(WORKSPACE_ID, PROJECT_ID, TASK_ID, file);

        assertNotNull(result);
        assertEquals("success", result.getMessage());
    }

    @Test
    void test_uploadPicture_success() {
        MockMultipartFile file = new MockMultipartFile("test.png", "test.png", "image/png",
            "test image content".getBytes(StandardCharsets.UTF_8));

        when(promptTaskMapper.selectByPrimaryKey(anyString(), anyString(), anyString())).thenReturn(
            new PromptTaskEntity());

        PromptBaseResp result = promptEngineerDataSetService.uploadPicture(WORKSPACE_ID, PROJECT_ID, TASK_ID, file);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
    }

    @Test
    void test_downloadEvalDataSetTemplate_success() throws Exception {
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        ServletOutputStream mockOutputStream = mock(ServletOutputStream.class);
        when(mockResponse.getOutputStream()).thenReturn(mockOutputStream);

        when(promptTaskMapper.selectByPrimaryKey(anyString(), anyString(), anyString())).thenReturn(
            new PromptTaskEntity());

        PromptEngineerDataSetService spyService = spy(promptEngineerDataSetService);
        byte[] mockZipBytes = "mock zip content".getBytes(StandardCharsets.UTF_8);
        doReturn(mockZipBytes).when(spyService).buildZipContent(anyString());

        Field responseField = PromptEngineerDataSetService.class.getDeclaredField("response");
        responseField.setAccessible(true);
        responseField.set(spyService, mockResponse);

        spyService.downloadEvalDataSetTemplate(PROJECT_ID, WORKSPACE_ID, "text");

        verify(mockResponse).setContentType("application/zip");
        verify(mockResponse).setHeader(eq("Content-Disposition"), contains("template.zip"));
        verify(mockOutputStream).flush();
    }

    @Test
    void test_addSingleEvalDataSetItem_success() {
        PromptEvalDataItem body = new PromptEvalDataItem();
        body.setContent(
            "[{\"name\":\"rt\",\"content\":\"good\",\"type\":\"text\"},{\"name\":\"drt\",\"content\":\"excellent\",\"type\":\"multi\"}]");

        when(promptTaskMapper.selectByPrimaryKey(anyString(), anyString(), anyString())).thenReturn(
            new PromptTaskEntity());
        when(obsService.uploadObsFile(anyString(), anyString(), eq(-1))).thenReturn("file");

        BaseInfo result = promptEngineerDataSetService.addSingleEvalDataSetItem(PROJECT_ID, TASK_ID, WORKSPACE_ID,
            body);

        assertNotNull(result);
        assertEquals("success", result.getMessage());
    }

    @Test
    void test_validBody_error() {
        PromptEvalDataItem body = new PromptEvalDataItem();
        body.setContent("[{\"content\":\"good\",\"type\":\"text\"}]");

        when(promptTaskMapper.selectByPrimaryKey(anyString(), anyString(), anyString())).thenReturn(
            new PromptTaskEntity());
        when(obsService.uploadObsFile(anyString(), anyString(), eq(-1))).thenReturn("file");

        assertThrows(AgentStudioException.class, () -> {
            promptEngineerDataSetService.addSingleEvalDataSetItem(PROJECT_ID, TASK_ID, WORKSPACE_ID, body);
        });
    }

    @Test
    void test_deleteEvalDataSetItem_success() {
        when(promptTaskMapper.selectByPrimaryKey(anyString(), anyString(), anyString())).thenReturn(
            new PromptTaskEntity());
        when(obsService.uploadObsFile(anyString(), anyString(), eq(-1))).thenReturn("file");
        when(obsService.downloadObsFile(anyString())).thenReturn(JSON_CONTENT);
        when(obsService.isExistObsFile(anyString())).thenReturn(true);

        BaseInfo result = promptEngineerDataSetService.deleteEvalDataSetItem(PROJECT_ID, TASK_ID, ITEM_ID,
            WORKSPACE_ID);

        assertNotNull(result);
        assertEquals(ITEM_ID, result.getId()); // 验证 id 是否正确
        assertEquals("success", result.getMessage()); // 验证 message 是否为 "success"
    }

    @Test
    void test_listEvalDataSet_success() {
        // 1. 构造请求参数
        ListEvalDataSetQo listEvalDataSetQo = new ListEvalDataSetQo();
        listEvalDataSetQo.setWorkspaceId(WORKSPACE_ID);
        listEvalDataSetQo.setPageNum(1);
        listEvalDataSetQo.setPageSize(8);

        // 2. 模拟依赖方法返回值
        when(promptTaskMapper.selectByPrimaryKey(anyString(), anyString(), anyString())).thenReturn(
            new PromptTaskEntity());
        when(obsService.isExistObsFile(anyString())).thenReturn(true);
        when(obsService.downloadObsFile(anyString())).thenReturn(JSON_CONTENT);

        // 3. 调用被测方法
        PromptBaseResp result = promptEngineerDataSetService.listEvalDataSet(PROJECT_ID, TASK_ID, listEvalDataSetQo);

        // 4. 验证返回结果
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
    }

    @Test
    void test_updateEvalDataSetItem_success() {
        PromptEvalDataItem body = new PromptEvalDataItem();
        body.setContent(
            "[{\"name\":\"rt\",\"content\":\"good\",\"type\":\"text\"},{\"name\":\"drt\",\"content\":\"excellent\",\"type\":\"multi\"}]");

        when(promptTaskMapper.selectByPrimaryKey(anyString(), anyString(), anyString())).thenReturn(
            new PromptTaskEntity());
        when(obsService.uploadObsFile(anyString(), anyString(), eq(-1))).thenReturn("file");
        when(obsService.downloadObsFile(anyString())).thenReturn(JSON_CONTENT);
        when(obsService.isExistObsFile(anyString())).thenReturn(true);

        PromptEvalDataItem result = promptEngineerDataSetService.updateEvalDataSetItem(PROJECT_ID, TASK_ID, ITEM_ID,
            WORKSPACE_ID, body);

        assertNotNull(result);
        assertEquals(ITEM_ID, result.getId());
        assertEquals(body.getContent(), result.getContent());
    }

    @Test
    public void test_getAllEvalDataSet_file_does_not_exist() {
        // Given
        String taskId = "task1";
        String projectId = "proj1";
        String workspaceId = "ws1";
        String obsFilePath = String.format("EvalData/%s/%s/%s.json", projectId, workspaceId, taskId);

        when(obsService.isExistObsFile(eq(obsFilePath))).thenReturn(false);

        // When & Then
        assertThrows(AgentStudioException.class, () -> {
            promptEngineerDataSetService.getAllEvalDataSet(taskId, projectId, workspaceId);
        });
    }

    @Test
    void test_getAllEvalDataSet_should_return_not_null() {
        // Given
        PromptTaskEntity promptTaskEntity = PromptTaskEntity.builder().build();
        when(promptTaskMapper.selectByPrimaryKey(anyString(), anyString(), anyString())).thenReturn(promptTaskEntity);
        when(obsService.downloadObsFile(anyString())).thenReturn(JSON_CONTENT);
        when(obsService.isExistObsFile(anyString())).thenReturn(true);

        // When
        List<List<PromptVar>> result = promptEngineerDataSetService.getAllEvalDataSet("not_empty", "not_empty",
            "not_empty");

        // Then
        assertNotNull(result);
    }

    @Test
    void test_getAllEvalDataSet_should_not_throw_exception() {
        assertThrows(AgentStudioException.class, () -> {
            try (MockedStatic<JsonUtils> mockedStaticJsonUtils = mockStatic(JsonUtils.class, RETURNS_DEEP_STUBS)) {
                // Given
                mockedStaticJsonUtils.when(() -> JsonUtils.json2Obj(anyString(), any(TypeReference.class)))
                    .thenReturn(null);

                when(promptTaskMapper.selectByPrimaryKey(anyString(), anyString(), anyString())).thenReturn(null);

                when(obsService.downloadObsFile(anyString())).thenReturn(null);
                when(obsService.isExistObsFile(anyString())).thenReturn(null);

                // When
                List<List<PromptVar>> result = promptEngineerDataSetService.getAllEvalDataSet(null, null, null);
            }
        });
    }

    @Test
    void test_copyObsFile_success() throws Exception {
        // Arrange
        String projectId = "proj1";
        String workspaceId = "ws1";
        String oldTaskId = "oldTask1";
        String newTaskId = "newTask1";
        PromptTaskEntity promptTaskEntity = new PromptTaskEntity();

        String arrayContent
            = "[{\"content\":\"[{\\\"name\\\":\\\"aaa\\\",\\\"type\\\":\\\"text\\\",\\\"content\\\":\\\"aaa\\\"},{\\\"name\\\":\\\"AGENT_BUILDER_PROMPT_OUTPUT\\\",\\\"type\\\":\\\"text\\\",\\\"content\\\":\\\"aaa\\\"}]\",\"id\":\"31378848-9ae4-4cf4-b9f2-34ad09d2d9eb\"}]";
        List<PromptEvalDataItem> mockPromptEvalDataItemList = mock(List.class);

        List<String> picturePaths = Arrays.asList("path1", "path2");

        String fileName = "image.jpg";
        String picture = "imageContent";
        when(obsService.downloadObsFile(eq("EvalData/proj1/ws1/oldTask1/oldTask1.json"))).thenReturn(arrayContent);
        when(obsService.isExistObsFile(anyString())).thenReturn(true);
        when(promptTaskMapper.selectByPrimaryKey(anyString(), anyString(), anyString())).thenReturn(promptTaskEntity);

        PromptEvalDataItem evalDataItem = mock(PromptEvalDataItem.class);
        when(evalDataItem.getContent()).thenReturn("content");
        List<PromptEvalDataItem> evalDataItems = Arrays.asList(evalDataItem);

        Matcher matcher = mock(Matcher.class);
        when(matcher.find()).thenReturn(true);
        when(matcher.group(eq(1))).thenReturn(fileName);
        when(matcher.group(eq(0))).thenReturn("path1");

        try (MockedStatic<JsonUtils> jsonUtilsMock = mockStatic(JsonUtils.class)) {
            jsonUtilsMock.when(() -> JsonUtils.json2Obj(anyString(), any(TypeReference.class)))
                .thenReturn(new ArrayList<>());
            jsonUtilsMock.when(
                    () -> JsonUtils.json2Obj(arrayContent, new TypeReference<List<PromptEvalDataItem>>() { }))
                .thenReturn(mockPromptEvalDataItemList);
        }

        try (MockedStatic<JSON> jsonMock = mockStatic(JSON.class)) {
            jsonMock.when(() -> JSON.toJSONString(any())).thenReturn("jsonString");
        }

        // Act
        promptEngineerDataSetService.copyObsFile(projectId, workspaceId, oldTaskId, newTaskId);

        // Assert
        verify(obsService, times(1)).uploadObsFile(anyString(), anyString(), eq(-1));
    }

    @Test
    void test_copyObsFile_data_not_found() throws Exception {
        // Arrange
        String projectId = "proj1";
        String workspaceId = "ws1";
        String oldTaskId = "oldTask1";
        String newTaskId = "newTask1";
        PromptTaskEntity promptTaskEntity = new PromptTaskEntity();
        when(promptTaskMapper.selectByPrimaryKey(anyString(), anyString(), anyString())).thenReturn(promptTaskEntity);
        // Act & Assert
        assertThrows(AgentStudioException.class, () -> {
            promptEngineerDataSetService.copyObsFile(projectId, workspaceId, oldTaskId, newTaskId);
        });
    }

    @Test
    void test_copyObsFile_throws_exception_when_eval_data_not_found() throws Exception {
        String projectId = "proj1";
        String workspaceId = "ws1";
        String oldTaskId = "oldTask1";
        String newTaskId = "newTask1";
        PromptTaskEntity promptTaskEntity = new PromptTaskEntity();
        when(promptTaskMapper.selectByPrimaryKey(anyString(), anyString(), anyString())).thenReturn(promptTaskEntity);
        Exception exception = assertThrows(AgentStudioException.class, () -> {
            promptEngineerDataSetService.copyObsFile(projectId, workspaceId, oldTaskId, newTaskId);
        });

    }

    @Test
    void test_copyObsFile_non_multi_type_prompt_var() throws Exception {
        // Arrange
        String projectId = "proj1";
        String workspaceId = "ws1";
        String oldTaskId = "oldTask1";
        String newTaskId = "newTask1";

        List<PromptEvalDataItem> promptEvalDataItemList = new ArrayList<>();
        PromptEvalDataItem item = new PromptEvalDataItem();
        promptEvalDataItemList.add(item);

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            promptEngineerDataSetService.copyObsFile(projectId, workspaceId, oldTaskId, newTaskId);
        });
    }

    @Test
    void test_copyObsFile_when_eval_data_not_found_should_throw_agent_studio_exception() throws Exception {
        // Arrange
        String projectId = "proj1";
        String workspaceId = "ws1";
        String oldTaskId = "oldTask1";
        String newTaskId = "newTask1";
        PromptTaskEntity promptTaskEntity = new PromptTaskEntity();
        when(promptTaskMapper.selectByPrimaryKey(anyString(), anyString(), anyString())).thenReturn(promptTaskEntity);

        // Act & Assert
        assertThrows(AgentStudioException.class, () -> {
            promptEngineerDataSetService.copyObsFile(projectId, workspaceId, oldTaskId, newTaskId);
        });
    }

    @Test
    public void testAddBatchEvalDataSetItem_NonEmptyBody() {
        // Given
        String projectId = "proj1";
        String taskId = "task1";
        String workspaceId = "ws1";
        List<PromptEvalDataItem> body = new ArrayList<>();
        PromptEvalDataItem item = new PromptEvalDataItem();
        item.setContent(ADD_JSON_CONTENT);
        body.add(item);

        PromptTaskEntity promptTaskEntity = new PromptTaskEntity();

        try (MockedStatic<CollectionUtils> mockedCollectionUtils = mockStatic(CollectionUtils.class)) {
            mockedCollectionUtils.when(() -> CollectionUtils.isEmpty((Collection<?>) any())).thenReturn(false);
            when(promptTaskMapper.selectByPrimaryKey(anyString(), anyString(), anyString())).thenReturn(
                promptTaskEntity);

            // When
            BaseInfo result = promptEngineerDataSetService.addBatchEvalDataSetItem(projectId, taskId, workspaceId,
                body);

            // Then
            assertNotNull(result);
            assertEquals("success", result.getMessage());
        }
    }

    @Test
    public void testAddBatchEvalDataSetItem_FileExists() {
        // Given
        String projectId = "proj1";
        String taskId = "task1";
        String workspaceId = "ws1";
        List<PromptEvalDataItem> body = new ArrayList<>();
        PromptEvalDataItem item = new PromptEvalDataItem();
        item.setContent(ADD_JSON_CONTENT);
        body.add(item);
        PromptTaskEntity promptTaskEntity = new PromptTaskEntity();

        try (MockedStatic<CollectionUtils> mockedCollectionUtils = mockStatic(CollectionUtils.class);
            MockedStatic<JSON> mockedJson = mockStatic(JSON.class)) {
            mockedCollectionUtils.when(() -> CollectionUtils.isEmpty((Collection<?>) any())).thenReturn(false);
            mockedJson.when(() -> JSON.toJSONString(any())).thenReturn("jsonString");
            when(obsService.isExistObsFile(anyString())).thenReturn(true);
            when(obsService.downloadObsFile(anyString())).thenReturn(JSON_CONTENT);
            when(promptTaskMapper.selectByPrimaryKey(anyString(), anyString(), anyString())).thenReturn(
                promptTaskEntity);
            // When
            BaseInfo result = promptEngineerDataSetService.addBatchEvalDataSetItem(projectId, taskId, workspaceId,
                body);

            // Then
            assertNotNull(result);
            assertEquals("success", result.getMessage());
        }
    }
}
