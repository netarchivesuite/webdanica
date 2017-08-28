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
if [ ! -f $L ]; then
   echo  "MISSING library '$L' in $PIGBOOTUP_FILE"
   exit 1	
fi

done 
