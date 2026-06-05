/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.runtime.constant.ConstantTest;
import com.openjiuwen.studio.agent.runtime.datasource.DatasourceFactory;
import com.openjiuwen.studio.agent.runtime.dto.DatasourceExecuteReq;
import com.openjiuwen.studio.agent.runtime.dto.DatasourceExecuteResp;
import com.openjiuwen.studio.agent.runtime.utils.BaseTest;
import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * 数据源运行面校验接口
 *
 */
@Slf4j
public class DatasourceRuntimeServiceTest extends BaseTest {
    @MockitoBean
    DatasourceFactory datasourceFactory;

    @Autowired
    private DatasourceRuntimeService datasourceRuntimeService;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(datasourceRuntimeService, "datasourceFactory", datasourceFactory);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_execute_sql_by_cache() {
        when(datasourceFactory.getConnectionPool(anyString(), anyBoolean())).thenReturn(mockConnectionPool());
        DatasourceExecuteReq req = new DatasourceExecuteReq().setQuery("select 1").setShouldByCache(true);
        DatasourceExecuteResp datasourceExecuteResp =
            datasourceRuntimeService.executeDatasource(ConstantTest.TEST_DATASOURCE_ID, req);
        Assertions.assertEquals(1, datasourceExecuteResp.getRowNum());
    }

    private HikariDataSource mockConnectionPool() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:h2:mem:test;mode=MYSQL");
        dataSource.setUsername("fake");
        dataSource.setPassword("fake");
        dataSource.setDriverClassName("org.h2.Driver");
        return dataSource;
    }
}
