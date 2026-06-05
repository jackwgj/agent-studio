package com.openjiuwen.studio.agent.space.app.service.session.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.google.common.base.Preconditions;
import com.openjiuwen.studio.agent.common.crypt.Ciphers;
import com.openjiuwen.studio.agent.space.app.model.session.SessionInfoDetail;
import com.openjiuwen.studio.agent.space.app.service.session.config.SessionProperties;
import com.openjiuwen.studio.agent.space.app.service.session.util.SessionIdHasher;
import com.openjiuwen.studio.agent.space.app.util.DateUtils;
import com.openjiuwen.studio.agent.space.common.utils.JacksonUtils;
import com.openjiuwen.studio.agent.space.common.utils.redis.RedisClient;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.Date;

@MockitoSettings(strictness = Strictness.LENIENT)
class SessionRedisServiceImplTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RedisClient redisClient;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SessionIdHasher sessionIdHasher;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Ciphers ciphers;

    @InjectMocks
    private SessionRedisServiceImpl sessionRedisServiceImpl;

    private AutoCloseable mockitoCloseable;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SessionProperties sessionProperties;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_querySession_should_return_not_null() throws Exception {
        try (MockedStatic<Preconditions> mockedStaticPreconditions = mockStatic(Preconditions.class,
            RETURNS_DEEP_STUBS);
            MockedStatic<JacksonUtils> mockedStaticJacksonUtils = mockStatic(JacksonUtils.class, RETURNS_DEEP_STUBS)) {
            // Given
            SessionInfoDetail sessionInfoDetail = new SessionInfoDetail();
            mockedStaticJacksonUtils.when(() -> JacksonUtils.json2pojo(anyString(), eq(SessionInfoDetail.class)))
                .thenReturn(sessionInfoDetail);

            when(ciphers.decrypt(anyString())).thenReturn("not_empty");

            when(sessionIdHasher.hash(anyString())).thenReturn("not_empty");

            when(redisClient.get(anyString())).thenReturn("");

            // When
            SessionInfoDetail result = sessionRedisServiceImpl.querySession("not_empty");

            // Then
            SessionInfoDetail expect = new SessionInfoDetail();
            expect.setSessionId("not_empty");
            assertEquals(expect, result);
        }
    }

    @Test
    void test_querySession_should_return_null() throws Exception {
        try (MockedStatic<Preconditions> mockedStaticPreconditions = mockStatic(Preconditions.class,
            RETURNS_DEEP_STUBS);
            MockedStatic<JacksonUtils> mockedStaticJacksonUtils = mockStatic(JacksonUtils.class, RETURNS_DEEP_STUBS)) {
            // Given
            SessionInfoDetail sessionInfoDetail = new SessionInfoDetail();
            mockedStaticJacksonUtils.when(() -> JacksonUtils.json2pojo(anyString(), eq(SessionInfoDetail.class)))
                .thenReturn(sessionInfoDetail);

            when(ciphers.decrypt(anyString())).thenReturn("not_empty");

            when(sessionIdHasher.hash(anyString())).thenReturn("not_empty");

            when(redisClient.get(anyString())).thenReturn("not_empty");

            // When
            SessionInfoDetail result = sessionRedisServiceImpl.querySession("not_empty");

            // Then
            assertNull(result);
        }
    }

    @Test
    void test_updateSession_should_not_throw_exception() throws Exception {
        assertDoesNotThrow(() -> {
            try (MockedStatic<DateUtils> mockedStaticDateUtils = mockStatic(DateUtils.class, RETURNS_DEEP_STUBS);
                MockedStatic<JacksonUtils> mockedStaticJacksonUtils = mockStatic(JacksonUtils.class,
                    RETURNS_DEEP_STUBS)) {
                // Given
                Date date = new Date();
                mockedStaticDateUtils.when(() -> DateUtils.asDate(any(LocalDateTime.class))).thenReturn(date);

                mockedStaticJacksonUtils.when(() -> JacksonUtils.obj2json(any(Object.class))).thenReturn("not_empty");

                when(sessionProperties.getTimeOut()).thenReturn(0L);
                when(sessionProperties.getRedisTimeOut()).thenReturn(0L);
                when(sessionProperties.getMaxTimeOut()).thenReturn(0L);

                when(sessionIdHasher.hash(anyString())).thenReturn("not_empty");

                when(ciphers.encrypt(anyString())).thenReturn("not_empty");

                SessionInfoDetail sessionInfo = new SessionInfoDetail();
                sessionInfo.setSessionId("not_empty");

                // When
                sessionRedisServiceImpl.updateSession(sessionInfo);
            }
        });
    }
}