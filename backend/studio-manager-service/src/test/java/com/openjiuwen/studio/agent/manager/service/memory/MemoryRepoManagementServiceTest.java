/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service.memory;

import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.dto.CreateMemoryRepoRequestBody;
import com.openjiuwen.studio.agent.manager.dto.CreateMemoryRepoResponseBody;
import com.openjiuwen.studio.agent.manager.dto.DeleteMemoryRepoResponseBody;
import com.openjiuwen.studio.agent.manager.dto.ListMemoryReposResponseBody;
import com.openjiuwen.studio.agent.manager.dto.ListMemoryRepositoriesQo;
import com.openjiuwen.studio.agent.manager.dto.LongTermMemoryStrategy;
import com.openjiuwen.studio.agent.manager.dto.ModifyMemoryRepoRequestBody;
import com.openjiuwen.studio.agent.manager.dto.ModifyMemoryRepoResponseBody;
import com.openjiuwen.studio.agent.manager.dto.ShowMemoryRepoResponseBody;
import com.openjiuwen.studio.agent.manager.entity.MemoryRepoEntity;
import com.openjiuwen.studio.agent.manager.mapper.MemoryRepoMapper;
import com.openjiuwen.studio.agent.manager.rce.client.MemoryServiceClient;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@MockitoSettings(strictness = Strictness.LENIENT)
class MemoryRepoManagementServiceTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MemoryRepoMapper memoryRepoMapper;

    @Mock
    private MemoryServiceClient memoryServiceClient;

    @InjectMocks
    private com.openjiuwen.studio.agent.manager.service.memory.MemoryRepoManagementService memoryRepoManagementService;


    MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils;

    @BeforeEach
    void setUp() throws Exception {
        mockedStaticRequestContextUtils = mockStatic(
            RequestContextUtils.class, RETURNS_DEEP_STUBS);
        // Given
        mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestAuthToken).thenReturn("token");
        mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserDomainId).thenReturn("domain_id");
        mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserName).thenReturn("name");
        mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserId).thenReturn("user_id");
    }

    @AfterEach
    void tearDown() throws Exception {
        mockedStaticRequestContextUtils.close();
    }

    @Test
    void test_createMemoryRepo_should_return_not_null() throws Exception {
        // Given
        when(memoryRepoMapper.insert(any(MemoryRepoEntity.class))).thenReturn(0);
        when(memoryServiceClient.saveOrUpdateMemoryRepositoryConfig(anyString(), anyString(), anyString(), any())).thenReturn(null);

        CreateMemoryRepoRequestBody body = new CreateMemoryRepoRequestBody();
        List<LongTermMemoryStrategy> strategies = new ArrayList<>();
        LongTermMemoryStrategy longTermMemoryStrategy = new LongTermMemoryStrategy();
        longTermMemoryStrategy.setType(LongTermMemoryStrategy.TypeEnum.SEMANTIC_MEMORY);
        longTermMemoryStrategy.setPrompt("test_prompt");
        strategies.add(longTermMemoryStrategy);
        body.setName("test_name");
        body.setDescription("test_description");
        body.setLongTermMemoryStrategies(strategies);


        // When
        CreateMemoryRepoResponseBody result = memoryRepoManagementService.createMemoryRepo("not_empty", "not_empty", body);

        // Then
        assertNotNull(result);
    }

    @Test
    void test_createMemoryRepo_should_not_throw_exception() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            // Given
            when(memoryRepoMapper.insert(any(MemoryRepoEntity.class))).thenReturn(0);

            // When
            CreateMemoryRepoResponseBody result = memoryRepoManagementService.createMemoryRepo(null, null, null);
        });
    }


    @Test
    void test_deleteMemoryRepo_should_return_not_null() throws Exception {
        // Given
        when(memoryRepoMapper.deleteById(anyString())).thenReturn(1);
        MemoryRepoEntity memoryRepoEntity = MemoryRepoEntity.builder().build();
        when(memoryRepoMapper.selectById(anyString())).thenReturn(memoryRepoEntity);


        // When
        DeleteMemoryRepoResponseBody result = memoryRepoManagementService.deleteMemoryRepo("not_empty", "not_empty", "not_empty");

        // Then
        assertNotNull(result);
    }

    @Test
    void test_deleteMemoryRepo_should_return_not_null2() throws Exception {
        // Given
        when(memoryRepoMapper.deleteById(anyString())).thenReturn(0);
        when(memoryRepoMapper.selectById(anyString())).thenReturn(null);

        // When
        DeleteMemoryRepoResponseBody result = memoryRepoManagementService.deleteMemoryRepo(null, null, null);

        // Then
        assertNotNull(result);
    }

    @Test
    void test_listMemoryRepositories_should_return_not_null() throws Exception {
        // Given
        List<MemoryRepoEntity> memoryRepos = new ArrayList<>();
        MemoryRepoEntity memoryRepoEntity = MemoryRepoEntity.builder().build();
        memoryRepos.add(memoryRepoEntity);
        when(memoryRepoMapper.selectByCondition(anyString(), anyString(), anyString(), any(), anyString())).thenReturn(memoryRepos);

        ListMemoryRepositoriesQo query = new ListMemoryRepositoriesQo();

        // When
        ListMemoryReposResponseBody result = memoryRepoManagementService.listMemoryRepositories("not_empty", query);

        // Then
        assertNotNull(result);
    }

    @Test
    void test_listMemoryRepositories_should_not_throw_exception() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            // Given
            when(memoryRepoMapper.selectByCondition(anyString(), anyString(), anyString(), any(), anyString())).thenReturn(null);

            // When
            ListMemoryReposResponseBody result = memoryRepoManagementService.listMemoryRepositories(null, null);
        });
    }


    @Test
    void test_modifyMemoryRepo_should_return_not_null() throws Exception {
        // Given
        when(memoryRepoMapper.updateById(any(MemoryRepoEntity.class))).thenReturn(0);

        ModifyMemoryRepoRequestBody body = new ModifyMemoryRepoRequestBody();
        List<LongTermMemoryStrategy> strategies = new ArrayList<>();
        LongTermMemoryStrategy longTermMemoryStrategy = new LongTermMemoryStrategy();
        longTermMemoryStrategy.setType(LongTermMemoryStrategy.TypeEnum.SEMANTIC_MEMORY);
        longTermMemoryStrategy.setPrompt("test_prompt");
        strategies.add(longTermMemoryStrategy);
        body.setName("test_name");
        body.setDescription("test_description");
        body.setLongTermMemoryStrategies(strategies);

        // When
        ModifyMemoryRepoResponseBody result = memoryRepoManagementService.modifyMemoryRepo("not_empty", "not_empty", "not_empty", body);

        // Then
        assertNotNull(result);
    }

    @Test
    void test_modifyMemoryRepo_should_not_throw_exception() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            // Given
            when(memoryRepoMapper.updateById(any(MemoryRepoEntity.class))).thenReturn(0);

            // When
            ModifyMemoryRepoResponseBody result = memoryRepoManagementService.modifyMemoryRepo(null, null, null, null);
        });
    }


    @Test
    void test_showMemoryRepo_should_return_not_null() throws Exception {
        // Given
        List<LongTermMemoryStrategy> longTermMemoryStrategies = new ArrayList<>();
        LongTermMemoryStrategy longTermMemoryStrategy = new LongTermMemoryStrategy();
        longTermMemoryStrategies.add(longTermMemoryStrategy);
        Date createTime = new Date();
        Date updateTime = mock(Date.class, Answers.RETURNS_DEEP_STUBS);
        MemoryRepoEntity memoryRepoEntity = MemoryRepoEntity.builder().id("not_empty").name("not_empty").description("not_empty").longTermMemoryStrategies(longTermMemoryStrategies).createdUserId("not_empty").createdUserName("not_empty").lastUpdateUserId("not_empty").lastUpdateUserName("not_empty").createTime(createTime).updateTime(updateTime).build();
        when(memoryRepoMapper.selectById(anyString())).thenReturn(memoryRepoEntity);

        // When
        ShowMemoryRepoResponseBody result = memoryRepoManagementService.showMemoryRepo("not_empty", "not_empty", "not_empty");

        // Then
        assertNotNull(result);
    }


    @Test
    void test_showMemoryRepo_should_return_not_null2() throws Exception {
        // Given
        when(memoryRepoMapper.selectById(anyString())).thenReturn(null);

        // When
        ShowMemoryRepoResponseBody result = memoryRepoManagementService.showMemoryRepo(null, null, null);

        // Then
        assertNotNull(result);
    }
}
