##bash ingestTool.sh $HARVESTLOG_FILE $CRITERIARESULTS_DIR $WORKFLOW_HOME $WEBDANICA_VERSION $NAS_VERSION
WORKFLOW_HOME=$3
WEBDANICA_VERSION=$4
NAS_VERSION=$5
SETTINGSFILE=$WORKFLOW_HOME/conf/webdanica_settings.xml 
OPTS2=-Dwebdanica.settings.file=$SETTINGSFILE
OPTS3=-Dlogback.configurationFile=$WORKFLOW_HOME/conf/silent_logback.xml 
MAILJAR1=lib/javax.mail-api-1.4.7.jar
MAILJAR2=lib/mail-1.4.7.jar

if [ ! -f "$SETTINGSFILE" ]; then
   echo "The SETTINGSFILE \'$SETTINGSFILE\' does not exist. Exiting program"
   exit 1
fi

java $OPTS2 $OPTS3 -cp lib/webdanica-core-$WEBDANICA_VERSION.jar:lib/phoenix-4.7.0-HBase-1.1-client.jar:lib/commons-io-2.0.1.jar:lib/common-core-$NAS_VERSION.jar:lib/harvester-core-$NAS_VERSION.jar:lib/archive-core-$NAS_VERSION.jar:lib/jwat-common-1.0.4.jar:lib/json-simple-1.1.1.jar:$MAILJAR1:$MAILJAR2 dk.kb.webdanica.core.tools.CriteriaIngestTool $1 $2

