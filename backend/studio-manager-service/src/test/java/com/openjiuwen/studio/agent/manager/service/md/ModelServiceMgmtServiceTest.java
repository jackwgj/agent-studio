/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.md;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.dto.AvailableModelServicesQo;
import com.openjiuwen.studio.agent.manager.dto.ModelServiceListQo;
import com.openjiuwen.studio.agent.manager.dto.ModelServiceListRsp;
import com.openjiuwen.studio.agent.manager.dto.ModelServiceRsp;
import com.openjiuwen.studio.agent.manager.dto.ProviderListRsp;
import com.openjiuwen.studio.agent.manager.entity.ModelExportEntity;
import com.openjiuwen.studio.agent.manager.entity.ProviderExportMetadata;
import com.openjiuwen.studio.agent.manager.entity.md.ModelServiceBase;
import com.openjiuwen.studio.agent.manager.entity.md.ModelServiceData;
import com.openjiuwen.studio.agent.manager.entity.md.ModelServiceProvider;
import com.openjiuwen.studio.agent.manager.entity.md.ProviderAuthData;
import com.openjiuwen.studio.agent.manager.entity.md.ProviderAuthMetadata;
import com.openjiuwen.studio.agent.manager.mapper.md.FreeModelServiceMapper;
import com.openjiuwen.studio.agent.manager.mapper.md.ModelServiceMapper;
import com.openjiuwen.studio.agent.manager.mapper.md.ProviderAuthDataMapper;
import com.openjiuwen.studio.agent.manager.mapper.md.SysModelServiceProviderMapper;
import com.openjiuwen.studio.agent.manager.mapper.md.UserModelServiceProviderMapper;
import com.openjiuwen.studio.agent.manager.rce.client.CustomModelManagerClient;
import com.openjiuwen.studio.agent.manager.rce.models.CustomModelListRsp;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ModelServiceMgmtServiceTest {
    @Test
    public void queryFreeModelTest() {
        ModelServiceMgmtService service = new ModelServiceMgmtService();
        Assertions.assertNull(service.queryFreeModel("null"));

        FreeModelServiceMapper freeModelMapper = Mockito.mock(FreeModelServiceMapper.class);

        ModelServiceData mockData = new ModelServiceData();
        mockData.setId("mock");

        Mockito.when(freeModelMapper.queryAllFreeModels(Mockito.any())).thenReturn(Collections.singletonList(mockData));
        ReflectionTestUtils.setField(service, "freeModelEnable", true);
        ReflectionTestUtils.setField(service, "freeModelMapper", freeModelMapper);

        ModelServiceBase base = service.queryFreeModel("mock");
        Assertions.assertNotNull(base);
        Assertions.assertEquals("mock", base.getId());
        assertNull(service.queryFreeModel("not_exit"));
    }

    @Test
    public void modelServiceDetailTest() {
        ModelServiceMapper modelServiceMapper = Mockito.mock(ModelServiceMapper.class);
        ModelServiceBase systemModel = new ModelServiceBase().setProjectId("SYSTEM")
            .setWorkspaceId("SYSTEM")
            .setProviderId("100");
        Mockito.when(modelServiceMapper.queryModelServiceDetail("deepseek")).thenReturn(systemModel);
        ModelServiceBase userModel = new ModelServiceBase().setProjectId("test_p")
            .setWorkspaceId("test_w")
            .setProviderId("200");
        Mockito.when(modelServiceMapper.queryModelServiceDetail("qwen")).thenReturn(userModel);

        SysModelServiceProviderMapper sysModelServiceProviderMapper = Mockito.mock(SysModelServiceProviderMapper.class);
        Mockito.when(sysModelServiceProviderMapper.selectById("100"))
            .thenReturn(new ModelServiceProvider().setProviderName("MaaS"));

        UserModelServiceProviderMapper userModelServiceProviderMapper = Mockito.mock(
            UserModelServiceProviderMapper.class);
        Mockito.when(userModelServiceProviderMapper.selectById("200")).thenReturn(null);

        ProviderAuthDataMapper authDataMapper = Mockito.mock(ProviderAuthDataMapper.class);
        Mockito.when(authDataMapper.selectByMetaId(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
            .thenReturn(new ProviderAuthData());

        ModelServiceMgmtService service = new ModelServiceMgmtService();
        ReflectionTestUtils.setField(service, "modelServiceMapper", modelServiceMapper);
        ReflectionTestUtils.setField(service, "sysModelServiceProviderMapper", sysModelServiceProviderMapper);
        ReflectionTestUtils.setField(service, "userModelServiceProviderMapper", userModelServiceProviderMapper);
        ReflectionTestUtils.setField(service, "authDataMapper", authDataMapper);

        ModelServiceRsp rsp = service.modelServiceDetail("test_p", "test_w", "deepseek");
        Assertions.assertEquals("MaaS", rsp.getProviderName());

        rsp = service.modelServiceDetail("test_p", "test_w", "qwen");
        Assertions.assertNull(rsp.getProviderName());
    }

    @Test
    public void customModelsTest() {
        ModelServiceMgmtService service = new ModelServiceMgmtService();

        ModelServiceMapper modelServiceMapper = Mockito.mock(ModelServiceMapper.class);
        FreeModelServiceMapper freeModelMapper = Mockito.mock(FreeModelServiceMapper.class);
        ModelLicenseCtrlService licenseCtrlService = Mockito.mock(ModelLicenseCtrlService.class);

        ReflectionTestUtils.setField(service, "modelServiceMapper", modelServiceMapper);
        ReflectionTestUtils.setField(service, "freeModelMapper", freeModelMapper);
        ReflectionTestUtils.setField(service, "licenseCtrlService", licenseCtrlService);

        ModelServiceData data = new ModelServiceData();
        data.setId("free");
        when(freeModelMapper.queryAllFreeModels(Mockito.any())).thenReturn(Collections.singletonList(data));

        AvailableModelServicesQo qo = new AvailableModelServicesQo().setModelType("LLM,NLP")
            .setPublishStatus(null)
            .setGroupby("provider");

        try (MockedStatic<RequestContextUtils> requestContextMock = Mockito.mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS)) {
            requestContextMock.when(RequestContextUtils::getRequestUserId).thenReturn("user");
            requestContextMock.when(RequestContextUtils::getRequestProjectId).thenReturn("project");
            requestContextMock.when(RequestContextUtils::getRequestUserDomainId).thenReturn("domain");
            requestContextMock.when(RequestContextUtils::getRequestAuthToken).thenReturn("token");

            when(modelServiceMapper.queryAvailableServices(Mockito.any())).thenReturn(new ArrayList<>());

            ReflectionTestUtils.setField(service, "platformProviderId", "100");
            ProviderListRsp rsp = (ProviderListRsp) service.availableModelServices("test_project", qo);
            Assertions.assertEquals(0, rsp.getData().size());

            ReflectionTestUtils.setField(service, "freeModelEnable", true);
            rsp = (ProviderListRsp) service.availableModelServices("test_project", qo);
            Assertions.assertEquals(0, rsp.getData().size());

            rsp = (ProviderListRsp) service.availableModelServices("test_project", qo);
            Assertions.assertEquals(0, rsp.getData().size());

            ReflectionTestUtils.setField(service, "customEnabled", true);
            CustomModelManagerClient customModelManagerClient = Mockito.mock(CustomModelManagerClient.class);
            ReflectionTestUtils.setField(service, "customModelManagerClient", customModelManagerClient);

            ResponseEntity<CustomModelListRsp> mockCustom = ResponseEntity.status(400).body(null);
            when(customModelManagerClient.getAvailableModelByUserId(Mockito.anyString(), Mockito.anyString())).thenReturn(
                mockCustom);
            try {
                service.availableModelServices("test_project", qo);
                fail();
            } catch (AgentStudioException e) {
                Assertions.assertEquals(StudioError.MD_GET_CUSTOM_MODELS_FAIL, e.getErrorCode());
            }

            mockCustom = ResponseEntity.status(200).body(null);
            when(customModelManagerClient.getAvailableModelByUserId(Mockito.anyString(), Mockito.anyString())).thenReturn(
                mockCustom);
            rsp = (ProviderListRsp) service.availableModelServices("test_project", qo);
            assertEquals(0, rsp.getData().size());
        }
    }

    @Test
    public void mockBuildExportResult() {
        // 1. 模拟数据库查询出的基础数据
        String pid = "p_001";
        String mid1 = "m_001";
        String mid2 = "m_002";

        // 模拟 2 个模型数据 (ModelServiceData)
        ModelServiceData m1 = new ModelServiceData();
        m1.setId(mid1);
        m1.setProviderId(pid);
        m1.setServiceName("Model-V1");
        m1.setThrottlingPolicy(100); // 验证之前报错的 Integer 类型

        ModelServiceData m2 = new ModelServiceData();
        m2.setId(mid2);
        m2.setProviderId(pid);
        m2.setServiceName("Model-V2");
        m2.setThrottlingPolicy(200);

        List<ModelServiceData> mockModelList = Arrays.asList(m1, m2);

        // 模拟供应商基础信息 (ModelServiceProvider)
        ModelServiceProvider sp = new ModelServiceProvider();
        sp.setId(pid);
        sp.setProviderName("Provider");

        // 模拟认证信息 (ProviderAuthMetadata)
        ProviderAuthMetadata am = new ProviderAuthMetadata();
        am.setId(pid);
        am.setProviderId(pid);
        am.setAuthType("IAM_TOKEN");

        // 2. 模拟 batchGetProviderExportMetadata 的内部组装
        Map<String, ProviderExportMetadata> providerExportMap = new HashMap<>();
        ProviderExportMetadata pem = new ProviderExportMetadata();
        pem.setModelServiceProviderMetadata(sp);
        pem.setProviderAuthMetadata(am);
        providerExportMap.put(pid, pem);

        // 3. 执行核心分组与封装逻辑 (对应你 buildModelExportEntity 里的循环)
        Map<String, List<ModelServiceData>> groupedModels = mockModelList.stream()
            .collect(Collectors.groupingBy(m -> Objects.toString(m.getProviderId(), "UNKNOWN")));

        List<ModelExportEntity> result = new ArrayList<>();
        for (Map.Entry<String, List<ModelServiceData>> entry : groupedModels.entrySet()) {
            ModelExportEntity exportEntity = new ModelExportEntity();
            exportEntity.setModelMetadata(new ArrayList<>(entry.getValue()));
            exportEntity.setProviderMetadata(providerExportMap.get(entry.getKey()));
            result.add(exportEntity);
        }

        // 4. 断言验证 (JUnit 方式)
        assert !result.isEmpty() : "结果集不能为空";
        assert result.size() == 1 : "由于是同一个 Provider，应该只有 1 个导出实体";

        ModelExportEntity finalEntity = result.get(0);
        assert finalEntity.getModelMetadata().size() == 2 : "模型数量应为 2";
        assert finalEntity.getProviderMetadata() != null : "供应商元数据不能为空";

        // 验证复合结构中的数据
        assert "Provider".equals(finalEntity.getProviderMetadata().getModelServiceProviderMetadata().getProviderName());
        assert "IAM_TOKEN".equals(finalEntity.getProviderMetadata().getProviderAuthMetadata().getAuthType());

        // 验证 Integer 类型的流控字段
        assert finalEntity.getModelMetadata().get(0).getThrottlingPolicy() == 100;
    }

    @Test
    public void modelServiceListUnsubTest() {
        ModelServiceMgmtService service = new ModelServiceMgmtService();
        ReflectionTestUtils.setField(service, "envType", "lite");
        ReflectionTestUtils.setField(service, "platformProviderId", "100");
        ModelServiceListRsp rsp = service.modelServiceList("test_project",
            new ModelServiceListQo().setProviderId("100").setIsSubscribed(false));
        Assertions.assertEquals(0, rsp.getTotal());
    }
}
