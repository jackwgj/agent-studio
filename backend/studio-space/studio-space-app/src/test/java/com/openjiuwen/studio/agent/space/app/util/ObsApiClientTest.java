package com.openjiuwen.studio.agent.space.app.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.obs.services.ObsClient;
import com.obs.services.model.CopyObjectResult;
import com.obs.services.model.DeleteObjectResult;
import com.obs.services.model.ObjectMetadata;
import com.obs.services.model.ObsObject;
import com.obs.services.model.PutObjectResult;
import com.obs.services.model.StorageClassEnum;
import com.obs.services.model.TemporarySignatureRequest;
import com.obs.services.model.TemporarySignatureResponse;
import com.openjiuwen.studio.agent.space.common.exception.AgentSpaceException;

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
import org.springframework.test.util.ReflectionTestUtils;

import java.io.InputStream;
import java.util.Date;

@MockitoSettings(strictness = Strictness.LENIENT)
class ObsApiClientTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ObsClient obsClient;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ObsClient lastObsClient;

    @InjectMocks
    private ObsApiClient obsApiClient;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(obsApiClient, "endPoint", "not_empty");
        ReflectionTestUtils.setField(obsApiClient, "ak", "not_empty");
        ReflectionTestUtils.setField(obsApiClient, "sk", "not_empty");
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_downloadFile_should_return_not_null() throws Exception {
        try (MockedStatic<AgentSpaceException> mockedStaticAgentSpaceException = mockStatic(AgentSpaceException.class,
            RETURNS_DEEP_STUBS)) {
            // Given
            ObsObject obsObject = new ObsObject();
            when(obsClient.getObject(anyString(), anyString())).thenReturn(obsObject);

            // When
            ObsObject result = obsApiClient.downloadFile("not_empty", "not_empty");

            // Then
            assertNotNull(result);
        }
    }

    @Test
    void test_downloadFile_should_not_throw_exception() throws Exception {
        try (MockedStatic<AgentSpaceException> mockedStaticAgentSpaceException = mockStatic(AgentSpaceException.class,
            RETURNS_DEEP_STUBS)) {
            // Given
            when(obsClient.getObject(anyString(), anyString())).thenReturn(null);

            // When
            ObsObject result = obsApiClient.downloadFile(null, null);
        }
    }

    @Test
    void test_downloadFile_should_return_not_null1() throws Exception {
        try (MockedStatic<AgentSpaceException> mockedStaticAgentSpaceException = mockStatic(AgentSpaceException.class,
            RETURNS_DEEP_STUBS)) {
            // Given
            when(obsClient.getObject(anyString(), anyString())).thenReturn(null);

            // When
            ObsObject result = obsApiClient.downloadFile(null, null);

            // Then
            assertNotNull(result);
        }
    }

    @Test
    void test_uploadFile_should_equal_result() throws Exception {
        try (MockedStatic<AgentSpaceException> mockedStaticAgentSpaceException = mockStatic(AgentSpaceException.class,
            RETURNS_DEEP_STUBS)) {
            // Given
            PutObjectResult putObjectResult = new PutObjectResult("not_empty", "not_empty", "not_empty", "not_empty",
                StorageClassEnum.STANDARD, "not_empty");
            when(obsClient.putObject(anyString(), anyString(), any(InputStream.class),
                any(ObjectMetadata.class))).thenReturn(putObjectResult);

            FileTransferUtils.AgentBuilderFile file = mock(FileTransferUtils.AgentBuilderFile.class,
                Answers.RETURNS_DEEP_STUBS);
            when(file.getOriginalFilename()).thenReturn("not_empty");
            InputStream inputStream = mock(InputStream.class, Answers.RETURNS_DEEP_STUBS);
            when(file.getInputStream()).thenReturn(inputStream);

            ObjectMetadata meta = new ObjectMetadata();

            // When
            String result = obsApiClient.uploadFile("not_empty", "not_empty", file, meta);
        }
    }

    @Test
    void test_uploadFile_should_not_throw_exception() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            try (MockedStatic<AgentSpaceException> mockedStaticAgentSpaceException = mockStatic(
                AgentSpaceException.class, RETURNS_DEEP_STUBS)) {
                // Given
                when(obsClient.putObject(anyString(), anyString(), any(InputStream.class),
                    any(ObjectMetadata.class))).thenReturn(null);

                // When
                String result = obsApiClient.uploadFile(null, null, null, null);
            }
        });
    }

    @Test
    void test_uploadFile1_should_equal_result() throws Exception {
        try (MockedStatic<AgentSpaceException> mockedStaticAgentSpaceException = mockStatic(AgentSpaceException.class,
            RETURNS_DEEP_STUBS)) {
            // Given
            PutObjectResult putObjectResult = new PutObjectResult("not_empty", "not_empty", "not_empty", "not_empty",
                StorageClassEnum.STANDARD, "not_empty");
            when(obsClient.putObject(anyString(), anyString(), any(InputStream.class),
                any(ObjectMetadata.class))).thenReturn(putObjectResult);

            FileTransferUtils.AgentBuilderFile file = mock(FileTransferUtils.AgentBuilderFile.class,
                Answers.RETURNS_DEEP_STUBS);

            // When
            String result = obsApiClient.uploadFile("not_empty", "not_empty", file);
        }
    }

    @Test
    void test_uploadFile1_should_not_throw_exception() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            try (MockedStatic<AgentSpaceException> mockedStaticAgentSpaceException = mockStatic(
                AgentSpaceException.class, RETURNS_DEEP_STUBS)) {
                // Given
                when(obsClient.putObject(anyString(), anyString(), any(InputStream.class),
                    any(ObjectMetadata.class))).thenReturn(null);

                // When
                String result = obsApiClient.uploadFile(null, null, null);
            }
        });
    }

    @Test
    void test_getTemporaryGetRsp_should_return_not_null() throws Exception {
        // Given
        TemporarySignatureResponse temporarySignatureResponse = new TemporarySignatureResponse("not_empty");
        when(obsClient.createTemporarySignature(any(TemporarySignatureRequest.class))).thenReturn(
            temporarySignatureResponse);

        // When
        TemporarySignatureResponse result = obsApiClient.getTemporaryGetRsp("not_empty", "not_empty", 0L);

        // Then
        assertNotNull(result);
    }

    @Test
    void test_getTemporaryGetRsp_should_not_throw_exception() throws Exception {
        // Given
        when(obsClient.createTemporarySignature(any(TemporarySignatureRequest.class))).thenReturn(null);

        // When
        TemporarySignatureResponse result = obsApiClient.getTemporaryGetRsp(null, null, 0L);
    }

    @Test
    void test_getTemporaryGetRsp_should_return_not_null1() throws Exception {
        // Given
        when(obsClient.createTemporarySignature(any(TemporarySignatureRequest.class))).thenReturn(null);

        // When
        TemporarySignatureResponse result = obsApiClient.getTemporaryGetRsp(null, null, 0L);
    }

    @Test
    void test_safeCopyObsObject_should_return_true() throws Exception {
        try (MockedStatic<AgentSpaceException> mockedStaticAgentSpaceException = mockStatic(AgentSpaceException.class,
            RETURNS_DEEP_STUBS)) {
            // Given
            Date lastModified = new Date();
            CopyObjectResult copyObjectResult = new CopyObjectResult("not_empty", lastModified, "not_empty",
                "not_empty", StorageClassEnum.STANDARD);
            when(obsClient.copyObject(anyString(), anyString(), anyString(), anyString())).thenReturn(copyObjectResult);

            // When
            boolean result = obsApiClient.safeCopyObsObject("not_empty", "not_empty", "not_empty", "not_empty");

            // Then
            assertTrue(result);
        }
    }

    @Test
    void test_safeCopyObsObject_should_not_throw_exception() throws Exception {
        try (MockedStatic<AgentSpaceException> mockedStaticAgentSpaceException = mockStatic(AgentSpaceException.class,
            RETURNS_DEEP_STUBS)) {
            // Given
            when(obsClient.copyObject(anyString(), anyString(), anyString(), anyString())).thenReturn(null);

            // When
            boolean result = obsApiClient.safeCopyObsObject(null, null, null, null);
        }
    }

    @Test
    void test_safeCopyObsObject_should_return_true1() throws Exception {
        try (MockedStatic<AgentSpaceException> mockedStaticAgentSpaceException = mockStatic(AgentSpaceException.class,
            RETURNS_DEEP_STUBS)) {
            // Given
            when(obsClient.copyObject(anyString(), anyString(), anyString(), anyString())).thenReturn(null);

            // When
            boolean result = obsApiClient.safeCopyObsObject(null, null, null, null);

            // Then
            assertTrue(result);
        }
    }

    @Test
    void test_copyObsObject_should_not_throw_exception() throws Exception {
        assertDoesNotThrow(() -> {
            try (MockedStatic<AgentSpaceException> mockedStaticAgentSpaceException = mockStatic(
                AgentSpaceException.class, RETURNS_DEEP_STUBS)) {
                // Given
                Date lastModified = new Date();
                CopyObjectResult copyObjectResult = new CopyObjectResult("not_empty", lastModified, "not_empty",
                    "not_empty", StorageClassEnum.STANDARD);
                when(obsClient.copyObject(anyString(), anyString(), anyString(), anyString())).thenReturn(
                    copyObjectResult);

                // When
                obsApiClient.copyObsObject("not_empty", "not_empty", "not_empty", "not_empty");
            }
        });
    }

    @Test
    void test_copyObsObject_should_not_throw_exception1() throws Exception {
        assertDoesNotThrow(() -> {
            try (MockedStatic<AgentSpaceException> mockedStaticAgentSpaceException = mockStatic(
                AgentSpaceException.class, RETURNS_DEEP_STUBS)) {
                // Given
                when(obsClient.copyObject(anyString(), anyString(), anyString(), anyString())).thenReturn(null);

                // When
                obsApiClient.copyObsObject(null, null, null, null);
            }
        });
    }

    @Test
    void test_safeDeleteObsObject_should_return_true() throws Exception {
        try (MockedStatic<AgentSpaceException> mockedStaticAgentSpaceException = mockStatic(AgentSpaceException.class,
            RETURNS_DEEP_STUBS)) {
            // Given
            DeleteObjectResult deleteObjectResult = new DeleteObjectResult(true, "not_empty");
            when(obsClient.deleteObject(anyString(), anyString())).thenReturn(deleteObjectResult);

            // When
            boolean result = obsApiClient.safeDeleteObsObject("", "");

            // Then
            assertTrue(result);
        }
    }

    @Test
    void test_safeDeleteObsObject_should_return_true1() throws Exception {
        try (MockedStatic<AgentSpaceException> mockedStaticAgentSpaceException = mockStatic(AgentSpaceException.class,
            RETURNS_DEEP_STUBS)) {
            // Given
            DeleteObjectResult deleteObjectResult = new DeleteObjectResult(true, "");
            when(obsClient.deleteObject(anyString(), anyString())).thenReturn(deleteObjectResult);

            // When
            boolean result = obsApiClient.safeDeleteObsObject("", "");

            // Then
            assertTrue(result);
        }
    }

    @Test
    void test_safeDeleteObsObject_should_return_true2() throws Exception {
        try (MockedStatic<AgentSpaceException> mockedStaticAgentSpaceException = mockStatic(AgentSpaceException.class,
            RETURNS_DEEP_STUBS)) {
            // Given
            DeleteObjectResult deleteObjectResult = new DeleteObjectResult(true, "not_empty");
            when(obsClient.deleteObject(anyString(), anyString())).thenReturn(deleteObjectResult);

            // When
            boolean result = obsApiClient.safeDeleteObsObject("not_empty", "not_empty");

            // Then
            assertTrue(result);
        }
    }

    @Test
    void test_safeDeleteObsObject_should_not_throw_exception() throws Exception {
        try (MockedStatic<AgentSpaceException> mockedStaticAgentSpaceException = mockStatic(AgentSpaceException.class,
            RETURNS_DEEP_STUBS)) {
            // Given
            when(obsClient.deleteObject(anyString(), anyString())).thenReturn(null);

            // When
            boolean result = obsApiClient.safeDeleteObsObject(null, null);
        }
    }

    @Test
    void test_safeDeleteObsObject_should_return_true3() throws Exception {
        try (MockedStatic<AgentSpaceException> mockedStaticAgentSpaceException = mockStatic(AgentSpaceException.class,
            RETURNS_DEEP_STUBS)) {
            // Given
            when(obsClient.deleteObject(anyString(), anyString())).thenReturn(null);

            // When
            boolean result = obsApiClient.safeDeleteObsObject(null, null);

            // Then
            assertTrue(result);
        }
    }

    @Test
    void test_deleteObsObject_should_return_true2() throws Exception {
        try (MockedStatic<AgentSpaceException> mockedStaticAgentSpaceException = mockStatic(AgentSpaceException.class,
            RETURNS_DEEP_STUBS)) {
            // Given
            DeleteObjectResult deleteObjectResult = new DeleteObjectResult(true, "not_empty");
            when(obsClient.deleteObject(anyString(), anyString())).thenReturn(deleteObjectResult);

            // When
            boolean result = obsApiClient.deleteObsObject("not_empty", "not_empty");

            // Then
            assertTrue(result);
        }
    }

    @Test
    void test_deleteObsObject_should_not_throw_exception() throws Exception {
        assertThrows(AgentSpaceException.class, () -> {
            try (MockedStatic<AgentSpaceException> mockedStaticAgentSpaceException = mockStatic(
                AgentSpaceException.class, RETURNS_DEEP_STUBS)) {
                // Given
                when(obsClient.deleteObject(anyString(), anyString())).thenReturn(null);

                // When
                boolean result = obsApiClient.deleteObsObject(null, null);
            }
        });
    }
}