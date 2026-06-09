/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.md;

import com.openjiuwen.studio.agent.manager.dto.ModelInterfaceProtocolListRsp;
import com.openjiuwen.studio.agent.manager.dto.ModelInterfaceProtocolRsp;
import com.openjiuwen.studio.agent.manager.dto.QueryMdInterfaceProtocolQo;
import com.openjiuwen.studio.agent.manager.entity.md.MdInterfaceProtocol;
import com.openjiuwen.studio.agent.manager.service.IModelInterfaceProtocolMgmtService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class ModelInterfaceProtocolMgmtService implements IModelInterfaceProtocolMgmtService {
    @Autowired
    private ModelInterfaceProtocolService service;

    @Override
    public ModelInterfaceProtocolListRsp queryMdInterfaceProtocol(String projectId, QueryMdInterfaceProtocolQo qo) {
        List<MdInterfaceProtocol> protocolList = service.queryProtocols(qo.getModelType(), qo.getVisible());

        List<ModelInterfaceProtocolRsp> data = new ArrayList<>(protocolList.size());
        for (MdInterfaceProtocol protocol : protocolList) {
            ModelInterfaceProtocolRsp rsp = new ModelInterfaceProtocolRsp();
            BeanUtils.copyProperties(protocol, rsp);
            data.add(rsp);
        }

        return new ModelInterfaceProtocolListRsp().setData(data);
    }
}
