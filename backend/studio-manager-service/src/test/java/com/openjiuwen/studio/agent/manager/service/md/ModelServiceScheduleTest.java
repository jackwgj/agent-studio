/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service.md;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.manager.entity.md.ModelServiceBase;
import com.openjiuwen.studio.agent.manager.entity.md.ModelServiceProvider;
import com.openjiuwen.studio.agent.manager.entity.md.ProviderAuthData;
import com.openjiuwen.studio.agent.manager.entity.md.ProviderAuthMetadata;
import com.openjiuwen.studio.agent.common.entity.RouterStrategyEntity;
import com.openjiuwen.studio.agent.manager.mapper.md.ModelServiceMapper;
import com.openjiuwen.studio.agent.manager.mapper.md.ProviderAuthDataMapper;
import com.openjiuwen.studio.agent.manager.mapper.md.ProviderAuthMetadataMapper;
import com.openjiuwen.studio.agent.manager.mapper.md.RouterStrategyMapper;
import com.openjiuwen.studio.agent.manager.mapper.md.UserModelServiceProviderMapper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

@MockitoSettings(strictness = Strictness.LENIENT)
class ModelServiceScheduleTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ModelServiceMapper modelServiceMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ProviderAuthMetadataMapper providerAuthMetadataMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ProviderAuthDataMapper providerAuthDataMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RouterStrategyMapper routerStrategyMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private UserModelServiceProviderMapper userModelServiceProviderMapper;

    @InjectMocks
    private ModelServiceSchedule modelServiceSchedule;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(modelServiceSchedule, "modelSoftDelete", true);
        ReflectionTestUtils.setField(modelServiceSchedule, "cycle", 0L);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_syncModelPhysicsDeleteTask_should_not_throw_exception() throws Exception {
        assertDoesNotThrow(() -> {
            List<ModelServiceBase> modelServiceBases = new ArrayList<>();
            ModelServiceBase modelServiceBase = new ModelServiceBase();
            modelServiceBases.add(modelServiceBase);
            when(modelServiceMapper.queryBackupByUpdateTime(anyLong())).thenReturn(modelServiceBases);

            List<ProviderAuthData> providerAuthDataList = new ArrayList<>();
            ProviderAuthData providerAuthData = new ProviderAuthData();
            providerAuthDataList.add(providerAuthData);
            when(providerAuthDataMapper.queryBackupByUpdateTime(anyLong())).thenReturn(providerAuthDataList);

            List<RouterStrategyEntity> routerStrategyEntities = new ArrayList<>();
            RouterStrategyEntity routerStrategyEntity = new RouterStrategyEntity();
            routerStrategyEntities.add(routerStrategyEntity);
            when(routerStrategyMapper.queryBackupByUpdateTime(anyLong())).thenReturn(routerStrategyEntities);

            List<ModelServiceProvider> modelServiceProviders = new ArrayList<>();
            ModelServiceProvider modelServiceProvider = new ModelServiceProvider();
            modelServiceProviders.add(modelServiceProvider);
            when(userModelServiceProviderMapper.queryBackupByUpdateTime(anyLong())).thenReturn(modelServiceProviders);

            List<ProviderAuthMetadata> providerAuthMetadataList = new ArrayList<>();
            ProviderAuthMetadata providerAuthMetadata = new ProviderAuthMetadata();
            providerAuthMetadataList.add(providerAuthMetadata);
            when(providerAuthMetadataMapper.queryBackupByUpdateTime(anyLong())).thenReturn(providerAuthMetadataList);

            modelServiceSchedule.syncModelPhysicsDeleteTask();
        });
    }

    @Test
    void test_syncModelPhysicsDeleteTask_should_not_throw_exception2() throws Exception {
        assertDoesNotThrow(() -> {
            when(modelServiceMapper.queryBackupByUpdateTime(anyLong())).thenReturn(null);

            when(providerAuthDataMapper.queryBackupByUpdateTime(anyLong())).thenReturn(null);

            when(routerStrategyMapper.queryBackupByUpdateTime(anyLong())).thenReturn(null);

            when(userModelServiceProviderMapper.queryBackupByUpdateTime(anyLong())).thenReturn(null);

            when(providerAuthMetadataMapper.queryBackupByUpdateTime(anyLong())).thenReturn(null);

            modelServiceSchedule.syncModelPhysicsDeleteTask();
        });
    }
}
