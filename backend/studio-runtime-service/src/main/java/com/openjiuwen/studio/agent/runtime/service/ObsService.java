/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service;

import com.obs.services.ObsClient;
import com.obs.services.ObsConfiguration;
import com.obs.services.exception.ObsException;
import com.obs.services.model.AccessControlList;
import com.obs.services.model.BucketStorageInfo;
import com.obs.services.model.BucketVersioningConfiguration;
import com.obs.services.model.CreateBucketRequest;
import com.obs.services.model.HttpMethodEnum;
import com.obs.services.model.ListObjectsRequest;
import com.obs.services.model.ObjectListing;
import com.obs.services.model.ObjectMetadata;
import com.obs.services.model.ObsObject;
import com.obs.services.model.PutObjectRequest;
import com.obs.services.model.TemporarySignatureRequest;
import com.obs.services.model.TemporarySignatureResponse;
import com.obs.services.model.VersioningStatusEnum;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.service.CommonObsService;
import com.openjiuwen.studio.agent.runtime.constant.Constant;
import com.openjiuwen.studio.agent.runtime.utils.AlarmLogUtil;
import com.openjiuwen.studio.agent.runtime.utils.OkHttpUtils;

import com.openjiuwen.studio.agent.common.utils.CryptoUtils;

import io.micrometer.common.util.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.PostConstruct;

/**
 * obs服务
 *
 */
@Slf4j
@Component
public class ObsService implements CommonObsService {
    @Value("${obs.ak}")
    private String accessKey;

    @Value("${obs.opensource:false}")
    private boolean opensource;

    @Value("${obs.sk}")
    private String secretKey;

    @Value("${obs.url}")
    private String url;

    @Value("${obs.bucket}")
    @Getter
    private String bucket;

    @Value("${obs.staging-bucket}")
    @Getter
    private String stagingBucket;

    @Value("${obs.bucket.auto-create:true}")
    private boolean isBucketAutoCreate;

    @Value("${obs.path.style}")
    private String pathStyle;

    @Setter
    private volatile ObsClient obsClient;

    @Autowired
    private OkHttpUtils okHttpUtils;

    @Autowired
    private AlarmLogUtil alarmLogUtil;

   @PostConstruct
    public void init() throws IOException {
        secretKey = CryptoUtils.decrypt(secretKey);
        try {
            ObsConfiguration config = new ObsConfiguration();
            config.setEndPoint(url);
            // 静态代码检查G.EXP.04：equals常量前置，避免pathStyle为null时抛出NullPointerException
            config.setPathStyle("path".equals(pathStyle));
            obsClient = new ObsClient(accessKey, secretKey, config);
        } catch (ObsException e) {
            log.error("init obs client failed!", e);
            alarmLogUtil.logAlarm("OBS", "init obs client failed! url: " + url, e.getErrorMessage());
            obsClient.close();
            throw new AgentStudioException(StudioError.UNEXPECTED_ERROR);
        }
        // 如不存在则新建桶
        if (isBucketAutoCreate) {
            creatBucketIfAbsent(bucket);
            creatBucketIfAbsent(stagingBucket);
        }
    }

    @Override
    public void putObject(String objectKey, String content) {
        putObject(null, objectKey, content);
    }

    @Override
    public void deleteObject(String objectKey) {
        try {
            if (obsClient.doesObjectExist(bucket, objectKey)) {
                obsClient.deleteObject(bucket, objectKey);
            }
        } catch (ObsException e) {
            log.error("delete file from obs failed!");
            alarmLogUtil.logAlarm("OBS", "delete file from obs failed! url:" + url, e.getErrorMessage());
            throw new AgentStudioException(StudioError.OBS_FAILED);
        }
    }

    @Override
    public String getObject(String objectKey) {
        return getObject(null, objectKey).get(Constant.Obs.CONTENT);
    }

    /**
     * 从OBS下载文件内容（UTF_8编码的文本文件）
     *
     * @param objectKey OBS对象地址
     * @return OBS文件内容
     */
    public String downloadObsFile(String objectKey) {
        long startTime = System.currentTimeMillis();
        ObsObject obsObject = obsClient.getObject(bucket, objectKey);
        try (InputStream inputStream = obsObject.getObjectContent()) {
            return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("download file from obs failed!");
            alarmLogUtil.logAlarm("OBS", "download file from obs failed! url:" + url, e.getMessage());
            throw new AgentStudioException(StudioError.OBS_FAILED);
        } finally {
            log.info("downloadObsFile:{}, cost: {} ms", objectKey, System.currentTimeMillis() - startTime);
        }
    }

    public void putObject(String basePath, String objectKey, String content) {
        long startTime = System.currentTimeMillis();

        ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength((long) inputStream.available());
        String finalObjKey = getObsObjectName(basePath, objectKey);
        obsClient.putObject(bucket, finalObjKey, inputStream, opensource ? metadata : null);
        log.info("putObject:{}, cost: {} ms", finalObjKey, System.currentTimeMillis() - startTime);
    }

    /**
     * 上传文件到obs
     *
     * @param objectPath 文件路径
     * @param inputStream 文件输入流
     * @param expires 过期时间（天）
     * @return 文件路径
     */
    public String putObject(String objectPath, InputStream inputStream, int expires) {
        return putObjectToBucket(bucket, objectPath, inputStream, expires);
    }

    public String putObjectToBucket(String bucketName, String objectPath, InputStream inputStream, int expires) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength((long) inputStream.available());
            PutObjectRequest request = new PutObjectRequest();
            request.setBucketName(bucketName);
            request.setInput(inputStream);
            request.setMetadata(opensource ? metadata : null);
            request.setObjectKey(objectPath);
            if (expires > 0) {
                request.setExpires(expires);
            }

            obsClient.putObject(request);
            log.info("upload obs file success, file path:{}", objectPath);
            return objectPath;
        } catch (ObsException | IOException e) {
            log.error("upload obs file failed!", e);
            alarmLogUtil.logAlarm("OBS", "upload obs file failed! url:" + url, e.getMessage());
            throw new AgentStudioException(StudioError.OBS_FAILED);
        }
    }

    public Map<String, String> getObject(String basePath, String objectKey) {
        long startTime = System.currentTimeMillis();
        Map<String, String> result = new HashMap<>();
        String finalObjKey = getObsObjectName(basePath, objectKey);

        // 校验obs桶中资源是否存在
        if (!obsClient.doesObjectExist(bucket, finalObjKey)) {
            log.error("OBS_ACCESS_FAILED, resource is not exist! bucket is {} ", bucket);
            throw new AgentStudioException(StudioError.OBS_FAILED);
        }
        ObsObject obsObject = obsClient.getObject(bucket, finalObjKey);
        String content = null;
        try (InputStream contentStream = obsObject.getObjectContent()) {
            if (contentStream != null) {
                content = IOUtils.toString(contentStream, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            log.error("download file from obs failed!");
            alarmLogUtil.logAlarm("OBS", "download file from obs failed!", e.getMessage());
            throw new AgentStudioException(StudioError.OBS_FAILED);
        }
        result.put(Constant.Obs.CONTENT, content);
        result.put(Constant.Obs.MD5, obsObject.getMetadata().getContentMd5());
        log.info("getObject:{}, cost: {} ms", finalObjKey, System.currentTimeMillis() - startTime);
        return result;
    }

    /**
     * 根据临时url从obs获取文件流
     *
     * @param url 临时url
     * @return InputStream
     */
    public InputStream getByUrl(String url) {
        long startTime = System.currentTimeMillis();
        InputStream inputStream;
        try {
            Request.Builder builder = new Request.Builder();
            Request httpRequest = builder.url(url).get().build();
            OkHttpClient httpClient = okHttpUtils.getHttpClient();
            Response rsp = httpClient.newCall(httpRequest).execute();
            if (rsp.body() == null) {
                log.error("File is empty.");
                alarmLogUtil.logAlarm("OBS", "File is empty.", "File is empty.");
                throw new AgentStudioException(StudioError.FILE_NOT_EXIST);
            }
            inputStream = rsp.body().byteStream();
        } catch (IOException e) {
            log.error("Get inputStream from obs by url failed.", e);
            alarmLogUtil.logAlarm("OBS", "Get inputStream from obs by url failed. url: " + url, e.getMessage());
            throw new AgentStudioException(StudioError.GET_BY_URL_FAILED);
        } finally {
            log.info("getByUrl cost: {} ms", System.currentTimeMillis() - startTime);
        }
        return inputStream;
    }

    public String getMd5(String basePath, String objectKey) {
        long startTime = System.currentTimeMillis();
        String finalObjKey = getObsObjectName(basePath, objectKey);
        ObsObject obsObject = obsClient.getObject(bucket, finalObjKey);
        String md5 = obsObject.getMetadata().getContentMd5();
        log.info("getMd5:{}, cost: {} ms", finalObjKey, System.currentTimeMillis() - startTime);
        return md5;
    }

    private String getObsObjectName(String basePath, String obsKey) {
        if (StringUtils.isEmpty(basePath)) {
            return obsKey;
        } else {
            return basePath + "/" + obsKey;
        }
    }

    private void creatBucketIfAbsent(String bucketName) {
        long startTime = System.currentTimeMillis();
        if (!obsClient.headBucket(bucketName)) {
            try {
                CreateBucketRequest request = new CreateBucketRequest();
                request.setBucketName(bucketName);
                // 设置桶访问权限为私有读写
                request.setAcl(AccessControlList.REST_CANNED_PRIVATE);
                obsClient.createBucket(request);
                // 开启多版本控制
                obsClient.setBucketVersioning(bucketName,
                    new BucketVersioningConfiguration(VersioningStatusEnum.ENABLED));
            } catch (ObsException e) {
                log.error("create obs bucket {} failed!", bucketName);
                alarmLogUtil.logAlarm("OBS", "create obs bucket failed!. bucketName: " + bucketName,
                    e.getErrorMessage());
                throw new AgentStudioException(StudioError.OBS_FAILED);
            } finally {
                log.info("creatBucketIfAbsent {} cost: {} ms", bucketName, System.currentTimeMillis() - startTime);
            }
        } else {
            log.info("creatBucketIfAbsent {} cost: {} ms", bucketName, System.currentTimeMillis() - startTime);
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

    /**
     * 上传文件，预设过期时间
     *
     * @param inputStream 待文件的输入流
     * @param fileName 文件名
     * @param expires 过期时间（天）
     * @return 对象名
     */
    public String uploadObsFileWithExpires(InputStream inputStream, String fileName, int expires) {
        String objectKey = String.format("%s/%s", Constant.FILE, fileName);
        return putObject(objectKey, inputStream, expires);
    }

    /**
     * 查询桶存量
     *
     * @return long
     */
    public long bucketStorage() {
        BucketStorageInfo bucketStorageInfo = obsClient.getBucketStorageInfo(bucket);
        if (Objects.isNull(bucketStorageInfo)) {
            return 0L;
        }

        // 值为非负整数，单位为Byte（字节）。
        return bucketStorageInfo.getSize();
    }

    /**
     * 上传文件到中转桶，临时桶，供外部网络使用
     *
     * @param inputStream 文件输入流
     * @param fileName 文件名
     * @param expires 过期时间
     * @return 文件访问链接
     */
    public String uploadToStagingWithPublicRead(InputStream inputStream, String fileName, int expires) {
        String objectKey = String.format("%s/%s", Constant.FILE, fileName);

        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength((long) inputStream.available());

            PutObjectRequest request = new PutObjectRequest();
            if (expires > 0) {
                request.setExpires(expires);
            }

            request.setBucketName(stagingBucket);
            request.setInput(inputStream);
            request.setMetadata(opensource ? metadata : null);
            request.setObjectKey(objectKey);
            request.setAcl(AccessControlList.REST_CANNED_PUBLIC_READ);

            obsClient.putObject(request);
            String readUrl = url + "/" + stagingBucket + "/" + objectKey;
            log.info("upload obs file with public read success, file path:{}", readUrl);
            return readUrl;
        } catch (ObsException | IOException e) {
            log.error("upload obs file with public read failed!", e);
            alarmLogUtil.logAlarm("OBS", "upload obs file with public read failed!", e.getMessage());
            throw new AgentStudioException(StudioError.OBS_FAILED);
        }
    }

    /**
     * 上传文件到中转桶，临时桶，供外部网络使用
     *
     * @param inputStream 文件输入流
     * @param fileName 文件名
     * @param expires 有效期
     * @return 访问链接
     */
    public String uploadToStagingWithExpires(InputStream inputStream, String fileName, int expires) {
        String objectKey = String.format("%s/%s", Constant.FILE, fileName);
        return putObjectToBucket(stagingBucket, objectKey, inputStream, expires);
    }

    /**
     * 获取中转桶下载指定对象的临时URL
     *
     * @param objectName 对象名，不包含桶名
     * @param expires 临时URL的过期时间（秒）
     * @return TemporarySignatureResponse
     */
    public TemporarySignatureResponse getStagingTemporaryGetRsp(String objectName, long expires) {
        try {
            TemporarySignatureRequest request = new TemporarySignatureRequest(HttpMethodEnum.GET, expires);
            request.setBucketName(stagingBucket);
            request.setObjectKey(objectName);
            return obsClient.createTemporarySignature(request);
        } catch (ObsException e) {
            log.error("Get obs temporary signature url failed.", e);
            alarmLogUtil.logAlarm("OBS", "Get obs temporary signature url failed.", e.getErrorMessage());
            throw new AgentStudioException(StudioError.GET_OBS_TEMPORARY_URL_FAILED);
        }
    }
}
