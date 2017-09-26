# Script for installation and starting a webdanica netarchivesuite system w/ derby
NAS_VERSION=5.2.2
NAS_ZIP=distribution-${NAS_VERSION}.zip
H3_ZIP=heritrix3-bundler-${NAS_VERSION}.zip
./RunNetarchiveSuite.sh $NAS_ZIP deploy_webdanica_netarchivesuite.xml deploy $H3_ZIP dryrun
