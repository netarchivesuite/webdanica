TIME=`date`
WEBDANICA_APP_STATUS_PAGE=narcana-webdanica01.statsbiblioteket.dk:8080/status/
NAS_APP_HISTORY_PAGE=narcana-webdanica01.statsbiblioteket.dk:8074/History/
wget -HEAD $WEBDANICA_APP_STATUS_PAGE -a /tmp/wgetlog
WEBDANICA_APP_CHECK=`echo $?`
wget -HEAD $NAS_APP_HISTORY_PAGE -a /tmp/wgetlog
NAS_APP_CHECK=`echo $?`
if [ $NAS_APP_CHECK -ne 0 ]; then
	echo "Page $NAS_APP_HISTORY_PAGE was down at $TIME!"	
fi
if [ $WEBDANICA_APP_CHECK -ne 0 ]; then
        echo "Page $WEBDANICA_APP_STATUS_PAGE was down at $TIME!" 
fi

