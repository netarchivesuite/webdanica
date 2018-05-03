# webdanica
System for finding Danish webpages outside the .dk domain

The system consists of a ROOT tomcat application (the webapp) running on port 8080, with three embedded workflows
 * a filtering workflow - used to reject undesirable seeds
 * a harvesting workflow, that makes small single seed harvests using a local NetarchiveSuite system.
 * a cacheupdate-workflow, that caches regularly counts for each seeds, criteriaresults, and harvests.

Note that the webapp file webdanica-webapp-war-$RELEASE.war on the releases page is the tomcat application file, and the file containing libraries (in WEB-INF/lib) used by the tools and automatic-workflow.

The result of the harvesting workflow is harvestlogs, read by an automatic analysis workflow, that from these harvestlogs 
 * makes parsedText out of the warc.gz files from heritrix3, and then 
 * does criteria-analysis on this text, and finally 
 * ingest the results into the database.

The database backend is HBase (currently 1.1) through Apache Phoenix. Building the code has the following requirement in webdanica-core/pom.xml
``` 
<dependency>
      <groupId>org.apache.phoenix</groupId>
      <artifactId>phoenix-core</artifactId>
      <version>4.7.0-HBase-1.1</version>
      <scope>provided</scope>
    </dependency>
```
Being 'provided' means, that is not downloaded and among the libs included in the produced war-file.
You should locate the phoenix-client-jar used with your cluster, and use this instead of the previously used phoenix-4.7.0-HBase-1.1-client.jar.
We are currently used Horton Works, and their phoenix-client-jar is located here: /usr/hdp/current/phoenix-client/phoenix-client.jar

Describing the installation of hbase is considered out of scope for this manual.

Creating the webdanica-tables in hbase are done using the psql.py script and the create-scripts found here: [scripts/hbase-phoenix](scripts/hbase-phoenix)<br/>
Please use the scripts from the sourcecode attached to the release on https://github.com/netarchivesuite/webdanica/releases, as the scripts on github could be newer.

There are the following create scripts for each of the required hbase tables 
 * create_blacklists.sql
 * create_criteria_results.sql
 * create_domains.sql
 * create_harvests.sql
 * create_ingestlog.sql
 * create_seeds.sql

Sample command to create the blacklists table with connectionstring=kb-test-hadoop-01.kb.dk:2181:/hbase
e.g. psql.py kb-test-hadoop-01.kb.dk:2181:/hbase create_blacklists.sql

[Building the war-file](warfile_building.md) - The WEB-INF/lib folder in the war-file is used as standard lib folder for the webdanica-tools and the scripts of the automatic and manual workflows.

[Installation and configuration of the webapp](webapp_install.md)

[Installation and configuration of the automatic workflow](workflow_install.md)

[Installation and configuration of the webdanica Netarchivesuite](webdanicaNAS_install.md)

[Tools manual](tools.md)

[Analysis manual](analysis.md)


