if [ -z "$1" ]; then
 echo ERROR: No SEQ_BASEDIR argument is given!
 echo "USAGE: bash criteria-worklow.sh <seq_basedir>"
 exit
fi
SEQ_BASEDIR=$1

BASE_CRITERIARESULTS_DIR=/home/test/criteria-results
SCRIPTPATH=/home/test/workflow/pigscripts/criteriaRun-combinedComboJson-seq.pig

DATESTAMP=`/bin/date '+%d-%m-%Y-%s'`

if [ ! -d "$SEQ_BASEDIR" ]; then
 echo ERROR: $SEQ_BASEDIR does not exist. The path must be wrong!
 exit 1
fi
SEQDIRS=`ls $SEQ_BASEDIR`
# Use the below to test on a single SEQ file
#SEQDIRS="54-41-20160803095223149-00000-dia-prod-udv-01.kb.dk.warc.gz"
CRITERIARESULTSDIR=$BASE_CRITERIARESULTS_DIR/$DATESTAMP
mkdir -p $CRITERIARESULTSDIR

for J in $SEQDIRS
do
FILE=$SEQ_BASEDIR/$J/$J
if [ ! -f "$FILE" ]; then
 echo ERROR: seqfile $FILE does not exist. The parsed-text computation must have gone wrong
 exit 1
fi

## TODO look for the SUCCESS file in the $SEQBASEDIR/$J directory

DESTINATION=$CRITERIARESULTSDIR/$J
#mkdir -p $DESTINATION
echo do criteria-analysis on file $FILE with destination $DESTINATION
bash pig16-call-script.sh $FILE $DESTINATION $SCRIPTPATH
done

