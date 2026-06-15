/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.service.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.runtime.dto.ClearLongTermMemoriesResponseBody;
import com.openjiuwen.studio.agent.runtime.dto.DeleteLongTermMemoriesRequestBody;
import com.openjiuwen.studio.agent.runtime.dto.DeleteLongTermMemoriesResponseBody;
import com.openjiuwen.studio.agent.runtime.dto.ListLongTermMemoriesQo;
import com.openjiuwen.studio.agent.runtime.dto.ListLongTermMemoriesResponseBody;
import com.openjiuwen.studio.agent.runtime.dto.Memory;
import com.openjiuwen.studio.agent.runtime.dto.MemoryUpdate;
import com.openjiuwen.studio.agent.runtime.dto.UpdateLongTermMemoriesRequestBody;
import com.openjiuwen.studio.agent.runtime.dto.UpdateLongTermMemoriesResponseBody;
import com.openjiuwen.studio.agent.runtime.rce.client.JiuWenClient;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

class UserProfileMemoryManagementServiceImplTest {

    private final JiuWenClient jiuWenClient = mock(JiuWenClient.class);

    private final UserProfileMemoryManagementServiceImpl service =
        new UserProfileMemoryManagementServiceImpl(jiuWenClient);

    @Test
    void test_clearLongTermMemories_should_return_not_null() throws Exception {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class)) {
            mockedStatic.when(RequestContextUtils::getRequestUserId).thenReturn("UserId");

            ClearLongTermMemoriesResponseBody result = service.clearLongTermMemories("projectId", "memoryRepoId");

            assertNotNull(result);
        }
    }

    @Test
    void test_deleteLongTermMemories_should_return_not_null() throws Exception {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class)) {
            mockedStatic.when(RequestContextUtils::getRequestUserId).thenReturn("UserId");

            DeleteLongTermMemoriesRequestBody body = new DeleteLongTermMemoriesRequestBody();
            body.setMemoryIds(List.of("test_memory_id"));

            DeleteLongTermMemoriesResponseBody result = service
                .deleteLongTermMemories("projectId", "memoryRepoId", body);

            assertNotNull(result);
        }
    }

    @Test
    void test_listLongTermMemories_should_return_not_null() throws Exception {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class)) {
            mockedStatic.when(RequestContextUtils::getRequestUserId).thenReturn("UserId");

            Map<String, Object> responseMap = Map.of(
                "total", 1,
                "memories", List.of(Map.of(
                    "memory_id", "mem-1",
                    "content", "test content",
                    "type", "semantic_memory",
                    "last_update_time", 1718112000L
                ))
            );
            org.mockito.Mockito.when(jiuWenClient.listUserMemories(anyString(), anyString(), any(), any(), any()))
                .thenReturn(new ResponseEntity<>(responseMap, HttpStatusCode.valueOf(200)));

            ListLongTermMemoriesQo qo = new ListLongTermMemoriesQo();
            qo.setMemoryRepoId("repo-1");
            qo.setMemoryType("user_profile");
            qo.setOffset(0);
            qo.setLimit(10);

            ListLongTermMemoriesResponseBody result = service.listLongTermMemories("projectId", qo);

            assertNotNull(result);
            assertEquals(1, result.getTotal());
            assertEquals(1, result.getMemories().size());
            Memory m = result.getMemories().get(0);
            assertEquals("mem-1", m.getMemoryId());
            assertEquals("test content", m.getContent());
            assertEquals("semantic_memory", m.getType());
            assertEquals(1718112000L, m.getLastUpdateTime().longValue());
        }
    }

    @Test
    void test_updateLongTermMemories_should_return_not_null() throws Exception {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class)) {
            mockedStatic.when(RequestContextUtils::getRequestUserId).thenReturn("UserId");

            UpdateLongTermMemoriesRequestBody body = new UpdateLongTermMemoriesRequestBody();
            MemoryUpdate memoryUpdate = new MemoryUpdate();
            memoryUpdate.setMemoryId("test_memory_id");
            memoryUpdate.setContent("new content");
            body.setMemories(List.of(memoryUpdate));

            UpdateLongTermMemoriesResponseBody result = service
                .updateLongTermMemories("projectId", "memoryRepoId", body);

            assertNotNull(result);
        }
    }
}
