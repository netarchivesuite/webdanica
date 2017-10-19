# replace path in TOOLS_HOME with the correct path
TOOLS_HOME=/home/svc/devel/webdanica/tools
ME=`basename $0`
if [ ! -d "$TOOLS_HOME" ]; then
  echo ERROR: The TOOLS_HOME \"$TOOLS_HOME\" does not exist. Please correct the path in $ME
  exit 1
fi

NAS_SETTINGSFILE=$TOOLS_HOME/conf/settings_NAS_Webdanica.xml
WEBDANICA_SETTINGSFILE=$TOOLS_HOME/conf/webdanica_settings.xml
LOGBACK_CONF_FILE=$TOOLS_HOME/conf/silent_logback.xml

OPTS1=-Ddk.netarkivet.settings.file=$NAS_SETTINGSFILE
OPTS2=-Dwebdanica.settings.file=$WEBDANICA_SETTINGSFILE
OPTS3=-Dlogback.configurationFile=$LOGBACK_CONF_FILE


if [ ! -f "$WEBDANICA_SETTINGSFILE" ]; then
 echo ERROR: Webdanica settingsfile \"$WEBDANICA_SETTINGSFILE\" does not exist. Please correct the path
 exit
fi

NAS_VERSION=5.2.2
VERSION=2.0
WEBDANICA_JAR=lib/webdanica-core-$VERSION.jar

if [ ! -f "$WEBDANICA_JAR" ]; then
 echo ERROR: The Webdanica-core.jar file \"$WEBDANICA_JAR\" does not exist. The version might be incorrect, or the lib folder is missing. Exiting program $ME
 exit
fi

#java $OPTS1 $OPTS2 $OPTS3 -cp lib/webdanica-core-$VERSION.jar:lib/common-core-$NAS_VERSION.jar:lib/harvester-core-$NAS_VERSION.jar:lib/dom4j-1.6.1.jar:lib/jaxen-1.1.jar:lib/lucene-core-4.4.0.jar dk.kb.webdanica.core.tools.CheckListFileFormat $1
java $OPTS1 $OPTS2 $OPTS3 -cp lib/webdanica-core-$VERSION.jar:lib/common-core-$NAS_VERSION.jar dk.kb.webdanica.core.tools.CheckListFileFormat $1
