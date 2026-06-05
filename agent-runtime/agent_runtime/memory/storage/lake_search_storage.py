#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
"""conversation and plan message definition"""

import json
import os
import uuid
from io import BytesIO
from itertools import islice

import aiohttp
import requests
from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.log.base import logger
from jiuwen.common.net import Connector
from jiuwen.common.security.cryptor import Crypt
from jiuwen.common.utils.utils import safe_json_loads
from pydantic import BaseModel, Field, ValidationError

from .base_vector import BaseVector
from .vector_db_factory import VectorDbFactory
from .. import long_memory_config
from ..config import VectorTypeEnum

MAX_RECALL_NUM = 50
MARKDOWN_TEMPLATE = "# %d\n%s\n"


class LakeSearchConfig(BaseModel):
    base_url: str = Field(alias="baseUrl", min_length=1)
    project_id: str = Field(
        alias="projectId", default="1ed40ceefc8d40f8b884edb6a84e7768"
    )
    app_id: str = Field(alias="appId", default="fb9731ab-7085-474fb6c7-64473586f0f3")
    repo_id: str = Field(alias="repoId", min_length=1)
    auth: str = Field(alias="auth", min_length=1)
    is_verify: bool = Field(alias="isVerify", default=True)
    timeout: int = Field(alias="timeout", default=60)


def _convert_to_markdown(data: list):
    return "\n".join([MARKDOWN_TEMPLATE % (i, v) for i, v in enumerate(data)])


class LakeSearchStorages(BaseVector):
    config: LakeSearchConfig

    @classmethod
    def register(cls):
        if long_memory_config.vector_type != VectorTypeEnum.LAKE_SEARCH:
            return
        try:
            cls.config = LakeSearchConfig.model_validate(
                safe_json_loads(os.environ.get("LAKE_SEARCH_CONFIG"))
            )
            cls.config.auth = Crypt().decrypt(cls.config.auth)
        except ValidationError as e:
            logger.error(f"lake search config error {e}")
            return
        VectorDbFactory.register_vector(cls)

    @classmethod
    async def asearch(
        cls,
        query: str,
        condition: str,
        recall_threshold: float,
        recall_num: int,
        *args,
        **kwargs,
    ) -> list:
        if not cls.config:
            raise JiuWenBaseException(
                error_code=StatusCode.PARAM_CHECK_FAILED_ERROR.code,
                message="lake search not init",
            )
        url = (
            f"{cls.config.base_url}/v1/{cls.config.project_id}/applications/"
            f"{cls.config.app_id}/uni-search/experience/searchtext"
        )
        headers = {
            "Content-Type": "application/json",
            "Authorization": "Basic " + cls.config.auth,
        }
        body = {
            "repo_id": cls.config.repo_id,
            "content": query,
            "page_num": 1,
            # 后续条目变多时，考虑分页查询
            "page_size": min(recall_num, MAX_RECALL_NUM),
            "filter_string": condition,
            "scope": "doc",
        }

        try:
            connector = Connector().get_tcp_connector()
            async with aiohttp.ClientSession(
                connector=connector, connector_owner=connector is None
            ) as session:
                async with session.post(
                    url=url,
                    json=body,
                    headers=headers,
                    ssl=cls.config.is_verify,
                    timeout=aiohttp.ClientTimeout(total=cls.config.timeout),
                ) as res:
                    logger.info(f"save data to lake search status_code: {res.status}")
                    if res.ok:
                        resp_json = await res.json()
                        resp = json.loads(resp_json)
                        return list(
                            islice(
                                (
                                    x
                                    for x in resp.get("doc_list", [])
                                    if x.get("score", 0) > recall_threshold
                                ),
                                recall_num,
                            )
                        )
                    else:
                        logger.error("search lake search fail")
                        return []
        except Exception as e:
            logger.error(f"save data to lake search error {e}")
            return []

    @classmethod
    def search(
        cls,
        query: str,
        condition: str,
        recall_threshold: float,
        recall_num: int,
        *args,
        **kwargs,
    ) -> list:
        if not cls.config:
            raise JiuWenBaseException(
                error_code=StatusCode.PARAM_CHECK_FAILED_ERROR.code,
                message="lake search not init",
            )
        url = (
            f"{cls.config.base_url}/v1/{cls.config.project_id}/applications/"
            f"{cls.config.app_id}/uni-search/experience/searchtext"
        )
        headers = {
            "Content-Type": "application/json",
            "Authorization": "Basic " + cls.config.auth,
        }
        body = {
            "repo_id": cls.config.repo_id,
            "content": query,
            "page_num": 1,
            # 后续条目变多时，考虑分页查询
            "page_size": min(recall_num, MAX_RECALL_NUM),
            "filter_string": condition,
            "scope": "doc",
        }

        try:
            response = requests.post(
                url,
                data=json.dumps(body),
                headers=headers,
                verify=cls.config.is_verify,
                timeout=cls.config.timeout,
            )
            logger.info(f"save data to lake search status_code: {response.status_code}")
            if response.ok:
                resp = json.loads(response.content.decode("utf-8"))
                return list(
                    islice(
                        (
                            x
                            for x in resp.get("doc_list", [])
                            if x.get("score", 0) > recall_threshold
                        ),
                        recall_num,
                    )
                )
            else:
                logger.error("search lake search fail")
                return []
        except Exception as e:
            logger.error(f"save data to lake search error {e}")
            return []

    @classmethod
    def _upload(cls, data: list, **kwargs):
        files = {
            "file": (
                str(uuid.uuid4()) + ".md",
                BytesIO(_convert_to_markdown(data).encode("utf-8")),
            )
        }
        headers = {"Authorization": "Basic " + cls.config.auth}

        url = (
            f"{cls.config.base_url}/v1/{cls.config.project_id}/applications/"
            f"{cls.config.app_id}/uni-search/{cls.config.repo_id}/files"
        )

        try:
            response = requests.post(
                url,
                files=files,
                headers=headers,
                params=kwargs,
                verify=cls.config.is_verify,
                timeout=cls.config.timeout,
            )
            logger.info(
                f"save data to lake search status_code: {response.status_code}, "
                f"rsp: {json.loads(response.content.decode('utf-8'))}"
            )
        except Exception as e:
            logger.error(f"save data to lake search error {e}")

    @classmethod
    def save(cls, file_data: list, **kwargs):
        if not cls.config:
            raise JiuWenBaseException(
                error_code=StatusCode.PARAM_CHECK_FAILED_ERROR.code,
                message="lake search not init",
            )
        cls._upload(file_data, **kwargs)

    @classmethod
    def _list_file_by_tag(cls, **kwargs):
        url = (
            f"{cls.config.base_url}/v1/{cls.config.project_id}/applications/"
            f"{cls.config.app_id}/uni-search/{cls.config.repo_id}/files/search"
        )
        headers = {"Authorization": "Basic " + cls.config.auth}
        try:
            page_num = 1
            single_tag_doc = []
            multi_tag_doc = []
            while page_num > 0:
                data = {
                    "page_num": page_num,
                    "page_size": 10,
                    "file_type": "md",
                    "file_status": "SUCCESS",
                }
                response = requests.get(
                    url,
                    headers=headers,
                    params=kwargs | data,
                    verify=cls.config.is_verify,
                    timeout=cls.config.timeout,
                )
                if response.ok:
                    resp = json.loads(response.content.decode("utf-8"))
                    for i in resp.get("files", []):
                        if len(i.get("tags", [])) > 1:
                            multi_tag_doc.append(i)
                        else:
                            single_tag_doc.append(i)

                    if resp.get("total", 0) > page_num * 10:
                        page_num += 1
                    else:
                        break
                else:
                    logger.error(f"get file lake search error {response.status_code}")
                    return single_tag_doc, multi_tag_doc
            return single_tag_doc, multi_tag_doc
        except Exception as e:
            logger.error(f"get file lake search error {e}")
            return [], []

    @classmethod
    def _batch_remove_files(cls, file_ids: list):
        url = (
            f"{cls.config.base_url}/v1/{cls.config.project_id}/applications/"
            f"{cls.config.app_id}/uni-search/{cls.config.repo_id}/files/batch"
        )
        headers = {
            "Content-Type": "application/json",
            "Authorization": "Basic " + cls.config.auth,
        }
        try:
            batch_size = 1000
            for i in range(0, len(file_ids), batch_size):
                response = requests.delete(
                    url,
                    headers=headers,
                    data=json.dumps({"file_ids": file_ids[i : i + batch_size]}),
                    verify=cls.config.is_verify,
                    timeout=cls.config.timeout,
                )
                if not response.ok:
                    logger.error(
                        f"delete file lake search error {response.status_code}"
                    )
                    break
        except Exception as e:
            logger.error(f"delete file lake search error {e}")

    @classmethod
    def _remove_tags(cls, files: list, remove_tags: list):
        url = (
            f"{cls.config.base_url}/v1/{cls.config.project_id}/applications/"
            f"{cls.config.app_id}/uni-search/{cls.config.repo_id}/files/"
        )
        headers = {
            "Content-Type": "application/json",
            "Authorization": "Basic " + cls.config.auth,
        }
        try:
            for i in files:
                params = {
                    "category": i.get("category"),
                    "tags": [x for x in i.get("tags") if x not in remove_tags],
                }
                response = requests.put(
                    url + i.get("id", ""),
                    headers=headers,
                    params=params,
                    verify=cls.config.is_verify,
                    timeout=cls.config.timeout,
                )
                if not response.ok:
                    logger.error(f"remove tag lake search error {response.status_code}")
                    break
        except Exception as e:
            logger.error(f"remove tag lake search error {e}")

    @classmethod
    def remove(cls, **kwargs):
        single_tag_doc, multi_tag_doc = cls._list_file_by_tag(**kwargs)
        cls._batch_remove_files([i.get("id") for i in single_tag_doc])
        cls._remove_tags(multi_tag_doc, kwargs.get("tags"))

    @classmethod
    def pop_old_data(cls, user_id, workflow_id):
        return []
