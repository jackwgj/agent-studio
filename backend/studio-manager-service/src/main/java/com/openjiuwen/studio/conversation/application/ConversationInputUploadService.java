/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.conversation.application;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;
import com.openjiuwen.studio.conversation.application.dto.ConversationInputUploadVo;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

/** 对话工作台附件上传；对象键仅由服务端身份和随机值派生。 */
@Service
public class ConversationInputUploadService {
    static final long MAX_INPUT_FILE_SIZE = 60L * 1024 * 1024;
    private static final String PREFIX = "conversation-inputs";

    private final MgObsService mgObsService;
    private final ConversationWorkspaceAccessGuard accessGuard;

    public ConversationInputUploadService(MgObsService mgObsService, ConversationWorkspaceAccessGuard accessGuard) {
        this.mgObsService = mgObsService;
        this.accessGuard = accessGuard;
    }

    public ConversationInputUploadVo upload(String projectId, String workspaceId, MultipartFile file) {
        return upload(projectId, workspaceId, file, null);
    }

    public ConversationInputUploadVo upload(String projectId, String workspaceId, MultipartFile file,
                                            String fileNameBase64) {
        accessGuard.requireAccess(projectId, workspaceId);
        if (file == null || file.isEmpty() || file.getSize() > MAX_INPUT_FILE_SIZE) {
            throw new AgentStudioException(StudioError.FILE_SIZE_EXCEED_LIMIT);
        }
        String fileName = StringUtils.isBlank(fileNameBase64)
            ? safeBasename(file.getOriginalFilename())
            : decodeFileName(fileNameBase64);
        byte[] bytes = readBytes(file);
        if (bytes.length == 0 || bytes.length > MAX_INPUT_FILE_SIZE) {
            throw new AgentStudioException(StudioError.FILE_SIZE_EXCEED_LIMIT);
        }
        String objectKey = String.format("%s/%s/%s/%s/%s/%s", PREFIX, sha256(projectId), sha256(workspaceId),
            sha256(RequestContextUtils.getRequestUserId()), UUID.randomUUID(), fileName);
        mgObsService.uploadObsFile(objectKey, new ByteArrayInputStream(bytes), -1);
        return ConversationInputUploadVo.builder().objectKey(objectKey).fileName(fileName).size(bytes.length)
            .checksum(sha256(bytes)).build();
    }

    private static String decodeFileName(String encodedFileName) {
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(encodedFileName);
            String fileName = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes))
                .toString();
            return safeBasename(fileName);
        } catch (IllegalArgumentException | CharacterCodingException exception) {
            throw new AgentStudioException(StudioError.METHOD_ARGUMENT_NOT_VALID);
        }
    }

    private static byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException exception) {
            throw new AgentStudioException(StudioError.OBS_FAILED);
        }
    }

    private static String safeBasename(String originalName) {
        String normalizedName = StringUtils.defaultString(originalName);
        String basename = FilenameUtils.getName(normalizedName);
        if (StringUtils.isBlank(basename) || basename.getBytes(StandardCharsets.UTF_8).length > 180
            || !basename.equals(normalizedName)
            || basename.chars().anyMatch(character -> character < 32 || character == 127)) {
            throw new AgentStudioException(StudioError.METHOD_ARGUMENT_NOT_VALID);
        }
        return basename;
    }

    static String sha256(String value) {
        return sha256(value.getBytes(StandardCharsets.UTF_8));
    }

    static String sha256(byte[] bytes) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
            StringBuilder result = new StringBuilder(digest.length * 2);
            for (byte value : digest) {
                result.append(String.format("%02x", value));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is required", exception);
        }
    }
}
