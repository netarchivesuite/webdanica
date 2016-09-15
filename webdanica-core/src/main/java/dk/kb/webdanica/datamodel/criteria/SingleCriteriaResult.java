package dk.kb.webdanica.datamodel.criteria;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SingleCriteriaResult {
	public static final String[] StringCriteria = new String[] {
		"C1a",
		"C2a", "C2b",
		"C3a", "C3b", "C3c", "C3d", "C3e", "C3f", "C3g",
		"C4a", "C4b",
		"C5a", "C5b",
		"C6a", "C6b", "C6c", "C6d",
		"C7a", "C7b", "C7c", "C7d", "C7e", "C7f", "C7g", "C7h",
		"C8a", "C8b", "C8c",
		"C9a", "C9b", "C9c", "C9d", "C9e", "C9f",
		"C10a", "C10b", "C10c",
		"C15a", "C15b",
		"C16a",
		"C17a",
		"C18a"
	};

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
    	String[] resultParts = trimmedLine.split(",");   
    	for (String resultPart: resultParts) {
    		String trimmedResultPart = resultPart.trim();
    		//System.out.println(trimmedResultPart);
    		parseString(trimmedResultPart); // Assigns Values to criteria
    	}
    	/*** url hack in order to have PK size < 1000 bytes ***/
    	if (url.length() > 900) {
    		urlOrig = url;
    		url = url.substring(0, 900); // TODO should not be necessary
    	} else {
    		urlOrig =""; //only set if != url
    	}
    	
    	/*** date/time hack in order to have efficient PK ***/
    	if (Cext3Orig==null || Cext3Orig.isEmpty()) {
    		System.err.println("no date for url: " + url + " --- got: " + Cext3Orig);
    	}
    	
    	//Cext3 = findDateFromString(Cext3Orig);
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
        C = new HashMap<String,String>();
        for (String criteria: StringCriteria) {
        	C.put(criteria, "");
        }
        
        intDanish = 0F;
        source = DataSource.NETARKIVET;
        calcDanishCode = 0;
        seedurl = "";
        harvestName = "";
    }

    /** Parse a criteria for a single url. 
     * 
     * @param trimmedResultPart
     * @param ingestMode
     */
    private void parseString(String trimmedResultPart) {
    	//There may be ',' in URL, therefore unexpected text will be part of Url
    	//Although a criteria cannot be part of an URL
        if (trimmedResultPart.startsWith("http")) {
        	this.url = trimmedResultPart;
        }
        else if (trimmedResultPart.startsWith("C")) 
    {
            String[] valueparts = trimmedResultPart.split(":");
            if (valueparts.length > 1) {
            	String criteriaContent = valueparts[1].trim();
                if (trimmedResultPart.startsWith("Cext1")) {
                    this.Cext1 = Long.parseLong(criteriaContent);
                } else if (trimmedResultPart.startsWith("Cext2")) {
                    this.Cext2 = Long.parseLong(criteriaContent);
                } else if (trimmedResultPart.startsWith("Cext3")) {
                    this.Cext3Orig = criteriaContent;
                    System.out.println("Date: " + this.Cext3Orig);
                } else if (trimmedResultPart.startsWith("C4a")) {
                    C.put("C4a", criteriaContent);
                } else if (trimmedResultPart.startsWith("C4b")) {
                	C.put("C4b", trimmedResultPart.split("C4b:")[1].trim());  
                	System.out.println("C4b: " + C.get("C4b"));
                } else if (trimmedResultPart.startsWith("C1a")) {
                	C.put("C1a",criteriaContent);
                } else if (trimmedResultPart.startsWith("C2a")) {
                	C.put("C2a", criteriaContent);
                } else if (trimmedResultPart.startsWith("C2b")) {
                	C.put("C2b", criteriaContent); 				
                } else if (trimmedResultPart.startsWith("C3a")) {
                	C.put("C3a", criteriaContent);
                } else if (trimmedResultPart.startsWith("C3b")) {
                	C.put("C3b",criteriaContent);
                } else if (trimmedResultPart.startsWith("C3c")) {
                	C.put("C3c", criteriaContent);
                } else if (trimmedResultPart.startsWith("C3d")) {
                	C.put("C3d", criteriaContent);
                } else if (trimmedResultPart.startsWith("C3g")) {
                	C.put("C3g", criteriaContent); 				
                } else if (trimmedResultPart.startsWith("C5a")) {
                	C.put("C5a", criteriaContent);
                } else if (trimmedResultPart.startsWith("C5b")) {
                	C.put("C5b", criteriaContent);
                } else if (trimmedResultPart.startsWith("C6a")) {
                	C.put("C6a", criteriaContent);
                } else if (trimmedResultPart.startsWith("C6b")) {
                	C.put("C6b", criteriaContent);
                } else if (trimmedResultPart.startsWith("C6c")) {
                	C.put("C6c", criteriaContent);
                } else if (trimmedResultPart.startsWith("C6d")) {
                	C.put("C6d", criteriaContent); 				
                } else if (trimmedResultPart.startsWith("C7a")) {
                	C.put("C7a", criteriaContent);
                } else if (trimmedResultPart.startsWith("C7b")) {
                	C.put("C7b", criteriaContent);
                } else if (trimmedResultPart.startsWith("C7c")) {
                	C.put("C7c", criteriaContent);
                } else if (trimmedResultPart.startsWith("C7d")) {
                	C.put("C7d", criteriaContent);
                } else if (trimmedResultPart.startsWith("C7e")) {
                	C.put("C7e", criteriaContent);
                } else if (trimmedResultPart.startsWith("C7f")) {
                	C.put("C7f", criteriaContent);
                } else if (trimmedResultPart.startsWith("C7g")) {
                	C.put("C7g", criteriaContent); 				
                } else if (trimmedResultPart.startsWith("C7h")) {
                	C.put("C7h", criteriaContent); 				
                } else if (trimmedResultPart.startsWith("C8a")) {
                	C.put("C8a", criteriaContent);
                } else if (trimmedResultPart.startsWith("C8b")) {
                	C.put("C8b", criteriaContent);
                } else if (trimmedResultPart.startsWith("C8c")) {
                	C.put("C8c", criteriaContent); 				
                } else if (trimmedResultPart.startsWith("C9a")) {
                	C.put("C9a", criteriaContent);
                } else if (trimmedResultPart.startsWith("C9b")) {
                	C.put("C9b", criteriaContent);
                } else if (trimmedResultPart.startsWith("C9c")) {
                	C.put("C9c", criteriaContent);
                } else if (trimmedResultPart.startsWith("C9d")) {
                	C.put("C9d", criteriaContent);
                } else if (trimmedResultPart.startsWith("C9e")) {
                	C.put("C9e", criteriaContent); 				
                } else if (trimmedResultPart.startsWith("C9f")) {
                	C.put("C9f", criteriaContent); 				
                } else if (trimmedResultPart.startsWith("C10a")) {
                	C.put("C10a", criteriaContent);
                } else if (trimmedResultPart.startsWith("C10b")) {
                	C.put("C10b", criteriaContent);
                } else if (trimmedResultPart.startsWith("C10c")) {
                	C.put("C10c", criteriaContent); 				
                } else if (trimmedResultPart.startsWith("C15a")) {
                	C.put("C15a", criteriaContent);
                } else if (trimmedResultPart.startsWith("C15b")) {
                	C.put("C15b", criteriaContent);
                } else if (trimmedResultPart.startsWith("C16a")) {
                	C.put("C16a", criteriaContent);
                } else if (trimmedResultPart.startsWith("C17a")) {
                	C.put("C17a", criteriaContent);
                } else if (trimmedResultPart.startsWith("C18a")) {
                	C.put("C18a", criteriaContent);
                } else if (trimmedResultPart.startsWith("CText")) {
                	this.CText = criteriaContent;
                } else if (trimmedResultPart.startsWith("CLinks")) {
                	this.CLinks = splitLinks(criteriaContent);
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
    
    private List<String> splitLinks(String criteriaContent) {
    	List<String> linkSet = new ArrayList<String>();
		String[] resultParts = criteriaContent.split("##");
		for (String link: resultParts) {
			linkSet.add(link);
		}	
		return linkSet;
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
