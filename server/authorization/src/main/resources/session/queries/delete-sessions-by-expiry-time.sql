DELETE
FROM http_session
WHERE expiry_time < TO_TIMESTAMP(? / 1000.0) AT TIME ZONE 'UTC'
