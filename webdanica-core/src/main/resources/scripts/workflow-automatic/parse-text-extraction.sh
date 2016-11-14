FILE_TO_EXTRACT=$1
OUTPUTDIR=$2
HADOOP_HOME=$3
WORKFLOW_HOME=$4

#-Dlog4j.configuration=/home/test/

if [ -z "$FILE_TO_EXTRACT" ]; then
 echo
 echo ERROR: FILE_TO_EXTRACT argument is not set!
 echo
 exit 1
fi

echo "performing text extraction on file $FILE_TO_EXTRACT' writing output to '$OUTPUTDIR'"
NAME=$(basename $1)

## verify that $OUTPUTDIR/$NAME must not exist before
if [ -d "$OUTPUTDIR/$NAME" ]; then
 echo
 echo ERROR: Directory \'$OUTPUTDIR/$NAME\' already exists. Use another outputdirectory
 echo
 exit 1
fi

if [ ! -f "$FILE_TO_EXTRACT" ]; then
 echo
 echo ERROR: FILE_TO_EXTRACT argument \'$FILE_TO_EXTRACT\' does not exist.
 echo
 exit 1
fi

if [ -z "$HADOOP_HOME" ]; then
 echo
 echo ERROR: HADOOP_HOME argument is not set!
 echo
 exit 1
fi


if [ ! -d "$HADOOP_HOME" ]; then
 echo
 echo ERROR: HADOOP_HOME directory \'$HADOOP_HOME\' does not exist.
 echo
 exit 1
fi

if [ -z "$WORKFLOW_HOME" ]; then
 echo
 echo ERROR: WORKFLOW_HOME argument is not set!
 echo
 exit 1
fi

if [ ! -d "$WORKFLOW_HOME" ]; then
 echo
 echo ERROR: WORKFLOW_HOME directory \'$WORKLOW_HOME\' does not exist.
 echo
 exit 1
fi

$HADOOP_HOME/bin/hadoop jar $WORKFLOW_HOME/lib-parse/jbs-fatjar.jar org.archive.jbs.Parse $OUTPUTDIR $FILE_TO_EXTRACT 
#2>&1 log/parse-${LOG}.log

