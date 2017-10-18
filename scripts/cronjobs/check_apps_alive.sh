## checks if the two webdanica websites on port 8080 and 8074 are answering.
## argument: the HOST of the webdanica system, e.g. http://kb-test-webdanica-001.kb.dk
## Also checks that the 3 WEBDANICA apps are running on the server
## one dk.netarkivet.harvester.heritrix3.HarvestControllerApplication
## one dk.netarkivet.harvester.scheduler.HarvestJobManagerApplication
## one dk.netarkivet.common.webinterface.GUIApplication

ME=`basename $0`
HOST=$1
if [ -z "$HOST" ]; then
   echo ERROR no HOST argument to script $ME. Exiting program
   exit
fi

TIME=`date`
WEBDANICA_APP_STATUS_PAGE=$HOST:8080/status/
NAS_APP_HISTORY_PAGE=$HOST:8074/History/
wget -HEAD $WEBDANICA_APP_STATUS_PAGE -a /tmp/wgetlog --delete-after
WEBDANICA_APP_CHECK=`echo $?`
wget -HEAD $NAS_APP_HISTORY_PAGE -a /tmp/wgetlog --delete-after
NAS_APP_CHECK=`echo $?`
if [ $NAS_APP_CHECK -ne 0 ]; then
  echo "Page $NAS_APP_HISTORY_PAGE was down at $TIME!"	
fi
if [ $WEBDANICA_APP_CHECK -ne 0 ]; then
  echo "Page $WEBDANICA_APP_STATUS_PAGE was down at $TIME!" 
fi
HarvestControllerApplication_CLASS=dk.netarkivet.harvester.heritrix3.HarvestControllerApplication
HarvestJobManagerApplication_CLASS=dk.netarkivet.harvester.scheduler.HarvestJobManagerApplication
GUIApplication_CLASS=dk.netarkivet.common.webinterface.GUIApplication

FOUND1=`ps auxwwww | grep $HarvestControllerApplication_CLASS | grep java | wc -l`
FOUND2=`ps auxwwww | grep $HarvestJobManagerApplication_CLASS | grep java | wc -l`
FOUND3=`ps auxwwww | grep $GUIApplication_CLASS | grep java | wc -l`

if [ $FOUND1 != "1" ]; then
    echo The $HarvestControllerApplication_CLASS program is not running on $HOST at $TIME	
fi

if [ $FOUND2 != "1" ]; then 
    echo The $HarvestJobManagerApplication_CLASS program is not running on $HOST at $TIME	
fi

if [ $FOUND3 != "1" ]; then
    echo The $GUIApplication_CLASS program is not running on $HOST at $TIME	
fi
