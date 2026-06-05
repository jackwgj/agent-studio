#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.

import time

from jiuwen.common.intent_detection.adapt.expander import BronKerboschExpander
from jiuwen.common.intent_detection.adapt.tools.text.trie import Trie


class Parser(object):
    """
    Coordinate a tagger and expander to yield valid parse results.
    """

    def __init__(self, tokenizer, tagger):
        self._tokenizer = tokenizer
        self._tagger = tagger

    def parse(self, utterance, context=None, n=1):
        """Used to find tags within utterance with a given confidence

        Args:
            utterance(str): conversational piece given by the user
            context(list): a list of entities
            n(int): number of results
        Returns: yield an object with the following fields
            utterance(str): the value passed in
            tags(list) : a list of tags found in utterance
            time(time) : duration since call of function
            confidence(float) : float indicating how confident of a match to the
                utterance. This might be used to determan the most likely intent.

        """
        start_time = time.time()
        processed_utterance = utterance.lower()
        utterance_length = len(utterance)

        context_trie = self._create_context_trie(context)
        tagged_entities = self._tagger.tag(
            processed_utterance, context_trie=context_trie
        )

        expander = BronKerboschExpander(self._tokenizer)
        parse_results = expander.expand(
            tagged_entities,
            clique_scoring_func=lambda clique: self._calculate_clique_score(
                clique, utterance_length
            ),
        )

        yield_count = 0
        for result in parse_results:
            if yield_count >= n:
                break

            result_confidence = self._calculate_result_confidence(
                result, utterance_length
            )

            yield {
                "utterance": utterance,
                "tags": result,
                "time": time.time() - start_time,
                "confidence": result_confidence,
            }
            yield_count += 1

    def _create_context_trie(self, context):
        """Create a trie structure from context entities, ordered by confidence in ascending order.

        Args:
            context (list): List of entity dictionaries containing confidence and data

        Returns:
            Trie: A trie structure containing context entities or None if invalid input
        """
        if not self._is_valid_context(context):
            return None

        sorted_context = sorted(context, key=lambda x: x.get("confidence", 0.0))
        trie = Trie()

        for entity in sorted_context:
            entity_data = entity.get("data")
            if not self._has_valid_entity_data(entity_data):
                continue

            try:
                entity_value, entity_type = entity_data[0]
            except (ValueError, TypeError):
                continue

            confidence = entity.get("confidence", 0.0)
            trie.insert(
                entity_value.lower(),
                data=(entity_value, entity_type),
                weight=confidence,
            )
        return trie

    def _is_valid_context(self, context):
        """Check if the context is valid (not None and is a list).

        Args:
            context: Context data to validate

        Returns:
            bool: True if context is valid, False otherwise
        """
        return context and isinstance(context, list)

    def _has_valid_entity_data(self, entity_data):
        """Check if entity data exists and has content.

        Args:
            entity_data: Entity data to validate

        Returns:
            bool: True if entity data is valid, False otherwise
        """
        return isinstance(entity_data, (list, tuple)) and len(entity_data) > 0

    def _calculate_clique_score(self, clique, utterance_length):
        """Calculate score for a clique of tagged entities based on confidence and match length.

        Args:
            clique (list): List of tagged entities
            utterance_length (int): Length of the original utterance

        Returns:
            float: Calculated score for the clique
        """
        if utterance_length == 0:
            return 0.0

        total_score = 0.0
        for tagged_entity in clique:
            entities = tagged_entity.get("entities", [])
            if not entities:
                continue

            primary_entity = entities[0]
            confidence = primary_entity.get("confidence", 0.0)
            match_text = primary_entity.get("match", "")
            match_length = len(match_text)

            # Add small constant to prevent division by zero
            total_score += confidence * match_length / (utterance_length + 1)
        return total_score

    def _calculate_result_confidence(self, result, utterance_length):
        """Calculate overall confidence for a parse result.

        Args:
            result (list): Parse result containing tags
            utterance_length (int): Length of the original utterance

        Returns:
            float: Overall confidence score for the result
        """
        if utterance_length == 0:
            return 0.0

        total_confidence = 0.0
        for tag in result:
            entities = tag.get("entities", [])
            if not entities:
                continue

            primary_entity = entities[0]
            confidence = primary_entity.get("confidence", 0.0)
            match_text = primary_entity.get("match", "")
            match_length = len(match_text)

            total_confidence += confidence * match_length / utterance_length
        return total_confidence
