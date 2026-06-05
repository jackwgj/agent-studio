import asyncio
import logging
import threading
from dataclasses import dataclass
from typing import Optional, Dict, Any

from memory_service.config.settings import IAM_ENDPOINT

# 创建logger实例
logger = logging.getLogger(__name__)

import aiohttp
import cachetools


@dataclass
class TokenProjectDomainResult:
    name: str
    id: str


@dataclass
class TokenUserDomainResult:
    name: str
    id: str


@dataclass
class TokenUserResult:
    name: str
    id: str
    domain: TokenUserDomainResult


@dataclass
class TokenProjectResult:
    name: str
    id: str
    domain: TokenProjectDomainResult


@dataclass
class TokenDomainResult:
    name: str
    id: str


@dataclass
class TokenResult:
    user: TokenUserResult
    project: TokenProjectResult
    domain: TokenDomainResult
    expires_at: str


@dataclass
class IamTokenCacheConfig:
    result_max_size: int = 1000
    result_expire_max_minute: int = 30
    result_expire_min_minute: int = 5


SESSION: Optional[aiohttp.ClientSession] = None


async def get_session():
    global SESSION
    if SESSION is None:
        SESSION = aiohttp.ClientSession()
    return SESSION


class IamService:
    def __init__(self, config: IamTokenCacheConfig = None):
        self.config = config = config or IamTokenCacheConfig()
        self.cache = cachetools.TTLCache(
            maxsize=config.result_max_size,
            ttl=config.result_expire_max_minute * 60,  # seconds
        )
        self.locks: Dict[str, asyncio.Lock] = {}
        self.locks_lock = threading.Lock()

    async def get(self, x_auth_token: str) -> Optional[TokenResult]:
        async with self._get_lock(x_auth_token):
            if x_auth_token in self.cache:
                return self.cache[x_auth_token]
            try:
                token_result = await self._analyze_iam_token(x_auth_token)
                if token_result:
                    self.cache[x_auth_token] = token_result
                return token_result
            except Exception as e:
                logger.error("analyze IAM token failed: %s", e, exc_info=True)
                raise

    def _get_lock(self, key: str) -> asyncio.Lock:
        with self.locks_lock:
            if key not in self.locks:
                self.locks[key] = asyncio.Lock()
            return self.locks[key]

    async def _analyze_iam_token(self, x_auth_token: str) -> Optional[TokenResult]:
        if IAM_ENDPOINT == "":
            logger.error("environment IAM_ADDRESS is empty.")
            raise RuntimeError("environment IAM_ENDPOINT is empty.")

        url = IAM_ENDPOINT + "/v3/auth/tokens"
        headers = {
            "X-Subject-Token": x_auth_token,
            "X-Auth-Token": x_auth_token,
            "Content-Type": "application/json;charset=UTF-8",
        }
        connector = aiohttp.TCPConnector(ssl=False)

        try:
            async with aiohttp.ClientSession(connector=connector) as session:
                async with session.get(
                    url, headers=headers, timeout=aiohttp.ClientTimeout(total=10)
                ) as resp:
                    if 200 <= resp.status < 300:
                        json_data: Dict[str, Any] = await resp.json()
                        token_data = json_data.get("token")
                        if not token_data:
                            logger.error("Missing 'token' field in response")
                            return None

                        user_data = token_data.get("user")
                        project_data = token_data.get("project")
                        if not user_data or not project_data:
                            logger.error("Missing user or project data in token")
                            return None

                        # 修复：确保 domain 数据正确解析
                        user_domain_data = user_data.get("domain", {})
                        project_domain_data = project_data.get("domain", {})

                        user_domain = TokenUserDomainResult(
                            name=user_domain_data.get("name", ""),
                            id=user_domain_data.get("id", ""),
                        )

                        project_domain = TokenProjectDomainResult(
                            name=project_domain_data.get("name", ""),
                            id=project_domain_data.get("id", ""),
                        )

                        user = TokenUserResult(
                            name=user_data.get("name", ""),
                            id=user_data.get("id", ""),
                            domain=user_domain,
                        )

                        project = TokenProjectResult(
                            name=project_data.get("name", ""),
                            id=project_data.get("id", ""),
                            domain=project_domain,
                        )

                        domain = TokenDomainResult(
                            name=project_domain.name, id=project_domain.id
                        )

                        token_result = TokenResult(
                            user=user,
                            project=project,
                            expires_at=token_data.get("expires_at", ""),
                            domain=domain,
                        )
                        return token_result
                    else:
                        logger.error(
                            "IAM token validation failed with status: %d", resp.status
                        )
                        raise RuntimeError("IAM token validation failed")

        except aiohttp.ClientError as e:
            logger.error(
                "HTTP client error during IAM token validation: %s", e, exc_info=True
            )
            raise RuntimeError(f"HTTP request failed: {e}") from e
        except Exception as e:
            logger.error(
                "Unexpected error during IAM token validation: %s", e, exc_info=True
            )
            raise RuntimeError(f"Token analysis failed: {e}") from e


token_result_cache = IamService()
