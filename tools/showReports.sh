# replace path in TOOLS_HOME with the correct full path
TOOLS_HOME=/REPLACE/WITH/CORRECT/FULL/PATH
ME=`basename $0`
if [ ! -d "$TOOLS_HOME" ]; then
  echo ERROR: The TOOLS_HOME \"$TOOLS_HOME\" does not exist. Please correct the path in $ME
  exit 1
fi

NAS_SETTINGSFILE=$TOOLS_HOME/conf/settings_NAS_Webdanica.xml
WEBDANICA_SETTINGSFILE=$TOOLS_HOME/conf/webdanica_settings.xml

if [ ! -f "$WEBDANICA_SETTINGSFILE" ]; then
 echo ERROR: Webdanica settingsfile \"$WEBDANICA_SETTINGSFILE\" does not exist. Please correct the path in $ME
 exit
fi

if [ ! -f "$NAS_SETTINGSFILE" ]; then
 echo ERROR: The netarchivesuite settingsfile \"$NAS_SETTINGSFILE\" does not exist. Please correct the path in $ME
 exit
fi

OPTS1=-Ddk.netarkivet.settings.file=$NAS_SETTINGSFILE
OPTS2=-Dwebdanica.settings.file=$WEBDANICA_SETTINGSFILE
OPTS3=-Dlogback.configurationFile=$TOOLS_HOME/conf/silent_logback.xml 

NAS_VERSION=5.2.2
VERSION=2.0
PHOENIX_CLIENT_JAR=lib/phoenix-4.7.0-HBase-1.1-client.jar
#PHOENIX_CLIENT_JAR=/usr/hdp/current/phoenix-client/phoenix-client.jar

WEBDANICA_JAR=lib/webdanica-core-$VERSION.jar

if [ ! -f "$WEBDANICA_JAR" ]; then
 echo ERROR: The webdanica jarfile \"$WEBDANICA_JAR\" does not exist. The version might be incorrect, or the lib folder is missing. Exiting program $ME
 exit
fi

java $OPTS1 $OPTS2 $OPTS3 -cp $WEBDANICA_JAR:$PHOENIX_CLIENT_JAR:lib/guava-11.0.2.jar:lib/dom4j-1.6.1.jar:lib/jaxen-1.1.jar:lib/lucene-core-4.4.0.jar:lib/commons-io-2.0.1.jar:lib/common-core-$NAS_VERSION.jar:lib/harvester-core-$NAS_VERSION.jar:lib/postgresql-9.2-1003-jdbc4.jar:lib/derbyclient-10.12.1.1.jar:lib/jwat-common-1.0.4.jar dk.kb.webdanica.core.tools.HarvestShowReports $1 $2

