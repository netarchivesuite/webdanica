# Installation and configuration of the NetarchiveSuite used by webdanica

The webdanica system uses a quickstart based Netarchivesuite 5.1 instance running on the same machine as the webapp.

Probably, Netarchivesuite 5.2+ will also work, but this hasn't been tested.

The differences from the quickstart are as follows: 
 * The netarchivesuite writes its harvestdata to $HOME/ARKIV (e.g. /home/test/ARKIV in our staging setup) using a localarcrepositoryclient in order for the data to easily accessable outside the netarchivesuite system folder
 * No bitpreservation is thus attempted, therefore the netarchive installation has no ArcrepositoryApplication, BitarchiveMonitorApplications, and BitarchiveApplications
 * IndexingApplication is not required, as we assume that the deduplication is disabled by removing the deduplication bean from the template used by Webdanica.

Note: Currently, we have no ViewerProxyApplication and IndexServerApplication as well, but they could be turned on, if viewerproxying the metadata files is required by the curators


Installation:

Fetch the openmq5_1-binary-linux.zip and the NetarchiveSuite-5.1.zip file from ????


