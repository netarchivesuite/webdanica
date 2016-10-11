package dk.kb.webdanica.datamodel.criteria;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;


public class SingleCriteriaResult {
	public static final String[] StringCriteria = new String[] {
		"C1a",
		"C2a","C2b",
		"C3a","C3b","C3c","C3d","C3e","C3f","C3g", 
		"C4a","C4b",
		"C5a","C5b",
		"C6a","C6b","C6c","C6d",
		"C7a","C7b","C7c","C7d",
		"C7e","C7f","C7g", "C7h", 
		"C8a","C8b","C8c",
		"C9a","C9b","C9c","C9d","C9e","C9f",
		"C10a","C10b","C10c",
		"C15a","C15b",
		"C16a",
		"C17a",
		"C18a"
	};

	private static Object CRITERIA_CEXT1;

	private static Object CRITERIA_CEXT2;

	private static Object CRITERIA_CEXT3;

	private static Object CRITERIA_URL;

	private static Object CRITERIA_CTEXT;

	private static Object CRITERIA_CLINKS;
	
    public String url;
    public String urlOrig; //only set if != url
    public Long Cext1;
    public Long Cext2;
    public Long Cext3;
    public String Cext3Orig; //date
    public Map<String,String> C = new HashMap<String,String>();
    public float intDanish;    
    public DataSource source;
    public int calcDanishCode;
	public String CText;
	public List<String> CLinks = new ArrayList<String>();
	public String seedurl;
	public String harvestName;

	// Database fields
	public long insertedDate;
	public long updatedDate;

	public String errorMsg;
    
	public List<String> getClinks() {
		return CLinks;
	}
	public String getCText() throws IOException {
		if (CText != null && !CText.isEmpty()) {
			return CriteriaUtils.fromBase64(CText);
		} else {
			return "";
		}
	}
    
	public SingleCriteriaResult(String trimmedLine) {
		this(trimmedLine, "Unknown", "N/A");
	}
	
    public SingleCriteriaResult(String trimmedLine, String harvestName, String seedurl) {
    	this.harvestName = harvestName;
    	this.seedurl = seedurl;
    	
    	/*
    	String[] resultParts = trimmedLine.split(",");   
    	for (String resultPart: resultParts) {
    		String trimmedResultPart = resultPart.trim();
    		//System.out.println(trimmedResultPart);
    		parseString(trimmedResultPart); // Assigns Values to criteria
    	}
    	*/
    	parseJson(trimmedLine, this);
    	
    	/*** url hack in order to have PK size < 1000 bytes ***/
    	if (url.length() > 900) {
    		urlOrig = url;
    		url = url.substring(0, 900); // TODO should not be necessary
    	} else {
    		urlOrig = ""; //only set if != url
    	}
    	
    	/*** date/time hack in order to have efficient PK ***/
    	if (Cext3Orig==null || Cext3Orig.isEmpty()) {
    		System.err.println("no date for url: " + url + " --- got: " + Cext3Orig);
    	}
    	
    	Cext3 = findDateFromString(Cext3Orig);
    }
    
    private Long findDateFromString(String cext3Orig2) {
    	Date readDate = null;
    	try {
    		readDate = DateConverter.getHeritrixDateFormat().parse(cext3Orig2);
    	} catch (java.text.ParseException e) {
    		return new Long(0L);
    	}
	    return readDate.getTime();

    }
	public static void parseJson(String trimmedLine, SingleCriteriaResult result) {
    	JSONObject o = new JSONObject(trimmedLine);
		Iterator i = o.keys();
		List<String> CriteriaList = Arrays.asList(StringCriteria);
		while (i.hasNext()) {
			String key = (String) i.next();
			if (CriteriaList.contains(key)) {
				result.C.put(key, (String) o.get(key));
			} else if (key.equals(CRITERIA_CEXT1)){
				result.Cext1 = Long.valueOf((String) o.get(key));
			} else if (key.equals(CRITERIA_CEXT2)) {
				result.Cext2 = Long.valueOf((String) o.get(key));
			} else if (key.equals(CRITERIA_CEXT3)) {
				result.Cext3Orig = (String) o.get(key);
			} else if (key.equals(CRITERIA_URL)) {
				String value = (String) o.get(key);
				result.url = CriteriaUtils.fromBase64(value);
			} else if (key.equals(CRITERIA_CTEXT)) {
				String value = (String) o.get(key);
				result.CText = CriteriaUtils.fromBase64(value);
			} else if (key.equals(CRITERIA_CLINKS)) {
				String value = CriteriaUtils.fromBase64((String) o.get(key));
				result.CLinks = splitLinks(value);
			} else {
				System.err.println("Ignoring key '" +  key + "' with value: " + (String) o.get(key));
			}
		}
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
        //Cext3 = findDateFromString(Cext3Orig);
        for (String criteria: StringCriteria) {
        	C.put(criteria, "");
        }
        
        intDanish = 0F;
        source = DataSource.NETARKIVET;
        calcDanishCode = 0;
        seedurl = "";
        harvestName = "";
    }

    
     
    private static List<String> splitLinks(String criteriaContent) {
    	List<String> linkSet = new ArrayList<String>();
		String[] resultParts = criteriaContent.split("##");
		for (String link: resultParts) {
			linkSet.add(link);
		}	
		return linkSet;
    }
    /*
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
    } */
    
    /**
     *  A sort of toString method for this class
     *  
     * @param row_delim
     * @param keyval_delim
     * @return
     */
    public String getValuesInString(String row_delim, String keyval_delim) {
    	String s = "";
    	s = s + "url" + keyval_delim + this.url;
    	s = s + row_delim + "date" + keyval_delim + this.Cext3; 
    	s = s + row_delim + "extSize" + keyval_delim + this.Cext1; //3
    	s = s + row_delim + "extDblChar" + keyval_delim + this.Cext2; //4
    	
    	for (String c: StringCriteria) {
    		s = s + row_delim + c + keyval_delim + (this.C.get(c) != null?this.C.get(c).replace(row_delim, ","):"");
    	}

    	s = s + row_delim + "intDanish" + keyval_delim + this.intDanish; //47
    	s = s + row_delim + "Source" + keyval_delim + this.source; //48
    	s = s + row_delim + "calcDanishCode" + keyval_delim + this.calcDanishCode; //49
    	return s;
    }
    
    public List<String> getValuesAsStringList(String row_delim, String keyval_delim) {
    	List<String> list = new ArrayList<String>();
    	list.add("url" + keyval_delim + this.url);
    	list.add("date" + keyval_delim + this.Cext3); 
    	list.add("Cext1/extsize" + keyval_delim + this.Cext1); //3
    	list.add("Cext2/extDblChar" + keyval_delim + this.Cext2); //4
    	
    	for (String c: StringCriteria) {
    		list.add(c + keyval_delim + (this.C.get(c) != null?this.C.get(c).replace(row_delim, ","):""));
    	}

    	list.add("intDanish" + keyval_delim + this.intDanish); //47
    	list.add("Source" + keyval_delim + this.source); //48
    	list.add("calcDanishCode" + keyval_delim + this.calcDanishCode); //49
    	return list;
    }

    public static SingleCriteriaResult createErrorResult(String error) {
		SingleCriteriaResult s = new SingleCriteriaResult();
		s.errorMsg = error;
		s.url = "Dummy";
		s.harvestName = "Dummy";
		s.seedurl = "Dummy";
	    return s;
    } 
    
	public boolean isError() {
		return errorMsg != null;
	}
}
