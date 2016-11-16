INPUT=$1
OUTPUT=$2
SCRIPT=$3
WORKFLOW_HOME=$4
PIG_HOME=$5

## configure for correct pig version and log4j properties file
## points the GEOIP_FILE to /full/path/to/GeoIP.dat       (MAYBE to be used by webdanica project)

## bash scripts/$callscript \'$prefix$J'/*gz'\' $basedir/$dirname /home/hadoop/scripts/criteriaRun-combo-v1.pig &> $basedir/error-$dirname.log


#export PIG_OPTS=-verbose:class
FULL_PATH_TO_PIGBOOTUP=$WORKFLOW_HOME/conf/.pigbootup
echo "Using PIGBOOTUP: $FULL_PATH_TO_PIGBOOTUP"
export PIG_OPTS=-Dpig.load.default.statements=$FULL_PATH_TO_PIGBOOTUP
export LOG4J_CONFIG=/home/test/conf/log4j_pig.properties

export LOG4J="-Dlog4j.configuration=file:${LOG4J_CONFIG}"

USAGE="usage: pig-call-template INPUT-files OUTPUT-dir PIG-script WORKFLOW_HOME PIG_HOME"

if [[ -z "$INPUT" ]]
then
        echo "ERROR: Missing INPUT destination. $USAGE"
        exit 1
fi

if [[ -z "$OUTPUT" ]]
then
	echo "ERROR: Missing OUTPUT destination. $USAGE"
        exit 1
fi

if [[ -z "$SCRIPT" ]]
then
        echo "ERROR: Missing SCRIPT value. $USAGE"
        exit 1
fi

if [[ -z "$WORKFLOW_HOME" ]]
then
        echo "ERROR: Missing WORKFLOW_HOME value. $USAGE"
        exit 1
fi

if [[ -z "$PIG_HOME" ]]
then
        echo "ERROR: Missing PIG_HOME value. $USAGE"
        exit 1
fi

#GEOIP_FILE=/home/hadoop/disk5_instans_m001/GeoIP.dat
#LINKDATABASE_HOME=/home/hadoop/disk5_instans_m001/kopi-db
#export GEOIP_FILE LINKDATABASE_HOME

## Lav .started fil baseret på output filen

touch ${OUTPUT}.started

$PIG_HOME/bin/pig -x local -4 $LOG4J_CONFIG -f $SCRIPT -param input=$INPUT -param output=$OUTPUT
rc=$?
if [[ $rc != 0 ]]
then 
	echo "ERROR: pig call failed with exitcode $rc"
	touch ${OUTPUT}.finished.failed
        exit $rc
else 
	touch ${OUTPUT}.finished.success
        exit $rc
fi

