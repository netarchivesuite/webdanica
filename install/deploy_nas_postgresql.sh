# Script for installation and starting a webdanica netarchivesuite system w/ derby
NAS_VERSION=5.2.2
NAS_ZIP=distribution-${NAS_VERSION}.zip
H3_ZIP=heritrix3-bundler-${NAS_VERSION}.zip
DEPLOY_TEMPLATE=deploy_webdanica_netarchivesuite_pgsql.xml
## If want to use dryrun
#DRYRUN=dryrun
DRYRUN=
./RunNetarchiveSuite.sh $NAS_ZIP $DEPLOY_TEMPLATE deploy $H3_ZIP $DRYRUN
