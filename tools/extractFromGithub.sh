BRANCHNAME=master
DATE=`/bin/date '+%d-%m-%Y'`
EXPORTDIR=$BRANCHNAME-$DATE
URL=https://github.com/netarchivesuite/webdanica/archive/$BRANCHNAME.zip
if [[ -f $BRANCHNAME.zip ]]; then
	echo Deleting old download of $BRANCHNAME.zip
	rm $BRANCHNAME.zip
fi

if [[ ! -d $EXPORTDIR ]]; then
wget $URL
unzip -q $BRANCHNAME.zip -d $EXPORTDIR
else 
 echo "Using existing download today of $URL"	
fi

