package com.openjiuwen.studio.conversation.application;

import com.openjiuwen.studio.agent.common.dto.simple.SimpleUser;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;
import com.openjiuwen.studio.conversation.application.dto.ConversationInputUploadVo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
}
