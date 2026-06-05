/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service.memory;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.manager.dto.UserProfileMemoryConfig;
import com.openjiuwen.studio.agent.manager.dto.UserProfileTagInfo;
import com.openjiuwen.studio.agent.manager.dto.UserProfileTopicInfo;
import com.openjiuwen.studio.agent.manager.rce.client.MemoryServiceClient;
import com.openjiuwen.studio.agent.manager.service.AgentCommonService;

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

@MockitoSettings(strictness = Strictness.LENIENT)
class AgentMemoryConfigServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MemoryServiceClient memoryServiceClient;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AgentCommonService agentCommonService;

    @InjectMocks
    private com.openjiuwen.studio.agent.manager.service.memory.AgentMemoryConfigService agentMemoryConfigService;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(agentMemoryConfigService, "userProfileEnable", true);
        ReflectionTestUtils.setField(agentMemoryConfigService, "memoryServiceClient", memoryServiceClient);
        ReflectionTestUtils.setField(agentMemoryConfigService, "agentCommonService", agentCommonService);
    }

    @Test
    void test_checkUserProfileConfig_should_not_throw_exception() throws Exception {
        assertDoesNotThrow(() -> {
            // Given
            UserProfileMemoryConfig userProfileMemoryConfig = new UserProfileMemoryConfig();

            // When
            agentMemoryConfigService.checkUserProfileConfig(userProfileMemoryConfig);
        });
    }

    @Test
    void test_checkUserProfileConfig_should_pass() throws Exception {
        assertDoesNotThrow(() -> {
            UserProfileMemoryConfig userProfileMemoryConfig = new UserProfileMemoryConfig();
            userProfileMemoryConfig.setUserProfileEnable(true);
            userProfileMemoryConfig.setTopics(new ArrayList<>());

            UserProfileTopicInfo topic1 = new UserProfileTopicInfo();
            topic1.setTopicName("topic1");
            topic1.setTags(new ArrayList<>());
            UserProfileTagInfo tag1 = new UserProfileTagInfo();
            tag1.setTagName("tagName1");
            topic1.getTags().add(tag1);
            userProfileMemoryConfig.getTopics().add(topic1);

            UserProfileTopicInfo topic2 = new UserProfileTopicInfo();
            topic2.setTopicName("topic2");
            topic2.setTags(new ArrayList<>());
            UserProfileTagInfo tag2 = new UserProfileTagInfo();
            tag2.setTagName("tagName2");
            topic2.getTags().add(tag2);
            userProfileMemoryConfig.getTopics().add(topic2);

            // When
            agentMemoryConfigService.checkUserProfileConfig(userProfileMemoryConfig);
        });
    }

    @Test
    void test_clearUserMemoriesInApplication_should_pass() throws Exception {
        assertDoesNotThrow(() -> {
            // Given
            when(memoryServiceClient.deleteLongTermMemories(any(), any(), any(), any())).thenReturn(null);

            // When
            agentMemoryConfigService.clearUserMemoriesInApplication("token", "zh-CN", "project-id", "app-id");
        });
    }
}
