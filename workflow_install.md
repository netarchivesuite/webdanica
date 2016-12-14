# Installation and configuration of the automatic workflow

An automatic workflow takes care of the analysis of the harvested files on the basis of harvestlog written by the webapp to a common
directory (e.g. /home/harvestLogs)

There is two scripts, one that automatically takes the available harvestlogs from the common directory, and processes the harvestlogs one by one, and one takes a harvestlog as argument and then processes the harvestlog:
 * webdanica-analysis-cron.sh
 * webdanica-analysis-manual.sh

These scripts include a file setenv.sh which must be configured correctly before use
``` 
WORKFLOW_USER_HOME=/home/test
WEBDANICA_VERSION=0.4.0-SNAPSHOT
WORKFLOW_HOME=$WORKFLOW_USER_HOME/automatic-workflow
WEBDATADIR=$WORKFLOW_USER_HOME/ARKIV
HADOOP_HOME=$WORKFLOW_USER_HOME/hadoop-1.2.1/
PIG_HOME=$WORKFLOW_USER_HOME/pig-0.16.0/
JAVA_HOME=/usr/java/jdk1.8.0_92_x64
## the below settings should not be altered
BUSYFILE=$WORKFLOW_HOME/.busy
WORKDIR=$WORKFLOW_HOME/working
OLDJOBSDIR=$WORKFLOW_HOME/oldjobs
PATH=$JAVA_HOME/bin:$PATH
FINDLOGS_SCRIPT=${WORKFLOW_HOME}/findharvestlogs.sh
AUTOMATIC_SCRIPT=${WORKFLOW_HOME}/automatic.sh
export WORKFLOW_HOME WEBDATADIR WEBDANICA_VERSION HADOOP_HOME PIG_HOME BUSYFILE WORKDIR OLDJOBSDIR JAVA_HOME PATH FINDLOGS_SCRIPT AUTOMATIC_SCRIPT
```

The import settings to look at is the WEBDATADIR, WEBDANICA_VERSION, and JAVA_HOME

Furthermore hadoop-1.2.1(http://archive.apache.org/dist/hadoop/core/hadoop-1.2.1/hadoop-1.2.1.tar.gz) and pig-0.16.0(http://ftp.download-by.net/apache/pig/pig-0.16.0/pig-0.16.0.tar.gz) must be downloaded and unpacked into the WORKFLOW_USER_HOME.

## The installation of the automatic-workflow 

Fetch the automatic-workflow folder from github using the script in the toolsfolder 

Copy the folder to its correct location, and change the owner of the files to the user running the script

cd automatic-workflow

Add the execution of the workflow to the crontab







