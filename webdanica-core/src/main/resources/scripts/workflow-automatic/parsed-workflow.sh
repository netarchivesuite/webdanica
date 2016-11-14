HARVESTLOG=$1
DATADIR=$2
SEQDIR=$3
WORKFLOW_HOME=$4

if [ -z "$HARVESTLOG" ]; then
   echo "The 'harvestlog' argument is missing. Exiting program"
   exit 1
 fi

if [ -z "$DATADIR" ]; then
   echo "The 'datadir' argument is missing. Exiting program"
   exit 1
 fi

if [ -z "$SEQDIR" ]; then
   echo "The 'seqdir' argument is missing. Exiting program"
   exit 1
 fi

if [ ! -f "$HARVESTLOG" ]; then
   echo "The harvestlog $HARVESTLOG does not exist. Exiting program"
   exit 1
 fi

if [ ! -d "$DATADIR" ]; then
   echo "The datadir '$DATADIR' does not exist. Exiting program"
   exit 1
 fi

if [ ! -d "$SEQDIR" ]; then
   echo "The seqdir '$SEQDIR' does not exist. Exiting program"
   exit 1
 fi


#if [ ! -d "$SEQBASEDIR" ]; then
# echo $SEQBASEDIR does not exist. Trying to create it
# mkdir -p $SEQBASEDIR
# if [ ! -d "$SEQBASEDIR" ]; then
#   echo "Unable to create directory $SEQBASEDIR. Exiting program"
#   exit	
# fi
#fi

#DATESTAMP=`/bin/date '+%d-%m-%Y'`
#SEQDIR=$SEQBASEDIR/$DATESTAMP


WARCS=`bash findwarcs.sh $HARVESTLOG $DATADIR $WORKFLOW_HOME`
#WARCS="/home/test/WEBDANICA/bitarkiv/filedir/72-59-20160803112522487-00000-dia-prod-udv-01.kb.dk.warc.gz"

for J in $WARCS 
do
echo "Processing $J"
BASENAME=`basename $J`
DESTINATION=$SEQDIR/$BASENAME
mkdir -p $DESTINATION
echo do parsed-extract on file $BASENAME with destination $DESTINATION

bash parse-text-extraction.sh $J $DESTINATION

done

