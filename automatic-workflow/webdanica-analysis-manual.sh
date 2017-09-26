SETENV=/home/test/automatic-workflow/setenv.sh
ME=`basename $0`
if [ -r "$SETENV" ]; then
  . "$SETENV"
else 
  echo Error: Path to setenv.sh is not correctly set in script $ME
  exit 1
fi

if [ ! -f $AUTOMATIC_SCRIPT ]; then
   echo "ERROR: The script '$AUTOMATIC_SCRIPT' does not exist. Exiting program"
   exit 1
 fi

cd $WORKFLOW_HOME

HARVESTLOG_FILE=$1

if [ -z $HARVESTLOG_FILE ]; then
   echo "ERROR: The HARVESTLOG_FILE argument (arg #1) is not set. Exiting program $PROG"
   exit 1
fi

echo Processing harvestlog: $HARVESTLOG_FILE
J=$HARVESTLOG_FILE

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
bash $AUTOMATIC_SCRIPT $HARVESTLOG $WORKFLOW_HOME $WEBDATADIR $WEBDANICA_VERSION $HADOOP_BINBIN $PIG_HOME $NAS_VERSION
RESCODE=$?
if [ -z $RESCODE ]; then
   echo "ERROR: The $AUTOMATIC_SCRIPT returned $RESCODE. Exiting program"
fi

## move $HARVESTLOG to OLDJOBSDIR
mv $HARVESTLOG $OLDJOBSDIR
echo
echo "Processing done of harvestlog: $J "

