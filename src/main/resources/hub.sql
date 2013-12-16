CREATE TABLE dosv.settings (
    semester VARCHAR(6),
    dosv_users_updated TIMESTAMP,
    dosv_applications_updated TIMESTAMP
);

INSERT INTO dosv.settings VALUES ('2014WS', NULL, NULL);

DROP table dosv.journal_record;

CREATE TABLE dosv.journal_record
(
  id bigserial PRIMARY KEY,
  action_type VARCHAR(100),
  object_type VARCHAR(20),
  object_id int,
  detail text,
  user_id int,
  timestamp TIMESTAMP NOT NULL
);

GRANT ALL PRIVILEGES ON table dosv.journal_record TO haphuong;

GRANT ALL PRIVILEGES ON sequence dosv.journal_record_id_seq TO haphuong;

INSERT INTO dosv.journal_record (action_type, object_type, object_id, detail, user_id, timestamp) VALUES ('USER_CREATED', 'APPLICANT', 1, 'SessionExample', 2, '2013-12-16 14:37:29.366');

