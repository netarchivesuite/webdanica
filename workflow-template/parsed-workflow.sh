HARVESTLOG=$1
DATADIR=$2
SEQDIR=$3
WORKFLOW_HOME=$4
HADOOP_HOME=$5
WEBDANICA_VERSION=$6
NAS_VERSION=$7

PROG=`basename "$0"`

if [ -z "$HARVESTLOG" ]; then
   echo "The 'HARVESTLOG' argument is missing (arg #1). Exiting program $PROG"
   exit 1
 fi

if [ -z "$DATADIR" ]; then
   echo "The 'DATADIR' argument is missing (arg #2). Exiting program $PROG"
   exit 1
 fi

if [ -z "$SEQDIR" ]; then
   echo "The 'SEQDIR' argument is missing (arg #3). Exiting program $PROG"
   exit 1
 fi

if [ -z "$WORKFLOW_HOME" ]; then
   echo "The 'WORKFLOW_HOME' argument is missing (arg #4). Exiting program $PROG"
   exit 1
 fi

if [ -z "$HADOOP_HOME" ]; then
   echo "The 'HADOOP_HOME' argument is missing (arg #5). Exiting program $PROG"
   exit 1
 fi

if [ -z "$WEBDANICA_VERSION" ]; then
   echo "The 'WEBDANICA_VERSION' argument is missing (arg #6). Exiting program $PROG"
   exit 1
 fi

if [ ! -f "$HARVESTLOG" ]; then
   echo "The harvestlog $HARVESTLOG does not exist. Exiting program $PROG"
   exit 1
 fi

if [ ! -d "$DATADIR" ]; then
   echo "The datadir '$DATADIR' does not exist. Exiting program $PROG"
   exit 1
 fi

if [ ! -d "$SEQDIR" ]; then
   echo "The seqdir '$SEQDIR' does not exist. Exiting program $PROG"
   exit 1
 fi


echo Calling "bash findwarcs.sh $HARVESTLOG $DATADIR $WORKFLOW_HOME $WEBDANICA_VERSION $NAS_VERSION" 

WARCS=`bash findwarcs.sh $HARVESTLOG $DATADIR $WORKFLOW_HOME $WEBDANICA_VERSION $NAS_VERSION`
RESCODE=$?
if [ $RESCODE -ne 0 ]; then
   echo "ERROR: The script 'findwarcs.sh' failed with statuscode $RESCODE. Exiting program $PROG"
   exit 1       
fi
echo Found warcs: $WARCS
failures=0
successes=0
for J in $WARCS 
do
echo "Processing $J"
BASENAME=`basename $J`
DESTINATION=$SEQDIR/$BASENAME
mkdir -p $DESTINATION
echo "do parsed-extract on file $BASENAME with destination $DESTINATION"
bash parse-text-extraction.sh $J $DESTINATION $HADOOP_HOME $WORKFLOW_HOME &>> logs/parsed.log
RESCODE=$?
if [ $RESCODE -ne 0 ]
then
   echo "ERROR: The call to parse-text-extraction.sh on file $J failed"
   failures=$((failures+1))
else 
   successes=$((successes+1))
fi
done
if [ "$successes" -gt 0 ]
then
    echo "Considering command successful: #successes=$successes,#failures=$failures"
    exit 0		
else
    exit 1
fi
