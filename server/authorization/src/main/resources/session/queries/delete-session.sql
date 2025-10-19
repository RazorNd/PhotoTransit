DELETE
FROM http_session
WHERE session_id = ?::UUID
  AND max_inactive_interval >= MAKE_INTERVAL(secs := 0)
