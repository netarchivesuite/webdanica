export OLDJOBS=/home/test/WEBDANICA/oldjobs/
if [[ -d ${OLDJOBS} ]]; then	
  cd $OLDJOBS
  rm -rfv */**/lib
fi

