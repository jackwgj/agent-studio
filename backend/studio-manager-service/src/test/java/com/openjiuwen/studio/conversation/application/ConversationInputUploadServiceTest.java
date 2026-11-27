package com.openjiuwen.studio.conversation.application;

import com.openjiuwen.studio.agent.common.dto.simple.SimpleUser;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;
import com.openjiuwen.studio.conversation.application.dto.ConversationInputUploadVo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class ConversationInputUploadServiceTest {
    private MgObsService obsService;
    private ConversationWorkspaceAccessGuard accessGuard;
    private ConversationInputUploadService service;

    @BeforeEach
    void setUp() {
        obsService = mock(MgObsService.class);
        accessGuard = mock(ConversationWorkspaceAccessGuard.class);
        service = new ConversationInputUploadService(obsService, accessGuard);
        SimpleUser user = new SimpleUser();
        user.setUserId("u1");
        user.setProjectId("p1");
        RequestContextUtils.setContext(user);
    }

    @AfterEach
    void tearDown() {
        RequestContextUtils.remove();
    }

    @Test
    void upload_生成身份绑定对象键并返回稳定元数据() {
        byte[] bytes = "report".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("file", "report.txt", "text/plain", bytes);

        ConversationInputUploadVo result = service.upload("p1", "w1", file);

        verify(accessGuard).requireAccess("p1", "w1");
        ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
        verify(obsService).uploadObsFile(key.capture(), any(InputStream.class), eq(-1));
        String prefix = "conversation-inputs/" + ConversationInputUploadService.sha256("p1") + "/"
            + ConversationInputUploadService.sha256("w1") + "/"
            + ConversationInputUploadService.sha256("u1") + "/";
        assertTrue(key.getValue().startsWith(prefix));
        assertTrue(key.getValue().endsWith("/report.txt"));
        assertEquals(key.getValue(), result.getObjectKey());
        assertEquals("report.txt", result.getFileName());
        assertEquals(bytes.length, result.getSize());
        assertEquals(ConversationInputUploadService.sha256(bytes), result.getChecksum());
    }

    @Test
    void upload_拒绝携带路径的客户端文件名() {
        MockMultipartFile file = new MockMultipartFile(
            "file", "../report.txt", "text/plain", "report".getBytes(java.nio.charset.StandardCharsets.UTF_8));

        assertThrows(AgentStudioException.class, () -> service.upload("p1", "w1", file));
    }

    @Test
    void upload_优先使用独立传输的UTF8原始文件名() {
        byte[] bytes = "中文内容".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile(
            "file", "Skill鍔熻兘鎵嬪伐楠岃瘉鎻愮ず璇�.md", "text/markdown", bytes);
        String encodedFileName = Base64.getUrlEncoder().withoutPadding()
            .encodeToString("Skill功能手工验证提示词.md".getBytes(StandardCharsets.UTF_8));

        ConversationInputUploadVo result = service.upload("p1", "w1", file, encodedFileName);

        ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
        verify(obsService).uploadObsFile(key.capture(), any(InputStream.class), eq(-1));
        assertTrue(key.getValue().endsWith("/Skill功能手工验证提示词.md"));
        assertEquals("Skill功能手工验证提示词.md", result.getFileName());
    }

    @Test
    void upload_将压缩包作为单个原始文件上传() throws IOException {
        ByteArrayOutputStream content = new ByteArrayOutputStream();
        try (ZipOutputStream archive = new ZipOutputStream(content)) {
            archive.putNextEntry(new ZipEntry("report.txt"));
            archive.write("report".getBytes(StandardCharsets.UTF_8));
            archive.closeEntry();
        }
        byte[] bytes = content.toByteArray();
        String fileName = "项目资料.ZIP";
        MockMultipartFile file = new MockMultipartFile("file", fileName, "application/octet-stream", bytes);

        ConversationInputUploadVo result = service.upload("p1", "w1", file);

        ArgumentCaptor<InputStream> uploaded = ArgumentCaptor.forClass(InputStream.class);
        verify(obsService).uploadObsFile(eq(result.getObjectKey()), uploaded.capture(), eq(-1));
        assertTrue(result.getObjectKey().endsWith("/" + fileName));
        assertEquals(fileName, result.getFileName());
        assertEquals(ConversationInputUploadService.sha256(bytes), result.getChecksum());
        assertArrayEquals(bytes, uploaded.getValue().readAllBytes());
    }

    @Test
    void upload_按实际格式识别PNG和JPEG而非浏览器声明() throws IOException {
        for (String format : new String[]{"png", "jpeg"}) {
            byte[] bytes = imageBytes(format);
            String fileName = "测试图片." + format.toUpperCase(java.util.Locale.ROOT);
            MockMultipartFile file = new MockMultipartFile("file", fileName, "application/octet-stream", bytes);

            ConversationInputUploadVo result = service.upload("p1", "w1", file);

            assertEquals("image/" + format, result.getMediaType());
            assertEquals(fileName, result.getFileName());
            ArgumentCaptor<InputStream> uploaded = ArgumentCaptor.forClass(InputStream.class);
            verify(obsService).uploadObsFile(eq(result.getObjectKey()), uploaded.capture(), eq(-1));
            assertArrayEquals(bytes, uploaded.getValue().readAllBytes());
        }
    }

    @Test
    void upload_拒绝伪造或后缀不匹配的图片() throws IOException {
        for (byte[] bytes : new byte[][]{"not PNG".getBytes(StandardCharsets.UTF_8), imageBytes("jpeg")}) {
            MockMultipartFile file = new MockMultipartFile("file", "fake.png", "image/png", bytes);
            AgentStudioException error = assertThrows(AgentStudioException.class, () -> service.upload("p1", "w1", file));
            assertEquals(StudioError.ILLEGAL_FILE_TYPE, error.getErrorCode());
        }
        verifyNoInteractions(obsService);
    }

    @Test
    void upload_拒绝超过独立字节或像素限制的图片() throws IOException {
        MockMultipartFile large = mock(MockMultipartFile.class);
        org.mockito.Mockito.when(large.getOriginalFilename()).thenReturn("large.jpg");
        org.mockito.Mockito.when(large.getSize()).thenReturn(10L * 1024 * 1024 + 1);
        byte[] hugeDimensions = imageBytes("png");
        ByteBuffer.wrap(hugeDimensions).putInt(16, 5000).putInt(20, 5000);
        CRC32 crc = new CRC32();
        crc.update(hugeDimensions, 12, 17);
        ByteBuffer.wrap(hugeDimensions).putInt(29, (int) crc.getValue());
        MockMultipartFile pixelLimit = new MockMultipartFile("file", "large.png", "image/png", hugeDimensions);

        for (MockMultipartFile file : new MockMultipartFile[]{large, pixelLimit}) {
            AgentStudioException error = assertThrows(AgentStudioException.class, () -> service.upload("p1", "w1", file));
            assertEquals(StudioError.PICTURE_FILE_SIZE_EXCEED_LIMIT, error.getErrorCode());
        }
        verifyNoInteractions(obsService);
    }

    private static byte[] imageBytes(String format) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        assertTrue(ImageIO.write(new BufferedImage(12, 8, BufferedImage.TYPE_INT_RGB), format, buffer));
        return buffer.toByteArray();
    }
}
