DROP TABLE IF EXISTS ingestlog;

CREATE TABLE ingestlog (
    inserted_date BIGINT PRIMARY KEY,
    duplicatecount BIGINT,
    filename VARCHAR(256),
    insertedcount BIGINT,
    linecount BIGINT,
    loglines VARCHAR[],
    rejectedcount BIGINT
);
