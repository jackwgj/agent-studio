/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.utils;

import static com.openjiuwen.studio.prompt.engineering.constant.CommonConstant.APPLICATION_EXCEL;
import static com.openjiuwen.studio.prompt.engineering.constant.CommonConstant.APPLICATION_MS_EXCEL;
import static com.openjiuwen.studio.prompt.engineering.enums.FileType.FILE_TYPE_EXCEL;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.prompt.engineering.enums.FileType;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Set;

import javax.imageio.ImageIO;


/**
 * 功能描述
 *
 */
@Slf4j
public class FileUtil {

    /**
     * 校验文件类型
     *
     * @param file 文件对象
     * @param fileType 文件类型
     * @return 是否配诶结果
     */
    public static boolean validateFileType(MultipartFile file, FileType fileType) {
        if (fileType == FILE_TYPE_EXCEL) {
            return file.getOriginalFilename().endsWith(APPLICATION_MS_EXCEL) || file.getOriginalFilename()
                .endsWith(APPLICATION_EXCEL);
        } else {
            throw new AgentStudioException(StudioError.FILE_TYPE_IS_UNDEFINED, Collections.singletonList(file
                .getContentType()));
        }
    }

    /**
     * 校验上传的文件：主要校验大小与类型，根据fileType进行限制
     *
     * @param file     file
     * @param fileType fileType
     */
    public static void validateFile(MultipartFile file, FileType fileType) {
        switch (fileType) {
            case JSON -> {
                validateFileSizeAndSuffix(file, 1024 * 1024 * 5, Set.of("json"));
            }
            case IMAGE -> {
                validateFileSizeAndSuffix(file, 1024 * 1024 * 5, Set.of("jpg", "jpeg", "png", "webp", "gif"));
            }
            case FILE_TYPE_EXCEL -> {
                validateFile(file, 1024 * 1024 * 20, Set.of("xlsx", "xls"));
            }
            default -> {
                log.error("validateFile: file type is not supported");
                throw new AgentStudioException(StudioError.UPLOAD_FILE_TYPE_INVALID);
            }
        }
    }

    public static void validateFileSizeAndSuffix(MultipartFile file, int fileSize, Set<String> suffixSet) {
        validateFile(file, fileSize, suffixSet);

        try (InputStream inputStream = file.getInputStream()) {
            BufferedImage img = ImageIO.read(inputStream);
            if (img == null) {
                throw new AgentStudioException(StudioError.ILLEGAL_IMAGE_FILE);
            }
        } catch (IOException e) {
            throw new AgentStudioException(StudioError.OBS_READ_IO_EXCEPTION);
        }

        try {
            byte[] bytes = file.getBytes();
            if (isBMPFormat(bytes)) {
                throw new AgentStudioException(StudioError.ILLEGAL_IMAGE_FILE);
            }
        } catch (IOException e) {
            throw new AgentStudioException(StudioError.OBS_READ_IO_EXCEPTION);
        }
    }

    public static void validateFile(MultipartFile file, int fileSize, Set<String> suffixSet) {
        if (file == null || file.isEmpty()) {
            log.error("validateFileSizeAndSuffix: file is empty");
            throw new AgentStudioException(StudioError.FILE_DOES_NOT_EXIST);
        }
        if (file.getSize() > fileSize) {
            log.error("validateFileSizeAndSuffix: file size is too large");
            throw new AgentStudioException(StudioError.TASK_FILE_SIZE_EXCEED_LIMIT);
        }
        String fileName = file.getOriginalFilename();
        if (StringUtils.isBlank(fileName) || fileName.contains("..") || fileName.contains("\\") || fileName.contains("/")) {
            log.error("The file name is illegal: {}", fileName);
            throw new AgentStudioException(StudioError.PE_ILLEGAL_FILE_NAME, Collections.singletonList(fileName));
        }
        String fileType = StringUtils.substringAfterLast(fileName, ".");
        if (StringUtils.isBlank(fileType) || !suffixSet.contains(StringUtils.toRootLowerCase(fileType))) {
            log.error("The file type is not supported: {}", fileType);
            throw new AgentStudioException(StudioError.ILLEGAL_FILE_TYPE);
        }
    }

    public static String validateFilePath(String base, String fileId) {
        return Paths.get(base).resolve(fileId).normalize().toString();
    }

    private static boolean isBMPFormat(byte[] header) {
        return header.length >= 2 &&
                header[0] == (byte) 0x42 &&
                header[1] == (byte) 0x4D;
    }
}
