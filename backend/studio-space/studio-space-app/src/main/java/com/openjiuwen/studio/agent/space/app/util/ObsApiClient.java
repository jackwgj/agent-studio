/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.util;

import com.obs.services.ObsClient;
import com.obs.services.ObsConfiguration;
import com.obs.services.exception.ObsException;
import com.obs.services.model.HttpMethodEnum;
import com.obs.services.model.ObjectMetadata;
import com.obs.services.model.ObsObject;
import com.obs.services.model.TemporarySignatureRequest;
import com.obs.services.model.TemporarySignatureResponse;
import com.openjiuwen.studio.agent.space.common.exception.AgentSpaceErrorCodes;
import com.openjiuwen.studio.agent.space.common.exception.AgentSpaceException;
import com.openjiuwen.studio.agent.space.common.utils.StringUtil;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 开发中心OBS桶client类
 * 保存、查询接口
 */
@Slf4j
@Component
public class ObsApiClient {
    @Value("${agent.builder.obs.endPoint}")
    private String endPoint;

    @Value("${agent.builder.obs.ak}")
    private String ak;

    @Value("${agent.builder.obs.sk}")
    private String sk;

    private volatile ObsClient obsClient;

    @PostConstruct
    public void initObsApiClient() {
        ObsConfiguration config = new ObsConfiguration();
        config.setSocketTimeout(30000);
        config.setConnectionTimeout(10000);
        config.setEndPoint(endPoint);
        try {
            obsClient = new ObsClient(ak, sk, config);
        } catch (ObsException ex) {
            log.error("init obs client failed!", ex);
            if (obsClient != null) {
                try {
                    obsClient.close();
                } catch (IOException e) {
                    log.error("init obs client failed!", ex);
                    throw new AgentSpaceException(AgentSpaceErrorCodes.AGENT_BUILDER_OBS_EXECUTE_ERROR, ex);
                }
            }
            throw new AgentSpaceException(AgentSpaceErrorCodes.AGENT_BUILDER_OBS_EXECUTE_ERROR, ex);
        }
    }

    @Scheduled(fixedDelay = 60 * 60 * 1000)
    public void refreshObsClient() throws IOException {
        ObsConfiguration config = new ObsConfiguration();
        config.setSocketTimeout(30000);
        config.setConnectionTimeout(10000);
        config.setEndPoint(endPoint);
        try {
            log.info("refresh Obs Client start!");
            synchronized (this) {
                if (obsClient == null) {
                    log.error("obsClient is null, refresh obs client failed!");
                    throw new AgentSpaceException(AgentSpaceErrorCodes.AGENT_BUILDER_OBS_EXECUTE_ERROR);
                }
            }
            log.info("refresh Obs Client succeed!");
        } catch (ObsException e) {
            log.error("refresh obs client failed!", e.getMessage(), e);
            if (obsClient != null) {
                obsClient.close();
            }
            throw new AgentSpaceException(AgentSpaceErrorCodes.AGENT_BUILDER_OBS_EXECUTE_ERROR, e);
        }
    }

    /**
     * 下载数据
     *
     * @param bucketName 桶名
     * @return obs对象 数据流
     */
    public ObsObject downloadFile(String bucketName, String obsKey) {
        try {
            return obsClient.getObject(bucketName, obsKey);
        } catch (Exception ex) {
            log.error("[downloadFile] download file from obs is error, error is {}", ex.getMessage() + ex.getCause());
            throw new AgentSpaceException(AgentSpaceErrorCodes.AGENT_BUILDER_OBS_EXECUTE_ERROR,
                "download file from obs is error");
        }
    }

    /**
     * 上传文件
     */
    public String uploadFile(String bucketName, String directory, FileTransferUtils.AgentBuilderFile file,
        ObjectMetadata meta) {
        String obsKey = directory + file.getOriginalFilename();
        if (!directory.endsWith(StringUtil.SLASHES_STRING)) {
            obsKey = directory + StringUtil.SLASHES_STRING + file.getOriginalFilename();
        }
        try {
            obsClient.putObject(bucketName, obsKey, file.getInputStream(), meta);
        } catch (Exception ex) {
            log.error("file {} upload to obs failed, error detail is: {}", file.getOriginalFilename(),
                ex.getMessage() + ex.getCause());
            throw new AgentSpaceException(AgentSpaceErrorCodes.AGENT_BUILDER_OBS_EXECUTE_ERROR,
                "file upload to obs failed");
        }
        return obsKey;
    }

    public String uploadFile(String bucketName, String directory, FileTransferUtils.AgentBuilderFile file) {
        return uploadFile(bucketName, directory, file, null);
    }

    /**
     * 获取下载指定对象的临时URL
     *
     * @param objectName 对象名，不包含桶名
     * @param expires    临时URL的过期时间（秒）
     * @return TemporarySignatureResponse
     */
    public TemporarySignatureResponse getTemporaryGetRsp(String bucket, String objectName, long expires) {
        try {
            TemporarySignatureRequest request = new TemporarySignatureRequest(HttpMethodEnum.GET, expires);
            request.setBucketName(bucket);
            request.setObjectKey(objectName);
            return obsClient.createTemporarySignature(request);
        } catch (Exception e) {
            log.error("Get temporary getRsp failed, error is {}", e);
            throw new AgentSpaceException(AgentSpaceErrorCodes.AGENT_BUILDER_OBS_EXECUTE_ERROR);
        }
    }

    /**
     * 安全复制obs对象
     *
     * @param srcBucketName  源桶
     * @param srcObsKey      源路径
     * @param destBucketName 目标桶
     * @param destObsKey     目标路径
     * @return copy结果
     */
    public boolean safeCopyObsObject(String srcBucketName, String srcObsKey, String destBucketName, String destObsKey) {
        try {
            copyObsObject(srcBucketName, srcObsKey, destBucketName, destObsKey);
            return true;
        } catch (Exception ex) {
            log.warn("ObsService: copy objects {} from obs bucket {} to {} failed. ex is {}", srcObsKey, srcBucketName,
                destBucketName, ex.getMessage() + " " + ex.getCause());
            return false;
        }
    }

    public void copyObsObject(String srcBucketName, String srcObsKey, String destBucketName, String destObsKey) {
        try {
            obsClient.copyObject(srcBucketName, srcObsKey, destBucketName, destObsKey);
        } catch (Exception ex) {
            log.error("ObsService: copy objects {} from obs bucket {} to {} failed. ex is {}", srcObsKey, srcBucketName,
                destBucketName, ex);
            throw new AgentSpaceException(AgentSpaceErrorCodes.AGENT_BUILDER_OBS_EXECUTE_ERROR,
                "copy objects from obs failed");
        }
    }

    /**
     * 安全删除
     *
     * @param bucketName 桶
     * @param directory  路径
     * @return 是否删除完成
     */
    public boolean safeDeleteObsObject(String bucketName, String directory) {
        if (StringUtils.isEmpty(bucketName) || StringUtils.isEmpty(directory)) {
            log.warn(
                "ObsService: delete all objects in directory failed, due to lack of bucket name or directory. bucket name is {}, directory is {}",
                bucketName, directory);
            // 不存在则为删了
            return true;
        }
        try {
            return deleteObsObject(bucketName, directory);
        } catch (Exception ex) {
            log.warn("delete obs object failed, error detail is: {}", ex.getMessage() + " " + ex.getCause());
            return false;
        }
    }

    public boolean deleteObsObject(String bucketName, String objectKey) {
        if (StringUtils.isEmpty(bucketName) || StringUtils.isEmpty(objectKey)) {
            log.error(
                "ObsService: delete all objects in directory failed, due to lack of bucket name or directory. bucket name is {}, directory is {}",
                bucketName, objectKey);
            throw new AgentSpaceException(AgentSpaceErrorCodes.AGENT_BUILDER_OBS_EXECUTE_ERROR,
                "invalid bucket name or directory");
        }
        try {
            obsClient.deleteObject(bucketName, objectKey);
            return true;
        } catch (Exception ex) {
            log.error("ObsService: delete objects {} from obs bucket {} failed. ex is {}", objectKey, bucketName,
                ex.getMessage() + " " + ex.getCause());
            throw new AgentSpaceException(AgentSpaceErrorCodes.AGENT_BUILDER_OBS_EXECUTE_ERROR,
                "delete object from obs failed");
        }
    }
}
