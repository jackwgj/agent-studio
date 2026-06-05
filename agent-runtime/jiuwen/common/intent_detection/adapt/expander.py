#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.


class SimpleGraph(object):
    """This class is to graph connected nodes
    Note:
        hash is a type that is hashable so independant values and tuples
        but not objects, classes or lists.
    """

    def __init__(self):
        """init an empty set"""
        self.adjacency_lists = {}

    def add_edge(self, a, b):
        """Used to add edges to the graph. 'a' and 'b' are vertexes and
        if 'a' or 'b' doesn't exisit then the vertex is created

        Args:
            a (hash): is one vertex of the edge
            b (hash): is another vertext of the edge


        """
        # Ensure both vertices exist in adjacency list
        if a not in self.adjacency_lists:
            self.adjacency_lists[a] = set()
        if b not in self.adjacency_lists:
            self.adjacency_lists[b] = set()

        # Add bidirectional connection
        self.adjacency_lists[a].add(b)
        self.adjacency_lists[b].add(a)

    def get_neighbors_of(self, a):
        """This will return the neighbors of the vertex

        Args:
            a (hash): is the vertex to get the neighbors for

        Returns:
            [] : a list of neighbors_of 'a'
                Will return an empty set if 'a' doesn't exist or has no
                neightbors.

        """
        return self.adjacency_lists.get(a, set())

    def vertex_set(self):
        """This returns a list of vertexes included in graph

        Returns:
            [] : a list of vertexes include in graph
        """
        return list(self.adjacency_lists)


def bronk(r, p, x, graph):
    """This is used to fine cliques and remove them from graph

    Args:
        graph (graph): this is the graph of verticies to search for
            cliques
        p (list): this is a list of the verticies to search
        r (list): used by bronk for the search
        x (list): used by bronk for the search

    Yields:
        list : found clique of the given graph and verticies
    """
    if not p and not x:
        yield r
        return

    for vertex in p[:]:
        new_r = r + [vertex]
        neighbors = graph.get_neighbors_of(vertex)
        new_p = [v for v in p if v in neighbors]
        new_x = [v for v in x if v in neighbors]

        yield from bronk(new_r, new_p, new_x, graph)

        p.remove(vertex)
        x.append(vertex)


def get_cliques(vertices, graph):
    """get cliques

    Args:
        vertices (list) : list of the verticies to search for cliques
        graph (graph) : a graph used to find the cliques using verticies

    Yields:
        list: a clique from the graph
    """
    yield from bronk([], vertices, [], graph)


def graph_key_from_tag(tag, entity_index):
    """Returns a key from a tag entity

    Args:
        tag (dict) : this is the tag selected to get the key from
        entity_index (int) : this is the index of the tagged entity

    Returns:
        str : String representing the key for the given tagged entity.
    """
    start_token = tag.get("start_token", 0)
    entities = tag.get("entities", [])
    if entity_index >= len(entities):
        raise IndexError("Entity index out of range")

    entity = entities[entity_index]
    key = entity.get("key")
    confidence = entity.get("confidence")
    return f"{start_token}-{key}-{confidence}"


class Lattice(object):
    """This manages a list of items or lists

    Attributes:
        nodes (list) : is a list of items or lists.
            This is used to track items and lists that are a part of the
            Lattice
    """

    def __init__(self):
        """Creates the Lattice with an empty list"""
        self.nodes = []

    def append(self, data):
        """Appends items or lists to the Lattice

        Args:
            data (item,list) : The Item or List to be added to the Lattice
        """
        if isinstance(data, list) and data:
            self.nodes.append(data)
        else:
            self.nodes.append([data])

    def traverse(self, index=0):
        """This is used to produce a list of lists where each each item
        in that list is a diffrent combination of items from the lists
        within with every combination of such values.

        Args:
            index (int) : the index at witch to start the list.
                Note this is used only in the function as a processing

        Returns:
            list : is every combination.
        """
        if index >= len(self.nodes):
            yield []
            return

        current_node = self.nodes[index]
        for entity in current_node:
            for next_result in self.traverse(index + 1):
                if isinstance(entity, list):
                    yield entity + next_result
                else:
                    yield [entity] + next_result


class BronKerboschExpander(object):
    """
    BronKerboschExpander
    """

    def __init__(self, tokenizer):
        self.tokenizer = tokenizer

    def expand(self, tags, clique_scoring_func=None):
        """This is the main function to expand tags into cliques

        Args:
            tags (list): a list of tags to find the cliques.
            clique_scoring_func (func): a function that returns a float
                value for the clique

        Returns:
            list : a list of cliques
        """
        lattice = Lattice()
        overlapping_spans = []

        for tag in tags:
            if overlapping_spans and self._get_max_end_token(
                overlapping_spans
            ) >= tag.get("start_token"):
                overlapping_spans.append(tag)
            else:
                if len(overlapping_spans) > 1:
                    cliques = list(self._sub_expand(overlapping_spans))
                    if clique_scoring_func:
                        cliques = sorted(cliques, key=clique_scoring_func, reverse=True)
                    lattice.append(cliques)
                else:
                    lattice.append(overlapping_spans)

                overlapping_spans = [tag]

        # Handle remaining overlapping spans
        if len(overlapping_spans) > 1:
            cliques = list(self._sub_expand(overlapping_spans))
            if clique_scoring_func:
                cliques = sorted(cliques, key=clique_scoring_func, reverse=True)
            lattice.append(cliques)
        else:
            lattice.append(overlapping_spans)

        return list(lattice.traverse())

    def _get_max_end_token(self, spans):
        """Get maximum end token from spans."""
        return max([span.get("end_token") for span in spans])

    def _build_graph(self, tags):
        """Builds a graph from the entities included in the tags.
        Note this is used internally.

        Args:
            tags (list): A list of the tags to include in graph

        Returns:
            graph : this is the resulting graph of the tagged entities.

        """
        graph = SimpleGraph()
        for tag_index, tag in enumerate(tags):
            entities = tag.get("entities", [])
            tag_start = tag.get("start_token", 0)

            for entity_index, entity in enumerate(entities):
                a_entity_name = graph_key_from_tag(tag, entity_index)
                match_text = entity.get("match", "")
                tokens = self.tokenizer.tokenize(match_text)
                token_span_end = tag_start + len(tokens)

                self._add_edges_to_later_entities(
                    graph, a_entity_name, token_span_end, tags[tag_index + 1 :]
                )
        return graph

    def _add_edges_to_later_entities(
        self, graph, a_entity_name, token_span_end, later_tags
    ):
        for later_tag in later_tags:
            later_start = later_tag.get("start_token", 0)
            if later_start < token_span_end:
                continue
            later_entities = later_tag.get("entities", [])
            for b_entity_index in range(len(later_entities)):
                b_entity_name = graph_key_from_tag(later_tag, b_entity_index)
                graph.add_edge(a_entity_name, b_entity_name)

    def _sub_expand(self, tags):
        """Yield cliques of tags, each clique sorted by start_token.

        Args:
            tags (list): List of tag dicts containing 'entities'.

        Yields:
            list: Sorted list of tag-like dicts representing a clique.
        """
        entities = self._build_entities_map(tags)
        graph = self._build_graph(tags)

        for clique in get_cliques(list(entities), graph):
            clique_tags = self._build_clique_tags(clique, entities)
            clique_tags.sort(key=lambda e: e["start_token"])
            yield clique_tags

    def _build_entities_map(self, tags):
        entities = {}
        for tag in tags:
            entities_list = tag.get("entities", [])
            for entity_index, entity in enumerate(entities_list):
                node_name = graph_key_from_tag(tag, entity_index)
                if node_name not in entities:
                    entities[node_name] = [entity, entity.get("confidence"), tag]
        return entities

    def _build_clique_tags(self, clique, entities):
        """Convert a clique (list of node names) into list of tag dicts."""
        result = []
        for entity_name in clique:
            start_token = self._parse_start_token(entity_name)
            entity_data = entities.get(entity_name)
            if entity_data:
                result.append(self._make_tag_from_entity_data(entity_data, start_token))
        return result

    def _parse_start_token(self, entity_name):
        """Extract start_token from entity_name (format: 'start-end-key')."""
        return int(entity_name.split("-", 2)[0])

    def _make_tag_from_entity_data(self, entity_data, start_token):
        """Build a tag dict from entity data and start_token."""
        entity, confidence, old_tag = entity_data
        return {
            "start_token": start_token,
            "entities": [entity],
            "confidence": confidence * old_tag.get("confidence", 1.0),
            "end_token": old_tag.get("end_token"),
            "match": entity.get("match"),
            "key": entity.get("key"),
            "from_context": old_tag.get("from_context", False),
        }
