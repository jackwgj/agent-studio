from memory_service.config.settings import (
    SSL_ENABLE,
    SSL_PASSWORD,
    SSL_CERT_FILE,
    SSL_KEY_FILE,
)


def get_ssl_cert_config() -> dict:
    if not SSL_ENABLE:
        return {}
    from memory_service.scc.crypto_utils import get_crypto_util

    decrypt_pwd = get_crypto_util().decrypt(SSL_PASSWORD) if SSL_PASSWORD else ""
    return {
        "ssl_certfile": SSL_CERT_FILE,
        "ssl_keyfile": SSL_KEY_FILE,
        "ssl_keyfile_password": decrypt_pwd,
    }
