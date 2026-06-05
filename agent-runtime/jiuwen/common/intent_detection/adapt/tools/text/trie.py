#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.


class TrieNode:
    """
    Represents a node in a Trie data structure.
    Each node stores data, children, and flags for terminal status and edit distance parameters.
    """

    def __init__(self, data=None, is_terminal=False):
        """
        Initializes a TrieNode.

        Args:
            data (object, optional): Initial data to add to the node's data set.
            is_terminal (bool): Whether this node marks the end of a complete key.
        """
        self.data = set()
        if data is not None:
            self.data.add(data)
        self.is_terminal = is_terminal
        self.children = {}
        self.key = None
        # Weight is stored on the node, reflecting the weight of the last inserted item.
        self.weight = 1.0

    @staticmethod
    def _should_explore_fuzzy(edit_distance, max_edit_distance, match_threshold, index):
        """Checks if fuzzy exploration is still viable based on thresholds."""
        if edit_distance >= max_edit_distance:
            return False

        denom = index + max_edit_distance - edit_distance
        if denom > 0:
            potential_confidence = (
                index + max_edit_distance - 2 * edit_distance
            ) / denom
        else:
            potential_confidence = 0.0

        return potential_confidence > match_threshold

    def lookup(
        self,
        iterable,
        index=0,
        gather=False,
        edit_distance=0,
        max_edit_distance=0,
        match_threshold=0.0,
        matched_length=0,
    ):
        """
        Recursively searches the Trie for keys matching the given iterable, allowing for a specified edit distance.

        Args:
            iterable (hashable): The sequence of elements to search for (e.g., a string).
            index (int): Current position in the iterable being processed.
            gather (bool): If True, yields results even when encountering spaces during traversal.
            edit_distance (int): Current number of edits made during the search.
            max_edit_distance (int): Maximum allowed edit distance for fuzzy matching.
            match_threshold (float): Minimum confidence threshold for a result to be yielded.
            matched_length (int): Length of the matched portion, used for confidence calculation.

        Yields:
            dict: A dictionary containing 'key', 'match', 'data', and 'confidence' of matching nodes.
        """
        # Yield result if this node is terminal and we've reached the end of the search iterable
        # or if gathering and the next character is a space.
        if self.is_terminal:
            is_at_end = index == len(iterable)
            is_word_break = gather and index < len(iterable) and iterable[index] == " "
            if is_at_end or is_word_break:
                confidence = self._calculate_confidence(
                    edit_distance, index, matched_length
                )
                if confidence > match_threshold:
                    yield self._create_result(iterable[:index], confidence)

        # Exact match path
        if index < len(iterable) and iterable[index] in self.children:
            yield from self.children[iterable[index]].lookup(
                iterable,
                index + 1,
                gather,
                edit_distance,
                max_edit_distance,
                match_threshold,
                matched_length + 1,
            )

        # Potential match path (if within edit distance budget and potential confidence is high enough)
        if self._should_explore_fuzzy(
            edit_distance, max_edit_distance, match_threshold, index
        ):
            for char, child_node in self.children.items():
                if index >= len(iterable) or char != iterable[index]:
                    # Substitution
                    yield from child_node.lookup(
                        iterable,
                        index + 1,
                        gather,
                        edit_distance + 1,
                        max_edit_distance,
                        match_threshold,
                        matched_length,
                    )
                    # Deletion (skip one character in the search iterable)
                    yield from child_node.lookup(
                        iterable,
                        index + 2,
                        gather,
                        edit_distance + 1,
                        max_edit_distance,
                        match_threshold,
                        matched_length,
                    )
                    # Insertion (skip one character in the Trie path, stay at current iterable index)
                    yield from child_node.lookup(
                        iterable,
                        index,
                        gather,
                        edit_distance + 1,
                        max_edit_distance,
                        match_threshold,
                        matched_length,
                    )

    def insert(self, iterable, index=0, data=None, weight=1.0):
        """
        Inserts a key (iterable) into the Trie, creating necessary nodes.

        Args:
            iterable (hashable): The sequence of elements representing the key.
            index (int): Current position in the iterable during insertion.
            data (object, optional): Data to associate with the key.
            weight (float): Weight associated with the inserted key/node.
        """
        if index == len(iterable):
            self.is_terminal = True
            self.key = iterable
            self.weight = weight
            if data is not None:
                self.data.add(data)
        else:
            char = iterable[index]
            if char not in self.children:
                self.children[char] = TrieNode()
            self.children[char].insert(iterable, index + 1, data, weight)

    def is_prefix(self, iterable, index=0):
        """
        Checks if the given iterable is a prefix present in the Trie.

        Args:
            iterable (hashable): The sequence to check for as a prefix.
            index (int): Current position in the iterable during the check.

        Returns:
            bool: True if the iterable is a prefix, False otherwise.
        """
        if index == len(iterable):
            return True
        char = iterable[index]
        if char in self.children:
            return self.children[char].is_prefix(iterable, index + 1)
        return False

    def remove(self, iterable, data=None, index=0):
        """
        Removes data or a key from the Trie.

        Args:
            iterable (hashable): The key sequence to remove.
            data (object, optional): Specific data to remove from the key's node.
                                     If None, removes the entire key/node if terminal.
            index (int): Current position in the iterable during removal.

        Returns:
            bool: True if a removal occurred, False otherwise.
        """
        if index == len(iterable):
            if self.is_terminal:
                if data is not None:
                    try:
                        self.data.discard(
                            data
                        )  # Use discard to avoid KeyError if data not present
                        if not self.data:
                            self.is_terminal = False
                    except KeyError:
                        return False
                else:
                    self.data.clear()
                    self.is_terminal = False
                return True
            return False

        char = iterable[index]
        if char in self.children:
            return self.children[char].remove(iterable, data, index + 1)
        return False

    def _calculate_confidence(self, edit_distance, index, matched_length):
        """Calculates the confidence score based on key length and edit distance."""
        # Confidence formula based on key length vs edit distance
        if len(self.key) > 0:
            return float(len(self.key) - edit_distance) / float(
                max(len(self.key), index)
            )
        return 0.0

    def _create_result(self, match_str, confidence):
        """Creates a result dictionary for a successful match."""
        return {
            "key": self.key,
            "match": match_str,
            "data": self.data,
            "confidence": confidence * self.weight,
        }


class Trie:
    """
    A prefix Trie implementation supporting exact and fuzzy lookups (with edit distance),
    gathering of partial matches, insertion, removal, and scanning.
    """

    def __init__(self, max_edit_distance=0, match_threshold=0.0):
        """
        Initializes the Trie.

        Args:
            max_edit_distance (int): Maximum Levenshtein distance allowed for fuzzy matching. 0 means exact match only.
            match_threshold (float): Minimum confidence required for a match to be returned (0.0 to 1.0).
        """
        self.root = TrieNode("root")
        self.max_edit_distance = max_edit_distance
        self.match_threshold = match_threshold

    def gather(self, iterable):
        """
        Performs a lookup that yields results for all terminal nodes encountered during traversal,
        potentially stopping at word breaks (spaces).

        Args:
            iterable (hashable): The sequence to search for.

        Yields:
            dict: Matching results found during the gather traversal.
        """
        yield from self.lookup(iterable, gather=True)

    def lookup(self, iterable, gather=False):
        """
        Performs a standard lookup in the Trie.

        Args:
            iterable (hashable): The sequence to search for.
            gather (bool): If True, enables gather mode during the lookup.

        Yields:
            dict: Matching results found during the lookup.
        """
        yield from self.root.lookup(
            iterable,
            gather=gather,
            edit_distance=0,
            max_edit_distance=self.max_edit_distance,
            match_threshold=self.match_threshold,
        )

    def insert(self, iterable, data=None, weight=1.0):
        """
        Inserts a key and its associated data into the Trie.

        Args:
            iterable (hashable): The key sequence to insert.
            data (object, optional): The data to associate with the key.
            weight (float): The weight to assign to the inserted key/node.
        """
        self.root.insert(iterable, index=0, data=data, weight=weight)

    def remove(self, iterable, data=None):
        """
        Removes data or an entire key from the Trie.

        Args:
            iterable (hashable): The key sequence to remove.
            data (object, optional): Specific data to remove. If None, removes the key entirely if terminal.

        Returns:
            bool: True if the item was successfully removed, False otherwise.
        """
        return self.root.remove(iterable, data=data)

    def scan(self, match_func):
        """
        Traverses the entire Trie and collects data from nodes where the match_func returns True.

        Args:
            match_func (callable): A function that takes a data item and returns True if it matches the criteria.

        Returns:
            list: A list of tuples (key_string, matched_data_item) for all matches found.
        """
        results = []

        def _dfs_traverse(node, current_path):
            # Check for matches in the current node's data
            for item in node.data:
                if match_func(item):
                    results.append((current_path, item))

            # Recursively explore children
            for char, child_node in node.children.items():
                _dfs_traverse(child_node, current_path + char)

        _dfs_traverse(self.root, "")
        return results
