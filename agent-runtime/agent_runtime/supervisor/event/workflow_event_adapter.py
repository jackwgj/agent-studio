"""Workflow runner adapter using the shared canonical builders."""
from .react_event_adapter import adapt_react_chunk, adapt_react_end

adapt_workflow_chunk = adapt_react_chunk
adapt_workflow_end = adapt_react_end
