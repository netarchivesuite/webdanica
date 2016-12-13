# Installation and configuration of the automatic workflow

An automatic workflow takes care of the analysis of the harvested files on the basis of harvestlog written by the webapp to a common
directory (e.g. /home/harvestLogs)

There is two scripts, one that automatically takes the available harvestlogs from the common directory, and processes the harvestlogs one by one, and one takes a harvestlog as argument and then processes the harvestlog:
 * webdanica-analysis-cron.sh
 * webdanica-analysis-manual.sh

These scripts include a file setenv.sh which must be configured correctly before use
``` 
WORKFLOW_USER_HOME=/home/test
WORKFLOW_HOME=$WORKFLOW_USER_HOME/automatic-workflow
WEBDATADIR=$WORKFLOW_USER_HOME/ARKIV
WEBDANICA_VERSION=0.4.0-SNAPSHOT
HADOOP_HOME=$WORKFLOW_USER_HOME/hadoop-1.2.1/
PIG_HOME=$WORKFLOW_USER_HOME/pig-0.16.0/
BUSYFILE=$WORKFLOW_HOME/.busy
WORKDIR=$WORKFLOW_HOME/working
OLDJOBSDIR=$WORKFLOW_HOME/oldjobs
JAVA_HOME=/usr/java/jdk1.8.0_92_x64
PATH=$JAVA_HOME/bin:$PATH
FINDLOGS_SCRIPT=${WORKFLOW_HOME}/findharvestlogs.sh
AUTOMATIC_SCRIPT=${WORKFLOW_HOME}/automatic.sh
export WORKFLOW_HOME WEBDATADIR WEBDANICA_VERSION HADOOP_HOME PIG_HOME BUSYFILE WORKDIR OLDJOBSDIR JAVA_HOME PATH FINDLOGS_SCRIPT AUTOMATIC_SCRIPT
```

The import settings to look at is the WEBDATADIR, WEBDANICA_VERSION, and JAVA_HOME

Furthermore hadoop-1.2.1 and pig-0.16.0 must be downloaded and unzipped into the WORKFLOW_USER_HOME.

## The installation of the automatic-workflow 

Fetch the automatic-workflow folder from github: 

Copy the folder to its correct location, and change the owner of the files to the user running the script

Add the execution of the workflow to the crontab





