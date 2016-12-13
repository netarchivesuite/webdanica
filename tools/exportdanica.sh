WORKFLOW_HOME=/home/test/workflow/
SETTINGSFILE=$WORKFLOW_HOME/conf/webdanica_settings.xml 
OPTS2=-Dwebdanica.settings.file=$SETTINGSFILE
OPTS3=-Dlogback.configurationFile=$WORKFLOW_HOME/conf/silent_logback.xml 

if [ ! -f "$SETTINGSFILE" ]; then
   echo "The SETTINGSFILE \'$SETTINGSFILE\' does not exist. Exiting program"
   exit 1
fi

WEBDANICAJAR=lib/webdanica-core-0.4.0-SNAPSHOT.jar
PHOENIXJAR=lib/phoenix-4.7.0-HBase-1.1-client.jar

if [ ! -f "$WEBDANICAJAR" ]; then
   echo "The WEBDANICAJAR \'$WEBDANICAJAR\' does not exist. Exiting program"
   exit 1
fi


if [ ! -f "$PHOENIXJAR" ]; then
   echo "The PHOENIXJAR \'$PHOENIXJAR\' does not exist. Exiting program"
   exit 1
fi

java $OPTS2 $OPTS3 -cp $WEBDANICAJAR:$PHOENIXJAR:lib/common-core-5.1.jar:lib/harvester-core-5.1.jar dk.kb.webdanica.core.tools.ExportFromWebdanica --list_already_exported

