# Building the war-file

Note: Java 8+ is required to build and run this software.

## Fetch the source from github using git

git https://github.com/netarchivesuite/webdanica.git

## Build the process

mvn clean install

The result of this process will generate a webdanica-webapp-war-VERSION.war in the webdanica-webapp-war/target folder.

This is the war-file which is deployed as ROOT.war to tomcat.

Note that we no longer includes a phoenix client.jar in our build. You should instead use the phoenix-client.jar installed on the system, where the webapp is being run.
