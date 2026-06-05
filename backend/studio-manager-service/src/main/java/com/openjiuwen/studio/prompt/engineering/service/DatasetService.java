/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.prompt.engineering.constant.CommonConstant;
import com.openjiuwen.studio.prompt.engineering.dto.CreateDatasetReq;
import com.openjiuwen.studio.prompt.engineering.dto.DatasetDto;
import com.openjiuwen.studio.prompt.engineering.dto.DatasetResp;
import com.openjiuwen.studio.prompt.engineering.dto.DatasetVo;
import com.openjiuwen.studio.prompt.engineering.dto.GetDatasetListReq;
import com.openjiuwen.studio.prompt.engineering.mapper.DatasetMapper;
import com.openjiuwen.studio.prompt.engineering.mapper.PeEvaluationTaskMapper;
import com.openjiuwen.studio.prompt.engineering.utils.ExcelUtil;
import com.openjiuwen.studio.prompt.engineering.utils.ObsUtil;
import com.openjiuwen.studio.prompt.engineering.utils.StringUtil;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Resource;

/**
 * 功能描述
 *
 */
@Slf4j
@Service
public class DatasetService {
    @Resource
    private PeEvaluationTaskMapper peEvaluationTaskMapper;

    @Autowired
    private DatasetMapper datasetMapper;

    @Autowired
    private ObsUtil obsUtil;

    @Autowired
    private ExcelUtil excelUtil;

    public static final int DATASET_MINIMUM_ROW = 10;


    public DatasetResp listDatasets(String projectId, String workspaceId, GetDatasetListReq req) {
        DatasetResp res = new DatasetResp();
        DatasetDto dto = DatasetDto.builder()
            .datasetType(req.getDatasetType())
            .datasetName(req.getDatasetName())
            .projectId(projectId)
            .workspaceId(workspaceId)
            .build();
        List<DatasetDto> dtos;
        if (req.getLimit() != null && req.getOffset() != null) {
            PageHelper.offsetPage(req.getOffset(), req.getLimit());
            dtos = datasetMapper.getDatasetRecords(dto);

            // 获取总记录数
            int total = (int) new PageInfo<>(dtos).getTotal();
            res.setCount(total);
        } else {
            dtos = datasetMapper.getDatasetRecords(dto);
        }
        List<DatasetVo> datasetRespList = dtos.stream().map(dataSetDto -> {
            DatasetVo dataSet = new DatasetVo();
            BeanUtils.copyProperties(dataSetDto, dataSet);
            return dataSet;
        }).collect(Collectors.toList());
        res.setDatasets(datasetRespList);
        return res;
    }


    public DatasetResp createDataset(String projectId, String workspaceId, CreateDatasetReq req) {
        if (!checkDatasetNum(projectId, req.getDatasetType(), workspaceId)) {
            throw new AgentStudioException(StudioError.DATASET_NUMBER_LIMIT);
        }
        if (!checkDatasetName(projectId, req.getDatasetName(), workspaceId)) {
            throw new AgentStudioException(StudioError.DATASET_NAME_ALREADY_EXIST);
        }
        if (!checkObsBucket(RequestContextUtils.getRequestAuthToken(), req.getObsPath())) {
            throw new AgentStudioException(StudioError.OBS_BUCKET_DOES_NOT_EXIST);
        }
        checkDatasetFile(RequestContextUtils.getRequestAuthToken(), req.getObsPath());
        DatasetDto dto = DatasetDto.builder()
            .datasetName(req.getDatasetName())
            .datasetType(req.getDatasetType())
            .obsPath(req.getObsPath())
            .description(req.getDescription())
            .build();
        dto.setDatasetId(UUID.randomUUID().toString());
        dto.setCreateTime(new Date());
        dto.setUpdateTime(new Date());
        dto.setProjectId(projectId);
        dto.setWorkspaceId(workspaceId);
        try {
            datasetMapper.createDataset(dto);
        } catch (RuntimeException e) {
            log.error("Add dataset failed, datasetName: {} msg: {}", req.getDatasetName(), e.getMessage());
            throw new AgentStudioException(StudioError.SYSTEM_ERROR_ADD_DATASET, req.getDatasetName());
        }
        DatasetResp datasetResp = new DatasetResp();
        datasetResp.setDatasetId(dto.getDatasetId());
        return datasetResp;
    }

    private boolean checkDatasetNum(String projectId, String dataType, String workspaceId) {
        int count = datasetMapper.queryDatasetCountByProjectId(projectId, dataType, workspaceId);
        return count <= CommonConstant.NUM_FIVE_HUNDRED;
    }

    private boolean checkDatasetName(String projectId, String datasetName, String workspaceId) {
        DatasetDto dataset = datasetMapper.getDatasetByName(projectId, datasetName, workspaceId);
        return ObjectUtils.isEmpty(dataset);
    }

    private boolean checkObsBucket(String xAuthToken, String obsPath) {
        String bucket = StringUtil.removeNewlineCharacter(obsPath).split(CommonConstant.FOLDER_SEPARATOR)[0];
        return obsUtil.hasBucket(xAuthToken, bucket);
    }

    private void checkDatasetFile(String xAuthToken, String obsPath) {
        String path = StringUtil.removeNewlineCharacter(obsPath);

        // 校验文件格式
        if (!path.endsWith(CommonConstant.EXCEL_SUFFIX_XLSX)) {
            throw new AgentStudioException(StudioError.DATASET_FILE_FORMAT_ERROR);
        }
        String bucket = path.split(CommonConstant.FOLDER_SEPARATOR)[0];
        String filePath = path.substring(obsPath.indexOf(CommonConstant.FOLDER_SEPARATOR) + 1);
        if (!obsUtil.hasFile(xAuthToken, bucket, filePath)) {
            throw new AgentStudioException(StudioError.DATASET_FILE_DOES_NOT_EXIST);
        }
        excelUtil.obtainDataSetContent(xAuthToken, bucket, filePath);
        int fileLength = excelUtil.getExcelLineLength(xAuthToken, bucket, filePath);

        // 内容行数
        if (fileLength < DATASET_MINIMUM_ROW || fileLength > CommonConstant.NUM_FIFTY) {
            throw new AgentStudioException(StudioError.DATASET_ROW_NUM_LIMIT);
        }
    }


    public DatasetResp deleteDataset(String projectId, String datasetId, String workspaceId) {
        if (!peEvaluationTaskMapper.queryEvaluationTaskIdsByTestSetId(datasetId, workspaceId).isEmpty()) {
            throw new AgentStudioException(StudioError.DATASET_IN_USING);
        }
        try {
            datasetMapper.deleteDataset(datasetId, workspaceId);
        } catch (RuntimeException e) {
            log.error("Delete dataset failed, datasetId: {}, cause by: {}", datasetId, e.getMessage());
            throw new AgentStudioException(StudioError.SYSTEM_ERROR_DELETE_DATASET_FAILED, datasetId);
        }
        DatasetResp datasetResp = new DatasetResp();
        datasetResp.setDatasetId(datasetId);
        return datasetResp;
    }


    public DatasetResp previewDataset(String projectId, String datasetId, String workspaceId)
        throws AgentStudioException {
        DatasetResp res = new DatasetResp();
        DatasetDto dataset = datasetMapper.getDatasetById(projectId, datasetId, workspaceId);
        String path = StringUtil.removeNewlineCharacter(dataset.getObsPath());
        String bucket = path.split(CommonConstant.FOLDER_SEPARATOR)[0];
        String filePath = path.substring(path.indexOf('/') + 1);
        List<Map<String, String>> valueList = excelUtil.obtainDataSetContent(RequestContextUtils.getRequestAuthToken(), bucket, filePath);
        res.setDatasetValues(valueList);
        return res;
    }
}
