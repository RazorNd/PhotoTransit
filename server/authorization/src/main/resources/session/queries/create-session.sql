INSERT INTO http_session (primary_id,
                          session_id,
                          creation_time,
                          last_access_time,
                          max_inactive_interval,
                          expiry_time,
                          principal_name)
VALUES (?::UUID,
        ?::UUID,
        TO_TIMESTAMP(? / 1000.0) AT TIME ZONE 'UTC',
        TO_TIMESTAMP(? / 1000.0) AT TIME ZONE 'UTC',
        MAKE_INTERVAL(secs := ?),
        TO_TIMESTAMP(? / 1000.0) AT TIME ZONE 'UTC',
        ?)
