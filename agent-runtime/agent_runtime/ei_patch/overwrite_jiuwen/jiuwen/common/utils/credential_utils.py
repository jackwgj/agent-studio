import os

from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.log.base import logger
from jiuwen.common.store.obs import OBSUtil
from jiuwen.common.utils.utils import safe_json_loads_raise_exception
from jiuwen.serve.common.logger.request_logger import log_function_timing
from overwrite_jiuwen.jiuwen.serve.controllers.execution.open_utils import EICacheUtils

# 缓存过期时间为10分钟
CACHE_CREDENTIAL_TTL_SECONDS = int(os.getenv("CACHE_CREDENTIAL_TTL_SECONDS", "600"))
MAX_CREDENTIAL_CACHE_NUM = int(os.getenv("MAX_WORKFLOW_CACHE_NUM", str(100)))


# 从obs里加载凭证
@log_function_timing
def load_credential_from_obs(credential_path):
    credential_ir_json_str = OBSUtil.get_content(object_key=credential_path).decode(
        "utf-8"
    )
    try:
        credential_data = safe_json_loads_raise_exception(credential_ir_json_str)
    except ValueError as e:
        raise JiuWenBaseException(
            error_code=StatusCode.IR_DATA_JSON_LOAD_FAILED.code,
            message=StatusCode.IR_DATA_JSON_LOAD_FAILED.errmsg,
        ) from e

    # 将credential_data转为对应的格式
    credential_dict = {}
    domain = credential_data["domain"].lower()
    credential_dict["headers"] = {}
    credential_dict["query"] = {}
    credential_dict[domain] = {
        credential["authName"]: credential["authValue"]
        for credential in credential_data["authKeys"]
    }
    credential_dict["scope"] = credential_data["scope"]
    credential_dict["crypt_method"] = "ei"

    return credential_dict


cache_credential_queue = EICacheUtils(
    capacity=MAX_CREDENTIAL_CACHE_NUM,
    should_serialize=True,
    cache_name="credential",
    memory_ttl=CACHE_CREDENTIAL_TTL_SECONDS,
    redis_ttl=CACHE_CREDENTIAL_TTL_SECONDS,
)


@log_function_timing
def credential_load(credential_path: str) -> dict:
    """加载credential ir内容"""
    logger.info("Loading Credential IR content from %s", credential_path)
    credential_value = cache_credential_queue.get(credential_path)
    if credential_value:
        logger.info(
            "Cache HIT! Process %d get cached data: %s", os.getpid(), credential_path
        )
        return credential_value

    logger.info(
        "Cache MISS! Process %d loading from OBS: %s", os.getpid(), credential_path
    )

    credential = load_credential_from_obs(credential_path)
    # 将credential放入缓存
    cache_credential_queue.put(credential_path, credential)

    logger.info("Process %d pickled and cached data: %s", os.getpid(), credential_path)
    return credential


def auth_from_credential(default_auth: dict, ir_data: dict) -> dict:
    """
    根据传入的ir_data中的credential_type字段，加载对应的凭证信息。
    如果credential_type字段不存在，则返回默认的default_auth
    :param default_auth: 默认凭证
    :param ir_data: plugin ir
    :return: auth
    """
    logger.info("overwrite plugin/common/utils/ir_utils.py")
    # 判断ir里是否有credential_type字段,如果存在则去获取凭证,如果不存在则返回默认
    if "credential_type" not in ir_data:
        return default_auth

    credential_ir_path = os.path.join(
        "credential",
        "ir",
        ir_data.get("credential_owner_id"),
        f"{ir_data.get('id')}.json",
    ).replace("\\", "/")
    auth = {}
    try:
        auth = credential_load(credential_ir_path)
    except Exception as e:
        logger.error(
            "Load credential ir failed, credential ir path: %s, error: %s",
            credential_ir_path,
            e,
        )
    return auth
