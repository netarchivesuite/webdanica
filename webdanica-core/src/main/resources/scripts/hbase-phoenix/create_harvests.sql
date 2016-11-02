DROP INDEX harvest_finalstate_idx ON harvests;
DROP INDEX harvest_seedurl_idx ON harvests;
DROP INDEX harvest_successful_idx ON harvests;
DROP TABLE harvests;

CREATE TABLE harvests (
    harvestname VARCHAR PRIMARY KEY,
    seedurl VARCHAR,
    error VARCHAR,
    successful BOOLEAN,
    finalState INTEGER,
    harvested_time BIGINT,
    files VARCHAR[],
    fetched_urls VARCHAR[], 
    seed_report VARCHAR,
    crawllog VARCHAR
);

CREATE INDEX harvest_finalstate_idx ON harvests (finalState);
CREATE INDEX harvest_seedurl_idx ON harvests (seedurl);
CREATE INDEX harvest_successful_idx ON harvests (successful);
