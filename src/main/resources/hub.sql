CREATE TABLE "user" (
    id VARCHAR(256) PRIMARY KEY,
    name VARCHAR(256) NOT NULL,
    email VARCHAR(256) UNIQUE NOT NULL,
    credential VARCHAR(256) UNIQUE NOT NULL
);

CREATE TABLE settings (
    id VARCHAR(256) PRIMARY KEY,
    semester VARCHAR(6),
    storage_version VARCHAR(256),
    dosv_applicants_update_time TIMESTAMP,
    dosv_applications_update_time TIMESTAMP
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
    published BOOLEAN NOT NULL DEFAULT FALSE
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
    status VARCHAR(256) NOT NULL
);

CREATE TABLE evaluation (
    id VARCHAR(256) PRIMARY KEY,
    application_id VARCHAR(256) REFERENCES application NOT NULL,
    criterion_id VARCHAR(256) NOT NULL,
    information_id VARCHAR(256),
    value FLOAT,
    status VARCHAR(256) NOT NULL
);

CREATE TABLE session (
    id VARCHAR(256) PRIMARY KEY,
    user_id VARCHAR(256) REFERENCES "user" NOT NULL,
    device VARCHAR(256) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL
);

INSERT INTO settings (id, semester, storage_version) VALUES ('settings', '2014WS', '0');
