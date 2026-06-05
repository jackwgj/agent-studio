/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.service.v2.fileparser;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.prompt.engineering.dto.PromptEvalDataItem;
import com.openjiuwen.studio.prompt.engineering.entity.v2.PromptVar;
import com.openjiuwen.studio.prompt.engineering.enums.v2.PtTypeEnum;
import com.openjiuwen.studio.prompt.engineering.service.PromptObsService;
import com.openjiuwen.studio.prompt.engineering.utils.PromptDataSetUtil;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Slf4j
@Component
public class ZipFileParser extends DataSetFileParser {

    private static final Set<String> SUPPORTED_EXTENSIONS = new HashSet<>(Arrays.asList("xlsx", "xls", "json"));

    private static final String PICTURE_DIR = "picture/";

    private static final String EVAL_PICTURE_PATH = "EvalData/%s/%s/%s/picture/%s";

    @Value("${prompt.tempurl.time: 2592000}") // 一个月
    private long EXPIRE_TIME;

    @Value("${prompt.evaldata.vars.picturesize: 10}")
    private int PICTURE_SIZE;

    @Autowired
    private PromptObsService obsService;

    @Autowired
    private ExcelFileParser excelFileParser;

    @Autowired
    private JsonFileParser jsonFileParser;

    @Override
    public String type() {
        return "zip";
    }

    @Override
    public List<List<PromptVar>> parseData(MultipartFile file, Map<String, PromptVar> varsConfig, String projectId,
        String workspaceId, String taskId, int restNum) {
        File tempZipFile = null;
        MultipartFile targetFile = null;

        List<List<PromptVar>> promptVarsList = null;
        List<PromptEvalDataItem> promptEvalDataItemList = null;

        try {
            // 步骤1：保存为临时压缩包
            tempZipFile = PromptDataSetUtil.saveToTempFile(file);

            // 步骤2：解析压缩包，获取外层的Excel/JSON文件
            targetFile = extractOuterExcelOrJson(tempZipFile);
            if (ObjectUtils.isEmpty(targetFile)) {
                throw new AgentStudioException(StudioError.FILE_DOES_NOT_EXIST);
            }
            String ext = FilenameUtils.getExtension(targetFile.getName());

            promptVarsList = switch (ext) {
                case "xlsx", "xls" ->
                    excelFileParser.parseData(targetFile, varsConfig, projectId, workspaceId, taskId, restNum);
                case "json" ->
                    jsonFileParser.parseData(targetFile, varsConfig, projectId, workspaceId, taskId, restNum);
                default -> {
                    log.error("unsupported file type, please check file format! taskId: {}", taskId);
                    throw new AgentStudioException(StudioError.PROMPT_DATA_TYPE_ERROR);
                }
            };
            // 步骤3：上传图片和获取EvalData
            uploadImagesAndGetEvalData(tempZipFile, promptVarsList, varsConfig, projectId, workspaceId, taskId);

        } catch (AgentStudioException e) {
            throw e;
        } catch (IOException e) {
            log.error("deal zip file occur error, taskId : {}", taskId);
            throw new AgentStudioException(StudioError.PROMPT_EVAL_DATA_FILE_ERROR);
        } catch (Exception e) {
            log.error("deal zip file occur error, please check zip format! taskId: {}", taskId);
            throw new AgentStudioException(StudioError.PROMPT_EVAL_DATA_FILE_ERROR);
        } finally {
            // 清理临时压缩包（可选，若已处理完成）
            PromptDataSetUtil.deleteTempFile(tempZipFile);
        }

        return promptVarsList;
    }

    public void uploadImagesAndGetEvalData(File tempZipFile, List<List<PromptVar>> promptVarsList,
        Map<String, PromptVar> varConfigMap, String projectId, String workspaceId, String taskId) throws IOException {

        // 从 data.json 中提取需要的图片文件名
        Set<String> requiredImages = acquireNeedPicture(promptVarsList, varConfigMap);

        // 上传图片
        Map<String, String> imageName2UUID = uploadPictureFromzip(requiredImages, tempZipFile, projectId, workspaceId,
            taskId);

        for (List<PromptVar> promptVars : promptVarsList) {
            for (PromptVar promptVar : promptVars) {
                if (varConfigMap.containsKey(promptVar.getName())) {
                    if (varConfigMap.get(promptVar.getName()).getType() == PtTypeEnum.MULTI
                        && PromptDataSetUtil.isAllowedImage(promptVar.getContent())) {
                        String fileName = FilenameUtils.getName(promptVar.getContent());
                        // 图片上传后文件名被替换成了UUID，通过映射找到对应的obs文件
                        if (imageName2UUID.containsKey(fileName)) {
                            String tempUrl = obsService.getObsTempUrl(
                                String.format(EVAL_PICTURE_PATH, projectId, workspaceId, taskId,
                                    imageName2UUID.get(fileName)), EXPIRE_TIME);
                            promptVar.setContent(tempUrl);
                        }
                    }
                }
            }
        }
    }

    public Map<String, String> uploadPictureFromzip(Set<String> requiredImages, File tempZipFile, String projectId,
        String workspaceId, String taskId) {
        Map<String, String> imageName2UUID = new HashMap<>();
        if (!requiredImages.isEmpty()) {
            log.info("data.json contains  pictures need upload!, start upload!");
            try (ZipFile zipFile = new ZipFile(tempZipFile)) {
                // 遍历压缩包中 picture 目录下的所有图片
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    String entryName = entry.getName().replace("\\", "/"); // 标准化路径

                    // 只处理 picture 目录下的文件
                    if (entry.isDirectory() || !entryName.startsWith(PICTURE_DIR)) {
                        continue;
                    }

                    // 提取文件名（如 "picture/xxx.jpg" -> "xxx.jpg"）
                    String fileName = entryName.substring(PICTURE_DIR.length());
                    if (!requiredImages.contains(fileName)) {
                        continue; // 跳过 JSON 中未引用的图片
                    }

                    long uncompressedRealSize = 0L;
                    try (InputStream tempIn = zipFile.getInputStream(entry)) {
                        // 读取流并统计字节数（Apache Commons IO工具类，也可自行实现）
                        byte[] fileBytes = IOUtils.toByteArray(tempIn);
                        uncompressedRealSize = fileBytes.length;

                        double uncompressedSizeMB = uncompressedRealSize / (1024.0 * 1024.0);
                        // 校验解压后的真实大小（根据业务选择压缩后/解压后大小）
                        if (uncompressedSizeMB > PICTURE_SIZE) {
                            log.error("upload picture size is more than {}MB!, ignore upload", PICTURE_SIZE);
                            continue;
                        }

                        // 重新构造输入流（因为tempIn已读取完毕，需用字节数组重建）
                        try (InputStream in = new ByteArrayInputStream(fileBytes)) {
                            String UUIDName = UUID.randomUUID().toString() + '.' + FilenameUtils.getExtension(fileName);
                            String objectKey = String.format(EVAL_PICTURE_PATH, projectId, workspaceId, taskId,
                                UUIDName);
                            obsService.uploadObsFile(objectKey, in, -1);
                            imageName2UUID.put(fileName, UUIDName);
                            log.info("success image !");
                        }
                    } catch (Exception e) {
                        log.error("upload picture occur error, {}", e.getMessage(), e);
                    }
                }
            } catch (Exception e) {
                log.error("upload picture occur error, {}", e.getMessage(), e);
                throw new AgentStudioException(StudioError.UPLOAD_FILE_TO_OBS_FAILED);
            }
        }
        return imageName2UUID;
    }

    public Set<String> acquireNeedPicture(List<List<PromptVar>> promptVarsList, Map<String, PromptVar> varConfigMap) {
        Set<String> requiredImages = new HashSet<>();

        for (List<PromptVar> PromptVars : promptVarsList) {
            for (PromptVar promptVar : PromptVars) {
                if (varConfigMap.containsKey(promptVar.getName())
                    && varConfigMap.get(promptVar.getName()).getType() == PtTypeEnum.MULTI) {
                    if (PromptDataSetUtil.isAllowedImage(promptVar.getContent())) {
                        String fileName = FilenameUtils.getName(promptVar.getContent());
                        requiredImages.add(fileName);
                    }
                }
            }
        }

        return requiredImages;
    }

    /**
     * 从压缩包中提取最外层的Excel/JSON文件
     *
     * @param zipFile 压缩包临时文件
     * @return 提取出的目标文件（临时文件）
     * @throws IOException IO异常
     */
    public MultipartFile extractOuterExcelOrJson(File zipFile) {
        try (ZipFile zip = new ZipFile(zipFile)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            File tempFile = null;

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                // 过滤目录和嵌套文件（只处理最外层）
                if (entry.isDirectory() || entry.getName().contains("/") || entry.getName().contains("\\")) {
                    continue;
                }
                String ext = FilenameUtils.getExtension(entry.getName());
                // 检查是否为支持的文件类型
                if (SUPPORTED_EXTENSIONS.contains(ext)) {
                    // 创建临时文件保存解压后的内容
                    try (InputStream is = zip.getInputStream(entry)) {
                        return new MockMultipartFile(entry.getName(),          // 表单字段名（可自定义）
                            entry.getName(),          // 原始文件名
                            getContentType(ext),      // 文件Content-Type
                            is);                       // ZIP内文件输入流
                    }
                } else {
                    log.error("unsupported file type, please check file format!");
                    throw new AgentStudioException(StudioError.PROMPT_DATA_TYPE_ERROR);
                }
            }
            return null;
        } catch (AgentStudioException e) {
            throw e;
        } catch (IOException e) {
            log.error("extract outer file occur error, please check zip format!");
            throw new AgentStudioException(StudioError.PROMPT_EVAL_DATA_FILE_ERROR);
        }
    }

    /**
     * 根据扩展名获取Content-Type
     */
    public String getContentType(String ext) {
        return switch (ext.toLowerCase(Locale.ROOT)) {
            case "xlsx", "xls" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "json" -> "application/json";
            default -> "application/octet-stream";
        };
    }

}
