UPDATE http_session
SET session_id            = ?::UUID,
    last_access_time = TO_TIMESTAMP(? / 1000.0) AT TIME ZONE 'UTC',
    max_inactive_interval = MAKE_INTERVAL(secs := ?),
    expiry_time      = TO_TIMESTAMP(? / 1000.0) AT TIME ZONE 'UTC',
    principal_name        = ?
WHERE primary_id = ?::UUID

