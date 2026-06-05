#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""
Plugin SQL mapper
"""

import hashlib
import inspect
import threading
from abc import ABC, abstractmethod

import numpy as np
import urllib3
from jiuwen.common.store.gauss import DBUtil
from jiuwen.memory.store.buffer import BufferConversation
from jiuwen.plugin.common import constant
from jiuwen.plugin.common.utils.db_utils import (
    exception_handler,
    do_db_connect,
    get_current_datetime,
)
from jiuwen.plugin.models.base_model import Field
from jiuwen.plugin.models.request_model import RegisterItem
from psycopg2 import sql
from urllib3.exceptions import InsecureRequestWarning

urllib3.disable_warnings(InsecureRequestWarning)
urllib3.disable_warnings(UserWarning)


class PluginDB(ABC):
    """Interface for embedding models."""

    @classmethod
    @abstractmethod
    def init(cls):
        """init"""
        pass

    @abstractmethod
    def add_plugin(self, **kwargs):
        """add_plugin"""
        pass

    @abstractmethod
    def delete_plugin(self, **kwargs):
        """delete_plugin"""
        pass

    @abstractmethod
    def get_plugin(self, **kwargs):
        """get_plugin"""
        pass

    @abstractmethod
    def update_plugin(self, **kwargs):
        """update_plugin"""
        pass

    @abstractmethod
    def find_top_k_plugins(self, **kwargs):
        """find_top_k"""
        pass

    @abstractmethod
    def cal_record_num(self, **kwargs):
        """cal number of record"""
        pass


class GaussConn(PluginDB):
    """GaussConn"""

    _table_name = sql.Identifier(constant.PLUGIN_DEFAULT_TABLE_NAME)
    _columns = [
        m[0] for m in inspect.getmembers(RegisterItem) if isinstance(m[1], Field)
    ]

    @property
    def columns_placeholder_str(self):
        """columns_placeholder_str"""
        placeholders = "%s"
        return "(" + ", ".join(placeholders) + ")"

    @classmethod
    def init(cls):
        with exception_handler():
            DBUtil.init()

    @do_db_connect
    def add_plugin(self, **kwargs):
        kwargs = {
            key: value for key, value in kwargs.items() if value or value is False
        }
        kwargs["create_at"] = kwargs["update_at"] = get_current_datetime()
        columns = [sql.Identifier(column) for column in kwargs.keys()]
        formatted_query = sql.SQL(
            'INSERT INTO {table} ({column}) VALUES %s RETURNING "id"'
        ).format(table=self._table_name, column=sql.SQL(", ").join(columns))
        args = [tuple(kwargs.values())]
        with exception_handler():
            return DBUtil.query_return_cols(formatted_query, args=args)

    @do_db_connect
    def delete_plugin(self, **kwargs):
        kwargs = {
            key: value for key, value in kwargs.items() if value or value is False
        }
        conditions = sql.SQL(" AND ").join(
            [
                sql.SQL("{} in {}").format(
                    sql.Identifier(key), sql.Literal(tuple(value))
                )
                if isinstance(value, list)
                else sql.SQL("{} = {}").format(sql.Identifier(key), sql.Literal(value))
                for key, value in kwargs.items()
            ]
        )
        query = sql.SQL('DELETE FROM {table} WHERE {condition} RETURNING "id"')
        formatted_query = query.format(table=self._table_name, condition=conditions)
        with exception_handler():
            return DBUtil.query_return_cols(formatted_query)

    @do_db_connect
    def get_plugin(self, **kwargs):
        kwargs = {
            key: value for key, value in kwargs.items() if value or value is False
        }
        args = kwargs.pop("args", {})
        default_args = {"limit": 10000, "order_by": "id", "offset": 0}
        args = {
            key: args.get(key) if args.get(key) else value
            for key, value in default_args.items()
        }
        conditions = self.get_conditions(**kwargs)
        columns = sql.SQL(", ").join(
            [sql.Identifier(column) for column in self._columns]
        )
        if conditions.seq:
            query = sql.SQL(
                "SELECT {column} FROM {table} WHERE {condition} ORDER BY {order_by} DESC LIMIT {limit} OFFSET {offset}"
            )
        else:
            query = sql.SQL(
                "SELECT {column} FROM {table} ORDER BY {order_by} DESC LIMIT {limit} OFFSET {offset}"
            )
        formatted_query = query.format(
            column=columns,
            table=self._table_name,
            condition=conditions,
            order_by=sql.Identifier(args.get("order_by")),
            limit=sql.Literal(args.get("limit")),
            offset=sql.Literal(args.get("offset")),
        )
        with exception_handler():
            rows, column_names = DBUtil.query_return_cols(formatted_query)
        plugins = [dict(zip(column_names, row)) for row in rows]
        return plugins

    @do_db_connect
    def cal_record_num(self, **kwargs):
        """cal number of record"""
        conditions = self.get_conditions(**kwargs)
        query = sql.SQL("SELECT COUNT(*) FROM {table} WHERE {condition}").format(
            table=self._table_name, condition=conditions
        )
        with exception_handler():
            rows = DBUtil.query(query)
        plugin_num = rows[0][0]
        return plugin_num

    @do_db_connect
    def update_plugin(self, **kwargs):
        kwargs = {
            key: value for key, value in kwargs.items() if value or value is False
        }
        kwargs["update_at"] = get_current_datetime()
        condition_dict = kwargs.pop("conditions")
        conditions = sql.SQL(" AND ").join(
            [
                sql.SQL("{} = {}").format(sql.Identifier(key), sql.Literal(value))
                for key, value in condition_dict.items()
                if value or value is False
            ]
        )
        columns_list = [sql.Identifier(column) for column in kwargs.keys()]
        formatted_query = sql.SQL(
            'UPDATE {table} SET ({column}) = %s WHERE {condition} RETURNING "id"'
        ).format(
            table=self._table_name,
            column=sql.SQL(", ").join(columns_list),
            condition=conditions,
        )
        args = (tuple(kwargs.values()),)
        with exception_handler():
            return DBUtil.query_return_cols(formatted_query, args=args)

    @do_db_connect
    def find_top_k_plugins(self, **kwargs):
        embedding_str = kwargs.pop("embedding_str")
        k = kwargs.pop("top_k")
        offset = kwargs.pop("offset")
        if embedding_str == "None" or not embedding_str:
            return []
        columns_list = [sql.Identifier(column) for column in self._columns]
        kwargs = {
            key: value
            for key, value in kwargs.items()
            if key in self._columns and (value or value is False)
        }
        if kwargs:
            conditions = sql.SQL(" AND ").join(
                [
                    sql.SQL("{} = {}").format(sql.Identifier(key), sql.Literal(value))
                    for key, value in kwargs.items()
                ]
            )
            formatted_query = sql.SQL(
                "SELECT {column} FROM {table} where {conditions} ORDER BY embedding <-> {embedding} LIMIT "
                "{topk} OFFSET {offset}"
            ).format(
                column=sql.SQL(", ").join(columns_list),
                table=self._table_name,
                conditions=conditions,
                embedding=sql.Literal(embedding_str),
                topk=sql.Literal(k),
                offset=sql.Literal(offset),
            )
        else:
            formatted_query = sql.SQL(
                "SELECT {column} FROM {table}  ORDER BY embedding <-> {embedding} LIMIT {topk}"
            ).format(
                column=sql.SQL(", ").join(columns_list),
                table=self._table_name,
                embedding=sql.Literal(embedding_str),
                topk=sql.Literal(k),
            )
        params = tuple(kwargs.values())
        with exception_handler():
            rows, column_names = DBUtil.query_return_cols(formatted_query, args=params)
        plugins = [dict(zip(column_names, row)) for row in rows]
        return plugins

    def get_conditions(self, **kwargs):
        """get conditions"""
        kwargs = {
            key: value for key, value in kwargs.items() if value or value is False
        }
        like_args = {
            key: value
            for key, value in kwargs.pop("like_args", {}).items()
            if value or value is False
        }
        not_like_args = {
            key: val
            for key, val in kwargs.pop("not_like_args", {}).items()
            if val or val is False
        }
        conditions = [
            sql.SQL("{} in {}").format(sql.Identifier(key), sql.Literal(tuple(value)))
            if isinstance(value, list)
            else sql.SQL("{} = {}").format(sql.Identifier(key), sql.Literal(value))
            for key, value in kwargs.items()
        ]
        if not_like_args:
            conditions += [
                sql.SQL("{} NOT LIKE {}").format(
                    sql.Identifier(key), sql.Literal(f"%{value}%")
                )
                for key, value in not_like_args.items()
            ]
        conditions = sql.SQL(" AND ").join(conditions)
        if like_args:
            like_conditions = sql.SQL(" OR ").join(
                [
                    sql.SQL("{} LIKE {}").format(
                        sql.Identifier(key), sql.Literal(f"%{value}%")
                    )
                    for key, value in like_args.items()
                ]
            )
            conditions = sql.SQL("{} AND ({})").format(conditions, like_conditions)
        return conditions


class PluginInMemory(PluginDB):
    """PluginInMemory"""

    _instance = None
    cache: BufferConversation
    _columns = [
        m[0] for m in inspect.getmembers(RegisterItem) if isinstance(m[1], Field)
    ]

    def __new__(cls, *args, **kwargs):
        if cls._instance is None:
            cls._instance = super().__new__(
                cls, *args, **kwargs
            )  # 调用父类的__new__方法创建一个新的对象
        return cls._instance

    def __init__(self, maxsize=1000, ttl=0, policy="Cache"):
        self.maxsize = maxsize
        self.ttl = ttl
        self.policy = policy
        self.cache = BufferConversation(maxsize=maxsize, ttl=ttl, policy=policy)

    @classmethod
    def init(cls):
        pass

    def add_plugin(self, **kwargs):
        name = self._concat_name(
            kwargs.get("plugin_name", ""), kwargs.get("func_name", "")
        )
        self.cache.add(name, kwargs)

    def delete_plugin(self, **kwargs):
        if kwargs.get("func_name"):
            name = self._concat_name(
                kwargs.get("plugin_name", ""), kwargs.get("func_name", "")
            )
            self.cache.delete(name)
        else:
            plugin_items = self.cache.get_all()
            for name, plugin in plugin_items.items():
                if name.startswith(plugin.get("plugin_name", "") + " + "):
                    self.cache.delete(name)

    def get_plugin(self, **kwargs):
        if kwargs.get("func_name"):
            name = self._concat_name(
                kwargs.get("plugin_name", ""), kwargs.get("func_name", "")
            )
            plugin_get = self.cache.get(name)
            if plugin_get:
                plugin_items = dict()
                plugin_items[name] = plugin_get
                plugins = self._paser_plugin(plugin_items)
            else:
                plugins = []
        else:
            plugin_items = self.cache.get_all()
            plugins = self._paser_plugin(
                plugin_items, plugin_select=kwargs.get("plugin_name")
            )
        return plugins

    def cal_record_num(self, **kwargs):
        """cal number of record"""
        return len(self.cache)

    def update_plugin(self, **kwargs):
        name = self._concat_name(kwargs.get("plugin_name"), kwargs.get("func_name"))
        if self.cache.get(name):
            self.cache.delete(name)
            self.cache.add(name, kwargs)

    def find_top_k_plugins(self, **kwargs):
        embedding_str = kwargs.get("embedding_str")
        k = kwargs.get("top_k")
        if embedding_str == "None" or not embedding_str:
            return []
        plugin_items = self.cache.get_all()
        top_k_items = self._sort_items_by_emb(plugin_items, embedding_str, k)
        plugins = self._paser_plugin(top_k_items)
        return plugins

    def _paser_plugin(self, plugin_items, plugin_select=""):
        plugins = []
        for name, plugin in plugin_items.items():
            if plugin_select and not name.startswith(plugin_select + " + "):
                continue  # 解析时限定了插件，则只选择对应插件
            for attr in self._columns:
                if attr not in plugin:
                    plugin[attr] = None
            dict_cols = {k: v for k, v in plugin.items() if k in self._columns}
            plugins.append(dict_cols)
        return plugins

    def _concat_name(self, plugin_name, func_name):
        return plugin_name + " + " + func_name

    def _str_to_float_array(self, emb_str):
        str_list = emb_str.strip("[").strip("]").split(",")
        float_list = [float(s) for s in str_list]
        float_array = np.array(float_list)
        return float_array

    def _cosine_similarity(self, array1, array2):
        dot_product = np.dot(array1, array2)
        norm1 = np.linalg.norm(array1)
        norm2 = np.linalg.norm(array2)
        cos_sim = dot_product / (norm1 * norm2)
        return cos_sim

    def _sort_items_by_emb(self, items, emb_query, k):
        emb_query = self._str_to_float_array(emb_query)
        items_and_sims = []
        for _, item in items.items():
            emb = self._str_to_float_array(item["embedding"])
            sim = self._cosine_similarity(emb, emb_query)
            items_and_sims.append((item, sim))
        items_and_sims.sort(key=lambda x: x[1], reverse=True)
        top_k_plugin = dict()
        for x in items_and_sims[:k]:
            plugin = x[0]
            name = self._concat_name(plugin.get("plugin_name"), plugin.get("func_name"))
            top_k_plugin[name] = plugin
        return top_k_plugin


class FuncitonInMemory:
    """FuncitonInMemory"""

    function_call_cache: BufferConversation
    _instance = None
    _init_flag = False
    _singleton_lock = threading.Lock()

    def __new__(cls, *args, **kwargs):
        if cls._instance is None:
            cls._instance = super().__new__(cls, *args, **kwargs)
        return cls._instance

    def __init__(self):
        if not FuncitonInMemory._init_flag:
            with FuncitonInMemory._singleton_lock:
                if not FuncitonInMemory._init_flag:
                    self.maxsize = None
                    self.policy = None
                    self.cache = None
                    self.ttl = None
                    FuncitonInMemory._init_flag = True

    def set_config(self, maxsize=1000, ttl=0, policy="Cache"):
        """set_config"""
        self.maxsize = maxsize
        self.ttl = ttl
        self.policy = policy
        self.cache = BufferConversation(maxsize=maxsize, ttl=ttl, policy=policy)

    def get(self, function_str):
        """get"""
        if not function_str:
            return None
        key = hashlib.sha256(function_str.encode("utf-8")).hexdigest()
        # 不存在就返回None
        return self.cache.get(key)

    def set(self, function_str, function):
        """set"""
        if not function_str:
            return
        key = hashlib.sha256(function_str.encode("utf-8")).hexdigest()
        self.cache.add(key, function)
