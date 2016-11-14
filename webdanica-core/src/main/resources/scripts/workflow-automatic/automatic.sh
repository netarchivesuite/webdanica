#For alle harvestlog-filer fundet i /home/harvestlogs/ folderen

#1) Flyt harvestlog til workflow-folderen

#2) lav parsed-text af det høstede

#Generer et unikt SEQ_DIR i SEQ_BASEDIR (/home/test/SEQ)

SEQ_BASEDIR=/home/test/SEQ
CRITERIA_RESULTS_BASEDIR=/home/test/criteria-results/

TIMESTAMP=`/bin/date '+%d-%m-%Y-%s'`
SEQ_DIR=$SEQ_BASEDIR/$TIMESTAMP
mkdir -p $SEQ_DIR

HARVESTLOG_FILE=/home/test/workflow/harvestlog-test
DATADIR=/home/test/WEBDANICA/bitarkiv/filedir

bash parsed-workflow.sh $HARVESTLOG_FILE $DATADIR $SEQ_DIR 

rc=$?
if [[ $rc != 0 ]]; then echo "parsed-workflow failed"; exit $rc; fi

#3) lav kriterie-analyse med pig

#Generer et unikt criteria_results_DIR i CRITERIA_RESULTS_BASEDIR (e.g. /home/test/criteria-results/)

bash criteria-workflow.sh $SEQ_DIR $CRITERIA_RESULTS_DIR
rc=$?
if [[ $rc != 0 ]]; then echo "criteria-workflow failed"; exit $rc; fi

#eller 
#
#for  alternativ kriterie-analyse med pig (der anvender en liste af bynavne i UTF-16 tekst (gemt fra Excel som UTF8 tekst))
#tilpas stien til bynavne-filen, så den er rigtig, i filen pigscripts/criteriaRun-combinedComboJson-alt-seq.pig.
#p.t. peges der på stien /home/test/workflow/Bynavne_JEI_UTF16.txt
#
#bash criteria-workflow-alt.sh <SEQ_DIR> <criteria_results_DIR>

#4) Efterprocessering af kriteria-analysen og ingest i databasen
 
bash ingestTool.sh $HARVESTLOG_FILE $CRITERIA_RESULTS_DIR
rc=$?
if [[ $rc != 0 ]]; then echo "criteria ingest failed"; exit $rc; fi

