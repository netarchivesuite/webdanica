# Tools manual

The tools folder in the root of the webdanica-repository holds sample scripts for the tools below: loadseeds.sh, loadBlacklist.sh, exportdanica.sh, importIntoNAS.sh.
Note that the output from exportdanica.sh is the input to the importIntoNAS.sh.

## tools/loadseed.sh
Takes one argument: a seedsfile, or two arguments: a seedsfile --accepted   
The result of this operation is added to the ingestlog table, and a rejectlog and an acceptlog is written to the same directory the seedsfile.
if the '--accepted' option is used, the seeds are declared with DanicaStatus.YES when they are inserted
If the seed is already registered as a danica-seed, nothing happens
If the seed is already registered as a not-danica-seed, the danicastate of the seed is changed to danica
Otherwise, the seed is registered as a danica-seed, and the domain of the seed created in the domains table

The template currently looks like this: [tools/loadseed.sh](tools/loadseed.sh)

## tools/loadBlacklist.sh
This script adds a new active blacklist to our webdanica workflow.
We currently don't support updating or deleting a blacklist using this script.
The current procedure is to erase all blacklists using the Apache phoenix CLI client 'sqlline.py' part of the phoenix-bin package 'apache-phoenix-PHOENIXVERSION-HBase-HADOOPVERSION-bin.tar.gz'
(currently PHOENIXVERSION 4.7.1, and HADOOPVERSION 1.1) with command "delete from blacklists;"

The template currently looks like this: [tools/loadBlacklist.sh](tools/loadBlacklist.sh)

## tools/loaddomains.sh
Loads a domain-list into webdanica, inserting them into the domains table.
if the option --accepted is used, the domains are assumed to be fully danica domains, and no further processing is to occur on these domains
Else the domains are ingested with danicastate UNDECIDED

The template currently looks like this: [tools/loaddomains.sh](tools/loaddomains.sh)

## tools/exportdanica.sh
This exports all danica-seeds from webdanica to a file
During the export, the danica seeds not already exported are marked them as exported=true, and the exportedTime is set to the current date.

When using the option '--list_already_exported' all danica seeds is written to a file, including those seeds previously exported

This option is current used in the template script here: [tools/exportdanica.sh](tools/exportdanica.sh)

## tools/importIntoNAS.sh

Note that the script needs to be changed according to the database used of the NAS-system
The seeds are added to a seedslist named 'webdanicaseeds' list. The seedlist is then added to the default configuration of the seed's domain if not already present.
If the domain does not exist in the NAS system, the domain is created, and the seeds added to the webdanica-seeds list as before, but all the seeds in the default seedlist created by NAS are disabled by prefixing each seed with a "#'

The argument are either one seed or a file with seeds.

The template currently looks like this: [tools/importIntoNAS.sh](tools/importIntoNAS.sh)

