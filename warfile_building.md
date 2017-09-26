#Building the war-file

Note: Java 7+ is required to build this software.

## Fetch the source from github using git

git https://github.com/netarchivesuite/webdanica.git

## Fetch the correct apache-phoenix-jar needed by the build process

First look at description of the phoenix dependency in webdanica-core/pom.xml 
```
<dependency>
      <groupId>phoenix-hbase</groupId>
      <artifactId>phoenix-HBase</artifactId>
      <version>4.7.0-1.1</version>
      <scope>system</scope>
      <systemPath>${basedir}/lib/phoenix-4.7.0-HBase-1.1-client.jar</systemPath>
    </dependency>
```
Currently this calls for download of the phoenix-4.7.0-HBase-1.1-bin.tar.gz downloaded from https://archive.apache.org/dist/phoenix/phoenix-4.7.0-HBase-1.1/bin/ 
and extracting the phoenix-4.7.0-HBase-1.1-client.jar from this distribution (is in the main folder).

and copy the phoenix-4.7.0-HBase-1.1-client.jar to webdanica-core/lib/

## Build the process

mvn clean install

The result of this process will generate a webdanica-webapp-war-VERSION.war in the webdanica-webapp-war/target folder.

This is the war-file which is deployed as ROOT.war to tomcat.

