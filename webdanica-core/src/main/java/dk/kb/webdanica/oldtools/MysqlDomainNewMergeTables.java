package dk.kb.webdanica.oldtools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
//import java.text.SimpleDateFormat;
import java.util.*;
import java.io.IOException;

import dk.kb.webdanica.oldtools.MysqlRes.DomNewSet;
import dk.kb.webdanica.oldtools.MysqlRes.DomainNewLevel;
import dk.kb.webdanica.oldtools.MysqlX.*;

public class MysqlDomainNewMergeTables {
    /**
     * @param args <JDBC-URL> jdbcUser=<JDBC-username> machine=<machine-name> mergetype=SrcCodes_to_Src|(allOnSource)|Src_to_New|Src_to_NYE|(noSourceOnCodes) level=all|l1|l2|l1_l2|l3
     * @throws ClassNotFoundException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     * @throws SQLException 
     * @throws IOException 
     */
	
    enum MergeType {
    	SrcCodes_to_Src_nye, 
    	SrcCodes_to_Src_new, 
    	SrcCodes_to_Codes, 
    	Src_to_New, 
    	Src_to_Nye,
    	Codes_to_Nye,
    	NewMachine_to_ALL,
    	NyeMachine_to_ALL,
    	none
    }
    
    public static class MergeUnit { 
        int level = 0;
        String targetTable = "";
    	Set<String>  sourceTableSet = new HashSet<String>();
    }
    
	// output on form Url # code # forklaring af code?????
    public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, SQLException {
        /*****************************************/
        /*** Arguments ***************************/
        /*****************************************/
    	String errArgTxt = "Proper args: <JDBC-URL> jdbcUser=<JDBC-username> "
    			+ "machine=<machine-name> "
    			+ "mergetype=SrcCodes_to_Src_nye|SrcCodes_to_Src_new|Src_to_New|Src_to_Nye|SrcCodes_to_Codes|Codes_to_Nye|NewMachine_to_ALL|NyeMachine_to_ALL"
    			+ "level=all|l1|l1_l2|l2|l3 ";
        if (args.length < 5) {
            System.err.println("Missing args!");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        if (args.length > 5) {
            System.err.println("Too many args!");
            System.err.println(errArgTxt);
            System.exit(1);
        }

        String jdbcUrl = args[0];
        if (!jdbcUrl.startsWith("jdbc:mysql:")) {
            System.err.println("Missing arg jdbc setting starting with 'jdbc:mysql'");
            System.err.println(errArgTxt);
            System.exit(1);
        }

        String jdbcUser = args[1];
        if (!jdbcUser.startsWith("jdbcUser=")) {
            System.err.println("Missing arg jdbcUser setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        jdbcUser = MysqlX.getStringSetting(jdbcUser).toLowerCase();
        
        Class.forName ("com.mysql.jdbc.Driver").newInstance ();
        Connection conn = DriverManager.getConnection (jdbcUrl, jdbcUser, "");    
        
        /**** args - machine ****/
        /** arg - machine name **/
        String machine = args[2];
        if (!machine.startsWith("machine=")) {
            System.err.println("ERROR: Missing arg machine setting - got " + machine);
            System.err.println(errArgTxt);
            System.exit(1);
        }
        machine = MysqlX.getStringSetting(machine);

        /**** args - merge-type ****/
        MergeType mt = MergeType.none;
    	String opTxt = args[3];
        if (!opTxt.startsWith("mergetype=")) {
            System.err.println("Missing arg mergetype setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        opTxt = MysqlX.getStringSetting(opTxt);
        if (opTxt.startsWith("SrcCodes_to_Src_new")) {
        	mt = MergeType.SrcCodes_to_Src_new;      
        } else if (opTxt.startsWith("SrcCodes_to_Src_nye")) {
            mt = MergeType.SrcCodes_to_Src_nye;      
        } else if (opTxt.startsWith("Src_to_New")) {
        	mt = MergeType.Src_to_New;
        } else if (opTxt.startsWith("Src_to_Nye")) {
        	mt = MergeType.Src_to_Nye;
        } else if (opTxt.startsWith("SrcCodes_to_Codes")) {
        	mt = MergeType.SrcCodes_to_Codes;
        } else if (opTxt.startsWith("Codes_to_Nye")) {
        	mt = MergeType.Codes_to_Nye;
        } else if (opTxt.startsWith("NewMachine_to_ALL")) {
        	mt = MergeType.NewMachine_to_ALL;
        } else if (opTxt.startsWith("NyeMachine_to_ALL")) {
        	mt = MergeType.NyeMachine_to_ALL;
        } else {
            System.err.println("Arg updateOperation setting is NOT valid or not implemented - got '" + opTxt + "'");
            System.err.println(errArgTxt);
            System.exit(1);
        }

        /**** args - level ****/
        LookupLevel pLevel = LookupLevel.all;
        String pLevelTxt = args[4];
        if (!pLevelTxt.startsWith("level=")) {
            System.err.println("Missing arg level setting - got " + pLevelTxt);
            System.err.println(errArgTxt);
            System.exit(1);
        }
        pLevelTxt = MysqlX.getStringSetting(pLevelTxt);
    	if (pLevelTxt.equals("all")) {
    		pLevel = LookupLevel.all;        	
        } else if (pLevelTxt.equals("l1")) {
        	pLevel = LookupLevel.l1;      
        } else if (pLevelTxt.equals("l2")) {
        	pLevel = LookupLevel.l2;      
        } else if (pLevelTxt.equals("l1_l2")) {
        	pLevel = LookupLevel.l1_l2;      
        } else if (pLevelTxt.equals("l3")) {
        	pLevel = LookupLevel.l3;      
        } else {
            System.err.println("Arg level setting is NOT valid - got '" + pLevelTxt + "'");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        
        /*****************************************/
        /*** Start processing ********************/
    	/*** Writing domain levels into files for likely Danish of finished table data ***/ 
        /*****************************************/
        
    	//SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    	//Date now = new Date(System.currentTimeMillis());

		Set<Integer> dkCodeSet = MysqlX.getCodesForDanishResults();
        Set<String> allTableSet = MysqlRes.getTables(conn);
        System.out.println("*** Finding tables ");
        
        /******  */
        if (mt.equals(MergeType.SrcCodes_to_Src_new) || mt.equals(MergeType.SrcCodes_to_Src_nye)) {
            for (int level=1;level<=MysqlX.noDomainLevels;level++) {
            	if ( !MysqlX.skipLevel(pLevel, level) ) {
                    System.out.println("*** Finding mergeunits for SrcCodes_to_Src on level " + level);
                    
                    /** Set of tables to extract URLs from */
	            	MergeUnit muIA = findMergeSrcUnitFromCodesSrcUnits(conn, allTableSet, level, Source.IA, mt.equals(MergeType.SrcCodes_to_Src_nye));
	            	MergeUnit muNA = findMergeSrcUnitFromCodesSrcUnits(conn, allTableSet, level, Source.NAS, mt.equals(MergeType.SrcCodes_to_Src_nye));

	            	/** merge to target tables */
	            	if (!muIA.targetTable.isEmpty()) 
	            		mergeCodeSrcTablesToSrc(conn, level, Source.IA, muIA);
	            	else
	                    System.out.println("*** No IA tabels found for level " + level);
	            	if (!muNA.targetTable.isEmpty()) 
	            		mergeCodeSrcTablesToSrc(conn, level, Source.NAS, muNA);
	            	else
	                    System.out.println("*** No NA tabels found for level " + level);
    	        } else {
                    System.out.println("*** ignoring level " + level);
    	        }
	        }
        }
        
        if (mt.equals(MergeType.SrcCodes_to_Codes) ) {
            for (int level=1;level<=MysqlX.noDomainLevels;level++) {
            	if ( !MysqlX.skipLevel(pLevel, level) ) {
                    System.out.println("*** Finding mergeunits for SrcCodes_to_Codes on level " + level);
	            	Set<MergeUnit> muCodes = new HashSet<MergeUnit>(); 
	            	
	            	for (int code: dkCodeSet) {
	            		MergeUnit mu = new MergeUnit();
	            		mu.targetTable = MysqlRes.domainNyeTableName(level, code);
	                	if (!allTableSet.contains(mu.targetTable)) {
	                		MysqlRes.createDomainNewLevelTable(conn, mu.targetTable);
	                		allTableSet.add(mu.targetTable);
	                		for (String table: allTableSet) {
	                    		if (table.startsWith(MysqlRes.domaintable_prefix) 
	                    				&& table.contains("_" + MysqlRes.domaintable_level_infix + level)
	                    				&& (table.contains("_" + Source.IA.name()) || table.contains("_" + Source.NAS.name()))
	                    				&& table.contains("_C" + code) 
	                    				&& table.contains("_NNYYEE")
	                    		   ) 
	                    		{
	                    			mu.sourceTableSet.add(table);
	                    		}
	                		}
	                	} else {
	            	        System.out.println("-- table " + mu.targetTable + " already existed - therefore ignored");
	            	        mu.targetTable = "";
	            		}
	                	muCodes.add(mu);
	            	}
	            			
	            	/** merge to target tables */
	            	for (MergeUnit mu: muCodes) {
		            	if (!mu.targetTable.isEmpty())
		            		mergeTablesToSingle(conn, level, mu);
		            	else
		                    System.out.println("*** No tabels found for level " + level);
	    	        }
    	        }
	        }
        }
        
        if (mt.equals(MergeType.Codes_to_Nye)) {
            for (int level=1;level<=MysqlX.noDomainLevels;level++) {
            	if ( !MysqlX.skipLevel(pLevel, level) ) {
                    System.out.println("*** Finding mergeunits for Codes_to_Nye on level " + level);

                    /** Set of tables to extract URLs from */
	            	MergeUnit mu = new MergeUnit();
	            	mu.targetTable = MysqlRes.domainNyeTableName(level);
	            	if (!allTableSet.contains(mu.targetTable)) {
	            		MysqlRes.createDomainNewLevelTable(conn, mu.targetTable);
	            		allTableSet.add(mu.targetTable);
	                    for (String table: allTableSet) {
	                		if (table.startsWith(MysqlRes.domaintable_prefix) 
	                				&& table.contains("_" + MysqlRes.domaintable_level_infix + level)
	                				&& !(table.contains("_" + Source.IA.name()) || table.contains("_" + Source.NAS.name()))
	                				&& table.contains("_C") 
	                				&& !table.contains("_LU") 
	                				&& !table.contains("_DM") 
	                				&& !table.contains("_NEW")
	                				&& !table.contains("_NYE")
	                				&& table.contains("_NNYYEE")
	                		   ) 
	                		{
	                			mu.sourceTableSet.add(table);
	                		}
	            		}
	            	} else {
	                    System.out.println("-- table " + mu.targetTable + " already existed - therefore ignored");
	                    mu.targetTable = "";
	            	}

	            	/** merge to target tables */
	            	if (!mu.targetTable.isEmpty())
	            		mergeTablesToSingle(conn, level, mu);
	            	else
	                    System.out.println("*** No tabels found for level " + level);
    	        }
	        }
        }
       
        
        if (mt.equals(MergeType.Src_to_New) || mt.equals(MergeType.Src_to_Nye)) {
            for (int level=1;level<=MysqlX.noDomainLevels;level++) {
            	if ( !MysqlX.skipLevel(pLevel, level) ) {
                    System.out.println("*** Finding mergeunits for Src_to_New on level " + level);

                    /** Set of tables to extract URLs from */
                    String trgTable = "";
                    if (mt.equals(MergeType.Src_to_New)) trgTable = MysqlRes.domainNewTableName(level);
                    if (mt.equals(MergeType.Src_to_Nye)) trgTable = MysqlRes.domainNyeTableName(level);
	            	MergeUnit mu = findMergeUnitFromSrcUnits(conn, trgTable, allTableSet, level, mt.equals(MergeType.Src_to_Nye));

	            	/** merge to target tables */
	            	if (!mu.targetTable.isEmpty())
	            		mergeTablesToSingle(conn, level, mu);
	            	else
	                    System.out.println("*** No tabels found for level " + level);
    	        }
	        }
        }
        
        if (mt.equals(MergeType.NewMachine_to_ALL) || mt.equals(MergeType.NyeMachine_to_ALL)) {
        	for (int level=1;level<=MysqlX.noDomainLevels;level++) {
            	if ( !MysqlX.skipLevel(pLevel, level) ) {
                    System.out.println("*** Finding mergeunits for NewMachine_to_ALL on level " + level);

            		/** Set of tables to extract URLs from */
                    MergeUnit mu = new MergeUnit();
                    if (mt.equals(MergeType.NewMachine_to_ALL))
                         mu = findMergeUnitFromMachineUnitsNew(conn, allTableSet, level);
                    if (mt.equals(MergeType.NyeMachine_to_ALL)) 
                    	mu = findMergeUnitFromMachineUnitsNye(conn, allTableSet, level);

	            	/** merge to target tables */
	            	if (!mu.targetTable.isEmpty()) 
	            		mergeTablesToSingle(conn, level, mu);
	            	else
	                    System.out.println("*** No tabels found for level " + level);
                }
            }
        }

        conn.close();
		System.out.println("*** Finished");
    }

	private static MergeUnit findMergeSrcUnitFromCodesSrcUnits(Connection conn, Set<String> allTableSet, int level, Source src, boolean nye) throws SQLException {
    	MergeUnit mu = new MergeUnit();
    	if (nye) mu.targetTable = MysqlRes.domainNyeTableName(level, src); 
    	else mu.targetTable = MysqlRes.domainNewTableName(level, src); 
    	if (!allTableSet.contains(mu.targetTable)) {
    		MysqlRes.createDomainNewLevelTable(conn, mu.targetTable);
    		allTableSet.add(mu.targetTable);
    		for (String table: allTableSet) {
        		if (table.startsWith(MysqlRes.domaintable_prefix) 
        				&& table.contains("_" + MysqlRes.domaintable_level_infix + level)
        				&& (table.contains("_" + src.name()))
        				&& table.contains("_C") 
        		   ) 
        		{
        			if (nye && table.contains("_NNYYEE")) mu.sourceTableSet.add(table);
        			else if ( (!nye) && (!table.contains("_NNYYEE")) ) mu.sourceTableSet.add(table);
        		}
    		}
    	} else {
	        System.out.println("-- table " + mu.targetTable + " already existed - therefore ignored");
	        mu.targetTable = "";
		}
        return mu;
	}

	private static MergeUnit findMergeUnitFromSrcUnits(Connection conn, String trgtable, Set<String> allTableSet, int level, boolean nye) throws SQLException {
    	MergeUnit mu = new MergeUnit();
    	mu.targetTable = trgtable; 
    	if (!allTableSet.contains(mu.targetTable)) {
    		MysqlRes.createDomainNewLevelTable(conn, mu.targetTable);
    		allTableSet.add(mu.targetTable);
            for (String table: allTableSet) {
        		if (table.startsWith(MysqlRes.domaintable_prefix) 
        				&& table.contains("_" + MysqlRes.domaintable_level_infix + level)
        				&& (table.contains("_" + Source.IA.name()) || table.contains("_" + Source.NAS.name()))
        				&& !table.contains("_C") 
        				&& !table.contains("_LU") 
        				&& !table.contains("_DM") 
        				&& !table.contains("_NEW")
        				&& !table.contains("_NYE")
        		   ) 
        		{
        			if (nye && table.contains("_NNYYEE")) mu.sourceTableSet.add(table);
        			else if ( (!nye) && (!table.contains("_NNYYEE")) ) mu.sourceTableSet.add(table);
        		}
    		}
    	} else {
            System.out.println("-- table " + mu.targetTable + " already existed - therefore ignored");
            mu.targetTable = "";
    	}
        return mu;
	}

	private static MergeUnit findMergeUnitFromMachineUnitsNye(Connection conn, Set<String> allTableSet, int level) throws SQLException {
    	MergeUnit mu = new MergeUnit();
        for (String table: allTableSet) {
    		if (table.startsWith(MysqlRes.domaintable_prefix) 
    				&& table.contains("_" + MysqlRes.domaintable_level_infix + level)
    				&& !table.contains("_" + Source.IA.name())
    				&& !table.contains("_" + Source.NAS.name())
    				&& !table.contains("_C") 
    				&& !table.contains("_LU") 
    				&& !table.contains("_DM") 
    				&& !table.contains("_ALL") 
    				&& table.contains("_NYE")
    				&& table.contains("_M")
    		   ) 
    		{
    			mu.sourceTableSet.add(table);	
    		}
		}
        if (mu.sourceTableSet.size()==0) {
            System.out.println("No source tables found for " + mu.targetTable);
        	mu.targetTable = "";
        } else { 
        	mu.targetTable = MysqlRes.domainNyeTableName(level) + "_ALL"; 
        	if (!allTableSet.contains(mu.targetTable)) {
                System.out.println("creating target table" + mu.targetTable);
        		MysqlRes.createDomainNewLevelTable(conn, mu.targetTable);
        		allTableSet.add(mu.targetTable);
        	} else {
                System.out.println("clearing target table" + mu.targetTable);
        		MysqlRes.clearTable(conn, mu.targetTable);
        	}
        }
        return mu;
	}

	private static MergeUnit findMergeUnitFromMachineUnitsNew(Connection conn, Set<String> allTableSet, int level) throws SQLException {
    	MergeUnit mu = new MergeUnit();
        for (String table: allTableSet) {
    		if (table.startsWith(MysqlRes.domaintable_prefix) 
    				&& table.contains("_" + MysqlRes.domaintable_level_infix + level)
    				&& !table.contains("_" + Source.IA.name())
    				&& !table.contains("_" + Source.NAS.name())
    				&& !table.contains("_C") 
    				&& !table.contains("_LU") 
    				&& !table.contains("_DM") 
    				&& !table.contains("_ALL") 
    				&& table.contains("_NEW")
    				&& table.contains("_M")
    		   ) 
    		{
    			mu.sourceTableSet.add(table);	
    		}
		}
        if (mu.sourceTableSet.size()==0) {
            System.out.println("No source tables found for " + mu.targetTable);
        	mu.targetTable = "";
        } else { 
        	mu.targetTable = MysqlRes.domainNewTableName(level) + "_ALL"; 
        	if (!allTableSet.contains(mu.targetTable)) {
                System.out.println("creating target table" + mu.targetTable);
        		MysqlRes.createDomainNewLevelTable(conn, mu.targetTable);
        		allTableSet.add(mu.targetTable);
        	} else {
                System.out.println("clearing target table" + mu.targetTable);
        		MysqlRes.clearTable(conn, mu.targetTable);
        	}
        }
        return mu;
	}

	private static void mergeCodeSrcTablesToSrc(Connection conn, int level, Source src, MergeUnit mu)  throws SQLException {
		List<String> sortedTables = new ArrayList<String>(mu.sourceTableSet.size());
		sortedTables.addAll(mu.sourceTableSet);
        Collections.sort(sortedTables);
    	
        DomNewSet target = new  DomNewSet();
        target.level= level;
        target.src = (src.equals(Source.IA) ? DataSource.source_IA :  DataSource.source_NAS);
        target.code = 0;
        target.codeSet  = new HashSet<Integer>();
        target.table = mu.targetTable;
        target.domMap = new HashMap<String,DomainNewLevel>();
        
        System.out.println("*** Finding data for table " + target.table);
        for (String source_table : sortedTables) {
            DomNewSet source_domset = MysqlRes.readDomNewSet(conn, source_table);
            
            for (String key : source_domset.domMap.keySet()) {
            	DomainNewLevel src_dom = source_domset.domMap.get(key);
            	DomainNewLevel trg_dom = new DomainNewLevel(level);
            	if (target.domMap.containsKey(key)) {
            		trg_dom = target.domMap.get(key);
            		if (src.equals(Source.IA)) trg_dom.CntInIaData = trg_dom.CntInIaData + src_dom.CntInIaData;
            		else if (src.equals(Source.NAS)) trg_dom.CntInNasData = trg_dom.CntInNasData + src_dom.CntInNasData;
            		trg_dom.CalcCodes.add(src_dom.CalcCode);
            	} else {
            		trg_dom.copy(src_dom);
            		trg_dom.CalcCodes = new HashSet<Integer>();
            		trg_dom.CalcCodes.add(trg_dom.CalcCode);
            		trg_dom.CalcCode = 0;
            		target.domMap.put(key, trg_dom);
            	}
            }
        }
        System.out.println("*** Inserting into table " + target.table + " cnt keys: " + target.domMap.size());
        for (String key : target.domMap.keySet()) {
        	MysqlRes.insertDomainNewLevel(conn, target.table, target.domMap.get(key));
        }
    }
	
	private static void mergeTablesToSingle(Connection conn, int level, MergeUnit mu)  throws SQLException {
        DomNewSet target = new  DomNewSet();
        target.level= level;
        target.src = DataSource.source_none;
        target.code = 0;
        target.codeSet  = new HashSet<Integer>();
        target.table = mu.targetTable;
        target.domMap = new HashMap<String,DomainNewLevel>();
        
        System.out.println("*** Finding data for table " + target.table);
        System.out.println("*** getTableCntEntries for table " + MysqlRes.getTableCntEntries(conn, target.table));
        for (String source_table : mu.sourceTableSet) {
            DomNewSet source_domset = MysqlRes.readDomNewSet(conn, source_table);
            
            for (String key : source_domset.domMap.keySet()) {
            	key = key.toLowerCase().trim();
            	DomainNewLevel src_dom = source_domset.domMap.get(key);
            	DomainNewLevel trg_dom = new DomainNewLevel(level);
            	if (target.domMap.containsKey(key)) {
            		trg_dom = target.domMap.get(key);
            		trg_dom.CntInIaData = trg_dom.CntInIaData + src_dom.CntInIaData;
            		trg_dom.CntInNasData = trg_dom.CntInNasData + src_dom.CntInNasData;
            		trg_dom.CalcCodes.add(src_dom.CalcCode);

            		//if (!trg_dom.LookedUpInBerkeley) 
            	    //    System.out.println("*** not lookedup domain " + key + " in table " + target.table);
            	} else {
            		trg_dom.copy(src_dom);
            		target.domMap.put(src_dom.KeyDomain, trg_dom);
            	}
            }
        }
        System.out.println("*** Inserting into table " + target.table);
        for (String key : target.domMap.keySet()) {
        	DomainNewLevel trg_dom = target.domMap.get(key);
        	//System.out.println("*** table: " + target.table + " key: '" + key + "' Domain '" + trg_dom.Domain + "'");
        	MysqlRes.insertDomainNewLevel(conn, target.table, trg_dom);
        }
    }
}
