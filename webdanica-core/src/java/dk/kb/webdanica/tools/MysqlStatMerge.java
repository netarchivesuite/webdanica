package dk.kb.webdanica.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.text.SimpleDateFormat;

import dk.kb.webdanica.tools.MysqlWorkFlow.IgnoreFile;
import dk.kb.webdanica.tools.MysqlX.*;


/*
String url = "jdbc:mysql://localhost/test|webdanica";
Class.forName ("com.mysql.jdbc.Driver").newInstance ();
Connection conn = DriverManager.getConnection (url, "username", "password");
*/

/*
Update TABLE ResFromHadoop with C15b varchar(20) with TLD
*/

public class MysqlStatMerge {

    /**
     * @param args datadir=<e.g. /data1/resultater/> outCodes=allCodes|positive|intervals|danish|allStats outDisplayCode=noCodes|onlyCodes|inText|separateText|allDisplays version=<numberofversion> machine=all|test1|... ignoreFile=true|false|warning";
     * @throws ClassNotFoundException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     * @throws SQLException 
     * @throws IOException 
     */
	
	private static String row_delim = MysqlX.row_delim;

	public enum StatHadoops {
		allhadoops,
		hadoops1only,
		hadoops2only
	}

	private static String getShAbbr(StatHadoops sc) {
		String s = "";
		switch (sc) {
			case allhadoops: s="hall_"; break;
			case hadoops1only: s="h1_"; break;
			case hadoops2only: s="h2_"; break;
		}
		return s;
	}
	
	private static int getShIndex(StatHadoops sc) {
		int i = -1;
		switch (sc) {
			case allhadoops: i=0; break;
			case hadoops1only: i=1; break;
			case hadoops2only: i=2; break;
		}
		return i;
	}
	
	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException  {
        /*****************************************/
        /*** parameters **************************/
        /*****************************************/

		String errArgTxt="Proper args: "
    			+ "datadir=<e.g. /data1/resultater/> " // stat/kage for input stat/kage_merge for output 
				//+ "stattype=kage|freq "
				//+ "inCodes=allCodes|positive|intervals|danish " must be allcodes
				//+ "inDisplayCode=onlyCodes|inText|separateText " must be onlycodes 
				+ "outCodes=allCodes|positive|intervals|danish|allStats "
				+ "outDisplayCode=noCodes|onlyCodes|inText|separateText|allDisplays "
				+ "version=<numberofversion> "
    			+ "ignoreFile=true|false|warning";

    	if (args.length < 6) {
            System.err.println("ERROR: Missing args!"); 
            System.err.println(errArgTxt);
            System.exit(1);
        }
        if (args.length > 6) {
            System.err.println("ERROR: Too many args! got " + args.length );
            for (int i=0; i< args.length; i++) System.err.println("p[" + i + "]='" + args[i] + "'");
            System.err.println(errArgTxt);
            System.exit(1);
        }

        /**** args - datadir files dir ****/
        String datadirTxt = args[0];
        if (!datadirTxt.startsWith("datadir=")) {
            System.err.println("ERROR: Missing arg datadir setting - got '" + datadirTxt + "'");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        datadirTxt = MysqlX.getStringSetting(datadirTxt);
        
        /**** args - stattype ****
        String stattypeTxt = args[1];
        if (!stattypeTxt.startsWith("stattype=")) {
            System.err.println("ERROR: Missing arg stattype setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        stattypeTxt = MysqlX.getStringSetting(stattypeTxt);
        StatType stattype = StatType.stat_none;
        if (stattypeTxt.equals("kage"))  stattype = StatType.stat_kage; 
        else if (stattypeTxt.equals("freq")) stattype = StatType.stat_freq; 
        else {
            System.err.println("ERROR: Arg stattype setting is not valid - got '" + stattypeTxt + "'");
            System.err.println(errArgTxt);
            System.exit(1);
        }

        /**** args - IN statistics inCodes ****/
		//+ "inCodes=allCodes|positive|intervals|danish " must be allcodes
		//+ "inDisplayCode=onlyCodes|inText|separateText " must be onlycodes 

		/**** args - OUT statistics outCodes ****/
        /** arg OUT codes for details  - codes=allCodes|positive|intervals|danish **/
        String outCodeTxt = args[1];
        if (!outCodeTxt.startsWith("outCodes=")) {
            System.err.println("ERROR: Missing arg outCodes setting - got " +  outCodeTxt);
            System.err.println(errArgTxt);
            System.exit(1);
        }
        outCodeTxt = MysqlX.getStringSetting(outCodeTxt);
        Level outLevel = Level.none;  
        if (outCodeTxt.matches("allCodes"))  {
        	outLevel = Level.allcodes; 
        } else if (outCodeTxt.matches("positive")) {
        	outLevel = Level.positive; 
        } else if (outCodeTxt.matches("intervals")) {
        	outLevel = Level.intervals;
        } else if (outCodeTxt.matches("danish")) {
        	outLevel = Level.danish;
		} else if (outCodeTxt.matches("allStats")) {
        	outLevel = Level.allStats;
		} else {
            System.err.println("ERROR: Arg outCodes setting is not valid - got '" + outCodeTxt + "'");
            System.err.println(errArgTxt);
            System.exit(1);
        }

        /** args - codes for output to outDisplay   displayCode=none|inText|separateText|allDisplays **/
        //System.out.println("outDisplayCodeTxt = args[2]]: " + args[2]);
        String outDisplayCodeTxt = args[2].trim();
        if (!outDisplayCodeTxt.startsWith("outDisplayCode=")) {
            System.err.println("ERROR: Missing arg outDisplayCode setting - got '" +  outDisplayCodeTxt + "'");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        outDisplayCodeTxt = MysqlX.getStringSetting(outDisplayCodeTxt);
        Display outDisplay = Display.noDisplay;  
        if (outDisplayCodeTxt.equals("noCodes")) outDisplay = Display.noCodes; 
        else if (outDisplayCodeTxt.equals("onlyCodes"))  outDisplay = Display.onlyCodes; 
        else if (outDisplayCodeTxt.matches("inText")) outDisplay = Display.inText; 
        else if (outDisplayCodeTxt.matches("separateText")) outDisplay = Display.separateText; 
        else if (outDisplayCodeTxt.matches("allDisplays")) outDisplay = Display.allDisplays;
        else {
            System.err.println("ERROR: Unknown value for outDisplayCode setting - got " +  outDisplayCodeTxt);
            System.err.println(errArgTxt);
            System.exit(1);
        }

        //System.out.println("versionTxt = args[3]]: " + args[3]);
        String versionTxt = args[3];
        if (!versionTxt.startsWith("version=")) {
            System.err.println("Missing arg version=<numberofversion> setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        versionTxt = MysqlX.getStringSetting(versionTxt);
         
        
        /**** args - machine ****/
        /** arg - machine name **/
        System.out.println("machine = args[4]: " + args[4]);
        String machine = args[4];
        if (!machine.startsWith("machine=")) {
            System.err.println("ERROR: Missing arg machine=all|test1|... setting - got " + machine);
            System.err.println(errArgTxt);
            System.exit(1);
        }
        machine = MysqlX.getStringSetting(machine);
        
        
        String ignoreFileTxt = args[5];
        System.out.println("ignoreFileTxt = args[5]: " + args[5]);
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
        //Dir with stat 
        String statDirname = datadirTxt 
        		+ (datadirTxt.substring(datadirTxt.length()-1).equals("/") ? "" : "/" )
        		+ MysqlX.statistics_dir + "/";
        MysqlX.checkDir(statDirname);

        //Dirs with extracted stat files
        String kagestatDirname = statDirname + "/" + MysqlX.lagkageextract_dir;
        File kagestatDir = MysqlX.checkDir(kagestatDirname);
        String freqstatDirname = statDirname + "/" + MysqlX.frequenceextract_dir;
        File freqstatDir = MysqlX.checkDir(freqstatDirname);

        //Dir with merged stat files
        String kagemergedirname = statDirname + "/" + MysqlX.lagkageextract_dir + MysqlX.merge_dir_suffix ;
        File kagemergeDir = MysqlX.checkDir(kagemergedirname);
        String freqmergedirname = statDirname + "/" + MysqlX.frequenceextract_dir + MysqlX.merge_dir_suffix;
        File freqmergeDir = MysqlX.checkDir(freqmergedirname);
        
        class MergeFile {
        	Set<File> files = new HashSet <File>();
        	MergedStatsResult res =  new MergedStatsResult();
        }
        
        MergeFile[][] kageFilesSet = new MergeFile[StatHadoops.values().length][Source.values().length];
        MergeFile[][] freqFilesSet = new MergeFile[StatHadoops.values().length][Source.values().length];

        	
        for (Source src : Source.values()) {
        	MergeFile kmf = new MergeFile();
        	kmf.files = findStatFiles(kagestatDir, MysqlX.lagkage_fileprefix, MysqlX.convertSource(src), versionTxt, machine);
        	kageFilesSet[getShIndex(StatHadoops.allhadoops)][MysqlX.getSourceIndex(src)] = kmf;
        	
        	MergeFile fmf = new MergeFile();
        	fmf.files	= findStatFiles(freqstatDir, MysqlX.frequence_fileprefix, MysqlX.convertSource(src), versionTxt, machine);
        	freqFilesSet[getShIndex(StatHadoops.allhadoops)][MysqlX.getSourceIndex(src)] = fmf;
        }
        
        for (StatHadoops sh : StatHadoops.values()) {
            for (Source src : Source.values()) {
            	if (sh!=StatHadoops.allhadoops) {
                	MergeFile kmf = new MergeFile();
                	kmf.files	= findStatFilesFromAllhadoops(
            					kagestatDir, MysqlX.lagkage_fileprefix,
            					sh, versionTxt,
            					kageFilesSet[getShIndex(StatHadoops.allhadoops)][MysqlX.getSourceIndex(src)].files
            			  );
                	kageFilesSet[getShIndex(sh)][MysqlX.getSourceIndex(src)] = kmf;  

                	MergeFile fmf = new MergeFile();
                	fmf.files = findStatFilesFromAllhadoops(
            					freqstatDir, MysqlX.frequence_fileprefix, 
            					sh, versionTxt,
            					freqFilesSet[getShIndex(StatHadoops.allhadoops)][MysqlX.getSourceIndex(src)].files
            			  );
                	freqFilesSet[getShIndex(sh)][MysqlX.getSourceIndex(src)] = fmf; 
            	}
            }
        }
        
        for (StatHadoops sh : StatHadoops.values()) {
            for (Source src : Source.values()) {
            	System.out.println("sh: " + sh.name() + ", src " + src.name() + ":" + freqFilesSet[getShIndex(sh)][MysqlX.getSourceIndex(src)].files.size());
            }
        }
        
        for (StatHadoops sh : StatHadoops.values()) {
            for (Source src : Source.values()) {
                /*** Find files for merge ***/
            	MergeFile kage = kageFilesSet[getShIndex(sh)][MysqlX.getSourceIndex(src)];
            	MergeFile freq = freqFilesSet[getShIndex(sh)][MysqlX.getSourceIndex(src)];
            	
            	/*** Read files into Merged stat map ***/
            	kage.res = mergeDataFromFiles(kage.files, outLevel, true);
        		freq.res = mergeDataFromFiles(freq.files, outLevel, false);
	        
		        /*** Write Merged-stat map ***/
		        Set<String> resFileSet = new HashSet<String>();
            	resFileSet = writeStatFiles(kagemergeDir, StatType.stat_kage, kage.res, src, sh, outDisplay, machine, ignoreFile );
		    	System.out.println("Resulted in IA kage files:");
		        for (String resFile : resFileSet) {
		        	System.out.println(" - " + kagemergeDir + "/" + resFile );
		        }

		        resFileSet = writeStatFiles(freqmergeDir, StatType.stat_freq, freq.res, src, sh, outDisplay, machine, ignoreFile );
		    	System.out.println("Resulted in IA bit-freq files:");
		        for (String resFile : resFileSet) {
		        	System.out.println(" - " + freqmergeDir + "/" + resFile );
		        }
            }
        }
	}

	public static MergedStatsResult mergeDataFromFiles(Set<File> statFilesSet, Level outLevel, boolean isKage) throws IOException {
		MergedStatsResult res = new MergedStatsResult();
		res.mergedStats = initStatistics(outLevel);
		System.err.println("isKage " +isKage );
	    for (File nextfile: statFilesSet) {
	        BufferedReader fr = new BufferedReader(new FileReader(nextfile));        
	        String line;
	        
	        /** Find all tables in merge **/
	        boolean ok = (line = fr.readLine()) != null;
	        if (ok) ok = line.startsWith(MysqlX.stat_tableline_start);
	        if (ok) {
	        	String s = getRowParts(line);
	            String[] parts = s.split(MysqlX.tablename_delim);
	            if (parts.length == 0) {
	            	System.err.println("ERROR: table line in " + nextfile + "did not include tables: '" + line + "'");
	                System.exit(1);
	            }
	            for (String part : parts) {
	            	if (res.tableSet.contains(part)) {
	                	System.err.println("ERROR: table " + part + " is included in more than one file - last one was : '" + nextfile + "'");
	                    System.exit(1);
	            	}
	            	res.tableSet.add(part.trim());
	            }
	        } else {
	            System.err.println("ERROR: tableline in file " + nextfile + " does not represent proper table line");
	            System.exit(1);
	        }
	
	        /** Ignore header line **/
	        ok = (line = fr.readLine()) != null;
	        if (ok) {
	        	ok = line.startsWith(row_delim);
	        	ok = ok && (
	        			(line.contains(MysqlStatExtract.lagkage_title_start) ) // && stattype==StatType.stat_kage)
	        			|| (line.contains(MysqlStatExtract.codefrequence_title_start) ) //&& stattype==StatType.stat_freq)
	        		  );
	        }
	        if (!ok) {
	            System.err.println("ERROR: headerline in file " + nextfile + " does not represent proper header line");
	            System.exit(1);
	        }
	        
	        /** Merge calc code lines header line **/
	        Set<Integer> codesErrors = MysqlX.getCodesForUdgaaede();
	        Set<Integer> codesIgnored = MysqlX.getCodesForFrasorterede();
	        Set<Integer> codesNotDk = MysqlX.getCodesForNOTDanishResults();
	        Set<Integer> codesDk = MysqlX.getCodesForDanishResults();
	        Set<Integer> codesmaybes = MysqlX.getCodesForMaybees();
	        //Set<Integer> codesUnknown = Not decided 0 and negative

	        boolean seenTotalLine = false; //only for lagkage
	        while ((line = fr.readLine()) != null) {
	        	boolean isTotalline = line.startsWith(MysqlStatExtract.stat_totalline_start);
	        	line = line.trim();
	            if (!line.isEmpty()) {
	            	//treat total line as last line
	            	if (seenTotalLine) {
	                    System.err.println("ERROR: line after total line in file " + nextfile + ": '" + line + "'");
	                    System.exit(1);
	                }
	            	
	            	//get value
	            	String[] parts = line.split(row_delim);
                    if (parts.length!=2) {
                        System.err.println("ERROR: line in " + nextfile + " cannot be reconized: '" + line + "'");
                        System.exit(1);
                    }
                    long cntVal = Long.parseLong(parts[1].trim());
	            	
	            	if  (isTotalline) {
		            	if  (parts[0].equals(MysqlStatExtract.stat_totalline_start)) {
		            		seenTotalLine = true;
		            		res.total = res.total + cntVal;
		            	} else {
		            		res.total0 = res.total0 + cntVal;
		            	}
	            	}

	        		//treat count line - only (codes,count) - i.e. only inCodes=codesOnly is implemented
	            	if (!isTotalline) {
	                    int key = Integer.parseInt(parts[0].trim());
	        				
	    				for (Statistics ms : res.mergedStats) {
	    					int newKey = key;
	    					long newVal = cntVal;
	    					if (ms.included) {
	    						if (ms.isumup && newKey<0) {
	    							newKey = 0; // negative summed up in key=0
	    						}
	    						
	    						if (ms.level==Level.intervals) {
		    						for (int i=10; i < 200; i = i+10) {
		    							if (newKey-i>=0 && newKey-i<=9) newKey=i;
		    						}
	    						}
	    						
	    						if (ms.dksumup && isKage) {
	    							if (codesErrors.contains(newKey)) { 
	    								newKey = MysqlX.cat_ERROR_dk;
	    							} else if (codesIgnored.contains(newKey)) { 
	    								newKey = MysqlX.cat_ignored_dk;
	    							} else if (codesNotDk.contains(newKey)) { 
	    								newKey = MysqlX.cat_not_likely_dk;
	    							} else if (codesDk.contains(newKey)) { 
	    								newKey = MysqlX.cat_likely_dk;
	    							} else if (codesmaybes.contains(newKey)) { 
	    								newKey = MysqlX.cat_maybes_dk;
	    							} else if (newKey<=0) { 
	    								newKey = MysqlX.cat_unknown_dk;
	    							} else { 
	    								System.out.println("UNCATEGORIZED code: " + newKey);
	    							}
	    						}
	    						
	    						//update map
	    						if (ms.countMap.containsKey(newKey)) {
	    							newVal= newVal + ms.countMap.get(newKey);
	    						}
	    						ms.countMap.put(newKey, newVal);
	    					}
	    				} // for statistics
	        		} else { // is totaline - NOT if (codes,count) line
	        			
	        		}
	            } // if line not empty
	        }  // while more lines  
	        fr.close();
	    } //for all files
	    
        for (Statistics st : res.mergedStats) {
        	st.total0urls = res.total0;
        	st.totalurls = res.total;
        }
	    return res;
	}

	public static Set<File> findStatFiles(File statDir, String statprefix, DataSource src, String versionTxt, String machine) {
	    Set<File> allStatFilesSet = new HashSet<File>();
	    Set<File> statFilesSet = new HashSet<File>();
	    Set<String> filenameSet = new HashSet<String>();
	    
    	String fileprefix = statprefix + MysqlX.getSourceInfix(src);
	    File[] dirfiles = statDir.listFiles();
	    
        System.out.println("Finding files");
	    for (File nextfile: dirfiles) {
	    	String name = nextfile.getName();
	    	if (name.startsWith(fileprefix)) {
		    	if (name.contains(versionTxt)) {
			    	if (machine.equals("all") || name.contains(machine)) {
			    		filenameSet.add(name);
			    		allStatFilesSet.add(nextfile);
			    	} //else System.out.println("NOT all and NOT contains: " +machine);
		    	}
	    	}
        }        
        System.out.println("allStatFilesSet.size " + allStatFilesSet.size());

	    for (File nextfile: allStatFilesSet) {
	    	String name = nextfile.getName();
	        System.out.println("PROCESSING " + name);
	    	if (name.contains(getShAbbr(StatHadoops.hadoops2only))) {
    			statFilesSet.add(nextfile);
    		} else if (name.contains(getShAbbr(StatHadoops.hadoops1only))) {
	    		//there may be an update later and therefore an hadoops2only
	    		String h2name = name.replace(getShAbbr(StatHadoops.hadoops1only), getShAbbr(StatHadoops.hadoops2only));
	    		if (!filenameSet.contains(h2name)) {
	    			statFilesSet.add(nextfile);
	    		}
    		} else { //status or url
    			statFilesSet.add(nextfile);
    		}
	    }        

	    if (statFilesSet.size() == 0) {
	    	System.err.println("ERROR: No files to be merged in dir " + statDir.getName());
	        //System.exit(1);
	    }
	    if (statFilesSet.size() == 1) {
	    	System.out.println("WARNING: Only ONE file in dir " + statDir.getName() );
	    	String filename = "";
	        for (File nextfile: statFilesSet) {
	        	filename = nextfile.getName();
	        }
	    	System.out.println("File " + filename);
	    }
	    return statFilesSet;
	}

	public static Set<File> findStatFilesFromAllhadoops(File statDir, String statprefix, StatHadoops sh, String versionTxt, Set<File> allFiles) {
	    Set<File> statFilesSet = new HashSet<File>();
	    
	    for (File nextfile: allFiles) {
	    	String name = nextfile.getName();
		    if (name.contains(getShAbbr(sh))) {
	    		statFilesSet.add(nextfile);
	        }        
        }        
        return statFilesSet;
    }
    
	public static Statistics[] initStatistics(Level level) {
        //l3.intervals = true; <=> level=Level.intervals
    	Statistics[] stats = new Statistics[4];
    	Statistics l1 = new Statistics();
        if (level==Level.allcodes || level==Level.allStats) l1.included = true;
        l1.level = Level.allcodes;
        l1.isumup = false;
        l1.dksumup = false;
        stats[MysqlX.getLevelId(Level.allcodes)] = l1;
        
        Statistics l2 = new Statistics();
        if (level==Level.positive || level==Level.allStats) l2.included = true;
        l2.level = Level.positive;
        l2.isumup = true;
        l2.dksumup = false;
        stats[MysqlX.getLevelId(Level.positive)] = l2;
        
        Statistics l3 = new Statistics();
        if (level==Level.intervals || level==Level.allStats) l3.included = true;
        l3.level = Level.intervals;
        l3.isumup = true;
        l3.dksumup = false;
        stats[MysqlX.getLevelId(Level.intervals)] = l3;

        Statistics l4 = new Statistics();
        if (level==Level.danish || level==Level.allStats) l4.included = true;
        l4.level = Level.danish;
        l4.isumup = false;
        l4.dksumup = true;
        stats[MysqlX.getLevelId(Level.danish)] = l4;
    	return stats;
	}
	
    public static Set<String> writeStatFiles(File mergedDir, StatType stattype, MergedStatsResult res, Source src, StatHadoops sh, Display display, String machine, IgnoreFile ignoreFile) throws IOException {
        Set<String> resFileSet = new HashSet<String>();
        for (Statistics st : res.mergedStats) {
			if (st.included) {
		        //sort key set
				List<Integer> sortedKeys = new ArrayList<Integer>(st.countMap.size());
		        sortedKeys.addAll(st.countMap.keySet());
		        Collections.sort(sortedKeys);
		        
		        //write stat(s) to file(s)
		        if (display==Display.onlyCodes || display==Display.allDisplays) {
		        	File lf = createMergedStatFile(mergedDir, stattype, src, sh, st.level, Display.onlyCodes, machine, ignoreFile);
			        MysqlStatExtract.writeStatFile(lf, stattype, st, sortedKeys, Display.onlyCodes, res.tableSet);
			        resFileSet.add(lf.getName()); 
		        }
		        
		        if (display==Display.noCodes || display==Display.allDisplays) {
		        	File lf = createMergedStatFile(mergedDir, stattype, src, sh, st.level, Display.noCodes, machine, ignoreFile);
			        MysqlStatExtract.writeStatFile(lf, stattype, st, sortedKeys, Display.noCodes, res.tableSet);
			        resFileSet.add(lf.getName()); 
		        }

		        if (display==Display.inText || display==Display.allDisplays) {
		        	File lf = createMergedStatFile(mergedDir, stattype, src, sh, st.level, Display.inText, machine, ignoreFile);
			        MysqlStatExtract.writeStatFile(lf, stattype, st, sortedKeys, Display.inText, res.tableSet);
			        resFileSet.add(lf.getName()); 
		        }
		        
		        if (display==Display.separateText || display==Display.allDisplays) {
		        	File lf = createMergedStatFile(mergedDir, stattype, src, sh, st.level, Display.separateText, machine, ignoreFile);
			        MysqlStatExtract.writeStatFile(lf, stattype, st, sortedKeys, Display.separateText, res.tableSet);
			        resFileSet.add(lf.getName()); 
		        }
			} // if included
		} // for stats
        
        return resFileSet;
	}
	
    public static File createMergedStatFile(File MergeDir, StatType stattype, Source src, StatHadoops sh, Level level, Display display, String machine, IgnoreFile ignoreFile) throws IOException {
    	String fn = getMergedStatFilename(stattype, MysqlIngester.ingest_current_update_no, src, sh, level, display) 
    			+ (machine.equals("all")?"":"_"+machine)
	    		+ MysqlX.txtfile_suffix;
	    File writeFile = new File(MergeDir.getAbsolutePath() + "/" + fn);
		if (writeFile.exists()) {
			if (ignoreFile.equals(IgnoreFile.if_false)) {
	            System.err.println("ERROR: merge file allready existed " + writeFile.getAbsolutePath() + writeFile.getName());
	            System.exit(1);
			} else if (ignoreFile.equals(IgnoreFile.if_warning)) {
	            System.out.println("WARNING: merge file allready existed " + writeFile.getAbsolutePath());
			} 
		} 
		writeFile.createNewFile();
		return writeFile;
	} 
	
    private static String getRowParts(String string) {
        String[] parts = string.split(row_delim);
        if (parts.length>1) return parts[1];
        else return "";
    }

    public static String stat_filename_levelpart(Level level)  {
	    String txt ="";
	    if (level == Level.allcodes) txt="allcodes"; 
	    else if (level == Level.positive) txt="positive";
	    else if (level == Level.intervals) txt="intervals";
	    else if (level == Level.danish) txt="danish";
	    else txt="ERRORLEVEL";
	    return txt;
    }

    public static String stat_filename_displaypart(Display d)  {
	    String txt ="";
        if (d == Display.inText) txt="inText"; 
        else if (d == Display.noCodes) txt="noCodes";
        else if (d == Display.onlyCodes) txt="onlyCodes";
        else if (d == Display.separateText) txt="separateText";
        else txt="ERRORDISPLAY";
	    return txt;
    }

    public static String getMergedStatusFilename(String seqno, Level level, Display d) {
    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    	Date now = new Date(System.currentTimeMillis());
    	return MysqlX.status_fileprefix
					+ "V" + seqno + "_"
					+ "L" + stat_filename_levelpart(level) + "_"
					+ "D" + stat_filename_displaypart(d) + "_"
					+ "T" + dateFormat.format(now);
    }
    
    public static String getMergedStatFilename(StatType statType, String seqno, Source src, StatHadoops sh, Level level, Display d) {
    	if (statType==StatType.stat_status) {
    		System.err.println("Coding error!"); System.exit(1);
    	}
    		
    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    	Date now = new Date(System.currentTimeMillis());
    	String s = (statType==StatType.stat_kage 
    				 ? MysqlX.lagkage_fileprefix 
    				 : MysqlX.frequence_fileprefix
    			    )
    			    + MysqlX.getSourceInfix(src)
    			    + getShAbbr(sh) 
	    			+ "V" + seqno + "_"
	        		+ "L" + stat_filename_levelpart(level) + "_"
	        		+ "D" + stat_filename_displaypart(d) + "_"
					+ "T" + dateFormat.format(now);
        return s;
    }
    
    
    public static class MergedStatsResult { 
        Statistics[] mergedStats = new Statistics[3];
	    Set<String> tableSet = new HashSet<String>();
        long total = 0;
        long total0 = 0;
    }
}
