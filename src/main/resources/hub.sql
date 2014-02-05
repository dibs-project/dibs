-- TODO: Schema zu hub umbenennen

CREATE TABLE dosv.settings (
    semester VARCHAR(6),
    dosv_applicants_update_time TIMESTAMP,
    dosv_applications_update_time TIMESTAMP
);

INSERT INTO dosv.settings VALUES ('2014WS', NULL, NULL);
