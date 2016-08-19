HADOOP_HOME=/home/test/hadoop-1.2.1/
FILE_TO_EXTRACT=$1
OUTPUTDIR=$2
#-Dlog4j.configuration=/home/test/
echo "performing text extraction on file $FILE_TO_EXTRACT' writing output to '$OUTPUTDIR'"

## TODO verify paths before running
$HADOOP_HOME/bin/hadoop jar ~/jbs-fatjar.jar org.archive.jbs.Parse $OUTPUTDIR $FILE_TO_EXTRACT 

#####################################################################################################################################################################
##./bin/hadoop jar ../jbs/jbs-fatjar.jar  org.archive.jbs.Parse /tmp/ ../jwat-tools-0.6.2/431-35-20160317083714655-00000-sb-test-har-001.statsbiblioteket.dk.warc.gz
#####################################################################################################################################################################

