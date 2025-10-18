UPDATE http_session_attributes
SET attributes = ENCODE(?, 'escape')::jsonb
WHERE session_primary_id=?::UUID
  AND attribute_name=?
