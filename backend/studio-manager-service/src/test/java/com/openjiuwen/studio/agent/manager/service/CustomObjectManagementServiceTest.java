/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.manager.dto.*;
import com.openjiuwen.studio.agent.manager.entity.CustomObjectEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
class CustomObjectManagementServiceTest {

    @Mock
    private com.openjiuwen.studio.agent.manager.mapper.CustomObjectMapper customObjectMapper;

    @InjectMocks
    private CustomObjectManagementService customObjectManagementService;

    private AutoCloseable mockitoCloseable;
    private static final String TEST_PROJECT_ID = "test-project-id";
    private static final String TEST_WORKSPACE_ID = "test-workspace-id";
    private static final String TEST_OBJECT_ID = "test-object-id";

    @BeforeEach
    void setUp() {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(customObjectManagementService, "envType", "test");
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void testCreateCustomObject_Success() {
        try (MockedStatic<com.openjiuwen.studio.agent.common.utils.RequestContextUtils> mockedStatic =
                     mockStatic(com.openjiuwen.studio.agent.common.utils.RequestContextUtils.class, RETURNS_DEEP_STUBS)) {
            // Given
            mockedStatic.when(com.openjiuwen.studio.agent.common.utils.RequestContextUtils::getRequestUserId)
                    .thenReturn("test-user-id");

            ObjectInfoReq body = new ObjectInfoReq();
            body.setName("TestObject");
            body.setDescription("Test Description");
            body.setSchemas("{}");

            when(customObjectMapper.countByNameAndProjectIdAndWorkspaceId(anyString(), anyString(), anyString()))
                    .thenReturn(0);
            when(customObjectMapper.insert(any(CustomObjectEntity.class))).thenReturn(1);

            // When
            ObjectBriefRsp result = customObjectManagementService.createCustomObject(TEST_PROJECT_ID, TEST_WORKSPACE_ID, body);

            // Then
            assertNotNull(result);
            assertNotNull(result.getId());
            verify(customObjectMapper, times(1)).insert(any(CustomObjectEntity.class));
        }
    }

    @Test
    void testCreateCustomObject_DuplicateName() {
        try (MockedStatic<com.openjiuwen.studio.agent.common.utils.RequestContextUtils> mockedStatic =
                     mockStatic(com.openjiuwen.studio.agent.common.utils.RequestContextUtils.class, RETURNS_DEEP_STUBS)) {
            // Given
            mockedStatic.when(com.openjiuwen.studio.agent.common.utils.RequestContextUtils::getRequestUserId)
                    .thenReturn("test-user-id");

            ObjectInfoReq body = new ObjectInfoReq();
            body.setName("ExistingObject");
            body.setDescription("Test Description");

            when(customObjectMapper.countByNameAndProjectIdAndWorkspaceId(anyString(), anyString(), anyString()))
                    .thenReturn(1);

            // When & Then
            assertThrows(AgentStudioException.class, () ->
                    customObjectManagementService.createCustomObject(TEST_PROJECT_ID, TEST_WORKSPACE_ID, body));
        }
    }

    @Test
    void testDeleteCustomObject_Success() {
        // Given
        when(customObjectMapper.deleteByIdAndProjectIdAndWorkspaceId(anyString(), anyString(), anyString()))
                .thenReturn(1);

        // When
        ObjectBriefRsp result = customObjectManagementService.deleteCustomObject(
                TEST_PROJECT_ID, TEST_OBJECT_ID, TEST_WORKSPACE_ID);

        // Then
        assertNotNull(result);
        assertEquals(TEST_OBJECT_ID, result.getId());
        verify(customObjectMapper, times(1))
                .deleteByIdAndProjectIdAndWorkspaceId(TEST_OBJECT_ID, TEST_PROJECT_ID, TEST_WORKSPACE_ID);
    }

    @Test
    void testListCustomObject_WithNameFilter() {
        // Given
        ListCustomObjectQo qo = new ListCustomObjectQo();
        qo.setWorkspaceId(TEST_WORKSPACE_ID);
        qo.setName("Test");
        qo.setOffset(0);
        qo.setLimit(10);

        List<CustomObjectEntity> entities = Arrays.asList(
                createTestCustomObjectEntity("id1"),
                createTestCustomObjectEntity("id2")
        );

        when(customObjectMapper.findByProjectIdAndWorkspaceIdAndNameContaining(
                eq(TEST_PROJECT_ID), eq(TEST_WORKSPACE_ID), eq("Test"), eq(10), eq(0)))
                .thenReturn(entities);
        when(customObjectMapper.countByProjectIdAndWorkspaceIdAndNameContaining(
                eq(TEST_PROJECT_ID), eq(TEST_WORKSPACE_ID), eq("Test")))
                .thenReturn(2L);

        // When
        ObjectListRsp result = customObjectManagementService.listCustomObject(TEST_PROJECT_ID, qo);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getCount());
        assertEquals(2, result.getObjects().size());
        verify(customObjectMapper, times(1)).findByProjectIdAndWorkspaceIdAndNameContaining(
                TEST_PROJECT_ID, TEST_WORKSPACE_ID, "Test", 10, 0);
    }

    @Test
    void testListCustomObject_WithoutNameFilter() {
        // Given
        ListCustomObjectQo qo = new ListCustomObjectQo();
        qo.setWorkspaceId(TEST_WORKSPACE_ID);
        qo.setOffset(0);
        qo.setLimit(10);

        List<CustomObjectEntity> entities = Arrays.asList(createTestCustomObjectEntity("id1"));

        when(customObjectMapper.findByProjectIdAndWorkspaceId(
                eq(TEST_PROJECT_ID), eq(TEST_WORKSPACE_ID), eq(10), eq(0)))
                .thenReturn(entities);
        when(customObjectMapper.countByProjectIdAndWorkspaceIdAndNameContaining(
                eq(TEST_PROJECT_ID), eq(TEST_WORKSPACE_ID), eq(null)))
                .thenReturn(1L);

        // When
        ObjectListRsp result = customObjectManagementService.listCustomObject(TEST_PROJECT_ID, qo);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getCount());
        assertEquals(1, result.getObjects().size());
        verify(customObjectMapper, times(1)).findByProjectIdAndWorkspaceId(
                TEST_PROJECT_ID, TEST_WORKSPACE_ID, 10, 0);
    }

    @Test
    void testModifyCustomObject_Success() {
        // Given
        ObjectInfoReq body = new ObjectInfoReq();
        body.setName("UpdatedName");
        body.setDescription("Updated Description");

        CustomObjectEntity existingEntity = createTestCustomObjectEntity(TEST_OBJECT_ID);
        when(customObjectMapper.findByIdAndProjectIdAndWorkspaceId(TEST_OBJECT_ID, TEST_PROJECT_ID, TEST_WORKSPACE_ID))
                .thenReturn(existingEntity);
        when(customObjectMapper.countByNameAndProjectIdAndWorkspaceId(anyString(), anyString(), anyString()))
                .thenReturn(0);
        when(customObjectMapper.updateById(any(CustomObjectEntity.class))).thenReturn(1);

        // When
        ObjectBriefRsp result = customObjectManagementService.modifyCustomObject(
                TEST_PROJECT_ID, TEST_OBJECT_ID, TEST_WORKSPACE_ID, body);

        // Then
        assertNotNull(result);
        assertEquals(TEST_OBJECT_ID, result.getId());
        verify(customObjectMapper, times(1)).updateById(any(CustomObjectEntity.class));
    }

    @Test
    void testModifyCustomObject_NotFound() {
        // Given
        ObjectInfoReq body = new ObjectInfoReq();
        body.setName("UpdatedName");

        when(customObjectMapper.findByIdAndProjectIdAndWorkspaceId(TEST_OBJECT_ID, TEST_PROJECT_ID, TEST_WORKSPACE_ID))
                .thenReturn(null);

        // When & Then
        assertThrows(AgentStudioException.class, () ->
                customObjectManagementService.modifyCustomObject(
                        TEST_PROJECT_ID, TEST_OBJECT_ID, TEST_WORKSPACE_ID, body));
    }

    @Test
    void testModifyCustomObject_DuplicateName() {
        // Given
        ObjectInfoReq body = new ObjectInfoReq();
        body.setName("ExistingObject");

        CustomObjectEntity existingEntity = createTestCustomObjectEntity(TEST_OBJECT_ID);
        existingEntity.setName("OldName");
        when(customObjectMapper.findByIdAndProjectIdAndWorkspaceId(TEST_OBJECT_ID, TEST_PROJECT_ID, TEST_WORKSPACE_ID))
                .thenReturn(existingEntity);
        when(customObjectMapper.countByNameAndProjectIdAndWorkspaceId(eq("ExistingObject"), eq(TEST_PROJECT_ID), eq(TEST_WORKSPACE_ID)))
                .thenReturn(1);

        // When & Then
        assertThrows(AgentStudioException.class, () ->
                customObjectManagementService.modifyCustomObject(
                        TEST_PROJECT_ID, TEST_OBJECT_ID, TEST_WORKSPACE_ID, body));
    }

    @Test
    void testRetrieveCustomObject_Success() {
        // Given
        CustomObjectEntity entity = createTestCustomObjectEntity(TEST_OBJECT_ID);
        when(customObjectMapper.findByIdAndProjectIdAndWorkspaceId(TEST_OBJECT_ID, TEST_PROJECT_ID, TEST_WORKSPACE_ID))
                .thenReturn(entity);

        // When
        ObjectRsp result = customObjectManagementService.retrieveCustomObject(
                TEST_PROJECT_ID, TEST_OBJECT_ID, TEST_WORKSPACE_ID);

        // Then
        assertNotNull(result);
        assertEquals(TEST_OBJECT_ID, result.getId());
        assertEquals("TestObject", result.getName());
        verify(customObjectMapper, times(1))
                .findByIdAndProjectIdAndWorkspaceId(TEST_OBJECT_ID, TEST_PROJECT_ID, TEST_WORKSPACE_ID);
    }

    @Test
    void testRetrieveCustomObject_NotFound() {
        // Given
        when(customObjectMapper.findByIdAndProjectIdAndWorkspaceId(TEST_OBJECT_ID, TEST_PROJECT_ID, TEST_WORKSPACE_ID))
                .thenReturn(null);

        // When & Then
        assertThrows(AgentStudioException.class, () ->
                customObjectManagementService.retrieveCustomObject(
                        TEST_PROJECT_ID, TEST_OBJECT_ID, TEST_WORKSPACE_ID));
    }

    private CustomObjectEntity createTestCustomObjectEntity(String id) {
        return CustomObjectEntity.builder()
                .id(id)
                .projectId(TEST_PROJECT_ID)
                .workspaceId(TEST_WORKSPACE_ID)
                .name("TestObject")
                .description("Test Description")
                .objectSchema("{}")
                .creatorId("creator-123")
                .createdOn(new Date())
                .updatedOn(new Date())
                .build();
    }
}