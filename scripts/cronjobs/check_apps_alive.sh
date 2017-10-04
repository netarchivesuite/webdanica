## checks if the two webdanica websites on port 8080 and 8074 are answering.
## argument: the HOST of the webdanica system, e.g. http://kb-test-webdanica-001.kb.dk
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

