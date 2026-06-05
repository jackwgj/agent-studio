/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.service.memory;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.runtime.dto.ClearLongTermMemoriesResponseBody;
import com.openjiuwen.studio.agent.runtime.dto.DeleteLongTermMemoriesRequestBody;
import com.openjiuwen.studio.agent.runtime.dto.DeleteLongTermMemoriesResponseBody;
import com.openjiuwen.studio.agent.runtime.dto.ListLongTermMemoriesQo;
import com.openjiuwen.studio.agent.runtime.dto.ListLongTermMemoriesResponseBody;
import com.openjiuwen.studio.agent.runtime.dto.MemoryUpdate;
import com.openjiuwen.studio.agent.runtime.dto.UpdateLongTermMemoriesRequestBody;
import com.openjiuwen.studio.agent.runtime.dto.UpdateLongTermMemoriesResponseBody;
import com.openjiuwen.studio.agent.runtime.rce.client.MemoryServiceClient;
import com.openjiuwen.studio.agent.runtime.rce.model.memory.BatchDeleteUserMemoriesResponseBody;
import com.openjiuwen.studio.agent.runtime.rce.model.memory.MemoryServiceOperationResponse;
import com.openjiuwen.studio.agent.runtime.rce.model.memory.UpdateUserProfileValuesResponseBody;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

@MockitoSettings(strictness = Strictness.LENIENT)
class UserProfileMemoryManagementServiceImplTest {
    @Mock
    private MemoryServiceClient memoryServiceClient;

    @InjectMocks
    private UserProfileMemoryManagementServiceImpl userProfileMemoryManagementServiceImpl;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(userProfileMemoryManagementServiceImpl, "memoryServiceClient",
            memoryServiceClient);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_clearLongTermMemories_should_return_not_null() throws Exception {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class)) {
            mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestAuthToken).thenReturn("test-token");
            mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserId).thenReturn("UserId");

            ResponseEntity<MemoryServiceOperationResponse> memoryResponseEntity = new ResponseEntity<>(
                HttpStatusCode.valueOf(200));
            when(memoryServiceClient.clearLongTermMemories(anyString(), any(), any(), any())).thenReturn(
                memoryResponseEntity);

            ClearLongTermMemoriesResponseBody result = userProfileMemoryManagementServiceImpl.clearLongTermMemories(
                "projectId", "memoryRepoId");

            assertNotNull(result);
        }
    }

    @Test
    void test_deleteLongTermMemories_should_return_not_null() throws Exception {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class)) {
            mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestAuthToken).thenReturn("test-token");

            ResponseEntity<BatchDeleteUserMemoriesResponseBody> memoryResponseEntity = new ResponseEntity<>(
                HttpStatusCode.valueOf(200));
            when(memoryServiceClient.deleteLongTermMemories(anyString(), any(), any(), any())).thenReturn(
                memoryResponseEntity);

            DeleteLongTermMemoriesRequestBody body = new DeleteLongTermMemoriesRequestBody();
            body.setMemoryIds(List.of("test_memory_id"));

            DeleteLongTermMemoriesResponseBody result = userProfileMemoryManagementServiceImpl.deleteLongTermMemories(
                "projectId", "memoryRepoId", body);

            assertNotNull(result);
        }
    }

    @Test
    void test_listLongTermMemories_should_return_not_null() throws Exception {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class)) {
            mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestAuthToken).thenReturn("test-token");

            ResponseEntity<ListLongTermMemoriesResponseBody> memoryResponseEntity = new ResponseEntity<>(
                new ListLongTermMemoriesResponseBody(), HttpStatusCode.valueOf(200));

            when(memoryServiceClient.listLongTermMemories(anyString(), any(), any(), any(), any(), any())).thenReturn(
                memoryResponseEntity);

            ListLongTermMemoriesResponseBody result = userProfileMemoryManagementServiceImpl.listLongTermMemories(
                "projectId", new ListLongTermMemoriesQo());

            assertNotNull(result);
        }
    }

    @Test
    void test_updateLongTermMemories_should_return_not_null() throws Exception {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class)) {
            mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestAuthToken).thenReturn("test-token");

            ResponseEntity<UpdateUserProfileValuesResponseBody> memoryResponseEntity = new ResponseEntity<>(
                HttpStatusCode.valueOf(200));
            when(memoryServiceClient.updateLongTermMemories(anyString(), any(), any(), any())).thenReturn(
                memoryResponseEntity);

            UpdateLongTermMemoriesRequestBody body = new UpdateLongTermMemoriesRequestBody();
            MemoryUpdate memoryUpdate = new MemoryUpdate();
            memoryUpdate.setMemoryId("test_memory_id");
            memoryUpdate.setContent("你好");
            body.setMemories(List.of(memoryUpdate));

            UpdateLongTermMemoriesResponseBody result = userProfileMemoryManagementServiceImpl.updateLongTermMemories(
                "projectId", "memoryRepoId", body);

            assertNotNull(result);
        }
    }
}