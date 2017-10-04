NAS_INSTALL=/home/test/WEBDANICA
OLDJOBS=$NAS_INSTALL/oldjobs
ME=`basename $0`

if [ ! -d "$NAS_INSTALL" ]; then
  echo ERROR: The netarchivesuite installdir \"$NAS_INSTALL\" does not exist. Please correct the path in $ME
  exit 1
fi

if [[ -d ${OLDJOBS} ]]; then	
  cd $OLDJOBS
  rm -rfv */**/lib
fi

