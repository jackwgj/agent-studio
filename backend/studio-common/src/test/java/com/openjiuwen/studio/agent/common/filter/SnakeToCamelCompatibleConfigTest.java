/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.core.Ordered;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SnakeToCamelCompatibleConfigTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    @Test
    void testSnakeToCamelFilter_Registration() {
        SnakeToCamelCompatibleConfig config = new SnakeToCamelCompatibleConfig();
        FilterRegistrationBean<?> registration = config.snakeToCamelFilter();
        assertNotNull(registration);
        assertEquals(Ordered.LOWEST_PRECEDENCE, registration.getOrder());
        assertEquals("snakeToCamelCompatibleFilter", registration.getFilterName());
    }

    @Test
    void testDoFilter_SnakeCaseParamConverted() throws Exception {
        when(request.getParameterMap()).thenReturn(Map.of("page_size", new String[]{"10"}));
        SnakeToCamelCompatibleConfig.SnakeToCamelCompatibleFilter filter =
                new SnakeToCamelCompatibleConfig.SnakeToCamelCompatibleFilter();
        filter.doFilter(request, response, chain);

        ArgumentCaptor<HttpServletRequest> captor = ArgumentCaptor.forClass(HttpServletRequest.class);
        verify(chain).doFilter(captor.capture(), any());
        HttpServletRequest wrapped = captor.getValue();

        assertEquals("10", wrapped.getParameter("page_size"));
        assertEquals("10", wrapped.getParameter("pageSize"));
    }

    @Test
    void testDoFilter_NoSnakeCaseParam() throws Exception {
        when(request.getParameterMap()).thenReturn(Map.of("pageSize", new String[]{"20"}));
        SnakeToCamelCompatibleConfig.SnakeToCamelCompatibleFilter filter =
                new SnakeToCamelCompatibleConfig.SnakeToCamelCompatibleFilter();
        filter.doFilter(request, response, chain);

        ArgumentCaptor<HttpServletRequest> captor = ArgumentCaptor.forClass(HttpServletRequest.class);
        verify(chain).doFilter(captor.capture(), any());
        HttpServletRequest wrapped = captor.getValue();

        assertEquals("20", wrapped.getParameter("pageSize"));
    }

    @Test
    void testDoFilter_MultipleSnakeCaseParams() throws Exception {
        when(request.getParameterMap()).thenReturn(Map.of(
                "page_size", new String[]{"10"},
                "sort_order", new String[]{"asc"}
        ));
        SnakeToCamelCompatibleConfig.SnakeToCamelCompatibleFilter filter =
                new SnakeToCamelCompatibleConfig.SnakeToCamelCompatibleFilter();
        filter.doFilter(request, response, chain);

        ArgumentCaptor<HttpServletRequest> captor = ArgumentCaptor.forClass(HttpServletRequest.class);
        verify(chain).doFilter(captor.capture(), any());
        HttpServletRequest wrapped = captor.getValue();

        assertEquals("10", wrapped.getParameter("pageSize"));
        assertEquals("asc", wrapped.getParameter("sortOrder"));
    }

    @Test
    void testDoFilter_ParameterValues() throws Exception {
        when(request.getParameterMap()).thenReturn(Map.of("my_param", new String[]{"v1", "v2"}));
        SnakeToCamelCompatibleConfig.SnakeToCamelCompatibleFilter filter =
                new SnakeToCamelCompatibleConfig.SnakeToCamelCompatibleFilter();
        filter.doFilter(request, response, chain);

        ArgumentCaptor<HttpServletRequest> captor = ArgumentCaptor.forClass(HttpServletRequest.class);
        verify(chain).doFilter(captor.capture(), any());
        HttpServletRequest wrapped = captor.getValue();

        assertArrayEquals(new String[]{"v1", "v2"}, wrapped.getParameterValues("myParam"));
    }

    @Test
    void testDoFilter_ParameterNames() throws Exception {
        when(request.getParameterMap()).thenReturn(Map.of("snake_key", new String[]{"val"}));
        SnakeToCamelCompatibleConfig.SnakeToCamelCompatibleFilter filter =
                new SnakeToCamelCompatibleConfig.SnakeToCamelCompatibleFilter();
        filter.doFilter(request, response, chain);

        ArgumentCaptor<HttpServletRequest> captor = ArgumentCaptor.forClass(HttpServletRequest.class);
        verify(chain).doFilter(captor.capture(), any());
        HttpServletRequest wrapped = captor.getValue();

        var names = Collections.list(wrapped.getParameterNames());
        assertTrue(names.contains("snake_key"));
        assertTrue(names.contains("snakeKey"));
    }

    @Test
    void testDoFilter_ParameterMap() throws Exception {
        when(request.getParameterMap()).thenReturn(Map.of("test_key", new String[]{"val"}));
        SnakeToCamelCompatibleConfig.SnakeToCamelCompatibleFilter filter =
                new SnakeToCamelCompatibleConfig.SnakeToCamelCompatibleFilter();
        filter.doFilter(request, response, chain);

        ArgumentCaptor<HttpServletRequest> captor = ArgumentCaptor.forClass(HttpServletRequest.class);
        verify(chain).doFilter(captor.capture(), any());
        HttpServletRequest wrapped = captor.getValue();

        Map<String, String[]> paramMap = wrapped.getParameterMap();
        assertTrue(paramMap.containsKey("test_key"));
        assertTrue(paramMap.containsKey("testKey"));
    }

    @Test
    void testDoFilter_NonExistentParam() throws Exception {
        when(request.getParameterMap()).thenReturn(Map.of());
        SnakeToCamelCompatibleConfig.SnakeToCamelCompatibleFilter filter =
                new SnakeToCamelCompatibleConfig.SnakeToCamelCompatibleFilter();
        filter.doFilter(request, response, chain);

        ArgumentCaptor<HttpServletRequest> captor = ArgumentCaptor.forClass(HttpServletRequest.class);
        verify(chain).doFilter(captor.capture(), any());
        HttpServletRequest wrapped = captor.getValue();

        assertNull(wrapped.getParameter("nonExistent"));
    }
}
