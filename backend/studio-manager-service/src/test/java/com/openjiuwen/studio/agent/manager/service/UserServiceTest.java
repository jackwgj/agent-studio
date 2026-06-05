/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.manager.entity.User;
import com.openjiuwen.studio.agent.manager.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    // 测试数据
    private static final String USERNAME = "testUser";

    private static final String REAL_NAME = "Test User";

    private static final String EMAIL = "test@example.com";

    private static final String DOMAIN_ID = "domain123";

    private static final String PROJECT_ID = "project456";

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void createUserOnFirstLogin_whenUserExists_returnsExistingUser() {
        // 1. 准备模拟数据 - 已存在用户
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername(USERNAME);

        // 2. 模拟Repository行为
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(existingUser));

        // 3. 调用测试方法
        User result = userService.createUserOnFirstLogin(USERNAME, REAL_NAME, EMAIL, DOMAIN_ID, PROJECT_ID);

        // 4. 验证行为 & 断言
        verify(userRepository, times(1)).findByUsername(USERNAME);
        verify(userRepository, never()).save(any());
        assertSame(existingUser, result, "应返回已存在的用户");
    }

    @Test
    void createUserOnFirstLogin_whenUserNotExists_savesCorrectUser() {
        // 1. 模拟Repository行为
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        // 2. 捕获保存的用户对象
        User savedUser = new User();
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            savedUser.setId(1L); // 模拟保存后生成ID
            savedUser.setUsername(user.getUsername());
            savedUser.setRealName(user.getRealName());
            savedUser.setEmail(user.getEmail());
            savedUser.setDomainId(user.getDomainId());
            savedUser.setProjectId(user.getProjectId());
            savedUser.setSource(user.getSource());
            savedUser.setCreatedTime(user.getCreatedTime());
            savedUser.setUpdatedTime(user.getUpdatedTime());
            return savedUser;
        });

        // 3. 调用测试方法
        User result = userService.createUserOnFirstLogin(USERNAME, REAL_NAME, EMAIL, DOMAIN_ID, PROJECT_ID);

        // 4. 验证保存的用户属性
        assertEquals(USERNAME, savedUser.getUsername(), "保存的用户名不匹配");
        assertEquals(REAL_NAME, savedUser.getRealName(), "保存的真实姓名不匹配");
        assertEquals(EMAIL, savedUser.getEmail(), "保存的邮箱不匹配");
        assertEquals(DOMAIN_ID, savedUser.getDomainId(), "保存的域ID不匹配");
        assertEquals(PROJECT_ID, savedUser.getProjectId(), "保存的项目ID不匹配");
        assertEquals(User.UserSource.SAML, savedUser.getSource(), "保存的用户来源应为SAML");
        assertNotNull(savedUser.getCreatedTime(), "保存的创建时间不应为null");
        assertNotNull(savedUser.getUpdatedTime(), "保存的更新时间不应为null");
        assertSame(savedUser, result, "返回的应是保存后的用户");
    }
}
