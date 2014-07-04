CREATE TABLE "user" (
    id VARCHAR(256) PRIMARY KEY,
    name VARCHAR(256) NOT NULL,
    email VARCHAR(256) NOT NULL
);

CREATE TABLE settings (
    id VARCHAR(256) PRIMARY KEY,
    semester VARCHAR(6),
    dosv_applicants_update_time TIMESTAMP,
    dosv_applications_update_time TIMESTAMP
);

CREATE TABLE allocation_rule (
    id VARCHAR(256) PRIMARY KEY
);

CREATE TABLE course (
    id VARCHAR(256) PRIMARY KEY,
    name VARCHAR(256) NOT NULL,
    capacity INT NOT NULL,
    allocation_rule_id VARCHAR(256) REFERENCES allocation_rule
);

CREATE TABLE journal_record (
    id VARCHAR(256) PRIMARY KEY,
    action_type VARCHAR(256) NOT NULL,
    object_type VARCHAR(256),
    object_id VARCHAR(256),
    user_id VARCHAR(256) REFERENCES "user",
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

CREATE TABLE quota (
    id VARCHAR(256) PRIMARY KEY,
    name VARCHAR(256) NOT NULL,
    percentage DOUBLE PRECISION NOT NULL
);

CREATE TABLE quota_ranking_criteria (
    PRIMARY KEY(quota_id)
    quota_id VARCHAR(256) REFERENCES quota,
    criterion_id VARCHAR(256) NOT NULL
);

INSERT INTO settings (id, semester) VALUES ('settings', '2014WS');
