/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.service.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.dto.simple.SimpleUser;
import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.runtime.model.memory.LongTermMemoryRefreshEvent;
import com.openjiuwen.studio.agent.runtime.utils.JsonUtils;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpHeaders;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserMemoryCacheMgmtServiceTest {
    @Mock
    private RedisClient redisClient;

    @InjectMocks
    private UserMemoryCacheMgmtService userMemoryCacheMgmtService;

    private static final String TEST_USER_ID = "test-user-123";

    private static final String TEST_REPO_ID = "test-repo-456";

    private static final String TEST_CONVERSATION_ID = "test-conversation-789";

    private static final String TEST_PROJECT_ID = "test-project-789";

    private static MockedStatic<RequestContextUtils> requestContextUtilsMockedStatic;

    @BeforeEach
    void setUp() {
        // 只创建 mock 实例，不自动关闭
        requestContextUtilsMockedStatic = Mockito.mockStatic(RequestContextUtils.class);
        requestContextUtilsMockedStatic.when(RequestContextUtils::getRequestIamCtx).thenReturn(new SimpleUser());
        requestContextUtilsMockedStatic.when(() -> RequestContextUtils.setContext(any()))
            .thenAnswer(invocation -> null);
        requestContextUtilsMockedStatic.when(RequestContextUtils::getRequestProjectId).thenReturn(TEST_PROJECT_ID);
    }

    @AfterEach
    void tearDown() {
        // 在所有测试完成后关闭 mock
        if (requestContextUtilsMockedStatic != null) {
            requestContextUtilsMockedStatic.close();
        }
    }

    @Test
    @Order(1)
    @DisplayName("getStoredLongTermMemoryRefreshEvent should return null when userId is blank")
    void getStoredLongTermMemoryRefreshEvent_WhenUserIdIsBlank_ShouldReturnNull() {
        // Given & When
        LongTermMemoryRefreshEvent result = userMemoryCacheMgmtService.getStoredLongTermMemoryRefreshEvent("",
            TEST_REPO_ID, TEST_CONVERSATION_ID);

        // Then
        assertNull(result);
    }

    @Test
    @Order(2)
    @DisplayName("getStoredLongTermMemoryRefreshEvent should return null when applicationId is blank")
    void getStoredLongTermMemoryRefreshEvent_WhenApplicationIdIsBlank_ShouldReturnNull() {
        // Given & When
        LongTermMemoryRefreshEvent result = userMemoryCacheMgmtService.getStoredLongTermMemoryRefreshEvent(TEST_USER_ID,
            "", TEST_CONVERSATION_ID);

        // Then
        assertNull(result);
    }

    @Test
    @Order(3)
    @DisplayName("getStoredLongTermMemoryRefreshEvent should return null when conversationId is blank")
    void getStoredLongTermMemoryRefreshEvent_WhenConversationIdIsBlank_ShouldReturnNull() {
        // Given & When
        LongTermMemoryRefreshEvent result = userMemoryCacheMgmtService.getStoredLongTermMemoryRefreshEvent(TEST_USER_ID,
            TEST_REPO_ID, "");

        // Then
        assertNull(result);
    }

    @Test
    @Order(4)
    @DisplayName("getStoredLongTermMemoryRefreshEvent should return null when stored event is blank")
    void getStoredUserProfileRefreshEvent_WhenStoredEventIsBlank_ShouldReturnNull() {
        // Given
        when(redisClient.get(anyString())).thenReturn("");

        // When
        LongTermMemoryRefreshEvent result = userMemoryCacheMgmtService.getStoredLongTermMemoryRefreshEvent(TEST_USER_ID,
            TEST_REPO_ID, TEST_CONVERSATION_ID);

        // Then
        assertNull(result);
    }

    @Test
    @Order(5)
    @DisplayName("getStoredLongTermMemoryRefreshEvent should return LongTermMemoryRefreshEvent when stored")
    void getStoredUserProfileRefreshEvent_WhenEventIsStored_ShouldReturnEvent() {
        // Given
        LongTermMemoryRefreshEvent expectedEvent = new LongTermMemoryRefreshEvent();
        LongTermMemoryRefreshEvent.EventData eventData = new LongTermMemoryRefreshEvent.EventData();
        eventData.setMemoryRepoId(TEST_REPO_ID);
        eventData.setConversationId(TEST_CONVERSATION_ID);
        eventData.setTime(System.currentTimeMillis());
        expectedEvent.setData(eventData);

        String eventJson = JsonUtils.toJson(expectedEvent);
        when(redisClient.get(anyString())).thenReturn(eventJson);

        // When
        LongTermMemoryRefreshEvent result = userMemoryCacheMgmtService.getStoredLongTermMemoryRefreshEvent(TEST_USER_ID,
            TEST_REPO_ID, TEST_CONVERSATION_ID);

        // Then
        assertNotNull(result);
        assertNotNull(result.getData());
        assertEquals(TEST_REPO_ID, result.getData().getMemoryRepoId());
        assertEquals(TEST_CONVERSATION_ID, result.getData().getConversationId());
    }

    @Test
    @Order(6)
    @DisplayName("deleteStoredLongTermMemoryRefreshEvent should do nothing when userId is blank")
    void deleteStoredUserProfileRefreshEvent_WhenUserIdIsBlank_ShouldDoNothing() {
        // Given & When
        userMemoryCacheMgmtService.deleteStoredLongTermMemoryRefreshEvent("", TEST_REPO_ID, TEST_CONVERSATION_ID);

        // Then
        verify(redisClient, never()).delete(anyString());
    }

    @Test
    @Order(7)
    @DisplayName("deleteStoredLongTermMemoryRefreshEvent should do nothing when memoryRepoId is blank")
    void deleteStoredLongTermMemoryRefreshEvent_WhenMemoryRepoIdIsBlank_ShouldDoNothing() {
        // Given & When
        userMemoryCacheMgmtService.deleteStoredLongTermMemoryRefreshEvent(TEST_USER_ID, "", TEST_CONVERSATION_ID);

        // Then
        verify(redisClient, never()).delete(anyString());
    }

    @Test
    @Order(8)
    @DisplayName("deleteStoredLongTermMemoryRefreshEvent should do nothing when conversationId is blank")
    void deleteStoredLongTermMemoryRefreshEvent_WhenConversationIdIsBlank_ShouldDoNothing() {
        // Given & When
        userMemoryCacheMgmtService.deleteStoredLongTermMemoryRefreshEvent(TEST_USER_ID, TEST_REPO_ID, "");

        // Then
        verify(redisClient, never()).delete(anyString());
    }

    @Test
    @Order(9)
    @DisplayName("deleteStoredLongTermMemoryRefreshEvent should delete event when all parameters are valid")
    void deleteStoredUserProfileRefreshEvent_WhenAllParametersAreValid_ShouldDeleteEvent() {
        // Given & When
        userMemoryCacheMgmtService.deleteStoredLongTermMemoryRefreshEvent(TEST_USER_ID, TEST_REPO_ID,
            TEST_CONVERSATION_ID);

        // Then
        verify(redisClient, times(1)).delete(anyString());
    }

    @Test
    @Order(10)
    @DisplayName("getStoredLongTermMemoryRefreshEvent should handle JSON deserialization failure gracefully")
    void getStoredLongTermMemoryRefreshEvent_WhenJsonDeserializationFails_ShouldReturnNull() {
        // Given
        when(redisClient.get(anyString())).thenReturn("{invalid json}");

        // When
        LongTermMemoryRefreshEvent result = userMemoryCacheMgmtService.getStoredLongTermMemoryRefreshEvent(TEST_USER_ID,
            TEST_REPO_ID, TEST_CONVERSATION_ID);

        // Then
        assertNull(result);
    }
}
