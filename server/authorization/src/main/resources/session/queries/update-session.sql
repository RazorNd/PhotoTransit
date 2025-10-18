UPDATE http_session
SET session_id            = ?::UUID,
    last_access_time      = TO_TIMESTAMP(? / 1000.0),
    max_inactive_interval = MAKE_INTERVAL(secs := ?),
    expiry_time           = TO_TIMESTAMP(? / 1000.0),
    principal_name        = ?
WHERE primary_id = ?::UUID

