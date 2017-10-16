# replace path in TOOLS_HOME with the correct full path
TOOLS_HOME=/REPLACE/WITH/CORRECT/FULL/PATH
ME=`basename $0`

if [ ! -d "$TOOLS_HOME" ]; then
  echo ERROR: The TOOLS_HOME \"$TOOLS_HOME\" does not exist. Please correct the path in $ME
  exit 1
fi
WEBDANICA_SETTINGSFILE=$TOOLS_HOME/conf/webdanica_settings.xml

if [ ! -f "$WEBDANICA_SETTINGSFILE" ]; then
 echo ERROR: The webdanica settingsfile \"$WEBDANICA_SETTINGSFILE\" does not exist. Please correct the path in $ME
 exit
fi

NAS_SETTINGSFILE=$TOOLS_HOME/conf/settings_NAS_Webdanica.xml 
if [ ! -f "$NAS_SETTINGSFILE" ]; then
 echo WARNING: The netarchivesuite settingsfile \"$NAS_SETTINGSFILE\" does not exist. You may want to correct the path in $ME
fi


OPTS1=-Ddk.netarkivet.settings.file=$NAS_SETTINGSFILE
OPTS2=-Dwebdanica.settings.file=$WEBDANICA_SETTINGSFILE
OPTS3=-Dlogback.configurationFile=$TOOLS_HOME/conf/silent_logback.xml 

NAS_VERSION=5.2.2
VERSION=2.0
PHOENIX_JAR=/usr/hdp/current/phoenix-client/phoenix-client.jar
WEBDANICA_JAR=lib/webdanica-core-$VERSION.jar

if [ ! -f "$WEBDANICA_JAR" ]; then
 echo ERROR: The webdanica jarfile \"$WEBDANICA_JAR\" does not exist. Please correct the path in this script \($ME\)
 exit
fi
if [ ! -f "$PHOENIX_JAR" ]; then
 echo ERROR: The phoenix jarfile \"$PHOENIX_JAR\" does not exist. Please correct the path in this script \($ME\)
 exit
fi

java $OPTS1 $OPTS2 $OPTS3 -cp $WEBDANICA_JAR:$PHOENIX_JAR:lib/commons-io-2.0.1.jar:lib/common-core-$NAS_VERSION.jar dk.kb.webdanica.core.tools.LoadBlacklists $1
