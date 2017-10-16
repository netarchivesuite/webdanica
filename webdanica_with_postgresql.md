# Installation and configuration of the NetarchiveSuite with Postgresql 8.4+

In the following postgresql (8.4.20) is assumed already installed on the machine where the webdanica system is installed.

The settings below show a sample netarchivesuite setup using Postgresql:
 * 5432 is the postgresql port
 * test_harvestdb is the name of the database used by Netarchivesuite
 * username/password is netarchivesuite

Furthermore, the following assumes also that Netarchivesuite is run as the user 'test'.

Note that if any of the settings are different in your installation, the instructions should be changed accordingly.

```
<settings>
        <common>
          <database>
            <class>dk.netarkivet.harvester.datamodel.PostgreSQLSpecifics</class>
            <baseUrl>jdbc:postgresql</baseUrl>
            <machine>localhost</machine>
            <dir>test_harvestdb</dir>
            <port>5432</port>
            <username>netarchivesuite</username>
            <password>netarchivesuite</password>
          </database>       
</common>
```
## Before installation of netarchivesuite postgresql needs to be configured to suite Netarchivesuite

1) As root or postgres user modify pg_hba.conf so 'ident' is replaced by 'trust' for all rules.
So we get a pg_hba.conf looking something like this
```
host    all   all         127.0.0.1/32          trust
local   all   all                               trust
```

2) Create the database 'test_harvestdb', a user 'netarchivesuite', and a tablespace 'tsindex' 
```
sudo su - postgres
mkdir /var/lib/pgsql/tsindex
psql

CREATE DATABASE test_harvestdb WITH ENCODING 'UTF8';
CREATE USER netarchivesuite WITH PASSWORD 'netarchivesuite';
CREATE TABLESPACE tsindex OWNER netarchivesuite LOCATION '/var/lib/pgsql/tsindex';

\q
pg_ctl reload
```
The last command tells Postgresql to reload its configuration.
This should enable us full access over database test_harvestdb as any system-user.

3) Creation of Netarchivesuite tables in the test_harvestdb database as user 'test'
This uses the scripts/sql/createHarvestDB.pgsql bundled with netarchivesuite 5.2.2. If the postgresql user is different from 'netarchivesuite',
you need to replace all instances of 'netarchivesuite' in that script 

```
unzip distribution-5.2.2.zip -d 5.2.2
psql test_harvestdb -U netarchivesuite < 5.2.2/scripts/sql/createHarvestDB.pgsql

psql test_harvestdb -U netarchivesuite

drop table harvestchannel;
delete from schemaversions where tablename='harvestchannel';
update schemaversions set version='3' where tablename='harvestdefinitions';
\q
```
Now we are ready to deploy netarchivesuite.

## Installation of netarchivesuite and updating the database
Now we can deploy netarchivesuite (Note that java in the path must be a JDK8, otherwise the deployment will fail)
Note: Another requirement is passwordless ssh-logon from the deploy server to the installation-server, otherwise you will be typing the password several times.
```
bash RunNetarchiveSuite.sh  distribution-5.2.2.zip deploy_webdanica_netarchivesuite_pgsql.xml USER heritrix3-bundler-5.2.2.zip 
```
This will script should install netarchivesuite correctly, but applications will fail to start of several times:
 * No default_orderxml template exists in the templates table
 * The tables 'harvestdefinitions' and 'harvestchannel' needs to be updated/created
This is done with
```
ssh test@localhost
cd WEBDANICA (= NAS_HOME)
cd conf 
./update_external_harvest_database.sh
```
Now the databasen is updated, but we still need to insert a default_orderxml.xml and the webdanica_order.xml, the latter used by webdanica.

Use a copy of the conf/update_external_harvest_database.sh and name it "conf/HarvestTemplateApplication.sh"
In the script, replace HarvestdatabaseUpdateApplication with HarvestTemplateApplication
and "< /dev/null >> update_external_harvest_database.log 2>&1 &" with "$1 $2 $3"
So the script will look like this:
```
#!/bin/bash
cd /home/test/WEBDANICA
export CLASSPATH=/home/test/WEBDANICA/lib/netarchivesuite-monitor-core.jar:/home/test/WEBDANICA/lib/netarchivesuite-harvest-scheduler.jar:/home/test/WEBDANICA/lib/netarchivesuite-monitor-core.jar:/home/test/WEBDANICA/lib/netarchivesuite-harvester-core.jar:
java -Ddk.netarkivet.settings.file=/home/test/WEBDANICA/conf/settings_update_external_harvest_database.xml dk.netarkivet.harvester.tools.HarvestTemplateApplication $1 $2 $3
```
The following will thus add a default_order (default_order.xml) and webdanica_order (webdanica_order.xml). The following assumes, that we have copied the install/templates ffolder in the zipball to /home/test:
```
cd conf
./HarvestTemplateApplication.sh create webdanica_order /home/test/templates/webdanica_order.xml
./HarvestTemplateApplication.sh create default_orderxml /home/test/templates/default_orderxml.xml
```

We can now restart netarchivesuite using command
```
./restart.sh
```
