DELETE
FROM http_session_attributes
WHERE session_primary_id = ?::UUID
  AND attribute_name = ?;
