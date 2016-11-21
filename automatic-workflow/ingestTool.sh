##bash ingestTool.sh $HARVESTLOG_FILE $CRITERIARESULTS_DIR $WORKFLOW_HOME
WORKFLOW_HOME=$3
SETTINGSFILE=$WORKFLOW_HOME/conf/webdanica_settings.xml 
OPTS2=-Dwebdanica.settings.file=$SETTINGSFILE
OPTS3=-Dlogback.configurationFile=$WORKFLOW_HOME/conf/silent_logback.xml 

if [ ! -f "$SETTINGSFILE" ]; then
   echo "The SETTINGSFILE \'$SETTINGSFILE\' does not exist. Exiting program"
   exit 1
fi

java $OPTS2 $OPTS3 -cp lib/webdanica-core-0.4.0-SNAPSHOT.jar:lib/phoenix-4.7.0-HBase-1.1-client.jar:lib/commons-io-2.0.1.jar:lib/common-core-5.1.jar:lib/harvester-core-5.1.jar:lib/derbyclient-10.12.1.1.jar:lib/archive-core-5.1.jar:lib/jwat-common-1.0.4.jar:lib/json-simple-1.1.1.jar dk.kb.webdanica.core.tools.CriteriaIngestTool $1 $2
