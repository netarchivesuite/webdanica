## possibly put this in bash_profile
export WORKFLOW_HOME=/home/test/automatic-workflow/
export WEBDATADIR=/home/test/ARKIV
export WEBDANICA_VERSION=0.4.0-SNAPSHOT
export HADOOP_HOME=/home/test/hadoop-1.2.1/
export PIG_HOME=/home/test/pig-0.16.0/
export BUSYFILE=$WORKFLOW_HOME/.busy
export WORKDIR=$WORKFLOW_HOME/working
export OLDJOBSDIR=$WORKFLOW_HOME/oldjobs

if [ -f $BUSYFILE ]; then
   STAT=`stat -c %y $BUSYFILE` 
   echo WARNING: Analysis-workflow already in progress. The current workflow started at: $STAT  
   exit 1
fi
## mark the workflow as being in progress
touch $BUSYFILE

FINDLOGS_SCRIPT=${WORKFLOW_HOME}/findharvestlogs.sh
AUTOMATIC_SCRIPT=${WORKFLOW_HOME}/automatic.sh

if [ ! -f $FINDLOGS_SCRIPT ]; then
   echo "ERROR: The script '$FINDLOGS_SCRIPT' does not exist. Exiting program"
   rm $BUSYFILE	
   exit 1
 fi

if [ ! -f $AUTOMATIC_SCRIPT ]; then
   echo "ERROR: The script '$AUTOMATIC_SCRIPT' does not exist. Exiting program"
   rm $BUSYFILE
   exit 1
 fi


## TODO verify that workflow-home and other paths exists 

cd $WORKFLOW_HOME
FILES=`bash $FINDLOGS_SCRIPT $WORKFLOW_HOME $WEBDANICA_VERSION`
RESCODE=$?
if [ -z $RESCODE ]; then
   echo "ERROR: The script '$FINDLOGS_SCRIPT' failed. Exiting program"
   rm $BUSYFILE
   exit 1	
fi
if [ ! -z $FILES ]
then
  echo Found harvest-logs: $FILES
else
  echo Found no harvest-logs. No processing needed
fi 

for J in $FILES
do
echo Processing $J

## move $J to WORKDIR
NAME=$(basename $J)
HARVESTLOG=$WORKDIR/$NAME

mv $J $HARVESTLOG
RESCODE=$?
if [ -z $RESCODE ]; then
   echo "ERROR: Failed to move the file $J to $HARVESTLOG. Exiting program"
   rm $BUSYFILE
   exit 1
fi

## start_progress
bash $AUTOMATIC_SCRIPT $HARVESTLOG $WORKFLOW_HOME $WEBDATADIR $WEBDANICA_VERSION $HADOOP_HOME $PIG_HOME
RESCODE=$?
if [ -z $RESCODE ]; then
   echo "ERROR: The $AUTOMATIC_SCRIPT returned $RESCODE. Exiting program"
fi

## move $HARVESTLOG to OLDJOBSDIR
mv $HARVESTLOG $OLDJOBSDIR
done
## Remove busy-file
rm $BUSYFILE

