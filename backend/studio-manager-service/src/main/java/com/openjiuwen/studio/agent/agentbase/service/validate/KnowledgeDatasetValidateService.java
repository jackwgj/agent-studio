/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.validate;

import com.openjiuwen.studio.agent.agentbase.common.enums.KnowledgeBaseType;
import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeBaseEntity;
import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeRepoEntity;
import com.openjiuwen.studio.agent.agentbase.model.KnowledgeRepo;
import com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.KnowledgeRepoContext;
import com.openjiuwen.studio.agent.manager.dto.FaqFileInfoListRsp;
import com.openjiuwen.studio.agent.manager.dto.FileInfo;
import com.openjiuwen.studio.agent.manager.dto.ListFaqFileReq;
import com.openjiuwen.studio.agent.manager.dto.ListFileReq;
import com.openjiuwen.studio.agent.manager.dto.ListKnowledgeFilesResponseBody;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Slf4j
@Service
public class KnowledgeDatasetValidateService {

    public boolean faqFileBelongToKnowledgeRepo(List<String> fileIds, KnowledgeBaseEntity knowledgeBaseEntity,
        KnowledgeRepoEntity knowledgeRepoEntity, KnowledgeRepoContext knowledgeRepoContext) {
        List<FileInfo> files = getAllFaqFiles(knowledgeBaseEntity, knowledgeRepoEntity, knowledgeRepoContext);
        return new HashSet<>(ObjectUtils.firstNonNull(files, new ArrayList<FileInfo>())
            .stream()
            .map(FileInfo::getFileId)
            .toList()).containsAll(fileIds);
    }

    public boolean fileBelongToKnowledgeRepo(List<String> fileIds, KnowledgeBaseEntity knowledgeBaseEntity,
        KnowledgeRepoEntity knowledgeRepoEntity, KnowledgeRepoContext knowledgeRepoContext) {
        List<FileInfo> files = getAllFiles(knowledgeBaseEntity, knowledgeRepoEntity, knowledgeRepoContext);
        return new HashSet<>(ObjectUtils.firstNonNull(files, new ArrayList<FileInfo>())
            .stream()
            .map(FileInfo::getFileId)
            .toList()).containsAll(fileIds);
    }

    private List<FileInfo> getAllFiles(KnowledgeBaseEntity knowledgeBaseEntity, KnowledgeRepoEntity knowledgeRepoEntity,
        KnowledgeRepoContext knowledgeRepoContext) {
        KnowledgeRepo knowledgeRepo = new KnowledgeRepo().setKnowledgeRepoId(knowledgeBaseEntity.getExternalId())
            .setType(KnowledgeBaseType.fromValue(knowledgeRepoEntity.getType()))
            .setMetadata(knowledgeRepoEntity.getMetadata());
        int pageNum = 1;
        int limit = 100;
        ListFileReq listFileReq = new ListFileReq().setPageNum(pageNum).setPageSize(limit);
        ListKnowledgeFilesResponseBody fileInfoListRsp;
        List<FileInfo> fileInfos = new ArrayList<>();
        while (true) {
            listFileReq.setPageNum(pageNum);
            fileInfoListRsp = knowledgeRepoContext.getService(knowledgeRepoEntity.getSource())
                .listFiles(knowledgeRepo, listFileReq);
            if (fileInfoListRsp == null || CollectionUtils.isEmpty(fileInfoListRsp.getFileInfoList())) {
                break;
            }
            fileInfos.addAll(fileInfoListRsp.getFileInfoList());
            pageNum++;
        }
        return fileInfos;
    }

    private List<FileInfo> getAllFaqFiles(KnowledgeBaseEntity knowledgeBaseEntity,
        KnowledgeRepoEntity knowledgeRepoEntity, KnowledgeRepoContext knowledgeRepoContext) {
        KnowledgeRepo knowledgeRepo = new KnowledgeRepo().setKnowledgeRepoId(knowledgeBaseEntity.getExternalId())
            .setType(KnowledgeBaseType.fromValue(knowledgeRepoEntity.getType()))
            .setMetadata(knowledgeRepoEntity.getMetadata());
        int pageNum = 1;
        int limit = 100;
        ListFaqFileReq listFileReq = new ListFaqFileReq().setPageNum(pageNum).setPageSize(limit);
        FaqFileInfoListRsp fileInfoListRsp = new FaqFileInfoListRsp();
        List<FileInfo> fileInfos = new ArrayList<>();
        while (true) {
            listFileReq.setPageNum(pageNum);
            fileInfoListRsp = knowledgeRepoContext.getService(knowledgeRepoEntity.getSource())
                .listFaqFiles(knowledgeRepo, listFileReq);
            if (fileInfoListRsp == null || CollectionUtils.isEmpty(fileInfoListRsp.getFileInfoList())) {
                break;
            }
            fileInfos.addAll(fileInfoListRsp.getFileInfoList());
            pageNum++;
        }
        return fileInfos;
    }

}
