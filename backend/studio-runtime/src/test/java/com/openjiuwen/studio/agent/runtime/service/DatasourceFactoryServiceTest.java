/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.crypt.Ciphers;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.common.utils.CryptoUtils;
import com.openjiuwen.studio.agent.runtime.constant.ConstantTest;
import com.openjiuwen.studio.agent.runtime.datasource.DatasourceFactory;
import com.openjiuwen.studio.agent.runtime.dto.DatasourceExecuteReq;
import com.openjiuwen.studio.agent.runtime.utils.BaseTest;
import com.openjiuwen.studio.agent.runtime.utils.TestUtil;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * 数据源内部执行接口
 *
 */
@Slf4j
public class DatasourceFactoryServiceTest extends BaseTest {
    @MockitoBean
    RedisClient redisClient;

    @MockitoBean
    ObsService obsService;

    @Autowired
    DatasourceFactory datasourceFactory;

    @Autowired
    private DatasourceRuntimeService datasourceRuntimeService;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(datasourceFactory, "obsService", obsService);
        ReflectionTestUtils.setField(datasourceFactory, "redisClient", redisClient);
        ReflectionTestUtils.setField(datasourceRuntimeService, "datasourceFactory", datasourceFactory);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_execute_sql_by_cache() {
        mockSccCryptoUtil();
        when(redisClient.exists(ConstantTest.TEST_REDIS_DATASOURCE_CACHE_KEY)).thenReturn(true);
        when(redisClient.get(ConstantTest.TEST_REDIS_DATASOURCE_CACHE_KEY)).thenReturn(
            TestUtil.getStringFromFile("classpath:service/datasource_config_cache.json"));
        DatasourceExecuteReq req = new DatasourceExecuteReq().setQuery("select 1").setShouldByCache(true);
        Assertions.assertThrows(AgentStudioException.class,
            () -> datasourceRuntimeService.executeDatasource(ConstantTest.TEST_DATASOURCE_ID, req));
    }

    @Test
    void test_execute_sql_not_by_cache() {
        mockSccCryptoUtil();
        when(redisClient.exists(ConstantTest.TEST_REDIS_DATASOURCE_CACHE_KEY)).thenReturn(false);
        when(obsService.getObject("datasource/ir/test_datasource_id.json")).thenReturn(
            TestUtil.getStringFromFile("classpath:service/datasource_config_cache.json"));
        DatasourceExecuteReq req = new DatasourceExecuteReq().setQuery("select 1").setShouldByCache(false);
        Assertions.assertThrows(AgentStudioException.class,
            () -> datasourceRuntimeService.executeDatasource(ConstantTest.TEST_DATASOURCE_ID, req));
    }

    private void mockSccCryptoUtil() {
        new CryptoUtils(new Ciphers(null, null));
        try (MockedStatic<CryptoUtils> mockedSccCryptoUtils = Mockito.mockStatic(CryptoUtils.class)) {
            mockedSccCryptoUtils.when(() -> CryptoUtils.decrypt(anyString())).thenReturn("decrypt");
            mockedSccCryptoUtils.when(() -> CryptoUtils.encrypt(anyString())).thenReturn("encrypt");
        }
    }
}
