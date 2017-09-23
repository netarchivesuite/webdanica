# replace path in LOADSEEDS_HOME with the correct full path
#LOADSEEDS_HOME=/REPLACE/WITH/CORRECT/DIR/load-seeds-0.0.4
LOADSEEDS_HOME=/home/svc/test-apps/
NAS_SETTINGSFILE=$LOADSEEDS_HOME/conf/settings_NAS_Webdanica.xml
WEBDANICA_SETTINGSFILE=$LOADSEEDS_HOME/conf/webdanica_settings.xml
OPTS1=-Ddk.netarkivet.settings.file=$NAS_SETTINGSFILE

OPTS2=-Dwebdanica.settings.file=$WEBDANICA_SETTINGSFILE
OPTS3=-Dlogback.configurationFile=$LOADSEEDS_HOME/conf/silent_logback.xml 

if [ ! -f "$WEBDANICA_SETTINGSFILE" ]; then
 echo ERROR: Webdanica settingsfile \"$WEBDANICA_SETTINGSFILE\" does not exist. Please correct the path in this script
 exit
fi

#lib/phoenix-4.7.0-HBase-1.1-client.jar:lib/common-core-5.1.jar:lib/harvester-core-5.1.jar:lib/dom4j-1.6.1.jar:lib/jaxen-1.1.jar:lib/lucene-core-4.4.0.jar dk.kb.webdanica.tools.LoadTest $1 $resultsdir $2

java $OPTS1 $OPTS2 $OPTS3 -cp libs/webdanica-core-1.1-RC4.jar:libs/phoenix-4.7.0-HBase-1.1-client.jar:libs/dom4j-1.6.1.jar:libs/jaxen-1.1.jar:libs/lucene-core-4.4.0.jar:libs/commons-io-2.0.1.jar:libs/common-core-5.1.jar:libs/harvester-core-5.1.jar:libs/derbyclient-10.12.1.1.jar:libs/jwat-common-1.0.4.jar dk.kb.webdanica.core.tools.HarvestShowReports $1 $2
