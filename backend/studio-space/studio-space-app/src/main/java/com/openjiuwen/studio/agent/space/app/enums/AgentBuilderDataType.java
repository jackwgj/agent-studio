/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.enums;

import com.openjiuwen.studio.agent.space.app.config.AgentBuilderConfiguration;
import com.openjiuwen.studio.agent.space.app.constant.Constant;
import com.openjiuwen.studio.agent.space.common.exception.AgentSpaceErrorCodes;
import com.openjiuwen.studio.agent.space.common.exception.AgentSpaceException;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import java.util.Locale;
import java.util.Set;

/**
 *
 */
@Slf4j
public enum AgentBuilderDataType {
    /**
     * 文档
     */
    TEXT {
        @Override
        public void checkAllFileSize(AgentBuilderConfiguration AgentBuilderConfiguration, long allSize) {
            long allFileSizeLimit = AgentBuilderConfiguration.getTextAllFileSizeLimit();
            if (allSize > allFileSizeLimit) {
                log.error("checkAllFileSize: all file size {} is large than limit {}", allSize, allFileSizeLimit);
                throwAllFileSizeExceedLimitError(allFileSizeLimit / Constant.MB + "MB");
            }
        }

        @Override
        public void checkSingleFile(AgentBuilderConfiguration AgentBuilderConfiguration, String fileName,
            long fileSize) {
            super.checkSingleFile(AgentBuilderConfiguration, fileName, fileSize);
            long textFileSizeLimit = AgentBuilderConfiguration.getTextFileSizeLimit();
            if (fileSize > textFileSizeLimit) {
                log.error("validateSingleFile: file {} size {} is large than limit {}", fileName, fileSize,
                    textFileSizeLimit);
                throwFileSizeExceedLimitError(fileName, textFileSizeLimit / Constant.MB + "MB");
            }
        }

        @Override
        public void localFileCheckAllFileSize(AgentBuilderConfiguration localUploadConfiguration, long allSize) {
            long allFileSizeLimit = localUploadConfiguration.getTextAllFileSizeLimit();
            if (allSize > allFileSizeLimit) {
                log.error("localFileCheckAllFileSize: all file size {} is large than limit {}", allSize,
                    allFileSizeLimit);
                throwAllFileSizeExceedLimitError(allFileSizeLimit / Constant.MB + "MB");
            }
        }
    },
    /**
     * 图片
     */
    IMAGE {
        @Override
        public void checkAllFileSize(AgentBuilderConfiguration AgentBuilderConfiguration, long allSize) {
            long allFileSizeLimit = AgentBuilderConfiguration.getImageAllFileSizeLimit();
            if (allSize > allFileSizeLimit) {
                log.error("checkAllFileSize: all file size {} is large than limit {}", allSize, allFileSizeLimit);
                throwAllFileSizeExceedLimitError(allFileSizeLimit / Constant.MB + "MB");
            }
        }

        @Override
        public void checkSingleFile(AgentBuilderConfiguration AgentBuilderConfiguration, String fileName,
            long fileSize) {
            super.checkSingleFile(AgentBuilderConfiguration, fileName, fileSize);
            long imageFileSizeLimit = AgentBuilderConfiguration.getImageFileSizeLimit();
            if (fileSize > imageFileSizeLimit) {
                log.error("validateSingleFile: file size {} is large than limit {}", fileSize, imageFileSizeLimit);
                throwFileSizeExceedLimitError(fileName, imageFileSizeLimit / Constant.MB + "MB");
            }
        }

        @Override
        public void localFileCheckAllFileSize(AgentBuilderConfiguration localUploadConfiguration, long allSize) {
            long allFileSizeLimit = localUploadConfiguration.getImageAllFileSizeLimit();
            if (allSize > allFileSizeLimit) {
                log.error("localFileCheckAllFileSize: all file size {} is large than limit {}", allSize,
                    allFileSizeLimit);
                throwAllFileSizeExceedLimitError(allFileSizeLimit / Constant.MB + "MB");
            }
        }
    };

    /**
     * 校验文件总大小
     *
     * @param AgentBuilderConfiguration
     * @param allSize                    所有文件总大小
     */
    public abstract void checkAllFileSize(AgentBuilderConfiguration AgentBuilderConfiguration, long allSize);

    /**
     * 入库前校验数据集文件格式
     *
     * @param AgentBuilderConfiguration kdsConfiguration
     * @param fileName                   fileName
     * @param fileSize                   fileSize
     */
    public void checkSingleFile(AgentBuilderConfiguration AgentBuilderConfiguration, String fileName, long fileSize) {
        // 校验文件名是否为空
        if (StringUtils.isEmpty(fileName)) {
            log.error("checkSingleFile: fileName is empty");
            throw new AgentSpaceException(AgentSpaceErrorCodes.AGENT_BUILDER_PARAM_INCORRECT_ERROR, "fileName is empty.");
        }
        // 校验文件大小是否为0
        if (fileSize <= 0) {
            log.error("checkSingleFile: fileSize {} <= 0", fileSize);
            throw new AgentSpaceException(AgentSpaceErrorCodes.AGENT_BUILDER_PARAM_INCORRECT_ERROR,
                String.format("file %s is empty.", fileName));
        }
        // 校验文件名有没有后缀名分隔符.
        if (!fileName.contains(".")) {
            log.error("checkSingleFile: splitSize == 0, fileName does not have suffix");
            throw new AgentSpaceException(AgentSpaceErrorCodes.AGENT_BUILDER_PARAM_INCORRECT_ERROR,
                "fileName does not have suffix");
        }
        // 校验文件格式
        Set<String> supportFileTypes = AgentBuilderFileType.getFileTypesOfDataType(this);
        if (!supportFileTypes.contains(StringUtils.substringAfterLast(fileName, '.').toLowerCase(Locale.ROOT))) {
            log.error("checkSingleFile: file {} format is illegal", fileName);
            throw new AgentSpaceException(AgentSpaceErrorCodes.AGENT_BUILDER_PARAM_INCORRECT_ERROR,
                String.format("file {%s} format is illegal", fileName));
        }
        // 校验文件名的最大长度
        int fileNameLengthLimit = AgentBuilderConfiguration.getFileNameLengthLimit();
        if (fileName.length() > fileNameLengthLimit) {
            log.error("checkSingleFile: length of {} exceeds the limit {}", fileName, fileNameLengthLimit);
            throw new AgentSpaceException(AgentSpaceErrorCodes.AGENT_BUILDER_PARAM_INCORRECT_ERROR,
                String.format("length of {%s} exceeds the limit {%s}", fileName, fileNameLengthLimit));
        }
    }

    /**
     * 本地文件接入，校验文件总大小
     *
     * @param fileConfiguration
     * @param allSize           所有文件总大小
     */
    public abstract void localFileCheckAllFileSize(AgentBuilderConfiguration fileConfiguration, long allSize);

    void throwFileSizeExceedLimitError(String fileName, String size) {
        throw new AgentSpaceException(AgentSpaceErrorCodes.AGENT_BUILDER_FILE_SIZE_EXCEED_LIMIT_ERROR, fileName, size);
    }

    void throwAllFileSizeExceedLimitError(String size) {
        throw new AgentSpaceException(AgentSpaceErrorCodes.AGENT_BUILDER_ALL_FILE_SIZE_EXCEED_LIMIT_ERROR, size);
    }
}
