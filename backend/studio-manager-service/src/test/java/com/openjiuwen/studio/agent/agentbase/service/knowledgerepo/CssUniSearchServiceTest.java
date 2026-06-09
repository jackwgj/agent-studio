/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.knowledgerepo;

import static com.openjiuwen.studio.agent.agentbase.constant.Constant.TEST_USER_ID;
import static com.openjiuwen.studio.common.service.constant.CommonConstant.TenantType.TENANT_TYPE_2C;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.agentbase.client.CssUniSearchClient;
import com.openjiuwen.studio.agent.agentbase.common.enums.KnowledgeBaseType;
import com.openjiuwen.studio.agent.agentbase.enums.RepoTypeEnum;
import com.openjiuwen.studio.agent.agentbase.model.BatchCreateResponse;
import com.openjiuwen.studio.agent.agentbase.model.CreateKnowledgeLabelsRsp;
import com.openjiuwen.studio.agent.agentbase.model.CreateKnowledgeRepoInfo;
import com.openjiuwen.studio.agent.agentbase.model.CreateKnowledgeRepoResp;
import com.openjiuwen.studio.agent.agentbase.model.CssFileInfo;
import com.openjiuwen.studio.agent.agentbase.model.DeleteKnowledgeLabelsReq;
import com.openjiuwen.studio.agent.agentbase.model.DeleteKnowledgeLabelsRsp;
import com.openjiuwen.studio.agent.agentbase.model.DeleteKnowledgeResp;
import com.openjiuwen.studio.agent.common.dto.knowledge.FaqDeleteBatchResp;
import com.openjiuwen.studio.agent.agentbase.model.FaqResp;
import com.openjiuwen.studio.agent.common.dto.knowledge.FaqSearchCriteria;
import com.openjiuwen.studio.agent.common.dto.knowledge.FileDocInfo;
import com.openjiuwen.studio.agent.agentbase.model.KnowledgeRepo;
import com.openjiuwen.studio.agent.common.dto.knowledge.KnowledgeRepoInfo;
import com.openjiuwen.studio.agent.agentbase.model.KnowledgeSegRuleInfo;
import com.openjiuwen.studio.agent.agentbase.model.ListFaqResponse;
import com.openjiuwen.studio.agent.agentbase.model.ListFileDocsRsp;
import com.openjiuwen.studio.agent.agentbase.model.ListFilesRsp;
import com.openjiuwen.studio.agent.agentbase.model.ListKnowledgeLabelInfo;
import com.openjiuwen.studio.agent.agentbase.model.ListKnowledgeLabelsResp;
import com.openjiuwen.studio.agent.agentbase.model.ListKnowledgeRepoResp;
import com.openjiuwen.studio.agent.agentbase.model.ListTaskCriteria;
import com.openjiuwen.studio.agent.agentbase.model.ModelSearchCriteria;
import com.openjiuwen.studio.agent.agentbase.model.SearchTextResp;
import com.openjiuwen.studio.agent.agentbase.model.SegmentRule;
import com.openjiuwen.studio.agent.agentbase.model.obs.RequestContextTaskDecorator;
import com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.connection.KooSearchConnection;
import com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.knowledgesourceprovider.KooSearchConnectionProvider;
import com.openjiuwen.studio.agent.agentbase.utils.TenantTypeUtil;
import com.openjiuwen.studio.agent.common.dto.knowledge.FaqInfo;
import com.openjiuwen.studio.agent.common.dto.knowledge.KnowledgeFaq;
import com.openjiuwen.studio.agent.common.dto.knowledge.ListModelResp;
import com.openjiuwen.studio.agent.common.utils.CryptoUtils;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;
import com.openjiuwen.studio.agent.foundation.base.exception.ErrorCode;
import com.openjiuwen.studio.agent.foundation.base.mapping.ErrorCodeMappingService;
import com.openjiuwen.studio.agent.manager.dto.BatchDeleteFaqResp;
import com.openjiuwen.studio.agent.manager.dto.BatchDeleteKnowledgeFilesResponseBody;
import com.openjiuwen.studio.agent.manager.dto.CommonBatchDeleteRsp;
import com.openjiuwen.studio.agent.manager.dto.CreateKnowledgeRepoTagsReq;
import com.openjiuwen.studio.agent.manager.dto.CreateKnowledgeRepoTagsRsp;
import com.openjiuwen.studio.agent.manager.dto.CreateKnowledgeTaskResponseBody;
import com.openjiuwen.studio.agent.manager.dto.DeleteKnowledgeRepoTagsReq;
import com.openjiuwen.studio.agent.manager.dto.DeleteKnowledgeRepoTagsRsp;
import com.openjiuwen.studio.agent.manager.dto.FaqFileChunkReq;
import com.openjiuwen.studio.agent.manager.dto.FaqFileInfoListRsp;
import com.openjiuwen.studio.agent.manager.dto.FileChunkListRsp;
import com.openjiuwen.studio.agent.manager.dto.FileChunkReq;
import com.openjiuwen.studio.agent.manager.dto.KnowledgeSegmentRule;
import com.openjiuwen.studio.agent.manager.dto.KnowledgeTagInfo;
import com.openjiuwen.studio.agent.manager.dto.ListFaqFileChunksReq;
import com.openjiuwen.studio.agent.manager.dto.ListFaqFileReq;
import com.openjiuwen.studio.agent.manager.dto.ListFaqResp;
import com.openjiuwen.studio.agent.manager.dto.ListFileChunksReq;
import com.openjiuwen.studio.agent.manager.dto.ListFileReq;
import com.openjiuwen.studio.agent.manager.dto.ListKnowledgeFilesResponseBody;
import com.openjiuwen.studio.agent.manager.dto.ListKnowledgeRepoReq;
import com.openjiuwen.studio.agent.manager.dto.ListKnowledgeTagsResp;
import com.openjiuwen.studio.agent.manager.dto.ListKnowledgeTasksResponseBody;
import com.openjiuwen.studio.agent.manager.dto.ModelInfo;
import com.openjiuwen.studio.agent.manager.dto.ModifyKnowledgeRepoResponseBody;
import com.openjiuwen.studio.agent.manager.dto.UpdateFileMetaInfoReq;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

@MockitoSettings(strictness = Strictness.LENIENT)
class CssUniSearchServiceTest {

    @Mock
    private KnowledgeConnectionRouterService knowledgeConnectionRouterService;

    @Mock
    private CssUniSearchClient cssUniSearchClient;

    @Mock
    private ErrorCodeMappingService errorCodeMappingService;

    @Mock
    private KooSearchConnectionProvider kooSearchConnectionProvider;

    private CssUniSearchService cssUniSearchService;

    @BeforeEach
    void init() {
        cssUniSearchService = new CssUniSearchService(cssUniSearchClient,
            kooSearchConnectionProvider, knowledgeConnectionRouterService);
        KooSearchConnection kooSearchConnection = new KooSearchConnection();
        kooSearchConnection.setConnectionId("testConnectionId");
        kooSearchConnection.setAuthMode("none");
        kooSearchConnection.setProjectId("testProjectId");
        kooSearchConnection.setOcrEnable(true);
        kooSearchConnection.setEndpoint("https://1.0.0.1");
        kooSearchConnection.setApplicationId("testApplicationId");
        when(kooSearchConnectionProvider.getKnowledgeSourceConnection(anyString()))
            .thenReturn(kooSearchConnection);
        when(knowledgeConnectionRouterService.findConnectionIdByContext()).thenReturn("testConnectionId");
        when(kooSearchConnectionProvider.getConnectionIdByRepoId(anyString())).thenReturn("testConnectionId");
        when(kooSearchConnectionProvider.getConnectionIdBySegmentId(anyString()))
            .thenReturn("testConnectionId");
    }

    @Test
    void createKnowledgeRepoTest() {
        try (MockedStatic<RequestContextUtils> requestContextUtilsMockedStatic = mockStatic(
            RequestContextUtils.class)) {
            requestContextUtilsMockedStatic.when(TenantTypeUtil::getTenantType).thenReturn("B");
            CreateKnowledgeRepoResp createKnowledgeRepoResp = new CreateKnowledgeRepoResp("testRepoId");
            when(cssUniSearchClient.createKnowledgeRepo(any(), any(), anyString(), anyString(), any()))
                .thenReturn(createKnowledgeRepoResp);
            CreateKnowledgeRepoInfo createKnowledgeRepoInfo = cssUniSearchService.createKnowledgeRepo(
                new KnowledgeRepo().setRepoType(RepoTypeEnum.SHARE));
            Assertions.assertEquals(createKnowledgeRepoInfo.getKnowledgeBaseConnectionId(), "testConnectionId");
        }
    }

    @Test
    void testCreateKbRepoWithoutEnoughCapacity() {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS);) {
            mockedStaticRequestContextUtils.when(TenantTypeUtil::getTenantType).thenReturn(TENANT_TYPE_2C);
            when(knowledgeConnectionRouterService.findConnectionIdByContext()).thenReturn("connectionShareId");
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            executor.setCorePoolSize(2);
            executor.setMaxPoolSize(4);
            executor.setQueueCapacity(100);
            executor.setThreadNamePrefix("templateRepoMgmtExecutor-");
            executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
            executor.setTaskDecorator(new RequestContextTaskDecorator());
            executor.initialize();
            ReflectionTestUtils.setField(cssUniSearchService, "templateRepoMgmtExecutor", executor);
            AgentBaseException exception = new AgentBaseException(ErrorCode.SHARE_REPO_IS_INSUFFICIENT);
            when(errorCodeMappingService.getAgentBaseErrorCode(any())).thenReturn(ErrorCode.SHARE_REPO_IS_INSUFFICIENT);
            when(cssUniSearchClient.createKnowledgeRepo(any(),any(), any(), any(), any())).thenThrow(exception);
            Assertions.assertThrows(AgentBaseException.class, () -> cssUniSearchService.createKnowledgeRepo(
                new KnowledgeRepo().setRepoType(RepoTypeEnum.SHARE)));
        }
    }

    @Test
    void modifyKnowledgeRepoTest() {
        KnowledgeRepo knowledgeRepo = new KnowledgeRepo();
        knowledgeRepo.setKnowledgeRepoId("test-repo-id");
        knowledgeRepo.setDisplayName("test-repo");
        knowledgeRepo.setRerankModel("test-rerank");

        when(kooSearchConnectionProvider.getConnectionIdByRepoId("test-repo-id")).thenReturn("test-connectId");

        ModifyKnowledgeRepoResponseBody ModifyKnowledgeRepoResponseBody = new ModifyKnowledgeRepoResponseBody();
        ModifyKnowledgeRepoResponseBody.setKnowledgeRepoId("test-repo-id");
        when(cssUniSearchClient.modifyKnowledgeRepo(any(), any(), anyString(), anyString(), anyString(), any()))
            .thenReturn(ModifyKnowledgeRepoResponseBody);
        String result = cssUniSearchService.modifyKnowledgeRepo(knowledgeRepo);
        Assertions.assertEquals(result, "test-repo-id");
    }

    @Test
    void modifyKnowledgeRepo_WithRerankModelProvided_Test() {
        KnowledgeRepo knowledgeRepo = new KnowledgeRepo();
        knowledgeRepo.setKnowledgeRepoId("repoIdTest");
        knowledgeRepo.setDisplayName("My Test Repo");

        String connectionId = "conn-123";
        when(kooSearchConnectionProvider.getConnectionIdByRepoId("repoIdTest")).thenReturn(connectionId);

        KnowledgeRepoInfo knowledgeRepoInfo = new KnowledgeRepoInfo();
        knowledgeRepoInfo.setRerankModel("rerank");
        when(cssUniSearchClient.showKnowledgeRepo(any(), any(), anyString(), anyString(), anyString()))
            .thenReturn(knowledgeRepoInfo);

        ModifyKnowledgeRepoResponseBody ModifyKnowledgeRepoResponseBody = new ModifyKnowledgeRepoResponseBody();
        ModifyKnowledgeRepoResponseBody.setKnowledgeRepoId("updated-repo-id");
        when(cssUniSearchClient.modifyKnowledgeRepo(any(), any(), anyString(), anyString(), anyString(), any()))
            .thenReturn(ModifyKnowledgeRepoResponseBody);

        String result = cssUniSearchService.modifyKnowledgeRepo(knowledgeRepo);
        Assertions.assertEquals("updated-repo-id", result);
    }

    @Test
    void deleteKnowledgeRepoTest() {
        DeleteKnowledgeResp deleteKnowledgeResp = new DeleteKnowledgeResp(1, 1);
        when(cssUniSearchClient.deleteKnowledgeRepo(any(), any(), anyString(), anyString(), any()))
            .thenReturn(deleteKnowledgeResp);
        KnowledgeRepo knowledgeRepo = new KnowledgeRepo();
        knowledgeRepo.setKnowledgeRepoId("repoIdTest");
        cssUniSearchService.deleteKnowledgeRepo(knowledgeRepo);
    }

    @Test
    void retrieveKnowledgeRepoTest() {
        KnowledgeRepoInfo knowledgeRepoInfo = new KnowledgeRepoInfo();
        knowledgeRepoInfo.setId("repoTestId");
        when(cssUniSearchClient.showKnowledgeRepo(any(), any(), anyString(), anyString(), anyString()))
            .thenReturn(knowledgeRepoInfo);
        KnowledgeRepo knowledgeRepo = new KnowledgeRepo();
        knowledgeRepo.setKnowledgeRepoId("repoIdTest");
        KnowledgeRepo result = cssUniSearchService.retrieveKnowledgeRepo(knowledgeRepo);
        Assertions.assertEquals(result.getKnowledgeRepoId(), "repoTestId");
    }

    @Test
    void listKnowledgeReposTest() {
        ListKnowledgeRepoReq listKnowledgeRepoReq = new ListKnowledgeRepoReq();
        listKnowledgeRepoReq.setPageNum(1);
        listKnowledgeRepoReq.setPageSize(1);
        listKnowledgeRepoReq.setName("testName");
        ListKnowledgeRepoResp listKnowledgeRepoResp = new ListKnowledgeRepoResp();
        listKnowledgeRepoResp.setTotal(0L);
        when(
            cssUniSearchClient.listKnowledgeRepos(any(), any(), anyString(), anyString(), anyInt(), anyInt(), anyString(),
                anyString(), anyString(), any(), any())).thenReturn(listKnowledgeRepoResp);
        Assertions.assertEquals(listKnowledgeRepoResp.getTotal(), 0L);
    }

    @Test
    void deleteFileTest() {
        KnowledgeRepo knowledgeRepo = new KnowledgeRepo();
        knowledgeRepo.setType(KnowledgeBaseType.INTERNAL);
        knowledgeRepo.setKnowledgeRepoId("repoIdTest");
        doNothing().when(cssUniSearchClient).deleteFiles(any(), any(), anyString(), anyString(), anyString(), any());
        cssUniSearchService.deleteFile(knowledgeRepo, "fileId");
    }

    @Test
    void listFilesTest() {
        try (MockedStatic<RequestContextUtils> requestContextUtilsMockedStatic = mockStatic(
            RequestContextUtils.class)) {
            requestContextUtilsMockedStatic.when(RequestContextUtils::getRequestProjectId).thenReturn("projectId");
            KnowledgeRepo knowledgeRepo = new KnowledgeRepo();
            knowledgeRepo.setKnowledgeRepoId("repoIdTest");
            knowledgeRepo.setType(KnowledgeBaseType.INTERNAL);
            ListFileReq listFileReq = new ListFileReq();
            listFileReq.setFileName("testFileName");
            listFileReq.setPageNum(1);
            listFileReq.setPageSize(10);
            listFileReq.setFileStatus("OPEN");
            listFileReq.setFileType("xlsx");
            CssFileInfo cssFileInfo = new CssFileInfo();
            cssFileInfo.setId("fileId");
            cssFileInfo.setName("fileName");
            cssFileInfo.setCreateTime("1758184749622");
            ListFilesRsp listFilesRsp = new ListFilesRsp(1, 1, 10, List.of(cssFileInfo));
            when(
                cssUniSearchClient.searchFiles(any(), any(), anyString(), anyString(), anyString(), anyInt(), anyInt(),
                    anyString(), anyString(), anyString())).thenReturn(listFilesRsp);
            ListKnowledgeFilesResponseBody fileInfoListRsp = cssUniSearchService.listFiles(knowledgeRepo, listFileReq);
            Assertions.assertEquals(fileInfoListRsp.getFileInfoList().size(), 1);
        }
    }

    @Test
    void listFilesTest_Exception() {
        try (MockedStatic<RequestContextUtils> requestContextUtilsMockedStatic = mockStatic(
            RequestContextUtils.class)) {
            requestContextUtilsMockedStatic.when(RequestContextUtils::getRequestProjectId).thenReturn("projectId");

            // 准备测试数据
            KnowledgeRepo knowledgeRepo = new KnowledgeRepo();
            knowledgeRepo.setKnowledgeRepoId("repoIdTest");
            knowledgeRepo.setType(KnowledgeBaseType.INTERNAL);
            ListFileReq listFileReq = new ListFileReq();
            listFileReq.setFileName("testFileName");
            listFileReq.setPageNum(1);
            listFileReq.setPageSize(10);
            listFileReq.setFileStatus("OPEN");
            listFileReq.setFileType("xlsx");

            // 创建异常对象
            AgentBaseException exception = new AgentBaseException(ErrorCode.KOO_SEARCH_SERVICE_EXCEPTION);

            // 模拟异常
            Mockito.doThrow(exception)
                .when(cssUniSearchClient)
                .searchFiles(any(), any(), anyString(), anyString(), anyString(), anyInt(), anyInt(), anyString(), anyString(),
                    anyString());

            // 使用 Assertions.assertThrows 来验证抛出的异常类型
            AgentBaseException thrownException = Assertions.assertThrows(AgentBaseException.class, () -> {
                cssUniSearchService.listFiles(knowledgeRepo, listFileReq);
            });
        }
    }

    @Test
    void listFilesTest_EmptyFileList() {
        try (MockedStatic<RequestContextUtils> requestContextUtilsMockedStatic = mockStatic(
            RequestContextUtils.class)) {
            requestContextUtilsMockedStatic.when(RequestContextUtils::getRequestProjectId).thenReturn("projectId");
            KnowledgeRepo knowledgeRepo = new KnowledgeRepo();
            knowledgeRepo.setKnowledgeRepoId("repoIdTest");
            knowledgeRepo.setType(KnowledgeBaseType.INTERNAL);
            ListFileReq listFileReq = new ListFileReq();
            listFileReq.setFileName("testFileName");
            listFileReq.setPageNum(1);
            listFileReq.setPageSize(10);
            listFileReq.setFileStatus("OPEN");
            listFileReq.setFileType("xlsx");

            // 模拟返回空文件列表
            ListFilesRsp listFilesRsp = new ListFilesRsp(0, 0, 10, List.of());
            when(
                cssUniSearchClient.searchFiles(any(), any(), anyString(), anyString(), anyString(), anyInt(), anyInt(),
                    anyString(), anyString(), anyString())).thenReturn(listFilesRsp);

            ListKnowledgeFilesResponseBody fileInfoListRsp = cssUniSearchService.listFiles(knowledgeRepo, listFileReq);
            Assertions.assertEquals(0, fileInfoListRsp.getFileInfoList().size());
        }
    }

    @Test
    void listFilesTest_FilterByFileTypeAndStatus() {
        try (MockedStatic<RequestContextUtils> requestContextUtilsMockedStatic = mockStatic(
            RequestContextUtils.class)) {
            requestContextUtilsMockedStatic.when(RequestContextUtils::getRequestProjectId).thenReturn("projectId");
            KnowledgeRepo knowledgeRepo = new KnowledgeRepo();
            knowledgeRepo.setKnowledgeRepoId("repoIdTest");
            knowledgeRepo.setType(KnowledgeBaseType.INTERNAL);
            ListFileReq listFileReq = new ListFileReq();
            listFileReq.setFileName("testFileName");
            listFileReq.setPageNum(1);
            listFileReq.setPageSize(10);
            listFileReq.setFileStatus("CLOSED");
            listFileReq.setFileType("pdf");

            CssFileInfo cssFileInfo = new CssFileInfo();
            cssFileInfo.setId("fileId");
            cssFileInfo.setName("fileName");
            cssFileInfo.setCreateTime("1758184749622");
            ListFilesRsp listFilesRsp = new ListFilesRsp(1, 1, 10, List.of(cssFileInfo));
            when(
                cssUniSearchClient.searchFiles(any(), any(), anyString(), anyString(), anyString(), anyInt(), anyInt(),
                    anyString(), anyString(), anyString())).thenReturn(listFilesRsp);

            ListKnowledgeFilesResponseBody fileInfoListRsp = cssUniSearchService.listFiles(knowledgeRepo, listFileReq);
            Assertions.assertEquals(1, fileInfoListRsp.getFileInfoList().size());
            Assertions.assertEquals("fileName", fileInfoListRsp.getFileInfoList().get(0).getFileName());
        }
    }

    @Test
    void listFilesTest_MultiPage() {
        try (MockedStatic<RequestContextUtils> requestContextUtilsMockedStatic = mockStatic(
            RequestContextUtils.class)) {
            requestContextUtilsMockedStatic.when(RequestContextUtils::getRequestProjectId).thenReturn("projectId");
            KnowledgeRepo knowledgeRepo = new KnowledgeRepo();
            knowledgeRepo.setKnowledgeRepoId("repoIdTest");
            knowledgeRepo.setType(KnowledgeBaseType.INTERNAL);
            ListFileReq listFileReq = new ListFileReq();
            listFileReq.setFileName("testFileName");
            listFileReq.setPageNum(2);
            listFileReq.setPageSize(5);
            listFileReq.setFileStatus("OPEN");
            listFileReq.setFileType("xlsx");

            CssFileInfo cssFileInfo1 = new CssFileInfo();
            cssFileInfo1.setId("fileId1");
            cssFileInfo1.setName("fileName1");
            // 修复：设置非null的时间戳
            cssFileInfo1.setCreateTime("123456789");  // 使用有效的时间戳字符串

            CssFileInfo cssFileInfo2 = new CssFileInfo();
            cssFileInfo2.setId("fileId2");
            cssFileInfo2.setName("fileName2");
            // 修复：设置非null的时间戳
            cssFileInfo2.setCreateTime("1758184749623");  // 使用有效的时间戳字符串

            ListFilesRsp listFilesRsp = new ListFilesRsp(2, 2, 5, List.of(cssFileInfo1, cssFileInfo2));
            when(
                cssUniSearchClient.searchFiles(any(), any(), anyString(), anyString(), anyString(), anyInt(), anyInt(),
                    anyString(), anyString(), anyString())).thenReturn(listFilesRsp);

            ListKnowledgeFilesResponseBody fileInfoListRsp = cssUniSearchService.listFiles(knowledgeRepo, listFileReq);
            Assertions.assertEquals(2, fileInfoListRsp.getFileInfoList().size());
        }
    }

    @Test
    void createFileChunkTest() {
        KnowledgeRepo knowledgeRepo = new KnowledgeRepo();
        knowledgeRepo.setType(KnowledgeBaseType.INTERNAL);
        knowledgeRepo.setKnowledgeRepoId("repoIdTest");
        doNothing().when(cssUniSearchClient).createFileDocs(any(), any(), anyString(), anyString(), any(), anyString(), any());
        cssUniSearchService.createFileChunk(knowledgeRepo, "fileId", new FileChunkReq());
    }

    @Test
    void updateFileChunkTest() {
        KnowledgeRepo knowledgeRepo = new KnowledgeRepo();
        knowledgeRepo.setType(KnowledgeBaseType.INTERNAL);
        knowledgeRepo.setKnowledgeRepoId("repoIdTest");
        doNothing().when(cssUniSearchClient)
            .updateFileDocs(any(), any(), anyString(), anyString(), anyString(), anyString(), any(), any());
        cssUniSearchService.updateFileChunk(knowledgeRepo, "fileId", "chunkId", new FileChunkReq());
    }

    @Test
    void deleteFileChunkTest() {
        KnowledgeRepo knowledgeRepo = new KnowledgeRepo();
        knowledgeRepo.setType(KnowledgeBaseType.INTERNAL);
        knowledgeRepo.setKnowledgeRepoId("repoIdTest");
        doNothing().when(cssUniSearchClient)
            .batchDeleteFileDocs(any(), any(), anyString(), anyString(), any(), anyString(), any());
        cssUniSearchService.deleteFileChunk(knowledgeRepo, "fileId", "chunkId");
    }

    @Test
    void listFileChunksTest() {
        KnowledgeRepo knowledgeRepo = new KnowledgeRepo();
        knowledgeRepo.setType(KnowledgeBaseType.INTERNAL);
        knowledgeRepo.setKnowledgeRepoId("repoIdTest");

        ListFileDocsRsp listFileDocsRsp = new ListFileDocsRsp();
        listFileDocsRsp.setTotal(10L);

        FileDocInfo fileDocInfo = new FileDocInfo();
        fileDocInfo.setId("fileId");
        fileDocInfo.setTitle("fileTitle");
        fileDocInfo.setContent("contentTest");
        fileDocInfo.setTimestamp("1758184749622");
        fileDocInfo.setComponentNum(1L);
        listFileDocsRsp.setDocs(List.of(fileDocInfo));
        when(cssUniSearchClient.listFileDocs(any(), any(), any(), anyString(), anyString(), anyString(), anyInt(),
            anyInt())).thenReturn(listFileDocsRsp);
        ListFileChunksReq listFileChunksReq = new ListFileChunksReq();
        listFileChunksReq.setFileId("testFileId");
        listFileChunksReq.setPageNum(1);
        listFileChunksReq.setPageSize(1);
        FileChunkListRsp fileChunkListRsp = cssUniSearchService.listFileChunks(knowledgeRepo, listFileChunksReq);
        Assertions.assertEquals(fileChunkListRsp.getCount(), 10);

    }

    @Test
    void searchTextTest() {
        KnowledgeRepo knowledgeRepo = new KnowledgeRepo();
        knowledgeRepo.setType(KnowledgeBaseType.INTERNAL);
        knowledgeRepo.setKnowledgeRepoId("repoIdTest");
        List<KnowledgeRepo> knowledgeRepos = List.of(knowledgeRepo);
        SearchTextResp searchTextResp = new SearchTextResp(1, List.of());
        when(cssUniSearchClient.searchText(any(), any(), anyString(), anyString(), any())).thenReturn(searchTextResp);
        SearchTextResp searchTextRespRes = cssUniSearchService.searchText(knowledgeRepos, "testText", "testMode", null,
            1, 10);
        Assertions.assertEquals(searchTextRespRes.getTotal(), 1);
    }

    @Test
    void createFaqTest() {
        KnowledgeFaq knowledgeFaq = new KnowledgeFaq();
        knowledgeFaq.setFaqId("testFaqId");
        knowledgeFaq.setKnowledgeRepoId("repoIdTest");
        FaqResp faqResp = new FaqResp();
        faqResp.setFaqId("faqId");
        when(cssUniSearchClient.createFaq(any(), any(), anyString(), anyString(), any())).thenReturn(faqResp);
        String result = cssUniSearchService.createFaq(knowledgeFaq);
        Assertions.assertEquals(result, "faqId");
    }

    @Test
    void deleteFaqTest() {

        doNothing().when(cssUniSearchClient).deleteFaq(any(), any(), anyString(), anyString(), anyString(), any());

        cssUniSearchService.deleteFaq("repoIdTest", "faqIdTest");

    }

    @Test
    void deleteFaqBatchTest() {
        FaqDeleteBatchResp faqDeleteBatchResp = new FaqDeleteBatchResp();
        faqDeleteBatchResp.setDeletedCount(1);
        when(cssUniSearchClient.deleteFaqBatch(any(), any(), anyString(), anyString(), any())).thenReturn(faqDeleteBatchResp);
        Integer number = cssUniSearchService.deleteFaqBatch("repoIdTest", null);
        Assertions.assertEquals(number, 1);
    }

    @Test
    void modifyFaqTest() {
        KnowledgeFaq knowledgeFaq = new KnowledgeFaq();
        knowledgeFaq.setFaqId("testFaqId");
        knowledgeFaq.setKnowledgeRepoId("repoIdTest");
        doNothing().when(cssUniSearchClient).updateFaq(any(), any(), anyString(), anyString(), any());
        cssUniSearchService.modifyFaq(knowledgeFaq);
    }

    @Test
    void retrieveFaqTest() {
        FaqInfo faqInfo = new FaqInfo();
        when(cssUniSearchClient.showFaq(any(), any(), anyString(), any(), anyString(), any())).thenReturn(faqInfo);
        cssUniSearchService.retrieveFaq("testFaqId", "repoIdTest");
    }

    @Test
    void listFaqTest() {
        FaqSearchCriteria faqSearchCriteria = new FaqSearchCriteria();

        faqSearchCriteria.setRepoId("testRepoId");
        faqSearchCriteria.setPageNum(1);
        faqSearchCriteria.setPageSize(4);
        faqSearchCriteria.setQuestion("Quest1");
        ListFaqResponse listFaqResponse = new ListFaqResponse();
        listFaqResponse.setTotal(10);
        FaqInfo faqInfo1 = new FaqInfo();
        faqInfo1.setCreateTime("123456789");
        faqInfo1.setUpdateTime("123456789");
        listFaqResponse.setRecords(List.of(faqInfo1));
        when(cssUniSearchClient.listFaq(any(), any(), anyString(), anyString(), anyString(), anyInt(), anyInt(),
            anyString())).thenReturn(listFaqResponse);
        ListFaqResp listFaqResp = cssUniSearchService.listFaq(faqSearchCriteria);
        Assertions.assertEquals(listFaqResp.getCount(), 10);

    }

    @Test
    void startKnowledgeRepoTest() {
        KnowledgeRepo knowledgeRepo = new KnowledgeRepo();
        knowledgeRepo.setKnowledgeRepoId("repoIdTest");
        knowledgeRepo.setType(KnowledgeBaseType.INTERNAL);
        doNothing().when(cssUniSearchClient).startKnowledgeRepo(any(), any(), anyString(), anyString(), any());
        cssUniSearchService.startKnowledgeRepo(knowledgeRepo);
    }

    @Test
    void startKnowledgeRepoWithTokenTest() {
        when(kooSearchConnectionProvider.getKooSearchAuthMode(any())).thenReturn("token");
        KnowledgeRepo knowledgeRepo = new KnowledgeRepo();
        knowledgeRepo.setKnowledgeRepoId("repoIdTest");
        knowledgeRepo.setType(KnowledgeBaseType.INTERNAL);
        doNothing().when(cssUniSearchClient).startKnowledgeRepo(any(), any(), any(), any(), any());
        assertDoesNotThrow(() -> cssUniSearchService.startKnowledgeRepo(knowledgeRepo));
    }



    @Test
    void startKnowledgeRepoWithAppCodeTest() {
        try (MockedStatic<CryptoUtils> CryptoUtilsMockedStatic = mockStatic(CryptoUtils.class)) {
            CryptoUtilsMockedStatic.when(() -> CryptoUtils.decrypt(anyString())).thenReturn("decryptedValue");
            when(kooSearchConnectionProvider.getKooSearchAuthMode(any())).thenReturn("app_code");
            KnowledgeRepo knowledgeRepo = new KnowledgeRepo();
            knowledgeRepo.setKnowledgeRepoId("repoIdTest");
            knowledgeRepo.setType(KnowledgeBaseType.INTERNAL);
            doNothing().when(cssUniSearchClient).startKnowledgeRepo(any(), any(), any(), any(), any());
            assertDoesNotThrow(() -> cssUniSearchService.startKnowledgeRepo(knowledgeRepo));
        }
    }

    @Test
    void stopKnowledgeRepoTest() {
        KnowledgeRepo knowledgeRepo = new KnowledgeRepo();
        knowledgeRepo.setKnowledgeRepoId("repoIdTest");
        knowledgeRepo.setType(KnowledgeBaseType.INTERNAL);
        doNothing().when(cssUniSearchClient).stopKnowledgeRepo(any(), any(), any(), any(), any());
        cssUniSearchService.startKnowledgeRepo(knowledgeRepo);
    }

    @Test
    void createSegmentRuleTest() {
        KnowledgeSegmentRule knowledgeSegmentRule = new KnowledgeSegmentRule();
        knowledgeSegmentRule.setRuleRegexs(List.of("*"));
        SegmentRule segmentRule = new SegmentRule();
        segmentRule.setId("segId");
        when(cssUniSearchClient.createSegmentRule(any(), any(), anyString(), anyString(), any())).thenReturn(segmentRule);
        KnowledgeSegRuleInfo knowledgeSegRuleInfo = cssUniSearchService.createSegmentRule(knowledgeSegmentRule, null);
        Assertions.assertEquals(knowledgeSegRuleInfo.getRuleId(), segmentRule.getId());
    }

    @Test
    void modifySegmentRuleTest() {
        KnowledgeSegmentRule knowledgeSegmentRule = new KnowledgeSegmentRule();
        knowledgeSegmentRule.setRuleRegexs(List.of("*"));
        knowledgeSegmentRule.setId("segId");
        SegmentRule segmentRule = new SegmentRule();
        segmentRule.setId("segId");
        when(cssUniSearchClient.modifySegmentRule(any(), any(), anyString(), anyString(), anyString(), any())).thenReturn(
            segmentRule);
        String ruleId = cssUniSearchService.modifySegmentRule(knowledgeSegmentRule);
        Assertions.assertEquals(ruleId, segmentRule.getId());
    }

    @Test
    void listModelsTest() {
        ListModelResp listModelResp = new ListModelResp();
        ModelSearchCriteria searchCriteria = new ModelSearchCriteria();
        when(cssUniSearchClient.listModels(any(), any(), anyString(), anyString(), anyString(), anyString(), anyString(),
            anyInt(), anyInt())).thenReturn(listModelResp);
        searchCriteria.setModelName("modelName");
        searchCriteria.setModelType("modelType");
        searchCriteria.setModelStatus("modelStatus");
        searchCriteria.setPageNum(1);
        searchCriteria.setPageSize(10);
        List<ModelInfo> modelInfos = cssUniSearchService.listModels(searchCriteria, "connId");
        Assertions.assertEquals(modelInfos.size(), 0);
    }

    @Test
    void createTaskTest() {
        BatchCreateResponse batchCreateResponse = new BatchCreateResponse();
        batchCreateResponse.setTotalCount(100);
        batchCreateResponse.setCreatedCount(80);
        when(cssUniSearchClient.createTask(any(), any(), anyString(), anyString(), anyString(), any())).thenReturn(
            batchCreateResponse);
        CreateKnowledgeTaskResponseBody createKnowledgeTaskResp = cssUniSearchService.createTask("repoId", "taskType",
            List.of(""));
        Assertions.assertEquals(createKnowledgeTaskResp.getCreatedCount(), 80);
    }

    @Test
    void listKnowledgeTaskTest() {

        ListKnowledgeTasksResponseBody listKnowledgeTaskResp = new ListKnowledgeTasksResponseBody();
        listKnowledgeTaskResp.setFailCount(1);
        when(cssUniSearchClient.listKnowledgeTask(any(), any(), anyString(), anyString(), anyString(), anyInt(), anyInt(),
            anyString(), anyString(), anyString())).thenReturn(listKnowledgeTaskResp);
        ListTaskCriteria listTaskCriteria = new ListTaskCriteria();
        listTaskCriteria.setPageNum(1);
        listTaskCriteria.setPageSize(10);
        listTaskCriteria.setTaskType("taskType");
        listTaskCriteria.setFileName("fileName");
        listTaskCriteria.setTaskStatus("taskStatus");
        ListKnowledgeTasksResponseBody result = cssUniSearchService.listKnowledgeTask("repoId", listTaskCriteria);
        Assertions.assertEquals(result.getFailCount(), 1);
    }

    @Test
    void deleteKnowledgeTaskTest() {

        BatchDeleteFaqResp batchDeleteFaqResp = new BatchDeleteFaqResp();
        batchDeleteFaqResp.setDeletedCount(2);
        batchDeleteFaqResp.setTotalCount(10);
        when(cssUniSearchClient.deleteKnowledgeTask(any(), any(), anyString(), anyString(), anyString(), any())).thenReturn(
            batchDeleteFaqResp);
        CommonBatchDeleteRsp result = cssUniSearchService.deleteKnowledgeTask("repoId", List.of(""));
        Assertions.assertEquals(result.getDeletedCount(), 2);
    }

    @Test
    void deleteFaqFileTest() {
        KnowledgeRepo knowledgeRepo = new KnowledgeRepo();
        knowledgeRepo.setKnowledgeRepoId("repoId");

        doNothing().when(cssUniSearchClient).deleteFaqFiles(any(), any(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void listFaqFilesTest() {
        try (MockedStatic<RequestContextUtils> requestContextUtilsMockedStatic = mockStatic(
            RequestContextUtils.class)) {
            requestContextUtilsMockedStatic.when(RequestContextUtils::getRequestProjectId).thenReturn("projectId");
            KnowledgeRepo knowledgeRepo = new KnowledgeRepo();
            knowledgeRepo.setKnowledgeRepoId("repoId");
            knowledgeRepo.setType(KnowledgeBaseType.INTERNAL);

            ListFaqFileReq listFaqFileReq = new ListFaqFileReq();
            listFaqFileReq.setFileName("filename");
            listFaqFileReq.setPageNum(1);
            listFaqFileReq.setPageSize(3);
            listFaqFileReq.setFileStatus("OPEN");
            listFaqFileReq.setIds(List.of("1"));
            ListFilesRsp listFilesRsp = new ListFilesRsp();
            CssFileInfo cssFileInfo = new CssFileInfo();
            cssFileInfo.setId("fileId");
            cssFileInfo.setName("fileName");
            cssFileInfo.setCreateTime("1758184749622");
            listFilesRsp.setFiles(List.of(cssFileInfo));

            when(cssUniSearchClient.searchFaqFiles(any(), any(), anyString(), anyString(), anyString(), anyInt(), anyInt(),
                anyString(), anyString(), any())).thenReturn(listFilesRsp);
            FaqFileInfoListRsp fileInfoListRsp = cssUniSearchService.listFaqFiles(knowledgeRepo, listFaqFileReq);
        }
    }

    @Test
    void createFaqFileChunkTest() {
        KnowledgeRepo knowledgeRepo = new KnowledgeRepo();
        knowledgeRepo.setKnowledgeRepoId("repoId");
        knowledgeRepo.setType(KnowledgeBaseType.INTERNAL);
        doNothing().when(cssUniSearchClient)
            .createFaqFileDocs(any(), any(), anyString(), anyString(), anyString(), any(), any());
        cssUniSearchService.createFaqFileChunk(knowledgeRepo, "fileId", new FaqFileChunkReq());
    }

    @Test
    void deleteFaqFileChunkTest() {
        KnowledgeRepo knowledgeRepo = new KnowledgeRepo();
        knowledgeRepo.setKnowledgeRepoId("repoId");
        knowledgeRepo.setType(KnowledgeBaseType.INTERNAL);
        doNothing().when(cssUniSearchClient)
            .batchDeleteFaqFileDocs(any(), any(), anyString(), anyString(), any(), anyString(), any());
        cssUniSearchService.deleteFaqFileChunk(knowledgeRepo, "fileId", "chunkId");
    }

    @Test
    void updateFaqFileChunkTest() {
        KnowledgeRepo knowledgeRepo = new KnowledgeRepo();
        knowledgeRepo.setKnowledgeRepoId("repoId");
        knowledgeRepo.setType(KnowledgeBaseType.INTERNAL);
        doNothing().when(cssUniSearchClient)
            .updateFaqFileDocs(any(), any(), anyString(), anyString(), anyString(), any(), anyString(), any());
        cssUniSearchService.updateFaqFileChunk(knowledgeRepo, "fileId", "chunkId", new FaqFileChunkReq());
    }

    @Test
    void listFaqFileChunksTest() {
        KnowledgeRepo knowledgeRepo = new KnowledgeRepo();
        knowledgeRepo.setKnowledgeRepoId("repoId");
        knowledgeRepo.setType(KnowledgeBaseType.INTERNAL);

        ListFileDocsRsp listFilesRsp = new ListFileDocsRsp();
        listFilesRsp.setTotal(10L);
        listFilesRsp.setDocs(List.of());
        when(cssUniSearchClient.listFaqFileDocs(any(), any(), anyString(), anyString(), anyString(), any(), anyInt(),
            anyInt())).thenReturn(listFilesRsp);
    }

    @Test
    void listKnowledgeRepoTagsTest() {
        KnowledgeRepo knowledgeRepo = new KnowledgeRepo();
        knowledgeRepo.setKnowledgeRepoId("repoId");
        knowledgeRepo.setType(KnowledgeBaseType.INTERNAL);

        ListKnowledgeLabelsResp listKnowledgeLabelsResp = new ListKnowledgeLabelsResp();
        listKnowledgeLabelsResp.setTotal(5);
        // 创建标签列表
        List<ListKnowledgeLabelInfo> labelList = new ArrayList<>();

        // 添加具体的标签数据
        labelList.add(new ListKnowledgeLabelInfo("1", "重要", "red", "1770122537000"));
        labelList.add(new ListKnowledgeLabelInfo("2", "紧急", "red", "1770122572000"));
        labelList.add(new ListKnowledgeLabelInfo("3", "待办", "red", "1770122592000"));
        labelList.add(new ListKnowledgeLabelInfo("4", "已完成", "red", "1770122672000"));
        labelList.add(new ListKnowledgeLabelInfo("5", "进行中", "red", "1770122872000"));

        // 设置标签列表到响应对象中
        listKnowledgeLabelsResp.setLabelList(labelList);

        when(cssUniSearchClient.listLabels(any(), any(), any(), any(), any(), any(), any())).thenReturn(
            listKnowledgeLabelsResp);

        ListKnowledgeTagsResp expectedResult = new ListKnowledgeTagsResp();
        expectedResult.setCount(5);
        // 创建标签列表
        List<KnowledgeTagInfo> tagList = new ArrayList<>();

        // 添加具体的标签数据
        tagList.add(new KnowledgeTagInfo().setId("5").setName("进行中").setColor("red").setCreateTime(1770122872000L));
        tagList.add(new KnowledgeTagInfo().setId("4").setName("已完成").setColor("red").setCreateTime(1770122672000L));
        tagList.add(new KnowledgeTagInfo().setId("3").setName("待办").setColor("red").setCreateTime(1770122592000L));
        tagList.add(new KnowledgeTagInfo().setId("2").setName("紧急").setColor("red").setCreateTime(1770122572000L));
        tagList.add(new KnowledgeTagInfo().setId("1").setName("重要").setColor("red").setCreateTime(1770122537000L));
        expectedResult.setTagList(tagList);

        ListKnowledgeTagsResp result = cssUniSearchService.listKnowledgeRepoTags(knowledgeRepo, 1, 10);

        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    void listKnowledgeRepoTagsShouldException() {
        KnowledgeRepo knowledgeRepo = new KnowledgeRepo();
        knowledgeRepo.setKnowledgeRepoId("repoId");
        knowledgeRepo.setType(KnowledgeBaseType.INTERNAL);

        AgentBaseException exception = new AgentBaseException(ErrorCode.KOO_SEARCH_SERVICE_EXCEPTION);
        when(cssUniSearchClient.listLabels(any(), any(), any(), any(), any(), any(), any())).thenThrow(exception);

        Assertions.assertThrows(AgentBaseException.class,
            () -> cssUniSearchService.listKnowledgeRepoTags(knowledgeRepo, 1, 10));
    }

    @Test
    void batchDeleteFileTest() {
        KnowledgeRepo knowledgeRepo = new KnowledgeRepo();
        knowledgeRepo.setKnowledgeRepoId("repoId");
        knowledgeRepo.setType(KnowledgeBaseType.INTERNAL);
        BatchDeleteKnowledgeFilesResponseBody batchDeleteFileResp = new BatchDeleteKnowledgeFilesResponseBody();
        when(cssUniSearchClient.batchDeleteFiles(any(), any(), anyString(), anyString(), anyString(), any())).thenReturn(
            batchDeleteFileResp);

    }

    @Test
    void downloadFileTest() {
        KnowledgeRepo knowledgeRepo = new KnowledgeRepo();
        knowledgeRepo.setKnowledgeRepoId("repoId");
        knowledgeRepo.setType(KnowledgeBaseType.INTERNAL);
        ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(new byte[1024], HttpStatus.OK);
        when(cssUniSearchClient.downloadFiles(any(), any(), any(), any(), any(), any())).thenReturn(responseEntity);
        cssUniSearchService.downloadFile(knowledgeRepo, "fileId");
    }

    @Test
    void listFaqFileChunks() {
        KnowledgeRepo knowledgeRepo = new KnowledgeRepo();
        knowledgeRepo.setKnowledgeRepoId("repoId");
        knowledgeRepo.setType(KnowledgeBaseType.INTERNAL);

        ListFileDocsRsp listFilesRsp = new ListFileDocsRsp();
        listFilesRsp.setTotal(10L);
        listFilesRsp.setDocs(List.of());
        when(cssUniSearchClient.listFaqFileDocs(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(
            listFilesRsp);
        ListFaqFileChunksReq listFaqFileChunksReq = new ListFaqFileChunksReq();
        cssUniSearchService.listFaqFileChunks(knowledgeRepo, listFaqFileChunksReq);
    }

    @Test
    void queryImage() {
        KnowledgeRepo knowledgeRepo = new KnowledgeRepo();
        knowledgeRepo.setKnowledgeRepoId("repoId");
        knowledgeRepo.setType(KnowledgeBaseType.INTERNAL);
        byte[] bytes = new byte[1024];
        ByteArrayResource transferResource = new ByteArrayResource(bytes);
        when(cssUniSearchClient.queryImage(any(), any(), any(), any(), any(), any(), any())).thenReturn(transferResource);
        cssUniSearchService.queryImage(knowledgeRepo, "imageId");
    }

    @Test
    void downloadFaqFile() {
        try (MockedStatic<RequestContextUtils> requestContextUtilsMockedStatic = mockStatic(
            RequestContextUtils.class);
            MockedStatic<CryptoUtils> CryptoUtilsMockedStatic = mockStatic(CryptoUtils.class);) {
            requestContextUtilsMockedStatic.when(RequestContextUtils::getRequestUserId).thenReturn(TEST_USER_ID);
            CryptoUtilsMockedStatic.when(() -> CryptoUtils.decrypt(anyString())).thenReturn("decryptedValue");

            KnowledgeRepo knowledgeRepo = new KnowledgeRepo();
            knowledgeRepo.setKnowledgeRepoId("repoId");
            knowledgeRepo.setType(KnowledgeBaseType.INTERNAL);
            byte[] byteVarArray = new byte[0];
            ResponseEntity<byte[]> responseEntity = new ResponseEntity<byte[]>(byteVarArray,
                HttpStatusCode.valueOf(200));
            when(cssUniSearchClient.downloadFaqFiles(any(), any(), any(), any(), any(), any())).thenReturn(responseEntity);
            cssUniSearchService.downloadFaqFile(knowledgeRepo, "fileId");
        }
    }

    /**
     * 用例描述：正常流程
     * 输入参数：knowledgeRepo、fileId、updateFileMetaInfoReq
     * 预期结果：cssUniSearchClient.updateFileInfo(...) 被调用一次，参数正确，无异常抛出
     */
    @Test
    void test_updateFileMetaInfo_success() {
        // Arrange
        KnowledgeRepo knowledgeRepo = new KnowledgeRepo();
        knowledgeRepo.setKnowledgeRepoId("repoId");
        knowledgeRepo.setType(KnowledgeBaseType.INTERNAL);

        String fileId = "fileId";
        UpdateFileMetaInfoReq updateFileMetaInfoReq = new UpdateFileMetaInfoReq();
        List<String> tags = new ArrayList<>();
        tags.add("tag1");
        updateFileMetaInfoReq.setTags(tags);

        doNothing().when(cssUniSearchClient)
            .updateFileInfo(any(), any(), anyString(), anyString(), anyString(), anyString(), any());
        assertDoesNotThrow(() -> cssUniSearchService.updateFileMetaInfo(knowledgeRepo, fileId, updateFileMetaInfoReq));
    }

    /**
     * 用例描述：正常创建知识库标签
     * 预置条件：cssUniSearchClient返回正常结果，标签数量未超过上限，标签名称未重复
     * 输入参数：knowledgeRepoId为"repo-123"，标签名称为"tag-1"
     * 预期结果：返回CreateKnowledgeTagsRsp对象，包含tagId为"label-456"
     */
    @Test
    void test_createKnowledgeRepoTags_when_normal_then_success() {
        // Arrange
        KnowledgeRepo knowledgeRepo = new KnowledgeRepo();
        knowledgeRepo.setKnowledgeRepoId("repo-123");

        CreateKnowledgeRepoTagsReq body = new CreateKnowledgeRepoTagsReq();
        body.setName("tag-1");

        CreateKnowledgeRepoTagsRsp expectedRsp = new CreateKnowledgeRepoTagsRsp();
        expectedRsp.setTagId("label-456");

        CreateKnowledgeLabelsRsp createLabelsRsp = new CreateKnowledgeLabelsRsp();
        createLabelsRsp.setLabelId("label-456");

        ListKnowledgeLabelsResp listKnowledgeLabelsResp = new ListKnowledgeLabelsResp();
        listKnowledgeLabelsResp.setLabelList(new ArrayList<>());
        listKnowledgeLabelsResp.setTotal(0);

        when(
            cssUniSearchClient.listLabels(any(), any(), anyString(), anyString(), anyString(), anyInt(), anyInt())).thenReturn(
            listKnowledgeLabelsResp);
        when(cssUniSearchClient.createLabel(any(), any(), anyString(), anyString(), anyString(), eq(body))).thenReturn(
            createLabelsRsp);

        // Act
        CreateKnowledgeRepoTagsRsp result = cssUniSearchService.createKnowledgeRepoTags(knowledgeRepo, body);

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals("label-456", result.getTagId());
    }

    /**
     * 用例描述：标签数量超过上限
     * 预置条件：cssUniSearchClient返回正常结果，标签数量超过上限
     * 输入参数：knowledgeRepoId为"repo-123"，标签名称为"tag-1"
     * 预期结果：抛出AgentBaseException，错误码为RESOURCE_CAPACITY_LIMIT_ERROR
     */
    @Test
    void test_createKnowledgeRepoTags_when_tagLimitExceeded_then_throwException() {
        // Arrange
        KnowledgeRepo knowledgeRepo = new KnowledgeRepo();
        knowledgeRepo.setKnowledgeRepoId("repo-123");

        CreateKnowledgeRepoTagsReq body = new CreateKnowledgeRepoTagsReq();
        body.setName("tag-1");

        ListKnowledgeLabelsResp listLabelsResp = new ListKnowledgeLabelsResp();
        listLabelsResp.setTotal(100);
        listLabelsResp.setLabelList(new ArrayList<>());

        when(
            cssUniSearchClient.listLabels(any(), any(), anyString(), anyString(), anyString(), anyInt(), anyInt())).thenReturn(
            listLabelsResp);

        // Act & Assert
        AgentBaseException exception = assertThrows(AgentBaseException.class, () -> {
            cssUniSearchService.createKnowledgeRepoTags(knowledgeRepo, body);
        });

        Assertions.assertEquals(ErrorCode.RESOURCE_CAPACITY_LIMIT_ERROR, exception.getErrorCode());
    }

    /**
     * 用例描述：标签名称重复
     * 预置条件：cssUniSearchClient返回正常结果，标签名称已存在
     * 输入参数：knowledgeRepoId为"repo-23"，标签名称为"tag-11"
     * 预期结果：抛出AgentBaseException，错误码为RESOURCE_NAME_DUPLICATE
     */
    @Test
    void test_createKnowledgeRepoTags_when_tagNameDuplicate_then_throwException() {
        // Arrange
        KnowledgeRepo knowledgeRepo = new KnowledgeRepo();
        knowledgeRepo.setKnowledgeRepoId("repo-123");

        CreateKnowledgeRepoTagsReq body = new CreateKnowledgeRepoTagsReq();
        body.setName("tag-1");

        ListKnowledgeLabelsResp listLabelsResp = new ListKnowledgeLabelsResp();
        listLabelsResp.setTotal(1);
        List<ListKnowledgeLabelInfo> labelList = new ArrayList<>();
        ListKnowledgeLabelInfo existingLabel = new ListKnowledgeLabelInfo();
        existingLabel.setName("tag-1");
        labelList.add(existingLabel);
        listLabelsResp.setLabelList(labelList);

        when(
            cssUniSearchClient.listLabels(any(), any(), anyString(), anyString(), anyString(), anyInt(), anyInt())).thenReturn(
            listLabelsResp);

        // Act & Assert
        AgentBaseException exception = assertThrows(AgentBaseException.class, () -> {
            cssUniSearchService.createKnowledgeRepoTags(knowledgeRepo, body);
        });

        Assertions.assertEquals(ErrorCode.TAG_NAME_ALREADY_EXIST, exception.getErrorCode());
    }

    @Test
    void createKnowledgeRepoTagsShouldException() {
        String connectionId = "conn-123";
        when(kooSearchConnectionProvider.getConnectionIdByRepoId("repoIdTest")).thenReturn(connectionId);

        KnowledgeRepo knowledgeRepo = new KnowledgeRepo();
        knowledgeRepo.setKnowledgeRepoId("repo-123");

        CreateKnowledgeRepoTagsReq body = new CreateKnowledgeRepoTagsReq();
        body.setName("tag-1");

        ListKnowledgeLabelsResp listLabelsResp = new ListKnowledgeLabelsResp();
        listLabelsResp.setTotal(0);
        listLabelsResp.setLabelList(new ArrayList<>());

        when(cssUniSearchClient.listLabels(any(), any(), any(), any(), any(), anyInt(), anyInt())).thenReturn(listLabelsResp);

        AgentBaseException exception = new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR);
        when(cssUniSearchClient.createLabel(any(), any(), any(), any(), any(), any())).thenThrow(exception);

        Assertions.assertThrows(AgentBaseException.class,
            () -> cssUniSearchService.createKnowledgeRepoTags(knowledgeRepo, body));
    }

    /**
     * 用例描述：正常删除知识库标签
     * 预置条件：LakeSearchClient返回正常响应
     * 输入参数：知识库ID为"repo-123"，标签ID为"tag-123"，请求体包含标签名称"tech"
     * 预期结果：返回DeleteKnowledgeTagsRsp对象，包含被删除的tagId为"label-456"
     */
    @Test
    void test_deleteKnowledgeRepoTags_when_normal_then_success() {
        String connectionId = "conn-123";
        when(kooSearchConnectionProvider.getConnectionIdByRepoId("repoIdTest")).thenReturn(connectionId);

        KnowledgeRepo knowledgeRepo = new KnowledgeRepo();
        knowledgeRepo.setKnowledgeRepoId("repo-123");

        String tagId = "tag-123";
        DeleteKnowledgeRepoTagsReq deleteKnowledgeTagsReqbody = new DeleteKnowledgeRepoTagsReq();
        deleteKnowledgeTagsReqbody.setTagName("tech");
        DeleteKnowledgeLabelsReq body = DeleteKnowledgeLabelsReq.builder()
            .labelName(deleteKnowledgeTagsReqbody.getTagName())
            .build();

        DeleteKnowledgeRepoTagsRsp expectedRsp = new DeleteKnowledgeRepoTagsRsp();
        expectedRsp.setTagId("tag-456");

        DeleteKnowledgeLabelsRsp deleteKnowledgeLabelsRsp = new DeleteKnowledgeLabelsRsp();
        deleteKnowledgeLabelsRsp.setLabelId("tag-456");

        when(cssUniSearchClient.deleteLabel(any(), any(), anyString(), anyString(), anyString(), anyString(),
            eq(body))).thenReturn(deleteKnowledgeLabelsRsp);

        // Act
        DeleteKnowledgeRepoTagsRsp result = cssUniSearchService.deleteKnowledgeRepoTags(knowledgeRepo, tagId,
            deleteKnowledgeTagsReqbody);

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals("tag-456", result.getTagId());
    }

    @Test
    void deleteKnowledgeRepoTagsShouldException() {
        String connectionId = "conn-123";
        when(kooSearchConnectionProvider.getConnectionIdByRepoId("repoIdTest")).thenReturn(connectionId);

        KnowledgeRepo knowledgeRepo = new KnowledgeRepo();
        knowledgeRepo.setKnowledgeRepoId("repo-123");

        String tagId = "tag-123";
        DeleteKnowledgeRepoTagsReq deleteKnowledgeTagsReqbody = new DeleteKnowledgeRepoTagsReq();
        deleteKnowledgeTagsReqbody.setTagName("tech");
        DeleteKnowledgeLabelsReq body = DeleteKnowledgeLabelsReq.builder()
            .labelName(deleteKnowledgeTagsReqbody.getTagName())
            .build();

        AgentBaseException exception = new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR);
        when(cssUniSearchClient.deleteLabel(any(), any(), any(), any(), any(), any(), eq(body))).thenThrow(exception);

        Assertions.assertThrows(AgentBaseException.class,
            () -> cssUniSearchService.deleteKnowledgeRepoTags(knowledgeRepo, tagId, deleteKnowledgeTagsReqbody));
    }
}