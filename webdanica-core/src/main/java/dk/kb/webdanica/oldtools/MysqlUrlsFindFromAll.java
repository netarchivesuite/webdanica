package dk.kb.webdanica.oldtools;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import dk.kb.webdanica.oldtools.MysqlRes.*;
import dk.kb.webdanica.oldtools.MysqlWorkFlow.*;

/*
String url = "jdbc:mysql://localhost/test|webdanica";
Class.forName ("com.mysql.jdbc.Driver").newInstance ();
Connection conn = DriverManager.getConnection (url, "username", "password");

urlfile on form
<url1> # <date1>
...
*/

public class MysqlUrlsFindFromAll {
    /**
     * @param args <JDBC-URL> jdbcUser=<JDBC-username> dbmachine=<machine-name> datadir=<dir for datadirs and urls dir> table=all|<tablename> urlfile=<file with url to be found> incldate=true|false inclCode=<code - 0 if none> withvalues=<true|false>gnoreFile=true|false   
     * @throws ClassNotFoundException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     * @throws SQLException 
     * @throws IOException 
     */
	
	// output on form Url # code # forklaring af code?????
    public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, SQLException {
        /*****************************************/
        /*** Arguments ***************************/
        /*****************************************/
    	String errArgTxt = "Proper args: <JDBC-URL> jdbcUser=<JDBC-username> "
    			+ "dbmachine=<dbmachine-name> "
    			+ "datadir=<dir for datadirs> "
    			+ "table=all|<tablename> "
    			+ "urlfile=<file with url to be found> "
    			+ "incldate=true|false "
    			+ "inclCode=<code - 0 if none> "
    			+ "withvalues=<true|false>"
    			+ "ignoreFile=true|false ";
        if (args.length < 10) {
            System.err.println("Missing args!");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        if (args.length > 10) {
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
        if (!machine.startsWith("dbmachine=")) {
            System.err.println("ERROR: Missing arg machine setting - got " + machine);
            System.err.println(errArgTxt);
            System.exit(1);
        }
        machine = MysqlX.getStringSetting(machine);

        /**** args - data-dir ****/
        String datadirTxt = args[3];
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
        File outDir = new File(dataDir.getAbsolutePath() + "/" + MysqlX.urlsfound_dir);
        if (!outDir.isDirectory()) {
            System.err.println("ERROR: The given datadir + " + MysqlX.urlsfound_dir + ": '"+ outDir.getAbsolutePath() + "' is not a proper directory or does not exist");
            System.err.println(errArgTxt);
            System.exit(1);
        }

    	/**** args - table(s) ****/
        /** arg - table all|<tablename> **/
        String tablename = args[4];
        if (!tablename.startsWith("table=")) {
            System.err.println("ERROR: Missing arg table setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        tablename = MysqlX.getStringSetting(tablename);

        /** args - urlfile **/
        String urlTxt = args[5];
        if (!urlTxt.startsWith("urlfile=")) {
            System.err.println("ERROR: Missing arg urlfile setting - got " + urlTxt);
            System.err.println(errArgTxt);
            System.exit(1);
        }
        urlTxt = MysqlX.getStringSetting(urlTxt);
        File urlfile = new File(urlTxt);
        if (!urlfile.isFile()) {
            System.err.println("ERROR: The given urlfile '" + urlfile.getAbsolutePath() + "' is not a proper file or does not exist");
            System.err.println(errArgTxt);
            System.exit(1);
        }

        /** arg - incldate **/
        String incldateTxt = args[6];
        if (!incldateTxt.startsWith("incldate=")) {
            System.err.println("Missing arg incldate setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        incldateTxt = MysqlX.getStringSetting(incldateTxt);
        boolean incldate = true;
        if (incldateTxt.equals("false"))  incldate = false; 
        else if (incldateTxt.equals("true")) incldate = true; 

        /** arg - inclCode=<code - 0 if none> **/
        String inclCodeTxt = args[7];
        if (!inclCodeTxt.startsWith("inclCode=")) {
            System.err.println("Missing arg inclCode setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        inclCodeTxt = MysqlX.getStringSetting(inclCodeTxt);
        int code = 0;
    	if (inclCodeTxt.matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+")) {
    		code = Integer.parseInt(inclCodeTxt);
    	} else {
            System.err.println("arg code is not an integer");
            System.err.println(errArgTxt);
            System.exit(1);
    	} 

        /** arg - withvalues **/
        String withvaluesTxt = args[8];
        if (!withvaluesTxt.startsWith("withvalues=")) {
            System.err.println("Missing arg ignoreFile setting");
            System.err.println(errArgTxt);
            System.exit(1);
        }
        withvaluesTxt = MysqlX.getStringSetting(withvaluesTxt);
        boolean withvalues = true;
        if (withvaluesTxt.equals("false"))  withvalues = false; 
        else if (withvaluesTxt.equals("true")) withvalues = true; 

        /**** args - ignore file ****/
        String ignoreFileTxt = args[9];
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
        
        /****** 
        /** Set of tables to find url in */
        System.out.println("*** Finding tables ");
        Set<String> tableSet = new HashSet<String>();
	    if (tablename.equals("all")) {
	    	tableSet = MysqlRes.getTables(conn);
	    } else {
	    	tableSet.add( tablename.trim() );
	    }
        
        /****** 
        /** Find url */
        System.out.println("*** Finding url ");
        Set<CriteriaKeyAndTableForUrl> resSet = new HashSet<CriteriaKeyAndTableForUrl>();
        resSet = getResesForSearch(conn, tableSet, urlfile, incldate, code, withvalues);
        
        /****** 
        /** Write Set of Urls to file */
        System.out.println("*** writing outfile ");
        String filename = writeTestReses(outDir, machine, urlfile, incldate, tableSet, resSet, ignoreFile, withvalues);
    	System.out.println("- resulted in file:" + filename);
    	
    	conn.close();
    }
    
	public static String writeTestReses(File outDir, String machine, File urlfile, boolean incldate, Set<String> tableSet, Set<CriteriaKeyAndTableForUrl> resSet, IgnoreFile ignoreFile, boolean withvalues) throws IOException {
		//create file
        String fn = getFilename(machine, urlfile); // + MysqlX.txtfile_suffix;
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

		String s =  "Machine: " + machine;
        bw.write(s);
		bw.newLine();

		s =  "Tables: " + MysqlX.getStringSequence(tableSet,MysqlX.tablename_delim);
        bw.write(s);
		bw.newLine();


		bw.write("************** Machine " + machine + " ****************");
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
				bw.write("*** " + i + ". url machine: " + machine + " table: " + item.tablename);
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
			        			+ MysqlX.row_delim + urlTxt  
			        			+ MysqlX.row_delim + item.allres.Cext3Orig
			        			+ MysqlX.row_delim + machine
			        			+ MysqlX.row_delim + item.allres.tablename
								+ MysqlX.row_delim + item.allres.getValuesInString(MysqlX.row_delim, ":")
						); //41 + 2
					} else {
				        bw.write(IAUrl 
				        			+ MysqlX.row_delim + urlTxt  
				        			+ MysqlX.row_delim + item.allres.Cext3Orig  
				        			+ MysqlX.row_delim + machine
				        			+ MysqlX.row_delim + item.allres.tablename
				        			+ MysqlX.row_delim + item.allres.calcDanishCode
				        		); // 3 rows + 2
					}
					bw.newLine();
				}
			}
		}
		bw.close();
		return outDir.getAbsolutePath() + "/" + fn;
	}

	private static Set<CriteriaKeyAndTable> getResForSearch(Connection conn, Set<String> tableSet, String url, String date, boolean incldate, int code, boolean withvalues) throws SQLException {
		Set<CriteriaKeyAndTable> resUrlSet = new HashSet<CriteriaKeyAndTable>();
		for (String nextTable: tableSet) {
            //System.out.println("- processing table: " + nextTable);
			if (nextTable.startsWith(MysqlRes.wf_table_prefix)) {
				String selectSQL = "";
				String urlExp = (url.endsWith("%") ? "Url LIKE ?" : "Url = ?");
	 	    	if (incldate) {
		 	       	selectSQL = "SELECT * FROM " + nextTable + " WHERE " + urlExp + " AND extWDate = ? ";
	 	    	} else {
					selectSQL = "SELECT * FROM " + nextTable + " WHERE " + urlExp;
	 	    	} 
	 	    	Set<String> sqlSet = new HashSet<String>();
	 	    	Set<Integer> codeSet = new HashSet<Integer>();
	 	    	if (code==0) codeSet.add(0);
	 	    	else {
	 	    		if (code == MysqlX.cat_ignored_dk)	
		 	    		codeSet = MysqlX.getCodesForFrasorterede();	//9100; //getCodesForFrasorterede
	 	    		else if (code == MysqlX.cat_not_likely_dk)	 // = 9101;
	 	    			codeSet = MysqlX.getCodesForNOTDanishResults();
	 		 	    else if (code == MysqlX.cat_maybes_dk)		// = 9500;
		 	    		codeSet = MysqlX.getCodesForMaybees();
			 		else if (code == MysqlX.cat_likely_dk)	// = 9999
		 	    		codeSet = MysqlX.getCodesForDanishResults();
			 		else { 
		 	    		//case MysqlX.cat_unknown_dk:	not implemented 	//= 9200; Not decided 0 and negative
	 	               System.err.println("illegal code setting: " + code);
	 	               System.exit(1);
	 	    		}
	 	    	}
	 	    	
	 	    	for (int calcCode: codeSet) {
	 	    		if (code == 0) {
	 	    			sqlSet.add(selectSQL);
	 	    		} else {
	 	    			sqlSet.add(selectSQL + " AND calcDanishCode=" + calcCode);
	 	    		}
	 	    	}
	 	    	
	 	    	for (String sql: sqlSet) {
		 	        PreparedStatement r = conn.prepareStatement(sql); 
		 	    	if (incldate) {
			 	        r.setString(1, url);
			 	        r.setTimestamp(2, MysqlRes.findDateFromString(date));
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

	private static Set<CriteriaKeyAndTableForUrl> getResesForSearch(Connection conn, Set<String> tableSet, File urlfile, boolean incldate, int code, boolean withvalues) throws SQLException, IOException {
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

    private static String getFilename(String machine, File file) {
    	//SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    	//Date now = new Date(System.currentTimeMillis());
    	String s = MysqlX.urlfind_fileprefix 
    			+ "M" + machine + "_"
    			+ file.getName();
        return s;
    }
    
    private static class CriteriaKeyAndTableForUrl {
    	Set<CriteriaKeyAndTable> itemSet = new HashSet<CriteriaKeyAndTable>();
    	String url = "";
    	String date = "";
    	String comment = "";
    }
    
}
