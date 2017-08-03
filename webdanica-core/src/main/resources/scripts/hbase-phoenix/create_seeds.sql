DROP INDEX IF EXISTS seeds_inserted_time_idx ON seeds;
DROP INDEX IF EXISTS seeds_status_idx ON seeds;
DROP INDEX IF EXISTS seeds_domain_idx ON seeds;
DROP INDEX IF EXISTS seeds_tld_idx ON seeds;
DROP INDEX IF EXISTS seeds_danica_idx ON seeds;
DROP TABLE IF EXISTS seeds;


CREATE TABLE seeds (
 url VARCHAR PRIMARY KEY,
 redirected_url VARCHAR,
 host VARCHAR(256),
 domain VARCHAR(256),
 tld VARCHAR(64),
 inserted_time TIMESTAMP,
 updated_time TIMESTAMP,
 danica INTEGER, 
 danica_reason VARCHAR,
 status INTEGER, 
 status_reason VARCHAR,
 exported BOOLEAN,
 exported_time TIMESTAMP
);

CREATE INDEX seeds_inserted_time_idx ON seeds (inserted_time);
CREATE INDEX seeds_domain_idx ON seeds(domain);
CREATE INDEX seeds_tld_idx ON seeds(tld);
CREATE INDEX seeds_danica_idx ON seeds(danica);
CREATE INDEX seeds_status_idx ON seeds (status);

