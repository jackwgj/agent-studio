/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.sensitive;

import com.openjiuwen.studio.agent.common.bo.AgentMetadata;
import com.openjiuwen.studio.agent.common.utils.LanguageUtils;
import com.openjiuwen.studio.agent.common.utils.SpringBeanUtils;
import com.openjiuwen.studio.agent.runtime.service.security.SecurityService;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * 敏感词辅助类
 *
 */
@Slf4j
@SuppressWarnings("checkstyle: all")
public class SensitiveTrieUtils {
    private static final String EN_ERROR_MSG
            = "Sorry, this question does not meet the safety and compliance requirements and cannot be answered at this time.";

    private static final String ZH_ERROR_MSG = "抱歉，该问题不符合安全合规要求，暂时无法回答";

    private static final String KEY_BLOCK = "block";

    private SensitiveTrie inputReplyTrie = null;

    private SensitiveTrie outputMixedTrie = null;

    // 缓冲区大小，消息长度累积到 miniBufferLength 后开始匹配
    private final int miniBufferLength;

    // 流式匹配中，保存已参与匹配但未确定结果部分(敏感词前缀)
    private final Map<String, StringBuilder> lastBufferMap = new HashMap<>();

    // 流式块缓存，缓存到达一定大小后再开始匹配
    private final Map<String, StringBuilder> streamBufferMap = new HashMap<>();

    // 缓存匹配处理完成的部分
    private final Map<String, Integer> matchedOffsetMap = new HashMap<>();

    // 缓存匹配完成且已经处理过的流式块
    private final Map<String, StringBuilder> matchedStreamMap = new HashMap<>();

    private boolean isContentReviewEnable;

    private final boolean isSafetyBarrierEnabled;

    private final SecurityService securityService;

    private final String message;

    public SensitiveTrieUtils(String appType, AgentMetadata metadata) {
        miniBufferLength = Integer.parseInt(SpringBeanUtils.getProperty("sensitive.buffer.length", "1"));
        if (metadata.isContentReviewEnable()) {
            SensitiveTrieCache cache = SpringBeanUtils.getBean(SensitiveTrieCache.class);
            SensitiveTrieCache.SensitiveTrieWrapper trieWrapper = cache.getSensitiveTrieWrapper(appType, metadata);
            isContentReviewEnable = trieWrapper.isEnabled;
            inputReplyTrie = trieWrapper.inputReplyTrie;
            outputMixedTrie = trieWrapper.outputMixedTrie;
        }
        this.isSafetyBarrierEnabled = metadata.isSafetyBarrier();
        this.securityService = SpringBeanUtils.getBean(SecurityService.class);
        message = LanguageUtils.isChinese() ? ZH_ERROR_MSG : EN_ERROR_MSG;
    }

    /**
     * 输入输出匹配
     *
     * @param text 待匹配输入文本
     * @param isInput 是否输入匹配
     * @return 匹配结果 Pair<#Integer, #String> left:匹配进行过的替换操作，right:匹配后的结果
     */
    public Pair<Integer, String> handleSensitiveMatch(@Nullable String text, boolean isInput) {
        if (StringUtils.isBlank(text)) {
            return Pair.of(0, text);
        }
        Pair<Integer, String> result = Pair.of(0, text);
        if (isContentReviewEnable) {
            result = isInput ? inputReplyTrie.match(text) : outputMixedTrie.match(text);
        }
        return result;
    }

    /**
     * 流式输出匹配
     *
     * @param text 待匹配输出文本
     * @param identifier 标识符，标识不同组件或节点
     * @param isFinished 匹配是否结束
     * @return MatchResultWrapper 本次匹配结果, op > 0 表示命中敏感词
     */
    public MatchResultWrapper handleOutputSensitiveMatch(@NotNull String text, @NotNull String identifier,
                                                         boolean isFinished) {
        MatchResultWrapper matchResultWrapper = matchContentReview(text, identifier, isFinished);
        return matchSafetyBarrier(matchResultWrapper, identifier, isFinished);
    }

    /**
     * 查看指定 id 的消息组是否在匹配过程中
     *
     * @param identifier 标记不同消息组
     * @return true/false
     */
    public boolean isMatchingWithId(String identifier) {
        return streamBufferMap.containsKey(identifier);
    }

    public boolean isInputMatchEnabled() {
        return (isContentReviewEnable && inputReplyTrie != null && !inputReplyTrie.isEmpty()) || isSafetyBarrierEnabled;
    }

    public boolean isOutputMatchEnabled() {
        return (isContentReviewEnable && outputMixedTrie != null && !outputMixedTrie.isEmpty())
                || isSafetyBarrierEnabled;
    }

    private MatchResultWrapper matchSafetyBarrier(MatchResultWrapper matchResultWrapper, @NotNull String identifier,
                                                  boolean isFinished) {
        if (isFinished) {
            matchedStreamMap.remove(identifier);
        }
        return matchResultWrapper;
    }

    private MatchResultWrapper matchContentReview(@NotNull String text, @NotNull String identifier,
                                                  boolean isFinished) {
        MatchResultWrapper matchResultWrapper = new MatchResultWrapper();
        matchResultWrapper.text = text;
        if (isContentReviewEnable) {
            int matchedOffset = matchedOffsetMap.computeIfAbsent(identifier, k -> 0);
            StringBuilder lastBuilder = lastBufferMap.computeIfAbsent(identifier, k -> new StringBuilder());
            StringBuilder streamBuilder = streamBufferMap.computeIfAbsent(identifier, k -> new StringBuilder());
            // 记录之前已匹配完成的字符数量
            matchResultWrapper.offset = matchedOffset;
            if (isFinished) {
                String lastText = lastBuilder.toString() + streamBuilder + text;
                Pair<Integer, String> result = outputMixedTrie.match(lastText);
                matchResultWrapper.text = result.getRight();
                matchResultWrapper.op = result.getLeft();
                // 上次匹配存在前缀，此处需要追加
                // 如果前缀被确定为敏感词，此处标记多余，但不影响后续的流式块的撤回替换操作
                if (!lastBuilder.isEmpty()) {
                    matchResultWrapper.op |= SensitiveTrie.SensitiveOp.APPEND.getCode();
                }
                // 如果存在兜底回复匹配结果，重置offset，全量替换
                if ((matchResultWrapper.op & SensitiveTrie.SensitiveOp.REPLY.getCode()) > 0) {
                    matchResultWrapper.offset = 0;
                }
                // 清理 identifier 对应缓存数据
                clearCachedBuilder(identifier);
                return matchResultWrapper;
            }
            streamBuilder.append(text);
            if (streamBuilder.length() + lastBuilder.length() >= miniBufferLength) {
                Pair<Integer, String> result = outputMixedTrie.matchStream(streamBuilder.toString(), lastBuilder);
                matchResultWrapper.op = result.getLeft();
                matchResultWrapper.text = result.getRight();
                streamBuilder.setLength(0);
                matchedOffsetMap.put(identifier, matchedOffset + result.getRight().length());
                // 如果存在兜底回复匹配结果，重置offset，全量替换
                if ((matchResultWrapper.op & SensitiveTrie.SensitiveOp.REPLY.getCode()) > 0) {
                    matchResultWrapper.offset = 0;
                    matchedOffsetMap.put(identifier, result.getRight().length());
                }
            }
            return matchResultWrapper;
        }
        return matchResultWrapper;
    }

    private void clearCachedBuilder(String identifier) {
        lastBufferMap.remove(identifier);
        streamBufferMap.remove(identifier);
        matchedOffsetMap.remove(identifier);
    }

    /**
     * 敏感词匹配结果
     *
     */
    @Getter
    public static class MatchResultWrapper {
        /**
         * 已完成匹配的部分的 length
         * 前端替换时从 offset 之后开始替换
         */
        int offset = 0;

        /**
         * 本次敏感词匹配结果
         */
        String text;

        /**
         * 本次匹配进行了哪些处理操作（替换，过滤，兜底）
         * op & FILTER(1) = FILTER(1)，存在过滤匹配
         * op & REPLACE(2) = REPLACE(2)，存在替换匹配
         * op & REPLY(4) = REPLY(4)，存在兜底回复匹配
         * FILTER，REPLACE，REPLY 定义于 SensitiveTrie.SensitiveOp
         */
        int op = 0;
    }
}
