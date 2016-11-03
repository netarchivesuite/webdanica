DROP INDEX IF EXISTS seeds_inserted_time_idx ON seeds;
DROP INDEX IF EXISTS seeds_status_idx ON seeds;
DROP TABLE IF EXISTS seeds;

CREATE TABLE seeds (
    url VARCHAR PRIMARY KEY,
    danica INTEGER,
    exported BOOLEAN,
    host VARCHAR(256),
    domain VARCHAR(256),
    inserted_time TIMESTAMP,
    redirected_url VARCHAR,
    status INTEGER,
    status_reason VARCHAR(256),
    tld VARCHAR(64)
);

CREATE INDEX seeds_inserted_time_idx ON seeds (inserted_time);
CREATE INDEX seeds_status_idx ON seeds (status);
