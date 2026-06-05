/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.sensitive;

import lombok.Getter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;

/**
 * 敏感词字典树
 *
 */
public class SensitiveTrie {
    private final TrieNode root = new TrieNode();

    @Getter
    private boolean isEmpty = true;

    /**
     * 插入敏感词
     *
     * @param word 敏感词
     * @param op 匹配成功后的操作
     * @param target 操作的目标字符串
     */
    public void insert(String word, SensitiveOp op, String target) {
        TrieNode current = root;
        for (char c : word.toCharArray()) {
            char lowerCase = Character.toLowerCase(c);
            current.children.putIfAbsent(lowerCase, new TrieNode());
            current = current.children.get(lowerCase);
        }
        current.isWord = true;
        current.op = op;
        current.text = target;
        if (SensitiveOp.FILTER.equals(op)) {
            current.text = "";
        }
        isEmpty = false;
    }

    /**
     * 处理文本匹配
     *
     * @param text 待匹配文本
     * @return 返回匹配结果 Pair<#Integer, #String> left: 涉及的匹配操作(二进制或运算结果)，right: 匹配处理后的字符串
     */
    public Pair<Integer, String> match(String text) {
        if (isEmpty || StringUtils.isBlank(text)) {
            return Pair.of(0, text);
        }
        int op = 0;
        StringBuilder sb = new StringBuilder();
        char[] charArray = text.toCharArray();
        int idx = 0;
        while (idx < charArray.length) {
            Pair<Integer, TrieNode> matchResult = matchFromIndex(idx, charArray);
            int pos = matchResult.getLeft();
            TrieNode trieNode = matchResult.getRight();
            if (trieNode != null) {
                op |= trieNode.op.getCode();
                if (trieNode.op.equals(SensitiveOp.REPLY)) {
                    return Pair.of(op, trieNode.text);
                }
                sb.append(trieNode.text);
                idx = pos + 1;
            } else {
                // 匹配失败，index 位置的字符不属于敏感词部分
                sb.append(charArray[idx]);
                idx++;
            }
        }
        return Pair.of(op, sb.toString());
    }

    /**
     * 敏感词匹配流式数据
     *
     * @param streamText 流式块
     * @param lastStringBuilder 上次剩余未匹配数据
     * @return 返回匹配结果 Pair<#Integer, #String> left: 涉及的匹配操作(二进制或运算结果)，right: 匹配处理后的字符串
     */
    public Pair<Integer, String> matchStream(String streamText, StringBuilder lastStringBuilder) {
        if (isEmpty) {
            return Pair.of(0, streamText);
        }
        int op = 0;
        boolean hasLastPrefix = lastStringBuilder.length() > 0;
        if (StringUtils.isBlank(streamText)) {
            // 存在敏感词前缀，追加到最终结果中
            if (hasLastPrefix) {
                op = op | SensitiveOp.APPEND.getCode();
            }
            return Pair.of(op, lastStringBuilder + streamText);
        }
        lastStringBuilder.append(streamText);
        char[] chars = lastStringBuilder.toString().toCharArray();
        StringBuilder stringBuilder = new StringBuilder();
        int index = 0;
        while (index < chars.length) {
            Pair<Integer, TrieNode> matchResult = matchFromIndex(index, chars);
            int pos = matchResult.getLeft();
            TrieNode sensitiveNode = matchResult.getRight();
            if (sensitiveNode != null) {
                op |= sensitiveNode.op.getCode();
                if (sensitiveNode.op.equals(SensitiveOp.REPLY)) {
                    return Pair.of(op, sensitiveNode.text);
                }
                stringBuilder.append(sensitiveNode.text);
                index = pos + 1;
                // 匹配到敏感词，重置前缀标记
                hasLastPrefix = false;
            } else {
                if (pos == chars.length) {
                    // pos 到达末尾，说明敏感词可能跨越2个流式块，匹配结束
                    break;
                } else {
                    // 存在待确认敏感词前缀，说明前缀已确认为非敏感词，需追加文本中
                    if (hasLastPrefix) {
                        op = op | SensitiveOp.APPEND.getCode();
                    }
                    stringBuilder.append(chars[index]);
                    index++;
                }
            }
        }
        lastStringBuilder.setLength(0);
        if (index < chars.length) {
            // 剩余未匹配的为敏感词前缀，保存作为下次匹配使用
            lastStringBuilder.append(chars, index, chars.length - index);
        }
        return Pair.of(op, stringBuilder.toString());
    }

    private Pair<Integer, TrieNode> matchFromIndex(int index, char[] chars) {
        int postion = index;
        TrieNode currentNode = root;
        TrieNode sensitiveNode = null;
        while (postion < chars.length) {
            char lowerCase = Character.toLowerCase(chars[postion]);
            if (currentNode.children.containsKey(lowerCase)) {
                currentNode = currentNode.children.get(lowerCase);
                if (currentNode.isWord) {
                    sensitiveNode = currentNode;
                    break;
                }
            } else {
                break;
            }
            postion++;
        }
        return Pair.of(postion, sensitiveNode);
    }

    /**
     * 节点描述
     *
     */
    static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();

        private boolean isWord = false;

        private SensitiveOp op;

        private String text;
    }

    /**
     * 敏感词匹配操作类型
     *
     */
    public enum SensitiveOp {
        /**
         * 过滤
         */
        FILTER(0x0001),
        /**
         * 替换
         */
        REPLACE(0x0010),
        /**
         * 兜底回复
         */
        REPLY(0x0100),
        /**
         * 上次匹配敏感词前缀确认为非敏感词，需追加到匹配确认的文本中
         */
        APPEND(0x1000);

        private int code = 0;

        SensitiveOp(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }
}
