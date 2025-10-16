CREATE TABLE photos
(
    id         uuid      NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id   uuid      NOT NULL,
    name       text      NOT NULL,
    created_at timestamp NOT NULL
);

CREATE UNIQUE INDEX photos_owner_id_name_idx ON photos (owner_id, name);

CREATE TABLE photo_files
(
    id           uuid NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    photo_id     uuid NOT NULL REFERENCES photos (id) ON DELETE CASCADE,
    type         text NOT NULL,
    storage_path text NOT NULL
)
