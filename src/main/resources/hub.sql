CREATE TABLE "user" (
    id VARCHAR(256) PRIMARY KEY,
    name VARCHAR(256) NOT NULL,
    email VARCHAR(256) NOT NULL
);

CREATE TABLE settings (
    semester VARCHAR(6),
    dosv_applicants_update_time TIMESTAMP,
    dosv_applications_update_time TIMESTAMP
);

INSERT INTO settings (semester) VALUES ('2014WS');

CREATE TABLE journal_record (
    id SERIAL PRIMARY KEY,
    action_type VARCHAR(256) NOT NULL,
    object_type VARCHAR(256),
    object_id INTEGER,
    user_id INTEGER,
    detail TEXT,
    time TIMESTAMP NOT NULL
);