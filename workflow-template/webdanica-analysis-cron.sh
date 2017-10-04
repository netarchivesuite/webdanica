SETENV=/home/test/automatic-workflow/setenv.sh
ME=`basename $0`
if [ -r "$SETENV" ]; then
  . "$SETENV"
else 
  echo "Error:The Path to setenv.sh ('$SETENV') is not correctly set in script $ME"
  exit 1
fi

## Verify JAVA_HOME

if [ ! -d "$JAVA_HOME" ]; then
 echo "ERROR: The JAVA_HOME '$JAVA_HOME' does not exist. Exiting program $ME"
   exit 1
fi

## Verify existence of conf/.pigbootup verify script
if [ ! -f $PIGBOOTUP_VERIFIER_SCRIPT ]; then
   echo "ERROR: The script '$PIGBOOTUP_VERIFIER_SCRIPT' does not exist. Exiting program $ME"
   exit 1
fi
## Verify validity of conf/.pigbootup 
RES=`bash $PIGBOOTUP_VERIFIER_SCRIPT $WORKFLOW_HOME`
if [ "$RES" != "" ]; then
     echo "Pig bootup file '$PIGBOOTUP_FILE' is invalid: '$RES'"    
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

if [ ! -d $WORKFLOW_HOME ]; then
   echo "ERROR: The WORKFLOW_HOME '$WORKFLOW_HOME' does not exist. Exiting program $ME"
   rm $BUSYFILE
   exit 1
fi

if [ ! -d $WEBDANICA_LIBDIR ]; then
   echo "ERROR: The WEBDANICA_LIBDIR '$WEBDANICA_LIBDIR' does not exist. Exiting program $ME"
   rm $BUSYFILE
   exit 1
fi

if [ -z $WEBDANICA_VERSION ]; then
   echo "ERROR: The WEBDANICA_VERSION is not set. Exiting program $ME"
   rm $BUSYFILE
   exit 1
fi

WEBDANICA_CORE_JARFILE=$WEBDANICA_LIBDIR/webdanica-core-${WEBDANICA_VERSION}.jar
if [ ! -f $WEBDANICA_CORE_JARFILE ]; then
   echo "ERROR: The jarfile '$WEBDANICA_CORE_JARFILE' does not exist. Exiting program $ME"
   rm $BUSYFILE
   exit 1
fi

cd $WORKFLOW_HOME
FILES=`bash $FINDLOGS_SCRIPT $WORKFLOW_HOME $WEBDANICA_VERSION $NAS_VERSION`
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

TOTALCOUNT=`wc -w <<< "$FILES"`
COUNT=0
echo "Found $TOTALCOUNT harvestlogs to process"
for J in $FILES
do
let "COUNT=COUNT+1"
echo "Processing harvestlog #$COUNT: $J"

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
bash $AUTOMATIC_SCRIPT $HARVESTLOG $WORKFLOW_HOME $WEBDATADIR $WEBDANICA_VERSION $HADOOP_BINBIN $PIG_HOME $NAS_VERSION
RESCODE=$?
if [ -z $RESCODE ]; then
   echo "ERROR: The $AUTOMATIC_SCRIPT returned $RESCODE. Moving $HARVESTLOG to $FAILEDJOBS"
   mv $HARVESTLOG $FAILEDJOBS/
else
   echo "Job successful - Moving $HARVESTLOG to $OKJOBS"
   mv $HARVESTLOG $OKJOBS/
fi

echo
echo "Processing done of harvestlog #$COUNT: $J "

done

## Remove busy-file
rm $BUSYFILE

TIMESTAMP=`date`
echo "Processing done of all $COUNT harvestlogs at $TIMESTAMP"

