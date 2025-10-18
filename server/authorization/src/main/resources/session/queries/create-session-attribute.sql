INSERT INTO http_session_attributes (session_primary_id, attribute_name, attributes)
VALUES (?::UUID, ?, ENCODE(?, 'escape')::jsonb)
ON CONFLICT (session_primary_id, attribute_name) DO UPDATE
    SET attributes = excluded.attributes
