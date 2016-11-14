# replace path in LOADSEEDS_HOME with the correct path
LOADSEEDS_HOME=/home/test/workflow
NAS_SETTINGSFILE=$LOADSEEDS_HOME/conf/settings_NAS_Webdanica.xml
WEBDANICA_SETTINGSFILE=$LOADSEEDS_HOME/conf/webdanica_settings.xml
LOGBACK_CONF_FILE=$LOADSEEDS_HOME/conf/silent_logback.xml

OPTS1=-Ddk.netarkivet.settings.file=$NAS_SETTINGSFILE
OPTS2=-Dwebdanica.settings.file=$WEBDANICA_SETTINGSFILE
OPTS3=-Dlogback.configurationFile=$LOGBACK_CONF_FILE


if [ ! -f "$WEBDANICA_SETTINGSFILE" ]; then
 echo ERROR: Webdanica settingsfile \"$WEBDANICA_SETTINGSFILE\" does not exist. Please correct the path
 exit
fi

java $OPTS1 $OPTS2 $OPTS3 -cp lib/webdanica-core-0.2.0-SNAPSHOT.jar:lib/phoenix-4.7.0-HBase-1.1-client.jar:lib/common-core-5.1.jar:lib/harvester-core-5.1.jar:lib/dom4j-1.6.1.jar:lib/jaxen-1.1.jar:lib/lucene-core-4.4.0.jar dk.kb.webdanica.tools.CheckListFileFormat $1
