SETENV=/home/test/automatic-workflow/setenv.sh
ME=`basename $0`
if [ -r "$SETENV" ]; then
  . "$SETENV"
else 
  echo Error: Path to setenv.sh is not correctly set in script $ME
  exit 1
fi

if [ -f $BUSYFILE ]; then
   STAT=`stat -c %y $BUSYFILE` 
   echo WARNING: Analysis-workflow already in progress. The current workflow started at: $STAT  
   exit 1
fi
## mark the workflow as being in progress
touch $BUSYFILE

if [ ! -f $FINDLOGS_SCRIPT ]; then
   echo "ERROR: The script '$FINDLOGS_SCRIPT' does not exist. Exiting program $ME"
   rm $BUSYFILE	
   exit 1
 fi

if [ ! -f $AUTOMATIC_SCRIPT ]; then
   echo "ERROR: The script '$AUTOMATIC_SCRIPT' does not exist. Exiting program $ME"
   rm $BUSYFILE
   exit 1
 fi


## TODO verify that workflow-home and other paths exists 

cd $WORKFLOW_HOME
FILES=`bash $FINDLOGS_SCRIPT $WORKFLOW_HOME $WEBDANICA_VERSION`
RESCODE=$?
if [ -z $RESCODE ]; then
   echo "ERROR: The script '$FINDLOGS_SCRIPT' failed. Exiting program $ME"
   rm $BUSYFILE
   exit 1	
fi
if [[ ! -z $FILES ]]
then
  echo Found harvest-logs: $FILES
  ###echo Found no harvest-logs. No processing needed
fi 

for J in $FILES
do
echo Processing harvestlog: $J


## move $J to WORKDIR
NAME=$(basename $J)
HARVESTLOG=$WORKDIR/$NAME

mv $J $HARVESTLOG
RESCODE=$?
if [ -z $RESCODE ]; then
   echo "ERROR: Failed to move the file $J to $HARVESTLOG. Exiting program $ME"
   rm $BUSYFILE
   exit 1
fi

## start_progress
bash $AUTOMATIC_SCRIPT $HARVESTLOG $WORKFLOW_HOME $WEBDATADIR $WEBDANICA_VERSION $HADOOP_HOME $PIG_HOME
RESCODE=$?
if [ -z $RESCODE ]; then
   echo "ERROR: The $AUTOMATIC_SCRIPT returned $RESCODE. Exiting program $ME"
fi

## move $HARVESTLOG to OLDJOBSDIR
mv $HARVESTLOG $OLDJOBSDIR
echo
echo "Processing done of harvestlog: $J "
done

## Remove busy-file
rm $BUSYFILE

