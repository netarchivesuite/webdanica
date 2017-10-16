# replace path in TOOLS_HOME with the correct full path
TOOLS_HOME=/REPLACE/WITH/CORRECT/FULL/PATH
ME=`basename $0`

if [ ! -d "$TOOLS_HOME" ]; then
  echo ERROR: The TOOLS_HOME \"$TOOLS_HOME\" does not exist. Please correct the path in $ME
  exit 1
fi

WEBDANICASETTINGS=$TOOLS_HOME/conf/webdanica_settings.xml
OPTS1=-Ddk.netarkivet.settings.file=$TOOLS_HOME/config/settings_NAS_Webdanica.xml 
OPTS2=-Dwebdanica.settings.file=$WEBDANICASETTINGS
OPTS3=-Dlogback.configurationFile=$TOOLS_HOME/conf/silent_logback.xml 

if [ ! -f "$WEBDANICASETTINGS" ]; then
 echo ERROR: Webdanica settingsfile \"$WEBDANICASETTINGS\" does not exist. Please correct the path in this script \($ME\)
 exit
fi

NAS_VERSION=5.2.2
VERSION=2.0
PHOENIX_JAR=/usr/hdp/current/phoenix-client/phoenix-client.jar
WEBDANICA_JAR=lib/webdanica-core-$VERSION.jar

if [ ! -f "$WEBDANICA_JAR" ]; then
 echo ERROR: The webdanica jarfile \"$WEBDANICA_JAR\" does not exist. Maybe the lib folder is missing, or the webdanica VERSION is wrong
 exit
fi

if [ ! -f "$PHOENIX_JAR" ]; then
 echo ERROR: The phoenix jarfile \"$PHOENIX_JAR\" does not exist.
 exit
fi

java $OPTS1 $OPTS2 $OPTS3 -cp $WEBDANICA_JAR:$PHOENIX_JAR:lib/common-core-$NAS_VERSION.jar:lib/harvester-core-$NAS_VERSION.jar:lib/dom4j-1.6.1.jar:lib/jaxen-1.1.jar:lib/lucene-core-4.4.0.jar dk.kb.webdanica.core.tools.ComputeStats
