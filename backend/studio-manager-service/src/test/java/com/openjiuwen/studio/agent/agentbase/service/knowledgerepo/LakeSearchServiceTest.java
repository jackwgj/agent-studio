/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.knowledgerepo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.agentbase.client.CssUniSearchClient;
import com.openjiuwen.studio.agent.agentbase.model.ListKnowledgeRepoResp;
import com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.knowledgesourceprovider.LakeSearchConnectionProvider;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.dto.ListKnowledgeRepoReq;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * CUSTOM 模式下知识库列表查询的 knowledgeBaseType 透传单测。
 * 验证 LakeSearchService.listKnowledgeRepos 将 req.knowledgeBaseType 透传到
 * CssUniSearchClient.listKnowledgeRepos 的 type 查询参数位（第 8 个参数）。
 */
@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class LakeSearchServiceTest {

    @Mock
    private CssUniSearchClient cssUniSearchClient;

    @Mock
    private LakeSearchConnectionProvider lakeSearchConnectionProvider;

    @Mock
    private KnowledgeConnectionRouterService knowledgeConnectionRouterService;

    private LakeSearchService lakeSearchService;

    @BeforeEach
    void init() {
        lakeSearchService = new LakeSearchService(cssUniSearchClient,
            lakeSearchConnectionProvider, knowledgeConnectionRouterService);
        // 注入 CUSTOM 模式相关配置（@Value 字段）
        ReflectionTestUtils.setField(lakeSearchService, "knowledgeSource", "CUSTOM");
        ReflectionTestUtils.setField(lakeSearchService, "lakeSearchEndpoint", "http://localhost:28081");
        ReflectionTestUtils.setField(lakeSearchService, "lakeSearchProjectId", "test_proj");
        ReflectionTestUtils.setField(lakeSearchService, "lakeSearchApplicationId", "test_app");
        ReflectionTestUtils.setField(lakeSearchService, "authMode", "none");

        when(knowledgeConnectionRouterService.findConnectionIdByContext()).thenReturn("testConnectionId");

        // CUSTOM 模式 getHeaders 需要从请求上下文取 header
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("cust-userid", "custUser");
        request.addHeader("cust-token", "custToken");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    /**
     * 选"企业知识库"时（knowledgeBaseType=2），type 参数应透传为 2。
     */
    @Test
    void listKnowledgeReposShouldPassKnowledgeBaseTypeToClientTest() {
        ListKnowledgeRepoReq req = new ListKnowledgeRepoReq();
        req.setPageNum(1);
        req.setPageSize(10);
        req.setName("testName");
        req.setKnowledgeBaseType(2);

        ListKnowledgeRepoResp mockResp = new ListKnowledgeRepoResp();
        mockResp.setTotal(15L);
        when(cssUniSearchClient.listKnowledgeRepos(any(), any(), anyString(), anyString(), anyInt(), anyInt(),
            anyString(), any(), any(), any(), any(), any())).thenReturn(mockResp);

        try (MockedStatic<RequestContextUtils> mocked = mockStatic(RequestContextUtils.class)) {
            mocked.when(RequestContextUtils::getRequestUserId).thenReturn("testUser");
            lakeSearchService.listKnowledgeRepos(req);
        }

        ArgumentCaptor<Integer> typeCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(cssUniSearchClient).listKnowledgeRepos(any(), any(), anyString(), anyString(), anyInt(), anyInt(),
            anyString(), typeCaptor.capture(), any(), any(), any(), any());
        assertEquals(2, typeCaptor.getValue());
    }

    /**
     * 选"全部"时（knowledgeBaseType=null），type 参数应透传为 null。
     */
    @Test
    void listKnowledgeReposShouldPassNullWhenKnowledgeBaseTypeAbsentTest() {
        ListKnowledgeRepoReq req = new ListKnowledgeRepoReq();
        req.setPageNum(1);
        req.setPageSize(10);
        req.setName("testName");
        // 不设置 knowledgeBaseType，默认 null（对应"全部"）

        ListKnowledgeRepoResp mockResp = new ListKnowledgeRepoResp();
        mockResp.setTotal(30L);
        when(cssUniSearchClient.listKnowledgeRepos(any(), any(), anyString(), anyString(), anyInt(), anyInt(),
            anyString(), any(), any(), any(), any(), any())).thenReturn(mockResp);

        try (MockedStatic<RequestContextUtils> mocked = mockStatic(RequestContextUtils.class)) {
            mocked.when(RequestContextUtils::getRequestUserId).thenReturn("testUser");
            lakeSearchService.listKnowledgeRepos(req);
        }

        ArgumentCaptor<Integer> typeCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(cssUniSearchClient).listKnowledgeRepos(any(), any(), anyString(), anyString(), anyInt(), anyInt(),
            anyString(), typeCaptor.capture(), any(), any(), any(), any());
        assertNull(typeCaptor.getValue());
    }
}
