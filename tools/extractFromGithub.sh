BRANCHNAME=$1
if [[ -z $BRANCHNAME ]]; then
  echo "Forgot to name the wanted branch name"
  exit 1  
 	
fi
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

