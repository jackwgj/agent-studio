"""Sandbox code runner module - executes code via remote sandbox service."""

import httpx

from openjiuwen.core.common.exception.codes import StatusCode
from openjiuwen.core.common.exception.errors import build_error


class SandboxCodeRunner:
    """Sandbox code runner - executes code via remote HTTP sandbox service.

    This runner sends code to a remote sandbox service for isolated execution.
    The sandbox service is expected to accept POST requests with JSON payload
    containing language, code, inputs, and timeout fields.

    Note: This class does NOT inherit from CodeRunner because it uses a
    completely different execution model (HTTP vs subprocess).
    """

    def __init__(self, sandbox_server: str, ssl_verify: bool = False):
        """Initialize sandbox runner.

        Args:
            sandbox_server: URL of the sandbox service endpoint.
            ssl_verify: Whether to verify SSL certificates. Defaults to False.
        """
        self.sandbox_server = sandbox_server
        self.ssl_verify = ssl_verify

    async def run(self, user_code: str, inputs: dict, timeout: int = 300) -> dict:
        """Execute user code via sandbox service.

        Args:
            user_code: User's Python code (must define main function).
            inputs: Input parameters passed to main function.
            timeout: Execution timeout in seconds.

        Returns:
            dict: Result from main function.

        Raises:
            RuntimeError: If sandbox execution fails.
            BuildError: If result format is invalid.
        """
        payload = {
            "language": "python",
            "code": user_code,
            "inputs": inputs,
            "timeout": timeout,
        }

        async with httpx.AsyncClient() as client:
            response = await client.post(
                self.sandbox_server,
                json=payload,
                headers={"Content-Type": "application/json"},
                timeout=timeout,
                verify=self.ssl_verify,
            )

        response.raise_for_status()
        return self._parse_result(response.json())

    def _parse_result(self, raw_result: dict) -> dict:
        """Parse sandbox response to extract result.

        Expected response format:
        {
            "output": {
                "return": {...},  # Result dict from code execution
                "error": None     # Error message if execution failed
            }
        }

        Args:
            raw_result: Raw response dict from sandbox service.

        Returns:
            Parsed result dict.

        Raises:
            RuntimeError: If sandbox reports an error.
            BuildError: If result is not a dict.
        """
        output = raw_result.get("output", {})
        error = output.get("error")

        if error:
            raise RuntimeError(f"Sandbox run error: {error}")

        result_dict = output.get("return")

        if not isinstance(result_dict, dict):
            raise build_error(
                StatusCode.WORKFLOW_COMPONENT_SCHEMA_INVALID,
                comp_id="flow_code",
                reason="Sandbox code must return a dict from main()",
                workflow="n/a",
            )

        return result_dict
