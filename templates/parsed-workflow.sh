DATADIR=/home/test/WEBDANICA/bitarkiv/filedir/
SEQBASEDIR=/home/test/SEQ/

if [ ! -d "$SEQBASEDIR" ]; then
 echo $SEQBASEDIR does not exist. Trying to create it
 mkdir -p $SEQBASEDIR
 if [ ! -d "$SEQBASEDIR" ]; then
   echo "Unable to create directory $SEQBASEDIR. Exiting program"
   exit	
 fi
fi

WARCS=`findwarcgz.sh $DATADIR`
#WARCS="/home/test/WEBDANICA/bitarkiv/filedir/72-59-20160803112522487-00000-dia-prod-udv-01.kb.dk.warc.gz"

for J in $WARCS 
do
BASENAME=`basename $J`
DESTINATION=$SEQBASEDIR/$BASENAME
mkdir -p $DESTINATION
echo do parsed-extract on file $BASENAME with destination $DESTINATION

bash parse-text-extraction.sh $J $DESTINATION
done

