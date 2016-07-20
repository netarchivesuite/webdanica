## template for calling pig script: scripts/criteriaRun-combo-v1.pig
##
basedir=/home/hadoop/disk5_instans_m001
callscript=pig-call-disk5-instans.sh

## Modify dirprefix, prefix, and JOBS.
## the rest should NOT be changed
prefix="/disk4/data/1/"
dirprefix="combo-r1-m001-disk4_1"
JOBS="1-24 1-25 1-26 1-27 1-28 1-29"

DATESTAMP=`/bin/date '+%d-%m-%Y'`
echo Beginning processing of subfolders: $JOBS at $DATESTAMP
for J in $JOBS 
do
DATESTAMP=`/bin/date '+%d-%m-%Y'`
dirname=$dirprefix-$J-$DATESTAMP
echo Processing folder $J starting at $DATESTAMP
bash scripts/$callscript \'$prefix$J'/*gz'\' $basedir/$dirname /home/hadoop/scripts/criteriaRun-combo-v1.pig &> $basedir/error-$dirname.log

DATESTAMPNEW=`/bin/date '+%d-%m-%Y'`
echo Processing folder $J begun at $DATESTAMP ended at $DATESTAMPNEW
echo Result in folder $basedir/$dirname
done



