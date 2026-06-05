# -*- coding: utf-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
"""
Plugin Workflow ref SQL mapper
"""

import inspect
from abc import ABC, abstractmethod

from jiuwen.common.store.gauss import DBUtil
from jiuwen.plugin.common import constant
from jiuwen.plugin.common.utils.db_utils import (
    exception_handler,
    do_db_connect,
    get_current_datetime,
)
from jiuwen.plugin.models.base_model import Field
from jiuwen.plugin.models.request_model import PluginFLowRefRegisterItem
from psycopg2 import sql


class PluginFlowRefDB(ABC):
    """Plugin flow ref DB"""

    @classmethod
    @abstractmethod
    def init(cls):
        """init"""
        pass

    @abstractmethod
    def add_ref(self, **kwargs):
        """add ref"""
        pass

    @abstractmethod
    def delete_ref(self, **kwargs):
        """delete ref"""
        pass

    @abstractmethod
    def get_ref(self, **kwargs):
        """get ref"""
        pass

    @abstractmethod
    def update_ref(self, **kwargs):
        """update ref"""
        pass


class PluginFlowGaussDB(PluginFlowRefDB):
    """
    GaussDB interface for PluginWorkflowRef
    """

    _table_name = sql.Identifier(constant.PLUGIN_WORKFLOW_REF_DEFAULT_TABLE_NAME)
    _columns = [
        m[0]
        for m in inspect.getmembers(PluginFLowRefRegisterItem)
        if isinstance(m[1], Field)
    ]

    @classmethod
    def init(cls):
        with exception_handler():
            DBUtil.init()

    @do_db_connect
    def add_ref(self, **kwargs):
        kwargs = {
            key: value for key, value in kwargs.items() if value or value is False
        }
        kwargs["create_at"] = kwargs["update_at"] = get_current_datetime()
        columns = [sql.Identifier(column) for column in kwargs.keys()]
        args = [tuple(kwargs.values())]
        formatted_query = sql.SQL(
            'INSERT INTO {table} ({column}) VALUES %s RETURNING "id"'
        ).format(table=self._table_name, column=sql.SQL(", ").join(columns))
        with exception_handler():
            return DBUtil.query_return_cols(formatted_query, args=args)

    @do_db_connect
    def delete_ref(self, **kwargs):
        kwargs = {
            key: value for key, value in kwargs.items() if value or value is False
        }
        conditions = self.get_conditions(**kwargs)
        query = sql.SQL('DELETE FROM {table} WHERE {condition} RETURNING "id"')
        formatted_query = query.format(table=self._table_name, condition=conditions)
        with exception_handler():
            return DBUtil.query_return_cols(formatted_query)

    @do_db_connect
    def get_ref(self, **kwargs):
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
        if conditions.seq:
            query = sql.SQL(
                "SELECT {column} FROM {table} WHERE {condition} ORDER BY {order_by} DESC LIMIT {limit} OFFSET {offset}"
            )
        else:
            query = sql.SQL(
                "SELECT {column} FROM {table} ORDER BY {order_by} DESC LIMIT {limit} OFFSET {offset}"
            )
        columns = sql.SQL(", ").join(
            [sql.Identifier(column) for column in self._columns]
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
        plugin_flow_refs = [dict(zip(column_names, row)) for row in rows]
        return plugin_flow_refs

    @do_db_connect
    def update_ref(self, **kwargs):
        kwargs = {
            key: value for key, value in kwargs.items() if value or value is False
        }
        kwargs["update_at"] = get_current_datetime()
        condition_dict = kwargs.pop("conditions")
        condition_list = []
        for key, value in condition_dict.items():
            if value or value is False:
                condition_list.append(
                    sql.SQL("{} = {}").format(sql.Identifier(key), sql.Literal(value))
                )
        conditions = sql.SQL(" AND ").join(condition_list)
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

    def get_conditions(self, **kwargs):
        """get conditions"""
        condition_list = []
        for key, value in kwargs.items():
            if isinstance(value, list):
                condition_list.append(
                    sql.SQL("{} in {}").format(
                        sql.Identifier(key), sql.Literal(tuple(value))
                    )
                )
            else:
                condition_list.append(
                    sql.SQL("{} = {}").format(sql.Identifier(key), sql.Literal(value))
                )
        return sql.SQL(" AND ").join(condition_list)
