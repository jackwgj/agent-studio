# -*- coding: utf-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
"""MYSQL utils"""

import os
import threading
from typing import List, Dict, Any

import pymysql
from agent_builder.common.exception.status_code import StatusCode
from agent_builder.common.security.cloudmap import rainbow
from agent_builder.adapter.exception_bridge import JiuWenBaseException
from agent_builder.adapter.cryptor import Crypt
from pydantic import BaseModel

VALID_TABLES = {"job_info", "optimize_info", "progress", "history", "cases"}


# 默认数据库读写超时阈值
DEFAULT_WR_TIMEOUT: int = 60


class MysqlConfig:
    """Mysql config."""

    def __init__(self):
        self.host = None
        self.port = None
        self.user = None
        self.password = None
        self.database = None

        is_mysql_mode = bool(os.getenv("STORE_MYSQL_ENABLE", "").lower() == "true")
        if is_mysql_mode:
            self.__init_config_mysql()
        else:
            self.__init_config_from_cloudmap()

    @staticmethod
    def _raise_config_error(error_msg: str):
        return JiuWenBaseException(
            StatusCode.MYSQL_CHECK_CONFIG_ERROR.code,
            StatusCode.MYSQL_CHECK_CONFIG_ERROR.errmsg.format(
                errno=-1, reason=error_msg
            ),
        )

    def __init_config_from_cloudmap(self):
        env_prefix = "CLOUD_MAP_"
        server_addr = os.getenv(env_prefix + "SERVER_ADDR")
        if not server_addr:
            raise self._raise_config_error("Invalid cloudmap server address")
        env_prefix = "CLOUD_MAP_" + "DB_"
        namespace = os.getenv(env_prefix + "NAMESPACE")
        if not namespace:
            raise self._raise_config_error("Invalid cloudmap namespace")
        cluster = os.getenv(env_prefix + "CLUSTER")
        if not cluster:
            raise self._raise_config_error("Invalid cloudmap cluster")
        database = os.getenv(env_prefix + "DATABASE")
        if not database:
            items = cluster.strip().split("@@")
            if len(items) != 2:
                raise self._raise_config_error(
                    "CloudMap failed to parse database name from cluster"
                )
            database = items[-1]
        properties = {
            "serverAddr": server_addr,
            "namespaceName": namespace,
        }
        try:
            data_source_info = rainbow.get_data_source_info(properties, cluster)
        except Exception as e:
            raise JiuWenBaseException(
                StatusCode.MYSQL_CHECK_CONFIG_ERROR.code,
                StatusCode.MYSQL_CHECK_CONFIG_ERROR.errmsg.format(
                    errno=-1, reason="CloudMap failed to get datasource info"
                ),
            ) from e
        if not data_source_info.addresses:
            raise self._raise_config_error(
                "CloudMap failed to get mysql service addresses"
            )
        address = data_source_info.addresses[0]
        items = address.ip_list.strip().split(":")
        if len(items) != 2:
            raise self._raise_config_error("CloudMap get a invalid address ip list")
        host, port = items[0], items[1]
        self.host = host
        self.port = int(port)
        self.user = data_source_info.account.user_name
        self.password = data_source_info.account.password
        self.database = database

    def __init_config_mysql(self):
        host = os.getenv("STORE_MYSQL_HOST", None)
        port = os.getenv("STORE_MYSQL_PORT", None)
        user = os.getenv("STORE_MYSQL_USER", None)
        password = os.getenv("STORE_MYSQL_PASSWORD", None)
        database = os.getenv("STORE_MYSQL_DATABASE", None)
        if not host or not port or not user:
            self._raise_config_error("Invalid mysql config")
        if not password or not database:
            self._raise_config_error("Invalid mysql config")
        try:
            self.host = host
            self.port = int(port)
            self.user = user
            self.password = Crypt().decrypt(password)
            self.database = database
        except Exception as e:
            self._raise_config_error(f"{str(e)}")


class MysqlUtil:
    """Mysql operation"""

    _instance = None
    _singleton_lock = threading.Lock()
    _rw_lock = threading.Lock()

    @classmethod
    def instance(cls, reconnect=False):
        """Get an mysql instance."""
        if cls._instance is None or reconnect:
            with cls._singleton_lock:
                # 单例标准写法：判断两次，多个线程可以避免不必要的锁竞争
                if cls._instance is None or reconnect:
                    mysql_env_config = MysqlConfig()
                    # 优先使用函数参数值，参数值为空时则取环境变量默认值
                    host = mysql_env_config.host
                    user = mysql_env_config.user
                    password = mysql_env_config.password
                    port = mysql_env_config.port
                    database = mysql_env_config.database

                    try:
                        cls._instance = pymysql.connect(
                            host=host,
                            port=int(port),
                            user=user,
                            password=password,
                            database=database,
                            read_timeout=DEFAULT_WR_TIMEOUT,
                            write_timeout=DEFAULT_WR_TIMEOUT,
                        )
                    except pymysql.MySQLError as e:
                        errno, reason = e.args[0], e.args[1]
                        raise JiuWenBaseException(
                            StatusCode.MYSQL_CHECK_CONFIG_ERROR.code,
                            StatusCode.MYSQL_CHECK_CONFIG_ERROR.errmsg.format(
                                errno=errno, reason=reason
                            ),
                        ) from e
                    except Exception as e:
                        raise JiuWenBaseException(
                            error_code=StatusCode.MYSQL_EXECUTION_ERROR.code,
                            message=StatusCode.MYSQL_EXECUTION_ERROR.errmsg.format(
                                errno=-1, reason=str(e)
                            ),
                        ) from e

        return cls._instance

    @classmethod
    def execute(cls, sql, fetch_all: bool = True, params=None):
        """Execute sql command"""
        with cls._rw_lock:
            try:
                cursor = cls._instance.cursor()
                cursor.execute(sql, params)
            except pymysql.Error:
                cls.instance(reconnect=True)
                cursor = cls._instance.cursor()
                cursor.execute(sql, params)
            try:
                cls._instance.commit()
                data = cursor.fetchall() if fetch_all else cursor.fetchone()
                cursor.close()
            except pymysql.Error as e:
                errno, reason = -1, "pymysql connection error"
                if isinstance(e.args, tuple) and len(e.args) >= 2:
                    errno, reason = e.args[0], "pymysql connection error"
                raise JiuWenBaseException(
                    error_code=StatusCode.MYSQL_EXECUTION_ERROR.code,
                    message=StatusCode.MYSQL_EXECUTION_ERROR.errmsg.format(
                        errno=errno, reason=reason
                    ),
                ) from e
            except Exception as e:
                raise JiuWenBaseException(
                    error_code=StatusCode.MYSQL_EXECUTION_ERROR.code,
                    message=StatusCode.MYSQL_EXECUTION_ERROR.errmsg.format(
                        errno=-1, reason=str(e)
                    ),
                ) from e
            finally:
                if cursor:  # 确保游标被关闭
                    cursor.close()
            return data


class MysqlTable:
    """Mysql table"""

    class Field(BaseModel):
        """Mysql field"""

        name: str = ""
        type: str = ""
        is_not_null: bool = True
        others: List[str] = None

    def __init__(self, table_name: str, fields: List[Field], primary_keys: List[str]):
        self._table_name = table_name
        self._primary_keys = primary_keys
        self._fields = fields
        self._sql_fields_string = ", ".join([f"`{f.name}`" for f in self._fields])
        self._sql_values_fstring = ", ".join([f"'{{{f.name}}}'" for f in self._fields])

    @property
    def table_name(self) -> str:
        """table name"""
        return self._table_name

    @property
    def fields(self) -> List[Field]:
        """table fields"""
        return self._fields

    def create(self):
        """create table"""
        result = MysqlUtil.execute(f"SHOW TABLES LIKE '{self._table_name}';")
        if not result:
            fields_str = ", ".join(
                [
                    f"`{f.name}` {f.type} {'NOT NULL' if f.is_not_null else ''}"
                    for f in self._fields
                ]
            )
            primary_keys_str = ", ".join([f"`{key}`" for key in self._primary_keys])
            MysqlUtil.execute(
                f"CREATE TABLE `{self._table_name}` ({fields_str}, PRIMARY KEY({primary_keys_str}));"
            )

    def insert(self, values: Dict[str, Any]):
        """insert values"""
        self._validate_insert_values(values)
        placeholders = ", ".join(["%s"] * len(self._fields))  # 名称使用占位符
        condition_values = tuple(values[f.name] for f in self._fields)

        sql = f"REPLACE INTO {self._table_name} ({self._sql_fields_string}) VALUES ({placeholders});"
        MysqlUtil.execute(sql, params=condition_values)

    def update(self, field: str, value: Any, **conditions: Dict[str, Any]):
        """update values"""
        if self._table_name not in VALID_TABLES:
            raise ValueError("mysql update failed caused by illegal table name")

        if field not in [f.name for f in self._fields]:
            raise ValueError("mysql update failed caused by illegal field name")

        sql_fields_string = f"{field} = %s"
        conditions_sql = " AND ".join([f"{key} = %s" for key in conditions])
        # 构建完整的 SQL 查询
        sql = (
            f"UPDATE {self._table_name} SET {sql_fields_string} WHERE {conditions_sql};"
        )
        params = (value,) + tuple(conditions.values())
        MysqlUtil.execute(sql, params=params)

    def exists(self, **conditions: Dict[str, Any]) -> bool:
        """exists values"""
        if not conditions:
            return False

        # 构建安全的WHERE条件
        condition_keys = []
        condition_values = []

        for key, value in conditions.items():
            condition_keys.append(f"{key}=%s")
            condition_values.append(value)

        conditions_sql = " AND ".join(condition_keys)

        # 使用参数化查询
        sql = f"SELECT * FROM {self._table_name} WHERE {conditions_sql}"
        result = MysqlUtil.execute(sql, params=condition_values)
        return len(result) > 0

    def query(self, target: str = "*", **conditions: Dict[str, Any]) -> Dict[str, Any]:
        """query values"""
        if target != "*" and target not in [field.name for field in self.fields]:
            raise ValueError("mysql query failed caused by illegal target")
        if not conditions:
            return {}

        # 构建安全的WHERE条件
        condition_keys = []
        condition_values = []

        for key, value in conditions.items():
            condition_keys.append(f"{key}=%s")
            condition_values.append(value)

        conditions_sql = " AND ".join(condition_keys)

        # 使用参数化查询
        sql = f"SELECT {target} FROM {self._table_name} WHERE {conditions_sql}"
        result = MysqlUtil.execute(sql, params=condition_values, fetch_all=False)
        if not result:
            return {}

        output = {}
        if target == "*":
            for field, value in zip(self._fields, result):
                output[field.name] = value
        else:
            # 目前只支持单field查询
            output[target] = result[0]

        return output

    def batch_query(
        self, target: str = "*", **conditions: Dict[str, Any]
    ) -> List[Dict[str, Any]]:
        """query batch values"""
        if target != "*" and target not in [field.name for field in self.fields]:
            raise ValueError("mysql query failed caused by illegal target")
        if not conditions:
            return []

        # 构建安全的WHERE条件
        condition_keys = []
        condition_values = []
        for key, value in conditions.items():
            condition_keys.append(f"{key}=%s")
            condition_values.append(value)

        conditions_sql = " AND ".join(condition_keys)

        # 使用参数化查询
        sql = f"SELECT {target} FROM {self.table_name} WHERE {conditions_sql}"
        results = MysqlUtil.execute(sql, params=condition_values)

        if not results:
            return []

        output = []
        for res in results:
            output.append(dict())
            for field, value in zip(self._fields, res):
                output[-1][field.name] = value

        return output

    def delete(self, **conditions: Dict[str, Any]):
        """delete values"""
        if not conditions:
            return

        # 构建安全的WHERE条件
        condition_keys = []
        condition_values = []

        for key, value in conditions.items():
            condition_keys.append(f"{key}=%s")
            condition_values.append(value)

        conditions_sql = " AND ".join(condition_keys)

        # 使用参数化查询
        sql = f"DELETE FROM {self._table_name} WHERE {conditions_sql}"
        MysqlUtil.execute(sql, fetch_all=False, params=condition_values)

    def _validate_insert_values(self, values: Dict[str, Any]) -> bool:
        """validate values"""
        if len(values) != len(self._fields):
            raise JiuWenBaseException(
                error_code=StatusCode.MYSQL_SQL_COMMAND_ERROR.code,
                message=StatusCode.MYSQL_SQL_COMMAND_ERROR.errmsg.format(
                    error_msg=f"table {self._table_name} expects {len(self._fields)} fields: {self._fields},"
                    f"but got {len(values)} values.",
                ),
            )

        for key in values:
            if key not in set([f.name for f in self._fields]):
                raise JiuWenBaseException(
                    error_code=StatusCode.MYSQL_SQL_COMMAND_ERROR.code,
                    message=StatusCode.MYSQL_SQL_COMMAND_ERROR.errmsg.format(
                        error_msg=f"table {self._table_name} has no column '{key}'"
                    ),
                )
        return True
