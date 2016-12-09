FILE_TO_EXTRACT=$1
OUTPUTDIR=$2
HADOOP_BIN_HOME=$3
WORKFLOW_HOME=$4

PROG=`basename "$0"`
PARENT_COMMAND="$(ps -o args= $PPID)"
#-Dlog4j.configuration=/home/test/

if [ -z "$FILE_TO_EXTRACT" ]; then
 echo "ERROR: FILE_TO_EXTRACT argument (arg #1) is not set!. Called by '$PARENT_COMMAND' . Exiting program $PROG"
 exit 1
fi
if [ -z "$OUTPUTDIR" ]; then
 echo "ERROR: OUTPUTDIR argument (arg #2) is not set!"
 exit 1
fi

if [ -z "$HADOOP_BIN_HOME" ]; then
 echo "ERROR: HADOOP_BIN_HOME argument (arg #3) is not set!  Called by $PARENT_COMMAND. Exiting program $PROG"
 exit 1
fi

if [ -z "$WORKFLOW_HOME" ]; then
 echo "ERROR: WORKFLOW_HOME argument (arg #4) is not set!  Called by $PARENT_COMMAND. Exiting program $PROG"
 exit 1
fi


echo "performing text extraction on file $FILE_TO_EXTRACT' writing output to '$OUTPUTDIR'"
NAME=$(basename $1)

## verify that $OUTPUTDIR exists and $OUTPUTDIR/$NAME does not exist now
if [ ! -d "$OUTPUTDIR" ]; then
 echo
 echo ERROR: OUTPUTDIR \'$OUTPUTDIR\' does not exists. Select an existing OUTPUTDIR. Exiting program $PROG
 echo
 exit 1
fi

if [ -d "$OUTPUTDIR/$NAME" ]; then
 echo
 echo ERROR: Directory \'$OUTPUTDIR/$NAME\' already exists. Use another OUTPUTDIR where this is not the case. Exiting program $PROG
 echo
 exit 1
fi

if [ ! -f "$FILE_TO_EXTRACT" ]; then
 echo
 echo "ERROR: FILE_TO_EXTRACT argument \'$FILE_TO_EXTRACT\' does not exist. Exiting program $PROG"
 echo
 exit 1
fi

if [ ! -d "$HADOOP_BIN_HOME" ]; then
 echo
 echo ERROR: HADOOP_BIN_HOME directory \'$HADOOP_BIN_HOME\' does not exist.  Exiting program $PROG
 echo
 exit 1
fi

if [ ! -d "$WORKFLOW_HOME" ]; then
 echo
 echo ERROR: WORKFLOW_HOME directory \'$WORKFLOW_HOME\' does not exist. Exiting program $PROG
 echo
 exit 1
fi

$HADOOP_BIN_HOME/bin/hadoop jar $WORKFLOW_HOME/lib-parse/jbs-fatjar.jar org.archive.jbs.Parse $OUTPUTDIR $FILE_TO_EXTRACT 
#2>&1 log/parse-${LOG}.log
RESCODE=$?
if [ -z $RESCODE ]
then
   echo "ERROR: The call to parse-text-extraction.sh on file '$FILE_TO_EXTRACT' failed"
   exit 1	
else  
   exit 0		  
fi



