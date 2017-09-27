# replace path in TOOLS_HOME with the correct full path
TOOLS_HOME=/REPLACE/WITH/CORRECT/FULL/PATH
ME=`basename $0`

if [ ! -d "$TOOLS_HOME" ]; then
  echo ERROR: The TOOLS_HOME \"$TOOLS_HOME\" does not exist. Please correct the path in $ME
  exit 1
fi
WEBDANICA_SETTINGSFILE=$TOOLS_HOME_HOME/conf/webdanica_settings.xml

if [ ! -f "$WEBDANICA_SETTINGSFILE" ]; then
 echo ERROR: Webdanica settingsfile \"$WEBDANICA_SETTINGSFILE\" does not exist. Please correct the path in $ME
 exit
fi

OPTS1=-Ddk.netarkivet.settings.file=$TOOLS_HOME/conf/settings_NAS_Webdanica.xml 
OPTS2=-Dwebdanica.settings.file=$WEBDANICA_SETTINGSFILE
OPTS3=-Dlogback.configurationFile=$TOOLS_HOME/conf/silent_logback.xml 

NAS_VERSION=5.2.2
VERSION=2.0
PHOENIX_JAR=lib/phoenix-4.7.0-HBase-1.1-client.jar
#PHOENIX_JAR=/usr/hdp/current/phoenix-client/phoenix-client.jar
WEBDANICA_JAR=lib/webdanica-core-$VERSION.jar

if [ ! -f "$WEBDANICA_JAR" ]; then
 echo ERROR: The webdanica jarfile \"$WEBDANICA_JAR\" does not exist. Please correct the path in this script \($ME\)
 exit
fi
if [ ! -f "$PHOENIX_JAR" ]; then
 echo ERROR: The phoenix jarfile \"$PHOENIX_JAR\" does not exist. Please correct the path in this script \($ME\)
 exit
fi

java $OPTS1 $OPTS2 $OPTS3 -cp $WEBDANICAJAR:$PHOENIXJAR:lib/commons-io-2.0.1.jar:lib/common-core-$NAS_VERSION.jar dk.kb.webdanica.core.tools.LoadBlacklists $1
