#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
"""store config file"""

import os
from abc import ABC


class AbstractConfig(ABC):
    """
    Abstract store config
    """

    def __repr__(self):
        return str(self.__dict__)


class GaussDBConfig(AbstractConfig):
    """
    Gaussdb config
    """

    def __init__(self, **kwargs):
        env_prefix = "DATASOURCE_GAUSSDB_"
        self.host = os.environ.get(env_prefix + "HOST")
        self.port = os.environ.get(env_prefix + "PORT")
        self.user = os.environ.get(env_prefix + "USER_NAME")
        self.dbname = os.environ.get(env_prefix + "DB_NAME")
        self.password = os.environ.get(env_prefix + "PASSWORD")
        self.ssl_cert = os.environ.get(env_prefix + "SSL_CERT")
        self.ssl_key = os.environ.get(env_prefix + "SSL_KEY")
        # 默认是ssl安装认证，模式必须是"verify-ca"
        self.ssl_mode = os.environ.get(env_prefix + "SSL_MODE") or "verify-ca"
        self.ssl_root_cert = os.environ.get(env_prefix + "SSL_ROOT_CERT")
        self.minconn = int(os.environ.get(env_prefix + "MIN_CONNECTIONS", 1))
        self.maxconn = int(os.environ.get(env_prefix + "MAX_CONNECTIONS", 10))

        self.keepalives = kwargs.get("keepalives", 1)
        self.keepalives_idle = kwargs.get("keepalives_idle", 30)
        self.keepalives_interval = kwargs.get("keepalives_interval", 10)
        self.keepalives_count = kwargs.get("keepalives_count", 5)
