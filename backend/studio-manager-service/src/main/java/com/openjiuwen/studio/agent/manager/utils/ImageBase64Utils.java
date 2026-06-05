/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.utils;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.stereotype.Component;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

/**
 * obs中图片转base64工具
 *
 */

@Slf4j
@Component
public class ImageBase64Utils {

    private static final Pattern BASE64_IMAGE_PATTERN = Pattern.compile(
 "^data:image/(?:png|jpeg|jpg|svg\\+xml);base64,[A-Za-z0-9+/=\\r\\n]+$"
    );

    /**
     * 获取 icon 的图片，并转为 Base64 编码
     *
     * @param iconName 图片名称
     * @param mgObsService OBS 服务实例
     * @return Base64 编码后的图片字符串
     */
    public static String getImageBase64(String iconName, MgObsService mgObsService) {
        if (iconName == null) {
            throw new AgentStudioException(StudioError.INVALID_ICON_NAME);
        }
        if (mgObsService == null) {
            throw new AgentStudioException(StudioError.OBS_FAILED);
        }
        try {
            // 将OBS加载的图片，进行base64编码
            String base64Image = mgObsService.downloadObsImageFile(iconName);

            // 获取MIME类型
            String extension = iconName.substring(iconName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
            String mimeType = switch (extension) {
                case "png" -> "image/png";
                case "jpeg", "jpg" -> "image/jpeg";
                case "gif" -> "image/gif";
                default -> "image/svg+xml";
            };

            return String.format("data:%s;base64,%s", mimeType, base64Image);

        } catch (Exception e) {
            log.error("get icon image failed!", e);
            throw new AgentStudioException(StudioError.FAILED_ICON_IMAGE);
        }
    }

    /**
     *
     * @description 验证图片格式
     * @param dataUri 图片的url
     * @date 10:46 2025/8/16
     * @return boolean
     **/
    public static boolean isValidBase64Image(String dataUri) {
        Matcher matcher = BASE64_IMAGE_PATTERN.matcher(dataUri);
        return matcher.matches();
    }

    /**
     *
     * @description 图片大小限制
     * @param dataUri 图片的url
     * @param maxSizeKB 现在大小
     * @date 11:04 2025/8/16
     **/
    public static void validateImage(String dataUri, int maxSizeKB) {
        if (StringUtils.isBlank(dataUri)) {
            throw new AgentStudioException(StudioError.ILLEGAL_IMAGE_FORMAT);
        }
        if (!Strings.CS.startsWith(dataUri, "data:image/")) {
            dataUri = StringUtils.join("data:image/png;base64,", dataUri);
        }
        if (!isValidBase64Image(dataUri)) {
            throw new AgentStudioException(StudioError.ILLEGAL_IMAGE_FORMAT);
        }
        String base64Data = dataUri.split(",")[1];
        int maxSizeBytes = maxSizeKB * 1024;
        base64Data = base64Data.replaceAll("[^A-Za-z0-9+/=]", "");
        try (InputStream decodedStream = Base64.getDecoder().wrap(new ByteArrayInputStream(base64Data.getBytes()))) {
            byte[] buffer = new byte[4096];
            long totalBytes = 0;
            while (true) {
                int bytesRead = decodedStream.read(buffer);
                if (bytesRead == -1) break;
                totalBytes += bytesRead;
                if (totalBytes > maxSizeBytes) {
                    throw new AgentStudioException(StudioError.ILLEGAL_IMAGE_FORMAT);
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            throw new AgentStudioException(StudioError.IMAGE_EXCEEDS_LIMIT);
        }
    }

    /**
     *
     * @description 图片大小限制 默认1MB
     * @param dataUri 图片的url
     * @date 11:04 2025/8/16
     **/
    public static void validateImage(String dataUri) {
        validateImage(dataUri, 1024);
    }

    /**
     * 将maas返回的Base64编码的PNG图片缩放到指定尺寸，并返回缩放后的Base64字符串
     *
     * @param base64Image 原始图片的Base64 字符串
     * @param targetWidth  目标宽度
     * @param targetHeight 目标高度
     * @return 缩放后的图片Base64 字符串
     */
    public static String resizeBase64Image(String base64Image, int targetWidth, int targetHeight) {
        try {
            final String prefix = "data:image/png;base64,";
            base64Image = base64Image.substring(base64Image.indexOf(',') + 1);
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);

            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));

            // 创建目标图像（支持透明通道）
            BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
            // 高质量缩放
            Graphics2D g2d = resizedImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
            g2d.dispose();

            // 将缩放后的图像写入字节数组（PNG 格式）
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(resizedImage, "png", baos);
            byte[] resizedImageBytes = baos.toByteArray();

            return prefix + Base64.getEncoder().encodeToString(resizedImageBytes);

        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to process Base64 image data", e);
        }
    }
}
