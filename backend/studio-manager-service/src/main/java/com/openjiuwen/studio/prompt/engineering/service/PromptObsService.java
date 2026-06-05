/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.service;

import com.obs.services.ObsClient;
import com.obs.services.exception.ObsException;
import com.obs.services.model.AccessControlList;
import com.obs.services.model.BucketVersioningConfiguration;
import com.obs.services.model.CreateBucketRequest;
import com.obs.services.model.DeleteObjectsRequest;
import com.obs.services.model.DeleteObjectsResult;
import com.obs.services.model.HttpMethodEnum;
import com.obs.services.model.ListBucketsRequest;
import com.obs.services.model.ListObjectsRequest;
import com.obs.services.model.ObjectListing;
import com.obs.services.model.ObjectMetadata;
import com.obs.services.model.ObsBucket;
import com.obs.services.model.ObsObject;
import com.obs.services.model.PutObjectRequest;
import com.obs.services.model.TemporarySignatureRequest;
import com.obs.services.model.TemporarySignatureResponse;
import com.obs.services.model.VersioningStatusEnum;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.prompt.engineering.constant.CommonConstant;
import com.openjiuwen.studio.prompt.engineering.dto.FileInfoVo;
import com.openjiuwen.studio.prompt.engineering.dto.GetObsObjectReq;
import com.openjiuwen.studio.prompt.engineering.dto.ObsObjectResp;
import com.openjiuwen.studio.prompt.engineering.dto.ObsResp;
import com.openjiuwen.studio.prompt.engineering.enums.FileType;
import com.openjiuwen.studio.prompt.engineering.utils.FileUtil;
import com.openjiuwen.studio.prompt.engineering.utils.HttpUtil;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;

/**
 * 功能描述
 *
 */
@Slf4j
@Service
public class PromptObsService {
    @Value("${obs.opensource:false}")
    private boolean opensource;

    @Value("${obs.ak}")
    private String accessKey;

    @Value("${obs.sk}")
    private String secretKey;

    @Value("${obs.url}")
    private String url;

    private static final String IMAGE_FOLDER = "image";

    @Value("${obs.bucket}")
    private String bucket;

    @Setter
    private volatile ObsClient obsClient;

    @PostConstruct
    public void init() throws IOException {
        try {
            obsClient = new ObsClient(accessKey, secretKey, url);
        } catch (ObsException e) {
            log.error("init obs client failed!", e);
            obsClient.close();
            throw new AgentStudioException(StudioError.OBS_RUNTIME_EXCEPTION);
        }
    }

    public List<String> listObjectKeys(String rootDir) {
        // 列举文件夹中的所有对象
        ListObjectsRequest request = new ListObjectsRequest(bucket);
        String prefix = org.apache.commons.lang3.StringUtils.isBlank(rootDir) ? "" : rootDir;
        request.setPrefix(prefix.endsWith("/") ? prefix : prefix + "/");
        request.setMaxKeys(1000);
        ObjectListing result = obsClient.listObjects(request);
        if (result.getObjects() == null) {
            return Collections.emptyList();
        }
        List<String> objectNames = new ArrayList<>(result.getObjects().size());
        for (ObsObject obj : result.getObjects()) {
            objectNames.add(obj.getObjectKey());
        }
        return objectNames;
    }


    public ObsObjectResp listObsObjects(String projectId, String workspaceId, GetObsObjectReq req)
        throws AgentStudioException {
        Map<String, List<ObsResp>> mapResp = new HashMap<>();
        ObsClient obsClient = null;
        try {
            obsClient = new ObsClient(accessKey, secretKey, url);
            ListObjectsRequest request = new ListObjectsRequest(req.getBucket());
            request.setDelimiter(CommonConstant.FOLDER_SEPARATOR);
            /* 如果path不以/结尾会出现如查询data返回data2/之类的错误文件夹信息 */
            String path = req.getPath();
            if (StringUtils.isNotEmpty(path) && !path.endsWith(CommonConstant.FOLDER_SEPARATOR)) {
                path = path + CommonConstant.FOLDER_SEPARATOR;
            }
            request.setPrefix(path);
            ObjectListing objects = obsClient.listObjects(request);
            List<ObsResp> folder = new ArrayList<>();
            for (String ele : objects.getCommonPrefixes()) {
                ObsResp obsResp = new ObsResp();
                obsResp.setFileName(ele);
                obsResp.setFileType("folder");
                folder.add(obsResp);
            }
            mapResp.put("folders", folder);
            List<ObsResp> files = new ArrayList<>();
            String finalPath = path;
            objects.getObjects().forEach(obsObject -> {
                int length = finalPath.length();
                if (!finalPath.equals(obsObject.getObjectKey())) {
                    ObsResp obsResp = new ObsResp();
                    obsResp.setFileName(obsObject.getObjectKey().substring(length));
                    obsResp.setLastModified(obsObject.getMetadata().getLastModified().getTime());
                    obsResp.setContentLength(obsObject.getMetadata().getContentLength());
                    obsResp.setFileType("file");
                    files.add(obsResp);
                }
            });
            mapResp.put("files", files);
            ObsObjectResp obsObjectResp = new ObsObjectResp();
            obsObjectResp.setObsObjectMap(mapResp);
            return obsObjectResp;
        } catch (ObsException e) {
            log.error("get obs bucket folders error, bucketName: {} {}", req.getBucket().replaceAll("[\r\n]", ""),
                e.getMessage());
            throw new AgentStudioException(StudioError.GET_OBS_BUCKET_ERROR, req.getBucket().replaceAll("[\r\n]", ""));
        } catch (RuntimeException e) {
            log.error("The Obs Service is not stable, error: {}", e.getMessage());
            throw new AgentStudioException(StudioError.OBS_SERVICE_NOT_STABLE);
        } finally {
            HttpUtil.safeClose(obsClient, "obs client");
        }
    }

    public FileInfoVo upLoadImage(String workspaceId, String projectId, MultipartFile file) {
        log.info("url: {}", this.url);
        // 文件校验
        FileUtil.validateFile(file, FileType.IMAGE);

        String fileId = UUID.randomUUID().toString();
        String objectKey = String.format("%s/%s", IMAGE_FOLDER, fileId);
        try {
            InputStream inputStream = file.getInputStream();
            uploadObsFile(inputStream, objectKey);
            String tempUrl = getObsTempUrl(objectKey, 3600L);
            return new FileInfoVo().setFileId(fileId).setTempUrl(tempUrl);
        } catch (Exception e) {
            log.error("upload image error, {}", e.getMessage());
            throw new AgentStudioException(StudioError.UPLOAD_FILE_ERROR);
        }
    }

    /**
     * 上传文件
     *
     * @param inputStream 待文件的输入流
     * @param objectKey key
     * @return 对象名
     */
    public void uploadObsFile(InputStream inputStream, String objectKey) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength((long) inputStream.available());
            obsClient.putObject(bucket, objectKey, inputStream, opensource ? metadata : null);
            log.info("upload obs file success, file path:{}", objectKey);
        } catch (ObsException | IOException e) {
            log.error("upload obs file failed, {}, opensource is {}", e.getMessage(), opensource);
            throw new AgentStudioException(StudioError.UPLOAD_FILE_TO_OBS_FAILED);
        }
    }

    public String getObsTempUrl(String objectKey, long expireSeconds) {
        TemporarySignatureRequest temporarySignatureRequest = new TemporarySignatureRequest(HttpMethodEnum.GET,
            expireSeconds);
        try {
            temporarySignatureRequest.setBucketName(bucket);
            temporarySignatureRequest.setObjectKey(objectKey);
            TemporarySignatureResponse temporarySignatureResponse = obsClient.createTemporarySignature(
                temporarySignatureRequest);
            return temporarySignatureResponse.getSignedUrl();
        } catch (Exception exception) {
            log.error("getObsTempUrl: getting temporary signature");
            throw new AgentStudioException(StudioError.DOWNLOAD_FILE_ERROR);
        }
    }

    /**
     * 上传OBS文件
     *
     * @param objectKey 文件路径
     * @param content 文件内容
     * @param expires 过期时间（单位：天）
     * @return 文件路径
     */
    public String uploadObsFile(String objectKey, String content, int expires) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
            return uploadObsFile(objectKey, inputStream, expires);
        } catch (ObsException | IOException e) {
            log.error("upload obs file failed!", e);
            throw new AgentStudioException(StudioError.OBS_FAILED);
        }
    }

    /**
     * 上传OBS文件
     *
     * @param objectKey 文件路径
     * @param inputStream 文件输入流
     * @param expires 过期时间（单位：天）
     * @return 文件路径
     */
    public String uploadObsFile(String objectKey, InputStream inputStream, int expires) {
        // 如不存在则新建桶
        creatBucketIfAbsent();

        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength((long) inputStream.available());
            PutObjectRequest request = new PutObjectRequest();
            request.setBucketName(bucket);
            request.setInput(inputStream);
            request.setMetadata(opensource ? metadata : null);
            request.setObjectKey(objectKey);
            if (expires > 0) {
                request.setExpires(expires);
            }

            obsClient.putObject(request);
            log.info("upload obs file success, file path:{}", objectKey);
            return objectKey;
        } catch (ObsException | IOException e) {
            log.error("upload obs file failed!", e);
            throw new AgentStudioException(StudioError.UPLOAD_FILE_TO_OBS_FAILED);
        }
    }

    /**
     * 下载OBS文件
     *
     * @param relativePath
     * @return
     */
    public String downloadObsFile(String relativePath) {
        String obsFile = null;
        InputStream inputStream = null;
        try {
            ObsObject obsObject = obsClient.getObject(bucket, relativePath);
            inputStream = obsObject.getObjectContent();
            if (inputStream != null) {
                obsFile = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            log.error("download file from obs failed!");
            throw new AgentStudioException(StudioError.OBS_FAILED);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        return obsFile;
    }

    private void creatBucketIfAbsent() {
        if (!obsClient.headBucket(bucket)) {
            try {
                CreateBucketRequest request = new CreateBucketRequest();
                request.setBucketName(bucket);

                // 设置桶访问权限为私有读写
                request.setAcl(AccessControlList.REST_CANNED_PRIVATE);
                obsClient.createBucket(request);
                // 开启多版本控制
                obsClient.setBucketVersioning(bucket, new BucketVersioningConfiguration(VersioningStatusEnum.ENABLED));
            } catch (ObsException e) {
                log.error("create obs bucket failed!");
                throw new AgentStudioException(StudioError.OBS_FAILED);
            }
        }
    }

    /**
     * 根据前缀删除obs对象
     *
     * @param prefix obs前缀
     * @return 删除结果
     */
    public DeleteObjectsResult deleteByPrefix(String prefix) {
        ListObjectsRequest request = new ListObjectsRequest(bucket);
        request.setPrefix(prefix);
        request.setMaxKeys(100);
        ObjectListing objectListing;
        DeleteObjectsResult sumDelResult = new DeleteObjectsResult();
        do {
            objectListing = obsClient.listObjects(request);
            DeleteObjectsRequest deleteRequest = new DeleteObjectsRequest(bucket);
            if (objectListing.getObjects().size() > 0) {
                for (ObsObject obj : objectListing.getObjects()) {
                    deleteRequest.addKeyAndVersion(obj.getObjectKey(), null);
                }
                DeleteObjectsResult delResult = obsClient.deleteObjects(deleteRequest);
                // 打印删除结果
                log.info("Batch delete OBS prefix:{}, success:{}. failed:{}.", prefix,
                    delResult.getDeletedObjectResults().size(), delResult.getErrorResults().size());
                sumDelResult.getDeletedObjectResults().addAll(delResult.getDeletedObjectResults());
                sumDelResult.getErrorResults().addAll(delResult.getErrorResults());
            }
        } while (objectListing != null && objectListing.isTruncated());
        log.info("Batch delete OBS summary prefix:{}, success:{}. failed:{}.", prefix,
            sumDelResult.getDeletedObjectResults().size(), sumDelResult.getErrorResults().size());
        return sumDelResult;
    }

    public void deleteObsFile(String deletePath) {
        try {
            if (obsClient.doesObjectExist(bucket, deletePath)) {
                obsClient.deleteObject(bucket, deletePath);
            }
        } catch (ObsException e) {
            log.error("delete file from obs failed!");
            throw new AgentStudioException(StudioError.OBS_FAILED);
        }
    }

    public Boolean isExistObsFile(String obsPath) {
        try {
            if (obsClient.doesObjectExist(bucket, obsPath)) {
                return true;
            }
        } catch (Exception e) {
            // 如果发生异常，说明文件不存在或发生错误
            return false;
        }
        return false;
    }

    public void copyFile(String oldPath, String newPath) {
        if (StringUtils.isBlank(oldPath) || StringUtils.isBlank(newPath)) {
            return;
        }
        try {
            obsClient.copyObject(bucket, oldPath, bucket, newPath);
        } catch (Exception e) {
            log.error("copy file from one to one failed!");
            throw new AgentStudioException(StudioError.OBS_FAILED);
        }
    }
}
