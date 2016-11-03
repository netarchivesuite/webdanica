DROP INDEX IF EXISTS domains_updated_time_idx ON domains;
DROP INDEX IF EXISTS domains_danicastatus_idx ON domains;
DROP TABLE IF EXISTS domains;

CREATE TABLE domains (
    domain VARCHAR PRIMARY KEY,
    description VARCHAR,
    danicastatus INTEGER,
    updated_time TIMESTAMP,
    danicastatus_reason VARCHAR(256),
    tld VARCHAR(64),
    danica_parts VARCHAR[]
);

CREATE INDEX domains_updated_time_idx ON domains (updated_time);
CREATE INDEX domains_danica_idx ON domains (danicastatus);
CREATE INDEX domains_tld_idx ON domains (tld);

