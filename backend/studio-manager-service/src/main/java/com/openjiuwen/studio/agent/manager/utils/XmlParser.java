/* * Copyright (c) Huawei Technologies Co., Ltd. 2021-2025. All rights reserved. */

package com.openjiuwen.studio.agent.manager.utils;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * 添加类的描述
 *
 */
@Slf4j
public class XmlParser {

    public static final String XML_FILE_SUFFIX = ".xml";

    private static final byte[] BUFFER = new byte[4096];

    /**
     * 解析压缩包内XML文件，新增【体积+文件数】双重压缩包炸弹校验
     * @param file 上传的压缩包文件（MultipartFile）
     * @param maxSize 最大允许解压体积（单位：MB）
     */
    public static void parseXml(MultipartFile file, long maxSize) {
        // 1. 原文件基础校验：空文件/原文件体积超阈值直接拒绝
        long maxSizeByte = maxSize * 1024 * 1024;
        if (file.isEmpty() || file.getSize() > maxSizeByte) {
            throw new AgentStudioException(StudioError.INTENT_IMPORT_FILE_SIZE_LIMIT, maxSize);
        }

        try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
            long totalUncompressedSize = 0L; // 全局实际解压体积计数器（字节）
            long totalFileCount = 0L; // 全局有效文件个数计数器（过滤目录，用long避免int溢出）
            SAXParser saxParser = createSafeSAXParser(); // 防XXE的安全SAX解析器
            ZipEntry entry;

            // 遍历压缩包内所有条目
            while ((entry = zis.getNextEntry()) != null) {
                try {
                    // 核心：过滤目录，仅对实际文件进行计数和校验（目录不占用文件数阈值）
                    if (!entry.isDirectory()) {
                        totalFileCount++; // 有效文件，计数器自增
                        // 即时校验文件数：超限立即抛异常，终止处理（防御大量小文件炸弹）
                        if (totalFileCount > 100) {
                            log.error("Compressed file count exceeds limit, actual: {}, max allowed: {}",
                                totalFileCount, 100);
                            throw new AgentStudioException(StudioError.FILE_COUNT_EXCEED_LIMIT, 100);
                        }
                    }

                    // 流式读取当前条目内容，实时累加实际解压体积
                    long entryReadSize = 0L;
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    int len;
                    while ((len = zis.read(BUFFER)) != -1) {
                        baos.write(BUFFER, 0, len);
                        entryReadSize += len;
                        totalUncompressedSize += len;

                        // 即时校验体积：超限立即抛异常，避免继续消耗资源
                        if (totalUncompressedSize > maxSizeByte) {
                            log.error("Compressed file uncompressed size exceeds limit, actual: {}MB, max allowed: {}MB",
                                totalUncompressedSize / 1024 / 1024, maxSize);
                            throw new AgentStudioException(StudioError.INTENT_IMPORT_FILE_SIZE_LIMIT, maxSize);
                        }
                    }

                    // 仅解析XML文件，原有业务逻辑完全保留
                    if (entry.getName().endsWith(XML_FILE_SUFFIX) && !entry.isDirectory()) {
                        try (InputStream entryStream = new ByteArrayInputStream(baos.toByteArray())) {
                            saxParser.parse(new InputSource(entryStream), new DefaultHandler());
                        }
                    }
                } finally {
                    // 强制关闭当前ZipEntry，释放资源，避免流泄漏
                    zis.closeEntry();
                }
            }

            log.info("Compressed file parse success, total files: {}, total uncompressed size: {}MB",
                totalFileCount, totalUncompressedSize / 1024 / 1024);
        } catch (AgentStudioException e) {
            // 业务自定义异常（体积/文件数超限）直接抛出，不重复包装
            log.error("Business exception when parsing compressed file: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            // 其他异常（解析失败/流异常等）包装为业务文件错误
            log.error("Failed to parse xml from compressed file, error: {}", e.getLocalizedMessage(), e);
            throw new AgentStudioException(StudioError.INTENT_IMPORT_FILE_ERROR);
        }
    }


    private static SAXParser createSafeSAXParser() throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        return factory.newSAXParser();
    }
}
