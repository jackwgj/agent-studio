/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.knowledgerepo;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.agentbase.client.AgentBaseRagClient;
import com.openjiuwen.studio.agent.agentbase.model.CreateKnowledgeRepoInfo;
import com.openjiuwen.studio.agent.agentbase.model.KnowledgeRepo;
import com.openjiuwen.studio.agent.agentbase.model.ragflow.Document;
import com.openjiuwen.studio.agent.agentbase.model.ragflow.ParserConfig;
import com.openjiuwen.studio.agent.agentbase.model.ragflow.RagFlowApiResponse;
import com.openjiuwen.studio.agent.agentbase.model.ragflow.RagFlowCreateKnowledgeBaseResp;
import com.openjiuwen.studio.agent.agentbase.model.ragflow.RagFlowListFilesResp;
import com.openjiuwen.studio.agent.agentbase.model.ragflow.RagFlowRetrieveKnowledgeBaseResp;
import com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.connection.AgentBaseRagConnection;
import com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.knowledgesourceprovider.AgentBaseRagConnectionProvider;
import com.openjiuwen.studio.agent.common.dto.knowledge.ParseConf;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;
import com.openjiuwen.studio.agent.manager.dto.CreateKnowledgeTaskResponseBody;
import com.openjiuwen.studio.agent.manager.dto.FileChunkReq;
import com.openjiuwen.studio.agent.manager.dto.ListFileReq;
import com.openjiuwen.studio.agent.manager.dto.ListKnowledgeFilesResponseBody;
import com.openjiuwen.studio.agent.manager.dto.RagChunkParserConf;

import org.apache.logging.log4j.core.util.Assert;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class AgentBaseRagServiceTest {
    @Mock
    private AgentBaseRagClient agentBaseRagClient;

    @Mock
    private AgentBaseRagConnectionProvider agentBaseRagConnectionProvider;

    @Mock
    private KnowledgeConnectionRouterService knowledgeConnectionRouterService;

    @InjectMocks
    private AgentBaseRagService agentBaseRagService;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void init() {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(agentBaseRagService, "knowledgeConnectionRouterService",
            knowledgeConnectionRouterService);
        ReflectionTestUtils.setField(agentBaseRagService, "agentBaseRagConnectionProvider",
            agentBaseRagConnectionProvider);
        ReflectionTestUtils.setField(agentBaseRagService, "agentBaseRagClient", agentBaseRagClient);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void createKnowledgeRepo() {
        RagFlowApiResponse<RagFlowCreateKnowledgeBaseResp> ragFlowApiResponse = new RagFlowApiResponse<>();
        ragFlowApiResponse.setCode(200);
        RagFlowCreateKnowledgeBaseResp ragFlowCreateKnowledgeBaseResp = new RagFlowCreateKnowledgeBaseResp();
        ragFlowCreateKnowledgeBaseResp.setKbId("mockWorkspaceId");
        ragFlowApiResponse.setData(ragFlowCreateKnowledgeBaseResp);
        when(agentBaseRagClient.createKnowledgeRepo(any(), any(), any(), any(), any())).thenReturn(ragFlowApiResponse);
        when(knowledgeConnectionRouterService.findConnectionIdByContext()).thenReturn("mockConnectionId");
        AgentBaseRagConnection agentBaseRagConnection = new AgentBaseRagConnection();
        agentBaseRagConnection.setEndpoint("https://1.1.1.1");
        when(agentBaseRagConnectionProvider.getKnowledgeSourceConnection(eq("mockConnectionId"))).thenReturn(
            agentBaseRagConnection);
        try (MockedStatic<RequestContextUtils> requestContextUtilsMockedStatic = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS)) {
            requestContextUtilsMockedStatic.when(RequestContextUtils::getRequestUserDomainId)
                .thenReturn("mockUserDomainId");
            requestContextUtilsMockedStatic.when(RequestContextUtils::getRequestAuthToken).thenReturn("mockAuthToken");
            requestContextUtilsMockedStatic.when(RequestContextUtils::getRequestProjectId).thenReturn("mockProjectId");
            KnowledgeRepo knowledgeRepo = generateKnowledgeRepo();
            CreateKnowledgeRepoInfo createKnowledgeRepoInfo = agentBaseRagService.createKnowledgeRepo(knowledgeRepo);
            Assertions.assertEquals(createKnowledgeRepoInfo.getKnowledgeBaseId(), "mockWorkspaceId");
        }
    }

    @NotNull
    private static KnowledgeRepo generateKnowledgeRepo() {
        KnowledgeRepo knowledgeRepo = new KnowledgeRepo();
        knowledgeRepo.setWorkspaceId("mockWorkspaceId");
        knowledgeRepo.setStatus("open");
        knowledgeRepo.setProjectId("mockProjectId");
        knowledgeRepo.setKnowledgeRepoId("mockKnowledgeRepoId");
        ParseConf parseConf = new ParseConf();
        parseConf.setCatalogEnabled(true);
        parseConf.setHeaderFooterEnabled(false);
        parseConf.setImageConf(ParseConf.ImageConfEnum.TEXT);
        parseConf.setOcrEnabled(false);
        parseConf.setImageEnabled(true);
        knowledgeRepo.setParseConf(parseConf);
        RagChunkParserConf ragChunkParserConf = new RagChunkParserConf();
        ragChunkParserConf.setChunkMethod(RagChunkParserConf.ChunkMethodEnum.QA);
        ragChunkParserConf.setDelimiter(Arrays.asList("mockDelimiter"));
        ragChunkParserConf.setChunkTokenNum(100);
        knowledgeRepo.setRagChunkParserConf(ragChunkParserConf);
        return knowledgeRepo;
    }

    @Test
    void getAgentBaseRagRceInvocationExceptionTest() {
        when(knowledgeConnectionRouterService.findConnectionIdByContext()).thenReturn("mockConnectionId");
        AgentBaseRagConnection agentBaseRagConnection = new AgentBaseRagConnection();
        when(agentBaseRagConnectionProvider.getKnowledgeSourceConnection(eq("mockConnectionId"))).thenReturn(
            agentBaseRagConnection);
        try (MockedStatic<RequestContextUtils> requestContextUtilsMockedStatic = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS)) {
            requestContextUtilsMockedStatic.when(RequestContextUtils::getRequestUserDomainId)
                .thenReturn("mockUserDomainId");
            requestContextUtilsMockedStatic.when(RequestContextUtils::getRequestAuthToken).thenReturn("mockAuthToken");
            requestContextUtilsMockedStatic.when(RequestContextUtils::getRequestProjectId).thenReturn("mockProjectId");
            KnowledgeRepo knowledgeRepo = generateKnowledgeRepo();
            assertThrows(AgentBaseException.class, () -> agentBaseRagService.createKnowledgeRepo(knowledgeRepo));
        }
    }

    @Test
    void modifyKnowledgeRepoTest() {
        KnowledgeRepo knowledgeRepo = generateKnowledgeRepo();
        knowledgeRepo.setRagChunkParserConf(new RagChunkParserConf().setDelimiter(null));
        AgentBaseRagConnection agentBaseRagConnection = new AgentBaseRagConnection();
        agentBaseRagConnection.setEndpoint("https://1.1.1.1");
        agentBaseRagConnection.setConnectionId("mockConnectionId");
        when(agentBaseRagConnectionProvider.getKnowledgeSourceConnection(any())).thenReturn(agentBaseRagConnection);

        RagFlowApiResponse<String> response = new RagFlowApiResponse<>();
        response.setData("ok");
        when(agentBaseRagClient.modifyKnowledgeRepo(any(), any(), any(), any(), any(), any())).thenReturn(response);
        try (MockedStatic<RequestContextUtils> requestContextUtilsMockedStatic = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS)) {
            requestContextUtilsMockedStatic.when(RequestContextUtils::getRequestUserDomainId)
                .thenReturn("mockUserDomainId");
            requestContextUtilsMockedStatic.when(RequestContextUtils::getRequestAuthToken).thenReturn("mockAuthToken");
            requestContextUtilsMockedStatic.when(RequestContextUtils::getRequestProjectId).thenReturn("mockProjectId");
            String modifyResult = agentBaseRagService.modifyKnowledgeRepo(knowledgeRepo);
            Assertions.assertEquals(modifyResult, "ok");
        }
    }

    @Test
    void createFaqTest() {
        Assertions.assertThrowsExactly(AgentBaseException.class, () -> agentBaseRagService.createFaq(null));
    }

    @Test
    void listFiles() {
        KnowledgeRepo knowledgeRepo = generateKnowledgeRepo();
        ListFileReq listFileReq = new ListFileReq();
        listFileReq.setFileName("file_name");
        listFileReq.setFileStatus("RUNNING");
        listFileReq.setFileType("doc");
        listFileReq.setPageNum(1);
        listFileReq.setPageSize(10);
        RagFlowApiResponse<RagFlowListFilesResp> response = new RagFlowApiResponse<>();
        RagFlowListFilesResp ragFlowListFilesResp = new RagFlowListFilesResp();
        ragFlowListFilesResp.setTotal(100);
        List<Document> docs = new ArrayList<>();
        Document document = new Document();
        document.setId("1");
        document.setName("file_name");
        document.setSize(1000L);
        document.setRun("1");
        document.setType("doc");
        document.setCreateTime(System.currentTimeMillis());
        document.setUpdateTime(System.currentTimeMillis());
        docs.add(document);
        ragFlowListFilesResp.setDocs(docs);
        response.setData(ragFlowListFilesResp);
        try (MockedStatic<RequestContextUtils> requestContextUtilsMockedStatic = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS)) {
            when(agentBaseRagClient.listFiles(any(), any(), any(), any(), any())).thenReturn(response);
            AgentBaseRagConnection agentBaseRagConnection = new AgentBaseRagConnection();
            agentBaseRagConnection.setEndpoint("https://1.1.1.1");
            when(agentBaseRagConnectionProvider.getKnowledgeSourceConnection(any())).thenReturn(agentBaseRagConnection);
            requestContextUtilsMockedStatic.when(RequestContextUtils::getRequestUserDomainId)
                .thenReturn("mockUserDomainId");
            requestContextUtilsMockedStatic.when(RequestContextUtils::getRequestAuthToken).thenReturn("mockAuthToken");
            requestContextUtilsMockedStatic.when(RequestContextUtils::getRequestProjectId).thenReturn("mockProjectId");
            ListKnowledgeFilesResponseBody fileInfoListRsp = agentBaseRagService.listFiles(knowledgeRepo, listFileReq);
            Assert.isNonEmpty(fileInfoListRsp);
        }
    }

    @Test
    void createFileChunkTest() {
        KnowledgeRepo knowledgeRepo = new KnowledgeRepo();
        knowledgeRepo.setKnowledgeRepoId("MockrepoId");
        doNothing().when(agentBaseRagClient).creatChunk(any(), any(), anyString(), anyString(), anyString(), any());
        try (MockedStatic<RequestContextUtils> requestContextUtilsMockedStatic = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS)) {
            AgentBaseRagConnection agentBaseRagConnection = new AgentBaseRagConnection();
            agentBaseRagConnection.setEndpoint("https://1.1.1.1");
            when(agentBaseRagConnectionProvider.getKnowledgeSourceConnection(any())).thenReturn(agentBaseRagConnection);
            requestContextUtilsMockedStatic.when(RequestContextUtils::getRequestProjectId).thenReturn("mockProjectId");
            agentBaseRagService.createFileChunk(knowledgeRepo, "fileId", new FileChunkReq());
        }
    }

    @Test
    void updateFileChunkTest() {
        KnowledgeRepo knowledgeRepo = new KnowledgeRepo();
        knowledgeRepo.setKnowledgeRepoId("repoIdTest");
        doNothing().when(agentBaseRagClient)
            .updateChunk(any(), any(), anyString(), anyString(), anyString(), anyString(), any());
        try (MockedStatic<RequestContextUtils> requestContextUtilsMockedStatic = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS)) {
            AgentBaseRagConnection agentBaseRagConnection = new AgentBaseRagConnection();
            agentBaseRagConnection.setEndpoint("https://1.1.1.1");
            when(agentBaseRagConnectionProvider.getKnowledgeSourceConnection(any())).thenReturn(agentBaseRagConnection);
            requestContextUtilsMockedStatic.when(RequestContextUtils::getRequestProjectId).thenReturn("mockProjectId");
            agentBaseRagService.updateFileChunk(knowledgeRepo, "fileId", "chunkId", new FileChunkReq());
        }
    }

    @Test
    void listFaqTest() {
        Assertions.assertThrowsExactly(AgentBaseException.class, () -> agentBaseRagService.listFaq(null));
    }

    @Test
    void createSegmentRuleTest() {
        Assertions.assertThrowsExactly(AgentBaseException.class,
            () -> agentBaseRagService.createSegmentRule(null, null));
    }

    @Test
    void modifySegmentRuleTest() {
        Assertions.assertThrowsExactly(AgentBaseException.class, () -> agentBaseRagService.modifySegmentRule(null));
    }

    @Test
    void deleteSegmentRuleTest() {
        Assertions.assertThrowsExactly(AgentBaseException.class, () -> agentBaseRagService.deleteSegmentRule(null));
    }

    @Test
    void deleteFaqTest() {
        Assertions.assertThrowsExactly(AgentBaseException.class, () -> agentBaseRagService.deleteFaq(null, null));
    }

    @Test
    void deleteFaqBatchTest() {
        Assertions.assertThrowsExactly(AgentBaseException.class, () -> agentBaseRagService.deleteFaqBatch(null, null));
    }

    @Test
    void modifyFaqTest() {
        Assertions.assertThrowsExactly(AgentBaseException.class, () -> agentBaseRagService.modifyFaq(null));
    }

    @Test
    void retrieveFaqTest() {
        Assertions.assertThrowsExactly(AgentBaseException.class, () -> agentBaseRagService.retrieveFaq(null, null));
    }

    @Test
    void listModelsTest() {
        Assertions.assertThrowsExactly(AgentBaseException.class, () -> agentBaseRagService.listModels(null, null));
    }

    @Test
    void createTask() {
        // 准备模拟请求
        String mockRepoId = "mockRepoId";
        String mockTaskType = "RETRY_FILES";
        List<String> mockFileIds = Arrays.asList("file1", "file2");

        // 创建模拟响应
        RagFlowApiResponse<Boolean> ragFlowApiResponse = new RagFlowApiResponse<>();
        ragFlowApiResponse.setCode(200);
        ragFlowApiResponse.setData(true);

        try (MockedStatic<RequestContextUtils> requestContextUtilsMockedStatic = mockStatic(
            RequestContextUtils.class)) {
            // 模拟依赖项
            requestContextUtilsMockedStatic.when(RequestContextUtils::getRequestProjectId).thenReturn("mockProjectId");
            when(agentBaseRagConnectionProvider.getConnectionIdByRepoId(any())).thenReturn("mockConnectionId");
            when(agentBaseRagClient.createTask(any(), any(), any(), any(), any())).thenReturn(ragFlowApiResponse);

            // 模拟 RceInvocation
            AgentBaseRagConnection agentBaseRagConnection = new AgentBaseRagConnection();
            agentBaseRagConnection.setEndpoint("https://1.1.1.1");
            agentBaseRagConnection.setConnectionId("mockConnectionId");
            when(agentBaseRagConnectionProvider.getKnowledgeSourceConnection(any())).thenReturn(agentBaseRagConnection);

            // 执行测试方法
            CreateKnowledgeTaskResponseBody createKnowledgeTaskResp = agentBaseRagService.createTask(mockRepoId,
                mockTaskType, mockFileIds);

            // 验证结果
            Assertions.assertEquals(createKnowledgeTaskResp.getTotalCount(), 1);
            Assertions.assertEquals(createKnowledgeTaskResp.getCreatedCount(), 1);
        }
    }

    @Test
    void retrieveKnowledgeRepo() {
        // 准备模拟请求
        KnowledgeRepo mockKnowledgeRepo = new KnowledgeRepo();
        mockKnowledgeRepo.setKnowledgeRepoId("mockRepoId");
        mockKnowledgeRepo.setWorkspaceId("mockWorkspaceId");

        // 创建模拟响应
        RagFlowRetrieveKnowledgeBaseResp ragFlowRetrieveKnowledgeBaseResp = new RagFlowRetrieveKnowledgeBaseResp();
        ragFlowRetrieveKnowledgeBaseResp.setEmbeddingModel("mockEmbeddingModel");
        ragFlowRetrieveKnowledgeBaseResp.setRerankModel("mockRerankModel");
        ragFlowRetrieveKnowledgeBaseResp.setParserId("naive");
        ragFlowRetrieveKnowledgeBaseResp.setParserConfig(
            ParserConfig.builder().chunkTokenNum(10).delimiter(null).build());
        RagFlowApiResponse<RagFlowRetrieveKnowledgeBaseResp> ragFlowApiResponse = new RagFlowApiResponse<>();
        ragFlowApiResponse.setCode(200);
        ragFlowApiResponse.setData(ragFlowRetrieveKnowledgeBaseResp);

        try (MockedStatic<RequestContextUtils> requestContextUtilsMockedStatic = mockStatic(
            RequestContextUtils.class)) {
            // 模拟依赖项
            requestContextUtilsMockedStatic.when(RequestContextUtils::getRequestProjectId).thenReturn("mockProjectId");
            when(agentBaseRagConnectionProvider.getConnectionIdByRepoId(any())).thenReturn("mockConnectionId");
            when(agentBaseRagClient.retrieveKnowledgeRepo(any(), any(), any(), any(), any())).thenReturn(
                ragFlowApiResponse);

            // 模拟 RceInvocation
            AgentBaseRagConnection agentBaseRagConnection = new AgentBaseRagConnection();
            agentBaseRagConnection.setEndpoint("https://1.1.1.1");
            agentBaseRagConnection.setConnectionId("mockConnectionId");
            when(agentBaseRagConnectionProvider.getKnowledgeSourceConnection(any())).thenReturn(agentBaseRagConnection);

            // 执行测试方法
            KnowledgeRepo retrievedKnowledgeRepo = agentBaseRagService.retrieveKnowledgeRepo(mockKnowledgeRepo);

            // 验证结果
            Assertions.assertNotNull(retrievedKnowledgeRepo);
            Assertions.assertEquals("mockEmbeddingModel", retrievedKnowledgeRepo.getEmbeddingModel());
            Assertions.assertEquals("mockRerankModel", retrievedKnowledgeRepo.getRerankModel());
            Assertions.assertNotNull(retrievedKnowledgeRepo.getRagChunkParserConf());
        }
    }

    @Test
    public void test_uploadFile_with_exception() throws Exception {
        KnowledgeRepo knowledgeRepo = mock(KnowledgeRepo.class);
        MultipartFile resource = mock(MultipartFile.class);
        AgentBaseException agentBaseException = mock(AgentBaseException.class);
        when(agentBaseRagClient.uploadFile(any(), any(), anyString(), anyString(), any())).thenThrow(
            agentBaseException);

        when(knowledgeRepo.getKnowledgeRepoId()).thenReturn("repoId");
        when(agentBaseRagConnectionProvider.getConnectionIdByRepoId(eq("repoId"))).thenReturn("connectionId");

        AgentBaseRagConnection agentBaseRagConnection = new AgentBaseRagConnection();
        agentBaseRagConnection.setEndpoint("https://1.1.1.1");
        agentBaseRagConnection.setConnectionId("mockConnectionId");
        when(agentBaseRagConnectionProvider.getKnowledgeSourceConnection(any())).thenReturn(agentBaseRagConnection);

        try (MockedStatic<RequestContextUtils> mockedRequestContextUtils = mockStatic(RequestContextUtils.class)) {
            mockedRequestContextUtils.when(RequestContextUtils::getRequestProjectId).thenReturn("projectId");

            assertThrows(AgentBaseException.class, () -> agentBaseRagService.uploadFile(knowledgeRepo, null, null));
        }
    }

    @Test
    void listKnowledgeTaskTest() {
        Assertions.assertThrowsExactly(AgentBaseException.class,
            () -> agentBaseRagService.listKnowledgeTask(null, null));
    }

    @Test
    void deleteKnowledgeTaskTest() {
        Assertions.assertThrowsExactly(AgentBaseException.class,
            () -> agentBaseRagService.deleteKnowledgeTask(null, null));
    }

    @Test
    void uploadFaqFileTest() {
        Assertions.assertThrowsExactly(AgentBaseException.class, () -> agentBaseRagService.uploadFaqFile(null, null));
    }

    @Test
    void deleteFaqFileTest() {
        Assertions.assertThrowsExactly(AgentBaseException.class, () -> agentBaseRagService.deleteFaqFile(null, null));
    }

    @Test
    void listFaqFilesTest() {
        Assertions.assertThrowsExactly(AgentBaseException.class, () -> agentBaseRagService.listFaqFiles(null, null));
    }

    @Test
    void downloadFaqFileTest() {
        Assertions.assertThrowsExactly(AgentBaseException.class, () -> agentBaseRagService.downloadFaqFile(null, null));
    }

    @Test
    void createFaqFileChunkTest() {
        Assertions.assertThrowsExactly(AgentBaseException.class,
            () -> agentBaseRagService.createFaqFileChunk(null, null, null));
    }

    @Test
    void updateFaqFileChunkTest() {
        Assertions.assertThrowsExactly(AgentBaseException.class,
            () -> agentBaseRagService.updateFaqFileChunk(null, null, null, null));
    }

    @Test
    void deleteFaqFileChunkTest() {
        Assertions.assertThrowsExactly(AgentBaseException.class,
            () -> agentBaseRagService.deleteFaqFileChunk(null, null, null));
    }

    @Test
    void listFaqFileChunksTest() {
        Assertions.assertThrowsExactly(AgentBaseException.class,
            () -> agentBaseRagService.listFaqFileChunks(null, null));
    }

    @Test
    void listKnowledgeRepoTagsTest() {
        Assertions.assertThrowsExactly(AgentBaseException.class,
            () -> agentBaseRagService.listKnowledgeRepoTags(null, 1, 1));
    }

    @Test
    void queryImageTest() {
        Assertions.assertThrowsExactly(AgentBaseException.class, () -> agentBaseRagService.queryImage(null, null));
    }
}