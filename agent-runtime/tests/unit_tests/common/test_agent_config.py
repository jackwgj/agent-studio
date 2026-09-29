"""Runtime Agent iteration settings tests."""

import os
from unittest.mock import patch

import pytest
from pydantic import ValidationError

from agent_runtime.common.config import AgentSettings, Settings


def test_agent_settings_default_max_iteration_is_100():
    with patch.dict(os.environ, {}, clear=False):
        os.environ.pop("AGENT_MAX_ITERATION", None)
        config = AgentSettings(_env_file=None)

    assert config.max_iteration == 100


def test_agent_settings_reads_max_iteration_from_environment():
    with patch.dict(os.environ, {"AGENT_MAX_ITERATION": "30"}):
        config = AgentSettings(_env_file=None)

    assert config.max_iteration == 30


@pytest.mark.parametrize("value", ["0", "101"])
def test_agent_settings_rejects_out_of_range_max_iteration(value):
    with patch.dict(os.environ, {"AGENT_MAX_ITERATION": value}):
        with pytest.raises(ValidationError):
            AgentSettings(_env_file=None)


def test_settings_exposes_agent_settings():
    config = Settings()

    assert isinstance(config.agent, AgentSettings)
