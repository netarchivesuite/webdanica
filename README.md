# webdanica
System for finding Danish webpages outside the .dk domain

The system consists of a ROOT tomcat application (the webapp) running on port 8080, with two embedded workflows, a filtering workflow - used to reject undesirable seeds, and a harvesting workflow, that makes small single seed harvests
 in a local NetarchiveSuite system the result of which is pushed to third major component, an automatic analysis workflow, that takes harvestlogs - written by the harvesting workflow to a common directory writeable by both, makes parsedText out of the warc.gz files from heritrix3, and then criteria-analysis on this text, and finally ingested into the database.

The database backend is HBase (currently 1.1.5) through Apache Phoenix. Using apache phoenix requires that phoenix-4.7.0-HBase-1.1-client.jar is downloaded from

Installation of hbase is not yet documented properly

Installation of the webdanica-tables are done using the psql.py script and the create-scripts found here: webdanica-core/src/main/resources/scripts/hbase-phoenix

There are the following create scripts for each of the required hbase tables 
 * create_blacklists.sql
 * create_criteria_results.sql
 * create_domains.sql
 * create_harvests.sql
 * create_ingestlog.sql
 * create_seeds.sql

Sample command to create the blacklists table with connectionstring=kb-test-hadoop-01.kb.dk:2181:/hbase
e.g. psql.py kb-test-hadoop-01.kb.dk:2181:/hbase create_blacklists.sql

[Installation and configuration of the webapp](webapp_install.md)

[Installation and configuration of the automatic workflow](workflow_install.md)

[Installation and configuration of the webdanica Netarchivesuite](webdanicaNAS_install.md)

[Tools manual](tools.md)














 


