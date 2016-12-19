# replace path in LOAD_SEEDS_HOME with the correct path
LOAD_SEEDS_HOME=/home/test/workflow

ME=`basename $0`
WEBDANICASETTINGS=$LOAD_SEEDS_HOME/conf/webdanica_settings.xml
OPTS1=-Dwebdanica.settings.file=$WEBDANICASETTINGS
OPTS2=-Dlogback.configurationFile=$LOAD_SEEDS_HOME/conf/silent_logback.xml 

echo Executing $ME using  webdanica settingsfile \"$WEBDANICASETTINGS\"
java  $OPTS1 $OPTS2 -cp lib/webdanica-core-1.0.0.jar:lib/slf4j-api-1.7.7.jar:lib/commons-io-2.0.1.jar:lib/common-core-5.1.jar:lib/harvester-core-5.1.jar:lib/derbyclient-10.12.1.1.jar:lib/jwat-common-1.0.4.jar:lib/guava-11.0.2.jar:lib/archive-core-5.1.jar:lib/phoenix-4.7.0-HBase-1.1-client.jar:lib/json-simple-1.1.1.jar dk.kb.webdanica.core.tools.LoadSeeds $1 $2
