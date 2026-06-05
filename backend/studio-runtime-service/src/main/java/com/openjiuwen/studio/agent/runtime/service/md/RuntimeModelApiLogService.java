/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.md;

import com.openjiuwen.studio.agent.common.utils.JsonUtils;
import com.openjiuwen.studio.agent.common.utils.LanguageUtils;
import com.openjiuwen.studio.agent.runtime.model.ModelApiLog;
import com.openjiuwen.studio.agent.runtime.model.SecurityCenterLog;
import com.openjiuwen.studio.agent.runtime.service.ObsService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RuntimeModelApiLogService {
    private static final String FORMAT = "yyyyMMddHHmmss";

    private static final Object LOCK = new Object();

    @Value("${model.security.report:false}")
    private boolean modelContentReport;

    @Value("${model.security.allreport:false}")
    private boolean allReportOpen;

    @Value("${model.security.record.pathPrefix:chat/completions/content/}")
    private String prefix;

    @Value("${model.security.record.keepNum:200}")
    private int keepNum;

    @Value("${model.security.record.localPath:}")
    private String inferLogsPath;

    @Autowired
    @Qualifier("securityCheckThreadPool")
    private ThreadPoolTaskExecutor executor;

    @Autowired(required = false)
    private ObsService obsService;

    private final String hostAddress;

    private String fileName;

    private OutputStream logFileOutputStream;

    private final BlockingQueue<String> logQueue = new ArrayBlockingQueue<>(10000);

    private final String region = System.getenv("common_region_id");

    private int counter;

    public RuntimeModelApiLogService() {
        hostAddress = getHostAddress();
    }

   // @PostConstruct
    public void postConstruct() {
        if (!modelContentReport) {
            return;
        }
        fileName = new SimpleDateFormat(FORMAT).format(new Date()) + "_" + hostAddress + ".txt";
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            try {
                syncLogFileToObs();
            } catch (Exception e) {
                log.warn("RuntimeModelApiLogService Fail handler model invoke logger report. {}", e.getMessage());
            }
        }, 1, 60, TimeUnit.SECONDS);

        try {
            if (!new File(inferLogsPath).exists()) {
                Files.createDirectories(Paths.get(inferLogsPath));
            }
        } catch (Exception e) {
            log.error("RuntimeModelApiLogService Fail init inferLogsPath. inferLogsPath:{}", inferLogsPath, e);
        }

        Executors.newSingleThreadExecutor().execute(this::saveLogToLocalFile);
    }

    /**
     * 模型调用记录
     *
     * @param apiLog 模型调用数据
     */
    public void saveModelApiLog(ModelApiLog apiLog) {
        executor.execute(() -> {
            try {
                saveModelChatContent(apiLog);
            } catch (Exception e) {
                log.warn("Save model api log fail. {}", e.getMessage());
            }
        });
    }

    private String getHostAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            log.warn("RuntimeModelApiLogService Fail get local host. {}", e.getMessage());
            return UUID.randomUUID().toString().substring(0, 6);
        }
    }

    private void saveModelChatContent(ModelApiLog apiLog) {
        if (!allReportOpen && !apiLog.isFreeModel()) {
            return;
        }
        saveSecurityCenterLog(apiLog);
    }

    public void saveSecurityCenterLog(ModelApiLog apiLog) {
        if (!modelContentReport) {
            return;
        }
        SecurityCenterLog data = new SecurityCenterLog().setId(UUID.randomUUID().toString())
            .setModel(apiLog.getModelName())
            .setModelId(apiLog.getModelId())
            .setInput(apiLog.getInput())
            .setOutput(apiLog.getOutput())
            .setHostIp(apiLog.getIpAddress())
            .setEndTimestamp(System.currentTimeMillis())
            .setStartTimestamp(apiLog.getStartTime())
            .setReason(apiLog.getReason())
            .setStatus(apiLog.getStatus())
            .setSessionId(apiLog.getRequestId())
            .setTenantId(apiLog.getDomainId())
            .setUserId(apiLog.getUserId())
            .setOutputLang(LanguageUtils.getLanguage())
            .setRegion(region);
        logQueue.add(JsonUtils.encode(data));
    }

    void syncLogFileToObs() {
        String oldFileName = fileName;
        ++counter;
        synchronized (LOCK) {
            fileName = new SimpleDateFormat(FORMAT).format(new Date()) + "_" + hostAddress + ".txt";
            closeResource(logFileOutputStream);
            try {
                logFileOutputStream = new FileOutputStream(inferLogsPath + "/" + fileName);
            } catch (Exception e) {
                log.error("RuntimeModelApiLogService Fail create input stream. ", e);
            }
        }

        if (counter > 10) {
            counter = 0;
            clearObsFiles();
        }

        File[] files = new File(inferLogsPath).listFiles();
        if (files == null || files.length == 0) {
            log.warn("RuntimeModelApiLogService No log file to sync to obs.");
            return;
        }
        File logFile = new File(inferLogsPath + "/" + oldFileName);
        if (!logFile.exists()) {
            log.info("RuntimeModelApiLogService Log file is not exist. {}", logFile.getName());
        } else {
            uploadToObs(logFile);
        }

        Arrays.sort(files, Comparator.comparingLong(File::lastModified));
        for (int idx = 0; idx < files.length - 30; idx++) {
            boolean ans = files[idx].delete();
            if (!ans) {
                log.warn("RuntimeModelApiLogService Delete {} fail", files[idx].getName());
            }
        }
    }

    private void uploadToObs(File file) {
        try (InputStream input = new FileInputStream(file)) {
            obsService.uploadObsFileWithExpires(input, prefix + file.getName(), 1);
            log.info("WiseApiLogsServiceImpl Success upload to obs. {}", prefix + file.getName());
        } catch (Exception e) {
            log.error("WiseApiLogsServiceImpl Upload file to obs fail. {}", file.getName(), e);
        }
    }

    void clearObsFiles() {
        try {
            List<String> files = new ArrayList<>(obsService.listObjectKeys("file/chat/contents/"));
            if (files.isEmpty()) {
                log.warn("RuntimeModelApiLogService Obs files is empty.");
                return;
            }
            files.sort((o1, o2) -> {
                String v1 = o1.substring(0, o1.lastIndexOf('_'));
                String v2 = o2.substring(0, o2.lastIndexOf('_'));
                return v2.compareTo(v1);
            });
            log.info("RuntimeModelApiLogService Start to clear obs files. files num:{}. first:{} last:{} keepNum:{}",
                files.size(), files.get(0), files.get(files.size() - 1), keepNum);
            if (files.size() <= keepNum) {
                return;
            }
            for (String file : files.subList(keepNum, files.size())) {
                try {
                    obsService.deleteObject(file);
                } catch (Exception e) {
                    log.warn("RuntimeModelApiLogService Delete obs file fail. {}", e.getMessage());
                }
            }

            log.info("RuntimeModelApiLogService Success clear {} file. ", files.size() - keepNum);
        } catch (Exception e) {
            log.error("RuntimeModelApiLogService Fail clear obs file.", e);
        }
    }

    void closeResource(Closeable resource) {
        if (resource == null) {
            return;
        }
        try {
            resource.close();
        } catch (Exception e) {
            log.warn("RuntimeModelApiLogService close resource fail. {}", e.getMessage());
        }
    }

    private void saveLogToLocalFile() {
        while (true) {
            try {
                String value = logQueue.take();
                synchronized (LOCK) {
                    if (logFileOutputStream == null) {
                        continue;
                    }
                    logFileOutputStream.write(value.getBytes(StandardCharsets.UTF_8));
                    logFileOutputStream.write("\n".getBytes(StandardCharsets.UTF_8));
                }
            } catch (Exception e) {
                log.error("RuntimeModelApiLogService Fail take value from block queue. {}", e.getMessage());
                threadSleep();
            }
        }
    }

    void threadSleep() {
        try {
            Thread.sleep(1000);
        } catch (Exception e2) {
            log.warn("RuntimeModelApiLogService Thread sleep fail. {}", e2.getMessage());
        }
    }
}
