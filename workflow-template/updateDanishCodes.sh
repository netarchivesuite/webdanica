# replace path in HARVEST_SEEDS_HOME with the correct path
HARVEST_SEEDS_HOME=/home/test/automatic-workflow
OPTS1=-Ddk.netarkivet.settings.file=$HARVEST_SEEDS_HOME/conf/settings_NAS_Webdanica_staging.xml 
OPTS2=-Dwebdanica.settings.file=$HARVEST_SEEDS_HOME/conf/webdanica_settings.xml 
OPTS3=-Dlogback.configurationFile=$HARVEST_SEEDS_HOME/conf/silent_logback.xml 

#echo `which java`

java $OPTS1 $OPTS2 $OPTS3 -cp lib/webdanica-core-1.0.0.jar:lib/phoenix-4.7.0-HBase-1.1-client.jar:lib/commons-io-2.0.1.jar:lib/common-core-5.1.jar:lib/harvester-core-5.1.jar:lib/derbyclient-10.12.1.1.jar:lib/archive-core-5.1.jar:lib/jwat-common-1.0.4.jar:lib/json-simple-1.1.1.jar:lib/log4j-1.2.17.jar:lib/slf4j-log4j12-1.7.12.jar:lib/commons-lang-2.3.jar dk.kb.webdanica.core.tools.UpdateDanishCodes
