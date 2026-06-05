# Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.

import heapq
import re
from functools import reduce
from typing import Optional, List, Dict, Generator, Union, Callable, Set, Tuple, Any

from jiuwen.common.intent_detection.adapt.entity_tagger import EntityTagger
from jiuwen.common.intent_detection.adapt.parser import Parser
from jiuwen.common.intent_detection.adapt.tools.text.tokenizer import EnglishTokenizer
from jiuwen.common.intent_detection.adapt.tools.text.trie import Trie


# =============== 辅助函数（保留必要独立逻辑，重命名+注释增强） ===============


def _build_context_entities(context_items: List[Dict]) -> List[Dict]:
    """将原始上下文项转换为标准实体格式，标记来源为上下文。

    Args:
        context_items: 原始上下文数据列表，每项含 'key' 字段。

    Returns:
        标准化后的实体列表，每个实体含 'key', 'entities', 'from_context' 字段。
    """
    return [
        {"key": item["key"], "entities": [item], "from_context": True}
        for item in context_items
    ]


def _extract_used_keys(tags: List[Dict]) -> Set[str]:
    """从解析标签中提取被上下文消耗的 key 集合。

    Args:
        tags: 解析后的标签列表。

    Returns:
        被使用的上下文 key 集合。
    """
    return {tag["key"] for tag in tags if tag.get("from_context")}


def _filter_unused_context(context: List[Dict], used_keys: Set[str]) -> List[Dict]:
    """筛选未被使用的上下文项。

    Args:
        context: 完整上下文列表。
        used_keys: 已被消耗的 key 集合。

    Returns:
        未被使用的上下文项列表。
    """
    return [item for item in context if item["key"] not in used_keys]


def _rank_by_confidence(intents: List[Dict]) -> List[Dict]:
    """按置信度降序排列意图列表。

    Args:
        intents: 待排序意图列表。

    Returns:
        排序后意图列表（高置信度在前）。
    """
    return sorted(intents, key=lambda x: x.get("confidence", 0.0), reverse=True)


# =============== IntentDeterminationEngine ===============


class IntentDeterminationEngine:
    """意图识别引擎：基于 Adapt 工具链的贪心解析实现。

    特点：
    - 使用 Trie + 正则进行实体识别
    - 支持上下文注入与消耗追踪
    - 支持多意图解析器注册与验证
    - 生成器驱动，支持短路优化（无正则/上下文时）
    """

    def __init__(self, tokenizer: Optional[Any] = None, trie: Optional[Trie] = None):
        self.tokenizer = tokenizer or EnglishTokenizer()
        self.trie = trie or Trie()
        self.regular_expressions_entities: List[re.Pattern] = []
        self._regex_patterns: Set[str] = set()  # internal record for dedup
        self.intent_parsers: List[Any] = []

    @property
    def regex_strings(self) -> Set[str]:
        """已注册的正则表达式字符串集合（只读）"""
        return self._regex_patterns.copy()

    @property
    def tagger(self) -> EntityTagger:
        """获取当前配置下的实体标注器实例"""
        return EntityTagger(
            self.trie, self.tokenizer, self.regular_expressions_entities
        )

    @staticmethod
    def _get_unused_context(parse_result: Dict, context: List[Dict]) -> List[Dict]:
        """获取未被当前解析结果使用的上下文项"""
        used = _extract_used_keys(parse_result["tags"])
        return _filter_unused_context(context, used)

    @staticmethod
    def _is_valid_intent(intent: Optional[Dict]) -> bool:
        """判断意图是否有效（存在且置信度 > 0）"""
        return bool(intent and intent.get("confidence", 0.0) > 0)

    def determine_intent(
        self,
        utterance: str,
        num_results: int = 1,
        include_tags: bool = False,
        context_manager: Optional[Any] = None,
    ) -> Generator[Dict, None, None]:
        """识别用户话语中的意图。

        Args:
            utterance: 用户输入文本。
            num_results: 最大返回结果数。
            include_tags: 是否在结果中包含解析标签详情。
            context_manager: 上下文管理器（可选）。

        Yields:
            符合条件的意图字典（含 confidence、intent_type 等字段）。
        """
        parser = Parser(self.tokenizer, self.tagger)
        context = context_manager.get_context() if context_manager else []

        # 解析生成器：始终 yield 所有候选意图
        def _intent_candidates():
            for parse in parser.parse(utterance, N=num_results, context=context):
                unused_ctx = self._get_unused_context(parse, context)
                intent, tags = self._select_highest_intent(parse, unused_ctx)

                if self._is_valid_intent(intent):
                    if include_tags:
                        intent["__tags__"] = tags
                    yield intent

        candidates = list(_intent_candidates())

        # 若含正则或上下文，需全局排序；否则保持解析器原始顺序（贪心短路）
        if self.regular_expressions_entities or context:
            candidates = _rank_by_confidence(candidates)

        yield from candidates[:num_results]

    def register_entity(
        self, entity_value: str, entity_type: str, alias_of: Optional[str] = None
    ) -> None:
        """注册一个实体值及其类型。

        Args:
            entity_value: 实体具体值（如 "周杰伦"）。
            entity_type: 实体类别（如 "歌手"）。
            alias_of: 可选别名指向（用于归一化）。
        """
        lower_val = entity_value.lower()
        if alias_of:
            self.trie.insert(lower_val, data=(alias_of, entity_type))
        else:
            self.trie.insert(lower_val, data=(entity_value, entity_type))
            self.trie.insert(entity_type.lower(), data=(entity_type, "Concept"))

    def register_regex_entity(self, regex_str: str) -> None:
        """注册一个命名捕获组正则表达式用于实体提取。

        示例：`(?P<Artist>.*)`

        Args:
            regex_str: 含命名组的正则表达式字符串。

        Raises:
            ValueError: 正则编译失败。
        """
        if not regex_str or regex_str in self._regex_patterns:
            return
        try:
            compiled = re.compile(regex_str, re.IGNORECASE)
        except re.error as e:
            raise ValueError(f"Invalid regex '{regex_str}': {e}") from e

        self._regex_patterns.add(regex_str)
        self.regular_expressions_entities.append(compiled)

    def register_intent_parser(self, intent_parser: Any) -> None:
        """注册一个意图解析器。

        要求：对象必须实现 `validate_with_tags(tags, base_score)` 方法。

        Args:
            intent_parser: 意图解析器实例。

        Raises:
            ValueError: 解析器接口不合法。
        """
        if not (
            hasattr(intent_parser, "validate_with_tags")
            and callable(intent_parser.validate_with_tags)
        ):
            raise ValueError(f"Invalid intent parser: {intent_parser}")
        self.intent_parsers.append(intent_parser)

    def drop_intent_parser(self, parser_names: Union[str, List[str]]) -> bool:
        """移除指定名称的意图解析器。

        Args:
            parser_names: 单个或多个解析器名称。

        Returns:
            是否有解析器被移除。
        """
        names = {parser_names} if isinstance(parser_names, str) else set(parser_names)
        before = len(self.intent_parsers)
        self.intent_parsers = [
            p for p in self.intent_parsers if getattr(p, "name", None) not in names
        ]
        return len(self.intent_parsers) < before

    def drop_entity(
        self, entity_type: Optional[str] = None, match_func: Optional[Callable] = None
    ) -> bool:
        """移除匹配条件的实体。

        Args:
            entity_type: 指定类型名称（默认匹配 data[1]）。
            match_func: 自定义匹配函数 `(data) -> bool`。

        Returns:
            是否有实体被移除。
        """
        match = match_func or (lambda d: d and d[1] == entity_type)
        removed = self.trie.remove_all(match)
        return len(removed) > 0

    def drop_regex_entity(
        self, entity_type: Optional[str] = None, match_func: Optional[Callable] = None
    ) -> bool:
        """移除匹配条件的正则实体。

        Args:
            entity_type: 指定命名组名（如 "Artist"）。
            match_func: 自定义匹配函数 `(re.Pattern) -> bool`。

        Returns:
            是否有正则实体被移除。
        """
        if entity_type is None and match_func is None:
            raise ValueError(
                "At least one of 'entity_type' or 'match_func' must be provided."
            )

        match = match_func or (lambda r: entity_type in r.groupindex)
        to_remove = [r for r in self.regular_expressions_entities if match(r)]
        if not to_remove:
            return False

        patterns = {r.pattern for r in to_remove}
        self.regular_expressions_entities = [
            r for r in self.regular_expressions_entities if r not in to_remove
        ]
        self._regex_patterns -= patterns
        return True

    # --- 内部辅助方法 ---

    def _select_highest_intent(
        self, parse: Dict, context: List[Dict]
    ) -> Tuple[Optional[Dict], Optional[List]]:
        """从所有解析器中选出置信度最高的意图"""
        tags = parse.get("tags", []) + _build_context_entities(context)
        base_score = parse.get("confidence", 0.0)

        # 使用 max 避免显式循环比较
        def _better(a, b):
            # a, b 为 (intent, tags) 元组；None 视为最弱
            if a[0] is None:
                return b
            if b[0] is None:
                return a
            conf_a = a[0].get("confidence", 0.0)
            conf_b = b[0].get("confidence", 0.0)
            return a if conf_a >= conf_b else b

        candidates = (
            parser.validate_with_tags(tags, base_score)
            for parser in self.intent_parsers
        )
        best_intent, best_tags = reduce(_better, candidates, (None, None))
        return best_intent, best_tags


# =============== DomainIntentDeterminationEngine ===============


class DomainIntentDeterminationEngine:
    """支持多领域（domain）的意图识别引擎。

    每个 domain 独立维护一套实体与解析器，最终合并结果并按置信度排序。
    """

    def __init__(self):
        self.domains: Dict[Any, IntentDeterminationEngine] = {}

    # --- 兼容性接口（仅用于 legacy 代码）---
    @property
    def tokenizer(self):
        """
        tokenizer
        """
        return self._ensure_domain(0).tokenizer

    @property
    def trie(self):
        """
        trie
        """
        return self._ensure_domain(0).trie

    @property
    def tagger(self):
        """
        tagger
        """
        return self._ensure_domain(0).tagger

    @property
    def intent_parsers(self):
        """
        intent_parsers
        """
        return self._ensure_domain(0).intent_parsers

    @property
    def _regex_strings(self):
        """
        regex_strings
        """
        return self._ensure_domain(0).regex_strings

    @property
    def regular_expressions_entities(self):
        """
        regular_expressions_entities
        """
        return self._ensure_domain(0).regular_expressions_entities

    # --- 域操作 API ---

    def register_domain(
        self,
        domain: Any = 0,
        tokenizer: Optional[Any] = None,
        trie: Optional[Trie] = None,
    ) -> None:
        """注册新领域并配置其引擎"""
        self.domains[domain] = IntentDeterminationEngine(tokenizer, trie)

    def register_entity(
        self,
        entity_value: str,
        entity_type: str,
        alias_of: Optional[str] = None,
        domain: Any = 0,
    ) -> None:
        """向指定 domain 注册实体"""
        engine = self._ensure_domain(domain)
        engine.register_entity(entity_value, entity_type, alias_of)

    def register_regex_entity(self, regex_str: str, domain: Any = 0) -> None:
        """向指定 domain 注册正则实体"""
        engine = self._ensure_domain(domain)
        engine.register_regex_entity(regex_str)

    def register_intent_parser(self, intent_parser: Any, domain: Any = 0) -> None:
        """向指定 domain 注册意图解析器"""
        engine = self._ensure_domain(domain)
        engine.register_intent_parser(intent_parser)

    def determine_intent(
        self, utterance: str, num_results: int = 1
    ) -> Generator[Dict, None, None]:
        """跨 domain 识别意图，返回全局 top-k"""
        all_intents = []
        for engine in self.domains.values():
            # 每个 domain 取最优 1 个（避免单 domain 过度主导）
            gen = engine.determine_intent(utterance, num_results=num_results)
            all_intents.extend(gen)

        # 全局 top-k
        top_k = heapq.nlargest(
            num_results, all_intents, key=lambda x: x.get("confidence", 0.0)
        )
        yield from top_k

    def drop_intent_parser(
        self, parser_names: Union[str, List[str]], domain: Any
    ) -> bool:
        """从指定 domain 移除解析器"""
        return self._ensure_domain(domain).drop_intent_parser(parser_names)

    def drop_entity(
        self,
        domain: Any,
        entity_type: Optional[str] = None,
        match_func: Optional[Callable] = None,
    ) -> bool:
        """从指定 domain 移除实体"""
        return self._ensure_domain(domain).drop_entity(entity_type, match_func)

    def drop_regex_entity(
        self,
        domain: Any,
        entity_type: Optional[str] = None,
        match_func: Optional[Callable] = None,
    ) -> bool:
        """从指定 domain 移除正则实体"""
        return self._ensure_domain(domain).drop_regex_entity(entity_type, match_func)

    def _ensure_domain(self, domain: Any) -> IntentDeterminationEngine:
        """确保 domain 存在，不存在则创建默认引擎"""
        if domain not in self.domains:
            self.domains[domain] = IntentDeterminationEngine()
        return self.domains[domain]
