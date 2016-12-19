
# arg 1: seed or file
# needs webdanica-core, common-core-5.1, harvester-core-5.1, JDBC driver for NAS harvestdatabase
SETTINGSFILE=conf/settings_WebdanicaClient.xml
SILENTLOGBACKFILE=conf/silent_logback.xml
OPTS1=-Ddk.netarkivet.settings.file=$SETTINGSFILE
OPTS2=-Dlogback.configurationFile=$SILENTLOGBACKFILE
WEBDANICAJAR=lib/webdanica-core-1.0.0.jar
DATABASEJAR=lib/postgresql-9.2-1003-jdbc4.jar
NAS_VERSION=5.1
NAS_COMMON_JAR=lib/common-core-$NAS_VERSION.jar
NAS_HARVESTER_JAR=lib/harvester-core-$NAS_VERSION.jar

java $OPTS1 $OPTS2 -cp $WEBDANICAJAR:$DATABASEJAR:$NAS_COMMON_JAR:$NAS_HARVESTER_JAR dk.kb.webdanica.core.tools.ImportIntoNetarchiveSuite $1
