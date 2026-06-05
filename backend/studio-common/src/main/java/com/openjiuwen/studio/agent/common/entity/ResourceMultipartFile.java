/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.entity;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * Resource转MultipartFile
 *
 */
public class ResourceMultipartFile implements MultipartFile {

    private final Resource resource;

    private final String fileName;

    private final String contentType;

    public ResourceMultipartFile(Resource resource) {
        this.resource = resource;
        this.fileName = resource.getFilename() == null ? "file.jsonl" : resource.getFilename();
        this.contentType = "application/json";
    }

    @Override
    public @NotNull String getName() {
        return "file";
    }

    @Override
    public String getOriginalFilename() {
        return fileName;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean isEmpty() {
        try {
            return resource.contentLength() == 0;
        } catch (IOException e) {
            return true;
        }
    }

    @Override
    public long getSize() {
        try {
            return resource.contentLength();
        } catch (IOException e) {
            return 0;
        }
    }

    // 基于InputStream读取字节，兼容jar包内资源
    @Override
    public byte @NotNull [] getBytes() throws IOException {
        try (InputStream is = resource.getInputStream(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            return bos.toByteArray();
        }
    }

    // 始终返回InputStream，兼容所有Resource类型
    @Override
    public @NotNull InputStream getInputStream() throws IOException {
        return resource.getInputStream();
    }

    // 基于InputStream写入目标文件，兼容jar包内资源
    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        try (InputStream is = resource.getInputStream()) {
            Files.copy(is, dest.toPath());
        }
    }
}
