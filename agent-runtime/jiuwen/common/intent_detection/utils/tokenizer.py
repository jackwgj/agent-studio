#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
"""
Unified tokenizer wrappers

Each tokenizer implements a .tokenize(text: str) -> List[str] interface
compatible with Adapt's IntentDeterminationEngine.
"""

from __future__ import annotations

import copy
import re
from abc import ABC, abstractmethod
from typing import List, Optional, Dict, Set, Union, Iterator

from jieba import Tokenizer
from jiuwen.common.exception import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.log.base import logger


class BaseTokenizer(ABC):
    """Adapt-compatible tokenizer interface."""

    @abstractmethod
    def tokenize(self, text: str) -> List[str]:
        """
        Adapt tokenizer interface.
        :param text: text to tokenize
        :return: tokenized text
        """
        raise NotImplementedError


class FreqProxy:
    """
    FREQ字典代理
    """

    def __init__(self, base_freq: Dict[str, int]):
        """
        初始化字典代理

        Args:
            base_freq: 基础FREQ字典（只读）
        """
        self._base_freq = base_freq
        self._user_freq: Dict[str, int] = {}
        self._deleted: Set[str] = set()

    def __getitem__(self, key: str) -> int:
        if key in self._deleted:
            raise KeyError(key)
        if key in self._user_freq:
            return self._user_freq[key]
        return self._base_freq[key]

    def __setitem__(self, key: str, value: int):
        self._user_freq[key] = value
        self._deleted.discard(key)

    def __delitem__(self, key: str):
        self._deleted.add(key)
        self._user_freq.pop(key, None)

    def __contains__(self, key: str) -> bool:
        if key in self._deleted:
            return False
        return key in self._user_freq or key in self._base_freq

    def __iter__(self):
        return iter(self.keys())

    def __len__(self):
        return len(self.keys())

    def get(self, key: str, default: Optional[int] = None) -> Optional[int]:
        """获取key对应的value"""
        try:
            return self[key]
        except KeyError:
            return default

    def keys(self):
        """返回所有有效的键"""
        base_keys = set(self._base_freq.keys()) - self._deleted
        return base_keys | set(self._user_freq.keys())

    def items(self):
        """返回所有有效的键值对"""
        for key in self.keys():
            yield key, self[key]

    def values(self):
        """返回所有有效的值"""
        for key in self.keys():
            yield self[key]

    def copy(self):
        """返回完整字典的副本"""
        result = dict(self._base_freq)
        for key in self._deleted:
            result.pop(key, None)
        result.update(self._user_freq)
        return result


class JiebaTokenizer(BaseTokenizer):
    """
    jieba分词器（副本）
    """

    def __init__(
        self,
        base_tokenizer: Tokenizer,
        base_freq: Dict[str, int],
        base_total: int,
        base_user_word_tag_tab: Dict[str, str],
    ):
        """
        初始化分词器副本
        """
        # 浅拷贝基础tokenizer
        self._tokenizer = copy.copy(base_tokenizer)

        # 使用FreqProxy代替复制FREQ字典
        self._freq_proxy = FreqProxy(base_freq)
        self._tokenizer.FREQ = self._freq_proxy

        # 保存基础total，用户词典会增加这个值
        self._base_total = base_total
        self._user_total_delta = 0
        self._tokenizer.total = base_total

        # 用户词性标签
        self._user_word_tag_tab: Dict[str, str] = {}
        self._base_user_word_tag_tab = base_user_word_tag_tab

        # 标记已添加的用户词
        self._user_words: Set[str] = set()

    def add_word(
        self, word: str, freq: Optional[int] = None, tag: Optional[str] = None
    ):
        """
        添加用户自定义词

        Args:
            word: 词语
            freq: 词频，默认自动计算
            tag: 词性标签
        """
        # 计算词频
        if freq is None:
            freq = self._tokenizer.suggest_freq(word, tune=False)

        # 更新FREQ
        original_freq = self._freq_proxy.get(word, 0)
        self._freq_proxy[word] = freq

        # 更新total
        self._user_total_delta += freq - original_freq
        self._tokenizer.total = self._base_total + self._user_total_delta

        for ch in range(len(word)):
            wfrag = word[: ch + 1]
            if wfrag not in self._freq_proxy:
                self._freq_proxy[wfrag] = 0

        # 词性标签
        if tag:
            self._user_word_tag_tab[word] = tag

        self._user_words.add(word)

    def del_word(self, word: str):
        """删除词语"""
        if word in self._freq_proxy:
            original_freq = self._freq_proxy.get(word, 0)
            del self._freq_proxy[word]
            self._user_total_delta -= original_freq
            self._tokenizer.total = self._base_total + self._user_total_delta

        self._user_word_tag_tab.pop(word, None)
        self._user_words.discard(word)

    def load_userdict(self, f: Union[str, object]):
        """
        从文件加载用户词典

        Args:
            f: 文件路径/对象
        """
        if isinstance(f, str):
            with open(f, "r", encoding="utf-8") as fp:
                lines = fp.readlines()
        else:
            lines = f.readlines()

        self.load_userdict_from_lines(lines)

    def load_userdict_from_lines(self, lines: List[str]):
        """
        从字符串加载用户词典

        Args:
            lines: 词典列表，格式为 "词语 词频 词性" 或 "词语"
                   例如: ["aaa 5 n", "bbb", "ccc 3"]
        """
        for line in lines:
            line = line.strip()
            if not line:
                continue

            parts = re.split(r"[\t ]+", line)
            word = parts[0]
            freq = None
            tag = None

            if len(parts) >= 2:
                try:
                    freq = int(parts[1])
                except ValueError:
                    tag = parts[1]

            if len(parts) >= 3:
                tag = parts[2]

            self.add_word(word, freq, tag)

    def suggest_freq(self, segment: Union[str, tuple], tune: bool = False) -> int:
        """建议词频"""
        return self._tokenizer.suggest_freq(segment, tune=tune)

    def cut(
        self, sentence: str, cut_all: bool = False, hmm: bool = True
    ) -> Iterator[str]:
        """
        分词

        Args:
            sentence: 待分词文本
            cut_all: 是否全模式
            hmm: 是否使用HMM模型

        Returns:
            生成器
        """
        return self._tokenizer.cut(sentence, cut_all=cut_all, HMM=hmm)

    def lcut(self, sentence: str, cut_all: bool = False, hmm: bool = True) -> List[str]:
        """分词并返回列表"""
        return self._tokenizer.lcut(sentence, cut_all=cut_all, HMM=hmm)

    def cut_for_search(self, sentence: str, hmm: bool = True) -> Iterator[str]:
        """搜索引擎模式分词"""
        return self._tokenizer.cut_for_search(sentence, HMM=hmm)

    def lcut_for_search(self, sentence: str, hmm: bool = True) -> List[str]:
        """搜索引擎模式分词"""
        return self._tokenizer.lcut_for_search(sentence, HMM=hmm)

    def get_user_words(self) -> Set[str]:
        """获取已添加的用户词列表"""
        return self._user_words.copy()

    def tokenize_with_pos(
        self, unicode_sentence: str, mode: str = "default", hmm: bool = True
    ):
        """返回tokenize结果，包含词语位置信息"""
        return self._tokenizer.tokenize(unicode_sentence, mode=mode, HMM=hmm)

    def tokenize(self, text: str) -> List[str]:
        """规则引擎分词接口"""
        return [t for t in self.cut(text) if t.strip()]

    def _get_word_tag(self, word: str) -> Optional[str]:
        """获取词性标签"""
        if word in self._user_word_tag_tab:
            return self._user_word_tag_tab[word]
        return self._base_user_word_tag_tab.get(word)


class JiebaTokenizerPool:
    """
    Jieba分词器池
    """

    _instance: Optional["JiebaTokenizerPool"] = None

    def __init__(self, dictionary: Optional[str] = None):
        """
        初始化基础分词器
        Args:
            dictionary: 自定义基础词典路径，默认使用jieba内置词典
        """
        self._base_tokenizer = Tokenizer(dictionary=dictionary)
        self._base_tokenizer.initialize()

        # 缓存基础分词器状态（只读）
        self._base_freq: Dict[str, int] = self._base_tokenizer.FREQ
        self._base_total: int = self._base_tokenizer.total
        self._base_user_word_tag_tab: Dict[str, str] = (
            self._base_tokenizer.user_word_tag_tab
        )

    @classmethod
    def initialize(cls, dictionary: Optional[str] = None) -> "JiebaTokenizerPool":
        """
        初始化

        Args:
            dictionary: 自定义基础词典路径，默认使用jieba内置词典

        Returns:
            JiebaTokenizerPool: 分词器池
        """
        if cls._instance is not None:
            logger.info(
                "JiebaTokenizerPool is already initialized. Return the initialized instance."
            )
            return cls._instance
        cls._instance = cls(dictionary=dictionary)
        return cls._instance

    @classmethod
    def get_instance(cls) -> "JiebaTokenizerPool":
        """
        获取分词器池

        Returns:
            JiebaTokenizerPool: 分词器池实例
        """
        if cls._instance is None:
            logger.error(
                "JiebaTokenizerPool is not initialized. Call 'initialize()' first."
            )
            raise JiuWenBaseException(
                error_code=StatusCode.TOKENIZER_GET_INSTANCE_ERROR.code,
                message=StatusCode.TOKENIZER_GET_INSTANCE_ERROR.errmsg,
            )
        return cls._instance

    @classmethod
    def is_initialized(cls) -> bool:
        """检查是否已初始化"""
        return cls._instance is not None

    @classmethod
    def _reset(cls):
        """重置"""
        cls._instance = None

    def get_tokenizer(self) -> "JiebaTokenizer":
        """
        获取分词器副本

        Returns:
            JiebaTokenizer: 独立的分词器实例
        """
        return JiebaTokenizer(
            base_tokenizer=self._base_tokenizer,
            base_freq=self._base_freq,
            base_total=self._base_total,
            base_user_word_tag_tab=self._base_user_word_tag_tab,
        )


def init_tokenizer_pool(dictionary: Optional[str] = None) -> JiebaTokenizerPool:
    """
    初始化分词器池

    Args:
        dictionary: 自定义基础词典路径，默认使用jieba内置词典

    Returns:
        JiebaTokenizerPool: 分词器池实例
    """
    return JiebaTokenizerPool.initialize(dictionary=dictionary)
