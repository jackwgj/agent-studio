/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */
package com.openjiuwen.studio.agent.manager.aop;

import com.alibaba.fastjson2.JSON;
import com.openjiuwen.studio.agent.common.annotation.OperationLog;
import com.openjiuwen.studio.agent.common.enums.OperationType;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;

import org.apache.logging.log4j.ThreadContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OperationLogAspectTest {

    private static final String SENSITIVE_FIELD_PATTERN = "(?i)(?:.*(?:password|pwd|token|authorization|api[-_]?key|"
            + "secret|credential|auth[-_]?(?:info|key|id|value|type|keys)|access[-_]?key|client[-_]?secret|"
            + "iam[-_]?credentials|oauth[-_]?scope|app[-_]?code|iam[-_]?(?:ak|sk)).*|(?:ak|sk))";

    private static final String MASKED_VALUE = "******";

    private static final int PERFORMANCE_TEST_ITERATIONS = 1200;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    private OperationLogAspect aspect;

    @BeforeEach
    void setUp() {
        aspect = new OperationLogAspect();
        ReflectionTestUtils.setField(aspect, "sensitiveFieldPattern", SENSITIVE_FIELD_PATTERN);
        ReflectionTestUtils.invokeMethod(aspect, "initSensitiveFieldPattern");
        ThreadContext.clearAll();
    }

    @AfterEach
    void tearDown() {
        ThreadContext.clearAll();
    }

    @Test
    void aroundReturnsResultWithoutAuditWhenSwitchDisabled() throws Throwable {
        OperationLog operationLog = operationLog("createAgent", String.class, Payload.class);
        ReflectionTestUtils.setField(aspect, "operationLogSwitch", false);
        when(joinPoint.proceed()).thenReturn("result");

        Object result = aspect.around(joinPoint, operationLog);

        assertEquals("result", result);
        verify(joinPoint, never()).getSignature();
    }

    @Test
    void aroundWritesAuditAfterSuccessfulInvocation() throws Throwable {
        Method method = method("createAgent", String.class, Payload.class);
        Payload payload = new Payload("agent-1", "Agent One", Arrays.asList("first", "second"));
        OperationLog operationLog = operationLog("createAgent", String.class, Payload.class);
        ReflectionTestUtils.setField(aspect, "operationLogSwitch", true);
        mockJoinPoint(method, new String[] {"operator", "body"}, new Object[] {"creator", payload});
        when(joinPoint.proceed()).thenReturn("ok");
        ThreadContext.put("request-id", "trace-1");

        try (MockedStatic<RequestContextUtils> requestContext = mockRequestContext("user-1", "Alice")) {
            Object result = aspect.around(joinPoint, operationLog);

            assertEquals("ok", result);
            requestContext.verify(RequestContextUtils::getRequestUserId);
            requestContext.verify(RequestContextUtils::getRequestUserName);
        }
    }

    @Test
    void aroundWritesFailureAuditAndRethrowsOriginalException() throws Throwable {
        Method method = method("deleteAgent", String.class);
        OperationLog operationLog = operationLog("deleteAgent", String.class);
        IllegalStateException failure = new IllegalStateException("delete failed");
        ReflectionTestUtils.setField(aspect, "operationLogSwitch", true);
        mockJoinPoint(method, new String[] {"agentId"}, new Object[] {"agent-9"});
        when(joinPoint.proceed()).thenThrow(failure);

        IllegalStateException thrown;
        try (MockedStatic<RequestContextUtils> ignored = mockRequestContext("user-1", "Alice")) {
            thrown = assertThrows(IllegalStateException.class, () -> aspect.around(joinPoint, operationLog));
        }

        assertSame(failure, thrown);
    }

    @Test
    void writeAuditLogFallsBackWhenSignatureIsNotMethodSignature() {
        OperationLog operationLog = operationLog("createAgent", String.class, Payload.class);
        when(joinPoint.getSignature()).thenReturn(null);

        try (MockedStatic<RequestContextUtils> ignored = mockRequestContext("user-1", "Alice")) {
            ReflectionTestUtils.invokeMethod(aspect, "writeAuditLog", joinPoint, operationLog, null);
        }

        verify(joinPoint).getSignature();
    }

    @Test
    void extractResourceIdSupportsParameterNameNestedPropertyListIndexAndArrayIndex() {
        Payload payload = new Payload("agent-1", "Agent One", Arrays.asList("first", "second"));
        payload.children = new Payload[] {new Payload("agent-child-1", "Child One", List.of("child")),
                new Payload("agent-child-2", "Child Two", List.of("child"))};
        Payload[] payloads = {payload};
        Method method = method("updateAgent", String.class, Payload.class, Payload[].class);
        mockJoinPoint(method, new String[] {"agentId", "body", "payloads"}, new Object[] {"direct-id", payload, payloads});

        assertEquals("direct-id", invokeExtractResourceId("agentId"));
        assertEquals("agent-1", invokeExtractResourceId("body.id"));
        assertEquals("second", invokeExtractResourceId("body.ids[1]"));
        assertEquals("agent-child-2", invokeExtractResourceId("body.children[1].id"));
    }

    @Test
    void extractResourceIdReturnsNullForMissingInvalidOrNullInputs() {
        Method method = method("updateAgent", String.class, Payload.class, Payload[].class);
        mockJoinPoint(method, new String[] {"agentId", "body", "payloads"}, new Object[] {null, null, new Payload[0]});

        assertNull(invokeExtractResourceId(null));
        assertNull(invokeExtractResourceId(""));
        assertNull(invokeExtractResourceId("missing"));
        assertNull(invokeExtractResourceId("body.id"));
        assertNull(invokeExtractResourceId("payloads[2].id"));
        assertNull(invokeExtractResourceId("body.ids[broken]"));
    }

    @Test
    void extractResourceIdReturnsNullWhenParameterNamesOrArgsAreUnavailable() {
        Method method = method("deleteAgent", String.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(methodSignature.getParameterNames()).thenReturn(null);
        when(joinPoint.getArgs()).thenReturn(new Object[] {"agent-1"});

        assertNull(invokeExtractResourceId("agentId"));

        when(methodSignature.getParameterNames()).thenReturn(new String[] {"agentId"});
        when(joinPoint.getArgs()).thenReturn(null);

        assertNull(invokeExtractResourceId("agentId"));
    }

    @Test
    void extractPropertyValueSupportsGetterPublicFieldNullAndInvalidValues() {
        Payload payload = new Payload("agent-1", "Agent One", Arrays.asList("first", "second"));
        PublicFieldHolder fieldHolder = new PublicFieldHolder("visible-value");
        PrimitiveArrayHolder arrayHolder = new PrimitiveArrayHolder(new int[] {1, 2});

        assertEquals("Agent One", ReflectionTestUtils.invokeMethod(aspect, "extractPropertyValue", payload, "name"));
        assertEquals("visible-value",
                ReflectionTestUtils.invokeMethod(aspect, "extractPropertyValue", fieldHolder, "visible"));
        assertEquals("2", ReflectionTestUtils.invokeMethod(aspect, "extractPropertyValue", arrayHolder, "numbers[1]"));
        assertNull(ReflectionTestUtils.invokeMethod(aspect, "extractPropertyValue", null, "name"));
        assertNull(ReflectionTestUtils.invokeMethod(aspect, "extractPropertyValue", payload, null));
        assertNull(ReflectionTestUtils.invokeMethod(aspect, "extractPropertyValue", payload, ""));
        assertNull(ReflectionTestUtils.invokeMethod(aspect, "extractPropertyValue", payload, "missing"));
        assertNull(ReflectionTestUtils.invokeMethod(aspect, "extractPropertyValue", payload, "ids[99]"));
    }

    @Test
    void getMethodParamsMapReturnsMaskedMethodParams() {
        Payload payload = new Payload("agent-1", "Agent One", Arrays.asList("first", "second"));
        Map<String, Object> authKeys = new HashMap<>();
        authKeys.put("token", "short");
        authKeys.put("secretKey", "abcdefghijkl");
        authKeys.put("safe", "plain");
        Method method = method("complexParams", String.class, Payload.class, Map.class, List.class);
        mockJoinPoint(method, new String[] {"password", "body", "authKeys", "items"},
                new Object[] {"1234567890", payload, authKeys,
                        Arrays.asList(new Payload("agent-2", "Agent Two", List.of("third")), null)});

        Map<String, Object> params = ReflectionTestUtils.invokeMethod(aspect, "getMethodParamsMap", joinPoint);
        Map<?, ?> body = (Map<?, ?>) params.get("body");
        Map<?, ?> sanitizedAuthKeys = (Map<?, ?>) params.get("authKeys");
        List<?> items = (List<?>) params.get("items");
        Map<?, ?> firstItem = (Map<?, ?>) items.get(0);
        String paramsJson = JSON.toJSONString(params);

        assertEquals(MASKED_VALUE, params.get("password"));
        assertEquals("agent-1", body.get("id"));
        assertEquals("Agent One", body.get("name"));
        assertEquals(List.of("first", "second"), body.get("ids"));
        assertEquals(MASKED_VALUE, sanitizedAuthKeys.get("token"));
        assertEquals(MASKED_VALUE, sanitizedAuthKeys.get("secretKey"));
        assertEquals("plain", sanitizedAuthKeys.get("safe"));
        assertEquals("agent-2", firstItem.get("id"));
        assertEquals("Agent Two", firstItem.get("name"));
        assertNull(items.get(1));
        assertFalse(paramsJson.contains("1234567890"));
        assertFalse(paramsJson.contains("abcdefghijkl"));
    }

    @Test
    void getMethodParamsMapReturnsEmptyMapWhenJoinPointMetadataCannotBeRead() {
        when(joinPoint.getSignature()).thenThrow(new IllegalStateException("bad signature"));

        Map<String, Object> params = ReflectionTestUtils.invokeMethod(aspect, "getMethodParamsMap", joinPoint);

        assertTrue(params.isEmpty());
    }

    @Test
    void getMethodParamsMapReturnsEmptyMapWhenParameterNamesAndArgsDoNotMatch() {
        Method method = method("deleteAgent", String.class);
        mockJoinPoint(method, new String[] {"agentId", "extra"}, new Object[] {"agent-1"});

        Map<String, Object> params = ReflectionTestUtils.invokeMethod(aspect, "getMethodParamsMap", joinPoint);

        assertTrue(params.isEmpty());
    }

    @Test
    void getMethodParamsMapSerializesArraysAndMasksSensitiveArrayValue() {
        Payload[] payloads = {
                new Payload("agent-1", "Agent One", List.of("id-1")),
                new Payload("agent-2", "Agent Two", List.of("id-2"))
        };
        Method method = method("arrayParams", String[].class, String[].class, int[].class, Payload[].class);
        mockJoinPoint(method, new String[] {"names", "password", "numbers", "payloads"},
                new Object[] {new String[] {"first", "second"}, new String[] {"12345678", "1234567890"},
                        new int[] {1, 2}, payloads});

        Map<String, Object> params = ReflectionTestUtils.invokeMethod(aspect, "getMethodParamsMap", joinPoint);
        List<?> payloadList = (List<?>) params.get("payloads");
        Map<?, ?> firstPayload = (Map<?, ?>) payloadList.get(0);
        Map<?, ?> secondPayload = (Map<?, ?>) payloadList.get(1);
        String paramsJson = JSON.toJSONString(params);

        assertEquals(List.of("first", "second"), params.get("names"));
        assertEquals(List.of(MASKED_VALUE, MASKED_VALUE), params.get("password"));
        assertEquals(List.of(1, 2), params.get("numbers"));
        assertEquals("agent-1", firstPayload.get("id"));
        assertEquals("agent-2", secondPayload.get("id"));
        assertFalse(paramsJson.contains("12345678"));
        assertFalse(paramsJson.contains("1234567890"));
    }

    @Test
    void inferResourceTypeUsesKnownSuffixesAndUnknownFallbacks() {
        assertEquals("Agent", ReflectionTestUtils.invokeMethod(aspect, "inferResourceType", "AgentManagementService"));
        assertEquals("ModelManager", ReflectionTestUtils.invokeMethod(aspect, "inferResourceType", "ModelManagerService"));
        assertEquals("Tool", ReflectionTestUtils.invokeMethod(aspect, "inferResourceType", "ToolService"));
        assertEquals("Unknown", ReflectionTestUtils.invokeMethod(aspect, "inferResourceType", (Object) null));
        assertEquals("Unknown", ReflectionTestUtils.invokeMethod(aspect, "inferResourceType", ""));
    }

    @Test
    void sensitiveFieldPatternCoversKnownSensitiveFieldNames() {
        List<String> sensitiveFields = List.of(
                "apiKey", "APIKey", "api_key", "api-key", "apikey", "apiKeyId", "apiKeyName", "apiKeyValue",
                "secretKey", "secret_key", "secret", "accessKey", "access_key", "access-key", "clientSecret",
                "client_secret", "iamSecret", "password", "pwd", "iamPassword", "redisPassword", "proxyPassword",
                "token", "xAuthToken", "authInfo", "auth_info", "authorization", "credential", "authKey",
                "auth_key", "authId", "auth_id", "authValue", "authType", "credentialsId", "iamCredentials",
                "iam_credentials", "oauthScope", "oauth_scope", "authKeys", "auth_keys", "ak", "sk", "iamAk",
                "iamSk", "x-custom-iam-ak", "x-custom-iam-sk", "appCode", "app_code", "app-code");

        for (String fieldName : sensitiveFields) {
            assertTrue((Boolean) ReflectionTestUtils.invokeMethod(aspect, "isSensitiveField", fieldName), fieldName);
        }
    }

    @Test
    void sensitiveFieldPatternDoesNotMaskCommonNonSensitiveNames() {
        List<String> nonSensitiveFields = List.of("name", "taskName", "maskType", "skuCode", "askText");

        for (String fieldName : nonSensitiveFields) {
            assertFalse((Boolean) ReflectionTestUtils.invokeMethod(aspect, "isSensitiveField", fieldName), fieldName);
        }
    }

    @Test
    void maskSensitiveTextMasksConfiguredKeywordFollowingChars() {
        String text = "{\"apiKey\":\"1234567890\",\"name\":\"visible\","
                + "\"authKeys\":{\"token\":\"short\",\"safe\":\"plain\"}}";

        String masked = ReflectionTestUtils.invokeMethod(aspect, "maskSensitiveText", text);

        assertTrue(masked.contains("\"apiKey\":\"" + MASKED_VALUE + "\""));
        assertTrue(masked.contains("\"name\":\"visible\""));
        assertTrue(masked.contains("\"authKeys\":{\"token\":\"" + MASKED_VALUE + "\",\"safe\":\"plain\"}"));
        assertFalse(masked.contains("1234567890"));
        assertFalse(masked.contains("short"));
    }

    @Test
    void maskSensitiveTextOnlyMasksConfiguredLengthForLongStringValue() {
        String text = "{\"token\":\"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ\",\"password\":\"1234567890\"}";

        String masked = ReflectionTestUtils.invokeMethod(aspect, "maskSensitiveText", text);

        assertEquals("{\"token\":\"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMN" + MASKED_VALUE + "\",\"password\":\""
                + MASKED_VALUE + "\"}", masked);
    }

    @Test
    void maskSensitiveTextKeepsContainerFieldsAndMasksNestedSensitiveValues() {
        String text = "{\"token\":{\"accessKey\":\"abc\",\"fgewf\":\"afewf\"}}";

        String masked = ReflectionTestUtils.invokeMethod(aspect, "maskSensitiveText", text);

        assertEquals("{\"token\":{\"accessKey\":\"" + MASKED_VALUE + "\",\"fgewf\":\"afewf\"}}", masked);
    }

    @Test
    void getMethodParamsMapMasksSensitiveJsonValuesAtAnySerializedLevel() {
        Map<String, Object> metadata = new HashMap<>();
        Map<String, Object> nested = new HashMap<>();
        nested.put("token", "short");
        nested.put("visible", "nested-value");
        metadata.put("safe", "plain");
        metadata.put("secretKey", "abcdefghijkl");
        metadata.put("nested", nested);
        Method method = method("mapParam", Map.class);
        mockJoinPoint(method, new String[] {"metadata"}, new Object[] {metadata});

        Map<String, Object> params = ReflectionTestUtils.invokeMethod(aspect, "getMethodParamsMap", joinPoint);
        Map<?, ?> sanitizedMetadata = (Map<?, ?>) params.get("metadata");
        Map<?, ?> sanitizedNested = (Map<?, ?>) sanitizedMetadata.get("nested");
        String paramsJson = JSON.toJSONString(params);

        assertEquals("plain", sanitizedMetadata.get("safe"));
        assertEquals(MASKED_VALUE, sanitizedMetadata.get("secretKey"));
        assertEquals(MASKED_VALUE, sanitizedNested.get("token"));
        assertEquals("nested-value", sanitizedNested.get("visible"));
        assertFalse(paramsJson.contains("abcdefghijkl"));
        assertFalse(paramsJson.contains("short"));
    }

    @Test
    void getMethodParamsMapProcessesComplexNestedParamsWithinBoundedTimeAndThreads() {
        Method method = method("mapParam", Map.class);
        mockJoinPoint(method, new String[] {"metadata"}, new Object[] {complexNestedParams()});

        for (int i = 0; i < 50; i++) {
            ReflectionTestUtils.invokeMethod(aspect, "getMethodParamsMap", joinPoint);
        }

        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        int threadsBefore = threadMXBean.getThreadCount();
        long startNanos = System.nanoTime();
        Map<String, Object> params = null;
        for (int i = 0; i < PERFORMANCE_TEST_ITERATIONS; i++) {
            params = ReflectionTestUtils.invokeMethod(aspect, "getMethodParamsMap", joinPoint);
        }
        long elapsedNanos = System.nanoTime() - startNanos;
        int threadsAfter = threadMXBean.getThreadCount();
        long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(elapsedNanos);
        long averageMicros = TimeUnit.NANOSECONDS.toMicros(elapsedNanos / PERFORMANCE_TEST_ITERATIONS);
        String paramsJson = JSON.toJSONString(params);

        System.out.printf("OperationLogAspect complex params performance: iterations=%d, elapsedMs=%d, "
                        + "avgMicros=%d, threadsBefore=%d, threadsAfter=%d%n",
                PERFORMANCE_TEST_ITERATIONS, elapsedMillis, averageMicros, threadsBefore, threadsAfter);

        assertTrue(paramsJson.contains(MASKED_VALUE));
        assertTrue(paramsJson.contains("visible-level-1"));
        assertTrue(paramsJson.contains("visible-leaf"));
        assertFalse(paramsJson.contains("deep-access-key-abcdefghijklmnopqrstuvwxyz"));
        assertFalse(paramsJson.contains("client-secret-0123456789"));
        assertFalse(paramsJson.contains("token-array-value-1"));
        assertFalse(paramsJson.contains("secret-in-array-object"));
        assertTrue(elapsedMillis < 10000,
                "Complex operation log masking should not take too long, elapsedMs=" + elapsedMillis);
        assertTrue(threadsAfter <= threadsBefore + 5,
                "Operation log masking should not create many JVM threads, before=" + threadsBefore
                        + ", after=" + threadsAfter);
    }

    @Test
    void getMethodParamsMapReturnsEmptyMapWhenReferenceJsonCannotBeParsed() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("safe", "plain");
        metadata.put("self", metadata);
        Method method = method("mapParam", Map.class);
        mockJoinPoint(method, new String[] {"metadata"}, new Object[] {metadata});

        Map<String, Object> params = ReflectionTestUtils.invokeMethod(aspect, "getMethodParamsMap", joinPoint);

        assertTrue(params.isEmpty());
    }

    @Test
    void sensitiveFieldPatternUsesConfiguredRegex() {
        OperationLogAspect configuredAspect = new OperationLogAspect();
        ReflectionTestUtils.setField(configuredAspect, "sensitiveFieldPattern", "(?i).*private.*");
        ReflectionTestUtils.invokeMethod(configuredAspect, "initSensitiveFieldPattern");

        assertTrue((Boolean) ReflectionTestUtils.invokeMethod(configuredAspect, "isSensitiveField", "privateKey"));
        assertFalse((Boolean) ReflectionTestUtils.invokeMethod(configuredAspect, "isSensitiveField", "apiKey"));
    }

    @Test
    void sensitiveFieldPatternIgnoresInvalidConfiguredRegex() {
        OperationLogAspect invalidRuleAspect = new OperationLogAspect();
        ReflectionTestUtils.setField(invalidRuleAspect, "sensitiveFieldPattern", "[");
        ReflectionTestUtils.invokeMethod(invalidRuleAspect, "initSensitiveFieldPattern");

        assertFalse((Boolean) ReflectionTestUtils.invokeMethod(invalidRuleAspect, "isSensitiveField", "password"));
    }

    @Test
    void sensitiveFieldPatternDoesNotMaskWhenNoRulesConfigured() {
        OperationLogAspect noRuleAspect = new OperationLogAspect();
        ReflectionTestUtils.setField(noRuleAspect, "sensitiveFieldPattern", "");
        ReflectionTestUtils.invokeMethod(noRuleAspect, "initSensitiveFieldPattern");

        assertFalse((Boolean) ReflectionTestUtils.invokeMethod(noRuleAspect, "isSensitiveField", "password"));
    }

    @Test
    void getMethodParamsMapIgnoresJsonGetterErrors() {
        Method method = method("explodingParam", ExplodingPayload.class);
        mockJoinPoint(method, new String[] {"body"}, new Object[] {new ExplodingPayload()});

        Map<String, Object> params = ReflectionTestUtils.invokeMethod(aspect, "getMethodParamsMap", joinPoint);

        assertTrue(((Map<?, ?>) params.get("body")).isEmpty());
    }

    private Map<String, Object> complexNestedParams() {
        Map<String, Object> leaf = new HashMap<>();
        leaf.put("accessKey", "deep-access-key-abcdefghijklmnopqrstuvwxyz");
        leaf.put("clientSecret", "client-secret-0123456789");
        leaf.put("visibleLeaf", "visible-leaf");

        Map<String, Object> arrayObject = new HashMap<>();
        arrayObject.put("secretKey", "secret-in-array-object");
        arrayObject.put("name", "array-object");

        Map<String, Object> level4 = new HashMap<>();
        level4.put("authInfo", leaf);
        level4.put("tokenList", List.of("token-array-value-1", "token-array-value-2", arrayObject));
        level4.put("safeList", List.of("safe-1", "safe-2"));

        Map<String, Object> level3 = new HashMap<>();
        level3.put("credentials", level4);
        level3.put("visibleLevel3", "visible-level-3");

        Map<String, Object> level2 = new HashMap<>();
        level2.put("children", List.of(level3, Map.of("visibleSibling", "visible-sibling")));
        level2.put("authorization", Map.of("scope", "read-write", "token", "nested-token-value"));

        Map<String, Object> level1 = new HashMap<>();
        level1.put("visibleLevel1", "visible-level-1");
        level1.put("payload", level2);
        level1.put("count", 42);
        return level1;
    }

    private MockedStatic<RequestContextUtils> mockRequestContext(String userId, String userName) {
        MockedStatic<RequestContextUtils> requestContext = org.mockito.Mockito.mockStatic(RequestContextUtils.class);
        requestContext.when(RequestContextUtils::getRequestUserId).thenReturn(userId);
        requestContext.when(RequestContextUtils::getRequestUserName).thenReturn(userName);
        return requestContext;
    }

    private void mockJoinPoint(Method method, String[] parameterNames, Object[] args) {
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(methodSignature.getDeclaringType()).thenReturn((Class) method.getDeclaringClass());
        when(methodSignature.getParameterNames()).thenReturn(parameterNames);
        when(joinPoint.getArgs()).thenReturn(args);
    }

    private String invokeExtractResourceId(String resourceId) {
        return ReflectionTestUtils.invokeMethod(aspect, "extractResourceId", joinPoint, resourceId);
    }

    private OperationLog operationLog(String methodName, Class<?>... parameterTypes) {
        return method(methodName, parameterTypes).getAnnotation(OperationLog.class);
    }

    private Method method(String methodName, Class<?>... parameterTypes) {
        try {
            return TestManagementService.class.getDeclaredMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    public static class TestManagementService {
        @OperationLog(operationType = OperationType.CREATE, resourceType = "Agent", description = "Create agent",
                resourceId = "body.id", resourceName = "body.name")
        void createAgent(String operator, Payload body) {
        }

        @OperationLog(operationType = OperationType.UPDATE, resourceType = "", description = "",
                resourceId = "body.ids[0]", resourceName = "body.name")
        void updateAgent(String agentId, Payload body, Payload[] payloads) {
        }

        @OperationLog(operationType = OperationType.DELETE, resourceType = "", description = "", resourceId = "agentId")
        void deleteAgent(String agentId) {
        }

        void complexParams(String password, Payload body, Map<String, Object> authKeys, List<Payload> items) {
        }

        void mapParam(Map<String, Object> metadata) {
        }

        void arrayParams(String[] names, String[] password, int[] numbers, Payload[] payloads) {
        }

        void explodingParam(ExplodingPayload body) {
        }
    }

    public static class Payload {
        private final String id;
        private final String name;
        private final List<String> ids;
        private Payload[] children;

        Payload(String id, String name, List<String> ids) {
            this.id = id;
            this.name = name;
            this.ids = ids;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public List<String> getIds() {
            return ids;
        }

        public Payload[] getChildren() {
            return children;
        }
    }

    public static class PublicFieldHolder {
        public String visible;

        PublicFieldHolder(String visible) {
            this.visible = visible;
        }
    }

    private static class PrimitiveArrayHolder {
        private final int[] numbers;

        PrimitiveArrayHolder(int[] numbers) {
            this.numbers = numbers;
        }

        public int[] getNumbers() {
            return numbers;
        }
    }

    public static class ExplodingPayload {
        public String getPassword() {
            throw new IllegalStateException("boom");
        }
    }
}
