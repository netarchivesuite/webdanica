export PIG_VERSION=0.12.1
#export PIG_HOME=$HOME/pig-$PIG_VERSION
export PIG_HOME=/usr/local/pig-$PIG_VERSION
export LOG4J_CONFIG=/home/hadoop/conf/log4j_disk2-instans.properties

export LOG4J="-Dlog4j.configuration=file:${LOG4J_CONFIG}"

#echo "fetching the input/output arguments"

INPUT=$1
OUTPUT=$2
SCRIPT=$3

echo "INPUT=$INPUT"
echo "OUTPUT=$OUTPUT"
echo "SCRIPT=$SCRIPT"


if [[ -z "$INPUT" ]]
then
        echo "Missing OUTPUT destination"
        exit 1
fi

if [[ -z "$OUTPUT" ]]
then
	echo "Missing OUTPUT destination"
        exit 1
fi

if [[ -z "$SCRIPT" ]]
then
        echo "Missing SCRIPT value"
        exit 1
fi


GEOIP_FILE=/home/hadoop/disk2_instans_m001/GeoIP.dat
LINKDATABASE_HOME=/home/hadoop/disk2_instans_m001/kopi-db
export GEOIP_FILE LINKDATABASE_HOME


## Lav .started fil baseret på output filen

touch ${OUTPUT}.started


$PIG_HOME/bin/pig -x local -4 $LOG4J_CONFIG -f $SCRIPT -param input=$INPUT -param output=$OUTPUT



touch ${OUTPUT}.finished
