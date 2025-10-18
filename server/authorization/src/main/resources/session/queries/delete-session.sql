DELETE
FROM http_session
WHERE session_id = ?::UUID
  AND max_inactive_interval >= 0
