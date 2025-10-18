CREATE TABLE http_session
(
    primary_id            UUID      NOT NULL,
    session_id            UUID      NOT NULL,
    creation_time         TIMESTAMP NOT NULL,
    last_access_time      TIMESTAMP NOT NULL,
    max_inactive_interval INTERVAL  NOT NULL,
    expiry_time           TIMESTAMP NOT NULL,
    principal_name        VARCHAR(100),
    CONSTRAINT spring_session_pk PRIMARY KEY (primary_id)
);

CREATE UNIQUE INDEX spring_session_ix1 ON http_session (session_id);
CREATE INDEX spring_session_ix2 ON http_session (expiry_time);
CREATE INDEX spring_session_ix3 ON http_session (principal_name);

CREATE TABLE http_session_attributes
(
    session_primary_id UUID         NOT NULL,
    attribute_name     VARCHAR(200) NOT NULL,
    attributes         JSONB        NOT NULL,
    CONSTRAINT spring_session_attributes_pk PRIMARY KEY (session_primary_id, attribute_name),
    CONSTRAINT spring_session_attributes_fk FOREIGN KEY (session_primary_id) REFERENCES http_session (primary_id) ON DELETE CASCADE
);
