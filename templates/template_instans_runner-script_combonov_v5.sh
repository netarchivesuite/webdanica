## template for calling pig script: scripts/criteriaRun-comboNov-v5.pig
##

basedir=/home/hadoop/disk5_instans_m001
callscript=pig-call-disk5-instans.sh

## Modify dirprefix, prefix, and JOBS.
## the rest should NOT be changed
prefix="/disk5/data/3/"
dirprefix="comboNov-r5-m001-disk5_3"
JOBS="3-1  3-2  3-3  3-4  3-5"

DATESTAMP=`/bin/date '+%d-%m-%Y'`
echo Beginning processing of subfolders: $JOBS at $DATESTAMP
for J in $JOBS 
do
DATESTAMP=`/bin/date '+%d-%m-%Y'`
dirname=$dirprefix-$J-$DATESTAMP
echo Processing folder $J starting at $DATESTAMP
bash scripts/$callscript \'$prefix$J'/*gz'\' $basedir/$dirname /home/hadoop/scripts/criteriaRun-comboNov-v5.pig &> $basedir/error-$dirname.log

DATESTAMPNEW=`/bin/date '+%d-%m-%Y'`
echo Processing folder $J begun at $DATESTAMP ended at $DATESTAMPNEW
echo Result in folder $basedir/$dirname
done


echo Processing finished at `/bin/date`

THISSCRIPT=`basename $0`
THISMACHINE=`uname -n`
FINISHED_TEXT="Processing is finished of script $THISSCRIPT on machine $THISMACHINE" 
RECIPIENT=svc@kb.dk
echo $FINISHED_TEXT >> /tmp/$THISSCRIPT.mailtext

cat /tmp/$THISSCRIPT.mailtext | mail -s "$FINISHED_TEXT" $RECIPIENT

rm /tmp/$THISSCRIPT.mailtext

