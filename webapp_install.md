# Installation and configuration of the webdanica webapp

Installation of the webapp requires installation of tomcat, and we have been running with Apache Tomcat 8.0.33, so any 8.0.33+ will probably do as well.

The webapp requires the following Environment variable to be declared in $TOMCAT_HOME/bin/setenv.sh file:
```
WEBDANICA_HOME=/usr/local/webdanica-home
export WEBDANICA_HOME
```
Furthemore the webapp requires two files to be present in the WEBDANICA_HOME directory:
 * webdanica_settings.xml
 * settings_NAS_Webdanica.xml

The names of these two files are hardwired into the web.xml of the webdanica webapp.

The webdanica_settings.xml is the primary configuration file for webdanica, whereas the settings_NAS_Webdanica.xml allows webdanica webapp to interface with the 
local webdanica netarchivesuite system.

The settings_NAS_Webdanica.xml will normally be a copy of the settings_GUIApplication.xml.
However it is advisable to change the applicationInstanceId setting like this
```
<settings>
    <common>
      <environmentName>WEBDANICA</environmentName>
      <applicationInstanceId>webClient</applicationInstanceId>
      ...
```

The webapp will fail to initiate properly, if these two files are absent

#Configuration of the webapp 

## configuration of the environment
There is an env setting in webdanica_setting.xml.

The environment is used in the header of the mails being sent
Header: 
```
[Webdanica-TEST] stopping
```

Body: 
```
[Webdanica-TEST] stopping
Webdanica Webapp (version 0.4.0-SNAPSHOT) stopped on server kb-test-webdanica-001.kb.dk at 'Wed Dec 07 14:13:11 CET 2016'
```
If env='UNKNOWN' or env='UNITTEST' (test is case-insensitive)
no mails are sent 

## mail-setup
```
<mail>
<host>localhost</host>
<admin>account@domain</admin>
<port>25</port>
</mail>
```
The above configuration tells the system to send system-messages to mail account 'account@domain' using port 25 on the localhost
Except for mails sent when the systemt starts and is shutdown, it's only severe errors that cause a mail to be sent.

## filtering-configuration
Filtering is done both by the loadSeeds utility and the filtering workflow running in the webapp.

### Filtering by loadSeeds.
The loadSeeds filters away any url which either
 * is not a valid Url (new URI(seed) throws URISyntaxException)
 * does not contain a correct hostname(url.getHost()==null) and a corect domainname (DomainUtils.domainNameFromHostname(host) == null)
 * is already in a seeds table (DUPLICATE)
 * has a URL protocol, which should be ignored (defined by the setting seeds.ignoredProtocols) - currently loadSeeds accepts only urls with the following schemes ftp,http, https and does not use setting seeds.ignoredProtocols
 * fails to be inserted in the database

The rejects of this filtering ends up in the ingestlog table for the specific seeds-ingest, plus some statistics, and both an accept.log and a reject.log is written to disk.

### Filtering by the filtering-workflow

The workflow starts with looking if the url is a possible redirect. If the url matches any of these regularexpressions
```
"/http", 
"redir.aspx", 
"http://bit.ly", 
"http://goo.gl",
"http://ow.ly", 
"http://t.co", 
"http://tinyurl.com", 
"http://tr.im"
```
then we try to see if the url has a redirect using the wget program.

If it has, the checks below will be done on the redirect url and not on the original url.

The filtering-workflow rejects any url which either
 * ends witha an ignored suffix
 * matches any of the entries in the active blacklists
 * is a url from the .dk domain and the setting seeds.rejectDkUrls is true

Otherwise it will mark the url ready for harvesting

A sample setup could look like this in the settingsfile
```
<seeds>
  <rejectDkUrls>false</rejectDkUrls>
  <ignoredSuffixes>
                <suffix>.jpg</suffix>
                <suffix>.avi</suffix>
                <suffix>.waw</suffix>
                <suffix>.gif</suffix>
                <suffix>.ico</suffix>
                <suffix>.bmp</suffix>                   
                <suffix>.doc</suffix>
                <suffix>.docx</suffix>
                <suffix>.dot</suffix>
                <suffix>.eps</suffix>
                <suffix>.exe</suffix>
                <suffix>.jp2</suffix>           
                <suffix>.jpe</suffix>           
                <suffix>.jpeg</suffix>
                <suffix>.mdb</suffix>           
                <suffix>.mov</suffix>                           
                <suffix>.mp3</suffix>   
                <suffix>.mp4</suffix>           
                <suffix>.mpeg</suffix>                                  
                <suffix>.odt</suffix>
                <suffix>.pdd</suffix>
                <suffix>.pdf</suffix>
                <suffix>.pict</suffix>
                <suffix>.png</suffix>
                <suffix>.psd</suffix>
                <suffix>.rar</suffix>
                <suffix>.raw</suffix>
                <suffix>.rtf</suffix>
                <suffix>.swf</suffix>
                <suffix>.tif</suffix>
                <suffix>.tiff</suffix>             
                <suffix>.wps</suffix>
                <suffix>.xls</suffix>
                <suffix>.css</suffix>
                <suffix>.js</suffix>
        </ignoredSuffixes>
        <ignoredProtocols>
        <protocol>mailto</protocol>
        <protocol>vimeo</protocol>
        <protocol>data</protocol>
        </ignoredProtocols>
</seeds>             
```

## harvesting-workflow-configuration
The below configuration defines how to construct the single seed harvests prepared and run by the harvesting worklow.
All these settings are necessary to enable the harvesting-workflow. Furthermore, the schedule defined by harvesting.schedule, and the template defined by harvesting.template must exist in the local
netarchivesuite system, otherwise the harvestworkflow will be disabled

When the harvestWorkflow is enabled, it will harvest maxSingleSeedHarvests (5 in the sample configuration below) in a row, one after the other, and then write a harvestlog with the successfull harvests to the 
harvestLogDir (/home/harvestlogs/ in the sample configuration below).

The harvestLogs are made writeable by all, so the automatic-workflow can remove the harvestlogs during its processing

```
<maxSingleSeedHarvests>5</maxSingleSeedHarvests>
<schedule>Once</schedule>
<template>webdanica_order</template>
<prefix>webdanica-trial-</prefix>
<maxbytes>10000</maxbytes>
<maxobjects>10000</maxobjects>
<harvestlogDir>/home/harvestlogs/</harvestlogDir>
<harvestlogPrefix>harvestLog-</harvestlogPrefix>
<harvestlogReadySuffix>.txt</harvestlogReadySuffix>
</harvesting>
```
##Notes
 * Setting maxSingleSeedHarvests to zero or a negative number, will also disable the harvestworkflow. Enabling this will currently require the setting to change to a number>0 and the restart of the webapp.
 * The harvestworkflow will currently wait forever for the completion of the harvestjob, so some monitoring of the running jobs page(http://$NASGUI_HOME/History/Harveststatus-running.jsp) and 
the updated time of the seed currently being harvested (Seen when clicking on the Show details page). If the harvesting is deadlocked, terminate the netarchivesuite job either through the Heritrix3 gui if possible, or by restarting the netarchivesuite system. This will make the job fail, and the harvesting workflow will continue with the next harvest



