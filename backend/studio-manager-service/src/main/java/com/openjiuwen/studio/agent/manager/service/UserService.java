/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service;

import com.openjiuwen.studio.agent.manager.entity.User;
import com.openjiuwen.studio.agent.manager.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 用户服务层
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * 首次登录时自动创建用户
     */
    @Transactional
    public User createUserOnFirstLogin(String username, String realName, String email, String domainId,
        String projectId) {
        Optional<User> existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setRealName(realName);
        newUser.setEmail(email);
        newUser.setDomainId(domainId);
        newUser.setProjectId(projectId);
        newUser.setSource(User.UserSource.SAML); // 使用 SAML 作为第三方来源
        newUser.setCreatedTime(LocalDateTime.now());
        newUser.setUpdatedTime(LocalDateTime.now());

        return userRepository.save(newUser);
    }
}
