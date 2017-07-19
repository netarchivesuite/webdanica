
CREATE TABLE statecache (
 last_updated TIMESTAMP PRIMARY KEY,
 totalSeedsCount BIGINT,
 harvestCount BIGINT,
 totalCritResults BIGINT,
 seedStatusCount VARCHAR[],
 seedDanicaStatusCounts VARCHAR[]
);

