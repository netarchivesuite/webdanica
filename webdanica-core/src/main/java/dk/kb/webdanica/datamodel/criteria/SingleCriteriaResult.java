package dk.kb.webdanica.datamodel.criteria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Set;

public class SingleCriteriaResult {
    public String url;
    public String urlOrig; //only set if != url
    public Long Cext1;
    public Long Cext2;
    public java.sql.Timestamp Cext3;
    public String Cext3Orig; //date
    public String C1a;
    public String C2a;
    public String C2b; //*
    public String C4a;
    public String C3a;
    public String C6a;
    public String C3b;
    public String C3c;
    public String C3d;
    public String C3e;
    public String C3f;
    public String C3g; //*
    public String C6b;
    public String C6c;
    public String C6d; //*
    public String C5a;
    public String C5b;
    public String C7a;
    public String C7b;
    public String C7c;
    public String C7d;
    public String C7e;
    public String C7f;
    public String C7g; //*
    public String C7h; //*
    public String C8a;
    public String C8b;
    public String C8c; //*
    public String C9a;
    public String C9b;
    public String C9c;
    public String C9d;
    public String C9e; //*
    public String C9f; //*
    public String C10a;
    public String C10b;
    public String C10c; //*
    public String C15a;
    public String C15b;
    public String C16a;
    public String C17a;
    public String C18a;
    public float intDanish;
    public DataSource source;
    public int calcDanishCode;
    public String tablename; //only for UrlExtract
	private String C4b;
    
    
    public SingleCriteriaResult(String trimmedLine, boolean ingestMode) {
    	String[] resultParts = trimmedLine.split(",");   
    	for (String resultPart: resultParts) {
    		String trimmedResultPart = resultPart.trim();
    		parseString(trimmedResultPart, ingestMode); // Assigns Values to criteria
    	}
    	/*** url hack in order to have PK size < 1000 bytes ***/
    	if (url.length() > 900) {
    		urlOrig =url;
    		url = url.substring(0, 900); // TODO should not be necessary
    	} else {
    		urlOrig =""; //only set if != url
    	}
    	
    	/*** date/time hack in order to have efficient PK ***/
    	if (Cext3Orig==null || Cext3Orig.isEmpty()) {
    		System.err.println("no date for url: " + url + " --- got: " + Cext3Orig);
    	}
    	Cext3 = findDateFromString(Cext3Orig);
    }
    /**
     * Construct a SingleCriteriaResult from a database ResultSet
     * @param res
     * @param extendedNewHadoopTable FIXME not necessary to be used?
     * @throws SQLException
     */
    public SingleCriteriaResult(ResultSet res, boolean extendedNewHadoopTable)  throws SQLException {
    	parseResultSet(res, extendedNewHadoopTable);
    }

    /**
     * Default constructor.
     */
    public SingleCriteriaResult() {
        url="";
        urlOrig=""; //only set if != url
        Cext1 =0L;
        Cext2 =0L;
        Cext3Orig="20140901000000"; //date //FIXME shouldn't this changed
        Cext3 = findDateFromString(Cext3Orig);
        C4a="";
        C4b="";
        C3a="";
        C6a="";
        C3b="";
        C3c="";
        C3d="";
        C3e="";
        C3f="";
        C3g="";
        C6b="";
        C6c="";
        C6d="";
        C5a="";
        C5b="";
        C1a="";
        C2a="";
        C2b="";
        C7a="";
        C7b="";
        C7c="";
        C7d="";
        C7e="";
        C7f="";
        C7g="";
        C7h="";
        C8a="";
        C8b="";
        C8c="";
        C9a="";
        C9b="";
        C9c="";
        C9d="";
        C9e="";
        C9f="";
        C10a="";
        C10b="";
        C10c="";
        C15a="";
        C15b="";
        C16a="";
        C17a="";
        C18a="";
        intDanish = 0F;
        source = DataSource.NETARKIVET;
        calcDanishCode = 0;
    }

    /** Parse a criteria for a single url. 
     * 
     * @param trimmedResultPart
     * @param ingestMode
     */
    private void parseString(String trimmedResultPart, boolean ingestMode) {
    	//There may be ',' in URL, therefore unexpected text will be part of Url
    	//Although a criteria cannot be part of an URL
        if (trimmedResultPart.startsWith("http")) {
        	this.url = trimmedResultPart;
        }
        else if (trimmedResultPart.startsWith("C")) 
        {
            String[] valueparts = trimmedResultPart.split(":");
            if (valueparts.length > 1) {
                if (trimmedResultPart.startsWith("Cext1")) {
                    //System.out.println("Cext1: " + trimmedResultPart);
                    this.Cext1 = Long.parseLong(valueparts[1].trim());
                    //System.out.println("Cext1: " + this.Cext1);
                } else if (trimmedResultPart.startsWith("Cext2")) {
                    this.Cext2 = Long.parseLong(valueparts[1].trim());
                    //System.out.println("Cext2: " + this.Cext2);
                } else if (trimmedResultPart.startsWith("Cext3")) {
                    this.Cext3Orig = valueparts[1].trim();
                    //System.out.println("Cext3: " + this.Cext3);
                } else if (trimmedResultPart.startsWith("C4a")) {
                    this.C4a = valueparts[1].trim();
                    //System.out.println("C4a: " + this.C4a);
                } else if (trimmedResultPart.startsWith("C4b")) {
                	this.C4b = valueparts[1].trim();
                    //System.out.println("C4a: " + this.C4a);    
                } else if (trimmedResultPart.startsWith("C1a")) {
                    this.C1a = valueparts[1].trim();
                    //System.out.println("C1a: " + this.C1a);
                } else if (trimmedResultPart.startsWith("C2a")) {
                    this.C2a = valueparts[1].trim();
                    //System.out.println("C2a: " + this.C2a);
                } else if (trimmedResultPart.startsWith("C2b")) {
                    this.C2b = valueparts[1].trim(); 				//TODO: check with ingestMode?
                    //System.out.println("C2b: " + this.C2b);
                } else if (trimmedResultPart.startsWith("C3a")) {
                    this.C3a = valueparts[1].trim();
                    //System.out.println("C3a: " + this.C3a); 
                } else if (trimmedResultPart.startsWith("C3b")) {
                    this.C3b = valueparts[1].trim();
                    //System.out.println("C3b: " + this.C3b);
                } else if (trimmedResultPart.startsWith("C3c")) {
                    this.C3c = valueparts[1].trim();
                    //System.out.println("C3c: " + this.C3c);
                } else if (trimmedResultPart.startsWith("C3d")) {
                    this.C3d = valueparts[1].trim();
                    //System.out.println("C3d: " + this.C3d);
                } else if (trimmedResultPart.startsWith("C3g")) {
                    this.C3g = valueparts[1].trim(); 				//TODO: check with ingestMode?
                    //System.out.println("C3g: " + this.C3g);
                } else if (trimmedResultPart.startsWith("C5a")) {
                    this.C5a = valueparts[1].trim();
                    //System.out.println("C5a: " + this.C5a);
                } else if (trimmedResultPart.startsWith("C5b")) {
                    this.C5b = valueparts[1].trim();
                    //System.out.println("C5b: " + this.C5b);
                } else if (trimmedResultPart.startsWith("C6a")) {
                    this.C6a = valueparts[1].trim();
                    //System.out.println("C6a: " + this.C6a);
                } else if (trimmedResultPart.startsWith("C6b")) {
                    this.C6b = valueparts[1].trim();
                    //System.out.println("C6b: " + this.C6b);
                } else if (trimmedResultPart.startsWith("C6c")) {
                    this.C6c = valueparts[1].trim();
                    //System.out.println("C6c: " + this.C6c);
                } else if (trimmedResultPart.startsWith("C6d")) {
                    this.C6d = valueparts[1].trim(); 				//TODO: check with ingestMode?
                    //System.out.println("C6d: " + this.C6d);
                } else if (trimmedResultPart.startsWith("C7a")) {
                    this.C7a = valueparts[1].trim();
                    //System.out.println("C7a: " + this.C7a);
                } else if (trimmedResultPart.startsWith("C7b")) {
                    this.C7b = valueparts[1].trim();
                    //System.out.println("C7b: " + this.C7b);    
                } else if (trimmedResultPart.startsWith("C7c")) {
                    this.C7c = valueparts[1].trim();
                    //System.out.println("C7c: " + this.C7c);    
                } else if (trimmedResultPart.startsWith("C7d")) {
                    this.C7d = valueparts[1].trim();
                    //System.out.println("C7d: " + this.C7d);    
                } else if (trimmedResultPart.startsWith("C7e")) {
                    this.C7e = valueparts[1].trim();
                    //System.out.println("C7e: " + this.C7e);    
                } else if (trimmedResultPart.startsWith("C7f")) {
                    this.C7f = valueparts[1].trim();
                    //System.out.println("C7f: " + this.C7f);    
                } else if (trimmedResultPart.startsWith("C7g")) {
                    this.C7g = valueparts[1].trim(); 				//TODO: check with ingestMode?
                    //System.out.println("C7g: " + this.C7g);
                } else if (trimmedResultPart.startsWith("C7h")) {
                    this.C7h = valueparts[1].trim(); 				//TODO: check with ingestMode?
                    //System.out.println("C7h: " + this.C7h);
                } else if (trimmedResultPart.startsWith("C8a")) {
                    this.C8a = valueparts[1].trim();
                    //System.out.println("C8a: " + this.C8a);    
                } else if (trimmedResultPart.startsWith("C8b")) {
                    this.C8b = valueparts[1].trim();
                    //System.out.println("C8b: " + this.C8b);    
                } else if (trimmedResultPart.startsWith("C8c")) {
                    this.C8c = valueparts[1].trim(); 				//TODO: check with ingestMode?
                    //System.out.println("C8c: " + this.C8c);
                } else if (trimmedResultPart.startsWith("C9a")) {
                    this.C9a = valueparts[1].trim();
                    //System.out.println("C9a: " + this.C9a);    
                } else if (trimmedResultPart.startsWith("C9b")) {
                    this.C9b = valueparts[1].trim();
                    //System.out.println("C9b: " + this.C9b);    
                } else if (trimmedResultPart.startsWith("C9c")) {
                    this.C9c = valueparts[1].trim();
                    //System.out.println("C9c: " + this.C9c);    
                } else if (trimmedResultPart.startsWith("C9d")) {
                    this.C9d = valueparts[1].trim();
                    //System.out.println("C9d: " + this.C9d);    
                } else if (trimmedResultPart.startsWith("C9e")) {
                    this.C9e = valueparts[1].trim(); 				//TODO: check with ingestMode?
                    //System.out.println("C9e: " + this.C9e);
                } else if (trimmedResultPart.startsWith("C9f")) {
                    this.C9f = valueparts[1].trim(); 				//TODO: check with ingestMode?
                    //System.out.println("C9f: " + this.C9f);
                } else if (trimmedResultPart.startsWith("C10a")) {
                    this.C10a = valueparts[1].trim();
                    //System.out.println("C10a: " + this.C10a);    
                } else if (trimmedResultPart.startsWith("C10b")) {
                    this.C10b = valueparts[1].trim();
                    //System.out.println("C10b: " + this.C10b);    
                } else if (trimmedResultPart.startsWith("C10c")) {
                    this.C10c = valueparts[1].trim(); 				//TODO: check with ingestMode?
                    //System.out.println("C10c: " + this.C10c);
                } else if (trimmedResultPart.startsWith("C15a")) {
                    this.C15a = valueparts[1].trim();
                    //System.out.println("C15a: " + this.C15a);    
                } else if (trimmedResultPart.startsWith("C15b")) {
                    this.C15b = valueparts[1].trim();
                    //System.out.println("C15b: " + this.C15b);    
                } else if (trimmedResultPart.startsWith("C16a")) {
                    this.C16a = valueparts[1].trim();
                    //System.out.println("C16a: " + this.C16a);    
                } else if (trimmedResultPart.startsWith("C17a")) {
                    this.C17a = valueparts[1].trim();
                    //System.out.println("C17a: " + this.C17a);   
                } else if (trimmedResultPart.startsWith("C18a")) {
                    this.C18a = valueparts[1].trim();
                    //System.out.println("C18a: " + this.C18a);    
                } else {
                	//not abbr. for criteria thus it is not a criteria, and therefore must be part of Url
                	this.url = this.url + "," + trimmedResultPart.trim();
                }
            } else {
            	//no ":" thus it is not a criteria, and therefore must be part of Url
            	this.url = this.url + "," + trimmedResultPart.trim();
            }
        } else {
            //System.out.println("Starts not with c: " + trimmedResultPart);
        	this.url = this.url + "," + trimmedResultPart.trim();
        }   
    }
    
    public static java.sql.Timestamp findDateFromString(String dateString) {
    	java.sql.Timestamp t;
    	t = java.sql.Timestamp.valueOf(//yyyy-[m]m-[d]d hh:mm:ss 
    			dateString.substring(0, 4) + "-"
        		+ dateString.substring(4, 6) + "-"
        		+ dateString.substring(6, 8) + " "
        		+ dateString.substring(8, 10) + ":"
        		+ dateString.substring(10, 12) + ":"
        		+ dateString.substring(12, 14)
        ); 
    	return t;
    }


    public static SingleCriteriaResult readUrl(Connection conn, String tablename, SingleCriteriaResult res, boolean extendedNewHadoopTable) throws SQLException {
        /*    
        System.out.println(res.url);
        */
    	String selectSQL = "SELECT * FROM " + tablename + " WHERE Url = ? AND extWDate = ? ";
        PreparedStatement s = conn.prepareStatement(selectSQL); 
        s.setString(1, res.url);
        s.setTimestamp(2, res.Cext3);
        ResultSet rs = s.executeQuery();

        SingleCriteriaResult r = new SingleCriteriaResult();
        if (rs.next()) {
        	r = new SingleCriteriaResult(rs, extendedNewHadoopTable);
        }
        s.close();
        return r;
    }

    public static boolean insertLine(Connection conn, SingleCriteriaResult res, String tablename) throws SQLException {
	    /*    
	    System.out.println(res.url);
	    System.out.println(res.C1a);
	    System.out.println(res.C2a);
	    System.out.println(res.C3a);
	    System.out.println(res.C3b);
	    */
	    PreparedStatement s = conn.prepareStatement("INSERT INTO " + tablename + " "
	            + "(Url, UrlOrig, C1a, C2a, C2b, C3a, C3b,C3c,C3d,C3e,C3f,C3g,C4a,C5a,C5b," // 11+2+2
	            + " C6a, C6b, C6c, C6d,C7a, C7b, C7c, C7d, C7e, C7f, C7g, C7h," //9+3
	            + " C8a, C8b, C8c, C9a, C9b, C9c, C9d, C9e, C9f, C10a, C10b, C10c," // 8+4
	            + " C15a, C15b, C16a, C17a, C18a, " // 5
	            + " extSize, extDblChar,extWDate, extWDateOrig, " //4
	            + " intDanish, IsIASource, calcDanishCode" // 3 - 40+9+2 = 51
	            + ") VALUES ("
	            + "?,?,?,?,?,?,?,?,?,?, " // 10
	            + "?,?,?,?,?,?,?,?,?,?, " // 10
	            + "?,?,?,?,?,?,?,?,?,?, " // 10
	            + "?,?,?,?,?,?,?,?,?,?, " // 10
	            + "?,?,?,?,?,?,?,?,?,?,? )" // 11
	            ); // 51

	    int index = 1;

	    s.setString(index, res.url);

	    index++;
	    s.setString(index, res.urlOrig);
	    
	    index++;
	    setString(s, index, res.C1a); 
	    index++;
	    setString(s, index, res.C2a);
	    index++;
	    setString(s, index, res.C2b);
	    index++;
	    setString(s, index, res.C3a);
	    index++;
	    setString(s, index, res.C3b);
	    
	    //        + " C6a,C6b,C6c
	    //,C7a,C7b,C7c,C7d,C7e, C7f,C8a,C8b,C9a,C9b,C9c,C9d,C10a,"
	    //        + " C10b, C15a, C15b, C16a, C17a, C18a
	    
	    index++;
	    setString(s, index, res.C3c);
	    index++;
	    setString(s, index, res.C3d);
	    index++;
	    setString(s, index, res.C3e);
	    index++;
	    setString(s, index, res.C3f);
	    index++;
	    setString(s, index, res.C3g);
	    index++;
	    setString(s, index, res.C4a);
	    index++;
	    setString(s, index, res.C5a);
	    index++;
	    setString(s, index, res.C5b);
	    
	    //System.out.println("Index for c5b: " + index);
	    //+ " C6a,C6b,C6c
	    index++;
	    setString(s, index, res.C6a);
	    index++;
	    setString(s, index, res.C6b);
	    index++;
	    setString(s, index, res.C6c);
	    index++;
	    setString(s, index, res.C6d);
	    //C7a,C7b,C7c,C7d,C7e, C7f
	    
	    index++;
	    setString(s, index, res.C7a);
	    index++;
	    setString(s, index, res.C7b);
	    index++;
	    setString(s, index, res.C7c);
	    index++;
	    setString(s, index, res.C7d);
	    index++;
	    setString(s, index, res.C7e);
	    
	    index++;
	    setString(s, index, res.C7f);
	    
	    index++;
	    setString(s, index, res.C7g);
	    
	    index++;
	    setString(s, index, res.C7h);
	    index++;
	    setString(s, index, res.C8a);
	    
	    index++;
	    setString(s, index, res.C8b);
	    index++;
	    setString(s, index, res.C8c);
	    index++;
	    setString(s, index, res.C9a);
	    index++;
	    setString(s, index, res.C9b);
	    index++;
	    setString(s, index, res.C9c);
	    index++;
	    setString(s, index, res.C9d);
	    index++;
	    setString(s, index, res.C9e);
	    index++;
	    setString(s, index, res.C9f);
	    index++;
	    setString(s, index, res.C10a);
	    
	    //System.out.println("Index for c10a: " + index);
	    index++;
	    setString(s, index, res.C10b);
	    index++;
	    setString(s, index, res.C10c);
	    
	    //C15a, C15b, C16a, C17a,
	    index++;
	    setString(s, index, res.C15a);
	    index++;
	    setString(s, index, res.C15b);
	    index++;
	    Long C16aValue = parseLongFromString(res.C16a);
	    setLong(s, index, C16aValue);
	    index++;
	    Long C17aValue = parseLongFromString(res.C17a);
	    setLong(s, index, C17aValue);
	    index++;
	    setString(s, index, res.C18a);
	    index++;
	    s.setLong(index, res.Cext1); // = extSize in table
	    index++;
	    // Cext2 can be null if Cext1 == 0
	    setLong(s, index, res.Cext2); // = extDblChar in table (truncated to int during processing)
	    
	    index++;
	    s.setTimestamp(index, res.Cext3); // = date

	    index++;
	    s.setString(index, res.Cext3Orig); // = date
	    
	    index++;
	    s.setFloat(index, res.intDanish); // Could be added to hadoop job later, so included here 
	    
	    index++;
	    s.setInt(index, res.source.ordinal()); 

	    index++;
	    s.setInt(index, res.calcDanishCode); // = extWDate in table (truncated to int during processing)
	    
	    s.executeUpdate();
	    s.close();
	    return true;
    }
    
    private static Long parseLongFromString(String value) {
	    Long result = null;
	    try {
	    	result = Long.parseLong(value);
	    } catch (NumberFormatException e) {
	    	e.printStackTrace();
	    }
	    return result;
    }
    
	private static void setString(PreparedStatement s, int index, String value) throws SQLException {
    	if (value != null) {
    		s.setString(index, value);
    	} else {
    		s.setNull(index, Types.VARCHAR);
    	}
    }
    
    private static void setLong(PreparedStatement s, int index, Long value) throws SQLException {
    	if (value != null) {
    		s.setLong(index, value);
    	} else {
    		s.setNull(index, Types.BIGINT);
    	}
    }
    
    
    
    private void parseResultSet(ResultSet rs, boolean extendedNewHadoop) throws SQLException {
        this.url = rs.getString("Url");
        this.urlOrig = rs.getString("UrlOrig");
        this.Cext3= rs.getTimestamp("extWDate");
        this.Cext3Orig = rs.getString("extWDateOrig");
        this.Cext1 = rs.getLong("extSize");
        this.Cext2 =rs.getLong("extDblChar");
        this.C1a = rs.getString("C1a");
        this.C2a = rs.getString("C2a");
        if (extendedNewHadoop) this.C2b = rs.getString("C2b");
        this.C3a = rs.getString("C3a");
        this.C3b = rs.getString("C3b");
        this.C3c = rs.getString("C3c");  
        this.C3d = rs.getString("C3d");
        this.C3e = rs.getString("C3e");
        this.C3f = rs.getString("C3f");
        if (extendedNewHadoop) this.C3g = rs.getString("C3g");
        this.C4a = rs.getString("C4a");
        this.C5a = rs.getString("C5a");
        this.C5b = rs.getString("C5b");
        this.C6a = rs.getString("C6a");
        this.C6b = rs.getString("C6b");
        this.C6c = rs.getString("C6c");
        if (extendedNewHadoop) this.C6d = rs.getString("C6d");
        this.C7a = rs.getString("C7a");
        this.C7b = rs.getString("C7b");
        this.C7c = rs.getString("C7c");
        this.C7d = rs.getString("C7d");
        this.C7e = rs.getString("C7e");
        this.C7f = rs.getString("C7f");
        if (extendedNewHadoop) this.C7g = rs.getString("C7g");
        if (extendedNewHadoop) this.C7h = rs.getString("C7h");
        this.C8a = rs.getString("C8a");
        this.C8b = rs.getString("C8b");
        if (extendedNewHadoop) this.C8c = rs.getString("C8c");
        this.C9a = rs.getString("C9a");
        this.C9b = rs.getString("C9b");
        this.C9c = rs.getString("C9c");
        this.C9d = rs.getString("C9d");
        if (extendedNewHadoop) this.C9e = rs.getString("C9e");
        if (extendedNewHadoop) this.C9f = rs.getString("C9f");
        this.C10a = rs.getString("C10a");
        this.C10b = rs.getString("C10b");
        if (extendedNewHadoop) this.C10c = rs.getString("C10c");
        this.C15a = rs.getString("C15a");
        this.C15b = rs.getString("C15b");
        this.C16a = rs.getString("C16a");
        this.C17a = rs.getString("C17a");
        this.C18a = rs.getString("C18a");
        this.intDanish = rs.getFloat("intDanish");
        this.source = DataSource.fromOrdinal(rs.getInt("source"));
        this.calcDanishCode = rs.getInt("calcDanishCode");
    }   
    
    /** Used by ???
     *  A sort of toString method ??
     *  
     * @param row_delim
     * @param keyval_delim
     * @return
     */
    public String getValuesInString(String row_delim, String keyval_delim) {
    	//EXCEPT Url and date!!
    	String s = "";
    	s = s + "extSize" + keyval_delim + keyval_delim + this.Cext1; //3
    	s = s + row_delim + "extDblChar" + keyval_delim + this.Cext2; //4
    	s = s + row_delim + "C1a" + keyval_delim + (this.C1a!=null?this.C1a.replace(row_delim, ","):""); //5
    	s = s + row_delim + "C2a" + keyval_delim + (this.C2a!=null?this.C2a.replace(row_delim, ","):""); //6
    	s = s + row_delim + "C2b" + keyval_delim + (this.C2b!=null?this.C2b.replace(row_delim, ","):""); //7
    	s = s + row_delim + "C3a" + keyval_delim + (this.C3a!=null?this.C3a.replace(row_delim, ","):""); //8
    	s = s + row_delim + "C3b" + keyval_delim + (this.C3b!=null?this.C3b.replace(row_delim, ","):""); //9
    	s = s + row_delim + "C3c" + keyval_delim + (this.C3c!=null?this.C3c.replace(row_delim, ","):""); //10
    	s = s + row_delim + "C3d" + keyval_delim + (this.C3d!=null?this.C3d.replace(row_delim, ","):""); //11
    	s = s + row_delim + "C3e" + keyval_delim + (this.C3e!=null?this.C3e.replace(row_delim, ","):""); //12
    	s = s + row_delim + "C3f" + keyval_delim + (this.C3f!=null?this.C3f.replace(row_delim, ","):""); //13
    	s = s + row_delim + "C3g" + keyval_delim + (this.C3g!=null?this.C3g.replace(row_delim, ","):""); //14
    	s = s + row_delim + "C4a" + keyval_delim + (this.C4a!=null?this.C4a.replace(row_delim, ","):""); //15
    	s = s + row_delim + "C4b" + keyval_delim + (this.C4b!=null?this.C4b.replace(row_delim, ","):""); //15a
    	s = s + row_delim + "C5a" + keyval_delim + (this.C5a!=null?this.C5a.replace(row_delim, ","):""); //16
    	s = s + row_delim + "C5b" + keyval_delim + (this.C5b!=null?this.C5b.replace(row_delim, ","):""); //17
    	s = s + row_delim + "C6a" + keyval_delim + (this.C6a!=null?this.C6a.replace(row_delim, ","):""); //18
    	s = s + row_delim + "C6b" + keyval_delim + (this.C6b!=null?this.C6b.replace(row_delim, ","):""); //19
    	s = s + row_delim + "C6c" + keyval_delim + (this.C6c!=null?this.C6c.replace(row_delim, ","):""); //20
    	s = s + row_delim + "C6d" + keyval_delim + (this.C6d!=null?this.C6d.replace(row_delim, ","):""); //21
    	s = s + row_delim + "C7a" + keyval_delim + (this.C7a!=null?this.C7a.replace(row_delim, ","):""); //22
    	s = s + row_delim + "C7b" + keyval_delim + (this.C7b!=null?this.C7b.replace(row_delim, ","):""); //23
    	s = s + row_delim + "C7c" + keyval_delim + (this.C7c!=null?this.C7c.replace(row_delim, ","):""); //24
    	s = s + row_delim + "C7d" + keyval_delim + (this.C7d!=null?this.C7d.replace(row_delim, ","):""); //25
    	s = s + row_delim + "C7e" + keyval_delim + (this.C7e!=null?this.C7e.replace(row_delim, ","):""); //26
    	s = s + row_delim + "C7f" + keyval_delim + (this.C7f!=null?this.C7f.replace(row_delim, ","):""); //27
    	s = s + row_delim + "C7g" + keyval_delim + (this.C7g!=null?this.C7g.replace(row_delim, ","):""); //28
    	s = s + row_delim + "C7h" + keyval_delim + (this.C7h!=null?this.C7h.replace(row_delim, ","):""); //29
    	s = s + row_delim + "C8a" + keyval_delim + (this.C8a!=null?this.C8a.replace(row_delim, ","):""); //30
    	s = s + row_delim + "C8b" + keyval_delim + (this.C8b!=null?this.C8b.replace(row_delim, ","):""); //31
    	s = s + row_delim + "C8c" + keyval_delim + (this.C8c!=null?this.C8c.replace(row_delim, ","):""); //32
    	s = s + row_delim + "C9a" + keyval_delim + (this.C9a!=null?this.C9a.replace(row_delim, ","):""); //33
    	s = s + row_delim + "C9b" + keyval_delim + (this.C9b!=null?this.C9b.replace(row_delim, ","):""); //34
    	s = s + row_delim + "C9c" + keyval_delim + (this.C9c!=null?this.C9c.replace(row_delim, ","):""); //35
    	s = s + row_delim + "C9d" + keyval_delim + (this.C9d!=null?this.C9d.replace(row_delim, ","):""); //36
    	s = s + row_delim + "C9e" + keyval_delim + (this.C9e!=null?this.C9e.replace(row_delim, ","):""); //37
    	s = s + row_delim + "C9f" + keyval_delim + (this.C9f!=null?this.C9f.replace(row_delim, ","):""); //38
    	s = s + row_delim + "C10a" + keyval_delim + (this.C10a!=null?this.C10a.replace(row_delim, ","):""); //39
    	s = s + row_delim + "C10b" + keyval_delim + (this.C10b!=null?this.C10b.replace(row_delim, ","):""); //40
    	s = s + row_delim + "C10c" + keyval_delim + (this.C10c!=null?this.C10c.replace(row_delim, ","):""); //41
    	s = s + row_delim + "C15a" + keyval_delim + (this.C15a!=null?this.C15a.replace(row_delim, ","):""); //42
    	s = s + row_delim + "C15b" + keyval_delim + (this.C15b!=null?this.C15b.replace(row_delim, ","):""); //43
    	s = s + row_delim + "C16a" + keyval_delim + (this.C16a!=null?this.C16a.replace(row_delim, ","):""); //44
    	s = s + row_delim + "C17a" + keyval_delim + (this.C17a!=null?this.C17a.replace(row_delim, ","):""); //45
    	s = s + row_delim + "C18a" + keyval_delim + (this.C18a!=null?this.C18a.replace(row_delim, ","):""); //46
    	s = s + row_delim + "intDanish" + keyval_delim + this.intDanish; //47
    	s = s + row_delim + "Source" + keyval_delim + this.source; //48
    	s = s + row_delim + "calcDanishCode" + keyval_delim + this.calcDanishCode; //49
    	return s;
    } 
    
    public static boolean createSingleCriteriaResultTable(Connection conn, String tablename) throws SQLException {
    	boolean ok = true; 
    	//NOTE test machine does not have same limit
    	String sql = "CREATE TABLE " + tablename + " ( ";
    	
    	sql = sql + "  Url varchar(900), ";
    	
    	sql = sql + "  extWDate DATETIME, "; 
    	sql = sql + "  UrlOrig TEXT, "; 
    	sql = sql + "  extWDateOrig varchar(14), "; 
    	sql = sql + "  extSize BIGINT, "; 
    	sql = sql + "  extDblChar INT, "; 
    	sql = sql + "  C1a TEXT, "; 
    	sql = sql + "  C2a varchar(500), "; 
    	sql = sql + "  C2b varchar(500), "; //NewHadoop
    	sql = sql + "  C3a varchar(500), "; 
    	sql = sql + "  C3b TEXT, "; 
    	sql = sql + "  C3c varchar(500), "; 
    	sql = sql + "  C3d TEXT, ";        
    	sql = sql + "  C3e TEXT, ";             //Added 9/9 as 3b  form ae, oe/o, aa but with reduced list
    	sql = sql + "  C3f varchar(500), ";     //Added 9/9 as 3d  form ae, oe/o, aa but with reduced list in URL
    	sql = sql + "  C3g TEXT, "; //NewHadoop
    	sql = sql + "  C4a varchar(100), "; 
    	sql = sql + "  C5a varchar(500), "; 
    	sql = sql + "  C5b varchar(500), ";
    	sql = sql + "  C6a TEXT, "; 
    	sql = sql + "  C6b varchar(100), "; 
    	sql = sql + "  C6c varchar(100), ";
    	sql = sql + "  C6d TEXT, "; //NewHadoop
    	sql = sql + "  C7a TEXT, ";
    	sql = sql + "  C7b varchar(500), "; 
    	sql = sql + "  C7c TEXT, "; 
    	sql = sql + "  C7d varchar(500), ";
    	sql = sql + "  C7e TEXT, "; 
    	sql = sql + "  C7f varchar(500), ";
    	sql = sql + "  C7g TEXT, "; //NewHadoop
    	sql = sql + "  C7h TEXT, "; //NewHadoop
    	sql = sql + "  C8a TEXT, "; 
    	sql = sql + "  C8b varchar(500), ";
    	sql = sql + "  C8c TEXT, "; //NewHadoop
    	sql = sql + "  C9a varchar(500), "; 
    	sql = sql + "  C9b TEXT, "; 
    	sql = sql + "  C9c varchar(500), ";
    	sql = sql + "  C9d varchar(500), ";
    	sql = sql + "  C9e TEXT, "; //NewHadoop
    	sql = sql + "  C9f varchar(500), ";//NewHadoop
    	sql = sql + "  C10a TEXT, "; 
    	sql = sql + "  C10b TEXT, "; 
    	sql = sql + "  C10c TEXT, "; //NewHadoop 
    	sql = sql + "  C15a varchar(10), "; 
    	sql = sql + "  C15b varchar(20), "; 
    	sql = sql + "  C16a BIGINT, "; 
    	sql = sql + "  C17a BIGINT, "; 
    	sql = sql + "  C18a varchar(1), "; 
    	sql = sql + "  intDanish FLOAT, "; 
    	sql = sql + "  Source TINYINT(1), "; // FIXME not relevant for webdanica project
    	sql = sql + "  calcDanishCode MEDIUMINT(3) "; //updated from calcDanishCode SMALLINT(2) 9/9
    	sql = sql + "  )";
    	
    	PreparedStatement s = conn.prepareStatement(sql);
	    s.executeUpdate();
	    s.close();
	    return ok;
    }
    
    public static boolean updateHadoopLineSingleTable(Connection conn, String tablename, SingleCriteriaResult res) throws SQLException {
    	String sql = "UPDATE " + tablename + " " ;
    	sql = sql  + "SET calcDanishCode = ?, ";
        sql = sql  +  "   intDanish = ?, ";
        sql = sql  +  "   C2b = ?, ";
        sql = sql  +  "   C3g = ?, ";
        sql = sql  +  "   C6d = ?, ";
        sql = sql  +  "   C7g = ?, ";
        sql = sql  +  "   C7h = ?, ";
        sql = sql  +  "   C8c = ?, ";
        sql = sql  +  "   C9e = ?, ";
        sql = sql  +  "   C9f = ?, ";
        sql = sql  +  "   C10c = ? ";
    	sql = sql  + "WHERE Url = ? AND extWDate = ?"; 
    	PreparedStatement s = conn.prepareStatement( sql ); 
	    
	    int index = 1;
	    s.setInt(index, res.calcDanishCode);
	    index++;
	    s.setDouble(index, res.intDanish);
	    index++;
	    s.setString(index, res.C2b);
	    index++;
	    s.setString(index, res.C3g);
	    index++;
	    s.setString(index, res.C6d);
	    index++;
	    s.setString(index, res.C7g);
	    index++;
	    s.setString(index, res.C7h);
	    index++;
	    s.setString(index, res.C8c);
	    index++;
	    s.setString(index, res.C9e);
	    index++;
	    s.setString(index, res.C9f);
	    index++;
	    s.setString(index, res.C10c);
	    index++;
	    s.setString(index, res.url);
	    index++;
	    s.setTimestamp(index, res.Cext3);
	    
	    boolean ok = true;
		try {
			s.executeUpdate();                
		} catch(SQLException ex) {
			ok = false;
		}
	    s.close();
	    return ok;
    }
    
    public static boolean updateHadoopLines(Connection conn, Set<String> tablenameSet, SingleCriteriaResult res) throws SQLException {
    	boolean found = false;
    	for (String t: tablenameSet) {
        	found = updateHadoopLineSingleTable(conn, t, res);
        	if (found) break;
        }
	    return found;
    }
    
    
}
