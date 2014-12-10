CREATE TABLE "user" (
    id VARCHAR(256) PRIMARY KEY,
    name VARCHAR(256) NOT NULL,
    email VARCHAR(256) UNIQUE NOT NULL,
    credential VARCHAR(256) UNIQUE NOT NULL,
    role VARCHAR(256) NOT NULL,
    dosv_bid VARCHAR(256) UNIQUE,
    dosv_ban VARCHAR(256)
);

CREATE TABLE settings (
    id VARCHAR(256) PRIMARY KEY,
    semester VARCHAR(6),
    storage_version VARCHAR(256),
    dosv_sync_time TIMESTAMP NOT NULL,
    dosv_remote_applications_pull_time TIMESTAMP
);

CREATE TABLE quota (
    id VARCHAR(256) PRIMARY KEY,
    name VARCHAR(256) NOT NULL,
    percentage INT NOT NULL
);

CREATE TABLE quota_ranking_criteria (
    quota_id VARCHAR(256) REFERENCES quota,
    criterion_id VARCHAR(256),
    PRIMARY KEY(quota_id, criterion_id)
);

CREATE TABLE allocation_rule (
    id VARCHAR(256) PRIMARY KEY,
    quota_id VARCHAR(256) REFERENCES quota
);

CREATE TABLE course (
    id VARCHAR(256) PRIMARY KEY,
    name VARCHAR(256) NOT NULL,
    capacity INT NOT NULL,
    allocation_rule_id VARCHAR(256) REFERENCES allocation_rule,
    published BOOLEAN NOT NULL DEFAULT FALSE,
    modification_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    dosv BOOLEAN NOT NULL
);

CREATE TABLE journal_record (
    id VARCHAR(256) PRIMARY KEY,
    action_type VARCHAR(256) NOT NULL,
    object_id VARCHAR(256),
    agent_id VARCHAR(256) REFERENCES "user",
    time TIMESTAMP NOT NULL,
    detail TEXT
);

CREATE TABLE qualification (
    id VARCHAR(256) PRIMARY KEY,
    user_id VARCHAR(256) REFERENCES "user",
    grade FLOAT NOT NULL
);

CREATE TABLE application (
    id VARCHAR(256) PRIMARY KEY,
    user_id VARCHAR(256) REFERENCES "user" NOT NULL,
    course_id VARCHAR(256) REFERENCES course NOT NULL,
    status VARCHAR(256) NOT NULL,
    modification_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    dosv_version INT NOT NULL DEFAULT -1
);

CREATE TABLE evaluation (
    id VARCHAR(256) PRIMARY KEY,
    application_id VARCHAR(256) REFERENCES application NOT NULL,
    criterion_id VARCHAR(256) NOT NULL,
    information_id VARCHAR(256),
    value FLOAT,
    status VARCHAR(256) NOT NULL
);

CREATE TABLE rank (
    id VARCHAR(256) PRIMARY KEY,
    quota_id VARCHAR(256) REFERENCES quota NOT NULL,
    user_id VARCHAR(256) REFERENCES "user" NOT NULL,
    application_id VARCHAR(256) REFERENCES application NOT NULL,
    index INT NOT NULL,
    lotnumber INT NOT NULL
);

CREATE TABLE session (
    id VARCHAR(256) PRIMARY KEY,
    user_id VARCHAR(256) REFERENCES "user" NOT NULL,
    device VARCHAR(256) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL
);

INSERT INTO settings (id, semester, storage_version, dosv_sync_time) VALUES ('settings', '2014WS', '0',
    CURRENT_TIMESTAMP);
