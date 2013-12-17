CREATE TABLE dosv.settings (
    semester VARCHAR(6),
    dosv_users_updated TIMESTAMP,
    dosv_applications_updated TIMESTAMP
);

CREATE TABLE dosv.journal_record (
    id SERIAL PRIMARY KEY,
    action_type VARCHAR(256) NOT NULL,
    object_type VARCHAR(256),
    object_id INTEGER,
    user_id INTEGER,
    detail TEXT,
    time TIMESTAMP NOT NULL
);

INSERT INTO dosv.settings VALUES ('2014WS', NULL, NULL);
