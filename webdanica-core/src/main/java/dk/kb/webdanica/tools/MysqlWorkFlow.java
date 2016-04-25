package dk.kb.webdanica.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.IOException;

import dk.kb.webdanica.tools.MysqlDomainExtract.ExtractDbOperation;

/*
String url = "jdbc:mysql://localhost/test|webdanica";
Class.forName ("com.mysql.jdbc.Driver").newInstance ();
Connection conn = DriverManager.getConnection (url, "username", "password");
*/

/*
Update TABLE ResFromHadoop with C15b varchar(20) with TLD
*/

public class MysqlWorkFlow {

    /**
     * @param args JDBC-URL jdbcUser=<JDBC-username>  dbmachine=<e.g. kb-test-webdania-001> IADATA=false|true datadir=<e.g. /data1/resultater/>  defFile=<file with inf. of finished data> flowpart=all|allbutcopy|dir|table|copy|checkcopy|forceingest|ingest|checkingest|compress|stat manualStep=none|all|none_exceptlost|copyhere|copyback|ingest|compress ignoreFile=true|false|warning  
     * @throws ClassNotFoundException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     * @throws SQLException 
     * @throws IOException 
     */
	
	public static void main(String[] args) throws  IOException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
        /*****************************************/
        /*** Arguments ***************************/
        /*****************************************/
		//def file has elements on form:
		//hadoop@kb-test-sol-001:/home/hadoop/disk7_instans_m004/combo-r1-m001-disk3_3-3-2-29-08-2014/,m001,disk3,3,3-2  or
		//hadoop@kb-test-sol-001:/home/hadoop/disk7_instans_m004/combo-r1-m001-disk3_3-3-2-29-08-2014/,m001,disk3,3,i00
        
    	String errArgTxt = "Proper args: <JDBC-URL> jdbcUser=<JDBC-username> "
    			+ "dbmachine=<e.g. kb-test-webdania-001> "
    			+ "IADATA=false|true "
    			+ "datadir=<e.g. /data1/resultater/>  "
    			+ "deffile=<file with inf. of finished data> "
    			+ "flowpart=all|allbutcopy|dir|copy|checkcopy|table|forceingest|ingest|checkingest|compress|stat "
    			+ "manualStep=none|all|none_exceptlost|copyhere|copyback|ingest|compress"
    			+ "ignoreFile=true|false|warning";
        if (args.length < 9) {
            System.err.println("Missing args!");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        if (args.length > 9) {
            System.err.println("Too many args!");
            System.err.println(errArgTxt);
            System.err.println("Got:");
            for (int i = 0; i < args.length; i++) {
            	 System.err.println(" - " + args[i]);
            }
            System.exit(1);
        }

        /**** args - jdbc ****/
        /** arg - url **/
        String jdbcUrl = args[0];
        if (!jdbcUrl.startsWith("jdbc:mysql:")) {
            System.err.println("Missing arg jdbc setting starting with 'jdbc:mysql'");
            System.err.println(errArgTxt);
            System.exit(1);
        }

        /** arg - user **/
        String jdbcUser = args[1];
        if (!jdbcUser.startsWith("jdbcUser=")) {
            System.err.println("Missing arg jdbcUser setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        jdbcUser = MysqlX.getStringSetting(jdbcUser).toLowerCase();
        
        Class.forName ("com.mysql.jdbc.Driver").newInstance ();
        Connection conn = DriverManager.getConnection (jdbcUrl, jdbcUser, "");    
        
        /**** args - db-machine ****/
        String dbMachine = args[2];
        if (!dbMachine .startsWith("dbmachine=")) {
            System.err.println("Missing arg dbmachine setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        dbMachine = MysqlX.getStringSetting(dbMachine);

        /**** args - IA-data ****/
        String isIADATAtxt = args[3];
        if (!isIADATAtxt .startsWith("IADATA=")) {
            System.err.println("Missing arg IADATA setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        isIADATAtxt = MysqlX.getStringSetting(isIADATAtxt);
        if ( !(isIADATAtxt.equals("true") || isIADATAtxt.equals("false")) ) {
            System.err.println("Illegal value of arg IADATA setting" + isIADATAtxt);
            System.err.println(errArgTxt);
            System.exit(1);
        }

        /**** args - data-dir ****/
        String datadirTxt = args[4];
        if (!datadirTxt.startsWith("datadir=")) {
            System.err.println("Missing arg datadir setting - got " + datadirTxt);
            System.err.println(errArgTxt);
            System.exit(1);
        }
        datadirTxt = MysqlX.getStringSetting(datadirTxt);
    	//System.out.println("datadirTxt: " + datadirTxt);
        File dataDir = new File(datadirTxt);
        if (!dataDir.isDirectory()) {
            System.err.println("ERROR: The given data-dir '" + dataDir.getAbsolutePath() + "' is not a proper directory or does not exist");
            System.err.println(errArgTxt);
            System.exit(1);
        }

        /**** args - def-file ****/
        String defFileTxt = args[5];
        if (!defFileTxt.startsWith("deffile=")) {
            System.err.println("Missing arg defFile setting - got " + defFileTxt);
            System.err.println(errArgTxt);
            System.exit(1);
        }
        defFileTxt = MysqlX.getStringSetting(defFileTxt);
        File defFile = new File(defFileTxt);
        if (!defFile.isFile()) {
            System.err.println("ERROR: The given def-file '" + defFile.getAbsolutePath() + "' is not a proper file or does not exist");
            System.err.println(errArgTxt);
            System.exit(1);
        }

        /**** args - part Of Flow ****/
        String partOfFlowTxt = args[6];
        if (!partOfFlowTxt.startsWith("flowpart=")) {
            System.err.println("Missing arg flowpart setting");
            System.err.println(partOfFlowTxt);
            System.exit(1);
        }
        partOfFlowTxt = MysqlX.getStringSetting(partOfFlowTxt).toLowerCase();
        WfPart partOfWf =  WfPart.wf_none;
        if (partOfFlowTxt.equals("all"))  partOfWf = WfPart.wf_all; 
        else if (partOfFlowTxt.equals("allbutcopy")) partOfWf = WfPart.wf_allbutcopy; 
        else if (partOfFlowTxt.equals("dir")) partOfWf = WfPart.wf_dir; 
        else if (partOfFlowTxt.equals("table")) partOfWf = WfPart.wf_table; 
        else if (partOfFlowTxt.equals("copy")) partOfWf = WfPart.wf_copy; 
        else if (partOfFlowTxt.equals("checkcopy")) partOfWf = WfPart.wf_checkcopy; 
        else if (partOfFlowTxt.equals("ingest")) partOfWf = WfPart.wf_ingest;
        else if (partOfFlowTxt.equals("forceingest")) partOfWf = WfPart.wf_forceingest;
        else if (partOfFlowTxt.equals("checkingest")) partOfWf = WfPart.wf_checkingest; 
        else if (partOfFlowTxt.equals("compress")) partOfWf = WfPart.wf_compress; 
        else if (partOfFlowTxt.equals("stat")) partOfWf = WfPart.wf_statistics;
        else {
            System.err.println("ERROR: Arg flowpart setting is not valid - got '" + partOfFlowTxt + "'");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        
        /**** args - manual steps ****/
        String manualStepTxt = args[7];
        if (!manualStepTxt.startsWith("manualStep=")) {
            System.err.println("Missing arg manualStep setting");
            System.err.println(manualStepTxt);
            System.exit(1);
        }
        manualStepTxt = MysqlX.getStringSetting(manualStepTxt).toLowerCase();
        WfManStep manualSteps =  WfManStep.step_all;
        if (manualStepTxt.equals("all"))  manualSteps = WfManStep.step_all; 
        else if (manualStepTxt.equals("none_exceptlost"))  manualSteps = WfManStep.step_noneexceptlost; 
        else if (manualStepTxt.equals("none")) manualSteps = WfManStep.step_none; 
        else if (manualStepTxt.equals("copyhere")) manualSteps = WfManStep.step_copyhere; 
        else if (manualStepTxt.equals("copyback")) manualSteps = WfManStep.step_copyback; 
        else if (manualStepTxt.equals("ingest")) manualSteps = WfManStep.step_ingest; 
        else if (manualStepTxt.equals("compress")) manualSteps = WfManStep.step_compress;
        else {
            System.err.println("ERROR: Arg manualStep setting is not valid - got '" + manualStepTxt + "'");
            System.err.println(errArgTxt);
            System.exit(1);
        }

        
        /**** args - ignore file ****/
        String ignoreFileTxt = args[8];
        if (!ignoreFileTxt.startsWith("ignoreFile=")) {
            System.err.println("Missing arg ignoreFile setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        ignoreFileTxt = MysqlX.getStringSetting(ignoreFileTxt);
        IgnoreFile ignoreFile = IgnoreFile.if_false;
        if (ignoreFileTxt.equals("false"))  ignoreFile = IgnoreFile.if_false; 
        else if (ignoreFileTxt.equals("true")) ignoreFile = IgnoreFile.if_true; 
        else if (ignoreFileTxt.equals("warning")) ignoreFile = IgnoreFile.if_warning; 
        else {
            System.err.println("ERROR: Arg IgnoreFile setting is not valid - got '" + ignoreFileTxt + "'");
            System.err.println(errArgTxt);
            System.exit(1);
        }

        /*****************************************/
        /*** Start processing ********************/
        /*****************************************/
        // Set of tables to extract lagkage from
        Set<HadoopResItem> itemSet = new HashSet<HadoopResItem>();
        itemSet = readItemsFromDefFile(defFile, dataDir.getAbsolutePath(), dbMachine);
        boolean stop_processing = false;

        
        System.out.println("*****");
    	System.out.println("*** Find intervals directories and table(s):");
        for (HadoopResItem it : itemSet) {
        	if (it.interval.equals("99")) {
        		int i = 1;
        		String datasubdirname = it.datasubdir.getAbsolutePath();
        		File datasubdir = new File (datasubdirname.replace(wf_interval_prefix + "99", wf_interval_prefix + "99" + wf_interval_infix + i) );
        		while (datasubdir.exists()) {
        			i++;
        			datasubdir = new File (datasubdirname.replace(wf_interval_prefix + "99", wf_interval_prefix + "99" + wf_interval_infix + i));
        		}
        		if (partOfWf != WfPart.wf_dir) i--;
        		it.datasubdir = new File (datasubdirname.replace(wf_interval_prefix + "99", wf_interval_prefix + "99" + wf_interval_infix + i));
        		it.interval = it.interval.replace("99", "99" + wf_interval_infix + i);
        		it.dataresfile = new File (it.dataresfile.getAbsolutePath().replace(wf_interval_prefix + "99", wf_interval_prefix + "99" + wf_interval_infix + i));
        		it.datacompresfile = new File (it.datacompresfile.getAbsolutePath().replace(wf_interval_prefix + "99", wf_interval_prefix + "99" + wf_interval_infix + i));
	        } 
        } 
        
        /****** 
        /** Make directory for hadoop data */
    	//is not target for manual steps
        if (partOfWf == WfPart.wf_dir || partOfWf == WfPart.wf_all) {
            System.out.println("*****");
        	System.out.println("*** Created directories:");
            for (HadoopResItem it : itemSet) {

            	
	        	if(it.datasubdir.exists()) {
	                System.out.println("WARNING: Dir altready exists: " + it.datasubdir.getAbsoluteFile());
	            } else {
		        	if(!it.datasubdir.mkdir()) {
		                System.err.println("Could not create dir: " + it.datasubdir.getAbsoluteFile());
		                System.exit(1);
		            }
		        	System.out.println("- " + it.datasubdir.getAbsoluteFile());
	            }

	        	System.out.println("- item datasubdir " + it.datasubdir.getAbsoluteFile());
	        	System.out.println("- item getname " + it.getname(wf_dir_delim));
	        	System.out.println("- item interval " + it.interval);
	        }
            stop_processing = (partOfWf == WfPart.wf_dir); //done work 
        }
        
    	ItemStatusInf itStatSets = new ItemStatusInf();
        itStatSets.findStatusItemSetsInternal(itemSet, false );
        
        /****** 
        /** Copy data to hadoop data dir */
        if (!stop_processing) {
	        if (partOfWf == WfPart.wf_copy || partOfWf == WfPart.wf_all ) {
		    	//via sh script both manual and automatic
	            Set<HadoopResItem> copySet = new HashSet<HadoopResItem>();
		        if (partOfWf == WfPart.wf_copy || partOfWf == WfPart.wf_all) {
		            System.out.println("*****");
		        	System.out.println("*** Copying from Hadoop-data-dir to local database-data-dir " );
		            for (HadoopResItem item : itemSet) {
		            	if (!item.emptydatadir) {
		                    System.out.println("--- WARNING: " + item.datasubdir.getAbsolutePath() + " was not empty!!!!");
		                    System.out.println("therefore the command was not included in the copying scipt: "); 
		                    System.out.println("scp " + item.hadoop_dir + "* " + item.datasubdir.getAbsolutePath() + "/.");
		            	} else if (!item.copyok) {
		                    copySet.add(item);
		            	}
		            }
		        }
		        if (copySet.size()==0) {
	                System.out.println("--- WARNING: nothing to copy!!!!");
		            stop_processing = (partOfWf == WfPart.wf_copy) || manualSteps.equals(WfManStep.step_all) || manualSteps.equals(WfManStep.step_copyhere);  
		        } else {
			        if (manualSteps.equals(WfManStep.step_all) || manualSteps.equals(WfManStep.step_copyhere)) {
			        	String chFilename = wf_copyherefilename_prefix + defFile.getName() +  wf_bash_suffix;
			    		File here_sh_file = new File(chFilename);
			        	createCopyHereCommadfile(copySet, here_sh_file, ignoreFile); //OBS boolean ok = 
			        	System.out.println("- produced copy sh-file: " + here_sh_file.getAbsolutePath() );
			        	System.out.println("*** Do manually: bash the sh-file: '" + here_sh_file.getAbsolutePath()  + "'");
			            stop_processing = true;
			        } else {
			        	//Cannot do it single - since * cannot be resolved!!!!!!!!!!!!!!!!!!
			        	for (HadoopResItem it : copySet) {
				        	String chFilename = it.datasubdir + "/" + wf_copyherefilename_prefix + it.datasubdirname()  + wf_bash_suffix;
				    		File here_sh_file = new File(chFilename);
			                Set<HadoopResItem> singleSet = new HashSet<HadoopResItem>();
			                singleSet.add(it);
				        	createCopyHereCommadfile(singleSet, here_sh_file, ignoreFile); //OBS boolean ok = 
				        	System.out.println("- produced copy sh-file: " + here_sh_file.getAbsolutePath() );
				        	Runtime.getRuntime().exec("bash " + here_sh_file.getAbsolutePath() );
				        	System.out.println("- executing: " + here_sh_file.getAbsolutePath() );
				        	boolean goon = true;
				        	int secondspassed = 0;
				        	int timeout = 1000 * 60 * 10; //10 minutes
				        	while (goon) {
				        		secondspassed = secondspassed +1000;
				        		try {
				        		    Thread.sleep(1000);                 //1000 milliseconds is one second.
				        		} catch(InterruptedException ex) {
				        		    Thread.currentThread().interrupt();
				        		    break;
				        		}
				        		secondspassed = secondspassed +1000;
					        	File[] dirfiles = it.datasubdir.listFiles();
					            for (File f : dirfiles) {
					            	String fname=f.getName();
					            	goon = goon && !fname.equals(wf_donehadoopfilename)
					            			&& !fname.equals(wf_done2hadoopfilename);
					            }
				        		goon = goon && secondspassed <= timeout;
				        	}
			        	}
			            stop_processing = (partOfWf == WfPart.wf_copy); //done work 
			        }
		        }
	        }
        }
        
        /** for all - after this point */
        if (partOfWf == WfPart.wf_all 
        		|| partOfWf == WfPart.wf_allbutcopy
        		|| partOfWf == WfPart.wf_checkcopy
        		|| partOfWf == WfPart.wf_table
        		|| partOfWf == WfPart.wf_ingest
        		|| partOfWf == WfPart.wf_checkingest
        		|| partOfWf == WfPart.wf_compress
        		|| partOfWf == WfPart.wf_statistics)
        {
    	    for (HadoopResItem it : itemSet) {
    	    	it.findCopyStatusPartFiles();
    	    }
        	itStatSets.findStatusItemSetsInternal(itemSet, false);
        }

        /****** 
        /** CHECK Copy data of to hadoop data dir */
        /** follow up on manual copy  */
        /** make done-files and update itStatSets for copied */
        if (!stop_processing) {
        	boolean docheck = true; 
	        if (partOfWf == WfPart.wf_checkcopy || partOfWf == WfPart.wf_all) {
	            System.out.println("*****");
	        	System.out.println("*** Checked copy " );
	        	//if (itStatSets.copied_here_Set.size()==0 ) {
	            //    System.err.println("--- WARNING: No dirs with data copied successfully.");
	        	//}
			    for (HadoopResItem it : itStatSets.copied_here_Set) {
		        	//make _COPIED (wf_donecopyherefilename)  for Success in data dirs
		    		createDoneCopyFile(it, ignoreFile);
			    }
	        	itStatSets.findStatusItemSetsInternal(itemSet, false);
		    } else {
		    	docheck = false;
		    }
	        	
            System.out.println("**docheck: " + docheck);
        	if (docheck) {
	        	itStatSets.findStatusItemSetsInternal(itemSet, false);

	            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
	        	Date now = new Date(System.currentTimeMillis());
	        	String back_fn =  wf_copybackfilename_prefix + defFile.getName() + dateFormat.format(now) + wf_bash_suffix;
                File back_sh_file = new File(back_fn);
                String here_fn =  wf_copyherefilename_prefix + defFile.getName() + dateFormat.format(now) + wf_bash_suffix;
                File here_sh_file = new File(here_fn);
	        	if (itStatSets.ok_for_ingest_Set.size()>0 || itStatSets.ok_for_hadoopUpdate_Set.size()>0) {
	        		if (itStatSets.ok_for_ingest_Set.size()>0) createCopyBackCommadfile(itStatSets.ok_for_ingest_Set, back_sh_file, ignoreFile); //OBS boolean ok = 
	        		else if (itStatSets.ok_for_hadoopUpdate_Set.size()>0) createCopyBackCommadfile(itStatSets.ok_for_hadoopUpdate_Set, back_sh_file, ignoreFile); //OBS boolean ok = 
	            	System.out.println("- Copy WITH success:");
	                for (HadoopResItem it : itStatSets.copyfile_produced_Set) {
	                	System.out.println("--- " + it.datasubdirname() + "(done)");
	                }
	                for (HadoopResItem it : itStatSets.copied_here_Set) {
	                	System.out.println("--- " + it.datasubdirname() + "(copied)");
	                }
	            	System.out.println("- new copy-back-file: " + back_sh_file.getAbsolutePath());
	            } else if (itStatSets.followup_Set.size() > 0) {
	            	Set<HadoopResItem> copyback_Set = new HashSet<HadoopResItem>();
	                for (HadoopResItem it : itStatSets.followup_Set) {
	                	if (it.copiedOk && !it.copybackfileProduced) {
	                		copyback_Set.add(it);
	                	}
	                }
	                if (copyback_Set.size() > 0) {
		                System.out.println("**create copy back? ");
		        		createCopyBackCommadfile(copyback_Set, back_sh_file, ignoreFile);
		            }
	            }
	
	        	//make script with copy NOT copies AND copy _COPIED (wf_donecopyherefilename) back to origin
	        	if (itStatSets.copy_failed_Set.size()>0) {
	                System.out.println("------------------------------" );
	            	System.out.println("- Copy WITHOUT success:");
	                createCopyHereCommadfile(itStatSets.copy_failed_Set, here_sh_file, ignoreFile); //OBS boolean ok = 
	            	System.out.println("- new copyfile - for failed: " + here_sh_file);
		        	System.out.println("*** Do manually: bash the copy here sh-file: '" + here_sh_file.getAbsolutePath()  + "'");
		        	stop_processing = manualSteps.equals(WfManStep.step_noneexceptlost ) ;
	        	}
	        	
	        	//Do copy back
	        	if (itStatSets.ok_for_ingest_Set.size()>0 || itStatSets.ok_for_hadoopUpdate_Set.size()>0) {
			        if (stop_processing || manualSteps.equals(WfManStep.step_all) || manualSteps.equals(WfManStep.step_copyhere)) {
			            stop_processing = true;
			        	System.out.println("*** Do manually: bash the copy back sh-file: '" + back_sh_file.getAbsolutePath()  + "'");
			        } else {
			        	Runtime.getRuntime().exec("bash " + back_sh_file.getAbsolutePath() );
			        	System.out.println("- executed command file: " +  back_sh_file.getAbsolutePath()  );
			            stop_processing = (partOfWf == WfPart.wf_checkcopy); //done work 
			        }
		        }
        	} //docheck
        } //not stopprocesssion

        /****** 
        /** Make table */
    	//is not target for manual steps
        if (!stop_processing) {
	        if (partOfWf == WfPart.wf_table || partOfWf == WfPart.wf_all || partOfWf == WfPart.wf_allbutcopy) {
	            // Set of tables in database 

	        	System.out.println("*****");
	        	System.out.println("*** Creating/Updating tables:");
	            for (HadoopResItem it : itemSet) {
	            	Set<String> updTableSet = findUpdateTables(conn, it);
	 	            for (String st: updTableSet) System.out.println("UpdateTable: " + st);
	            	
	        		if (!updTableSet.isEmpty()) {
		            	if (it.hadoop_version.isEmpty()) { //otherwise it is an update
		            		System.out.println("--- WARNING: table "  + it.tablename() + " allready exists");
		            	} else {
		        			System.out.println("--- table "  + it.tablename() + " ok for update" );
		            	}
	        		} else {
		            	if (it.hadoop_version.isEmpty()) { //otherwise it is an update
					        //Create table and indexes
		        			MysqlRes.createTableWithIndexes(conn, it.tablename(), jdbcUser);
		        			System.out.println("--- " + it.tablename() + " with indexes" );
		            	} else {
		        			System.out.println("--- WARNING: table "  + it.tablename() + " DID NOT EXISTS FOR UPDATE" );
		            	}
		            }
	            }
	            stop_processing = (partOfWf == WfPart.wf_table); //done work 
	        }
        }
	
        /****** 
        /** Ingest hadoop data into table */
    	System.out.println("*** BEFORE Ingesting/updating files " );
        if (!stop_processing) {
        	System.out.println("*** STARTING Ingesting/updating files " );
	    	itStatSets.findStatusItemSetsInternal(itemSet, (partOfWf == WfPart.wf_forceingest));
	    	boolean ingest = true;
	    	boolean update = true;
	        if (partOfWf == WfPart.wf_ingest || partOfWf == WfPart.wf_forceingest || partOfWf == WfPart.wf_all || partOfWf == WfPart.wf_allbutcopy) {
	            System.out.println("*****");
	        	System.out.println("*** Ingesting/updating files " );
	        	if (itStatSets.ok_for_ingest_Set.size()==0 ) ingest = false;
	        	if (itStatSets.ok_for_hadoopUpdate_Set.size()==0 ) update = false;
	        	if (!(ingest || update)) {
	                System.out.println("--- WARNING: No dirs with data for ingest/update.");
	                stop_processing = false;
	        	}
	        	if (ingest && (itStatSets.ok_for_ingest_Set.size() < itemSet.size())) {
	                System.out.println("--- WARNING: not all copies are completed - only dirs with completed copies are ingested.");
		            for (HadoopResItem it : itemSet) {
		            	if (!itStatSets.ok_for_ingest_Set.contains(it))
		            		System.out.println("---- " + it.datasubdirname());
		            }
		        	stop_processing = manualSteps.equals(WfManStep.step_noneexceptlost ) ;
	        	}
	        	
	        	//System.out.println("itStatSets.ok_for_hadoopUpdate_Set.size() " + itStatSets.ok_for_hadoopUpdate_Set.size());
	        	//System.out.println("itemSet.size() " + itemSet.size());
	        	if (update && (itStatSets.ok_for_hadoopUpdate_Set.size() < itemSet.size())) {
	                System.out.println("--- WARNING: not all copies are completed - only dirs with completed copies are updated.");
		            for (HadoopResItem it : itemSet) {
		            	if (!itStatSets.ok_for_hadoopUpdate_Set.contains(it))
		            		System.out.println("---- " + it.datasubdirname() + "(failed)");
		            }
		        	stop_processing = manualSteps.equals(WfManStep.step_noneexceptlost ) ;
	        	}
	        	
	        	if (update) {
		        	//check tables
	        		//System.out.println("itStatSets.ok_for_hadoopUpdate_Set: " +itStatSets.ok_for_hadoopUpdate_Set.size());
		            for (HadoopResItem it : itStatSets.ok_for_hadoopUpdate_Set) {
		                Set<String> updTableSet = findUpdateTables(conn, it); 
		            	it.emptytable = updTableSet.isEmpty();
		            	for (String t : updTableSet) {
		            		it.emptytable = it.emptytable || (MysqlRes.getTableCntEntries(conn, t,"") == 0);
		                	if (it.emptytable) {
		                		System.out.println("--- WARNING: table " + it.tablename() + " did NOT contain data");
		                	}
		            	}
                	}
		            boolean ok = true;
		            
			        if (manualSteps.equals(WfManStep.step_all) || manualSteps.equals(WfManStep.step_ingest)) {
			            stop_processing = true;
			        	String fn = wf_multiupdatehadoopfilename_prefix + defFile.getName() + wf_bash_suffix;
			            File mupd_sh_file = new File(fn);
			        	ok = createMultiUpdateCommadfile(itStatSets.ok_for_hadoopUpdate_Set, mupd_sh_file, jdbcUser, dataDir.getAbsolutePath(), ignoreFile);  
			        	System.out.println("- Produced update sh-file: " + mupd_sh_file );
			        	System.out.println("*** Do manually: bash of the update sh-file: '" + mupd_sh_file.getAbsolutePath()  + "'");
			        } else {
			            for (HadoopResItem item : itStatSets.ok_for_hadoopUpdate_Set) {
				        	ok = javaMultiUpdateCommadfile(item, conn, ignoreFile);
				        	/*if (ok) {
					            String uHadoopUpdFilename = item.datasubdir.getAbsolutePath() + "/" + MysqlWorkFlow.wf_update_newHadoop_filename;
					            File uhad = new File(uHadoopUpdFilename); 
					    		if (!uhad.exists()) uhad.createNewFile();
				        	}*/
				        	
				            stop_processing = stop_processing || (manualSteps.equals(WfManStep.step_noneexceptlost) && !ok);
				            
				            //Check update in order to register update - otherwise interrupt can be expensive
					        if (ok && !stop_processing) {
				            	ok = checkUpdate(item, defFile, jdbcUser, manualSteps, ignoreFile);
					            stop_processing = (partOfWf == WfPart.wf_ingest) || !ok; //done work 
			                }
			            }
			        }
            	}
	        	if (ingest) {
		        	//check tables
		            for (HadoopResItem it : itStatSets.ok_for_ingest_Set) {
		            	it.emptytable = (MysqlRes.getTableCntEntries(conn, it.tablename(),"") == 0);
	                	if (!it.emptytable) {
	                		System.out.println("--- WARNING: table " + it.tablename() + " did already contain data");
	                		System.out.println("    checkDoublets will be set to true, and processing may take some time!!!");
	                	}
                	}
		            boolean ok = true;
			        if (manualSteps.equals(WfManStep.step_all) || manualSteps.equals(WfManStep.step_ingest)) {
			            stop_processing = true;
			        	String fn = wf_multiingestfilename_prefix + defFile.getName() + wf_bash_suffix;
			            File ming_sh_file = new File(fn);
			        	ok = createMultiIngestCommadfile(itStatSets.ok_for_ingest_Set, ming_sh_file, jdbcUser, isIADATAtxt, dataDir.getAbsolutePath(), ignoreFile);  
			        	System.out.println("- Produced ingest sh-file: " + ming_sh_file );
			        	System.out.println("*** Do manually: bash of the ingest sh-file: '" + ming_sh_file.getAbsolutePath()  + "'");
			        } else {
			            for (HadoopResItem item : itStatSets.ok_for_ingest_Set) {
				        	ok = javaMultiIngestCommadfile(item, conn, isIADATAtxt, ignoreFile);
				            stop_processing = stop_processing || (manualSteps.equals(WfManStep.step_noneexceptlost) && !ok);
				            
				            //Check ingest in order to register ingest - otherwise interrupt can be expensive
					        if (ok && !stop_processing) {
				            	ok = checkIngest(item, defFile, jdbcUser, manualSteps, isIADATAtxt, ignoreFile);
					            stop_processing = (partOfWf == WfPart.wf_ingest) || !ok; //done work 
			                }
			            }
			        }
	        	}
	        }
        }

        /****** 
        /** Check ingested/updated data  */
    	//is not target for manual steps
        if (!stop_processing) {
	        if (partOfWf == WfPart.wf_checkingest || partOfWf == WfPart.wf_all || partOfWf == WfPart.wf_allbutcopy) {
	        	itStatSets.findStatusItemSetsInternal(itemSet, false);
	            System.out.println("*****");
	        	System.out.println("*** Checking ingest in directories:" );
	        	if (itStatSets.ok_for_ingest_Set.size()==0 || itStatSets.ok_for_hadoopUpdate_Set.size()==0) {
		        	System.out.println("- no directories are ready for ingest/update checking" );
	        	}
	            for (HadoopResItem it : itemSet) {
	            	if (itStatSets.ok_for_ingest_Set.contains(it)) {
		            	boolean ok = checkIngest(it, defFile, jdbcUser, manualSteps, isIADATAtxt, ignoreFile);
			            stop_processing = (partOfWf == WfPart.wf_checkingest) || !ok; //done work 
		            }
	            }
	            for (HadoopResItem it : itemSet) {
	            	if (itStatSets.ok_for_hadoopUpdate_Set.contains(it) ) { //&& it.hadoopfileProduced ) {
		            	boolean ok = checkUpdate(it, defFile, jdbcUser, manualSteps, ignoreFile);
			            stop_processing = (partOfWf == WfPart.wf_checkingest) || !ok; //done work 
		            }
	            }
            }
        }
        
    	/****** 
        /** compress data in datadir */
        if (!stop_processing) {
	        if (partOfWf == WfPart.wf_compress || partOfWf == WfPart.wf_all || partOfWf == WfPart.wf_allbutcopy) {
	        	itStatSets.findStatusItemSetsInternal(itemSet, false);
	            System.out.println("*****");
	        	System.out.println("*** Compressing part-files in directories:" );
	        	if (itStatSets.ok_for_compress_Set.size()==0) {
		        	System.out.println("- no directories are ready for copression" );
	        	}
	        	for (HadoopResItem it : itStatSets.ok_for_compress_Set) {
	        		createCompressCommadfile(it, it.datacompresfile, ignoreFile); //boolean ok =
		        	System.out.println("- produced compress command file: " + it.datacompresfile.getAbsolutePath() );
			        if (manualSteps.equals(WfManStep.step_all) || manualSteps.equals(WfManStep.step_compress)) {
			        	System.out.println("*** Do manually: bash the sh-file: '" + it.datacompresfile.getAbsolutePath()  + "'");
			        } else {
			        	Runtime.getRuntime().exec("bash " + it.datacompresfile.getAbsolutePath());
			        	System.out.println("- executed command file: " +  it.datacompresfile.getAbsolutePath() );
			        }
	            }
		        if (manualSteps.equals(WfManStep.step_all) || manualSteps.equals(WfManStep.step_compress)) {
		            stop_processing = true;
		        } else {
		            stop_processing = (partOfWf == WfPart.wf_compress); //done work 
		        }
	        }
        }
        
    	/****** 
        /** make stat */
        if (!stop_processing) {
        	
	        if (partOfWf == WfPart.wf_statistics || partOfWf == WfPart.wf_all || partOfWf == WfPart.wf_allbutcopy) {
	        	System.out.println("Start stat 2");
	        	itStatSets.findStatusItemSetsInternal(itemSet, false);
	            System.out.println("*****");
	        	System.out.println("*** Find statistics for directories:" );
	        	if (itStatSets.ok_for_stat_Set.size()==0) {
		        	System.out.println("- no directories are ready for statistics" );
	        	}
	        	for (HadoopResItem it : itStatSets.ok_for_stat_Set) {
		        	System.out.println("*** Item ok for stat:" + it.tablename());
	        		MysqlStatExtract.extractStatForItem(conn, it); 
		        	//System.out.println("- produced compress command file: " + it.datacompresfile.getAbsolutePath() );
		        	File statmark = new File(it.datasubdir.getAbsolutePath() + "/" + MysqlWorkFlow.wf_donestatfilename + MysqlIngester.ingest_current_update_no); 
		    		if (!statmark.exists()) {
		    			statmark.createNewFile();
		    		} 
	            }
		        stop_processing = (partOfWf == WfPart.wf_statistics); //done work 
	        }
        }

    	/****** 
        /** update general status on db machine */
        MysqlAllStatusExtract.extractAllStatus(conn, dbMachine, datadirTxt);
        
        /****** 
        /** write status */
        itStatSets.findStatusItemSetsInternal(itemSet, false);
        System.out.println("*****");
    	System.out.println("*** General status for directories after run:" );
    	String[] resLines = itStatSets.writeStatusItemSets(itemSet.size());
        for (String l: resLines) {
        	if (l!=null) {
        		System.out.println(l);
        	}
        }
	}

	private static boolean checkIngest(HadoopResItem it, File defFile, String jdbcUser, WfManStep manualSteps, String isIADATAtxt, IgnoreFile ignoreFile) throws IOException {
		boolean ok = true;

		System.out.println("--- checking " + it.datasubdirname());
		
        if(!it.datasubdir.exists()) {
            System.err.println("- Could not find dir: " + it.datasubdir.getAbsoluteFile());
            System.exit(1);
        }
        //Do check
    	MysqlX.CheckResult chkRes = MysqlCheckIngests.checkIngestResFile(it.dataresfile, 0, it.maxPartNo, false);
    	
    	//Check possible missing files
    	File[] dirfiles = it.datasubdir.listFiles();
        for (File f : dirfiles) {
        	if (chkRes.checkSet.contains(f.getName())) {
        		chkRes.missingSet.add(f.getName());
        	}
        }

        //find result lines
        String[] resLines = findIngestCheckResultLines(chkRes, IngestType.it_base_ingest); 

        //Write result
        if (chkRes.missingSet.size()==0 && chkRes.resfileLineCount>0) {
        	createDoneIngestFile(it, resLines, ignoreFile);
        } else {
        	String fn = wf_ingestfilename_prefix + defFile.getName() + wf_bash_suffix;
            File ing_sh_file = new File(fn);
        	createIngestCommadfile(it, chkRes.missingSet, ing_sh_file, jdbcUser, isIADATAtxt, true, it.datadir.getAbsolutePath(), ignoreFile); // boolean ok = 
            System.out.println( "* NOT OK!!!!!");
        	System.out.println("- Produced ingest sh-file of MISSING part-files: " + ing_sh_file.getAbsolutePath() );
        	System.out.println("*** Do manually: bash of the ingest sh-file: '" + ing_sh_file.getAbsolutePath()  + "'");
        	ok = ok && manualSteps.equals(WfManStep.step_noneexceptlost ) ;
        }
        for (String l: resLines) {
        	System.out.println(l);
        }
        
        return ok;
	}
	
	private static boolean checkUpdate(HadoopResItem it, File defFile, String jdbcUser, WfManStep manualSteps, IgnoreFile ignoreFile) throws IOException {
		boolean ok = true;

		System.out.println("--- checking " + it.datasubdirname());

        if(!it.datasubdir.exists()) {
            System.err.println("- Could not find dir: " + it.datasubdir.getAbsoluteFile());
            System.exit(1);
        }
        //Do check
    	MysqlX.CheckResult chkRes = MysqlCheckIngests.checkIngestResFile(it.dataresfile, 0, it.maxPartNo,true);
    	
    	//Check possible missing files
    	File[] dirfiles = it.datasubdir.listFiles();
        for (File f : dirfiles) {
        	if (chkRes.checkSet.contains(f.getName())) {
        		chkRes.missingSet.add(f.getName());
        	}
        }

        //find result lines
        String[] resLines = findIngestCheckResultLines(chkRes, IngestType.it_first_hadoopupadte); 

        //Write result
        if (chkRes.missingSet.size()==0 && chkRes.resfileLineCount>0) {
        	createDoneUpdateFile(it, resLines, ignoreFile);
        } else {
        	String fn = wf_updatehadoopfilename_prefix + defFile.getName() + wf_bash_suffix;
            File upd_sh_file = new File(fn);
            createUpdatehadoopCommadfile(it, chkRes.missingSet, upd_sh_file, jdbcUser, it.datadir.getAbsolutePath(), ignoreFile); // boolean ok = 
            System.out.println( "* NOT OK!!!!!");
        	System.out.println("- Produced upadte sh-file of MISSING part-files: " + upd_sh_file.getAbsolutePath() );
        	System.out.println("*** Do manually: bash of the update sh-file: '" + upd_sh_file.getAbsolutePath()  + "'");
        	ok = ok && manualSteps.equals(WfManStep.step_noneexceptlost ) ;
        }
        for (String l: resLines) {
        	System.out.println(l);
        }
        return ok;
	}
	
	private static String[] findIngestCheckResultLines(MysqlX.CheckResult chkRes, IngestType ingType) {
		String[] resLines = new String[3+chkRes.missingSet.size()+3+chkRes.warningSet.size()];
		int i = 0; 
		if (chkRes.missingSet.size()==0 && chkRes.resfileLineCount>0) {
			resLines[i]="* OK";
			i++;
		}
		resLines[i]="- Res-file #lines " + chkRes.resfileLineCount + " lines";
		i++;
		resLines[i]="- Missing " + chkRes.missingSet.size() + " lines";
		i++;
		for (String s : chkRes.missingSet) {
			resLines[i]="--- MISSING: " + s + "' possibly missing in database";
			i++;
		}
		resLines[i]="- Accummulated processed lines in ingest:  " + chkRes.processedCount;
		i++;
		resLines[i]="- of which are skipped:                   " + chkRes.skippedCount;
		i++;
		if (ingType==IngestType.it_base_ingest)
			resLines[i]="- of which are Ignored: " + chkRes.ignoredOrUpdateCount;
		else if (ingType==IngestType.it_first_hadoopupadte)
			resLines[i]="- of which are Updated: " + chkRes.ignoredOrUpdateCount;
		i++;
		for (String s : chkRes.warningSet) {
			resLines[i]="--- " + s;
			i++;
		}
		return resLines; 
	}

	private static boolean createCopyHereCommadfile(Set<HadoopResItem> itemSet, File writeFile, IgnoreFile ignoreFile) throws IOException {
		boolean ok = true;
		if (writeFile.exists()) {
			if (ignoreFile.equals(IgnoreFile.if_false)) {
	            System.err.println("ERROR: copy-here file allready existed " + writeFile.getAbsolutePath());
	            System.exit(1);
			} else if (ignoreFile.equals(IgnoreFile.if_warning)) {
	            System.out.println("WARNING: copy-here file allready existed " + writeFile.getAbsolutePath());
			} 
		} 
		writeFile.createNewFile();

		FileWriter fw = new FileWriter(writeFile.getAbsoluteFile());
        BufferedWriter  bw = new BufferedWriter(fw);        

        bw.write("#/bin/bash");
        bw.newLine();
        
        for (HadoopResItem item : itemSet) {
        	String cp_line1 = "scp " 
        			+ item.hadoop_dir 
        			+ (item.interval.isEmpty() 
        					? "" 
        					: (item.hadoop_version.isEmpty() 
        							? MysqlX.partfile_prefix + item.interval
        					    	: "" 
        					  )	
        			   )  + "* "
        			+ item.datasubdir.getAbsolutePath() + "/.";
        	String cp_line2 = (item.interval.isEmpty() 
        			? "" : "scp " + item.hadoop_dir + wf_donehadoopfilename_prefix + "* " + item.datasubdir.getAbsolutePath() + "/.");
       		if (!item.emptydatadir) {
                System.out.println("--- WARNING: " + item.datasubdir.getAbsolutePath() + " was not empty!!!!");
                System.out.println("therefore the command was not included in the copying scipt: "); 
                System.out.println(cp_line1);
                if(!cp_line2.isEmpty()) System.out.println(cp_line2);
        	} else if (!item.copyok) {
                System.out.println("--- " + cp_line1 + (cp_line2.isEmpty() ? "" : " - " + cp_line2 ));
    	        bw.write(cp_line1);
    			bw.newLine();
                if (!cp_line2.isEmpty()) {
                	bw.write(cp_line2);
        			bw.newLine();
                }
        	}
        }
	
		bw.close();
		return ok;
	}

	private static boolean createCopyBackCommadfile(Set<HadoopResItem> itSet, File writeFile , IgnoreFile ignoreFile) throws IOException {
		boolean ok = true;
		if (writeFile.exists()) {
			if (ignoreFile.equals(IgnoreFile.if_false)) {
	            System.err.println("ERROR: copy-back file allready existed " + writeFile.getAbsolutePath());
	            System.exit(1);
			} else if (ignoreFile.equals(IgnoreFile.if_warning)) {
	            System.out.println("WARNING: copy-back file allready existed " + writeFile.getAbsolutePath());
			} 
		} 
		writeFile.createNewFile();

		FileWriter fw = new FileWriter(writeFile.getAbsoluteFile());
        BufferedWriter  bw = new BufferedWriter(fw);        

        bw.write("#/bin/bash");
        bw.newLine();
        for (HadoopResItem item : itSet) {
	        bw.write("scp " +  item.datasubdir.getAbsolutePath() + "/" + wf_donecopyherefilename + item.interval + " " +  item.hadoop_dir + ".");
			bw.newLine();
	        bw.write("echo \" \" > " +  item.datasubdir.getAbsolutePath() + "/" + wf_donecopybackfilename );
			bw.newLine();
        }
		bw.close();
		return ok;
	}

	private static boolean createDoneCopyFile(HadoopResItem it, IgnoreFile ignoreFile) throws IOException {
		boolean ok = true;
        File writeFile = new File(it.datasubdir.getAbsolutePath()+ "/" + wf_donecopyherefilename + it.interval ); //datadir ?
		if (writeFile.exists()) {
			if (ignoreFile.equals(IgnoreFile.if_false)) {
	            System.err.println("ERROR: done file allready existed " + writeFile.getAbsolutePath());
	            System.exit(1);
			} else if (ignoreFile.equals(IgnoreFile.if_warning)) {
	            System.out.println("WARNING: done file allready existed " + writeFile.getAbsolutePath());
			} 
		} 
		writeFile.createNewFile();
		FileWriter fw = new FileWriter(writeFile.getAbsoluteFile());
        BufferedWriter  bw = new BufferedWriter(fw);        

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    	Date now = new Date(System.currentTimeMillis());

    	bw.write("From db machine: '" + it.dbmachine + "'");
        bw.newLine();
        bw.write("For datadir: '" + it.datasubdirname() + "'");
        bw.newLine();
        bw.write("#files copies: datadir: " + it.fileCnt);
        bw.newLine();
        bw.write("last partno: " + it.maxPartNo);
        bw.newLine();
        bw.write("Date: " + dateFormat.format(now));
        bw.newLine();
		bw.close();
		return ok;
	}

	private static boolean createDoneUpdateFile(HadoopResItem it, String[] resLines, IgnoreFile ignoreFile) throws IOException {
		boolean ok = true;
		File writeFile = new File(it.datasubdir.getAbsolutePath()+ "/" + wf_doneupdatenewHadoopfilename ); //datadir ?
		if (writeFile.exists()) {
			if (ignoreFile.equals(IgnoreFile.if_false)) {
	            System.err.println("ERROR: Done update file allready existed " + writeFile.getAbsolutePath());
	            System.exit(1);
			} else if (ignoreFile.equals(IgnoreFile.if_warning)) {
	            System.out.println("WARNING: Done update file allready existed " + writeFile.getAbsolutePath());
			} 
		} 
		writeFile.createNewFile();
		FileWriter fw = new FileWriter(writeFile.getAbsoluteFile());
        BufferedWriter  bw = new BufferedWriter(fw);        

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    	Date now = new Date(System.currentTimeMillis());

    	bw.write("Updated");
        bw.newLine();
        bw.write("For datadir: '" + it.datasubdirname() + "'");
        bw.newLine();
        bw.write("Date: " + dateFormat.format(now));
        bw.newLine();
        bw.newLine();
        for (String l : resLines) {
        	bw.write(l);
            bw.newLine();
        }
		bw.close();

		String exeTxt = "touch " + it.datasubdir.getAbsolutePath() + "/" + MysqlWorkFlow.wf_updatefilename_prefix + MysqlIngester.ingest_current_update_no;
    	Runtime.getRuntime().exec(exeTxt);
    	System.out.println("- executed: '" +  exeTxt + "'");
		return ok;
		
	}
	private static boolean createDoneIngestFile(HadoopResItem it, String[] resLines, IgnoreFile ignoreFile) throws IOException {
		boolean ok = true;
		File writeFile = new File(it.datasubdir.getAbsolutePath()+ "/" + wf_doneingestfilename ); //datadir ?
		if (writeFile.exists()) {
			if (ignoreFile.equals(IgnoreFile.if_false)) {
	            System.err.println("ERROR: Done ingest file allready existed " + writeFile.getAbsolutePath());
	            System.exit(1);
			} else if (ignoreFile.equals(IgnoreFile.if_warning)) {
	            System.out.println("WARNING: Done ingest file allready existed " + writeFile.getAbsolutePath());
			} 
		} 
		writeFile.createNewFile();
		FileWriter fw = new FileWriter(writeFile.getAbsoluteFile());
        BufferedWriter  bw = new BufferedWriter(fw);        

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    	Date now = new Date(System.currentTimeMillis());

    	bw.write("Ingested on machine: '" + it.dbmachine + "'");
        bw.newLine();
        bw.write("For datadir: '" + it.datasubdirname() + "'");
        bw.newLine();
        bw.write("Date: " + dateFormat.format(now));
        bw.newLine();
        bw.newLine();
        for (String l : resLines) {
        	bw.write(l);
            bw.newLine();
        }
		bw.close();
		
		String exeTxt = "touch " + it.datasubdir.getAbsolutePath() + "/" + MysqlWorkFlow.wf_updatefilename_prefix + MysqlIngester.ingest_current_update_no;
    	Runtime.getRuntime().exec(exeTxt);
    	System.out.println("- executed: '" +  exeTxt + "'");
		
		return ok;
	}

	private static boolean createMultiUpdateCommadfile(Set<HadoopResItem> itSet, File writeFile, String jdbcUser, String datadir, IgnoreFile ignoreFile) throws IOException {
		boolean ok = true;
		if (writeFile.exists()) {
			if (ignoreFile.equals(IgnoreFile.if_false)) {
	            System.err.println("ERROR: multi update cmd file allready existed " + writeFile.getAbsolutePath());
	            System.exit(1);
			} else if (ignoreFile.equals(IgnoreFile.if_warning)) {
	            System.out.println("WARNING: multi update cmd file allready existed " + writeFile.getAbsolutePath());
			} 
		} 
		writeFile.createNewFile();
		
		FileWriter fw = new FileWriter(writeFile.getAbsoluteFile());
        BufferedWriter  bw = new BufferedWriter(fw);        

        bw.write("#/bin/bash");
        bw.newLine();
        bw.write("TIME_SETTING=$1");
        bw.newLine();
        bw.write("OK=\"1\"");
        bw.newLine();
        bw.write("if [ \"$TIME_SETTING\"  = \"\" ]; then");
		bw.newLine();
        bw.write("    echo \"Missing def of whether time is now or a specific time\"");
		bw.newLine();
        bw.write("    echo \"on form $TIME_SETTING | <DD-MM-YYYY>\"");
		bw.newLine();
        bw.write("    OK=\"0\"");
		bw.newLine();
        bw.write("fi");
		bw.newLine();
        bw.write("if [ \"$OK\"  = \"1\" ]; then");
		bw.newLine();
		
        for (HadoopResItem item : itSet) {
        	String param = " jdbcUser=" + jdbcUser
        			+ " " + datadir + "/" + item.datasubdirname()
					+ " ignoreFile="
        			      + (ignoreFile.equals(IgnoreFile.if_true) 
        			          ? "true" 
        			          : (ignoreFile.equals(IgnoreFile.if_false) ? "false" : "warning"));
	        bw.write("    echo \"" + item.dataresfile.getAbsolutePath() + "\"");
			bw.newLine();
			bw.write("    bash db_multinewupdate_data.sh $TIME_SETTING" + param );
			bw.newLine();
        }
        bw.write("fi");
		bw.newLine();
		bw.newLine();
		bw.close();
		return ok;
	};

	private static boolean createMultiIngestCommadfile(Set<HadoopResItem> itSet, File writeFile, String jdbcUser, String iaData, String datadir, IgnoreFile ignoreFile) throws IOException {
		boolean ok = true;
		if (writeFile.exists()) {
			if (ignoreFile.equals(IgnoreFile.if_false)) {
	            System.err.println("ERROR: multi ingest cmd file allready existed " + writeFile.getAbsolutePath());
	            System.exit(1);
			} else if (ignoreFile.equals(IgnoreFile.if_warning)) {
	            System.out.println("WARNING: multi ingest cmd file allready existed " + writeFile.getAbsolutePath());
			} 
		} 
		writeFile.createNewFile();
		
		FileWriter fw = new FileWriter(writeFile.getAbsoluteFile());
        BufferedWriter  bw = new BufferedWriter(fw);        

        bw.write("#/bin/bash");
        bw.newLine();
        bw.write("TIME_SETTING=$1");
        bw.newLine();
        bw.write("OK=\"1\"");
        bw.newLine();
        bw.write("if [ \"$TIME_SETTING\"  = \"\" ]; then");
		bw.newLine();
        bw.write("    echo \"Missing def of whether time is now or a specific time\"");
		bw.newLine();
        bw.write("    echo \"on form $TIME_SETTING | <DD-MM-YYYY>\"");
		bw.newLine();
        bw.write("    OK=\"0\"");
		bw.newLine();
        bw.write("fi");
		bw.newLine();
        bw.write("if [ \"$OK\"  = \"1\" ]; then");
		bw.newLine();
		
        for (HadoopResItem item : itSet) {
        	String param = " jdbcUser=" + jdbcUser
        			+ " table=" + item.tablename()
        			+ " IADATA=" + iaData
        			+ " " + datadir + "/" + item.datasubdirname()
        			+ " checkDoublets=" + (item.emptytable ? "false" : "true")
        			+ " listIgnored="+ (item.emptytable ? "false" : "true")
					+ " ignoreFile="
        			      + (ignoreFile.equals(IgnoreFile.if_true) 
        			          ? "true" 
        			          : (ignoreFile.equals(IgnoreFile.if_false) ? "false" : "warning"));
	        bw.write("    echo \"" + item.dataresfile.getAbsolutePath() + "\"");
			bw.newLine();
			bw.write("    bash db_multiingest_data.sh $TIME_SETTING" + param );
			bw.newLine();
        }
        bw.write("fi");
		bw.newLine();
		bw.newLine();
		bw.close();
		return ok;
	};

	private static boolean javaMultiIngestCommadfile(HadoopResItem item, Connection conn, String iaData, IgnoreFile ignoreFile) throws  InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, IOException {
		boolean ok = true;
    	if (item.datasubdir.exists()) {
            if (item.dataresfile.exists()) {
    			if (ignoreFile.equals(IgnoreFile.if_false)) {
    	            System.err.println("ERROR: copy-here file allready existed " + item.dataresfile.getAbsolutePath());
    	            System.exit(1);
    			} else if (ignoreFile.equals(IgnoreFile.if_warning)) {
    	            System.out.println("WARNING: copy-here file allready existed " + item.dataresfile.getAbsolutePath());
    			} 
    		} 
            item.dataresfile.createNewFile();
            System.out.println("--- ingest: " + item.dataresfile.getAbsoluteFile());
        	FileWriter fw = new FileWriter(item.dataresfile.getAbsoluteFile());
            BufferedWriter resfile = new BufferedWriter(fw);  
            resfile.write("Running WebdanicaJobs - MysqlIngester from workflow");
            resfile.newLine();
            resfile.close();
        	File[] dirfiles = item.datasubdir.listFiles();
			List<File> sortedFiles = new ArrayList<File>(dirfiles.length);
            for (File f : dirfiles) {
            	sortedFiles.add(f);
            }
	        Collections.sort(sortedFiles);
            for (File f : sortedFiles) {
            	if (MysqlX.isPartfile(f.getName())) {
            		boolean iadatabool = iaData.equals("true"); 
            		boolean checkDoublets = (!item.emptytable) || (!iadatabool);
                	ok = ok && MysqlIngester.ingest(conn, f, item, item.tablename(), iadatabool, checkDoublets, checkDoublets);
            	}
            }
        } else {
        	ok = false;
        }
        return ok;
	};

	private static boolean javaMultiUpdateCommadfile(HadoopResItem item, Connection conn, IgnoreFile ignoreFile) throws  InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, IOException {
		boolean ok = true;
    	if (item.datasubdir.exists()) {
            if (item.dataresfile.exists()) {
    			if (ignoreFile.equals(IgnoreFile.if_false)) {
    	            System.err.println("ERROR: copy-here file allready existed " + item.dataresfile.getAbsolutePath());
    	            System.exit(1);
    			} else if (ignoreFile.equals(IgnoreFile.if_warning)) {
    	            System.out.println("WARNING: copy-here file allready existed " + item.dataresfile.getAbsolutePath());
    			} 
    		} 
            item.dataresfile.createNewFile();
            System.out.println("--- update: " + item.dataresfile.getAbsoluteFile());
        	FileWriter fw = new FileWriter(item.dataresfile.getAbsoluteFile());
            BufferedWriter resfile = new BufferedWriter(fw);  
            resfile.write("Running WebdanicaJobs - MysqlUpdateNewHadoop from workflow");
            resfile.newLine();
            resfile.close();
        	File[] dirfiles = item.datasubdir.listFiles();
			List<File> sortedFiles = new ArrayList<File>(dirfiles.length);
            for (File f : dirfiles) {
            	sortedFiles.add(f);
            }
	        Collections.sort(sortedFiles);
	        
	        
        	// find tables to be updated
            Set<String> updTableSet = findUpdateTables(conn, item);

            // update tables
			for (File f : sortedFiles) {
            	if (MysqlX.isPartfile(f.getName())) {
        			ok = ok && MysqlUpdateNewHadoop.updateHadoop(conn, f, item, updTableSet);
        		}
        	}
        } else {
        	ok = false;
        }
        return ok;
	};

	private static Set<String> findUpdateTables(Connection conn, HadoopResItem item) throws SQLException {
		Set<String> updTableSet = new HashSet<String>();
        Set<String> allTableSet = MysqlRes.getTables(conn);

        if (item.interval.isEmpty() || item.hadoop_version.isEmpty()) {
        	if (allTableSet.contains(item.tablename())) {
        		updTableSet.add(item.tablename());
        	}
	    } else {
	    	/* extend for intervals */
	        String tablePrefix = 
	        		MysqlRes.wf_table_prefix +
	        		item.getBasicName(
	        			MysqlWorkFlow.wf_table_delim, 
	        			ItemNameType.inteval_item, 
	        			ItemNameVersion.V1
	        		).replace(item.interval, "");

	        for (String t: allTableSet) {
	        	if (t.startsWith(tablePrefix)) {
	        		updTableSet.add(t);
	        	}
	        }
        }
		return updTableSet;
    }
    

	private static boolean createIngestCommadfile(HadoopResItem item, Set<String> filenameSet, File writeFile, String jdbcUser, String iaData, boolean checkDoublets, String datadir, IgnoreFile ignoreFile) throws IOException {
		boolean ok = true;
		if (writeFile.exists()) {
			if (ignoreFile.equals(IgnoreFile.if_false)) {
	            System.err.println("ERROR: ingest cmd file allready existed " + writeFile.getAbsolutePath());
	            System.exit(1);
			} else if (ignoreFile.equals(IgnoreFile.if_warning)) {
	            System.out.println("WARNING: ingest cmd file allready existed " + writeFile.getAbsolutePath());
			} 
		} 
		writeFile.createNewFile();
		
		FileWriter fw = new FileWriter(writeFile.getAbsoluteFile());
        BufferedWriter  bw = new BufferedWriter(fw);        

        bw.write("#/bin/bash");
        bw.newLine();
        bw.write("TIME_SETTING=$1");
        bw.newLine();
        bw.write("OK=\"1\"");
        bw.newLine();
        bw.write("if [ \"$TIME_SETTING\"  = \"\" ]; then");
		bw.newLine();
        bw.write("    echo \"Missing def of whether time is now or a specific time\"");
		bw.newLine();
        bw.write("    echo \"on form $TIME_SETTING | <DD-MM-YYYY>\"");
		bw.newLine();
        bw.write("    OK=\"0\"");
		bw.newLine();
        bw.write("fi");
		bw.newLine();
        bw.write("if [ \"$OK\"  = \"1\" ]; then");
		bw.newLine();

    	for (String f: filenameSet) {
    		String param = " jdbcUser=" + jdbcUser
    			+ " table=" + item.tablename()
    			+ " IADATA=" + iaData
    			+ " " + datadir + "/" + item.datasubdirname() + "/" + f
    			+ " checkDoublets=" + (checkDoublets ? "true" : "false")
    			+ " listIgnored="+ (checkDoublets ? "true" : "false")
				+ " ignoreFile="
        			      + (ignoreFile.equals(IgnoreFile.if_true) 
        			          ? "true" 
        			          : (ignoreFile.equals(IgnoreFile.if_false) ? "false" : "warning"));
    			
    		String resfile = item.dataresfile.getAbsolutePath() + "-" +  f + wf_resfilename_suffix; 
	    	bw.write("    echo \"" + resfile + "\"");
			bw.newLine();
			bw.write("    bash db_ingest_data.sh $TIME_SETTING" + param );
			bw.newLine();
    	}
		bw.write("fi");
		bw.newLine();
		bw.newLine();
		bw.close();
		return ok;
	};

	private static boolean createUpdatehadoopCommadfile(HadoopResItem item, Set<String> filenameSet, File writeFile, String jdbcUser, String datadir, IgnoreFile ignoreFile) throws IOException {
		boolean ok = true;
		if (writeFile.exists()) {
			if (ignoreFile.equals(IgnoreFile.if_false)) {
	            System.err.println("ERROR: updatehadoop cmd file allready existed " + writeFile.getAbsolutePath());
	            System.exit(1);
			} else if (ignoreFile.equals(IgnoreFile.if_warning)) {
	            System.out.println("WARNING: updatehadoop cmd file allready existed " + writeFile.getAbsolutePath());
			} 
		} 
		writeFile.createNewFile();
		
		FileWriter fw = new FileWriter(writeFile.getAbsoluteFile());
        BufferedWriter  bw = new BufferedWriter(fw);        

        bw.write("#/bin/bash");
        bw.newLine();
        bw.write("TIME_SETTING=$1");
        bw.newLine();
        bw.write("OK=\"1\"");
        bw.newLine();
        bw.write("if [ \"$TIME_SETTING\"  = \"\" ]; then");
		bw.newLine();
        bw.write("    echo \"Missing def of whether time is now or a specific time\"");
		bw.newLine();
        bw.write("    echo \"on form $TIME_SETTING | <DD-MM-YYYY>\"");
		bw.newLine();
        bw.write("    OK=\"0\"");
		bw.newLine();
        bw.write("fi");
		bw.newLine();
        bw.write("if [ \"$OK\"  = \"1\" ]; then");
		bw.newLine();

    	for (String f: filenameSet) {
    		String param = " jdbcUser=" + jdbcUser
    			+ " " + datadir + "/" + item.datasubdirname() + "/" + f
				+ " ignoreFile="
        			      + (ignoreFile.equals(IgnoreFile.if_true) 
        			          ? "true" 
        			          : (ignoreFile.equals(IgnoreFile.if_false) ? "false" : "warning"));
    			
    		String resfile = item.dataresfile.getAbsolutePath() + "-" +  f + wf_resfilename_suffix; 
	    	bw.write("    echo \"" + resfile + "\"");
			bw.newLine();
			bw.write("    bash db_newupdate_data.sh $TIME_SETTING" + param );
			bw.newLine();
    	}
		bw.write("fi");
		bw.newLine();
		bw.newLine();
		bw.close();
		return ok;
	};

	private static boolean createCompressCommadfile(HadoopResItem item, File writeFile, IgnoreFile ignoreFile) throws IOException {
		boolean ok = true;
		if (writeFile.exists()) {
			if (ignoreFile.equals(IgnoreFile.if_false)) {
	            System.err.println("ERROR: compress cmd file allready existed " + writeFile.getAbsolutePath());
	            System.exit(1);
			} else if (ignoreFile.equals(IgnoreFile.if_warning)) {
	            System.out.println("WARNING: compress cmd file allready existed " + writeFile.getAbsolutePath());
			} 
		} 
		writeFile.createNewFile();
		
		FileWriter fw = new FileWriter(writeFile.getAbsoluteFile());
        BufferedWriter  bw = new BufferedWriter(fw);        

        bw.write("#/bin/bash");
        bw.newLine();
        bw.write("gzip " +  item.datasubdir.getAbsolutePath() + "/part*");
        bw.newLine();
        bw.write("echo \" \" > " +  item.datasubdir.getAbsolutePath() + "/" + wf_donecompressfilename );
		bw.newLine();
		bw.close();
		return ok;
	};
	
	public static HadoopResItem readItemFromIngestFile(File ingestFile,String dbMachine, String hadoopdir) {
        String filepath = ingestFile.getAbsolutePath();
    	int pos = filepath.lastIndexOf("/");
    	if (pos == filepath.length()-1) { 
    		filepath = filepath.substring(0,pos);
    		pos = filepath.lastIndexOf("/");
    	} 
    	//System.out.println("path filepath.substring(0,pos) - " + filepath.substring(0,pos));
    	return readItemFromDataSubdirname(filepath.substring(0,pos), dbMachine, hadoopdir);
	}
	
	public static HadoopResItem readItemFromDataSubdirname(String subdirpath,String dbMachine, String hadoopdir) {
    	//System.out.println("subdirpath - " + subdirpath);
    	int pos = subdirpath.lastIndexOf("/");
    	if (pos == subdirpath.length()-1) { 
    		subdirpath = subdirpath.substring(0,pos);
    		pos = subdirpath.lastIndexOf("/");
    	} 
    	String datapath = subdirpath.substring(0,pos);
    	HadoopResItem tmp = new HadoopResItem("", "", "", "");
    	tmp.hadoop_dir = hadoopdir;
        tmp.set_nos_from_name( subdirpath.substring(pos+1), MysqlWorkFlow.wf_dir_delim);
        
    	HadoopResItem item = new HadoopResItem(
    			datapath, tmp.datasubdirname(), 
    			tmp.resfilename(),
    			tmp.compressfilename()
    			);
    	item.dbmachine = dbMachine;
    	item.hadoop_dir = tmp.hadoop_dir;
    	item.machine_no = tmp.machine_no ;
    	item.diskTld_no = tmp.diskTld_no;
    	item.part_no = tmp.part_no;
    	item.subpart = tmp.subpart;
    	item.interval = tmp.interval;
    	item.hadoop_version = tmp.hadoop_version;
        
    	return item;
	}

	public static HadoopResItem readItemFromTablename(String tablename, String resultdir, String dbMachine, String hadoopdir) {
    	HadoopResItem tmp = new HadoopResItem("", "", "", "");
        tmp.hadoop_dir = hadoopdir;
        
        if (tablename.startsWith(MysqlRes.wf_table_prefix)) {
        	tmp.set_nos_from_name( tablename.substring(MysqlRes.wf_table_prefix.length()), wf_table_delim);
    	}
        
    	HadoopResItem item = new HadoopResItem(
    			resultdir, tmp.datasubdirname(), 
    			tmp.resfilename(),
    			tmp.compressfilename()
    			);
    	item.dbmachine = dbMachine;
    	item.hadoop_dir = tmp.hadoop_dir;
    	item.machine_no = tmp.machine_no ;
    	item.diskTld_no = tmp.diskTld_no;
    	item.part_no = tmp.part_no;
    	item.subpart = tmp.subpart;
    	item.interval = tmp.interval;
    	item.hadoop_version = tmp.hadoop_version;

    	return item;
	}

	public static Set<HadoopResItem> readItemsFromDefFile( File defFile, String datapath, String dbMachine) throws IOException {
		Set<HadoopResItem> itemSet = new HashSet<HadoopResItem>();
		
		BufferedReader fr = new BufferedReader(new FileReader(defFile));        
	    String line;
            
        while ((line = fr.readLine()) != null) {
        	line = line.trim();
        	HadoopResItem item = new HadoopResItem("", "", "", "");
        	
            if (!line.isEmpty()) {
            	HadoopResItem tmp = new HadoopResItem("", "", "", "");
                String[] parts = line.split(",");
                if ((parts.length !=5) && (parts.length !=6)) {
                    System.err.println("ERROR: Invalid format of def-line - got '" + line + "'");
                    System.exit(1);
                }
                
                //hadoop dir
            	tmp.hadoop_dir = parts[0].trim();
            	tmp.hadoop_dir = ( tmp.hadoop_dir.startsWith("hadoop@") || tmp.hadoop_dir.startsWith("test@")
            			            ? "" 
            			            :  "hadoop@") 
            			         + tmp.hadoop_dir;

                //m00x
                String s = parts[1].trim();
                if (s.startsWith("m")) {
                	s = s.substring(1);
                 	if (MysqlX.isNumeric(s)) {
                 		tmp.machine_no = Integer.parseInt(s);
                	} else {
                        System.err.println("ERROR: Invalid format of machine part in def-line - got '" + line + "'");
                        System.exit(1);
                	} 
                }

                //dx
                s = parts[2].trim();
                if (s.startsWith("disk")) {
                	s = s.substring(4).trim();
                	if (MysqlX.isNumeric(s)) {
                 		tmp.diskTld_no = s;
                	} else {
                        System.err.println("ERROR: Invalid format of disk part in def-line - got '" + line + "'");
                        System.exit(1);
                	} 
                } else if (s.startsWith("tld-")) {
                	s = s.substring(4).trim();
                 	if (!s.isEmpty()) {
                 		tmp.diskTld_no = s;
                	} else {
                        System.err.println("ERROR: Invalid format of tld part in def-line - got '" + line + "'");
                        System.exit(1);
                	} 
                } 

                //partno
                s = parts[3].trim();
                if (!s.isEmpty()) {
                	tmp.part_no = parts[3].trim();
            	} else {
                    System.err.println("ERROR: Invalid format of disk part in def-line - got '" + line + "'");
                    System.exit(1);
            	}                 
         		
                //part subno
                String l = parts[4].trim();
                int pos = l.indexOf("-");
                if(pos>0) {
                	tmp.subpart = l.substring(pos+1);
                } else { 
                    if(l.startsWith(wf_interval_prefix)) {
                    	tmp.interval = l.substring(1);
                    } else if(l.startsWith(wf_version_prefix + "5")) {
                    	tmp.hadoop_version = parts[4].trim();
                    	tmp.interval = "99";
                    } else {
                        System.err.println("ERROR: Invalid format of last part in def-line - got '" + line + "'");
                        System.exit(1);
                    }
                }
                
                if (parts.length==6) {
                	tmp.hadoop_version = parts[5].trim();
                }

                if (datapath.isEmpty()) {
                	item = tmp;
                } else {
                	item = new HadoopResItem(
                			datapath , tmp.datasubdirname(), 
                			tmp.resfilename(),
                			tmp.compressfilename()
                	);
                }
            	item.dbmachine = dbMachine;
            	item.hadoop_dir = tmp.hadoop_dir;
            	item.machine_no = tmp.machine_no ;
            	item.diskTld_no = tmp.diskTld_no;
            	item.part_no = tmp.part_no;
            	item.subpart = tmp.subpart;
            	item.interval = tmp.interval;
            	item.hadoop_version = tmp.hadoop_version;

            	item.defFileLine = line;
            	//System.out.println("item.line read: " + item.defFileLine);
            	itemSet.add(item);
            }  // if line not empty  
        }  // while more lines  
        fr.close();
	    return itemSet;
	}

	
	//def file has elements on form:
	//hadoop@kb-test-sol-001:/home/hadoop/disk7_instans_m004/combo-r1-m001-disk3_3-3-2-29-08-2014/,m001,disk3,3,3-2  or
	//hadoop@kb-test-sol-001:/home/hadoop/disk7_instans_m004/combo-r1-m001-disk3_3-3-2-29-08-2014/,m001,disk3,3,i00
    public static class HadoopResItem{
    	public String hadoop_dir = ""; 
    	public int machine_no = 0; 
    	public String diskTld_no = ""; 
    	public String part_no = ""; 
    	public String subpart = ""; 
    	public String interval = ""; 
    	public String hadoop_version = ""; 
    	public File datadir; 
    	public File datasubdir; 
    	public File dataresfile; 
    	public File datacompresfile; 
    	public boolean copyok = false;
    	public String extrafiles = "";
    	public boolean emptytable = true;
        public String dbmachine = "";
        public int fileCnt = 0;
        public int maxPartNo =0;
        public boolean hasTable = false; //only used for all status 
        public boolean hasDir = false; //only used for all status 
    	public boolean emptydatadir = true;
        public boolean h_hasDir = false; //only used for all status 
    	public boolean h_emptydatadir = true;
        public boolean copiedOk=false;
        public boolean copyherefileProduced=false;
        public boolean copybackfileProduced=false;
        public boolean compressedfileProduced=false;
        public String backedup="";
        public boolean h_copiedOk=false;
        public boolean h_copyherefileProduced=false;
        public boolean h_copybackfileProduced=false;
        public boolean h_compressedfileProduced=false;
        public String h_backedup="";
        public boolean hadoopfileProduced=false;
        public boolean ingestfileProduced=false;
        public boolean berDbUpdatefileProduced=false;
//        public boolean hadoopCheckedNewUpdate=false;
        public boolean statProduced=false;
        public boolean updatedProduced=false;
        public int statVersion=0;
        public int updateVersion=0;
        public String statusDate="";
        public boolean IAdata=false;
        public String defFileLine=""; //only for V5
        public List<Boolean> urlLevelExtracted = new ArrayList<Boolean>(MysqlX.noDomainLevels);
        public List<Boolean> urlLevelNewExtracted = new ArrayList<Boolean>(MysqlX.noDomainLevels);
        public List<Boolean> urlLevelDbSearched = new ArrayList<Boolean>(MysqlX.noDomainLevels);

        public HadoopResItem() {
        	for (int i=0; i < MysqlX.noDomainLevels; i++) {
        		urlLevelExtracted.add(i,false);
        		urlLevelNewExtracted.add(i,false);
        		urlLevelDbSearched.add(i,false);
        	}
        }

        public HadoopResItem(String statusLine) {
        	String [] partsHd = getItemStatusItemHeader().split(MysqlX.row_delim);
        	String [] parts = statusLine.split(MysqlX.row_delim);
        	
        	if (parts.length != partsHd.length) {
        		System.err.println("partsHd.length " + partsHd.length);
        		System.err.println("partsHd '" + getItemStatusItemHeader() + "'");
                System.err.println("ERROR: unrecognized line (not " + partsHd.length + " parts) in status file: ");
        		System.err.println("partsHd '" + getItemStatusItemHeader() + "'");
                System.err.println( "'" + statusLine + "'");
                System.exit(1);
        	} else {
        		machine_no = Integer.parseInt(parts[0]); 
				diskTld_no = parts[1].trim(); 
				part_no = parts[2].trim(); 
				String p = parts[3];
				if (p.startsWith(wf_interval_prefix)) interval = p.substring(1);
				else subpart = p;
            	hadoop_version =  parts[4];
				dbmachine = parts[5]; 
				hasTable = parts[6].equals("true"); 
				emptytable = parts[7].equals("true"); 
				hasDir = parts[8].equals("true"); 
				emptydatadir = parts[9].equals("true"); 
				h_hasDir = parts[10].equals("true"); 
				h_emptydatadir = parts[11].equals("true"); 
				extrafiles = parts[12]; 
				hadoopfileProduced = parts[13].equals("true"); 
				ingestfileProduced = parts[14].equals("true"); 
				updatedProduced = parts[15].equals("true"); 
				updateVersion = Integer.parseInt(parts[16]); 
				copiedOk = parts[17].equals("true"); 
				copyherefileProduced = parts[18].equals("true"); 
				copybackfileProduced = parts[19].equals("true"); 
				compressedfileProduced = parts[20].equals("true");
				backedup =  parts[21].trim();
				h_copiedOk = parts[22].equals("true"); 
				h_copyherefileProduced = parts[23].equals("true"); 
				h_copybackfileProduced = parts[24].equals("true"); 
				h_compressedfileProduced = parts[25].equals("true");  
				h_backedup =  parts[26].trim();
				//hadoopCheckedNewUpdate = parts[25].equals("true");
				berDbUpdatefileProduced = parts[27].equals("true");
				statProduced = parts[28].equals("true"); 
				statVersion = Integer.parseInt(parts[29]);
				statusDate = parts[30];

				for (int i=0; i < MysqlX.noDomainLevels; i++) {
	        		urlLevelExtracted.add(false);
	        		urlLevelNewExtracted.add(false);
	        		urlLevelDbSearched.add(false);
	        	}
	        	String[] ue_parts = parts[31].split(",");
	        	String[] uen_parts = parts[32].split(",");
	        	String[] us_parts = parts[33].split(",");

	        	for (int i=0; i < MysqlX.noDomainLevels; i++) {
	        		urlLevelExtracted.set(i, ue_parts[i].equals("true"));
	        		urlLevelNewExtracted.set(i, uen_parts[i].equals("true"));
	        		urlLevelDbSearched.set(i, us_parts[i].equals("true"));
	        	}
        	}
        }

        public HadoopResItem(String datadirTxt, String datasubdirTxt, String resfilename, String compressfilename) {
    		datadir = new File(datadirTxt);
    		datasubdir = new File(datadirTxt + "/" + datasubdirTxt); 
        	dataresfile = new File(datadirTxt + "/" + datasubdirTxt + "/" + resfilename);
        	datacompresfile = new File(datadirTxt + "/" + datasubdirTxt + "/" + compressfilename);
        	for (int i=0; i < MysqlX.noDomainLevels; i++) {
        		urlLevelExtracted.add(false);
        		urlLevelNewExtracted.add(false);
        		urlLevelDbSearched.add(false);
        	}
        }

        public boolean allOk(String hadoop_version, int update_version, int domLevels, ExtractDbOperation op) {
        	boolean ok = true;
        	boolean checkV5 = hadoop_version.equals("V5") || hadoop_version.equals("v5");
        	if ((!checkV5) && (!hadoop_version.isEmpty())) {
        		System.out.println("ERROR: Unknown hadoop version: " + hadoop_version);
                System.exit(1);
        	}
            //System.out.println("checkV5: " + checkV5);
            //System.out.println("copiedOk: " + copiedOk);

        	ok = ok && hasTable 
	        		&& (!emptytable) 
	        		&& hasDir 
	        		&& (!emptydatadir) 
	        		&& copiedOk
	        		&& copyherefileProduced
	        		&& (copybackfileProduced || (!backedup.isEmpty())) 
	        		&& compressedfileProduced
	        		&& ingestfileProduced
	        		&& (updateVersion == update_version);
	        if (checkV5) {
		         ok = ok && hadoop_version.equals("v5")
		        		 && h_hasDir		        		
		        		 && (!h_emptydatadir)
		        		 && h_copiedOk
		        		 && h_copyherefileProduced
		        		 && (h_copybackfileProduced || (!h_backedup.isEmpty())) 
		        		 && h_compressedfileProduced
			        	 && hadoopfileProduced;
	        }
    		//&& berDbUpdatefileProduced
	        if (op.equals(ExtractDbOperation.mysql) || op.equals(ExtractDbOperation.all)) {
	        	for (int i=0; i < domLevels; i++) {
	        		ok = ok && urlLevelExtracted.get(i);
	        	}
	        }
	        if (op.equals(ExtractDbOperation.mysql) || op.equals(ExtractDbOperation.all)) {
	        	for (int i=0; i < domLevels; i++) {
	        		ok = ok && urlLevelNewExtracted.get(i);
	        	}
	        }
	        if (op.equals(ExtractDbOperation.berkeley) || op.equals(ExtractDbOperation.all)) {
	        	for (int i=0; i < domLevels; i++) {
	        		ok = ok && urlLevelDbSearched.get(i);
	        	}
	        }
	        return ok;
        }
	        		

        
        public void copy(HadoopResItem hri) {
    		machine_no = hri.machine_no; 
			diskTld_no = hri.diskTld_no; 
			part_no = hri.part_no; 
			interval = hri.interval;
			subpart = hri.subpart; 
        	hadoop_version = hri.hadoop_version; 
			dbmachine = hri.dbmachine; 
			hasTable = hri.hasTable;  
			emptytable = hri.emptytable;  
			hasDir = hri.hasDir;  
			emptydatadir = hri.emptydatadir;  
			h_hasDir = hri.h_hasDir;  
			h_emptydatadir = hri.h_emptydatadir;  
			extrafiles = hri.extrafiles;  
			hadoopfileProduced = hri.hadoopfileProduced;  
			ingestfileProduced = hri.ingestfileProduced;  
			updatedProduced = hri.updatedProduced;  
			updateVersion = hri.updateVersion;  
			copiedOk = hri.copiedOk;  
			copyherefileProduced = hri.copyherefileProduced;  
			copybackfileProduced = hri.copybackfileProduced;  
			compressedfileProduced = hri.compressedfileProduced;   
			backedup = hri.backedup;   
			h_copiedOk = hri.h_copiedOk;  
			h_copyherefileProduced = hri.h_copyherefileProduced;  
			h_copybackfileProduced = hri.h_copybackfileProduced;  
			h_compressedfileProduced = hri.h_compressedfileProduced;   
			h_backedup = hri.h_backedup;   
			//hadoopCheckedNewUpdate = hri.hadoopCheckedNewUpdate; 
			berDbUpdatefileProduced = hri.berDbUpdatefileProduced; 
			statProduced = hri.statProduced;  
			statVersion = hri.statVersion; 
			statusDate = hri.statusDate;         	
    		urlLevelExtracted = hri.urlLevelExtracted;
    		urlLevelNewExtracted = hri.urlLevelNewExtracted;
    		urlLevelDbSearched = hri.urlLevelDbSearched;
        }
    	public String tablename () {
    		return MysqlRes.wf_table_prefix + getname(wf_table_delim); 
    	}
    	
    	public String datasubdirname () {
    		return getname(wf_dir_delim); 
    	}

    	public String resfilename () {
    		return wf_resfilename_prefix + getname(wf_dir_delim) + wf_resfilename_suffix; 
    	}

    	public String copyfilename () {
    		return wf_copyherefilename_prefix + getname(wf_dir_delim) + wf_bash_suffix; 
    	}

    	public String compressfilename () {
    		return wf_compressfilename_prefix + getname(wf_dir_delim) + wf_bash_suffix; 
    	}

    	public String getname(String delim) { //int machine_no, int disk_no, String part, String interval_no, hadoop_version,String delim) {
        	//examples m1-d3-p3-21  m2-d4-p1-i00 
        	String s = wf_maschine_prefix + machine_no 
        		 + delim + wf_disk_prefix + diskTld_no
        		 + delim + wf_part_prefix + part_no
        		 + delim + (interval.isEmpty() ? subpart : wf_interval_prefix + interval)
        		 + (hadoop_version.isEmpty() || wf_table_delim.equals(delim) ? "" : delim + hadoop_version);
            return s;
        }

    	public String getBasicName(String delim, ItemNameType t, ItemNameVersion v) { //int machine_no, int disk_no, String part, String interval_no, hadoop_version,String delim) {
        	//examples m1-d3-p3-21  m2-d4-p1-i00 
        	String s = wf_maschine_prefix + machine_no 
        		 + delim + wf_disk_prefix + diskTld_no
        		 + delim + wf_part_prefix + part_no
        		 + delim + (interval.isEmpty() ? subpart : wf_interval_prefix + interval);
        	if (v.equals(ItemNameVersion.V5)) {
        		s = s + (hadoop_version.isEmpty() ? "" : delim + hadoop_version);
        	}
            return s;
        }

    	public void set_nos_from_name(String name, String delim) { //int machine_no, int disk_no, String part, String interval_no, hadoop_version, String delim) {
        	//examples m1-d3-p3-21  m2-d4-p1-i00
    		String s = name;
    		String[] parts = s.split(delim);
    		int pos = 0 ;

    		if (parts.length == 4 || parts.length == 5) {
	    		pos = parts[0].indexOf(wf_maschine_prefix);
	    		s = parts[0].substring(pos+1);
	    		if (MysqlX.isNumeric(s) && !s.isEmpty()) {
	    			machine_no = Integer.parseInt(s); 
	    		}
	    		
		    	pos = parts[1].indexOf(wf_disk_prefix);
		    	s = parts[1].substring(pos+1).trim();
	    		if (!s.isEmpty()) {
	    			diskTld_no = s; 
	    		}

		    	pos = parts[2].indexOf(wf_part_prefix);
	    		s = parts[2].substring(pos+1).trim();
	    		if (!s.isEmpty()) {
	    			part_no = s;
	    		}

	    		s = parts[3].trim();
	    		if (s.startsWith(wf_interval_prefix)) {
	    			interval = s.substring(1); 
	    		} else {
	    			subpart = s; 
	    		}
	    		
	    		if (parts.length == 5) {
	    			s = parts[4].trim();
	    			if (!s.isEmpty()) {
		    			hadoop_version= s;
		    		}
	    		}
    		}
        }

    	public void findCopyStatusPartFiles() {
        	if (datasubdir.exists()) {
	        	File[] dirfiles = datasubdir.listFiles();
	            for (File f : dirfiles) {
	            	if (MysqlX.isPartfile(f.getName())) {
	            		fileCnt++;
	    	        	int n = MysqlX.getPartno(f.getName());
	    	        	if (maxPartNo < n) maxPartNo = n;
	            	}
	            }
            } else {
            	maxPartNo = 0;
            	fileCnt = 0;
            }
        }

    	public String getItemStatusItemHeader() {
			return    "machine_no" + MysqlX.row_delim 
					+ "disk_no" + MysqlX.row_delim 
					+ "part_no" + MysqlX.row_delim 
					+ "subpart/interval" + MysqlX.row_delim 
					+ "hadoop_version" + MysqlX.row_delim 
					+ "db-machine" + MysqlX.row_delim 
					+ "hasTable" + MysqlX.row_delim 
					+ "emptytable" + MysqlX.row_delim 
					+ "hasDir" + MysqlX.row_delim 
					+ "emptydatadir" + MysqlX.row_delim 
					+ "h_hasDir" + MysqlX.row_delim 
					+ "h_emptydatadir" + MysqlX.row_delim 
					+ "extrafiles" + MysqlX.row_delim 
					+ "hadoopfileProduced" + MysqlX.row_delim
					+ "ingestfileProduced" + MysqlX.row_delim 
					+ "updatedProduced" + MysqlX.row_delim 
					+ "updateVersion" + MysqlX.row_delim 
					+ "copiedOk" + MysqlX.row_delim 
					+ "copyherefileProduced" + MysqlX.row_delim 
					+ "copybackfileProduced" + MysqlX.row_delim 
					+ "compressedfileProduced" + MysqlX.row_delim  
					+ "backedup" + MysqlX.row_delim  
					+ "h_copiedOk" + MysqlX.row_delim 
					+ "h_copyherefileProduced" + MysqlX.row_delim 
					+ "h_copybackfileProduced" + MysqlX.row_delim 
					+ "h_compressedfileProduced" + MysqlX.row_delim  
					+ "h_backedup" + MysqlX.row_delim  
					//+ "hadoopNewUpdate" + MysqlX.row_delim  
					+ "berDbUpdatefileProduced" + MysqlX.row_delim  
					+ "statProduced" + MysqlX.row_delim 
					+ "statVersion" + MysqlX.row_delim
					+ "date"+ MysqlX.row_delim
					+ "urlLevelExtracted" + MysqlX.row_delim
					+ "urlLevelNewExtracted" + MysqlX.row_delim
					+ "urlLevelDbSearched"; 
    	}
    	
    	public String getItemStatusFileHeader() {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        	Date now = new Date(System.currentTimeMillis());
        	return  /* 1 machine_no */ MysqlStatExtract.status_title_start + MysqlX.row_delim 
		    		/* 2 disk_no */ + dateFormat.format(now)+ MysqlX.row_delim 
		    		/* 3 part_no */  + MysqlX.row_delim 
					/* 4 subpart/interval */ + MysqlX.row_delim 
					/* 5 hadoop update */ + MysqlX.row_delim 
					/* 6 db-machine */ + MysqlX.row_delim 
					/* 7 hasTable */ + MysqlX.row_delim 
					/* 8 emptytable */ + MysqlX.row_delim 
					/* 9 hasDir */ + MysqlX.row_delim 
					/* 10 emptydatadir */ + MysqlX.row_delim 
					/* 11 h_hasDir */ + MysqlX.row_delim 
					/* 12 h_emptydatadir */ + MysqlX.row_delim 
					/* 13 extrafiles */ + MysqlX.row_delim 
					/* 14 hadoopfileProduced */ + MysqlX.row_delim 
					/* 15 ingestfileProduced */ + MysqlX.row_delim 
					/* 16 updatedProduced */ + MysqlX.row_delim 
					/* 17 updateVersion */ + MysqlX.row_delim 
					/* 18 copiedOk */ + MysqlX.row_delim 
					/* 19 copyherefileProduced */ + MysqlX.row_delim 
					/* 20 copybackfileProduced */ + MysqlX.row_delim 
					/* 21 compressedfileProduced */ + MysqlX.row_delim  
					/* 22 backedup */ + MysqlX.row_delim  
					/* 23 h_copiedOk */ + MysqlX.row_delim 
					/* 24 h_copyherefileProduced */ + MysqlX.row_delim 
					/* 25 h_copybackfileProduced */ + MysqlX.row_delim 
					/* 26 h_compressedfileProduced */ + MysqlX.row_delim  
					/* 27 h_backedup */ + MysqlX.row_delim  
					//	/* 26 hadoopNewUpdate */ + MysqlX.row_delim  
					/* 28 berDbUpdatefileProduced */ + MysqlX.row_delim  
					/* 29 statProduced */ + MysqlX.row_delim 
					/* 30 statVersion */ + MysqlX.row_delim
					/* 31 date */ + MysqlX.row_delim
					/* 32 urlLevelExtracted */ + MysqlX.row_delim
					/* 33 urlLevelNewExtracted */ + MysqlX.row_delim
					/* 34 urlLevelDbSearched */;
    	}

    	public String getItemStatusLine() {
        	return 
        	machine_no + MysqlX.row_delim 
			+ diskTld_no + MysqlX.row_delim 
			+ part_no + MysqlX.row_delim 
			+ (interval.isEmpty() ? subpart : wf_interval_prefix + interval) + MysqlX.row_delim 
			+ hadoop_version + MysqlX.row_delim 
			+ dbmachine + MysqlX.row_delim 
			+ hasTable + MysqlX.row_delim 
			+ emptytable + MysqlX.row_delim 
			+ hasDir + MysqlX.row_delim 
			+ emptydatadir + MysqlX.row_delim 
			+ h_hasDir + MysqlX.row_delim 
			+ h_emptydatadir + MysqlX.row_delim 
			+ extrafiles + MysqlX.row_delim 
			+ hadoopfileProduced + MysqlX.row_delim 
			+ ingestfileProduced + MysqlX.row_delim 
			+ updatedProduced + MysqlX.row_delim 
			+ updateVersion + MysqlX.row_delim 
			+ copiedOk + MysqlX.row_delim 
			+ copyherefileProduced + MysqlX.row_delim 
			+ copybackfileProduced + MysqlX.row_delim 
			+ compressedfileProduced + MysqlX.row_delim  
			+ backedup + MysqlX.row_delim  
			+ h_copiedOk + MysqlX.row_delim 
			+ h_copyherefileProduced + MysqlX.row_delim 
			+ h_copybackfileProduced + MysqlX.row_delim 
			+ h_compressedfileProduced + MysqlX.row_delim  
			+ h_backedup + MysqlX.row_delim  
			//+ hadoopCheckedNewUpdate + MysqlX.row_delim  
			+ berDbUpdatefileProduced + MysqlX.row_delim  
			+ statProduced + MysqlX.row_delim 
			+ statVersion + MysqlX.row_delim
			+ statusDate + MysqlX.row_delim
			+ MysqlX.getBooleanSequence(urlLevelExtracted, ",")  + MysqlX.row_delim
			+ MysqlX.getBooleanSequence(urlLevelNewExtracted, ",")  + MysqlX.row_delim
			+ MysqlX.getBooleanSequence(urlLevelDbSearched, ",");
    	}
    }
    
    public static class ItemStatusInf{
    	public Set<HadoopResItem> copy_failed_Set = new HashSet<HadoopResItem>();
    	public Set<HadoopResItem> copied_here_Set = new HashSet<HadoopResItem>();
    	public Set<HadoopResItem> copyfile_produced_Set = new HashSet<HadoopResItem>();
    	public Set<HadoopResItem> copyfile_returned_Set = new HashSet<HadoopResItem>();
    	public Set<HadoopResItem> hadoop2_Set = new HashSet<HadoopResItem>(); //after check hadoop
    	public Set<HadoopResItem> ingested_Set = new HashSet<HadoopResItem>(); //after check ingest
    	public Set<HadoopResItem> stat_Set = new HashSet<HadoopResItem>(); 
    	public Set<HadoopResItem> compress_Set = new HashSet<HadoopResItem>();
    	public Set<HadoopResItem> berDb_Set = new HashSet<HadoopResItem>();
    	public Set<HadoopResItem> hadoopUpdate_Set = new HashSet<HadoopResItem>();
    	public Set<HadoopResItem> ok_for_hadoopUpdate_Set = new HashSet<HadoopResItem>();
    	public Set<HadoopResItem> ok_for_ingest_Set = new HashSet<HadoopResItem>();
    	public Set<HadoopResItem> ok_for_compress_Set = new HashSet<HadoopResItem>();
    	public Set<HadoopResItem> ok_for_stat_Set = new HashSet<HadoopResItem>();
    	public Set<HadoopResItem> followup_Set = new HashSet<HadoopResItem>(); //inkonsistens in files and status eg. lagkage but no success
    	public Set<HadoopResItem> error_Set = new HashSet<HadoopResItem>(); //server inkonsistens in files and status
    	public Set<HadoopResItem> nothing_Set = new HashSet<HadoopResItem>(); //nothing done

    	private void init() {
        	copy_failed_Set.clear(); //No hadoop success file 
        	copied_here_Set.clear(); //No copied file 
        	copyfile_produced_Set.clear(); //copied file 
        	copyfile_returned_Set.clear(); //copiedback file
        	ingested_Set.clear(); //ingested file
        	hadoop2_Set.clear();
        	stat_Set.clear(); //e.g. lagkaged file
        	compress_Set.clear(); //compress file
        	ok_for_hadoopUpdate_Set.clear();
        	ok_for_ingest_Set.clear(); //success file ok copy ok
        	ok_for_compress_Set.clear(); //ingested and no inconsistancies
        	ok_for_stat_Set.clear(); //ingested and no inconsistancies
        	followup_Set.clear(); //inconsistancies (excl. copy failed)
        	error_Set.clear();    //inconsistancies suggeting errors
        	nothing_Set.clear();  // not even dir exists
    	}

    	class ItemPair {
    		boolean extra = false;
    		HadoopResItem origItem;
    		HadoopResItem extraHadoopItem;
    	}
    	
    	public Map<String,HadoopResItem> mergeStatusItemSetsInternal(Set<HadoopResItem> itemSet) {
        	Map<String,HadoopResItem> itemMap = new HashMap<String,HadoopResItem>();
        	for (HadoopResItem it : itemSet) {
        		itemMap.put(it.getname(wf_dir_delim), it);
        	}
        	
        	//Find hadoop pairs
        	System.out.println("Find hadoop pairs");
        	Map<String,ItemPair> itemPairMap = new HashMap<String,ItemPair>();
        	for (HadoopResItem it : itemSet) {
        		if (it.hadoop_version.isEmpty()) {
            		String name = it.getname(wf_dir_delim);
        			ItemPair pair = new ItemPair();
        			pair.origItem = it;
        			itemPairMap.put(name, pair);
                //	System.out.println("pair head: " + name);
        		//} else {
                //	System.out.println("not yet paired: " + it.getname(wf_dir_delim) + " with version: " + it.hadoop_version);
        		}
        	}
        	
        	for (HadoopResItem it : itemSet) {
        		if (!it.hadoop_version.isEmpty()) {
            		String non_h_name = it.getBasicName(wf_dir_delim, ItemNameType.normal_item, ItemNameVersion.V1);

            		int pos = non_h_name.indexOf(wf_interval_prefix + "99");
                	if (pos>0) {
                		System.out.println("Interval: " + non_h_name);
                		String tablePrefix = non_h_name.substring(0,pos+1);
                		System.out.println("tablePrefix : " + tablePrefix);
                		boolean found = false;
                		for (int i = 0; i<=99; i++) {
                			String t = tablePrefix + (i<10 ? "0" : "") + i;
	                		System.out.println("table : " + t);
    	            		if (itemMap.containsKey(t)) {
    	            			found = true;
    		            		ItemPair pair = itemPairMap.get(t);
    		            		pair.extra = true;
    		        			pair.extraHadoopItem = it;
    	                		System.out.println("paired with : " + t);
    	            		} else {
                    			break;
    	            		}
                		}
                		if (!found) {
	                        System.err.println("V5: " + it.getname(wf_dir_delim) + " with no matching " + non_h_name);
	                        System.exit(1);
                		}
            		} else {
	            		if (!itemMap.containsKey(non_h_name)) {
	                        System.err.println("V5: " + it.getname(wf_dir_delim) + " with no matching " + non_h_name);
	                        System.exit(1);
	            		}
	            		ItemPair pair = itemPairMap.get(non_h_name);
	            		pair.extra = true;
	        			pair.extraHadoopItem = it;
            		}
        		}
        	}
        	
        	/*for (String s: itemPairMap.keySet()) {
        		ItemPair pair = itemPairMap.get(s);
            	System.out.println("pair orig: " + pair.origItem.getname(wf_dir_delim));
            	System.out.println("pair extraHadoopItem: " + (pair.extra ? pair.origItem.getname(wf_dir_delim) : "none"));
        	}*/
        	
        	//Merged hadoop pairs
        	Map<String,HadoopResItem> mergedItemMap = new HashMap<String,HadoopResItem>();
        	
        	
        	for (String key : itemPairMap.keySet()) {
        		ItemPair pair = itemPairMap.get(key);
        		if (pair.extra) {
        			HadoopResItem mergedItem = new HadoopResItem();
        			mergedItem.copy(pair.origItem);
        			mergedItem.hadoop_version = pair.extraHadoopItem.hadoop_version;
        			mergedItem.h_copiedOk = pair.extraHadoopItem.h_copiedOk;
        			mergedItem.h_copyherefileProduced = pair.extraHadoopItem.h_copyherefileProduced;
        			mergedItem.h_copybackfileProduced = pair.extraHadoopItem.h_copybackfileProduced;
        			mergedItem.h_compressedfileProduced = pair.extraHadoopItem.h_compressedfileProduced;
        			mergedItem.h_hasDir = pair.extraHadoopItem.h_hasDir;
        			mergedItem.h_emptydatadir = pair.extraHadoopItem.h_emptydatadir;
                	//mergedItem.hadoopCheckedNewUpdate = pair.extraHadoopItem.hadoopCheckedNewUpdate; 
        			mergedItem.hadoopfileProduced = pair.extraHadoopItem.hadoopfileProduced; 
        			mergedItemMap.put(key,mergedItem);
        		} else {
        			mergedItemMap.put(key,pair.origItem);
        		}
        	}
        	return mergedItemMap;
    	}

    	public void findStatusItemSetsInternal( Set<HadoopResItem> itemSet, boolean forceingest) {
        	//clear previous sets
        	init();
    	    for (HadoopResItem it : itemSet) {
            	if (!it.datasubdir.exists()) {
    	        	nothing_Set.add(it);
            	} else {
        	        File[] dirfiles = it.datasubdir.listFiles();
        	        //if (it.datasubdir.listFiles())
        	        
        	        int countPartfiles = 0;
        	        for (File f : dirfiles) {
        	        	String fname = f.getName();
        	        	if ((!fname.startsWith(wf_copyherefilename_prefix)) && (!fname.startsWith(wf_resfilename_prefix))
        	        			&& (!fname.startsWith(wf_compressfilename_prefix)) ) { //else ignore
	        	        	if (MysqlX.isPartfile(fname)) {
	        	        		countPartfiles++;
	        	        	} else if (fname.equals(wf_donehadoopfilename) || fname.equals(wf_done2hadoopfilename)) {
	        	        		it.copiedOk = true;
	        	        	} else if (fname.equals(wf_doneupdatenewHadoopfilename)) {
	        	        		it.hadoopfileProduced = true;
	        	        	} else if (fname.startsWith(wf_urllevels_extractfilename_prefix)) {
	        	        		int i = (int) Integer.parseInt(fname.replace(wf_urllevels_extractfilename_prefix + "_", ""));
	        	        		it.urlLevelExtracted.set(i-1, true);
	        	        	} else if (fname.startsWith(wf_urllevelsnew_extractfilename_prefix)) {
	        	        		int i = (int) Integer.parseInt(fname.replace(wf_urllevelsnew_extractfilename_prefix + "_", ""));
	        	        		it.urlLevelNewExtracted.set(i-1, true);
	        	        	} else if (fname.equals(wf_doneingestfilename)) {
	        	        		it.ingestfileProduced = true;
	        	        	} else if (fname.startsWith(wf_updatefilename_prefix)) {
	        	        		it.updatedProduced = true;
	        	        		int i = findPartFileNumber(fname, wf_updatefilename_prefix);
	        	        		if (it.updateVersion < i) it.updateVersion = i;  
	        	        	} else if (fname.startsWith(wf_donestatfilename)) {
	        	        		it.statProduced = true;
	        	        		int i = findPartFileNumber(fname, wf_donestatfilename);
	        	        		if (it.statVersion < i) it.statVersion=i;
	        	        	} else if (fname.equals(wf_donecompressfilename)) {
	        	        		it.compressedfileProduced = true;
	        	        	//} else if (fname.equals(wf_updatecheck_newHadoop_filename)) {
	        	        	//	it.hadoopCheckedNewUpdate = true;
	        	        	} else if (fname.equals(wf_update_links_filename)) {
	        	        		it.berDbUpdatefileProduced = true;
	        	        	} else if (fname.startsWith(wf_bakedup)) {
	        	        		it.backedup = fname.replace(wf_bakedup, "");
	        	        	} else if (fname.startsWith(wf_donecopyfilename_prefix)) {
	        	        		if(fname.equals(wf_donecopybackfilename)) {
	        	        			it.copybackfileProduced = true;
	        	        		} else { //startswith wf_donecopyherefilename
	        	        			it.copyherefileProduced = true;
	        	        		}
	        	        	} else {
	        	        		it.extrafiles = it.extrafiles 
	        	        				+ (it.extrafiles.isEmpty() ? "" : MysqlX.statustext_delim )
	        	        				+ fname;
	        	        	}
        	        	}
        	        	it.statProduced = it.statProduced || (fname.startsWith(wf_donestatfilename + MysqlIngester.ingest_current_update_no));
        	        	if (MysqlX.isPartfile(fname)) countPartfiles++;
        	        }
        	        
        	        //System.out.println("it.backedup: " + it.backedup);
        	        //System.out.println("it.copiedOk 1: " + it.copiedOk);
        	        it.copiedOk = it.copiedOk && (countPartfiles > 0 || !it.backedup.isEmpty());
        	        it.copyok = it.copyok && it.copiedOk && it.copybackfileProduced && it.copyherefileProduced;
        	        //System.out.println("it.copiedOk 2: " + it.copiedOk);
        	        it.emptydatadir = (dirfiles.length == 0) 
        	        		             || (dirfiles.length == 1 && dirfiles[0].getName().startsWith(wf_copyherefilename_prefix))
        	        		             || (dirfiles.length == 2 && dirfiles[0].getName().startsWith(wf_copyherefilename_prefix)
        	        		            		 				  && dirfiles[1].getName().startsWith(wf_copyherefilename_prefix));
        	        if (!it.hadoop_version.isEmpty()) {
            	        it.h_hasDir = it.hasDir;
            	        it.h_emptydatadir = it.emptydatadir;
            	        it.h_copiedOk = it.copiedOk;
            	        it.h_copybackfileProduced = it.copybackfileProduced;
            	        it.h_copyherefileProduced = it.copyherefileProduced;
        	        	it.h_compressedfileProduced = it.compressedfileProduced;
        	        	it.h_backedup = it.backedup;
        	        }
        	        	
            		
            		updateWithItem(it, forceingest);
        	    } //end else dir exists
    	    }
        }
        
    	public int findPartFileNumber(String fname, String prefix) {
    		String fn_suf = fname.substring(prefix.length());
    		if (fn_suf.endsWith(".gz")) fn_suf = fn_suf.replace(".gz", "");
    		return Integer.parseInt(fn_suf);
    	}
    	
    	public void findStatusItemSetsExternal( Set<HadoopResItem> itemSet) {
        	//clear previous sets
        	init();
    	    for (HadoopResItem it : itemSet) {
            	if (!it.hasDir) {
    	        	nothing_Set.add(it);
            	} else {
            		updateWithItem(it, false);
        	    } 
    	    }
        }
        
        private void updateWithItem(HadoopResItem it, boolean forceingest) {
        	//System.out.println("updateWithItem: " + it.datasubdirname());
	        if (it.compressedfileProduced) {
	        	compress_Set.add(it);
	        	if (!(it.copiedOk && it.copyherefileProduced && it.copybackfileProduced && (it.ingestfileProduced || it.hadoopfileProduced))) {
	        		followup_Set.add(it);
	        	} 
	        	if (it.copiedOk && it.copyherefileProduced) {
	        		if (!it.statProduced) {
	        			ok_for_stat_Set.add(it);
	        		}
	        	}
        	}
	        if (it.berDbUpdatefileProduced) {
	        	berDb_Set.add(it);
        	}
	        /*if (it.hadoopCheckedNewUpdate) {
	        	hadoopUpdate_Set.add(it);
        	}*/
	        
	        if (it.statProduced) {
	        	stat_Set.add(it);
	        	if (!(it.copiedOk && it.copyherefileProduced && it.copybackfileProduced && (it.ingestfileProduced || it.hadoopfileProduced))) {
	        		followup_Set.add(it);
	        	} 
	        	if (it.copiedOk && it.copyherefileProduced) {
	        		if (!it.compressedfileProduced) {
	        			ok_for_compress_Set.add(it);
	        		}
        		}
        	}
	        if (it.hadoopfileProduced) {
	        	hadoop2_Set.add(it);
	        	if (!(it.copiedOk && it.copyherefileProduced && it.copybackfileProduced)) {
	        		followup_Set.add(it);
	        	}
	        	if (it.copiedOk && it.copyherefileProduced) {
	        		if (!it.compressedfileProduced) {
	        			ok_for_compress_Set.add(it);
	        		}
        		}
        	}
	        
	        if (it.ingestfileProduced) {
	        	ingested_Set.add(it);
	        	if (!(it.copiedOk && it.copyherefileProduced && it.copybackfileProduced)) {
	        		followup_Set.add(it);
	        	}
	        	if (it.copiedOk && it.copyherefileProduced) {
	        		if (!it.compressedfileProduced) {
	        			ok_for_compress_Set.add(it);
	        		}
	        		if (!it.statProduced) {
	        			ok_for_stat_Set.add(it);
	        		}
        		}
        	}
        	//System.out.println("copybackfileProduced: " + it.copybackfileProduced);

	        if (it.copybackfileProduced) {
        		//System.out.println("it.ingestfileProduced: " + it.ingestfileProduced);
        		//System.out.println("it.hadoopfileProduced: " + it.hadoopfileProduced);

        		if (!(it.ingestfileProduced || it.hadoopfileProduced)) {
	        		copyfile_returned_Set.add(it);
	        		
	        		//System.out.println("it.copiedOk: " + it.copiedOk);
	        		//System.out.println("it.copyherefileProduced: " + it.copyherefileProduced);
	        		if (!(it.copiedOk && it.copyherefileProduced)) {
    	        		error_Set.add(it);
    	        	} else {
	    	        	if (forceingest || (it.copiedOk && !error_Set.contains(it))) {
	    	        		if (it.hadoop_version.isEmpty()) {
	        	            	//System.out.println("ingest");
	    	        			ok_for_ingest_Set.add(it);
	    	        		} else {
	        	            	//System.out.println("ok_for_hadoopUpdate_Set");
	    	        			ok_for_hadoopUpdate_Set.add(it); 
	    	        		}
	    	        	}
    	        	}
	        	}
        	}

	        if (it.copyherefileProduced) {
	        	if (!(it.ingestfileProduced || it.hadoopfileProduced)) {
    	        	copyfile_produced_Set.add(it);
	            	//System.out.println("hadoopfileProduced - copyherefileProduced: " + it.datasubdirname());
    	        	if (forceingest || (it.copiedOk && !error_Set.contains(it))) {
    	        		if (it.hadoop_version.isEmpty()) {
    	        			//System.out.println("ingest");
    	        			ok_for_ingest_Set.add(it);
    	        		} else {
    	        			//System.out.println("ok_for_hadoopUpdate_Set");
    	        			ok_for_hadoopUpdate_Set.add(it); 
    	        		}
    	        	} else {
    	        		error_Set.add(it);
    	        	}
	        	}
        	}
	        if (forceingest || it.copiedOk) {
	        	if (!(it.ingestfileProduced || it.hadoopfileProduced)) { 
	        		//System.out.println("copiedOk - copyherefileProduced: " + it.datasubdirname());
	        		if (it.hadoop_version.isEmpty()) {
	        			//System.out.println("ingest");
	        			ok_for_ingest_Set.add(it);
	        		} else {
	        			//System.out.println("ok_for_hadoopUpdate_Set");
	        			ok_for_hadoopUpdate_Set.add(it); 
	        		}
	        	}
	        	if (!it.copyherefileProduced) {
	        		copied_here_Set.add(it);
	        	}
	        } else {
	        	it.copyok = false;
	        	copy_failed_Set.add(it);
	        }
        }

        public String[] writeStatusItemSets(int countItems) {
        	String[] resLines = new String[countItems*13];
        	int i = 0;
        	if (copy_failed_Set.size() > 0) {
        		resLines[i] = "--- Copy failed:";
        		i++;
        	    for (HadoopResItem it : copy_failed_Set) {
                	resLines[i] = " - " + it.datasubdirname();
            		i++;
        	    }
    	    }
        	if (copied_here_Set.size() > 0) {
        		resLines[i] = "--- Copy ok:";
        		i++;
        	    for (HadoopResItem it : copied_here_Set) {
        	    	resLines[i] = " - " + it.datasubdirname();
            		i++;
        	    }
    	    }
        	if (copyfile_produced_Set.size() > 0) {
            	resLines[i] = "--- Copy status produced:";
        		i++;
        	    for (HadoopResItem it : copyfile_produced_Set) {
                	resLines[i] = " - " + it.datasubdirname();
            		i++;
        	    }
    	    }
        	if (copyfile_returned_Set.size() > 0) {
            	resLines[i] = "--- copy status returned:";
        		i++;
        	    for (HadoopResItem it : copyfile_returned_Set) {
                	resLines[i] = " - " + it.datasubdirname();
            		i++;
        	    }
    	    }
        	if (hadoop2_Set.size() > 0) {
            	resLines[i] = "--- hadooped 2.:";
        		i++;
        	    for (HadoopResItem it : hadoop2_Set) {
                	resLines[i] = " - " + it.datasubdirname();
            		i++;
        	    }
    	    }
        	if (ingested_Set.size() > 0) {
            	resLines[i] = "--- Ingested:";
        		i++;
        	    for (HadoopResItem it : ingested_Set) {
                	resLines[i] = " - " + it.datasubdirname();
            		i++;
        	    }
    	    }
        	if (stat_Set.size() > 0) {
            	resLines[i] = "--- Lagkaged:";
        		i++;
        	    for (HadoopResItem it : stat_Set) {
                	resLines[i] = " - " + it.datasubdirname();
            		i++;
        	    }
    	    }
        	if (compress_Set.size() > 0) {
            	resLines[i] = "--- compressed:";
        		i++;
        	    for (HadoopResItem it : compress_Set) {
                	resLines[i] = " - " + it.datasubdirname();
            		i++;
        	    }
    	    }
	        if (berDb_Set.size() > 0) {
            	resLines[i] = "--- Link updated:";
        		i++;
        	    for (HadoopResItem it : berDb_Set) {
                	resLines[i] = " - " + it.datasubdirname();
            		i++;
        	    }
        	}
	        if (hadoopUpdate_Set.size() > 0) {
            	resLines[i] = "--- Hadoop updated:";
        		i++;
        	    for (HadoopResItem it : hadoopUpdate_Set) {
                	resLines[i] = " - " + it.datasubdirname();
            		i++;
        	    }
        	}

        	if (ok_for_ingest_Set.size() > 0) {
            	resLines[i] = "--- Ok for ingest:";
        		i++;
        	    for (HadoopResItem it : ok_for_ingest_Set) {
                	resLines[i] = " - " + it.datasubdirname();
            		i++;
        	    }
    	    }
        	if (ok_for_compress_Set.size() > 0) {
            	resLines[i] = "--- Ok for compress:";
        		i++;
        	    for (HadoopResItem it : ok_for_compress_Set) {
                	resLines[i] = " - " + it.datasubdirname();
            		i++;
        	    }
    	    }
        	if (ok_for_stat_Set.size() > 0) {
            	resLines[i] = "--- Ok for statistics:";
        		i++;
        	    for (HadoopResItem it : ok_for_stat_Set) {
                	resLines[i] = " - " + it.datasubdirname();
            		i++;
        	    }
    	    }
        	if (followup_Set.size() > 0) {
            	resLines[i] = "--- follow up missing:";
        		i++;
        	    for (HadoopResItem it : followup_Set) {
                	resLines[i] = " - " + it.datasubdirname();
            		i++;
        	    }
    	    }
        	if (error_Set.size() > 0) {
            	resLines[i] = "--- Errors/servere inkonsistancies:";
        		i++;
        	    for (HadoopResItem it : error_Set) {
                	resLines[i] = " - " + it.datasubdirname();
            		i++;
        	    }
    	    }
        	if (nothing_Set.size() > 0) {
            	resLines[i] = "--- Nothing done:";
        		i++;
        	    for (HadoopResItem it : nothing_Set) {
                	resLines[i] = " - " + it.datasubdirname();
            		i++;
        	    }
    	    }
        	return resLines;
        }
    }
        
    public static String wf_calcDanishCode_dir = "codes";
    public static String wf_table_delim = "_";
    public static String wf_dir_delim = "-";
    public static String wf_interval_prefix = "i";
    public static String wf_interval_infix = "x";
    public static String wf_version_prefix = "v";
    public static String wf_maschine_prefix = "m";
    public static String wf_disk_prefix = "d"; //can also be tld- for non-IA data
    public static String wf_part_prefix = "p";
    public static String wf_resfilename_prefix = "res-";
    public static String wf_resfilename_suffix = ".txt";
    public static String wf_copyherefilename_prefix = "copyhere_";
    public static String wf_copybackfilename_prefix = "copyback_";
    public static String wf_ingestfilename_prefix = "ingest_";
    public static String wf_updatehadoopfilename_prefix = "update_";
    public static String wf_multiingestfilename_prefix = "multiingest_";
    public static String wf_multiupdatehadoopfilename_prefix = "multiupdate_";
    public static String wf_compressfilename_prefix = "compress_";
    public static String wf_bash_suffix = ".sh";
    public static String wf_donehadoopfilename_prefix = "_S";
    public static String wf_donehadoopfilename = "_SUCCESS";
    public static String wf_done2hadoopfilename = "_STOPPED";
    public static String wf_donecopyfilename_prefix = "_COPIED";
    public static String wf_donecopyherefilename = "_COPIED";
    public static String wf_donecopybackfilename = "_COPIEDBACK";
    public static String wf_doneingestfilename = "_INGESTED";
    public static String wf_doneupdatenewHadoopfilename = "_HADOOPUPDATE";
    public static String wf_donestatfilename = "_STATED";
    public static String wf_donecompressfilename = "_COMPRESSED";
    public static String wf_updatefilename_prefix = "_UPDATE";
    public static String wf_urllevels_searchfilename_prefix = "_URLLEVEL_SEARCH";
    public static String wf_urllevels_extractfilename_prefix = "_URLLEVEL_EXTRACT";
    public static String wf_urllevelsnew_extractfilename_prefix = "_URLLEVELNEW_EXTRACT";
    public static String wf_update_links_filename = "_LINKUPDATE";
    public static String wf_bakedup = "_BCK_COPIED_TO_";
    //public static String wf_update_newHadoop_filename = "_HADOOPUPDATE";

    enum WfPart{
    	wf_all,
    	wf_allbutcopy,
    	wf_dir,
    	wf_copy,
    	wf_checkcopy,
    	wf_table,
    	wf_ingest,
    	wf_forceingest,
    	wf_checkingest,
    	wf_compress,
    	wf_statistics,
    	wf_none
    }

    enum WfManStep{
    	step_all,
    	step_copyhere,
    	step_copyback,
    	step_ingest,
    	step_compress,
    	step_noneexceptlost,
    	step_none
    }

    enum IgnoreFile{
    	if_true,
    	if_false,
    	if_warning
    }

    enum IngestType{
    	it_base_ingest,
    	it_first_hadoopupadte
    }

    enum ItemNameType{
    	normal_item,
    	inteval_item
    }

    enum ItemNameVersion{
    	V1,
    	V5
    }
}
