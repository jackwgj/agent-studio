# tests/unit_tests/extension/workflow_node/test_llm_chain.py

from unittest.mock import MagicMock

import pytest
from jiuwen.extension.workflow_node.llm_chain import LLMChain
from openjiuwen.core.common.exception.errors import BaseError


def _make_llm_chain_conf(
    thinking_type="enabled",
    model_name="test-model",
    deploy_mode="workflow",
    template_content=None,
    response_format=None,
):
    """构造最小 LLMChain 配置 dict"""
    return {
        "model": {
            "modelName": model_name,
            "modelType": "LLM",
            "hyperParameters": {"thinking": {"type": thinking_type}},
            "extension": {
                "deploymentId": "deploy-1",
                "authId": "auth-1",
                "safetyBarrier": False,
            },
        },
        "deployMode": deploy_mode,
        "templateContent": template_content
        or [{"content": "{{query}}", "role": "user"}],
        "responseFormat": response_format or {"type": "text"},
        "userFields": {
            "inputs": [
                {"sourceType": "ref", "id": "query", "type": "string", "required": True}
            ],
            "outputs": [
                {
                    "sourceType": "input",
                    "id": "raw_output",
                    "type": "string",
                    "required": True,
                }
            ],
        },
        "stream": True,
        "enableHistory": False,
        "memory": {"enable": False},
        "historySize": 3,
        "vision": [],
    }


class TestLLMChainThinkingStreamErrorPropagation:
    """验证 thinking stream 路径中 LLM 异常能正确向上传播"""

    @pytest.mark.asyncio
    async def test_process_thinking_stream_raises_on_llm_error(self):
        """当 LLM stream 抛出 BadRequestError 时，_process_thinking_stream 应抛出 JiuWenBaseException"""

        conf = _make_llm_chain_conf(thinking_type="enabled")

        # 构造一个会抛异常的 async generator
        async def failing_stream(**kwargs):
            raise Exception("模型接入URL无效")

        chain = LLMChain(conf=conf)
        chain._llm = MagicMock()
        chain._llm.stream = MagicMock(return_value=failing_stream())
        chain._session = None
        chain._context = None

        with pytest.raises(BaseError, match="LLM stream failed"):
            await chain._process_thinking_stream(
                chain._llm.stream(messages=[]),
                {},
            )

    @pytest.mark.asyncio
    async def test_stream_method_raises_on_thinking_llm_error(self):
        """当 thinking 模式下 LLM stream 抛异常时，stream() 方法应抛出 JiuWenBaseException"""

        conf = _make_llm_chain_conf(thinking_type="enabled")

        async def failing_stream(**kwargs):
            raise Exception("模型接入URL无效")

        chain = LLMChain(conf=conf)
        # 跳过 _initialize_if_needed
        chain._initialized = True
        chain._llm = MagicMock()
        chain._llm.stream = MagicMock(return_value=failing_stream())
        chain._session = None
        chain._context = None
        chain._stream_final_output = None

        with pytest.raises(BaseError, match="LLM stream failed"):
            async for _ in chain.stream(
                inputs={"userFields": {"query": "你好"}}, session=None, context=None
            ):
                pass


class TestLLMChainThinkingStreamHappyPath:
    """验证 thinking stream 正常工作时的行为"""

    @pytest.mark.asyncio
    async def test_process_thinking_stream_returns_generator_on_success(self):
        """当 LLM stream 正常返回时，_process_thinking_stream 应返回 (generator, reasoning_content)"""
        conf = _make_llm_chain_conf(thinking_type="enabled")

        # 构造正常的 async generator — 返回带 content 的 chunk
        mock_chunk = MagicMock()
        mock_chunk.content = "你好世界"
        mock_chunk.reasoning_content = "思考中..."

        async def successful_stream(**kwargs):
            yield mock_chunk

        chain = LLMChain(conf=conf)
        chain._llm = MagicMock()
        chain._llm.stream = MagicMock(return_value=successful_stream())
        chain._session = None
        chain._context = None

        gen, reasoning_content = await chain._process_thinking_stream(
            chain._llm.stream(messages=[]),
            {},
        )

        # reasoning_content 应被收集
        assert reasoning_content == "思考中..."

        # generator 应能产出内容
        chunks = []
        async for chunk_data in gen:
            chunks.append(chunk_data)
        assert len(chunks) > 0
        assert "raw_output" in chunks[0]


class TestNormalizeTemplatePlaceholders:
    """验证模板占位符空白归一化"""

    def test_no_whitespace(self):
        assert LLMChain._normalize_template_placeholders("{{query}}") == "{{query}}"

    def test_leading_trailing_whitespace(self):
        assert LLMChain._normalize_template_placeholders("{{ query }}") == "{{query}}"

    def test_excessive_whitespace(self):
        assert (
            LLMChain._normalize_template_placeholders("{{ query            }}")
            == "{{query}}"
        )

    def test_multiple_placeholders(self):
        assert (
            LLMChain._normalize_template_placeholders("{{ query }} and {{ context }}")
            == "{{query}} and {{context}}"
        )

    def test_mixed_whitespace_and_clean(self):
        assert (
            LLMChain._normalize_template_placeholders("{{query}} {{ name }}")
            == "{{query}} {{name}}"
        )

    def test_no_placeholders(self):
        assert LLMChain._normalize_template_placeholders("hello world") == "hello world"

    def test_dot_notation_preserved(self):
        assert (
            LLMChain._normalize_template_placeholders("{{ query.name }}")
            == "{{query.name}}"
        )


class TestRenderPromptWithWhitespace:
    """验证 _render_prompt 在模板含空白占位符时能正确替换"""

    def test_render_with_whitespace_placeholder(self):
        chain = LLMChain(conf=_make_llm_chain_conf(
            template_content=[{"content": "{{ query            }}", "role": "user"}],
        ))
        result = chain._render_prompt("{{ query            }}", {"query": "你好"})
        assert result == "你好"

    def test_render_rejects_undefined_variable(self):
        from openjiuwen.core.common.exception.errors import ExecutionError

        chain = LLMChain(conf=_make_llm_chain_conf())
        with pytest.raises(ExecutionError, match="Error parsing the placeholder"):
            chain._render_prompt("{{query.aa}}", {"query": "你好"})
