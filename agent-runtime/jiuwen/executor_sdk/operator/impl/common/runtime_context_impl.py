"""Minimal RuntimeContextImpl for local tests."""


class _ContextStore:
    def __init__(self):
        self._session_ctx = {}

    def get_session_ctx(self, key, is_remote=False):
        return self._session_ctx.get(key)

    def put_session_ctx(self, key, value):
        self._session_ctx[key] = value


class RuntimeContextImpl:
    def __init__(self, session_id="local-session"):
        self.session_id = session_id
        self._context = _ContextStore()

    def get_context(self):
        return self._context

    def get_session_id(self):
        return self.session_id

    def enqueue_side_output(self, *args, **kwargs):
        return None
