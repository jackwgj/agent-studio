#!/usr/bin/env python
# coding: utf-8
# Copyright (c) Huawei Technologies Co., Ltd. 2025. All rights reserved.
import json
from typing import Tuple, Any

from jiuwen.common.log.base import logger
from jiuwen.context.memory_engine.store.base_kv_store import BaseKVStore


class KVStoreWrapper:
    def __init__(self, kv_store: BaseKVStore):
        self._kv_store: BaseKVStore = kv_store
        if not self._kv_store:
            raise ValueError("KV Store not initialized.")
        self.separator = "/"

    async def set(self, main_key: list[str], sub_key: list[str], value: str):
        """
        Set a value in the key-value store under a composite key.
        Args:
            main_key (list[str]): A list of strings representing the main hierarchical key.
            sub_key (list[str]): A list of strings representing the sub-key path.
            value (str): The string value to store.
        Returns:
            None
        """
        logger.debug(
            f"Start to set main_key: {main_key}, sub_key:{sub_key}, value: {value}"
        )
        try:
            main_key_str, new_value = self._build_main_key_value(
                main_key, sub_key, value
            )
        except KeyError as e:
            logger.error(f"Failed to set, error: {str(e)}")
            return

        if not sub_key:
            await self._kv_store.set(main_key_str, value)
            return

        exist_value = await self._kv_store.get(main_key_str)
        logger.debug(f"Before set, get value: {exist_value}")
        if not exist_value:
            await self._kv_store.set(
                main_key_str, json.dumps(new_value, ensure_ascii=False)
            )
            return

        merge_value = self._merge_value(new_value=new_value, exist_value=exist_value)
        logger.debug(f"Set main_key: {main_key}, merge_value: {merge_value}")
        await self._kv_store.set(main_key_str, merge_value)

    async def get(self, main_key: list[str], sub_key: list[str]) -> str | None:
        """
        Retrieve a value from the key-value store using a composite key.
        Args:
            main_key (list[str]): A list of strings representing the main hierarchical key.
            sub_key (list[str]): A list of strings representing the sub-key path.
        Returns:
            str | None: The stored value if found, otherwise None.
        """
        logger.debug(f"Start to get main_key: {main_key}, sub_key:{sub_key}")

        main_key = self.separator.join(main_key)
        exist_value = await self._kv_store.get(main_key)
        logger.debug(f"Get value: {exist_value}")
        if not exist_value:
            return None
        if not sub_key:
            return exist_value
        try:
            exist_value_dict = json.loads(exist_value)
        except json.decoder.JSONDecodeError as e:
            logger.error(f"Failed to decode json, error: {str(e)}")
            return None

        cnt = 0
        for k in sub_key:
            cnt += 1
            if isinstance(exist_value_dict, dict):
                exist_value_dict = exist_value_dict.get(k)
            else:
                if cnt != len(sub_key):
                    logger.error(
                        f"failed to get, invalid key list for main_key: {main_key}, sub_key: {sub_key}"
                    )
                    return None
                break
        return exist_value_dict

    async def exists(self, main_key: list[str], sub_key: list[str]) -> bool:
        """
        Check if a composite key exists in the key-value store.
        Args:
            main_key (list[str]): A list of strings representing the main hierarchical key.
            sub_key (list[str]): A list of strings representing the sub-key path.
        Returns:
            bool: True if the key exists, False otherwise.
        """
        logger.debug(f"Start to check main_key: {main_key}, sub_key:{sub_key}")
        try:
            res = await self.get(main_key, sub_key)
        except KeyError:
            return False

        return bool(res)

    async def delete(self, main_key: list[str], sub_key: list[str]):
        """
        Delete a specific composite key from the key-value store.
        Args:
            main_key (list[str]): A list of strings representing the main hierarchical key.
            sub_key (list[str]): A list of strings representing the sub-key path.
        Returns:
            None
        """
        logger.debug(f"Start to delete main_key: {main_key}, sub_key:{sub_key}")
        main_key = self.separator.join(main_key)
        exist_value = await self._kv_store.get(main_key)
        await self._inner_delete(main_key, exist_value, sub_key)

    async def get_all_value(self, main_key: list[str]) -> dict[str, Any]:
        """
        Retrieve all values stored under a given main key.
        Args:
            main_key (list[str]): A list of strings representing the main hierarchical key.
        Returns:
            dict[str, Any]: A dictionary of sub-keys and their associated values.
        """
        main_key = self.separator.join(main_key)
        exist_value = await self._kv_store.get(main_key)
        logger.debug(
            f"Start to get all_value, exist_value: {exist_value}, main_key: {main_key}"
        )
        if not exist_value:
            return {}
        try:
            exist_value_dict = json.loads(exist_value)
        except json.decoder.JSONDecodeError as e:
            logger.error(f"Failed to decode json, error: {str(e)}")
            return {}

        return exist_value_dict

    async def get_all_value_by_prefix(
        self, main_key_prefix: list[str]
    ) -> dict[str, dict[str, Any]]:
        """
        Retrieve all values stored under a given main key.
        Args:
            main_key_prefix (list[str]): A list of strings representing the main prefix key.
        Returns:
            dict[str, dict[str, Any]]: Multiple dictionaries of sub-keys and their associated values.
        """
        main_key_prefix_str = self.separator.join(main_key_prefix) + self.separator
        multi_value_dict = await self._kv_store.get_by_prefix(main_key_prefix_str)
        if not multi_value_dict:
            return {}
        result_value = {}
        for key, value in multi_value_dict.items():
            try:
                dict_value = json.loads(value)
                if isinstance(dict_value, dict):
                    result_value[key] = dict_value
            except json.decoder.JSONDecodeError as e:
                logger.debug(
                    f"Failed to decode json during getting value by prefix, skip it, error: {str(e)}"
                )
                continue
        return result_value

    async def get_next_key_of_main(self, main_key_prefix: list[str]) -> list[str]:
        """
        Get next key of main key from main_key_prefix.
        Args:
            main_key_prefix (list[str]): A list of strings representing the main prefix key.
        Returns:
            list[str]: A list of strings representing the next key.
        """
        len_main_key_prefix = len(main_key_prefix)
        main_key = self.separator.join(main_key_prefix)
        main_key_value_dict = await self._kv_store.get_by_prefix(main_key)
        ret = []
        for key in main_key_value_dict.keys():
            key_list = key.split(self.separator)
            if len(key_list) == len_main_key_prefix + 1:
                ret.append(key_list[len_main_key_prefix])
        return ret

    async def delete_main_key(self, main_key: list[str]):
        """
        Delete all entries associated with a given main key.
        Args:
            main_key (list[str]): A list of strings representing the main hierarchical key.
        Returns:
            None
        """
        main_key_str = self.separator.join(main_key)
        await self._kv_store.delete(main_key_str)

    async def delete_main_key_by_prefix(self, main_key: list[str]):
        """
        Delete all entries whose main key starts with the given prefix.
        Args:
            main_key (list[str]): A list of strings representing the prefix of the main hierarchical key.
        Returns:
            None
        """
        # delete self
        main_key_str = self.separator.join(main_key)
        await self._kv_store.delete(main_key_str)
        # delete prefix
        main_key_str += self.separator
        await self._kv_store.delete_by_prefix(main_key_str)

    async def delete_by_prefix_suffix(
        self, main_key_prefix: list[str], main_key_suffix: list[str], sub_key: list[str]
    ):
        """
        Delete composite keys that match the prefix and suffix from the key-value store.
        Args:
            main_key_prefix (list[str]): A list of strings representing the prefix of the main hierarchical key.
            main_key_suffix (list[str]): A list of strings representing the suffix of the main hierarchical key.
            sub_key (list[str]): A list of strings representing the sub-key path.
        Returns:
            None
        """
        main_key_prefix_str = self.separator.join(main_key_prefix)
        main_key_suffix_str = self.separator.join(main_key_suffix)
        prefix_match_data_dict = await self._kv_store.get_by_prefix(main_key_prefix_str)
        for main_key, exist_value in prefix_match_data_dict.items():
            if not main_key.endswith(main_key_suffix_str):
                continue
            await self._inner_delete(main_key, exist_value, sub_key)

    def get_seperator(self) -> str:
        """
        Get a string representing the separator between keys.
        Returns:
            str: A string representing the separator.
        """
        return self.separator

    async def delete_by_pattern(self, pattern: str):
        """
        Delete all entries associated with a given pattern.
        """
        await self._kv_store.delete_by_pattern(pattern)

    def _build_main_key_value(
        self, main_key: list[str], sub_key: list[str], value: str
    ) -> Tuple[str, dict]:
        main_key = self.separator.join(main_key)
        nested = value
        for k in reversed(sub_key):
            nested = {k: nested}
        return main_key, nested

    def _merge_dicts(self, old: dict, new: dict):
        result = old.copy()
        for k, v in new.items():
            if k in result and isinstance(result[k], dict) and isinstance(v, dict):
                result[k] = self._merge_dicts(result[k], v)
            else:
                result[k] = v
        return result

    def _merge_value(self, new_value: dict, exist_value: str) -> str:
        try:
            exist_json = json.loads(exist_value)
        except json.decoder.JSONDecodeError as e:
            logger.error(f"Failed to decode json, error: {str(e)}")
            return json.dumps(new_value, ensure_ascii=False)
        merge_result = self._merge_dicts(old=exist_json, new=new_value)
        logger.debug(
            f"merge_result: {merge_result}, exist_json: {exist_value}, new_value: {new_value}"
        )
        return json.dumps(merge_result, ensure_ascii=False)

    async def _inner_delete(self, main_key: str, exist_value: str, sub_key: list[str]):
        logger.debug(f"Get value: {exist_value}")
        if not exist_value:
            return
        try:
            exist_value_dict = json.loads(exist_value)
        except json.decoder.JSONDecodeError as e:
            logger.error(f"Failed to decode json, error: {str(e)}")
            return
        exist_value_dict_copy = exist_value_dict
        cnt = 0
        for k in sub_key:
            cnt += 1
            if cnt == len(sub_key):
                exist_value_dict.pop(k, None)
                break
            if isinstance(exist_value_dict, dict):
                exist_value_dict = exist_value_dict.get(k)

        logger.debug(f"After delete, update: {exist_value_dict}")
        if len(exist_value_dict_copy) == 0:
            logger.debug(f"After delete, remove empty main_key: {main_key}")
            await self._kv_store.delete(main_key)
            return
        await self._kv_store.set(
            main_key, json.dumps(exist_value_dict_copy, ensure_ascii=False)
        )
