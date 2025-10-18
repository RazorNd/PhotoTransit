SELECT s.primary_id,
       s.session_id,
       EXTRACT(EPOCH FROM s.creation_time) * 1000    AS creation_time,
       EXTRACT(EPOCH FROM s.last_access_time) * 1000 AS last_access_time,
       EXTRACT(SECOND FROM s.max_inactive_interval)  AS max_inactive_interval,
       sa.attribute_name,
       sa.attributes                                 AS attribute_bytes
FROM http_session s
         LEFT JOIN http_session_attributes sa ON s.primary_id = sa.session_primary_id
WHERE s.principal_name = ?
