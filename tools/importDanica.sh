# replace path in TOOLS_HOME with the correct full path
TOOLS_HOME=/REPLACE/WITH/CORRECT/FULL/PATH
ME=`basename $0`

if [ ! -d "$TOOLS_HOME" ]; then
  echo ERROR: The TOOLS_HOME \"$TOOLS_HOME\" does not exist. Please correct the path in $ME
  exit 1
fi

WEBDANICASETTINGS=$TOOLS_HOME/conf/webdanica_settings.xml

if [ ! -f "$WEBDANICA_SETTINGSFILE" ]; then
 echo ERROR: Webdanica settingsfile \"$WEBDANICA_SETTINGSFILE\" does not exist. Please correct the path in $ME
 exit
fi

OPTS1=-Dwebdanica.settings.file=$WEBDANICASETTINGS
OPTS2=-Dlogback.configurationFile=$LOAD_SEEDS_HOME/conf/silent_logback.xml 

NAS_VERSION=5.2.2
VERSION=2.0
PHOENIX_JAR=lib/phoenix-4.7.0-HBase-1.1-client.jar
#PHOENIX_JAR=/usr/hdp/current/phoenix-client/phoenix-client.jar

WEBDANICA_JAR=lib/webdanica-core-$VERSION.jar

if [ ! -f "$WEBDANICA_JAR" ]; then
 echo ERROR: The Webdanica-core.jar file \"$WEBDANICA_JAR\" does not exist. The version might be incorrect, or the lib folder is missing. Exiting program $ME
 exit
fi

echo Executing $ME using  webdanica settingsfile \"$WEBDANICASETTINGS\"

java  $OPTS1 $OPTS2 -cp $WEBDANICA_JAR:$PHOENIX_JAR:lib/slf4j-api-1.7.7.jar:lib/commons-io-2.0.1.jar:lib/common-core-$NAS_VERSION.jar:lib/harvester-core-$NAS_VERSION.jar:lib/derbyclient-10.12.1.1.jar:lib/jwat-common-1.0.4.jar:lib/guava-11.0.2.jar:lib/archive-core-$NAS_VERSION.jar:lib/json-simple-1.1.1.jar dk.kb.webdanica.core.tools.LoadSeeds $1 --accept
