# Tools manual

The tools folder in the root of the webdanica-repository holds sample scripts for the tools below: loadseeds.sh, loadblacklist.sh exportdanica.sh, importdanica.sh, importIntoNAS.sh.
Note that the output from exportdanica.sh is the input to the importIntoNAS.sh.

## load seeds
Takes one argument a seedsfile.
The result of this operation is added to the ingestlog table, and a rejectlog and an acceptlog is written to the same directory the seedsfile.

## load blacklists
This script adds a new active blacklist to our webdanica workflow.
We currently don't support updating or deleting a blacklist using this script.
The current procedure is to erase all blacklists using the Apache phoenix CLI client 'sqlline.py' part of the phoenix-bin package 'apache-phoenix-PHOENIXVERSION-HBase-HADOOPVERSION-bin.tar.gz'
(currently PHOENIXVERSION 4.7.1, and HADOOPVERSION 1.1) with command "delete from blacklists;"

## export danica-seeds from webdanica to a file
This script exports all the danica-seeds to a file.
By default the script exports the danica seeds not already exported, and then marks them as exported=true, and sets exportedTime to the date exported

When using the option '--list_already_exported' all danica seeds is written to a file, including those seeds previously exported

## import danica-seeds into webdanica from a file. 
If the seed is already registered as a danica-seed, nothing happens
If the seed is already registered as a not-danica-seed, the danicastate of the seed is changed to danica
Otherwise, the seed is registered as a danica-seed, and the domain of the seed created in the domains table

We here use the same tool as Load seeds but with the difference that the seed is accepted, if the seed is otherwise valid

##importIntoNAS.sh

Note that the script needs to be changed according to the database used of the NAS-system
The seeds are added to a seedslist named 'webdanicaseeds' list. The seedlist is then added to the default configuration of the seed's domain if not already present.
If the domain does not exist in the NAS system, the domain is created, and the seeds added to the webdanica-seeds list as before, but all the seeds in the default seedlist created by NAS are disabled by prefixing each seed with a "#'


##loadDomains.sh





