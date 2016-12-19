# replace path in LOADBLACKLISTS_HOME with the correct path
LOADBLACKLISTS_HOME=/home/test/workflow
WEBDANICA_SETTINGSFILE=$LOADBLACKLISTS_HOME/conf/webdanica_settings.xml
OPTS1=-Ddk.netarkivet.settings.file=$LOADBLACKLISTS_HOME/conf/settings_NAS_Webdanica.xml 
OPTS2=-Dwebdanica.settings.file=$WEBDANICA_SETTINGSFILE
OPTS3=-Dlogback.configurationFile=$LOADBLACKLISTS_HOME/conf/silent_logback.xml 
ME=`basename "$0"`
if [ ! -f "$WEBDANICA_SETTINGSFILE" ]; then
 echo ERROR: Webdanica settingsfile \"$WEBDANICA_SETTINGSFILE\" does not exist. Please correct the path in this script \($ME\)
 exit
fi

WEBDANICAJAR=lib/webdanica-core-1.0.0.jar
PHOENIXJAR=lib/phoenix-4.7.0-HBase-1.1-client.jar

if [ ! -f "$WEBDANICAJAR" ]; then
 echo ERROR: The webdanica jarfile \"$WEBDANICAJAR\" does not exist. Please correct the path in this script \($ME\)
 exit
fi
if [ ! -f "$PHOENIXJAR" ]; then
 echo ERROR: The phoenix jarfile \"$PHOENIXJAR\" does not exist. Please correct the path in this script \($ME\)
 exit
fi



java $OPTS1 $OPTS2 $OPTS3 -cp $WEBDANICAJAR:$PHOENIXJAR:lib/commons-io-2.0.1.jar:lib/common-core-5.1.jar dk.kb.webdanica.core.tools.LoadBlacklists $1
