# Installation and configuration of the automatic workflow

## What is the automatic-workflow 

The automatic workflow takes care of the analysis of the harvested files on the basis of harvestlogs written by the webapp to a common
directory. This directory is defined by the webdanica setting settings.harvesting.harvestlogDir (e.g. /home/harvestLogs).
One script executes the entire automatic-workflow, i.e. the 'webdanica-analysis-cron.sh'. There is also a 'webdanica-analysis-manual.sh script, that
takes one harvestlog, and does the analysis on that harvestlog alone.

Both these scripts both include a common file 'setenv.sh' which must be configured correctly before enabling the harvesting workflow and the cronjobs: 
``` 
WORKFLOW_USER_HOME=/home/test
WEBDANICA_VERSION=2.1
WORKFLOW_HOME=$WORKFLOW_USER_HOME/automatic-workflow
WEBDATADIR=$WORKFLOW_USER_HOME/ARKIV
HADOOP_HOME=$WORKFLOW_USER_HOME/hadoop-1.2.1/
PIG_HOME=$WORKFLOW_USER_HOME/pig-0.16.0/
JAVA_HOME=/usr/java/jdk1.8.0_92_x64
NAS_VERSION=5.4
## the below settings should not be altered
BUSYFILE=$WORKFLOW_HOME/.busy
WORKDIR=$WORKFLOW_HOME/working
OLDJOBSDIR=$WORKFLOW_HOME/oldjobs
PATH=$JAVA_HOME/bin:$PATH
FINDLOGS_SCRIPT=${WORKFLOW_HOME}/findharvestlogs.sh
AUTOMATIC_SCRIPT=${WORKFLOW_HOME}/automatic.sh
export WORKFLOW_HOME WEBDATADIR WEBDANICA_VERSION HADOOP_HOME PIG_HOME BUSYFILE WORKDIR OLDJOBSDIR JAVA_HOME PATH FINDLOGS_SCRIPT AUTOMATIC_SCRIPT NAS_VERSION
```
The important settings to verify are the following
 * WEBDATADIR - represents the location of the folder with the harvested data, where the Netarchivesuite stores its harvestdata is stored (e.g. /home/test/ARKIV)
 * WEBDANICA_VERSION - the version of the webdanica release being used.
 * NAS_VERSION - the version of Netarchivesuite being used, currently 5.2.2
 * JAVA_HOME - the version of java being used by the workflow.

## Downloading and installing hadoop-1.2.1 and pig-0.16.0

Apache hadoop (version 1.2.1) and Apache pig (version 0.16.0) are required by the automatic-workflow.</br>.
They must be downloaded and unpacked into the WORKFLOW_USER_HOME like this:
```
cd $WORKFLOW_USER_HOME
wget http://archive.apache.org/dist/hadoop/core/hadoop-1.2.1/hadoop-1.2.1.tar.gz
wget http://ftp.download-by.net/apache/pig/pig-0.16.0/
tar xfz hadoop-1.2.1.tar.gz
tar xfz pig-0.16.0.tar.gz
```

Note that we want to use the hadoop embedded with pig, not any external hadoop installation. So, if hadoop is in the path (`which hadoop` gives a positive result), you need to 
adapt the `pig-0.16.0/bin/pig` script like shown in this diff:
```
 #    done
 #fi
 
-if which hadoop >/dev/null; then
-    HADOOP_BIN=`which hadoop`
-fi
+## SVC: uncomment if which hadoop
+#if which hadoop >/dev/null; then
+#    HADOOP_BIN=`which hadoop`
+#fi
 
 if [[ -z "$HADOOP_BIN" && -n "$HADOOP_PREFIX" ]]; then
     if [ -f $HADOOP_PREFIX/bin/hadoop ]; then
@@ -278,12 +279,13 @@
     fi
 fi
 
-if [ -z "$HADOOP_BIN" ]; then
-    # if installed with rpm/deb package
-    if [ -f /usr/bin/hadoop ]; then
-        HADOOP_BIN=/usr/bin/hadoop
-    fi
-fi
+## SVC: Disabled if 
+#if [ -z "$HADOOP_BIN" ]; then
+#    # if installed with rpm/deb package
+#    if [ -f /usr/bin/hadoop ]; then
+#       HADOOP_BIN=/usr/bin/hadoop
+#    fi
+#fi
 
 # find out the HADOOP_HOME in order to find hadoop jar
 # we use the name of hadoop jar to decide if user is using
```
Thus tricking it into not finding the hadoop installed on the machine or in the path.

## The installation of the automatic-workflow 

First the sourcecode for the release from https://github.com/netarchivesuite/webdanica/releases
unzip the source code. This will produce a `webdanica-$RELEASE` folder (e.g. webdanica-1.2.0), and we want the webdanica-$RELEASE/workflow-template folder.  

If you want instead to download the current source from github, use the `extractFromGithub.sh` command in the tools folder.
```
bash extractFromGithub.sh 1.X
```
This will download a zipfile of the 1.X branch from github and unpack it in the folder 1.X-DD-MM-YYYY/webdanica-1.X Where DD-MM-YY represents the current date.

Now copy the workflow-template folder to $WORKFLOW_USER_HOME/automatic-workflow (e.g. /home/test), and change the owner of the files to the user running the automatic workflow. We have this template, because you can also use the same template-folder to install a manual workflow, manual-workflow. We recommend two separate workflows, even though it is not required.

Download the webdanica-webapp-war-$RELEASE.war from the release page, and unzip it e.g.
```
unzip webdanica-webapp-war-$RELEASE.war -d $RELEASE.war
cp -av $RELEASE.war/WEB-INF/lib WORKFLOW_USER_HOME/automatic-workflow/
```
Remove the logback-classic-1.0.13.jar from WORKFLOW_USER_HOME/automatic-workflow/lib
otherwise you will get a cron-mail from the webdanica-analysis-cron.sh below, even if there is no harvestlogs to process with this content:
```
SLF4J: Class path contains multiple SLF4J bindings.
SLF4J: Found binding in [jar:file:/disk2/test/automatic-workflow/lib/slf4j-log4j12-1.7.12.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: Found binding in [jar:file:/disk2/test/automatic-workflow/lib/logback-classic-1.0.13.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: See http://www.slf4j.org/codes.html#multiple_bindings for an explanation.
SLF4J: Actual binding is of type [org.slf4j.impl.Log4jLoggerFactory]
```

Correct the automatic-workflow/setenv.sh to match your setup. If the information in setenv.sh is wrong, the scripts will fail with an explanation about what is wrong.
Furthermore, you should verify, that the webdanica-core jarfile REGISTER'ed in workflow-template/conf/.pigbootup matches the WEBDANICA_VERSION in the setenv.sh
(e.g. if the WEBDANICA_VERSION is 2.0, lib/webdanica-core-2.0.jar should be REGISTER'ed in automatic-workflow/conf/.pigbootup and/or manual-workflow/conf/.pigbootup).

## The configuration of the crontab 

Copy the scripts/cronjobs folder to the $WORKFLOW_USER_HOME<br/>
Before inserting/updating the crontab for the user running the automatic workflow, do the following checks:
 * Check that the NAS_INSTALL value in cleanup_oldjobs.sh is correct
 * Check that the CRONDIR in the crontab refers to an existing directory
 * Check that the host argument to check_apps_alive.sh is the correct host.
 * Check that the mails of the user running the crontab (e.g. test) should be forwarded to the adminstrators of the webdanica system. This is most easily done by making an alias for the user in /etc/aliases ( remember to renew the aliases.db by running the /usr/bin/newaliases as root ).

In our staging webdanica-analysis-cron.sh is currently run every 2 hours:
this is done with this cron-statement:
```
0 */2  * * *  bash /home/test/automatic-workflow/webdanica-analysis-cron.sh 2>&1 | tee -a $CRONDIR/webdanica-analysis-cron.sh.log
```

The cleanup_oldjobs.sh (found in the cronjobs folder) removes the Heritrix3 libraries from oldjobs, and is currently run every 6 hours.
This is declared with this cron-statement:
```
0 */6 * * * bash /home/test/cronjobs/cleanup_oldjobs.sh 2>&1 | tee -a $CRONDIR/cleanup_oldjobs.sh.sh.log
```

Finally, the check_apps_alive.sh is run once every hour, and checks if the tomcat application and netarchivesuite is alive and well.

This gives us a crontab looking like [this](scripts/cronjobs/crontab.test):

```
CRONDIR=/home/test/cronlogs
## Run the webdanica-analysis-program every 2 hours
0 */2 * * *  bash /home/webdanica/automatic-workflow/webdanica-analysis-cron.sh 2>&1 | tee -a $CRONDIR/webdanica-analysis-cron.sh.log

## Cleanup oldjobs every 6 hours
0 */6 * * * bash /home/webdanica/cronjobs/cleanup_oldjobs.sh 2>&1 | tee -a $CRONDIR/cleanup_oldjobs.sh.log
## restart netarchivesuite once a week (every monday at 02.00 AM) - disabled by default: only enable it in case of memory-leaks in Netarchivesuite
#0 2 * * 1 cd /home/webdanica/WEBDANICA/conf ; ./restart.sh 2>&1 | tee -a  /home/webdanica/WEBDANICA/restart.log

## Check if apps alive tr (e.g. replace http://kb-test-webdanica-001.kb.dk with the correct host)
0 * * * * bash /home/webdanica/cronjobs/check_apps_alive.sh http://kb-test-webdanica-001.kb.dk 2>&1 | tee -a $CRONDIR/check_apps_alive.log
```

Disabling some or all scripts in the crontab is most easily done by running crontab -e
and then writing '#' as the first character of the line and then saving the crontab.

