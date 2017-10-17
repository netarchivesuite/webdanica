# Tools manual

The tools folder in the root of the released source-code (https://github.com/netarchivesuite/webdanica/releases) holds sample scripts for the tools below: loadSeeds.sh, loadBlacklists.sh, loadDomains.sh, exportDanica.sh, importIntoNAS.sh, importDanica.sh, loadTest.sh, showReports.sh, synchronizeTraps.sh, extractFromGithub.sh, cacheTest.sh, databaseStats.sh.
Installing the tools is easy:
```
## change RELEASE to the correct release
RELEASE=2.0
## change TOOLS_HOME to the correct TOOLS_HOME for your environment
TOOLS_HOME=/opt/workflows/tools
wget https://github.com/netarchivesuite/webdanica/archive/
unzip $RELEASE.zip
cp -av webdanica-$RELEASE/tools $TOOLS_HOME 
cd $TOOLS_HOME
AUTOMATIC_WORKFLOW_HOME=/home/test/automatic-workflow
ln -s $AUTOMATIC_WORKFLOW_HOME/conf .
ln -s $AUTOMATIC_WORKFLOW_HOME/lib .
```
The last commands add symbolic links to the lib-folder and conf-folder used by the automatic-workflow. 

The following variables in the scripts should be modified:
 * The path to TOOLS_HOME in the top of the scripts should point to the correct TOOLS_HOME. 
 * The NAS_VERSION should be 5.2.2
 * The VERSION should be same as RELEASE above.
 * The PHOENIX_JAR should point to the phoenix-client.jar used on your system.


Note that the output from exportdanica.sh is the input to the importIntoNAS.sh.

## tools/loadSeeds.sh
Takes one argument: a seedsfile, or two arguments: a seedsfile --accepted
The result of this operation is added to the ingestlog table, and a rejectlog and an acceptlog is written to the same directory the seedsfile.
If the '--accepted' option is used, the seeds are declared with DanicaStatus.YES when they are inserted.<br/>
If the seed is already registered as a danica-seed, nothing happens.<br/>
If the seed is already registered as a not-danica-seed, the danicastate of the seed is changed to danica<br/>
Otherwise, the seed is registered as a danica-seed, and the domain of the seed created in the domains table<br/>

The template currently looks like this: [tools/loadSeeds.sh](tools/loadSeeds.sh)<br/>
The source code looks like this: [webdanica-core/src/main/java/dk/kb/webdanica/core/tools/loadSeeds.java](webdanica-core/src/main/java/dk/kb/webdanica/core/tools/LoadSeeds.java).

## tools/loadBlacklists.sh
This script adds a new active blacklist to our webdanica workflow.
We currently don't support updating or deleting a blacklist using this script.
The current procedure is to erase all blacklists using the Apache phoenix CLI client 'sqlline.py' part of the phoenix-bin package 'apache-phoenix-PHOENIXVERSION-HBase-HADOOPVERSION-bin.tar.gz'
(currently PHOENIXVERSION 4.7.1, and HADOOPVERSION 1.1) with command "delete from blacklists;"

The template currently looks like this: [tools/loadBlacklists.sh](tools/loadBlacklists.sh)<br/>
The source code looks like this: [webdanica-core/src/main/java/dk/kb/webdanica/core/tools/LoadBlacklists.java](webdanica-core/src/main/java/dk/kb/webdanica/core/tools/LoadBlacklists.java).

## tools/loadDomains.sh
Loads a domain-list into webdanica, inserting them into the domains table.<br/>
If the option --accepted is used, the domains are assumed to be fully danica domains, and no further processing is to occur on these domains.<br/>
If the option --rejected is used, the domains are assumed to be not danica, and no further processing is to occur on these domains.<br/>
Else the domains are ingested with danicastate UNDECIDED.

The template currently looks like this: [tools/loadDomains.sh](tools/loadDomains.sh)<br/>
The source code looks like this: [webdanica-core/src/main/java/dk/kb/webdanica/core/tools/LoadDomains.java](webdanica-core/src/main/java/dk/kb/webdanica/core/tools/LoadDomains.java).

## tools/exportDanica.sh
This exports all danica-seeds from webdanica to a file<br/>
During the export, the danica seeds not already exported are marked them as exported=true, and the exportedTime is set to the current date.<br/>

When using the option '--list_already_exported' all danica seeds is written to a file, including those seeds previously exported

This option is current used in the template script here: [tools/exportDanica.sh](tools/exportDanica.sh)<br/>
The source code looks like this: [webdanica-core/src/main/java/dk/kb/webdanica/core/tools/ExportFromWebdanica.java](webdanica-core/src/main/java/dk/kb/webdanica/core/tools/ExportFromWebdanica.java).

## tools/importIntoNAS.sh

Note that the script might need to be changed according to the database used by the NAS-system (derby or postgresql).<br/>
The seeds are added to a seedslist named 'webdanicaseeds' list. The seedlist is then added to the default configuration of the seed's domain if not already present.<br/>
If the domain does not exist in the NAS system, the domain is created, and the seeds added to the webdanica-seeds list as before, but all the seeds in the default seedlist created by NAS are disabled by prefixing each seed with a "#'

The argument are either one seed or a file with seeds.

The template currently looks like this: [tools/importIntoNAS.sh](tools/importIntoNAS.sh)<br/>
The source code looks like this: [webdanica-core/src/main/java/dk/kb/webdanica/core/tools/ImportIntoNetarchiveSuite.java](webdanica-core/src/main/java/dk/kb/webdanica/core/tools/ImportIntoNetarchiveSuite.java).

## tools/importDanica.sh

This can be used to add seeds we already known to be Danica. This is actually just the loadSeeds.sh with the --accepted argument preset.<br/>
The template currently looks like this: [tools/importDanica.sh](tools/importDanica.sh).<br/>
The source code looks like this: [webdanica-core/src/main/java/dk/kb/webdanica/core/tools/LoadSeeds.java](webdanica-core/src/main/java/dk/kb/webdanica/core/tools/LoadSeeds.java).

## tools/loadTest.sh

This script is used to test Webdanica datamodel.<br/>
It requires three arguments
 * numberofseedstoingest - e.g. 100000 (this will generate and insert 100K autogenerated seeds)
 * criteriaresultsdir - a folder with existing criteriaresults
 * criteriaresultmultiples - the amount of times to process the contents of this criteriaresultsdir folder

The template currently looks like this: [tools/loadTest.sh](tools/loadTest.sh).<br/>
The source code looks like this: [webdanica-core/src/main/java/dk/kb/webdanica/core/tools/LoadTest.java](webdanica-core/src/main/java/dk/kb/webdanica/core/tools/LoadTest.java).

## tools/showReports.sh

This script is able to show the reports for a specific netarchivesuite JobID found in the metadata file for the job.<br/>
It requires one argument, and has one optional argument --dont-print<br/>
```
bash showReports.sh JobID [--dont-print]
```
If the "--dont-print" argument is used, then the reports are not printed to screen, and it just reports the names/urls of the reports found.<br/>
The template currently looks like this: [tools/showReports.sh](tools/showReports.sh).<br/>
The source code looks like this: [webdanica-core/src/main/java/dk/kb/webdanica/core/tools/HarvestShowReports.java](webdanica-core/src/main/java/dk/kb/webdanica/core/tools/HarvestShowReports.java).

## tools/synchronizeTraps.sh

This scripts has no arguments. It synchronizes the blacklists in hbase with the global crawlertraps in netarchivesuite.<br/>
Currently, there is by default a maximum of 1000 characters on each trap found in hbase, because the default derby database has a max of 1000 characters.
This default can be changed by setting the 'settings.crawlertraps.maxTrapSize' explicitly.<br/>
Any trap exceeding this value or not valid xml is skipped during the synchronization.
During synchronization, each blacklist in hbase will be created as a globalcrawlertrap in Netarchivesuite with the same name, and contents.
If the globalcrawlertrap already exists in netarchivesuite, it will be updated.<br/>
The template currently looks like this: [tools/synchronizeTraps.sh](tools/synchronizeTraps.sh).<br/>
The source code looks like this: [webdanica-core/src/main/java/dk/kb/webdanica/core/interfaces/harvesting/SynchronizeCrawlertraps.java](webdanica-core/src/main/java/dk/kb/webdanica/core/interfaces/harvesting/SynchronizeCrawlertraps.java).

## tools/extractFromGithub.sh

This tool is used to extract the source code of a specific webdanica branch from github.<br/>
The only argument is branch (e.g. master)<br/>
The template currently looks like this: [tools/extractFromGithub.sh](tools/extractFromGithub.sh).

## tools/cacheTest.sh

This tool is used to update the statecache in hbase.<br/>
It has no arguments.<br/>
The template currently looks like this: [tools/cacheTest.sh](tools/cacheTest.sh).<br/>
The source code looks like this: [webdanica-core/src/main/java/dk/kb/webdanica/core/datamodel/Cache.java](webdanica-core/src/main/java/dk/kb/webdanica/core/datamodel/Cache.java).

## tools/databaseStats.sh
This tool is used to show the statistics of the webdanica tables.<br/>
The output looks like this:
```
Seeds-stats at 'Wed Sep 27 12:30:46 CEST 2017':
=========================================
Total-seeds: 0
#seeds with status 'NEW': 0
#seeds with status 'READY_FOR_HARVESTING': 0
#seeds with status 'HARVESTING_IN_PROGRESS': 0
#seeds with status 'HARVESTING_FINISHED': 0
#seeds with status 'READY_FOR_ANALYSIS': 0
#seeds with status 'ANALYSIS_COMPLETED': 0
#seeds with status 'REJECTED': 0
#seeds with status 'AWAITS_CURATOR_DECISION': 0
#seeds with status 'HARVESTING_FAILED': 0
#seeds with status 'DONE': 0
#seeds with status 'ANALYSIS_FAILURE': 0
Total number of entries in 'harvests' table: 0
Total number of entries in 'criteria_results' table: 0
Time spent computing the stats in secs: 22

```
The template currently looks like this: [tools/databaseStats.sh](tools/databaseStats.sh).<br/>
The source code looks like this: [webdanica-core/src/main/java/dk/kb/webdanica/core/tools/ComputeStats.java](webdanica-core/src/main/java/dk/kb/webdanica/core/tools/ComputeStats.java).

## miscellaneous tools available in webdanica-core package:

 * dk.kb.webdanica.core.tools.Harvest [webdanica-core/src/main/java/dk/kb/webdanica/core/tools/Harvest.java](webdanica-core/src/main/java/dk/kb/webdanica/core/tools/Harvest.java). Used in manuel workflow to do a one or several singleseedharvest. Usage: java Harvest <seedsfile>|<seed> [--store].
 * dk.kb.webdanica.core.tools.FindHarvestWarcs [webdanica-core/src/main/java/dk/kb/webdanica/core/tools/FindHarvestWarcs.java](webdanica-core/src/main/java/dk/kb/webdanica/core/tools/FindHarvestWarcs.java). Used in manual and automatic workflows to find warcs related to a harvestlog. Usage: java FindHarvestWarcs harvestLog filedir.
 * dk.kb.webdanica.core.tools.CheckSettings [webdanica-core/src/main/java/dk/kb/webdanica/core/tools/CheckSettings.java](webdanica-core/src/main/java/dk/kb/webdanica/core/tools/CheckSettings.java). can be used if our our settingsfiles are valid.
 * dk.kb.webdanica.core.tools.ShowContentType.java [webdanica-core/src/main/java/dk/kb/webdanica/core/tools/ShowContentType.java](webdanica-core/src/main/java/dk/kb/webdanica/core/tools/ShowContentType.java). Tool for checking the format of the files in a folder.
 * dk.kb.webdanica.core.tools.CriteriaIngestTool.java [webdanica-core/src/main/java/dk/kb/webdanica/core/tools/CriteriaIngestTool.java](webdanica-core/src/main/java/dk/kb/webdanica/core/tools/CriteriaIngestTool.java).Used in automatic and manuel workflow to ingest the analyzed data into hbase. Usage:
java dk.kb.webdanica.core.tools.CriteriaIngestTool <harvestlogfile> <criteria-results-dir> [--no-add-harvests-to-database] [--no-add-criteriaResults-to-database]
 * dk.kb.webdanica.core.tools.CheckListFileFormat.java [webdanica-core/src/main/java/dk/kb/webdanica/core/tools/CheckListFileFormat.java](webdanica-core/src/main/java/dk/kb/webdanica/core/tools/CheckListFileFormat.java). Used for checking the wordlists used in the analysis workflow. Usage: java CheckListFileFormat file


