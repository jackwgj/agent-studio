/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.utils;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Slf4j
public class PromptDataSetUtil {

    // 允许的图片后缀
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png");

    // 临时文件前缀/后缀
    private static final String TEMP_ZIP_PREFIX = "template_";
    private static final String TEMP_ZIP_SUFFIX = ".zip";

    // 上传ZIP文件大小限制（MB，从配置文件读取，默认100MB）
    private static long ZIP_FILE_SIZE = 100L;

    // ========== ZIP炸弹校验相关常量（可根据业务调整） ==========
    private static final long MAX_SINGLE_UNCOMPRESSED_SIZE = 100 * 1024 * 1024L; // 单个文件解压后最大限制（100MB）
    private static final long MAX_TOTAL_UNCOMPRESSED_SIZE = 1024 * 1024 * 1024L; // 所有文件解压后总大小限制（1GB）
    private static final int MAX_COMPRESSION_RATIO = 10; // 最大允许压缩比（10:1，避免极端压缩）
    private static final int MAX_DIR_DEPTH = 2; // 最大目录层级（避免无限递归目录）
    private static final int MAX_FILE_COUNT = 1000; // 最大文件数量（避免海量小文件）
    private static final int STREAM_CHUNK_SIZE = 8192; // 流式读取块大小


    public static File saveToTempFile(MultipartFile file) throws IOException {
        // 1. 原有基础校验（文件非空、后缀为ZIP、大小不超限）
        validateBasicFileInfo(file);

        // 2. 创建临时文件并写入内容
        File tempFile = createTempFile(file);

        try {
            // 3. 新增：ZIP炸弹校验（核心逻辑）
            validateZipBomb(tempFile);
            log.info("temp zip file saved successfully(size:{}MB), ZIP bomb check passed",
                tempFile.length() / (1024 * 1024));
            return tempFile;
        } catch (AgentStudioException e) {
            // 校验失败：主动删除临时文件，抛出异常
            deleteTempFile(tempFile);
            throw e;
        } catch (IOException e) {
            deleteTempFile(tempFile);
            log.error("ZIP file parse failed (ZIP bomb check)", e);
            throw new AgentStudioException(StudioError.PROMPT_EVAL_DATA_FILE_ERROR);
        }
    }

    /**
     * 原有基础校验：文件非空、后缀、大小
     */
    private static void validateBasicFileInfo(MultipartFile file) {
        if (file.isEmpty()) {
            log.error("eval data file is empty!");
            throw new AgentStudioException(StudioError.PROMPT_EVAL_DATA_FILE_ERROR);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase(Locale.ENGLISH).endsWith(".zip")) {
            log.error("upload file is not a zip file! filename:{}", originalFilename);
            throw new AgentStudioException(StudioError.PROMPT_EVAL_DATA_NOT_FOUND);
        }

        long fileSizeMB = file.getSize() / (1024 * 1024);
        if (fileSizeMB > ZIP_FILE_SIZE) {
            log.error("The file size exceeds the limit. limit:{}MB, actual:{}MB", ZIP_FILE_SIZE, fileSizeMB);
            throw new AgentStudioException(StudioError.PROMPT_DATA_SIZE_ERROR);
        }
    }

    /**
     * 原有逻辑：创建临时文件并写入MultipartFile内容
     */
    private static File createTempFile(MultipartFile file) throws IOException {
        File tempFile = File.createTempFile(TEMP_ZIP_PREFIX, TEMP_ZIP_SUFFIX);
        tempFile.deleteOnExit(); // JVM退出时兜底删除（Spring Boot中需配合主动删除）

        try (InputStream in = file.getInputStream();
            OutputStream out = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        }
        return tempFile;
    }

    /**
     * 新增：ZIP炸弹校验核心逻辑（基于JDK原生ZipFile）
     */
    private static void validateZipBomb(File zipFile) throws IOException {
        long totalUncompressedSize = 0L; // 所有文件解压后总大小（基于真实读取）
        int fileCount = 0; // 已遍历文件数量

        // 流式解析ZIP文件（使用ZipFile，避免内存溢出）
        try (ZipFile zip = new ZipFile(zipFile)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String entryName = entry.getName();

                // 3.0 新增：校验条目名称（防御路径遍历攻击）
                if (isMaliciousEntryName(entryName)) {
                    log.error("ZIP bomb detected: malicious entry name. entry:{}", entryName);
                    throw new AgentStudioException(StudioError.PROMPT_EVAL_DATA_FILE_ERROR, "ZIP条目包含恶意路径");
                }

                // 3.1 校验目录层级（避免无限递归目录）
                if (entry.isDirectory()) {
                    int dirDepth = calculateDirDepth(entryName);
                    if (dirDepth > MAX_DIR_DEPTH) {
                        log.error("ZIP bomb detected: directory depth exceeds limit. depth:{}, limit:{}, entry:{}",
                            dirDepth, MAX_DIR_DEPTH, entryName);
                        throw new AgentStudioException(StudioError.PROMPT_EVAL_DATA_FILE_ERROR);
                    }
                    continue;
                }

                // 3.2 安全获取单个文件真实大小（绕过元数据，流式读取验证）
                long realUncompressedSize = getRealEntrySize(zip, entry);
                // 校验单个文件真实大小（替代不可信的entry.getSize()）
                if (realUncompressedSize > MAX_SINGLE_UNCOMPRESSED_SIZE) {
                    log.error("ZIP bomb detected: single file too large. entry:{}, realSize:{}MB, limit:{}MB",
                        entryName, realUncompressedSize / (1024 * 1024), MAX_SINGLE_UNCOMPRESSED_SIZE / (1024 * 1024));
                    throw new AgentStudioException(StudioError.FILE_ENTRY_SIZE_LARGE, entryName, realUncompressedSize);
                }

                // 3.3 安全校验压缩比（基于真实大小+压缩大小，修复逻辑缺陷）
                long compressedSize = entry.getCompressedSize();
                // 处理compressedSize=0/负数的情况（伪造值）
                if (compressedSize <= 0) {
                    // 压缩大小非法，使用真实读取的大小作为参考（未压缩文件压缩比=1）
                    compressedSize = realUncompressedSize;
                }
                // 计算压缩比（避免除零，且用真实大小）
                double compressionRatio = (double) realUncompressedSize / compressedSize;
                if (compressionRatio > MAX_COMPRESSION_RATIO) {
                    log.error("ZIP bomb detected: abnormal compression ratio. entry:{}, ratio:{:.2f}, limit:{}",
                        entryName, compressionRatio, MAX_COMPRESSION_RATIO);
                    throw new AgentStudioException(StudioError.PROMPT_EVAL_DATA_FILE_ERROR, "ZIP文件压缩比异常");
                }

                // 3.4 累计统计：总解压大小 + 文件数量（基于真实大小）
                totalUncompressedSize += realUncompressedSize;
                fileCount++;

                // 3.5 校验总解压大小（基于真实大小）
                if (totalUncompressedSize > MAX_TOTAL_UNCOMPRESSED_SIZE) {
                    log.error("ZIP bomb detected: total uncompressed size exceeds limit. total:{}GB, limit:{}GB",
                        totalUncompressedSize / (1024 * 1024 * 1024), MAX_TOTAL_UNCOMPRESSED_SIZE / (1024 * 1024 * 1024));
                    throw new AgentStudioException(StudioError.FILE_SIZE_TOO_LARGE, totalUncompressedSize);
                }

                // 3.6 校验文件数量
                if (fileCount > MAX_FILE_COUNT) {
                    log.error("ZIP bomb detected: too many files. count:{}, limit:{}", fileCount, MAX_FILE_COUNT);
                    throw new AgentStudioException(StudioError.ZIP_FILE_SIZE_LARGE);
                }
            }
        }
    }

    /**
     * 校验ZIP条目名称是否恶意（防御路径遍历、空名称等）
     * @param entryName ZIP条目名称
     * @return true=恶意，false=合法
     */
    private static boolean isMaliciousEntryName(String entryName) {
        if (entryName == null || entryName.isEmpty()) {
            return true;
        }
        // 防御路径遍历（如 ../、..\、/../ 等）
        if (entryName.contains("..")
            || entryName.startsWith("/")
            || entryName.startsWith("\\")
            || entryName.contains("/../")
            || entryName.contains("\\..\\")) {
            return true;
        }
        // 防御超长名称（避免解析异常）
        return entryName.length() > 255;
    }

    /**
     * 安全获取ZIP条目的真实大小（绕过元数据，流式读取验证）
     * @param zip ZipFile对象
     * @param entry ZIP条目
     * @return 真实的未压缩大小
     * @throws IOException IO异常
     */
    private static long getRealEntrySize(ZipFile zip, ZipEntry entry) {
        long realSize = 0L;
        // 流式读取条目内容，统计真实大小（不解压到磁盘）
        try (InputStream in = zip.getInputStream(entry)) {
            byte[] buffer = new byte[STREAM_CHUNK_SIZE];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                realSize += bytesRead;
                // 超过单个文件阈值，直接终止（避免读取超大文件）
                if (realSize > MAX_SINGLE_UNCOMPRESSED_SIZE) {
                    break;
                }
            }
        } catch (IOException e) {
            log.error("Failed to read ZIP entry for size validation.");
            throw new AgentStudioException(StudioError.PROMPT_EVAL_DATA_FILE_ERROR);
        }
        // 处理元数据返回-1但真实大小合法的情况
        return realSize;
    }

    /**
     * 工具方法：计算目录/文件的层级（兼容Windows\和Linux/路径）
     */
    private static int calculateDirDepth(String entryName) {
        // 统一路径分隔符为/
        String normalizedPath = entryName.replace("\\", "/");
        // 分割路径，排除空字符串和当前目录（.）
        String[] pathParts = normalizedPath.split("/");
        int depth = 0;
        for (String part : pathParts) {
            if (!part.isEmpty() && !part.equals(".")) {
                depth++;
            }
        }
        // 若为目录（末尾带/），层级减1（如 "a/b/c/" 实际层级是2）
        if (entryName.endsWith("/") || entryName.endsWith("\\")) {
            depth--;
        }
        return Math.max(depth, 0); // 确保层级非负
    }

    /**
     * 工具方法：主动删除临时文件（校验失败时调用）
     */
    public static void deleteTempFile(File tempFile) {
        if (tempFile != null && tempFile.exists()) {
            boolean deleteSuccess = tempFile.delete();
            log.info("Temporary ZIP file deleted (validation failed). success:{}", deleteSuccess);
        }
    }

    /**
     * 判断是否为允许的图片文件
     */
    public static boolean isAllowedImage(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1) {
            return false;
        }
        String ext = fileName.substring(lastDot + 1).toLowerCase(Locale.ENGLISH);
        return ALLOWED_IMAGE_EXTENSIONS.contains(ext);
    }

}
