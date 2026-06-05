/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.utils;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.prng.SP800SecureRandomBuilder;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.Normalizer;

/**
 * UsUriUtils工具类
 *
 */
public class UsCommonUtils {

    /**
     * URI类型
     */
    public static final String URI = "URI";

    private static SecureRandom SECURE_RANDOM;

    private static final int CIPHER_LEN = 256;

    private static final int ENTROPY_BITS_REQUIRED = 384;


    /**
     * uri归一化
     *
     * @param requestURI request中的uri
     * @return 合法的path
     */
    public static String normalizeURI(String requestURI) {
        return normalize(requestURI, true, URI);
    }

    private static String normalize(String path, boolean isCheckMultipleUrlEncoding, String type) {
        if (StringUtils.isEmpty(path)) {
            return path;
        }
        // 归一化
        String normalized = normalizeStringLimitLen(path);
        // 移除路径参数
        normalized = removePathParameters(normalized);
        // 反编码
        normalized = decode(path, normalized, isCheckMultipleUrlEncoding, type);
        // 移除路径参数
        normalized = removePathParameters(normalized);
        // 处理"\", "//", "/./" and "/../"
        return getNormalString(normalized);
    }

    /**
     * 字符串归一化处理
     *
     * @param str 输入字符串
     * @return 归一化后的字符串
     */
    public static String normalizeStringLimitLen(String str) {
        if (StringUtils.isNotEmpty(str)) {
            return Normalizer.normalize(str, Normalizer.Form.NFKC);
        }
        return str;
    }

    /**
     * 移除路径参数
     * 处理如 ;jsessionid=xxx 这样的路径参数
     *
     * @param path 路径
     * @return 移除路径参数后的路径
     */
    private static String removePathParameters(String path) {
        if (StringUtils.isEmpty(path)) {
            return path;
        }
        // 移除路径参数（分号后面直到下一个斜杠或结尾的内容）
        int semicolonIndex = path.indexOf(';');
        if (semicolonIndex == -1) {
            return path;
        }
        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < path.length()) {
            if (path.charAt(i) == ';') {
                // 找到分号，跳过直到下一个斜杠或结尾
                int nextSlash = path.indexOf('/', i + 1);
                if (nextSlash == -1) {
                    // 没有下一个斜杠，移除分号后的所有内容
                    break;
                } else {
                    // 有下一个斜杠，直接跳到下一个斜杠位置
                    i = nextSlash;
                    continue;
                }
            }
            result.append(path.charAt(i));
            i++;
        }
        return result.toString();
    }

    /**
     * URL解码
     *
     * @param originalPath 原始路径
     * @param decodedPath  已解码的路径
     * @param isCheckMultipleUrlEncoding 是否检查多次编码
     * @param type 归一化的path类型
     * @return 解码后的路径
     */
    private static String decode(String originalPath, String decodedPath,
            boolean isCheckMultipleUrlEncoding, String type) {
        if (StringUtils.isEmpty(decodedPath)) {
            return decodedPath;
        }
        String result = UriUtils.decode(decodedPath, StandardCharsets.UTF_8.name());
        // 检查多次编码
        if (isCheckMultipleUrlEncoding && containsDoubleEncoding(result)) {
            // 如果检测到多次编码，进行额外解码
            result = UriUtils.decode(result, StandardCharsets.UTF_8.name());
        }
        return result;
    }

    /**
     * 检查是否包含双重编码
     *
     * @param path 路径
     * @return 是否包含双重编码
     */
    private static boolean containsDoubleEncoding(String path) {
        if (StringUtils.isEmpty(path)) {
            return false;
        }
        // 检查常见的编码模式，如 %25 表示 % 的编码
        return path.contains("%25");
    }

    /**
     * 处理"\", "//", "/./" and "/../"
     *
     * @param path 路径
     * @return 归一化后的路径
     */
    private static String getNormalString(String path) {
        if (StringUtils.isEmpty(path)) {
            return path;
        }
        // 先处理反斜杠
        String result = path.replace('\\', '/');

        // 处理多个连续斜杠 "//"
        while (result.contains("//")) {
            result = result.replace("//", "/");
        }

        // 处理 "/./"
        while (result.contains("/./")) {
            result = result.replace("/./", "/");
        }

        // 处理 "/../" - 需要回退目录
        result = resolveDotDot(result);

        // 处理开头和结尾的斜杠（保留根路径 "/"）
        if (result.length() > 1) {
            if (result.startsWith("/")) {
                result = result.substring(1);
            }
            if (result.endsWith("/") && result.length() > 1) {
                result = result.substring(0, result.length() - 1);
            }
        }

        return result;
    }

    /**
     * 处理 "/../" 模式
     *
     * @param path 路径
     * @return 处理后的路径
     */
    private static String resolveDotDot(String path) {
        if (StringUtils.isEmpty(path)) {
            return path;
        }
        StringBuilder result = new StringBuilder();
        String[] segments = path.split("/");
        for (String segment : segments) {
            if ("..".equals(segment)) {
                // 回退到上一级目录
                int lastSlash = result.lastIndexOf("/");
                if (lastSlash > 0) {
                    result.delete(lastSlash, result.length());
                } else if (lastSlash == 0) {
                    // 根目录，不能再回退
                    result.delete(1, result.length());
                } else {
                    // 没有父目录，保留 ".."
                    if (result.length() > 0) {
                        result.append("/");
                    }
                    result.append("..");
                }
            } else if (!".".equals(segment) && !segment.isEmpty()) {
                if (result.length() > 0) {
                    result.append("/");
                }
                result.append(segment);
            }
        }
        return result.toString();
    }

    @SuppressWarnings("deprecation")
    public static SecureRandom getInstance() throws NoSuchAlgorithmException {
        if (null == SECURE_RANDOM) {
            SecureRandom source = SecureRandom.getInstanceStrong();
            boolean predictionResistant = true; // /dev/random可以认为是活熵源
            // NID_aes_256_ctr
            BlockCipher cipher = new AESEngine();
            // 生成nonce 用于和熵源共同生成seed，可以为null
            byte[] nonce = null;
            boolean reSeed = false; // 是否每次取完随机数都重新刷新熵源，否则根据算法不同定期刷新
            SECURE_RANDOM = new SP800SecureRandomBuilder(source, predictionResistant).setEntropyBitsRequired(
                ENTROPY_BITS_REQUIRED).buildCTR(cipher, CIPHER_LEN, nonce, reSeed);
        }
        return SECURE_RANDOM;
    }
}
