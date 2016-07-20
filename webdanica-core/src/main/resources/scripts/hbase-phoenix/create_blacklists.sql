DROP INDEX blacklists_is_active_idx ON blacklists;
DROP TABLE blacklists;

CREATE TABLE blacklists (
 uid VARCHAR(64) PRIMARY KEY,
 name VARCHAR(256),
 description VARCHAR(8192),
 blacklist VARCHAR[],
 last_update BIGINT,
 is_active BOOLEAN
);

CREATE INDEX blacklists_is_active_idx ON blacklists (is_active);

