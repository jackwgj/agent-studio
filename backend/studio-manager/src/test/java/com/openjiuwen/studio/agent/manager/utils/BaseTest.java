/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.utils;


import feign.Client;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * 升级巫山3.17.1需对restClient进行mock，所有@SpringBootTest测试类继承此类
 *
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:environment.properties")
public abstract class BaseTest {

    @MockitoBean
    protected Client client;
}