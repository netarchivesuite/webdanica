# replace path in TOOLS_HOME with the correct full path
TOOLS_HOME=/REPLACE/WITH/CORRECT/FULL/PATH
NAS_SETTINGSFILE=$TOOLS_HOME/conf/settings_NAS_Webdanica.xml
WEBDANICA_SETTINGSFILE=$TOOLS_HOME/conf/webdanica_settings.xml
OPTS1=-Ddk.netarkivet.settings.file=$NAS_SETTINGSFILE

OPTS2=-Dwebdanica.settings.file=$WEBDANICA_SETTINGSFILE
OPTS3=-Dlogback.configurationFile=$TOOLS_HOME/conf/silent_logback.xml 

if [ ! -f "$WEBDANICA_SETTINGSFILE" ]; then
 echo ERROR: Webdanica settingsfile \"$WEBDANICA_SETTINGSFILE\" does not exist. Please correct the path in this script
 exit
fi
NAS_VERSION=5.2.2
VERSION=2.0
PHOENIX_JAR=lib/phoenix-4.7.0-HBase-1.1-client.jar

java $OPTS1 $OPTS2 $OPTS3 -cp libs/webdanica-core-$VERSION.jar:$PHOENIX_JAR:libs/dom4j-1.6.1.jar:libs/jaxen-1.1.jar:libs/lucene-core-4.4.0.jar:libs/commons-io-2.0.1.jar:libs/common-core-${NAS_VERSION}.jar dk.kb.webdanica.core.datamodel.Cache
