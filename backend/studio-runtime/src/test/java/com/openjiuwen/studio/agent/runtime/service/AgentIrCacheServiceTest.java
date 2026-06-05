/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service;

import static com.openjiuwen.studio.agent.runtime.constant.ConstantTest.IR_MD5;
import static com.openjiuwen.studio.agent.runtime.constant.ConstantTest.TEST_AGENT_ID;
import static com.openjiuwen.studio.agent.runtime.constant.ConstantTest.TEST_PROJECT_ID;
import static com.openjiuwen.studio.agent.runtime.constant.ConstantTest.TEST_UPDATED_TIME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.runtime.constant.Constant;
import com.openjiuwen.studio.agent.runtime.enums.AgentIrType;
import com.openjiuwen.studio.agent.runtime.model.AgentIrMetadata;
import com.openjiuwen.studio.agent.runtime.model.AgentIrWrapper;
import com.openjiuwen.studio.agent.runtime.utils.BaseTest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * AgentIrCacheService测试类
 *
 */
@MockitoSettings(strictness = Strictness.LENIENT)
class AgentIrCacheServiceTest extends BaseTest {
    @MockitoBean
    RedissonClient redissonClientMock;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ObsService obsService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private LoadingCache<String, AgentIrWrapper> agentIrCache;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private LoadingCache<String, AgentIrWrapper> publishedAgentIrCache;

    @Autowired
    private AgentIrCacheService agentIrCacheService;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(agentIrCacheService, "irObsPath", "string");
        ReflectionTestUtils.setField(agentIrCacheService, "obsService", obsService);
        ReflectionTestUtils.setField(agentIrCacheService, "agentIrCache", agentIrCache);
        ReflectionTestUtils.setField(agentIrCacheService, "publishedAgentIrCache", publishedAgentIrCache);

        when(agentIrCache.get(anyString())).thenReturn(mockAgentIrWrapper());
        when(obsService.getMd5(anyString(), anyString())).thenReturn(IR_MD5);
        when(obsService.getObject(anyString(), anyString())).thenReturn(mockObsMap());
        doNothing().when(agentIrCache).invalidate(anyString());
        when(publishedAgentIrCache.get(anyString())).thenReturn(mockAgentIrWrapper());
        doNothing().when(publishedAgentIrCache).invalidate(anyString());
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_getLatestAgentIr_should_succeed_when_test_data_combination() {
        // run test
        AgentIrWrapper latestAgentIr = agentIrCacheService.getLatestAgentIr(TEST_AGENT_ID);

        // verify result
        assertEquals(IR_MD5, latestAgentIr.getMd5());
    }

    @Test
    void test_getLatestAgentIr_should_succeed_when_id_need_refresh() {
        // set up
        when(obsService.getMd5(anyString(), anyString())).thenReturn("test_id_md5_change");

        // run test
        AgentIrWrapper latestAgentIr = agentIrCacheService.getLatestAgentIr(TEST_AGENT_ID);

        // verify result
        assertEquals(IR_MD5, latestAgentIr.getMd5());
    }

    @Test
    void test_getLatestAgentIr_should_throw_AgentRuntimeException_when_agentIr_is_null() {
        Assertions.assertThrows(AgentStudioException.class, () -> {
            // set up
            when(agentIrCache.get(anyString())).thenReturn(null);

            // run test
            agentIrCacheService.getLatestAgentIr(TEST_AGENT_ID);
        });
    }

    @Test
    void test_getPublishedAgentIr_should_succeed_when_test_data_combination() {
        // run test
        AgentIrWrapper publishedAgentIr = agentIrCacheService.getPublishedAgentIr(TEST_AGENT_ID,
            String.valueOf(TEST_UPDATED_TIME));

        // verify result
        assertEquals(IR_MD5, publishedAgentIr.getMd5());
    }

    @Test
    void test_getPublishedAgentIr_should_throw_AgentRuntimeException_when_agentIr_is_null() {
        Assertions.assertThrows(AgentStudioException.class, () -> {
            // set up
            when(publishedAgentIrCache.get(anyString())).thenReturn(null);

            // run test
            agentIrCacheService.getPublishedAgentIr(TEST_AGENT_ID, String.valueOf(TEST_UPDATED_TIME));
        });
    }

    @Test
    void test_getIrWrapperFromObs_should_succeed_when_test_data_combination() {
        // run test
        AgentIrWrapper latestAgentIr = agentIrCacheService.getIrWrapperFromObs(TEST_AGENT_ID);

        // verify result
        assertEquals(IR_MD5, latestAgentIr.getMd5());
        assertEquals(AgentIrType.AGENT, latestAgentIr.getIrType());
    }

    @Test
    void test_getIrWrapperFromObs_should_succeed_when_ir_not_exist() {
        // set up
        when(obsService.getObject(anyString(), anyString())).thenReturn(new HashMap<>());

        // run test
        AgentIrWrapper latestAgentIr = agentIrCacheService.getIrWrapperFromObs(TEST_AGENT_ID);

        // verify result
        assertNull(latestAgentIr);
    }

    @Test
    void test_getPublishedIrWrapperFromObs_should_succeed_when_test_data_combination() {
        // run test
        AgentIrWrapper publishedIrWrapper = agentIrCacheService.getPublishedIrWrapperFromObs(
            TEST_AGENT_ID + "_" + TEST_UPDATED_TIME);

        // verify result
        assertEquals(IR_MD5, publishedIrWrapper.getMd5());
    }

    private AgentIrWrapper mockAgentIrWrapper() {
        AgentIrWrapper agentIr = new AgentIrWrapper();
        agentIr.setMd5(IR_MD5);
        AgentIrMetadata irMetadata = new AgentIrMetadata();
        irMetadata.setProjectId(TEST_PROJECT_ID);
        irMetadata.setUpdatedAt(TEST_UPDATED_TIME);
        agentIr.setMetadata(irMetadata);
        return agentIr;
    }

    private Map<String, String> mockObsMap() {
        Map<String, String> obsMap = new HashMap<>();
        obsMap.put(Constant.Obs.CONTENT,
            "{\"configs\":{\"mode\":\"ReAct\",\"sysPromptTemplate\":\"\",\"modelConfig\":{\"modelName\":\"f1593ad5-2255-46ee-b715-219c759a64c9\",\"hyperParameters\":{\"top_p\":1.0,\"temperature\":0.0},\"modelType\":\"ei_agentBuilder\"},\"plugins\":[],\"maxIteration\":5,\"workflows\":[]},\"agentId\":\"717aacfa-2d3f-4b66-9d9d-90fb9c6101d7\",\"metadata\":{\"memoryVariables\":null,\"projectId\":\"0ba805c3edf64df48732c5bf47c40e70\",\"triggerConfigs\":[{\"created_on\":null,\"cron\":null,\"hook_url\":null,\"name\":\"事件触发器\",\"params\":[{\"description\":\"地点\",\"name\":\"location\"}],\"prompt\":\"打车到{{location}}\",\"trigger_id\":\"test_id\",\"type\":\"EVENT\"},{\"created_on\":\"2024-12-25 14:09:49\",\"cron\":null,\"hook_url\":\"test_yrl\",\"name\":\"事件触发器\",\"params\":[{\"description\":\"地点\",\"name\":\"location\"}],\"prompt\":\"打车到{{location}}\",\"trigger_id\":\"test_id\",\"type\":\"EVENT\"},{\"created_on\":\"2024-12-28 15:59:38\",\"cron\":null,\"hook_url\":\"test_url\",\"name\":\"事件触发器\",\"params\":[{\"description\":\"地点\",\"name\":\"location\"}],\"prompt\":\"打车到{{location}}\",\"trigger_id\":\"test_id\",\"type\":\"EVENT\"},{\"created_on\":\"2024-12-30 11:46:17\",\"cron\":null,\"hook_url\":\"test_url\",\"name\":\"事件触发器\",\"params\":[{\"description\":\"地点\",\"name\":\"location\"}],\"prompt\":\"打车到{{location}}\",\"trigger_id\":\"test_id\",\"type\":\"EVENT\"}],\"updatedAt\":\"2024-12-30 11:46:18\"},\"schemaVersion\":\"0.6.0\",\"agentName\":\"xxq\",\"description\":\"test\",\"agentVersion\":\"0.6.0\"}");
        obsMap.put(Constant.Obs.MD5, IR_MD5);
        return obsMap;
    }

    @SuppressWarnings("unchecked")
    @Test
    public void getIrWorkspaceFromCacheTest() {
        AgentIrCacheService service = new AgentIrCacheService();

        LoadingCache<String, AgentIrWrapper> agentIrCache = Mockito.mock(LoadingCache.class);

        LoadingCache<String, AgentIrWrapper> publishedAgentIrCache = Mockito.mock(LoadingCache.class);

        ReflectionTestUtils.setField(service, "agentIrCache", agentIrCache);

        ReflectionTestUtils.setField(service, "publishedAgentIrCache", publishedAgentIrCache);

        AgentIrMetadata metadata = new AgentIrMetadata();
        metadata.setWorkspaceId("1");
        AgentIrWrapper wrapper = new AgentIrWrapper();
        wrapper.setMetadata(metadata);

        Mockito.when(agentIrCache.getIfPresent("exist")).thenReturn(wrapper);
        Mockito.when(agentIrCache.getIfPresent("not_exist_1")).thenReturn(null);
        String id = service.getIrWorkspaceFromCache("not_exist", "1");
        assertNull(id);

        id = service.getIrWorkspaceFromCache("exist", null);
        assertEquals("1", id);
    }

    private Map<String, String> mockDeepResearchObsMap() {
        Map<String, String> obsMap = new HashMap<>();
        obsMap.put(Constant.Obs.CONTENT,
                "{\"configs\":{\"deep_search_execute_mode\":\"commercial\",\"workflow_human_in_the_loop\":true,\"outliner_max_section_num\":5,\"source_tracer_research_trace_source_switch\":true,\"llm_config\":{\"model_name\":\"180\",\"model_type\":\"openai\",\"api_key\":\"\",\"base_url\":\"\",\"hyperParameters\":{\"top_p\":1.0,\"frequency_penalty\":0.0,\"max_tokens\":12800,\"temperature\":0.0},\"extension\":{}},\"info_collector_search_method\":\"web\",\"web_search_engine_config\":{\"search_engine_name\":\"petal\",\"search_api_key\":\"test\",\"search_url\":\"test\",\"max_web_search_results\":5,\"extension\":{}},\"local_search_engine_config\":{\"search_engine_name\":\"test\",\"search_api_key\":\"\",\"search_url\":\"\",\"search_datasets\":[],\"max_local_search_results\":5,\"similarity_threshold\":0.5,\"extension\":{}},\"custom_web_search_config\":{\"custom_web_search_file\":\"\",\"custom_web_search_func\":\"\",\"extension\":{}},\"custom_local_search_config\":{\"custom_local_search_file\":\"\",\"custom_local_search_func\":\"\",\"extension\":{}},\"has_template\":false,\"is_template\":false},\"agentId\":\"deepresearch_ir_petal_template\",\"metadata\":{\"memoryVariables\":null,\"projectId\":\"test\",\"triggerConfigs\":null,\"workspaceId\":\"default\",\"updatedAt\":\"2025-11-04 12:09:15\"},\"schemaVersion\":\"0.6.0\",\"agentName\":\"九问DeepSearch助手\",\"description\":\"九问DeepSearch描述\",\"agentVersion\":\"0.6.0\",\"template_path\":\"\"}");
        obsMap.put(Constant.Obs.MD5, IR_MD5);
        return obsMap;
    }

    @Test
    public void test_getIrWrapperFromObsMap_should_succeed_with_DeepResearchType() {
        // set up
        when(obsService.getObject(anyString(), anyString())).thenReturn(mockDeepResearchObsMap());

        // run test
        AgentIrWrapper latestAgentIr = agentIrCacheService.getIrWrapperFromObs(TEST_AGENT_ID);

        // verify result
        assertEquals(IR_MD5, latestAgentIr.getMd5());
        assertEquals(AgentIrType.DEEP_RESEARCH, latestAgentIr.getIrType());
    }

    @Test
    public void test_parseIrType_WorkflowType() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // Create workflow JSON
        String workflowJson = "{\"workflowId\":\"test-workflow-id\"}";
        JSONObject ir = JSON.parseObject(workflowJson);

        // Since parseIrType is private, we need to test via reflection
        java.lang.reflect.Method method = AgentIrCacheService.class.getDeclaredMethod("parseIrType", JSONObject.class);
        method.setAccessible(true);
        Object result = method.invoke(agentIrCacheService, ir);

        assertEquals(AgentIrType.WORKFLOW, result);
    }

    @Test
    public void test_parseIrType_MultiAgentsType() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // Create multi-agents JSON
        String multiAgentsJson = "{\"agentId\":\"test-agent-id\",\"configs\":{\"agents\":[{\"id\":\"agent1\"},{\"id\":\"agent2\"}],\"deep_search_execute_mode\":\"commercial\"}}";
        JSONObject ir = JSON.parseObject(multiAgentsJson);

        java.lang.reflect.Method method = AgentIrCacheService.class.getDeclaredMethod("parseIrType", JSONObject.class);
        method.setAccessible(true);
        Object result = method.invoke(agentIrCacheService, ir);

        assertEquals(AgentIrType.MULTI_AGENTS, result);
    }

    @Test
    public void test_parseIrType_DeepResearchType() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // Create deep research JSON
        String deepResearchJson = "{\"agentId\":\"test-agent-id\",\"configs\":{\"deep_search_execute_mode\":\"commercial\"}}";
        JSONObject ir = JSON.parseObject(deepResearchJson);

        java.lang.reflect.Method method = AgentIrCacheService.class.getDeclaredMethod("parseIrType", JSONObject.class);
        method.setAccessible(true);
        Object result = method.invoke(agentIrCacheService, ir);

        assertEquals(AgentIrType.DEEP_RESEARCH, result);
    }

    @Test
    public void test_parseIrType_AgentType() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // Create single agent JSON
        String agentJson = "{\"agentId\":\"test-agent-id\",\"configs\":{}}";
        JSONObject ir = JSON.parseObject(agentJson);

        java.lang.reflect.Method method = AgentIrCacheService.class.getDeclaredMethod("parseIrType", JSONObject.class);
        method.setAccessible(true);
        Object result = method.invoke(agentIrCacheService, ir);

        assertEquals(AgentIrType.AGENT, result);
    }

    @Test
    public void test_parseIrType_UnknownType() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // Create JSON without workflowId or agentId
        String unknownJson = "{\"metadata\":{},\"schemaVersion\":\"0.6.0\"}";
        JSONObject ir = JSON.parseObject(unknownJson);

        java.lang.reflect.Method method = AgentIrCacheService.class.getDeclaredMethod("parseIrType", JSONObject.class);
        method.setAccessible(true);
        Object result = method.invoke(agentIrCacheService, ir);

        assertEquals(AgentIrType.UNKNOWN, result);
    }

    @Test
    public void testClearCache() {
        when(publishedAgentIrCache.asMap().keySet()).thenReturn(Set.of("id"));
        agentIrCacheService.clearAgentCache("id");
    }
}
