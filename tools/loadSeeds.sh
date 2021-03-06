# replace path in TOOLS_HOME with the correct full path
TOOLS_HOME=/REPLACE/WITH/CORRECT/FULL/PATH
ME=`basename $0`

if [ ! -d "$TOOLS_HOME" ]; then
  echo ERROR: The TOOLS_HOME \"$TOOLS_HOME\" does not exist. Please correct the path in $ME
  exit 1
fi

NAS_SETTINGSFILE=$TOOLS_HOME/conf/settings_NAS_Webdanica.xml
WEBDANICA_SETTINGSFILE=$TOOLS_HOME/conf/webdanica_settings.xml

OPTS1=-Ddk.netarkivet.settings.file=$NAS_SETTINGSFILE
OPTS2=-Dwebdanica.settings.file=$WEBDANICA_SETTINGSFILE
OPTS3=-Dlogback.configurationFile=$TOOLS_HOME/conf/silent_logback.xml 

if [ ! -f "$WEBDANICA_SETTINGSFILE" ]; then
 echo ERROR: Webdanica settingsfile \"$WEBDANICA_SETTINGSFILE\" does not exist. Please correct the path in $ME
 exit
fi

if [ ! -f "$NAS_SETTINGSFILE" ]; then
 echo WARNING: the netarchivesuite settingsfile \"$NAS_SETTINGSFILE\" does not exist. You may want to correct the path in $ME
fi

NAS_VERSION=5.2.2
VERSION=2.0

PHOENIX_JAR=/usr/hdp/current/phoenix-client/phoenix-client.jar
if [ ! -f "$PHOENIX_JAR" ]; then
 echo ERROR: The phoenix-client-jar file \"$PHOENIX_JAR\" does not exist. 
 exit
fi

WEBDANICA_JAR=lib/webdanica-core-$VERSION.jar

if [ ! -f "$WEBDANICA_JAR" ]; then
 echo ERROR: The Webdanica-core.jar file \"$WEBDANICA_JAR\" does not exist. The version might be incorrect, or the lib folder is missing.
 exit
fi

echo Executing $ME using webdanica settingsfile \"$WEBDANICA_SETTINGSFILE\"

java $OPTS1 $OPTS2 $OPTS3 -cp $WEBDANICA_JAR:$PHOENIX_JAR:lib/dom4j-1.6.1.jar:lib/jaxen-1.1.jar:lib/lucene-core-4.4.0.jar:lib/commons-io-2.0.1.jar:lib/common-core-${NAS_VERSION}.jar dk.kb.webdanica.core.tools.LoadSeeds $1 $2 $3
