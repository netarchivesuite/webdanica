## checks if the two webdanica websites on port 8080 and 8074 are answering.
## argument: the HOST of the webdanica system, e.g. http://kb-test-webdanica-001.kb.dk
## Also checks that the 3 WEBDANICA apps are running on the server
NAS_APPS_COUNT_REQUIRED=3
NAS_ENV=WEBDANICA

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
## Finally, check the number of NAS WEBDANICA apps on the machine
NAS_APPS_COUNT_FOUND=`ps auxwwww | grep $NAS_ENV | grep java | wc -l`

if [ $NAS_APPS_COUNT_FOUND != $NAS_APPS_COUNT_REQUIRED ]; then
        echo "Only found $NAS_APPS_COUNT_FOUND NetarchiveSuite apps, required is $NAS_APPS_COUNT_REQUIRED"
fi
