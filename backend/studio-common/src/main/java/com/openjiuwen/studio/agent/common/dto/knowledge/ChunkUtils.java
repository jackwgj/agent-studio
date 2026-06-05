/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.knowledge;

import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 切片处理的工具类
 *
 * @since 2025-09-01
 */
public class ChunkUtils {

    private static final String IMAGE_ID_PATTERN_STR = "\\{(img-[a-z0-9-]+)}";

    private static final Pattern IMAGE_ID_PATTERN = Pattern.compile(IMAGE_ID_PATTERN_STR, Pattern.CASE_INSENSITIVE);

    /**
     * 和前端约定检索中的图片格式：![img](https://agent_arts_knowledge_img_url/%s)
     * %s 为图片在Redis中的缓存Key，用于映射真实图片资源，通过redis缓存过期机制控制图片访问期限
     */
    private static final String RETRIEVAL_IMAGE_FORMAT = "![img](https://agent_arts_knowledge_img_url/%s)";

    private static final Pattern IMAGE_FORMAT_PATTERN = Pattern.compile(
        "!\\[img\\]\\(https://agent_arts_knowledge_img_url/([A-Za-z0-9\\-_]+)\\)");

    private static final String IMAGE_URI_PATH = "/v1/%s/agent-manager/knowledges/%s/image/%s";

    /**
     * 从切片内容中提取并且移除图片的ID
     * (适用于KooSearch中的切片内容)
     *
     * @param chunk
     * @return
     */
    public static List<String> extractImageId(String chunk) {
        List<String> ids = new ArrayList<>();
        if (StringUtils.isBlank(chunk)) {
            return ids;
        }
        // 2. 每次 new 一个 Matcher，开销极小
        Matcher matcher = IMAGE_ID_PATTERN.matcher(chunk);
        while (matcher.find()) {
            ids.add(matcher.group(1));
        }
        return ids;
    }

    /**
     * 从切片内容中移除图片的ID
     * (适用于KooSearch中的切片内容)
     *
     * @param chunk
     * @return
     */
    public static String removeImageId(String chunk) {
        return IMAGE_ID_PATTERN.matcher(chunk).replaceAll("");
    }

    /**
     * 将原文本中的图片id替换成imageIdAccess，作为Redis中的缓存Key
     *
     * @param chunk
     * @param retrieveImage 是否召回图片
     * @return
     */
    public static ChunkImageFormat replaceImageAccessKeyAndFormat(String chunk, boolean retrieveImage) {

        Map<String, String> imageIdMap = new HashMap<>();
        List<String> imageIds = new ArrayList<>();
        if (StringUtils.isBlank(chunk)) {
            // 不处理知识库ID为空这种非正常场景，防止返回内容达不到预期
            return ChunkImageFormat.builder().chunkResult(chunk).imageAccessMaps(imageIdMap).imageIds(imageIds).build();
        }
        if (!retrieveImage) {
            // 若不需检索图片，直接移除图片ID占位符并返回
            return ChunkImageFormat.builder()
                .chunkResult(removeImageId(chunk))
                .imageAccessMaps(imageIdMap)
                .imageIds(imageIds)
                .build();
        }
        Matcher matcher = IMAGE_ID_PATTERN.matcher(chunk);
        StringBuilder stringBuilder = new StringBuilder();
        while (matcher.find()) {
            String imageId = matcher.group(1);
            // 生成imageIdAccess，作为Redis中的缓存Key
            String imageIdAccess = generateImageAccessKey();
            imageIdMap.put(imageId, imageIdAccess);
            // 将imageId替换成imageIdAccess
            String replacement = String.format(RETRIEVAL_IMAGE_FORMAT, imageIdMap.get(imageId));
            matcher.appendReplacement(stringBuilder, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(stringBuilder);
        return ChunkImageFormat.builder()
            .chunkResult(stringBuilder.toString())
            .imageAccessMaps(imageIdMap)
            .imageIds(imageIds)
            .build();
    }

    private static String generateImageAccessKey() {
        String uuid = UUID.randomUUID().toString();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(uuid.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 将内部检索接口包装过图片的返回内容提取成占位符和图片列表
     * 占位符格式为 {KI-<index>}，index 对应 imageIds 列表索引
     *
     * @param content
     * @return
     */
    public static ChunkImageFormat extractChunkImgForOpenApi(String content, ImageMaskPolicy type) {
        List<String> imageIds = new ArrayList<>();
        // 1. 处理输入为空或空白字符串的情况
        if (StringUtils.isBlank(content)) {
            return ChunkImageFormat.builder().chunkResult(content).imageIds(imageIds).build();
        }

        Matcher matcher = IMAGE_FORMAT_PATTERN.matcher(content);
        StringBuilder resultBuffer = new StringBuilder();
        int placeholderCount = 0;
        while (matcher.find()) {
            String imageAccessKey = matcher.group(1);
            imageIds.add(imageAccessKey);
            String replacement = switch (type) {
                case RETAIN_IMAGE_ID ->
                    // 格式：{KI|imageAccessKey}
                    "{KI|" + imageAccessKey + "}";
                case RETAIN_PLACEHOLDER ->
                    // 格式：{KI|N}，N为序号
                    "{KI|" + placeholderCount + "}";
                default ->
                    // 默认为空
                    "";
            };
            matcher.appendReplacement(resultBuffer, Matcher.quoteReplacement(replacement));
            placeholderCount++;
        }
        matcher.appendTail(resultBuffer);
        return ChunkImageFormat.builder().chunkResult(resultBuffer.toString()).imageIds(imageIds).build();
    }

    /**
     * 生成切片中图片下载地址
     *
     * @param projectId
     * @param knowledgeBaseId
     * @param imageId
     * @return
     */
    public static String generateChunkImageUriPath(String projectId, String knowledgeBaseId, String imageId) {
        return String.format(IMAGE_URI_PATH, projectId, knowledgeBaseId, imageId);
    }
}
