WORKFLOW_HOME=$1
ME=`basename $0`

if [ -z $WORKFLOW_HOME ]; then
   echo "Missing WORKFLOW_HOME argument to script $ME"
   exit 1;
fi
if [ ! -d $WORKFLOW_HOME ]; then
   echo "ERROR: The WORKFLOW_HOME '$WORKFLOW_HOME' is not properly set. Exiting script $ME"
   exit 1
fi

PIGBOOTUP_FILE=$WORKFLOW_HOME/conf/.pigbootup

if [ ! -f $PIGBOOTUP_FILE ]; then
   echo "ERROR: The pig-bootup file '$PIGBOOTUP_FILE' does not exist." 
   exit 1
fi


LIBS=`grep REGISTER $PIGBOOTUP_FILE | grep -v "\-\-" | cut -d ' ' -f2`
for L in $LIBS
do
if [ ! -f $WORKFLOW_HOME/$L ]; then
   echo  "MISSING library '$L' in $PIGBOOTUP_FILE"
   exit 1	
fi

done 

METHODS=`grep DEFINE $PIGBOOTUP_FILE  | grep -v "\-\-" | awk '$1=$1' | cut -d ' ' -f3 | tr -d '();'`

let FAILURE=0
for M in $METHODS
do
# check if  M is part of the libraries in the LIBS list
let FOUND=0
for L in $LIBS 
do  
  RES=`grep $M $WORKFLOW_HOME/$L`
  if [ "$RES" != "" ];
  then 
     let FOUND=1
     #echo "Found method $M in library $L"
  fi
done
if [ $FOUND -eq 0 ];
then
  echo "FAILURE to find method '$M' in the registered libraries."
  let FAILURE=1
fi
done
if [ "$FAILURE" -eq 1 ];
then 
 echo "Missing method definitions in the registered libraries '$LIBS'"
 echo "Please REGISTER the missing library/libraries"
 exit 1
else 
 exit 0
fi
  
 	
