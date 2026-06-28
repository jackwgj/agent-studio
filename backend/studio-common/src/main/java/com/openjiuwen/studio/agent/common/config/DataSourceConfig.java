/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.config;

import com.openjiuwen.studio.agent.common.utils.CryptoUtils;

import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * 数据源配置，对数据库密码进行解密
 */
@Slf4j
@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties, CryptoUtils cryptoUtils) {
        HikariDataSource dataSource = properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
        String originalPassword = dataSource.getPassword();
        if (originalPassword != null && !originalPassword.isEmpty()) {
            String decryptedPassword = cryptoUtils.decrypt(originalPassword);
            dataSource.setPassword(decryptedPassword);
            log.info("DataSource password decrypted successfully");
        }
        return dataSource;
    }
}