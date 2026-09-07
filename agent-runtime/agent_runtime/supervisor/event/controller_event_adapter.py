"""Controller runner adapter using the shared canonical builders."""
from .react_event_adapter import adapt_react_chunk, adapt_react_end

adapt_controller_chunk = adapt_react_chunk
adapt_controller_end = adapt_react_end
