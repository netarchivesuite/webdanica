DROP INDEX IF EXISTS seeds_inserted_time_idx ON seeds;
DROP INDEX IF EXISTS seeds_status_idx ON seeds;
DROP TABLE IF EXISTS seeds;

CREATE TABLE seeds (
    url VARCHAR(256) PRIMARY KEY,
    danica INTEGER,
    exported BOOLEAN,
    hostname VARCHAR(256),
    inserted_time TIMESTAMP,
    redirected_url VARCHAR(256),
    status INTEGER,
    status_reason VARCHAR(256),
    tld VARCHAR(64)
);

CREATE INDEX seeds_inserted_time_idx ON seeds (inserted_time);
CREATE INDEX seeds_status_idx ON seeds (status);
