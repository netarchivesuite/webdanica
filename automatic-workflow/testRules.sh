# look for existence of WORKFLOW_HOME/lib/webdanica-core-$WEBDANICA_VERSION.jar
WORKFLOW_HOME=/home/test/automatic-workflow
CONFDIR=$WORKFLOW_HOME/conf
JARFILE=$WORKFLOW_HOME/lib/webdanica-core-0.4.0-SNAPSHOT.jar
if [ ! -f "$JARFILE" ]; then
   echo "The required jarfile '$JARFILE' does not exist. Exiting program $PROG"
   exit 1
fi
OPTS2=-Dwebdanica.settings.file=$CONFDIR/webdanica_settings.xml 
OPTS3=-Dlogback.configurationFile=$CONFDIR/silent_logback.xml 

java $OPTS2 $OPTS3 -cp $JARFILE:lib/slf4j-api-1.7.7.jar:lib/commons-io-2.0.1.jar:lib/common-core-5.1.jar:lib/harvester-core-5.1.jar:lib/derbyclient-10.12.1.1.jar:lib/archive-core-5.1.jar:lib/phoenix-4.7.0-HBase-1.1-client.jar:lib/json-simple-1.1.1.jar:lib/jwat-common-1.0.4.jar dk.kb.webdanica.core.tools.TestRules $1
