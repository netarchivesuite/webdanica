# replace path in TOOLS_HOME with the correct full path
TOOLS_HOME=/REPLACE/WITH/CORRECT/FULL/PATH
ME=`basename $0`

if [ ! -d "$TOOLS_HOME" ]; then
  echo ERROR: The TOOLS_HOME \"$TOOLS_HOME\" does not exist. Please correct the path in $ME
  exit 1
fi

SETTINGSFILE=$TOOLS_HOME/conf/webdanica_settings.xml 
NAS_SETTINGSFILE=$TOOLS_HOME/conf/settings_NAS_Webdanica.xml
OPTS2=-Dwebdanica.settings.file=$SETTINGSFILE
OPTS3=-Dlogback.configurationFile=$TOOLS_HOME/conf/silent_logback.xml 

if [ ! -f "$SETTINGSFILE" ]; then
   echo "ERROR: The webdanica settingsfile \'$SETTINGSFILE\' does not exist. Exiting program $ME"
   exit 1
fi

if [ ! -f "$NAS_SETTINGSFILE" ]; then
   echo "WARNING: The netarchivesuite settingsfile \'$NAS_SETTINGSFILE\' does not exist. You may want to correct this in script $ME"
fi


NAS_VERSION=5.2.2
VERSION=2.0
PHOENIX_JAR=/usr/hdp/current/phoenix-client/phoenix-client.jar
WEBDANICA_JAR=lib/webdanica-core-$VERSION.jar

if [ ! -f "$WEBDANICA_JAR" ]; then
   echo "ERROR: The WEBDANICA_JAR \'$WEBDANICA_JAR\' does not exist. Maybe the lib folder is missing, or the webdanica VERSION is wrong. Exiting program $ME"
   exit 1
fi

if [ ! -f "$PHOENIX_JAR" ]; then
   echo "ERROR: The PHOENIX_JAR \'$PHOENIX_JAR\' does not exist. Maybe the lib folder is missing, or the webdanica VERSION is wrong. Exiting program $ME"
   exit 1
fi

java $OPTS1 $OPTS2 $OPTS3 -cp $WEBDANICA_JAR:$PHOENIX_JAR:lib/common-core-$NAS_VERSION.jar:lib/harvester-core-$NAS_VERSION.jar dk.kb.webdanica.core.tools.ExportFromWebdanica --list_already_exported

