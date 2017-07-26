CREATE TABLE statecache (
 uuid BIGINT PRIMARY KEY,
 totalSeedsCount BIGINT,
 harvestCount BIGINT,
 totalCritResults BIGINT,
 seedStatusCounts VARCHAR[],
 seedDanicaStatusCounts VARCHAR[],
 last_updated TIMESTAMP
 );


