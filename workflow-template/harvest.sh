# replace path in WORKFLOW_HOME with the correct full path
#WORKFLOW_HOME=/REPLACE/WITH/CORRECT/FULL/PATH
WORKFLOW_HOME=/home/test/workflow
ME=`basename $0`


if [ ! -d "$WORKFLOW_HOME" ]; then
  echo ERROR: The WORKFLOW_HOME \"$WORKFLOW_HOME\" does not exist. Please correct the path in $ME
  exit 1
fi

NAS_SETTINGSFILE=$WORKFLOW_HOME/conf/settings_NAS_Webdanica.xml
WEBDANICA_SETTINGSFILE=$WORKFLOW_HOME/conf/webdanica_settings.xml

if [ ! -f "$WEBDANICA_SETTINGSFILE" ]; then
 echo ERROR: Webdanica settingsfile \"$WEBDANICA_SETTINGSFILE\" does not exist. Please correct the path in $ME
 exit
fi

OPTS1=-Ddk.netarkivet.settings.file=$NAS_SETTINGSFILE
OPTS2=-Dwebdanica.settings.file=$WEBDANICA_SETTINGSFILE
OPTS3=-Dlogback.configurationFile=$WORKFLOW_HOME/conf/silent_logback.xml 

NAS_VERSION=5.2.2
VERSION=2.0

WEBDANICA_JAR=lib/webdanica-core-$VERSION.jar
if [ ! -f "$WEBDANICA_JAR" ]; then
 echo ERROR: The Webdanica-core.jar file \"$WEBDANICA_JAR\" does not exist. The version might be incorrect, or the lib folder is missing.
 exit
fi

echo Executing $ME using webdanica settingsfile \"$WEBDANICA_SETTINGSFILE\"


java $OPTS1 $OPTS2 $OPTS3 -cp $WEBDANICA_JAR:lib/slf4j-api-1.7.7.jar:lib/commons-io-2.0.1.jar:lib/common-core-$NAS_VERSION.jar:lib/harvester-core-$NAS_VERSION.jar:lib/derbyclient-10.12.1.1.jar:lib/jwat-common-1.0.4.jar:lib/guava-11.0.2.jar:lib/archive-core-$NAS_VERSION.jar:lib/postgresql-9.2-1003-jdbc4.jar dk.kb.webdanica.core.tools.Harvest $1
