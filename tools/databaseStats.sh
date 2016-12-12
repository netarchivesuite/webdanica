# replace path in LOADSEEDS_HOME with the correct path
LOADSEEDS_HOME=/home/test/workflow
WEBDANICASETTINGS=$LOADSEEDS_HOME/conf/webdanica_settings.xml
#OPTS1=-Ddk.netarkivet.settings.file=$LOADSEEDS_HOME/config/settings_NAS_Webdanica.xml 
OPTS2=-Dwebdanica.settings.file=$WEBDANICASETTINGS
OPTS3=-Dlogback.configurationFile=$LOADSEEDS_HOME/conf/silent_logback.xml 

ME=`basename "$0"`
if [ ! -f "$WEBDANICASETTINGS" ]; then
 echo ERROR: Webdanica settingsfile \"$WEBDANICASETTINGS\" does not exist. Please correct the path in this script \($ME\)
 exit
fi

WEBDANICAJAR=lib/webdanica-core-0.4.0-SNAPSHOT.jar
PHOENIXJAR=lib/phoenix-4.7.0-HBase-1.1-client.jar

if [ ! -f "$WEBDANICAJAR" ]; then
 echo ERROR: The webdanica jarfile \"$WEBDANICAJAR\" does not exist. Please correct the path in this script \($ME\)
 exit
fi
if [ ! -f "$PHOENIXJAR" ]; then
 echo ERROR: The phoenix jarfile \"$PHOENIXJAR\" does not exist. Please correct the path in this script \($ME\)
 exit
fi


java $OPTS2 $OPTS3 -cp $WEBDANICAJAR:$PHOENIXJAR:lib/common-core-5.1.jar:lib/harvester-core-5.1.jar:lib/dom4j-1.6.1.jar:lib/jaxen-1.1.jar:lib/lucene-core-4.4.0.jar dk.kb.webdanica.core.tools.ComputeStats
