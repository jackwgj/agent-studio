/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.agentbase.mapper.KnowledgeBaseMapper;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;
import com.openjiuwen.studio.agent.manager.dto.ListFaqQo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class KnowledgeBaseFaqServiceImplTest {

    @InjectMocks
    private com.openjiuwen.studio.agent.agentbase.service.KnowledgeBaseFaqServiceImpl knowledgeBaseFaqService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private KnowledgeBaseMapper knowledgeBaseMapper;

    @Test
    void test_listFaq_throw_exception() {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS)) {
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestProjectId())
                .thenReturn("not_empty");
            when(knowledgeBaseMapper.selectById(anyString(), anyString())).thenReturn(null);
            assertThrows(AgentBaseException.class,
                () -> knowledgeBaseFaqService.listFaq("projectId", "knowledgeBaseId", new ListFaqQo()));
        }
    }
}
