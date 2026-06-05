#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.

"""
Context Management code for Adapt (where context ~= persistent session state).
"""

from typing import Optional, List, Dict, Any


class ContextManagerFrame:
    """
    Represents a single frame within a conversation context, holding related entities and metadata.

    This frame checks for metadata compatibility and merges new data when appropriate.

    Attributes:
        entities (List[Any]): A list of entities associated with this context frame.
        metadata (Dict[str, Any]): Metadata describing the context of this frame.
    """

    def __init__(
        self,
        entities: Optional[List[Any]] = None,
        metadata: Optional[Dict[str, Any]] = None,
    ):
        """
        Initializes a ContextManagerFrame instance.

        Args:
            entities: A list of entities to initialize the frame with. Defaults to an empty list.
            metadata: A dictionary of metadata for the frame. Defaults to an empty dictionary.
        """
        self.entities = entities or []
        self.metadata = metadata or {}

    def matches_metadata_query(self, query_metadata: Optional[Dict[str, Any]]) -> bool:
        """
        Checks if the provided query metadata is a subset of this frame's metadata.

        Args:
            query_metadata: The metadata dictionary to check against this frame's metadata.

        Returns:
            True if all key-value pairs in query_metadata exist in this frame's metadata,
            False otherwise or if query_metadata is empty.
        """
        if not query_metadata:
            return False

        for key, value in query_metadata.items():
            if self.metadata.get(key) != value:
                return False
        return True

    def add_entity_and_metadata(self, new_entity: Any, new_metadata: Dict[str, Any]):
        """
        Adds a new entity to the frame and merges new metadata keys if they don't already exist.

        Args:
            new_entity: The entity to append to the entities list.
            new_metadata: The metadata dictionary to potentially merge into this frame's metadata.
        """
        self.entities.append(new_entity)
        for key, value in new_metadata.items():
            # Only add the key-value pair if the key doesn't already exist in our metadata
            if key not in self.metadata:
                self.metadata[key] = value


class ContextManager:
    """
    Manages the lifecycle of conversation context frames as a stack.

    This class provides methods to inject new context and retrieve the accumulated context,
    applying decay based on the frame's age (depth in the stack).
    """

    def __init__(self):
        """Initializes the context manager with an empty stack of frames."""
        self._frames = []  # Renamed from frame_stack for internal clarity

    @staticmethod
    def _select_specific_entities(
        all_entities: List[Dict[str, Any]], target_entity_tags: List[str]
    ) -> List[Dict[str, Any]]:
        """
        Filters a list of entities to include only those whose 'data' field matches a tag in the target list.

        Matches are made without replacement; the first occurrence of a matching tag is taken.
        The order of the returned list follows the order of the input list.

        Args:
            all_entities: The full list of entity dictionaries to filter.
            target_entity_tags: A list of entity tags ('data' field values) to select.

        Returns:
            A new list containing only the selected entities, preserving their original order.
        """
        # Create a set for O(1) average time complexity lookup
        target_set = set(target_entity_tags)
        # Keep track of found tags to ensure uniqueness based on first occurrence
        found_tags = set()
        filtered_result = []

        for entity in all_entities:
            tag = entity.get("data")
            # Check if the tag is desired and hasn't been added yet
            if tag in target_set and tag not in found_tags:
                filtered_result.append(entity)
                found_tags.add(tag)

        return filtered_result

    def inject_context(
        self, entity: Dict[str, Any], metadata: Optional[Dict[str, Any]] = None
    ):
        """
        Adds an entity to the current context by either merging into the top frame or creating a new one.

        It attempts to merge the entity and metadata into the existing top frame if its metadata matches.
        Otherwise, a new frame is created and pushed onto the stack.

        Args:
            entity: An entity dictionary with keys like 'data', 'key', 'confidence'.
            metadata: Optional metadata dictionary describing the entity. Defaults to an empty dict.
        """
        metadata = metadata or {}

        # Get the top frame if the stack isn't empty
        top_frame = self._frames[0] if self._frames else None

        # If there's a top frame and its metadata matches the incoming metadata, merge
        if top_frame and top_frame.matches_metadata_query(metadata):
            top_frame.add_entity_and_metadata(entity, metadata)
        else:
            # Create a new frame and push it onto the stack
            new_frame = ContextManagerFrame(entities=[entity], metadata=metadata.copy())
            self._frames.insert(0, new_frame)

    def get_context(
        self,
        max_frames: Optional[int] = None,
        missing_entities: Optional[List[str]] = None,
    ) -> List[Any]:
        """
        Retrieves the context entities from the most recent frames, applying decay to their confidence.

        Confidence values are divided by a factor increasing with frame depth (older frames have lower confidence).
        Optionally filters the results to only include specific entities listed in missing_entities.

        Args:
            max_frames: The maximum number of recent frames to retrieve context from. If None, uses all frames.
            missing_entities: An optional list of entity tags to filter the returned context.

        Returns:
            A list of entity dictionaries, potentially filtered and with adjusted confidence scores.
        """
        missing_entities = missing_entities or []

        # Determine how many frames to process
        num_frames_to_process = (
            min(len(self._frames), max_frames)
            if max_frames is not None
            else len(self._frames)
        )

        # Build the raw context list from the specified number of frames
        context_list = self._collect_entities_from_frames(num_frames_to_process)

        # Apply confidence decay based on frame depth
        self._apply_decay_to_entities(context_list, num_frames_to_process)

        # Filter the final list if a list of missing entities was provided
        if missing_entities:
            return self._select_specific_entities(context_list, missing_entities)

        return context_list

    def _collect_entities_from_frames(self, num_frames: int) -> List[Dict[str, Any]]:
        """
        Collects entities from the top N frames, making shallow copies.

        Args:
            num_frames: The number of top frames to collect entities from.

        Returns:
            A list of copied entity dictionaries.
        """
        collected_entities = []
        for idx in range(num_frames):
            current_frame = self._frames[idx]
            # Make a shallow copy of each entity dictionary
            copied_entities = [entity.copy() for entity in current_frame.entities]
            collected_entities.extend(copied_entities)
        return collected_entities

    def _apply_decay_to_entities(self, entities: List[Dict[str, Any]], num_frames: int):
        """
        Applies a confidence decay factor to entities based on their originating frame's depth.

        The decay factor is calculated as (2.0 + frame_depth_index), starting from 0.
        This modifies the entities list in-place.

        Args:
            entities: The list of entity dictionaries to modify.
            num_frames: The number of frames that contributed to the entities list.
        """
        entity_idx = 0
        for frame_depth in range(num_frames):
            # Calculate the decay factor for the current frame's depth
            decay_factor = 2.0 + frame_depth

            # Apply the decay factor to the correct number of entities
            # (those originating from the current frame)
            current_frame_entity_count = len(self._frames[frame_depth].entities)
            for _ in range(current_frame_entity_count):
                entity = entities[entity_idx]
                # Use the entity's own confidence or default to 1.0, then apply decay
                base_confidence = entity.get("confidence", 1.0)
                entity["confidence"] = base_confidence / decay_factor
                entity_idx += 1
