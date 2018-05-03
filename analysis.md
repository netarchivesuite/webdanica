# Analysis of the harvested data

With each execution of the HarvestWorkThread a number of seeds (defined by settings.harvesting.maxSingleSeedHarvests) is harvested through NetarchiveSuite, the harvest metadata fetched and stored in the database, and written to a harvestlog in the harvestlogDir (defined by setting settings.harvesting.harvestlogDir).

An example of such a file is the following
``` 
Harvestlog for harvests initiated by the Webdanica webapp at Tue May 01 18:37:21 CEST 2018
################################################################################
Seed: http://netarkivet.dk
HarvestName: webdanica-trial-1525192251644
Successful: true
EndState: DONE
HarvestedTime: 1525192595728
Files harvested: 2753-2810-20180501163526376-00000-kb-test-webdanica-001.kb.dk.warc.gz,2753-metadata-1.warc
Errors:  
################################################################################
``` 

In the example above, it contains only one harvest.

The script *webdanica-analysis-cron.sh* processes one harvestlog at a time, and again one harvest at a time.
The frequency is defined in your crontab for bash $HOME/automatic-workflow/webdanica-analysis-cron.sh.
In the example below, this is run every 2 hours.
```
HOME=/home/test
CRONDIR=/home/test/cronlog
## Run the webdanica-analysis-program every 2 hours
0 */2 * * *   bash $HOME/automatic-workflow/webdanica-analysis-cron.sh 2>&1 | tee -a $CRONDIR/webdanica-analysis-cron.sh.log
``` 

## The steps of the analysis

### Step 1 - making a text-extract of the harvested files (except any metadata-1.warc[.gz] files)

This is done by the script parse-text-extraction, that includes a call to hadoop:
``` 
$HADOOP_BIN_HOME/bin/hadoop jar $WORKFLOW_HOME/lib-parse/jbs-fatjar.jar org.archive.jbs.Parse $OUTPUTDIR $FILE_TO_EXTRACT
```

The output of the text-extraction is placed in a subfolder of $WORKFLOW_HOME/SEQ_AUTOMATIC/$TIMESTAMP
e.g 
```
/home/test/automatic-workflow/SEQ_AUTOMATIC/$TIMESTAMP/1438-1455-20171019220324290-00000-kb-test-webdanica-001.kb.dk.warc.gz
```
All parsed-extracts from a harvestlog is placed in the same folder $TIMESTAMP folder.

### Step 2 - criteria analysis is then done on the parsed-extracts using a pig script

During this step, we remember whether or not external files were used for some of the analyses.
This is later part of the data being shown on the criteriaresult page:
```
Information about CriteriaResult for url "http://gmpg.org/xfn/11.php", seedUri = "http://www.hammel.nu/", harvest="webdanica-trial-1509391977129":
URL: http://gmpg.org/xfn/11.php
UrlOrig: N/A
HarvestName: webdanica-trial-1509391977129
SeedUrl: http://www.hammel.nu/
DanishCode: -1065536(UNKNOWN) (Not decided - but has: neighboring tld++new limited freq. used dk word++size>250++new limited wordlist o,oe,ae,aa)
InsertedDate: Mon Oct 30 22:01:23 CET 201 

Log:
All relative paths are considered relative to: '/disk2/test/automatic-workflow'
Using cityFile '/disk2/test/automatic-workflow/wordslist/danishMajorCities_UTF8.txt' for C7B and C7G
Using danishNamesFile '/disk2/test/automatic-workflow/wordslist/DanishNames_UTF8.txt' for test C10C
Using foreningerFile 'wordslist/foreninger_lowercased_UTF8.txt' for test C8A
Using foreningerOneWordFile 'wordslist/foreninger_one_word_lowercased_UTF8.txt' for tests C8b and C8c
Using placeNamesFile 'wordslist/placenames_UTF8.txt' for tests C7C, C7D
Using virksomhederFile 'wordslist/virksomheder_lowercased_UTF8.txt' for test C9B
Using virksomhederOnewordFile 'wordslist/virksomheder_one_word_lowercased_UTF8.txt' for tests C9C and C9E

Liste over kriterie-analyser:
....
```

### Step 3 - Evaluating the output from the criteria-analysis

In this the criteria-analysis of each harvested seed is evaluated, and the seed is either declared as DANICA, NOT_DANICA, or UNDECIDED

If the setting *settings.analysis.considerSeedNotDanicaIfNotExplicitlyDanica* is enabled, we declare the seed NOT_DANICA instead of UNDECIDED





