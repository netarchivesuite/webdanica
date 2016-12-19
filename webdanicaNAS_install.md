# Installation and configuration of the NetarchiveSuite used by webdanica

The webdanica system uses a quickstart based Netarchivesuite 5.1 instance running on the same machine as the webapp.
Probably, Netarchivesuite 5.2+ will also work, but this hasn't been tested.

The recipe used is as written on the page https://sbforge.org/display/NASDOC51/Installation+of+the+Quickstart+system

Only exception is the use of nas_install/deploy_webdanica_netarchivesuite.xml instead of the deploy_standalone_example.xml 

Before deploying netarchivesuite with the RunNetarchivesuite.sh, you need to change the following

 * The /home/test/ARKIV should be replaced with the correct path (this variable must be the same as the WEBDATADIR in the automatic-workflow/setenv.sh)
 * The deployInstallDir (default = /home/test) must be adapted to your environment
 * The deployMachineUserName (default = test) must be adapted to your environment
 * The receiver and sender (default = test@localhost)  must be adapted to your environment
 * The mail.server setting (default = post.kb.dk)  must be adapted to your environment

The differences from the quickstart are as follows: 
 * The netarchivesuite writes its harvestdata to $HOME/ARKIV (e.g. /home/test/ARKIV in our staging setup) using a localarcrepositoryclient in order for the data to easily accessable outside the netarchivesuite system folder
 * No bitpreservation is thus attempted, therefore the netarchive installation has no ArcrepositoryApplication, BitarchiveMonitorApplications, and BitarchiveApplications
 * IndexingApplication is not required, as we assume that the deduplication is disabled by removing the deduplication bean from the template used by Webdanica.

Note: Currently, we have no ViewerProxyApplication and IndexServerApplication as well, but they could be turned on, if viewerproxying the metadata files is required by the curators

A sample Heritrix3 template with the deduplication beans can be found here: nas_install/default_webdanica.xml

What you call this template is up to your, but the name must be same as the setting settings.harvesting.template in your webdanica_settings.xml file.

The same goes for the schedule used by the automatic harvesting workflow defined by the setting settings.harvesting.schedule in your webdanica_settings.xml file.

You need to use a schedule that only runs once.
You create this in the NetarchiveSuite GUI by 
 * going to Definitions->Schedules 
 * select "Create new schedule"
 * Choose name = Once
 * Choose the second Until option (under the Continue subheading)
 * Write 1 (thus it reads until 1 harvests have been done)
 * Save 




