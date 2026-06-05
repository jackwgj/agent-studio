#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.

"""
实体标记器模块。

该模块提供了一个基于Trie树和正则表达式的实体识别功能。
它可以在给定的自然语言文本中高效地搜索并标记已知的实体。
"""

from jiuwen.common.intent_detection.adapt.tools.text.trie import Trie


class EntityTagger:
    """
    已知实体标记器。
    给定一个已知实体的索引（Trie树），可以在此索引内高效搜索提供的语句中的实体。
    """

    def __init__(self, trie, tokenizer, regex_entities=None, max_tokens=20):
        """
        初始化实体标记器。

        Args:
            trie: 用于存储主要实体的Trie树结构。
            tokenizer: 用于将输入语句分词的分词器对象。
            regex_entities: 一个包含正则表达式对象的列表，默认为None。
            max_tokens: 最大处理的token数量，默认为20。
        """
        self.trie = trie
        self.tokenizer = tokenizer
        self.max_tokens = max_tokens
        self.regex_entities = regex_entities if regex_entities is not None else []

    @staticmethod
    def _generate_subsequences(tokens):
        """
        生成所有可能的token子序列及其在原列表中的起始索引。

        注意：此操作的时间复杂度较高，为O(N^2)。

        Args:
            tokens (list): token列表。

        Yields:
            tuple: (str, int) 其中str是连接的子序列，int是其在原列表中的起始索引。
        """
        num_tokens = len(tokens)
        for start_idx in range(num_tokens):
            for end_idx in range(start_idx + 1, num_tokens + 1):
                subsequence_str = " ".join(tokens[start_idx:end_idx])
                yield subsequence_str, start_idx

    @staticmethod
    def _sort_and_merge_tags(tags):
        """
        对实体标签列表进行排序。

        排序规则是首先按'start_token'升序，然后按'end_token'升序。

        Args:
            tags (list): 包含实体信息字典的列表。

        Returns:
            list: 排序后的实体标签列表。
        """
        # 使用元组作为排序键进行排序
        sorted_tags = sorted(
            tags, key=lambda tag: (tag["start_token"], tag["end_token"])
        )
        return sorted_tags

    def tag(self, utterance, context_trie=None):
        """
        在语句中标记已知的实体。

        Args:
            utterance (str): 自然语言文本字符串。
            context_trie (Trie, optional): 可选参数，一个仅包含当前请求上下文实体的Trie树。

        Returns:
            list: 包含识别出的实体信息字典的列表。每个字典包含以下键：
                - 'match' (str): 匹配到的标准实体名称。
                - 'key' (str): 与实体匹配的原始字符串。
                - 'start_token' (int): 0为起始索引的第一个被匹配的token。
                - 'end_token' (int): 0为起始索引的最后一个被匹配的token。
                - 'entities' (list): 实体类型的字符串列表（例如：Artist, Location）。
                - 'from_context' (bool): 标识该实体是否来自上下文Trie树。
        """
        tokens = self.tokenizer.tokenize(utterance)
        entities = []

        # 处理正则表达式实体
        if self.regex_entities:
            regex_entities = self._tag_regex_entities(tokens)
            entities.extend(regex_entities)

        # 标记主Trie树中的实体
        main_entities = self._find_entities_in_trie(
            tokens, self.trie, from_context=False
        )

        # 标记上下文Trie树中的实体
        context_entities = []
        if context_trie:
            context_entities = self._find_entities_in_trie(
                tokens, context_trie, from_context=True
            )
            # 对上下文实体增加置信度
            for ent in context_entities:
                if "confidence" in ent.get("entities", [{}])[0]:
                    ent["entities"][0]["confidence"] *= 2.0

        all_entities = entities + main_entities + context_entities

        # 如果有需要合并的实体，则进行排序和合并
        if len(all_entities) > 1:
            all_entities = self._sort_and_merge_tags(all_entities)

        return all_entities

    def _find_entities_in_trie(self, tokens, trie_instance, from_context):
        """辅助方法，用于在指定的Trie树中查找实体。"""
        found_entities = []
        num_tokens = len(tokens)
        for start_idx in range(num_tokens):
            # 构建从当前token开始到句子末尾的子串
            sub_sentence = " ".join(tokens[start_idx:])

            # 在Trie树中查找所有匹配项
            gathered_items = trie_instance.gather(sub_sentence)
            for item in gathered_items:
                item["data"] = list(item["data"])

                # 计算匹配实体的结束token索引
                match_tokens_count = len(self.tokenizer.tokenize(item.get("match", "")))
                end_idx = start_idx + match_tokens_count - 1

                entity_dict = {
                    "match": item.get("match"),
                    "key": item.get("key"),
                    "start_token": start_idx,
                    "end_token": end_idx,
                    "entities": [item],
                    "from_context": from_context,
                }
                found_entities.append(entity_dict)
        return found_entities

    def _tag_regex_entities(self, tokens):
        """
        使用正则表达式实体列表来标记tokens。

        Args:
            tokens (list): 分词后的token列表。

        Returns:
            list: 由正则表达式匹配到的实体信息字典列表。
        """
        all_regex_entities = []
        # 遍历所有可能的token子序列
        for subsequence, start_idx in self._generate_subsequences(tokens):
            temp_trie = Trie()
            # 尝试用每个正则表达式匹配当前子序列
            for regex_pattern in self.regex_entities:
                match_result = regex_pattern.match(subsequence)
                if match_result:
                    # 提取命名捕获组中的匹配内容
                    captured_groups = match_result.groupdict()
                    for name, value in captured_groups.items():
                        if value:
                            temp_trie.insert(value, (value, name))

            # 创建一个临时标记器来处理当前子序列
            temp_tagger = EntityTagger(
                temp_trie, self.tokenizer, max_tokens=self.max_tokens
            )
            sub_results = temp_tagger.tag(subsequence)
            # 调整token索引以适应原始语句的位置
            for result in sub_results:
                result["start_token"] += start_idx
                result["end_token"] += start_idx
                # 设置正则匹配的默认置信度
                for entity_info in result.get("entities", []):
                    entity_info["confidence"] = 0.5
                all_regex_entities.append(result)
        return all_regex_entities
