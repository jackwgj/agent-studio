/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.utils;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.obs.services.ObsClient;
import com.obs.services.ObsConfiguration;
import com.obs.services.exception.ObsException;
import com.obs.services.model.ObsObject;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.annotation.PostConstruct;

/**
 * OBS工具类
 *
 */
@Slf4j
@Component
public class ObsUtil {
    // 文件最大允许解压后总大小 (10MB)
    private static final long MAX_TOTAL_SIZE = 10L * 1024 * 1024;

    // 单个文件最大大小 (5MB)
    private static final long MAX_ENTRY_SIZE = 5L * 1024 * 1024;

    static final int BUFFER = 512;

    /**
     * 连接超时时间
     */
    private static final int CONNECTION_TIMEOUT = 10000;

    private static final int SOCKET_TIMEOUT = 30000;

    private static ObsClient obsClient;

    private static String obsEndpoint;

    @Value("${obs.endpoint}")
    private String appObsEndpoint;

    @Value("${obs.ak}")
    private String accessKey;

    @Value("${obs.sk}")
    private String secretKey;

    @Value("${obs.url}")
    private String url;

    @Value("${obs.path.style}")
    private String pathStyle;

    private ObsUtil() {
    }

    @PostConstruct
    public void init() {
        obsEndpoint = appObsEndpoint;
    }

    /**
     * obsClient初始化
     */
    public void init(String xAuthToken) {
        ObsConfiguration config = new ObsConfiguration();
        config.setEndPoint(url);
        config.setPathStyle(pathStyle.equals("path"));
        ObsUtil.obsClient = new ObsClient(accessKey, secretKey, config);
    }

    /**
     * 设置ak sk
     *
     * @param ak
     * @param sk
     * @param obsConfiguration
     */
    public void setObsClient(String ak, String sk, String secToken, ObsConfiguration obsConfiguration) {
        ObsConfiguration config = new ObsConfiguration();
        config.setEndPoint(url);
        config.setPathStyle(pathStyle.equals("path"));
        ObsUtil.obsClient = new ObsClient(accessKey, secretKey, config);
    }

    /**
     * 获取初始化客户端
     *
     * @return
     */
    public ObsClient getObsClient(String xAuthToken) {
        init(xAuthToken);
        return obsClient;
    }

    /**
     * 检查桶是否存在
     *
     * @param bucket 桶名
     * @return
     * @throws AgentStudioException
     */
    public boolean hasBucket(String xAuthToken, String bucket) throws AgentStudioException {
        try {
            init(xAuthToken);
            if (!obsClient.headBucket(bucket)) {
                return false;
            }
        } catch (ObsException e) {
            log.error("The bucket is not exist, bucketName: {} , error: {}", bucket, e.getMessage());
            throw new AgentStudioException(StudioError.OBS_ACCESS_EXCEPTION, bucket);
        } catch (RuntimeException e) {
            log.error("The Obs Service is not stable, bucketName: {} , runtime error: {}", bucket, e.getMessage());
            throw new AgentStudioException(StudioError.OBS_SERVICE_NOT_STABLE);
        } finally {
            HttpUtil.safeClose(obsClient, "obs client");
        }
        return true;
    }

    /**
     * 检查桶内文件是否存在
     *
     * @param bucket 桶名
     * @param filePath 文件路径
     * @return
     * @throws AgentStudioException
     */
    public boolean hasFile(String xAuthToken, String bucket, String filePath) throws AgentStudioException {
        try {
            init(xAuthToken);
            if (!obsClient.doesObjectExist(bucket, filePath)) {
                return false;
            }
        } catch (ObsException e) {
            log.error("The obs file is not exist, filePath: {} , error: {}", filePath, e.getMessage());
            throw new AgentStudioException(StudioError.OBS_FILE_NOT_EXIST, filePath);
        } catch (RuntimeException e) {
            log.error("The Obs Service is not stable, bucketName: {} , runtime error: {}", bucket, e.getMessage());
            throw new AgentStudioException(StudioError.OBS_SERVICE_NOT_STABLE);
        } finally {
            HttpUtil.safeClose(obsClient, "obs client");
        }
        return true;
    }

    /**
     * 下载OBS上的文件
     *
     * @param bucket 桶名
     * @param filePath 文件路径
     * @return InputStream 文件流
     */
    public InputStream downloadObsFile(String xAuthToken, String bucket, String filePath) {
        InputStream objectContent = null;
        try {
            if (!hasBucket(xAuthToken, bucket)) {
                log.error("The obs bucket does not exist.");
                throw new AgentStudioException(StudioError.OBS_BUCKET_DOES_NOT_EXIST);
            }
            init(xAuthToken);
            log.info("Downloading the obs object");

            // 校验obs文件zip炸弹安全场景
            InputStream checkContent = obsClient.getObject(bucket, filePath, null).getObjectContent();
            checkZipBomb(checkContent);
            ObsObject obsObject = obsClient.getObject(bucket, filePath, null);
            objectContent = obsObject.getObjectContent();
        } catch (ObsException e) {
            log.error("Response Code: {}\nError Message: {}\nError Code: {}", e.getResponseCode(), e.getErrorMessage(),
                e.getErrorCode());
            throw new AgentStudioException(StudioError.OBS_ACCESS_FAILED_EXCEPTION);
        } catch (RuntimeException e) {
            log.error("download obs file failed, error: {}", e.getMessage());
            throw new AgentStudioException(StudioError.OBS_DOWNLOAD_FILE_FAILED);
        } finally {
            HttpUtil.safeClose(obsClient, "obs client");
        }
        return objectContent;
    }

    /**
     * 检查zip炸弹
     *
     * @param is is
     */
    public void checkZipBomb(InputStream is) {
        ZipInputStream zis = null;
        try {
            zis = new ZipInputStream(is);
            ZipEntry entry;
            int zipSize = 0;
            long totalSize = 0L;
            while ((entry = zis.getNextEntry()) != null) {
                int count;
                byte data[] = new byte[BUFFER];
                // 检查压缩文件数
                zipSize++;
                if (zipSize > 20) {
                    log.error("zip file size is too large");
                    throw new AgentStudioException(StudioError.ZIP_FILE_SIZE_LARGE);
                }

                // 使用 Commons IO 规范化路径
                String safePath = FilenameUtils.normalize(entry.getName());
                if (safePath == null || safePath.startsWith("..")) {
                    log.error("Invalid file path: {}", entry.getName());
                    throw new AgentStudioException(StudioError.INVALID_FILE_PATH, entry.getName());
                }

                // 跳过目录条目，不进行读取
                if (entry.isDirectory()) {
                    continue;
                }

                // 检查单个文件大小
                long singleRead = 0;
                while ((count = zis.read(data, 0, BUFFER)) != -1) {
                    singleRead += count;
                    if (singleRead > MAX_ENTRY_SIZE) {
                        log.error("file entry size is too large, name: {} ,size:{}", entry.getName(), singleRead);
                        throw new AgentStudioException(StudioError.FILE_ENTRY_SIZE_LARGE, entry.getName(), singleRead);
                    }

                    // 统计总大小
                    totalSize += count;
                    if (totalSize > MAX_TOTAL_SIZE) {
                        log.error("file size is too large, size: {}", totalSize);
                        throw new AgentStudioException(StudioError.FILE_SIZE_TOO_LARGE, totalSize);
                    }
                }

                zis.closeEntry();
            }
        } catch (IOException ioException) {
            log.error("obs file check zip bomb failed, {}", ioException.getMessage());
            throw new AgentStudioException(StudioError.OBS_FILE_CHECK_ZIP_BOMB);
        } finally {
            if (Objects.nonNull(is)) {
                try {
                    is.close();
                } catch (IOException e) {
                    log.error("input stream close failed.");
                }
            }
            if (Objects.nonNull(zis)) {
                try {
                    zis.close();
                } catch (IOException e) {
                    log.error("zip input stream close failed.");
                }
            }
        }
    }
}
