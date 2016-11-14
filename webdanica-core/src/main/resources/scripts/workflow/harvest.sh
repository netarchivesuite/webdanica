# replace path in HARVEST_SEEDS_HOME with the correct path
HARVEST_SEEDS_HOME=/home/test/workflow
OPTS1=-Ddk.netarkivet.settings.file=$HARVEST_SEEDS_HOME/conf/settings_NAS_Webdanica_staging.xml 
OPTS2=-Dwebdanica.settings.file=$HARVEST_SEEDS_HOME/conf/webdanica_settings.xml 
OPTS3=-Dlogback.configurationFile=$HARVEST_SEEDS_HOME/conf/silent_logback.xml 

#echo `which java`

java $OPTS1 $OPTS2 $OPTS3 -cp lib/webdanica-core-0.3.0-SNAPSHOT.jar:lib/slf4j-api-1.7.7.jar:lib/commons-io-2.0.1.jar:lib/common-core-5.1.jar:lib/harvester-core-5.1.jar:lib/derbyclient-10.12.1.1.jar:lib/archive-core-5.1.jar dk.kb.webdanica.core.tools.Harvest $1
