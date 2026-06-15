# coding=utf-8
"""
Cryptor: delegates to agent-core (openjiuwen) CryptUtils registry.

Uses agent-core's pluggable crypto framework with a PlainCrypt
(no-op pass-through) implementation, replacing the standalone stub.

agent-core capability used:
  - openjiuwen.core.common.security.crypt_utils.BaseCrypt
  - openjiuwen.core.common.security.crypt_utils.CryptUtils
"""

from openjiuwen.core.common.security.crypt_utils import BaseCrypt, CryptUtils


class _PlainCrypt(BaseCrypt):
    """No-op crypt implementation — returns input unchanged.

    Registered with agent-core's CryptUtils under the name 'plain_prompt'.
    This matches the original jiuwen Crypt behavior (plaintext passwords).
    """

    def encrypt(self, key: bytes, origin: str) -> str:
        return origin

    def decrypt(self, key: bytes, encrypt_str: str) -> str:
        return encrypt_str


# Register at module import time
_PLAIN_CRYPT_NAME = "plain_prompt"
CryptUtils.register_crypt(_PLAIN_CRYPT_NAME, _PlainCrypt())


class Crypt:
    """
    Compatible Crypt interface using agent-core's CryptUtils registry.

    Delegates to the registered PlainCrypt for pass-through encrypt/decrypt.
    If a real crypto backend is registered (e.g. 'aes_gcm'), callers can
    switch by changing _PLAIN_CRYPT_NAME or using CryptUtils directly.
    """

    @staticmethod
    def encrypt(origin: str) -> str:
        """Pass-through encrypt (delegates to CryptUtils registry)."""
        crypt = CryptUtils.get_crypt(_PLAIN_CRYPT_NAME)
        return crypt.encrypt(b"", origin)

    @staticmethod
    def decrypt(encrypt_str: str) -> str:
        """Pass-through decrypt (delegates to CryptUtils registry)."""
        crypt = CryptUtils.get_crypt(_PLAIN_CRYPT_NAME)
        return crypt.decrypt(b"", encrypt_str)
