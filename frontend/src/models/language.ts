/**
 * 语言数据模型
 */
import { Immutable, freeze } from 'immer';

/**
 * 语言枚举
 */
export enum Language {
  /** 中文 */
  'zh' = 'zh',
  /** 中文 - 简体 */
  'zh-Hans' = 'zh-Hans',
  /** 中文 - 中国大陆 */
  'zh-CN' = 'zh-CN',
  /** 英文 */
  'en' = 'en',
  /** 英文 - 美国 */
  'en-US' = 'en-US',
}

/**
 * 语言正则
 */
export const LANGUAGE_PATTERNS: Readonly<Record<string, RegExp>> = {
  /**
   * 匹配语言
   */
  lang: /^(?<lang>[A-Za-z]+)-?.*$/,
};

/**
 * 语言枚举映射
 *
 * HACK: 处理 CF UI 语言枚举全局变量 `cfCurrentLan` 为 paraCase
 */
export const LANGUAGE_MAP: Immutable<Map<string, Language>> = freeze(
  new Map([
    ['zh-cn', Language['zh-CN']],
    ['en-us', Language['en-US']],
  ]),
);
