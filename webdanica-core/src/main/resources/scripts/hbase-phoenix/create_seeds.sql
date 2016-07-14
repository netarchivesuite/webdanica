CREATE TABLE seeds (
    url VARCHAR(256) PRIMARY KEY,
    danica INTEGER,
    exported BOOLEAN,
    hostname VARCHAR(256),
    inserted_time TIMESTAMP,
    redirected_url VARCHAR(256),
    status INTEGER,
    status_reason VARCHAR(256),
    tld VARCHAR(256)
);

CREATE INDEX seeds_inserted_time_idx ON webdanica.seeds (inserted_time);
CREATE INDEX seeds_status_idx ON webdanica.seeds (status);
