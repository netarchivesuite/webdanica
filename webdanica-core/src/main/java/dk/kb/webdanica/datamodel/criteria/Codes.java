package dk.kb.webdanica.datamodel.criteria;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import dk.kb.webdanica.oldtools.MysqlRes;

public class Codes {
	
	/**
	 * Constants for belonging to different categories.
	 */
	public static int cat_ERROR_dk = 9000; //getCodesForUdgaaede
    public static int cat_ignored_dk = 9100; //getCodesForFrasorterede
    public static int cat_not_likely_dk = 9101; //getCodesForNOTDanishResults
    public static int cat_unknown_dk = 9200;	//Not decided 0 and negative
    public static int cat_maybes_dk = 9500;	//getCodesForMaybees
    public static int cat_likely_dk = 9999; //getCodesForDanishResults

    //cat_likely_dk
    public static Set<Integer> getCodesForDanishResults() {
		Set<Integer> codeSet = new HashSet<Integer>();
	    for (int code=20; code<=27; code++) {
	    	codeSet.add(code);
	    }
	    for (int code=40; code<=47; code++) {
	    	codeSet.add(code);
	    }
	    codeSet.add(72); 
	    codeSet.add(110); 
	    codeSet.add(120); 
	    codeSet.add(123); 
	    codeSet.add(126); 
	    codeSet.add(310); 
	    codeSet.add(315); 
	    codeSet.add(320); 
	    return codeSet;
	}

    //cat_ignored_dk
	public static Set<Integer> getCodesForFrasorterede() {
		Set<Integer> codeSet = new HashSet<Integer>();
	    codeSet.add(1); 
	    codeSet.add(3); 
	    return codeSet;
	}
		
    //ERROR_dk
	public static Set<Integer> getCodesForUdgaaede() {
		Set<Integer> codeSet = new HashSet<Integer>();
	    codeSet.add(5); 
	    codeSet.add(6); 
	    codeSet.add(7); 
	    codeSet.add(8); 
	    for (int code=70; code<=79; code++) {
	    	codeSet.add(code);
	    }
	    for (int code=200; code<=203; code++) {
	    	codeSet.add(code);
	    }
	    for (int code=206; code<=209; code++) {
	    	codeSet.add(code);
	    }
	    return codeSet;
	}
	
	//cat_maybes_dk
	public static Set<Integer> getCodesForMaybees() {
		Set<Integer> codeSet = new HashSet<Integer>();
	    codeSet.add(111); 
	    codeSet.add(121); 
	    codeSet.add(124); 
	    codeSet.add(127); 
	    codeSet.add(130);
	    codeSet.add(230);
	    codeSet.add(311); 
	    codeSet.add(313); 
	    codeSet.add(316); 
	    codeSet.add(318); 
	    codeSet.add(321); 
	    codeSet.add(322); 
	    codeSet.add(323); 
	    codeSet.add(324); 
	    codeSet.add(326); 
	    return codeSet;
	}
	
	//cat_not_likely_dk
	public static Set<Integer> getCodesForNOTDanishResults() {
		Set<Integer> codeSet = new HashSet<Integer>();
	    codeSet.add(2); //ignore 1 and 3
	    for (int code=10; code<=12; code++) {
	    	codeSet.add(code);
	    }
	    for (int code=30; code<=35; code++) {
	    	codeSet.add(code);
	    }
	    codeSet.add(38); 
	    for (int code=50; code<=55; code++) {
	    	codeSet.add(code);
	    }
	    codeSet.add(58); 
	    for (int code=100; code<=107; code++) {
	    	codeSet.add(code);
	    }
	    codeSet.add(112); 
	    codeSet.add(122); 
	    codeSet.add(125); 
	    codeSet.add(128); 
	    codeSet.add(220); 
	    codeSet.add(301); 
	    codeSet.add(302);
	    codeSet.add(312); 
	    codeSet.add(317); 
	    codeSet.add(327); 
	    return codeSet;
	}
	
	
	 // From MysqlUrlsFindFromAll
	
	// args: table=all|<tablename> "
	//+ "urlfile=<file with url to be found> "
	// + "incldate=true|false "
	//+ "inclCode=<code - 0 if none> "
	//+ "withvalues=<true|false>"
	//+ "ignoreFile=true|false ";

	
	public static void getResults() throws SQLException, IOException {
		String tablename = "unused";

		/****** 
    	/** Set of tables to find url in */
		System.out.println("*** Finding tables ");
		Set<String> tableSet = new HashSet<String>();
		if (tablename.equals("all")) {
			//tableSet = MysqlRes.getTables(conn);
		} else {
			tableSet.add( tablename.trim() );
		}

		 
		/** Find url */
		System.out.println("*** Finding url ");
		Set<CriteriaKeyAndTableForUrl> resSet = new HashSet<CriteriaKeyAndTableForUrl>();
		boolean incldate= true;
		boolean withvalues = true;
		File urlfile = null; // file with list of urls to find
		IgnoreFile ignoreFile = IgnoreFile.if_true; // ignore and override any existing file.
		int code = 0;
		Connection conn = null;

		resSet = getResesForSearch(conn, tableSet, urlfile, incldate, code, withvalues);

 
    	/** Write Set of Urls to file */
		System.out.println("*** writing outfile ");
		/*
    	String filename = writeTestReses(outDir, urlfile, incldate, tableSet, resSet, ignoreFile, withvalues);
		System.out.println("- resulted in file:" + filename);
		 */

		//conn.close();
	}
	/*
	public static String writeTestReses(File outDir, File urlfile, boolean incldate, Set<String> tableSet, Set<CriteriaKeyAndTableForUrl> resSet, IgnoreFile ignoreFile, boolean withvalues) throws IOException {
		//create file
        //String fn = getFilename(machine, urlfile); // + MysqlX.txtfile_suffix;
		String fn = "dummy"; // so this compiles
        File writeFile = new File(outDir.getAbsolutePath() + "/" + fn);
		if (writeFile.exists() && ignoreFile==IgnoreFile.if_false) {
            System.err.println("ERROR: file allready existed " + writeFile.getAbsolutePath() + writeFile.getName());
            System.exit(1);
		} else {
			writeFile.createNewFile();
		}
		
		FileWriter fw = new FileWriter(writeFile.getAbsoluteFile());
        BufferedWriter  bw = new BufferedWriter(fw);        

        //make lines and write them

        String s =  "Tables: " + StringUtils.join(tableSet, ", ");
        bw.write(s);
		bw.newLine();

		for (CriteriaKeyAndTableForUrl res: resSet) {
			bw.write("*****************************************************");
			bw.newLine();
			
			if (res.itemSet.size()==0) s = "NO URL";
			else s = "FOUND URL";
			s = s + ":" + res.url + (incldate ? ", date:" + res.date : "");
			bw.write(s);
			bw.newLine();
				
	        s = (res.itemSet.size()==0 ? "" : res.itemSet.size() + " times ") 
	        		+ (incldate ? "" : "(parm. date: '" + res.date + "')");
	        if (s.trim().length()>0) {
				bw.write(s);
				bw.newLine();
	        }
			
			if (!res.comment.isEmpty()) {
		        bw.write("comment: '" + res.comment + "'");
				bw.newLine();
			}
			
			//System.out.println("resSet.count " + resSet.size());
			int i = 1;
			for (CriteriaKeyAndTable item: res.itemSet) {
				//bw.write("*** " + i + ". url machine: " + machine + " table: " + item.tablename);
				bw.newLine();
				i++;
				
				bw.write("url: " + (item.urlOrig.isEmpty() ? item.url : item.urlOrig) + " date: " + item.Cext3Orig);
				bw.newLine();
				
				if (withvalues) {
					String urlTxt = (item.allres.urlOrig.isEmpty() ? item.allres.url : item.allres.urlOrig);
					// IA-url fx. http://web.archive.org/web/20120812090132/http://www.yachtworld.com/boats/1975/Hubber-Custom-Sport-Fisherman-2398590/Miami/FL/United-States 
					// dvs. web.archive.org/web/[date]/[URL]
					String IAUrl = "http://web.archive.org/web/" + item.allres.Cext3Orig + "/" + urlTxt;

					if (withvalues) {
						bw.write(
								( item.allres.IsIASource ? IAUrl : urlTxt ) 
			        			+ row_delim + urlTxt  
			        			+ row_delim + item.allres.Cext3Orig
			        			//+ MysqlX.row_delim + machine
			        			+ row_delim + item.allres.tablename
								+ row_delim + item.allres.getValuesInString(row_delim, ":")
						); //41 + 2
					} else {
				        bw.write(IAUrl 
				        			+ row_delim + urlTxt  
				        			+ row_delim + item.allres.Cext3Orig  
				        			//+ MysqlX.row_delim + machine
				        			+ row_delim + item.allres.tablename
				        			+ row_delim + item.allres.calcDanishCode
				        		); // 3 rows + 2
					}
					bw.newLine();
				}
			}
		}
		bw.close();
		return outDir.getAbsolutePath() + "/" + fn;
	}
*/
	
	/**
	 * Get 
	 * 
	 * @param conn
	 * @param tableSet
	 * @param url
	 * @param date
	 * @param incldate
	 * @param code
	 * @param withvalues
	 * @return
	 * @throws SQLException
	 */
	private static Set<CriteriaKeyAndTable> getResForSearch(Connection conn, Set<String> tableSet, String url, String date, boolean incldate, int code, boolean withvalues) throws SQLException {
		Set<CriteriaKeyAndTable> resUrlSet = new HashSet<CriteriaKeyAndTable>();
		for (String nextTable: tableSet) {
            //System.out.println("- processing table: " + nextTable);
			if (nextTable.startsWith("")) { //nextTable.startsWith(MysqlRes.wf_table_prefix))
				String selectSQL = "";
				String urlExp = (url.endsWith("%") ? "Url LIKE ?" : "Url = ?");
	 	    	if (incldate) {
		 	       	selectSQL = "SELECT * FROM " + nextTable + " WHERE " + urlExp + " AND extWDate = ? ";
	 	    	} else {
					selectSQL = "SELECT * FROM " + nextTable + " WHERE " + urlExp;
	 	    	} 
	 	    	Set<String> sqlSet = new HashSet<String>();
	 	    	Set<Integer> codeSet = new HashSet<Integer>();
	 	    	if (code==0) { 
	 	    		codeSet.add(0);
	 	    	} else {
	 	    		if (code == Codes.cat_ignored_dk)	
		 	    		codeSet = Codes.getCodesForFrasorterede();	//9100; //getCodesForFrasorterede
	 	    		else if (code == Codes.cat_not_likely_dk)	 // = 9101;
	 	    			codeSet = Codes.getCodesForNOTDanishResults();
	 		 	    else if (code == Codes.cat_maybes_dk)		// = 9500;
		 	    		codeSet = Codes.getCodesForMaybees();
			 		else if (code == Codes.cat_likely_dk)	// = 9999
		 	    		codeSet = Codes.getCodesForDanishResults();
			 		else { 
		 	    		//case MysqlX.cat_unknown_dk:	not implemented 	//= 9200; Not decided 0 and negative
	 	               System.err.println("illegal code setting: " + code);
	 	               System.exit(1);
	 	    		}
	 	    	}
	 	    	if (code == 0) {
 	    			sqlSet.add(selectSQL); // 
	 	    	} else { // Generate select for the individual DanishCodes selected
	 	    		for (int calcCode: codeSet) {
	 	    			sqlSet.add(selectSQL + " AND calcDanishCode=" + calcCode);
	 	    		}
	 	    	}

	 	    	
	 	    	for (String sql: sqlSet) {
		 	        PreparedStatement r = conn.prepareStatement(sql); 
		 	    	if (incldate) {
			 	        r.setString(1, url);
			 	        r.setTimestamp(2, SingleCriteriaResult.findDateFromString(date));
		 	    	} else {
			 	        r.setString(1, url);
		 	    	} 
		 	    	ResultSet rs = r.executeQuery();
			        while (rs.next()) {
		 	    		CriteriaKeyAndTable res = new CriteriaKeyAndTable();
				        res.url = rs.getString("Url");
				        res.urlOrig = rs.getString("UrlOrig");
				        res.Cext3 = rs.getTimestamp("extWDate");
				        res.Cext3Orig = rs.getString("extWDateOrig");
				        res.tablename = nextTable;
				        if (withvalues) res.allres = new SingleCriteriaResult(rs, true);
				        resUrlSet.add(res);
					}
			        r.close();
				}
			}
		}
		return resUrlSet;
	}

	private static Set<CriteriaKeyAndTableForUrl> getResesForSearch(Connection conn, Set<String> tableSet, File urlfile, boolean incldate, int code, boolean withvalues) 
			throws SQLException, IOException {
		Set<CriteriaKeyAndTableForUrl> resSet = new HashSet<CriteriaKeyAndTableForUrl>();
		
        BufferedReader fr = new BufferedReader(new FileReader(urlfile));        
        String line ="";
        String trimmedLine = null;
        int linecount = 0;
    
        //read file and ingest
        while ((line = fr.readLine()) != null) {
            trimmedLine = line.trim();
            if (!trimmedLine.isEmpty()) {
                CriteriaKeyAndTableForUrl res = new CriteriaKeyAndTableForUrl();
                
                // read parts from line
                String[] parts = trimmedLine.split("#");
                if (parts.length > 3) {
                    System.err.println("Ill formatted line - more than 3 parts - got " + trimmedLine);
                    System.exit(1);
                }
                
        		res.url = parts[0].trim();
        		res.date = "";
        		if (parts.length > 1) {
        			res.date = parts[1].trim();
        			if (MysqlRes.checkDateFromString(res.date)) {
            			if (parts.length > 2) {
            				res.comment = parts[2].trim();
            			}
        			} else {
            			if (parts.length > 2) {
                            System.err.println("Ill formatted line - more than 2 parts and no date - got " + trimmedLine);
                            System.exit(1);
            			}
        				res.comment = res.date;
        				res.date = "";
        			}
        		}
        		
                // check consistency
        		if (res.date.isEmpty() && incldate) {
                    System.err.println("Ill formatted line where date was expected date - got " + trimmedLine);
                    System.exit(1);
                }
        		
                // find items for line
        		res.itemSet = getResForSearch(conn, tableSet, res.url, res.date, incldate, code, withvalues);
        		
        		resSet.add(res);
				linecount++;
			}
            System.out.println("processed " + linecount + " urls");
        }
        fr.close();
		return resSet;
	}

	/*
    private static String getFilename(String machine, File file) {
    	//SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    	//Date now = new Date(System.currentTimeMillis());
    	String s = MysqlX.urlfind_fileprefix 
    			+ "M" + machine + "_"
    			+ file.getName();
        return s;
    }
    */
	public static String row_delim = "#";
}
