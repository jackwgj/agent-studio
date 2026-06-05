/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.service.v2.fileparser;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.manager.utils.JsonUtils;
import com.openjiuwen.studio.prompt.engineering.entity.v2.PromptVar;
import com.openjiuwen.studio.prompt.engineering.enums.v2.PtTypeEnum;
import com.openjiuwen.studio.prompt.engineering.service.PromptObsService;
import com.openjiuwen.studio.prompt.engineering.utils.PromptDataSetUtil;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@MockitoSettings(strictness = Strictness.LENIENT)
public class ZipFileParserTest {

    // 待测试类（注入@Value配置）
    @InjectMocks
    private ZipFileParser zipFileParser;

    // Mock依赖组件
    @Mock
    private PromptObsService obsService;
    @Mock
    private ExcelFileParser excelFileParser;
    @Mock
    private JsonFileParser jsonFileParser;

    // 配置参数验证
    @Value("${prompt.tempurl.time: 2592000}")
    private long testExpireTime;
    @Value("${prompt.evaldata.vars.picturesize: 10}")
    private int testPictureSize;



    // 测试常量
    private static final String PROJECT_ID = "proj_123";
    private static final String WORKSPACE_ID = "ws_456";
    private static final String TASK_ID = "task_789";
    private static final int REST_NUM = 50;

    // 测试数据
    private Map<String, PromptVar> mockVarsConfig;
    private List<List<PromptVar>> mockPromptVarsList;
    private File tempZipFile;

    /**
     * 初始化测试数据
     */
    @BeforeEach
    void setUp() throws IOException {
        ReflectionTestUtils.setField(zipFileParser, "PICTURE_SIZE", 20);
        ReflectionTestUtils.setField(zipFileParser, "EXPIRE_TIME", 2592000);

        // 1. 构造变量配置（包含MULTI类型的图片变量）
        PromptVar imageVar = new PromptVar();
        imageVar.setName("imageVar");
        imageVar.setContent("picture/test.jpg");
        imageVar.setType(PtTypeEnum.MULTI);
        mockVarsConfig = new HashMap<>();
        mockVarsConfig.put("imageVar", imageVar);

        // 2. 构造PromptVar列表
        List<PromptVar> varList = Collections.singletonList(imageVar);
        mockPromptVarsList = Collections.singletonList(varList);

        // 3. 创建临时ZIP文件（包含JSON文件+picture目录+测试图片）
        tempZipFile = File.createTempFile("test", ".zip");
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tempZipFile))) {
            // 添加JSON文件（最外层）
            ZipEntry jsonEntry = new ZipEntry("test.json");
            zos.putNextEntry(jsonEntry);
            zos.write(JsonUtils.toJson(mockPromptVarsList).getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            // 添加图片文件（picture/test.jpg）
            ZipEntry imageEntry = new ZipEntry("picture/test.jpg");
            zos.putNextEntry(imageEntry);
            zos.write(new byte[1024*1024*10]); // 1KB测试图片
            zos.closeEntry();
        }
    }

    /**
     * 清理临时文件
     */
    @BeforeEach
    void tearDown() {
        if (tempZipFile != null && tempZipFile.exists()) {
            tempZipFile.delete();
        }
    }

    // ===================== 基础行为测试 =====================

    /**
     * 测试：type()方法返回"zip"
     */
    @Test
    void type_ReturnsZip() {
        Assertions.assertEquals("zip", zipFileParser.type());
    }


    /**
     * 测试：getContentType方法返回正确的Content-Type
     */
    @Test
    void getContentType_ReturnsCorrectValue() {
        Assertions.assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            zipFileParser.getContentType("xlsx"));
        Assertions.assertEquals("application/json", zipFileParser.getContentType("json"));
        Assertions.assertEquals("application/octet-stream", zipFileParser.getContentType("txt"));
    }

    // ===================== 核心方法测试 =====================

    /**
     * 测试：extractOuterExcelOrJson 提取ZIP最外层的JSON文件
     */
    @Test
    void extractOuterExcelOrJson_ExtractsOuterJsonFile() throws IOException {
        // 执行方法
        MultipartFile result = zipFileParser.extractOuterExcelOrJson(tempZipFile);

        // 断言结果
        Assertions.assertNotNull(result);
        Assertions.assertEquals("test.json", result.getOriginalFilename());
        Assertions.assertEquals("application/json", result.getContentType());
    }

    /**
     * 测试：extractOuterExcelOrJson 过滤嵌套文件（含目录/子路径）
     * @throws IOException
     */
    @Test
    void extractOuterExcelOrJson_IgnoresNestedFiles() throws IOException {
        // 创建包含嵌套JSON的ZIP文件
        File nestedZipFile = File.createTempFile("nested", ".zip");
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(nestedZipFile))) {
            // 添加嵌套在dir/下的JSON文件
            ZipEntry nestedEntry = new ZipEntry("dir/test.json");
            zos.putNextEntry(nestedEntry);
            zos.write("{}".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }

        // 执行方法
        MultipartFile result = zipFileParser.extractOuterExcelOrJson(nestedZipFile);

        // 断言无文件被提取
        Assertions.assertNull(result);

        // 清理
        nestedZipFile.delete();
    }

    /**
     * 测试：acquireNeedPicture 正确提取需要的图片文件名
     */
    @Test
    void acquireNeedPicture_ExtractsImageFilenames() {
        // 执行方法
        Set<String> requiredImages = zipFileParser.acquireNeedPicture(mockPromptVarsList, mockVarsConfig);

        // 断言结果
        Assertions.assertEquals(1, requiredImages.size());
        Assertions.assertTrue(requiredImages.contains("test.jpg"));
    }





    /**
     * 测试：uploadImagesAndGetEvalData 替换图片内容为OBS临时URL
     */
    @Test
    void uploadImagesAndGetEvalData_ReplacesContentWithObsUrl() throws IOException {
        // Mock图片上传映射
        Map<String, String> imageMap = new HashMap<>();
        String uuidName = "12345.jpg";
        imageMap.put("test.jpg", uuidName);

        // Mock OBS临时URL
        String tempUrl = "https://obs.example.com/temp/12345.jpg";
        Mockito.when(obsService.getObsTempUrl(Mockito.anyString(), Mockito.anyLong())).thenReturn(tempUrl);

        // Mock uploadPictureFromzip 返回映射
        ZipFileParser spyParser = Mockito.spy(zipFileParser);
        Mockito.doReturn(imageMap).when(spyParser).uploadPictureFromzip(Mockito.anySet(), Mockito.any(File.class), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());

        // 执行方法
        spyParser.uploadImagesAndGetEvalData(tempZipFile, mockPromptVarsList, mockVarsConfig, PROJECT_ID, WORKSPACE_ID, TASK_ID);

        // 断言内容被替换为临时URL
        PromptVar promptVar = mockPromptVarsList.get(0).get(0);
        Assertions.assertEquals(tempUrl, promptVar.getContent());
    }


    // ===================== 异常场景测试 =====================

    /**
     * 测试：parseData ZIP内无支持的文件类型，抛出异常
     * @throws Exception
     */
    @Test
    void parseData_UnsupportedFileTypeInZip_ThrowsException() throws Exception {
        // 1. 创建包含txt文件的ZIP
        File txtZipFile = File.createTempFile("txt", ".zip");
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(txtZipFile))) {
            ZipEntry txtEntry = new ZipEntry("test.txt");
            zos.putNextEntry(txtEntry);
            zos.write("test".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }

        // 2. Mock静态工具类
        try (MockedStatic<PromptDataSetUtil> mockedUtil = Mockito.mockStatic(PromptDataSetUtil.class)) {
            mockedUtil.when(() -> PromptDataSetUtil.saveToTempFile(Mockito.any(MultipartFile.class))).thenReturn(txtZipFile);

            // 3. 构造上传文件
            MockMultipartFile zipFile = new MockMultipartFile(
                "file", "test.zip", "application/zip", IOUtils.toByteArray(new FileInputStream(txtZipFile))
            );

            // 4. 执行并断言异常
            AgentStudioException exception = Assertions.assertThrows(AgentStudioException.class, () ->
                zipFileParser.parseData(zipFile, mockVarsConfig, PROJECT_ID, WORKSPACE_ID, TASK_ID, REST_NUM)
            );
            Assertions.assertEquals(StudioError.PROMPT_DATA_TYPE_ERROR, exception.getErrorCode());
        }

        // 清理
        txtZipFile.delete();
    }



    @Test
    void uploadPictureFromzip_ObsUpload() throws Exception {
        // Mock OBS上传抛出异常
        Mockito.doThrow(new RuntimeException("OBS上传失败"))
            .when(obsService)
            .uploadObsFile(Mockito.anyString(), Mockito.any(InputStream.class), Mockito.eq(-1));

        // 执行方法并断言异常
        Assertions.assertDoesNotThrow(
            () -> zipFileParser.uploadPictureFromzip(Collections.singleton("test.jpg"), tempZipFile, PROJECT_ID,
                WORKSPACE_ID, TASK_ID));

    }
}
