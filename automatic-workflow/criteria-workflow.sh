SEQ_BASEDIR=$1
CRITERIARESULTSDIR=$2
WORKFLOW_HOME=$3
PIG_HOME=$4

USAGE="USAGE: bash criteria-worklow.sh <seq_basedir> <criteriaresultsdir> <workflow_home> <pig_home>"


if [ -z "$1" ]; then
 echo ERROR: No SEQ_BASEDIR argument is given!
 echo $USAGE
 exit 1
fi
if [ -z "$2" ]; then
 echo ERROR: No CRITERIARESULTSDIR argument is given!
 echo $USAGE
 exit 1
fi
if [ -z "$3" ]; then
 echo ERROR: No WORKFLOW_HOME argument is given!
 echo $USAGE
 exit 1
fi

if [ -z "$4" ]; then
 echo ERROR: No PIG_HOME argument is given!
 echo $USAGE
 exit 1
fi


SCRIPTPATH=$WORKFLOW_HOME/pigscripts/criteriaRun-combinedCombo-seq.pig
SEQDIRS=`ls $SEQ_BASEDIR`
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

