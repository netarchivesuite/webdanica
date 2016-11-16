SEQ_BASEDIR=$1
CRITERIARESULTSDIR=$2
WORKFLOW_HOME=$3
PIG_HOME=$4

if [ -z "$1" ]; then
 echo ERROR: No SEQ_BASEDIR argument is given!
 echo "USAGE: bash criteria-worklow.sh <seq_basedir>"
 exit
fi

SCRIPTPATH=$WORKFLOW_HOME/pigscripts/criteriaRun-combinedComboJson-seq.pig

for J in $SEQDIRS
do
FILE=$SEQ_BASEDIR/$J/$J
if [ ! -f "$FILE" ]; then
 echo ERROR: seqfile $FILE does not exist. The parsed-text computation must have gone wrong
 exit 1
fi

## TODO look for the SUCCESS file in the $SEQBASEDIR/$J directory
DESTINATION=$CRITERIARESULTSDIR/$J
echo "do criteria-analysis on file $FILE with destination $DESTINATION"
bash pig16-call-script.sh $FILE $DESTINATION $SCRIPTPATH $WORKFLOW_HOME $PIG_HOME
rc=$?
if [[ $rc != 0 ]]; then 
	echo "ERROR: criteria-analysis on file $FILE with destination $DESTINATION: failed with exitcode $rc"
        exit $rc
fi
done

