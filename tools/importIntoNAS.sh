ME=`basename $0`
# arg 1: seed or file
# needs webdanica-core, common-core-5.2.2, harvester-core-5.2.2, JDBC driver for NAS harvestdatabase

SETTINGSFILE=conf/settings_WebdanicaClient.xml
if [ ! -f "$SETTINGSFILE" ]; then
 echo ERROR: The Netarchivesuite settingsfile \"$SETTINGSFILE\" does not exist. Please correct the path in $ME
 exit
fi

SILENTLOGBACKFILE=conf/silent_logback.xml
OPTS1=-Ddk.netarkivet.settings.file=$SETTINGSFILE
OPTS2=-Dlogback.configurationFile=$SILENTLOGBACKFILE

NAS_VERSION=5.2.2
VERSION=2.0

WEBDANICA_JAR=lib/webdanica-core-$VERSION.jar
if [ ! -f "$WEBDANICA_JAR" ]; then
 echo ERROR: The webdanica jarfile \"$WEBDANICA_JAR\" does not exist.  Maybe the lib folder is missing, or the webdanica VERSION is wrong. Exiting program $ME
 exit
fi

DATABASEJAR=lib/postgresql-9.2-1003-jdbc4.jar
if [ ! -f "$DATABASEJAR" ]; then
 echo ERROR: The database jarfile \"$DATABASEJAR\" does not exist. Maybe the lib folder is missing or path is wrong. Exiting program $ME
 exit
fi

NAS_COMMON_JAR=lib/common-core-$NAS_VERSION.jar
if [ ! -f "$NAS_COMMON_JAR" ]; then
 echo ERROR: The Netarchivesuite common-core jarfile \"$NAS_COMMON_JAR\" does not exist. Maybe the lib folder is missing, or the NAS_VERSION is wrong. Exiting program $ME
 exit
fi
NAS_HARVESTER_JAR=lib/harvester-core-$NAS_VERSION.jar
if [ ! -f "$NAS_HARVESTER_JAR" ]; then
 echo ERROR: The Netarchivesuite harvester-core \"$NAS_HARVESTER_JAR\" does not exist. Maybe the lib folder is missing, or the webdanica VERSION is wrong. Exiting program $ME
 exit
fi


java $OPTS1 $OPTS2 -cp $WEBDANICAJAR:$DATABASEJAR:$NAS_COMMON_JAR:$NAS_HARVESTER_JAR dk.kb.webdanica.core.tools.ImportIntoNetarchiveSuite $1
