CREATE TABLE ingestlog (
    inserted_date BIGINT PRIMARY KEY,
    duplicatecount BIGINT,
    filename VARCHAR(128),
    insertedcount BIGINT,
    linecount BIGINT,
    loglines VARCHAR[],
    rejectedcount BIGINT
);
