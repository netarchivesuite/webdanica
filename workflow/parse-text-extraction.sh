HADOOP_HOME=/home/test/hadoop-1.2.1/
WORKFLOW_HOME=/home/test/workflow/
FILE_TO_EXTRACT=$1
OUTPUTDIR=$2
#-Dlog4j.configuration=/home/test/
echo "performing text extraction on file $FILE_TO_EXTRACT' writing output to '$OUTPUTDIR'"
NAME=$(basename $1)
## verify that $OUTPUTDIR/$NAME must not exist before
if [ -d "$OUTPUTDIR/$NAME" ]; then
 echo
 echo ERROR: $OUTPUTDIR/$NAME already exists. Use another outputdirectory
 echo
 exit
fi
if [ ! -f "$FILE_TO_EXTRACT" ]; then
 echo
 echo ERROR: $FILE_TO_EXTRACT does not exist.
 echo
 exit
fi

$HADOOP_HOME/bin/hadoop jar $WORKFLOW_HOME/lib-parse/jbs-fatjar.jar org.archive.jbs.Parse $OUTPUTDIR $FILE_TO_EXTRACT 
#2>&1 log/parse-${LOG}.log

#####################################################################################################################################################################
##./bin/hadoop jar ../jbs/jbs-fatjar.jar  org.archive.jbs.Parse /tmp/ ../jwat-tools-0.6.2/431-35-20160317083714655-00000-sb-test-har-001.statsbiblioteket.dk.warc.gz
#####################################################################################################################################################################

