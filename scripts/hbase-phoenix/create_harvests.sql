DROP INDEX IF EXISTS harvest_finalstate_idx ON harvests;
DROP INDEX IF EXISTS harvest_seedurl_idx ON harvests;
DROP INDEX IF EXISTS harvest_successful_idx ON harvests;
DROP TABLE IF EXISTS harvests;

CREATE TABLE harvests (
  harvestname VARCHAR PRIMARY KEY,
  seedurl VARCHAR,
  error VARCHAR,
  successful BOOLEAN,
  finalState INTEGER,
  harvested_time BIGINT,
  files VARCHAR[],
  fetched_urls VARCHAR[],
  analysis_state INTEGER,
  analysis_state_reason VARCHAR,
  reports VARCHAR[]
);

CREATE INDEX harvest_finalstate_idx ON harvests (finalState);
CREATE INDEX harvest_seedurl_idx ON harvests (seedurl);
CREATE INDEX harvest_successful_idx ON harvests (successful);
