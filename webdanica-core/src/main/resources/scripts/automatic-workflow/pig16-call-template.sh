## template for pig script pig-call-template.sh
## configure for correct pig version and log4j properties file

## points the GEOIP_FILE to /full/path/to/GeoIP.dat       (MAYBE to be used by webdanica project)
## points the LINKDATABASE_HOME to /full/path/to/kopi-db  (NOT to used by webdanica project)

## bash scripts/$callscript \'$prefix$J'/*gz'\' $basedir/$dirname /home/hadoop/scripts/criteriaRun-combo-v1.pig &> $basedir/error-$dirname.log

export PIG_VERSION=0.16.0
#export PIG_HOME=$HOME/pig-$PIG_VERSION
export PIG_HOME=/home/test/pig-$PIG_VERSION
#export PIG_OPTS=-verbose:class
export LOG4J_CONFIG=/home/test/conf/log4j_pig.properties

export LOG4J="-Dlog4j.configuration=file:${LOG4J_CONFIG}"
USAGE="usage: pig-call-template INPUT-files OUTPUT-dir PIG-script"

#echo "fetching the input/output arguments"

INPUT=$1
OUTPUT=$2
SCRIPT=$3

echo "INPUT=$INPUT"
echo "OUTPUT=$OUTPUT"
echo "SCRIPT=$SCRIPT"


if [[ -z "$INPUT" ]]
then
        echo "Missing INPUT destination. $USAGE"
        exit 1
fi

if [[ -z "$OUTPUT" ]]
then
	echo "Missing OUTPUT destination. $USAGE"
        exit 1
fi

if [[ -z "$SCRIPT" ]]
then
        echo "Missing SCRIPT value. $USAGE"
        exit 1
fi

GEOIP_FILE=/home/hadoop/disk5_instans_m001/GeoIP.dat
LINKDATABASE_HOME=/home/hadoop/disk5_instans_m001/kopi-db
export GEOIP_FILE LINKDATABASE_HOME

## Lav .started fil baseret på output filen

touch ${OUTPUT}.started

$PIG_HOME/bin/pig -x local -4 $LOG4J_CONFIG -f $SCRIPT -param input=$INPUT -param output=$OUTPUT

touch ${OUTPUT}.finished
