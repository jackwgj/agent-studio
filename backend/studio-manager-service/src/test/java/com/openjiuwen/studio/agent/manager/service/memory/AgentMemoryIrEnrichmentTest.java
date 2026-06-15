/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service.memory;

import com.openjiuwen.studio.agent.manager.dto.AgentMemoryConfig;
import com.openjiuwen.studio.agent.manager.dto.ControllerConfigIR;
import com.openjiuwen.studio.agent.manager.dto.ControllerIR;
import com.openjiuwen.studio.agent.manager.dto.LongTermMemoryStrategy;
import com.openjiuwen.studio.agent.manager.dto.MemoryConfigIR;
import com.openjiuwen.studio.agent.manager.dto.ModifyAgentReq;
import com.openjiuwen.studio.agent.manager.entity.MemoryRepoEntity;
import com.openjiuwen.studio.agent.manager.mapper.MemoryRepoMapper;
import com.openjiuwen.studio.agent.manager.service.ControllerManagementService;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

/**
 * 验证 Controller/Agent IR 生成时，记忆配置从 DB 补全 extract_config 和 strategies。
 *
 * 对应评审项 P0-1：Agent IR 路径原先只嵌入 memoryRepoId/name/description，
 * 缺少 strategies 和 extractConfig。修改后应从 DB 查询完整 MemoryConfigIR。
 */
@MockitoSettings(strictness = Strictness.LENIENT)
class AgentMemoryIrEnrichmentTest {

    @Mock
    private MemoryRepoMapper memoryRepoMapper;

    @InjectMocks
    private ControllerManagementService controllerManagementService;

    // ========== convertControllerMemoryToIr 测试 ==========

    @Test
    void test_controller_ir_should_include_extract_config_and_strategies() throws Exception {
        // Given — DB 中有记忆库，包含提取频率和策略
        String repoId = "repo-enriched";
        List<LongTermMemoryStrategy> strategies = new ArrayList<>();
        LongTermMemoryStrategy strategy = new LongTermMemoryStrategy();
        strategy.setType(LongTermMemoryStrategy.TypeEnum.SEMANTIC_MEMORY);
        strategy.setPrompt("custom extraction prompt");
        strategies.add(strategy);

        MemoryRepoEntity entity = MemoryRepoEntity.builder()
            .id(repoId)
            .conversationRound(10)
            .timeSpan(30)
            .longTermMemoryStrategies(strategies)
            .build();
        when(memoryRepoMapper.selectById(repoId)).thenReturn(entity);

        ModifyAgentReq body = new ModifyAgentReq();
        AgentMemoryConfig memCfg = new AgentMemoryConfig();
        memCfg.setMemoryRepoId(repoId);
        body.setMemoryConfig(memCfg);

        ControllerIR ir = new ControllerIR();
        ir.setConfigs(new ControllerConfigIR());

        // When — 通过反射调用 private convertControllerMemoryToIr
        Method method = ControllerManagementService.class.getDeclaredMethod(
            "convertControllerMemoryToIr", ModifyAgentReq.class, ControllerIR.class);
        method.setAccessible(true);
        method.invoke(controllerManagementService, body, ir);

        // Then — IR 中应包含完整的 extract_config 和 strategies
        MemoryConfigIR result = ir.getConfigs().getMemory();
        assertNotNull(result);
        assertEquals(repoId, result.getMemoryRepoId());

        // extract_config
        assertNotNull(result.getExtractConfig());
        assertEquals(10, result.getExtractConfig().getMaxChatTurn());
        assertEquals(30, result.getExtractConfig().getTimeWindow());

        // strategies
        assertNotNull(result.getStrategies());
        assertEquals(1, result.getStrategies().size());
        assertEquals("custom extraction prompt", result.getStrategies().get(0).getPrompt());
    }

    @Test
    void test_controller_ir_should_work_when_repo_not_found() throws Exception {
        // Given — DB 中不存在该记忆库
        String repoId = "repo-deleted";
        when(memoryRepoMapper.selectById(repoId)).thenReturn(null);

        ModifyAgentReq body = new ModifyAgentReq();
        AgentMemoryConfig memCfg = new AgentMemoryConfig();
        memCfg.setMemoryRepoId(repoId);
        body.setMemoryConfig(memCfg);

        ControllerIR ir = new ControllerIR();
        ir.setConfigs(new ControllerConfigIR());

        // When
        Method method = ControllerManagementService.class.getDeclaredMethod(
            "convertControllerMemoryToIr", ModifyAgentReq.class, ControllerIR.class);
        method.setAccessible(true);
        method.invoke(controllerManagementService, body, ir);

        // Then — 仍设置 memoryRepoId，但无 extract_config 和 strategies
        MemoryConfigIR result = ir.getConfigs().getMemory();
        assertNotNull(result);
        assertEquals(repoId, result.getMemoryRepoId());
        assertNull(result.getExtractConfig());
        assertNull(result.getStrategies());
    }

    @Test
    void test_controller_ir_should_skip_when_memory_config_null() throws Exception {
        // Given
        ModifyAgentReq body = new ModifyAgentReq();
        body.setMemoryConfig(null);

        ControllerIR ir = new ControllerIR();
        ir.setConfigs(new ControllerConfigIR());

        // When
        Method method = ControllerManagementService.class.getDeclaredMethod(
            "convertControllerMemoryToIr", ModifyAgentReq.class, ControllerIR.class);
        method.setAccessible(true);
        method.invoke(controllerManagementService, body, ir);

        // Then — 不设置 memory
        assertNull(ir.getConfigs().getMemory());
    }

    @Test
    void test_controller_ir_should_handle_partial_extract_config() throws Exception {
        // Given — 只有 conversationRound，没有 timeSpan
        String repoId = "repo-partial";
        MemoryRepoEntity entity = MemoryRepoEntity.builder()
            .id(repoId)
            .conversationRound(5)
            .timeSpan(null)
            .longTermMemoryStrategies(new ArrayList<>())
            .build();
        when(memoryRepoMapper.selectById(repoId)).thenReturn(entity);

        ModifyAgentReq body = new ModifyAgentReq();
        AgentMemoryConfig memCfg = new AgentMemoryConfig();
        memCfg.setMemoryRepoId(repoId);
        body.setMemoryConfig(memCfg);

        ControllerIR ir = new ControllerIR();
        ir.setConfigs(new ControllerConfigIR());

        // When
        Method method = ControllerManagementService.class.getDeclaredMethod(
            "convertControllerMemoryToIr", ModifyAgentReq.class, ControllerIR.class);
        method.setAccessible(true);
        method.invoke(controllerManagementService, body, ir);

        // Then — extract_config 存在但 timeWindow 为 null
        MemoryConfigIR result = ir.getConfigs().getMemory();
        assertNotNull(result);
        assertNotNull(result.getExtractConfig());
        assertEquals(5, result.getExtractConfig().getMaxChatTurn());
        assertNull(result.getExtractConfig().getTimeWindow());
        // strategies 为空列表时不设置
        assertNull(result.getStrategies());
    }

    @Test
    void test_controller_ir_should_handle_db_exception_gracefully() throws Exception {
        // Given — DB 查询抛异常
        String repoId = "repo-db-error";
        when(memoryRepoMapper.selectById(repoId)).thenThrow(new RuntimeException("DB connection error"));

        ModifyAgentReq body = new ModifyAgentReq();
        AgentMemoryConfig memCfg = new AgentMemoryConfig();
        memCfg.setMemoryRepoId(repoId);
        body.setMemoryConfig(memCfg);

        ControllerIR ir = new ControllerIR();
        ir.setConfigs(new ControllerConfigIR());

        // When
        Method method = ControllerManagementService.class.getDeclaredMethod(
            "convertControllerMemoryToIr", ModifyAgentReq.class, ControllerIR.class);
        method.setAccessible(true);
        method.invoke(controllerManagementService, body, ir);

        // Then — 异常不阻断流程，仍设置 memoryRepoId
        MemoryConfigIR result = ir.getConfigs().getMemory();
        assertNotNull(result);
        assertEquals(repoId, result.getMemoryRepoId());
        assertNull(result.getExtractConfig());
        assertNull(result.getStrategies());
    }

    @Test
    void test_controller_ir_should_skip_when_repo_id_empty() throws Exception {
        // Given — memoryRepoId 为空
        ModifyAgentReq body = new ModifyAgentReq();
        AgentMemoryConfig memCfg = new AgentMemoryConfig();
        memCfg.setMemoryRepoId("");
        body.setMemoryConfig(memCfg);

        ControllerIR ir = new ControllerIR();
        ir.setConfigs(new ControllerConfigIR());

        // When
        Method method = ControllerManagementService.class.getDeclaredMethod(
            "convertControllerMemoryToIr", ModifyAgentReq.class, ControllerIR.class);
        method.setAccessible(true);
        method.invoke(controllerManagementService, body, ir);

        // Then — 空 repoId 直接跳过，不设置 memory
        assertNull(ir.getConfigs().getMemory());
    }
}
