# replace path in LOADSEEDS_HOME with the correct full path
LOADSEEDS_HOME=/REPLACE/WITH/CORRECT/DIR
ME=`basename $0`
NAS_SETTINGSFILE=$LOADSEEDS_HOME/conf/settings_NAS_Webdanica.xml
WEBDANICA_SETTINGSFILE=$LOADSEEDS_HOME/conf/webdanica_settings.xml

OPTS1=-Ddk.netarkivet.settings.file=$NAS_SETTINGSFILE
OPTS2=-Dwebdanica.settings.file=$WEBDANICA_SETTINGSFILE
#OPTS3=-Dlogback.configurationFile=$LOADSEEDS_HOME/conf/silent_logback.xml 

if [ ! -f "$WEBDANICA_SETTINGSFILE" ]; then
 echo ERROR: Webdanica settingsfile \"$WEBDANICA_SETTINGSFILE\" does not exist. Please correct the path in this script
 exit
fi


PHOENIX_CLIENT_JAR=/usr/hdp/current/phoenix-client/phoenix-client.jar
#PHOENIX_CLIENT_JAR=lib/phoenix-4.7.0-HBase-1.1-client.jar

echo Executing $ME using webdanica settingsfile \"$WEBDANICASETTINGS\"

java $OPTS1 $OPTS2 $OPTS3 -cp lib/webdanica-core-1.1-RC6.jar:$PHOENIX_CLIENT_JAR:lib/dom4j-1.6.1.jar:lib/jaxen-1.1.jar:lib/lucene-core-4.4.0.jar:lib/commons-io-2.0.1.jar:lib/common-core-5.1.jar dk.kb.webdanica.core.tools.LoadSeeds $1 $2
