#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.

"""handles the conversion between message list and template."""

import html
import json
import re
from typing import List

from jiuwen.common.utils.utils import safe_json_loads_raise_exception
from jiuwen.prompt.common.exception.assemble import TemplateFormatError

MESSAGE_VALIDATE_SCHEMA = {
    "system": {
        "role": str,
        "content": str,
    },
    "assistant": {
        "role": str,
        "content": (type(None), str),
        "function_call": (type(None), dict),
    },
    "user": {
        "role": str,
        "content": str,
    },
    "function": {"role": str, "content": str, "name": str},
}

EXTRA_VALIDATE_SCHEMA = {"function_call": {"name": str, "arguments": str}}


def schema_validate(data, schema):
    """do validate"""
    if len(set(data.keys()) - set(schema.keys())) > 0:
        raise TemplateFormatError("Failed validating the data against the schema.")
    for name, data_type in schema.items():
        if not isinstance(data.get(name), data_type):
            raise TemplateFormatError("Failed validating the data against the schema.")
        if name in EXTRA_VALIDATE_SCHEMA and data.get(name) is not None:
            schema_validate(data.get(name), EXTRA_VALIDATE_SCHEMA[name])


def messages_to_template(messages: List[dict]) -> str:
    """messages to template"""
    template = ""
    for message in messages:
        if not isinstance(message, dict):
            raise TemplateFormatError("Each message in the template must be a dict.")
        role = html.escape(message.get("role"))
        validation_schema = MESSAGE_VALIDATE_SCHEMA.get(role)
        if not validation_schema:
            raise TemplateFormatError(
                f"No validation schema found for message role `{role}`."
            )
        schema_validate(message, validation_schema)
        content = message.get("content")
        if not content:
            content = ""
        content_escaped = html.escape(content, quote=False)
        template += f"`#{role}#`\n{content_escaped}\n"
        for extra_key in set(message.keys()) - {"role", "content"}:
            if isinstance(message[extra_key], str):
                extra_content = message[extra_key]
            elif isinstance(message[extra_key], dict):
                extra_content = json.dumps(
                    message.get("function_call", {}), ensure_ascii=False
                )
            else:
                raise TemplateFormatError("Cannot parse data into string")
            extra_content_escaped = html.escape(extra_content, quote=False)
            template += f"`*{extra_key}*`\n{extra_content_escaped}\n"
    return template


def extract_message(message_index, message_match, message_prefix_matches, template):
    """extract message"""
    message_prefix = message_match.group(1)
    message_start_idx = message_match.end()
    message_end_idx = (
        message_prefix_matches[message_index + 1].start()
        if message_index < len(message_prefix_matches) - 1
        else len(template)
    )

    message_content = template[message_start_idx:message_end_idx].strip()
    validate_schema = MESSAGE_VALIDATE_SCHEMA.get(message_prefix)
    if not validate_schema:
        raise TemplateFormatError(
            f"No validation schema found for message role: `{message_prefix}`."
        )
    return message_content, message_prefix, validate_schema


def padding_message(message_prefix, message_content, validation_schema):
    """padding message"""
    role_key = "role"
    content_key = "content"
    message = {
        role_key: message_prefix,
        content_key: message_content,
    }
    extra_fields_matches = list(
        re.finditer(r"`\*(name|function_call)\*`", message_content)
    )
    for field_index, field_match in enumerate(extra_fields_matches):
        field_name = field_match.group(1)
        field_start_idx = field_match.end()
        field_end_idx = (
            extra_fields_matches[field_index + 1].start()
            if field_index < len(extra_fields_matches) - 1
            else len(message_content)
        )

        field_content = message_content[field_start_idx:field_end_idx].strip()
        try:
            field_type = validation_schema.get(field_name)
            if (
                isinstance(field_type, tuple) and dict in field_type
            ) or dict == field_type:
                field_content = safe_json_loads_raise_exception(field_content)
        except Exception as e:
            raise TemplateFormatError(
                f"Errors occur when parsing field `{field_name}` into dict"
            ) from e
        message[field_name] = field_content
        schema_validate(message, MESSAGE_VALIDATE_SCHEMA.get(message_prefix))
        if field_index == 0:
            message[content_key] = message_content[: field_match.start()].strip()
            if len(message[content_key]) == 0:
                message[content_key] = None

    return message


def template_to_messages(template: str) -> List[dict]:
    """template to messages"""
    messages = []
    message_prefix_matches = list(
        re.finditer(r"`#(system|assistant|user|tool|function)#`", template)
    )
    for message_index, message_match in enumerate(message_prefix_matches):
        message_content, message_prefix, validation_schema = extract_message(
            message_index, message_match, message_prefix_matches, template
        )
        message = padding_message(message_prefix, message_content, validation_schema)
        schema_validate(message, validation_schema)
        messages.append(message)
    return messages
