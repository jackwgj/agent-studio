#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
import itertools

CLIENT_ENTITY_NAME = "Client"


def is_entity(tag, entity_name):
    """
    Checks if the specified entity name exists within the tag's entities.

    Args:
        tag (dict): The tag dictionary potentially containing entities.
        entity_name (str): The entity name to search for.

    Returns:
        bool: True if the entity name is found, False otherwise.
    """
    if not tag or "entities" not in tag:
        return False

    entity_name_lower = entity_name.lower()
    for entity in tag["entities"]:
        data_list = entity.get("data", [])
        for _, entity_type in data_list:
            if entity_type.lower() == entity_name_lower:
                return True
    return False


def find_first_tag(tags, entity_type, after_index=-1):
    """
    Finds the first tag containing an entity of the specified type after a given index.

    Args:
        tags (list): A list of tag dictionaries.
        entity_type (str): The type of entity to search for.
        after_index (int, optional): The starting token index threshold. Defaults to -1.

    Returns:
        tuple: A tuple containing the found tag (dict), the corresponding value (str),
               and the confidence score (float). Returns (None, None, None) if not found.
    """
    entity_type_lower = entity_type.lower()
    for tag in tags:
        entities = tag.get("entities", [])
        for entity in entities:
            data_list = entity.get("data", [])
            for item in data_list:
                if not isinstance(item, (list, tuple)) or len(item) < 2:
                    continue
                value, e_type = item[0], item[1]
                if e_type.lower() == entity_type_lower:
                    start_token = tag.get("start_token", 0)
                    from_context = tag.get("from_context", False)
                    if start_token > after_index or from_context:
                        return tag, value, entity.get("confidence")
    return None, None, None


def find_next_tag(tags, end_index=0):
    """
    Finds the first tag whose start token is greater than the given index.

    Args:
        tags (list): A list of tag dictionaries.
        end_index (int, optional): The index threshold. Defaults to 0.

    Returns:
        dict: The first matching tag, or None if no match is found.
    """
    for tag in tags:
        if tag.get("start_token", float("inf")) > end_index:
            return tag
    return None


def choose_1_from_each(lists):
    """
    Generates all combinations by picking one element from each list.

    This function wraps itertools.product to yield lists instead of tuples,
    maintaining compatibility with existing callers.

    Args:
        lists (list): A list of lists or tuples to generate the Cartesian product from.

    Returns:
        generator: A generator yielding lists representing each combination.
    """
    for combination_tuple in itertools.product(*lists):
        yield list(combination_tuple)


def resolve_one_of(tags, at_least_one):
    """
    Attempts to find a valid combination of tags satisfying the 'at least one' constraints.

    For each sub-list in 'at_least_one', one entity type must be found in 'tags'.
    Tags are consumed without replacement, and later occurrences of the same entity
    type in a combination are sought after the previous occurrence's end token.

    Args:
        tags (list): The list of available tags to search within.
        at_least_one (list): A list of lists, where each sub-list contains entity types,
                             and at least one from each sub-list must be present.

    Returns:
        dict: A dictionary mapping resolved entity types to their found tag lists,
              or None if no valid combination exists.
    """
    for candidate_combo in choose_1_from_each(at_least_one):
        resolved_map = {}
        success = True
        for entity_typ in candidate_combo:
            last_end_idx = -1
            if entity_typ in resolved_map and resolved_map[entity_typ]:
                last_end_idx = resolved_map[entity_typ][-1].get("end_token", -1)

            found_tag, _, _ = find_first_tag(tags, entity_typ, after_index=last_end_idx)
            if not found_tag:
                success = False
                break

            if entity_typ not in resolved_map:
                resolved_map[entity_typ] = []
            resolved_map[entity_typ].append(found_tag)

        if success and len(resolved_map) == len(candidate_combo):
            return resolved_map

    return None


class Intent(object):
    def __init__(self, name, requires, at_least_one, optional):
        """
        Initializes an Intent object.

        Args:
            name (str): The name of the intent.
            requires (list): A list of tuples (entity_type, attribute_name) for required entities.
            at_least_one (list): A list of tuples (entity_type, attribute_name) for 'at least one' groups.
            optional (list): A list of tuples (entity_type, attribute_name) for optional entities.
        """
        self.name = name
        self.requires = requires
        self.at_least_one = at_least_one
        self.optional = optional

    def validate(self, tags, confidence):
        """
        Validates tags against the intent's requirements and returns the result.

        This method discards the tags returned by validate_with_tags.

        Args:
            tags (list): The list of tags to validate.
            confidence (float): The parser's base confidence level.

        Returns:
            dict: The parsed intent result.
        """
        intent_result, _ = self.validate_with_tags(tags, confidence)
        return intent_result

    def validate_with_tags(self, tags, confidence):
        """
        Validates tags against the intent's requirements and returns the result along with used tags.

        Args:
            tags (list): The list of tags to validate.
            confidence (float): The parser's base confidence level.

        Returns:
            tuple: A tuple containing the parsed intent result (dict) and the list of used tags (list).
                   If validation fails, returns a result with confidence 0.0 and an empty list of tags.
        """
        result = {"intent_type": self.name}
        remaining_tags = tags[:]
        used_tags_list = []
        intent_confidence_sum = 0.0

        # Validate required entities
        (
            validation_success,
            result,
            used_tags_list,
            remaining_tags,
            intent_confidence_sum,
        ) = self._validate_required_entities(
            result, remaining_tags, used_tags_list, intent_confidence_sum
        )
        if not validation_success:
            result["confidence"] = 0.0
            return result, []

        # Validate 'at least one' entities
        (
            validation_success,
            result,
            used_tags_list,
            remaining_tags,
            intent_confidence_sum,
        ) = self._validate_at_least_one_entities(
            result, remaining_tags, used_tags_list, intent_confidence_sum
        )
        if not validation_success:
            result["confidence"] = 0.0
            return result, []

        # Validate optional entities
        result, used_tags_list, remaining_tags, intent_confidence_sum = (
            self._validate_optional_entities(
                result, remaining_tags, used_tags_list, intent_confidence_sum
            )
        )

        # Calculate final confidence
        final_confidence = self._calculate_final_confidence(
            intent_confidence_sum, len(tags), confidence
        )

        # Extract target client
        target_client_tag = self._find_target_client(remaining_tags)
        result["target"] = target_client_tag.get("key") if target_client_tag else None
        result["confidence"] = final_confidence

        return result, used_tags_list

    def _validate_required_entities(
        self, result_dict, available_tags, used_tags, current_confidence
    ):
        for entity_type, attr_name in self.requires:
            tag, canonical_val, tag_conf = find_first_tag(available_tags, entity_type)
            if not tag:
                return False, result_dict, used_tags, available_tags, current_confidence
            result_dict[attr_name] = canonical_val
            if tag in available_tags:
                available_tags.remove(tag)
            used_tags.append(tag)
            current_confidence += tag_conf
        return True, result_dict, used_tags, available_tags, current_confidence

    def _validate_at_least_one_entities(
        self, result_dict, available_tags, used_tags, current_confidence
    ):
        if not self.at_least_one:
            return True, result_dict, used_tags, available_tags, current_confidence

        resolved_entities = resolve_one_of(available_tags, self.at_least_one)
        if not resolved_entities:
            return False, result_dict, used_tags, available_tags, current_confidence

        for key, tag_list in resolved_entities.items():
            first_tag = tag_list[0]
            result_dict[key] = first_tag.get("key")
            first_entity_data = first_tag["entities"][0]
            conf_score = first_entity_data.get("confidence", 1.0)
            current_confidence += 1.0 * conf_score
            if first_tag in available_tags:
                available_tags.remove(first_tag)
            used_tags.append(first_tag)
        return True, result_dict, used_tags, available_tags, current_confidence

    def _validate_optional_entities(
        self, result_dict, available_tags, used_tags, current_confidence
    ):
        for entity_type, attr_name in self.optional:
            # Skip if already populated by 'at least one' logic
            if attr_name in result_dict:
                continue

            tag, canonical_val, tag_conf = find_first_tag(available_tags, entity_type)
            if tag:
                result_dict[attr_name] = canonical_val
                if tag in available_tags:
                    available_tags.remove(tag)
                used_tags.append(tag)
                current_confidence += tag_conf
        return result_dict, used_tags, available_tags, current_confidence

    def _calculate_final_confidence(
        self, sum_intent_conf, total_num_tags, base_parser_conf
    ):
        if total_num_tags == 0:
            return 0.0
        return (sum_intent_conf / total_num_tags) * base_parser_conf

    def _find_target_client(self, available_tags):
        client_tag, _, _ = find_first_tag(available_tags, CLIENT_ENTITY_NAME)
        return client_tag


class IntentBuilder(object):
    """
    Builder class for constructing Intent objects declaratively.

    Provides methods to specify required, optional, and 'at least one' entity types
    before building the final Intent instance.

    Example:
        intent = IntentBuilder("MyIntent")\
            .require("EntityA")\
            .one_of("EntityB", "EntityC")\
            .optionally("EntityD")\
            .build()
    """

    def __init__(self, intent_name):
        """
        Initializes the IntentBuilder.

        Args:
            intent_name (str): The name of the intent to be built.
        """
        self.name = intent_name
        self.requires = []
        self.at_least_one = []
        self.optional = []

    def one_of(self, *args):
        """
        Adds a group of entities where at least one must be present for the intent.

        Args:
            *args: Variable length argument list of entity type names.

        Returns:
            IntentBuilder: The current instance for method chaining.
        """
        if args:
            self.at_least_one.append(list(args))
        return self

    def require(self, entity_type, attribute_name=None):
        """
        Adds a required entity type to the intent.

        Args:
            entity_type (str): The type of the required entity.
            attribute_name (str, optional): The name of the attribute to store the entity's value.
                                            Defaults to the entity_type if not provided.

        Returns:
            IntentBuilder: The current instance for method chaining.
        """
        attr_name = attribute_name or entity_type
        self.requires.append((entity_type, attr_name))
        return self

    def optionally(self, entity_type, attribute_name=None):
        """
        Adds an optional entity type to the intent.

        Args:
            entity_type (str): The type of the optional entity.
            attribute_name (str, optional): The name of the attribute to store the entity's value.
                                            Defaults to the entity_type if not provided.

        Returns:
            IntentBuilder: The current instance for method chaining.
        """
        attr_name = attribute_name or entity_type
        self.optional.append((entity_type, attr_name))
        return self

    def build(self):
        """
        Constructs and returns the final Intent object based on the builder's configuration.

        Returns:
            Intent: An instance of the Intent class.
        """
        return Intent(self.name, self.requires, self.at_least_one, self.optional)
