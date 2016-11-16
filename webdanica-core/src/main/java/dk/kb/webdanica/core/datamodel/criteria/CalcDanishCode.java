package dk.kb.webdanica.core.datamodel.criteria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import dk.kb.webdanica.core.datamodel.criteria.CodesFraction;
import dk.kb.webdanica.core.datamodel.criteria.CodesResult.Display;
import dk.kb.webdanica.core.datamodel.criteria.CodesResult.Level;

public class CalcDanishCode {

	public static void main(String[] args) {
		Set<Integer> set1 = Codes.getCodesForLikelyDanishResults();
		Set<Integer> set2 = Codes.getCodesForFrasorterede();
		Set<Integer> set3 = Codes.getCodesForMaybees();
		Set<Integer> set4 = Codes.getCodesForUdgaaede();
		Set<Integer> set5 = Codes.getCodesForNOTDanishResults();
		
		Set<Integer> allsets = new TreeSet<Integer>();
		allsets.addAll(set1);
		allsets.addAll(set2);
		allsets.addAll(set3);
		allsets.addAll(set4);
		allsets.addAll(set5);
		for (Integer i: allsets) {
			System.out.println(i);
		}
		
		Level level = Level.none;
		Display codesOut = Display.noCodes;
		boolean viaFields = false;
		System.out.println("Describing codes for cat_likely_dk");
		for (Integer code: set1){
			System.out.println(getCalcDkCodeText(code, codesOut, level, viaFields));
		}
		System.out.println("-----------------------------------");
		System.out.println("Describing codes for cat_ignored_dk:");
		for (Integer code: set2){
			System.out.println(getCalcDkCodeText(code, codesOut, level, viaFields));
		}
		System.out.println("-----------------------------------");
		System.out.println("Describing codes for cat_maybes_dk:");
		for (Integer code: set3){
			System.out.println(getCalcDkCodeText(code, codesOut, level, viaFields));
		}
		System.out.println("-----------------------------------");
		System.out.println("Describing codes for ERROR_dk:");
		for (Integer code: set4){
			System.out.println(getCalcDkCodeText(code, codesOut, level, viaFields));
		}
		System.out.println("-----------------------------------");
		System.out.println("Describing codes for cat_not_likely_dk:");
		for (Integer code: set5){
			System.out.println(getCalcDkCodeText(code, codesOut, level, viaFields));
		}
		
	}	
	
	public static int maxbit = 22;
	public static String row_delim = "#";
	
	public static Set<Integer> getCodeSet (Connection conn, String tablename, CodesFraction frac) throws SQLException {
		Set<Integer> codeSet = new HashSet<Integer>();
		String selectSQL = "SELECT DISTINCT calcDanishCode FROM "+ tablename;

		switch (frac) {
		case codes_positive: 
			selectSQL = selectSQL + " WHERE calcDanishCode>0 ";
			break;
		case codes_negative: 
			selectSQL = selectSQL + " WHERE calcDanishCode<0 "; 
			break;
		case codes_nonpositive: 
			selectSQL = selectSQL + " WHERE calcDanishCode<=0 "; 
			break;
		case codes_nonnegative: 
			selectSQL = selectSQL + " WHERE calcDanishCode>=0 ";
			break;
		case codes_all: //ignore 
		}

		PreparedStatement t = conn.prepareStatement(selectSQL); 
		ResultSet trs = t.executeQuery();
		while (trs.next()) {
			codeSet.add(trs.getInt("calcDanishCode"));
		}
		trs.close();
		t.close();
		return codeSet;
	}


	public static Set<String> getUrlsForCalcCode(Connection conn, String tablenm, int code) throws SQLException {
		return getUrlsForCalcCode(conn, tablenm, code, "");
	}



	public static Set<String> getUrlsForCalcCode(Connection conn, String tablenm, int code, String where) throws SQLException {
		Set<String> urlSet = new HashSet<String>();
		String sql = "SELECT url FROM " + tablenm + " WHERE (calcDanishCode=" + code + ")" + (where.isEmpty() ? "" : " AND (" + where  + ")") ; 
		PreparedStatement s = conn.prepareStatement(sql); 
		ResultSet rs = s.executeQuery();
		while (rs.next()) {
			String url = rs.getString("Url");
			urlSet.add(url);
		}
		rs.close();
		s.close();
		return urlSet;
	}
	
	/**
	 * Used by the getCalcDkCodeText() method to describe which criteria if any finds anything for a certain url.
	 * @param bit
	 * @param codesOut
	 * @param viaFields
	 * @return
	 */
	public static String getBitCalcDkCodeText(short bit, Display codesOut, boolean viaFields) {
	    //text for codes
		String s = "";

		if (codesOut!=Display.onlyCodes) { 
			if (bit==1) s = (viaFields? "C1a[1]<>0 or C2a[1]<>0" : "ph or mail");
	        else if (bit==2) s = (viaFields? "C3a[1]<>0 or C3c[1]<>0" : "incl æ ø å");
	        else if (bit==3) s = (viaFields? "C4a = 'da' 'no' or 'sv'" : "language like danish");
	        else if (bit==4) s = (viaFields? "C6b[1]<>0 or C6c[1]<>0" : "freq. used selected words");
	        else if (bit==5) s = (viaFields? "C7a[1]<>0 or C7b[1]<>0 or C7e[1]<>0 or C7f[1]<>0" : "largest dk towns");
	        else if (bit==6) s = (viaFields? "C9a[1]<>0 or C9d[1]<>0" : "A/S… or CVR");
	        else if (bit==7) s = (viaFields? "C15a='y'" : "neighboring tld");
	        else if (bit==8) s = (viaFields? "C16a>0 or C17a>0" : "links");
	        else if (bit==9) s = (viaFields? "C3b[1]<>0 or C3d[1]<>0"  : "incl ae, oe, aa");
	        else if (bit==10) s = (viaFields? "new C6d[1]<>0 (as 6a)" : "new limited freq. used dk word"); //(viaFields? "C6a[1]<>0" : "freq. used dk word");
	        else if (bit==11) s = (viaFields? "C7c[1]<>0 or C7d[1]<>0" : "suffixes in town");
	        else if (bit==12) s = (viaFields? "C8a[1]<>0 or C8b[1]<>0" : "union and asscociation");
	        else if (bit==13) s = (viaFields? "C9b[1]<>0 or C9c[1]<>0" : "company names");
	        else if (bit==14) s = (viaFields? "new C10c[1]<>0 (as 10b)" : "new limited danish names"); //(viaFields? "C10a[1]<>0 or C10b[1]<>0" : "danish surnames & endings");
	        else if (bit==15) s = (viaFields? "Cext1>250" : "size>250");
	        else if (bit==16) s = (viaFields? "C10a[1]<>0" : "danish names (no endings)");
	        else if (bit==17) s = (viaFields? "new C8c[1]<>0 | C9e[1]<>0 (as 8a,9a)" : "new limited union or companies"); //Reset
	        else if (bit==18) s = (viaFields? "new C7g[1]<>0 | C7h[1]<>0 (as 7e,7a)" : "new limited largest dk towns (incl. translations)"); //Reset 
	        else if (bit==19) s = (viaFields? "Cext2>=120 (<200)" : "maybe chinese (Charsize>150)");
	        else if (bit==20) s = (viaFields? "C3e[1]<>0 (from 3b) and C3f[1]<>0  (from 3d)" : "limited wordlist o,oe,ae,aa");
	        else if (bit==21) s = (viaFields? "new C3g[1]<>0 (as 3b)" : "new limited wordlist o,oe,ae,aa");
	        else if (bit==22) s = (viaFields? "new C2b[1]='y' | C9f[1]='y'" : "ph. or cvr from reg. exp");
	        else if (bit==23) s = (viaFields? "C4b says da": "Tika says language is danish with over 90% certainty");
	        else s="UNKNOWN BIT" + bit;
	    }
	    if (codesOut==Display.onlyCodes) s = String.valueOf(bit) ; //text before count
	    else if (codesOut==Display.inText) s = s +  "(" + bit + ")"; //text before count
	    else if (codesOut==Display.separateText) s = bit +  row_delim  + s; //(number , text) before count
	    else if (codesOut==Display.allDisplays) s = bit +  row_delim  + s +  "(" + bit + ")" + row_delim  + s; //(number , text(number), text) before count
	    //else if (codesOut==Display.noCodes) s = s;
	    
	    return s;
	}    

	public static String getCalcDkCodeText(int code, Display codesOut, Level level, boolean viaFields) {
	    //text for codes
		String s = "";
		
	    if (codesOut!=Display.onlyCodes) { 
	        if (code==0) s = (viaFields ? "No bits and no criteria fulfilled" : "Not set yet");
	        else if (code==1) s = (viaFields ? "Cext1=0" : "size of html=0");
	        else if (code==2) s = (viaFields ? "Cext2>= 200" : "bytes per char > 2");
	        else if (code==3) s = (viaFields ? "C15b='dk'" : "tld=dk");
	        else if (code==4) s = (viaFields ? "C4a,C4b=da": "Language is Danish with over 90% certainty over 90%");
	        else if (code==5) s = "UDGÅET"; //(viaFields ? "C1a>0" : "dk mail address");
	        else if (code==6) s = "UDGÅET - WRONG ph.";
	        else if (code==7) s = "UDGÅET"; // (viaFields ? "C2a incl. +45 & tlf. + C5a>0 C5b=0" : "ph. and dk + NOT no. words");
	        else if (code==8) s = "UDGÅET"; // (viaFields ? "C2a incl. +45 & tlf. + C5a>0 C5b>0" : "ph. and dk + no.swords");
	        else if (code>=10 && code<=12) { //asian/arabic languages
	        	if (level==Level.intervals) s = (viaFields ? "C4a is arabic/asian language code" : "chinese/arabic");
	        	else if (code==10) s = (viaFields ? "C4a='zh'/'ja'/'ko'" : "chinese like languages");
	            else if (code==11) s = (viaFields ? "C4a = 'bo'/'hi'/'mn'/'my'/'ne'/'ta'/'th'/'vi'" : "asian languages");
	            else if (code==12) s = (viaFields ? "C4a='he'/'fa'/'ur'/'yi'/'ar'" : "arabic languages");
	        } else if ((code>=20 && code<=27) || (code>=40 && code<=47)) { //many dk indications 
	    	    int interval = ((code >=20 && code <=27) ? 20 : 40 ); 
	            s = "likely dk";
	            s = s + (interval == 20 ? " (size>250)" : " (200<=size<250)" );
	        	if (level!=Level.intervals) {
		    		int bit1 = (code-interval) / 4;
		    		int bit2 = (code-interval-(4*bit1)) / 2;
		    		int bit3 = (code-interval-(4*bit1)-(2*bit2)) ;
		        	boolean inclTld = (bit1==1);     // The URL belongs to a TLD often used by Danes
		        	boolean inclToLinks = (bit2==1); // There are .dk sites that points to the webpage
		        	boolean inclFromLinks = (bit3==1);	 // <The webpage points to other .dk sites>
		            s = s + " - " +(inclTld ? (viaFields?"C15a='y'":"dk used tld") : (viaFields?"C15a='n'":"NOT dk used tld") );
		            s = s + " - " + (inclToLinks ? (viaFields?"C16a>0":"has to links") : (viaFields?"C16a=0":"has NO to links") );
		            s = s + " - " + (inclFromLinks ? (viaFields?"C17a>0":"has from links") : (viaFields?"C17a=0":"has NO from links") );
	        	}
	        } else if ((code>=30 && code<=35) || code==38 || code==58 || (code>=50 && code<=55)) { //  no dk indications    
	    	    int interval = ((code >=30 && code <=35) ? 30 : 50 ); 
	            s = "NOT likely dk";
	            s = s + (interval == 30 ? "(size>250)" : "(200<=size<250)" );
	        	if (level!=Level.intervals) {
	        		if (code==38 || code==58) {
			            s = s + " - on new fields";
	        		} else {
			        	boolean inclTld = (code%2==1);     // The URL belongs to a TLD often used by Danes
			            s = s + " - " + (inclTld ? (viaFields?"C15a='y'":"dk used tld") : (viaFields?"C15a='n'":"NOT dk used tld") );
			        	boolean incl6aOnly = (code%10==2 || code%10==3);
			            s = s + (incl6aOnly ? (viaFields?" - 6a>0 only":" - freq dk words"): "" );
			        	boolean incl6aOr7cdOnly = (code%10==4 || code%10==5);
			            s = s + (incl6aOr7cdOnly ? (viaFields?" - 6a>0 | 7c>0 | 7d>0":" - freq dk words | dk town suffixes") : "" );
	        		}
	    		}
		    } else if (code >= 301 && code <= 302) {
	            s = "NOT likely dk - but ";
	        	if (level!=Level.intervals) {
	        		if (code==301) { 
	        			s = s + (viaFields? " - C8c>0":" - has unions");
	        		} else if (code==302) { 
	        			s = s + (viaFields? " - C9e or f>0":" - has companies");
	    	        } 
		        }
	        } else if (code >= 70 && code <= 79) {
		        s = "UDGÅET (110's and 120's instead)";
	        } else if (code>=100 && code<=112) { //(code>=70 && code<=79) { //  danish language indications
	            s = "dk language æøå";  //replaces 71, 72 - only html not URL
	        	if (level!=Level.intervals) {
	        		if (code==110) {
	        			s = s + (viaFields?"C3a>0 & C5a>0 & C5b=0":" - dk words & NOT 'no' words");
	        		} else if (code==111) {
	        			s = s + (viaFields?"C3a>0 & C5a>0 & C5b>0 & C15b<>no/sv":" - dk words  & 'no' words & ‘no’ or ‘sv’ tld");
	        		} else if (code==112) {
	        			s = s + (viaFields?"C3b>0 & C5a=0 & C5b>0 & C15b<>no/sv":" - NO dk & 'no' words & ‘no’ or ‘sv’ tld");
		        	}
	        	}
	        } else if (code>=120 && code<=128) { //(code>=70 && code<=79) { //  danish language indications
	            s = "dk language identified by Apache Tika";  //replaces 71, 72 - only html not URL
	        	if (level!=Level.intervals) {
	        		int dif = 0;
	        		if (code>=120 && code<=122) {
	        			s = s + (viaFields? " - C4a=da" :" - 'dk'");
	        			dif = code - 120;
	        		} else if (code>=123 && code<=125) {
		        		s = s + (viaFields? " - C4a=no" : " - 'no'");
	        			dif = code - 123;
	        		} else if (code>=126 && code<=128) {
		        		s = s + (viaFields? " - C4a=sv" : " - 'sv'");
	        			dif = code - 126;
		        	}
	        		if (dif==0) {
	        			s = s + (viaFields?" & C5a>0 & C5b=0":" - dk words & NOT 'no' words");
	        		} else if (dif==1) {
	        			s = s + (viaFields?" & C5a>0 & C5b>0 & C15b<>no/sv":" - dk words  & 'no' words & ‘no’ or ‘sv’ tld");
	        		} else if (dif==2) {
	        			s = s + (viaFields?" & C5a=0 & C5b>0 & C15b<>no/sv":" - NO dk & 'no' words & ‘no’ or ‘sv’ tld");
		        	}
	        	}
	        } else if (code>=100 && code<=107) { 
	            s = "size<=200";
	        	if (level!=Level.intervals) {
		    		int bit1 = (code-100) / 4;
		    		int bit2 = (code-100-(4*bit1)) / 2;
		    		int bit3 = (code-100-(4*bit1)-(2*bit2)) ;
		        	boolean inclc4a = (bit1==1);     // The URL belongs to a TLD often used by Danes
		        	boolean inclc3abcd = (bit2==1); // There are .dk sites that points to the webpage
		        	boolean inclc6abc = (bit3==1);	 // <The webpage points to other .dk sites>
		            s = s + " - " + (inclc4a ? (viaFields?"C4a='da'/'no'/'sv'":"scandinavian language") : (viaFields?"NOT C4a='da'/'no'/'sv'":"NOT scandinavian language" ));
		            s = s + " - " + (inclc3abcd ? (viaFields?"incl min. 1 C3abcd>0":"incl min. 1 æøå (C3*)") : (viaFields?"NO C3abcd>0)":"has NO æøå (C3*)" ));
		            s = s + " - " + (inclc6abc ? (viaFields?"incl min. 1 C6abc>0":"incl min. 1 dk words (C6*)") : (viaFields?"NO C6abc>0)":"has NO dk words (C6*)" ));
	        	}
	        } else if (code>=200 && code<=203) { 
	        	s = "UDGÅET";
	            /*s = "NOT likely dk CHECK union/comp";
	        	if (!intervals) {
		        	boolean inclTld = (code%2==1);     // The URL belongs to a TLD often used by Danes
		            s = s + (inclTld ? (viaFields?"C15a='y'":"dk used tld") : (viaFields?"C15a='n'":"NOT dk used tld") );
		        	boolean inclUnion = (code%200<2);
		            s = s + " - " + (inclUnion  ? (viaFields?"8ab>0":"unions") : (viaFields?"9ab>0":"companies") );
	        	} */
	        } else if (code>=206 && code<=207) { 
	        	s = "UDGÅET (mails)";
	        } else if (code>=208 && code<=209) { 
	        	s = "UDGÅET (phone)";
	        } else if (code==220) {
	        	s = (viaFields ? "Cext2>= 130 (<200)" : "bytes per char > 1,3 (<2)");
	        } else if (code==230) {
	        	s = (viaFields ? "C7g>0" : "dk towns (new)" );
	        } else if ((code>=310 && code<=313) || (code>=315 && code<=318) ) {
	        	int dif = 0;
	        	if (code>=310 && code<=313) {
	        		s = (viaFields ? "C2b>0" : "tlf (new)" );
	        		dif = code - 310;
		        } else if (code>=315 && code<=318) {
		        	s = (viaFields ? "C2a>0" : "tlf (old)" );
	        		dif = code - 315;
		        }
	    		if (dif==0) {
	    			s = s + (viaFields?" & C5a>0 & C5b=0":" - dk words & NOT 'no' words");
	    		} else if (dif==1) {
	    			s = s + (viaFields?" & C5a>0 & C5b>0 & C15b<>no/sv":" - dk words  & 'no' words & ‘no’ or ‘sv’ tld");
	    		} else if (dif==2) {
	    			s = s + (viaFields?" & C5a=0 & C5b>0 & C15b<>no/sv":" - NO dk & 'no' words & ‘no’ or ‘sv’ tld");
	        	} else { 
	    			s = s + " - rest";
	        	}
	        } else if ((code>=320 && code<=327)) {
	        	s = (viaFields ? "C1a>0" : "mail" );
	        	if (code==320) {
	    			s = s + (viaFields?" & C5a>0 & C5b=0":" - dk words & NOT 'no' words");
	        	} else if (code==326) {
	    			s = s + (viaFields?" - C7g>0 -  C5a=0 & C5b>0 & C15b<>no/sv":" - bynavn - NO dk & 'no' words & ‘no’ or ‘sv’ tld") ;
	        	} else if ( code==327) {
	    			s = s + (viaFields?" - C7g=0 -  C5a=0 & C5b>0 & C15b<>no/sv":" - NO dk & 'no' words & ‘no’ or ‘sv’ tld") ;
	        	} else if ( code==321) {
	    			s = s + (viaFields?" - C7g>0 - C5a>0 & C5b>0 & C15b<>no/sv":" - bynavn - dk words  & 'no' words & ‘no’ or ‘sv’ tld");
	        	} else if ( code==322) {
	    			s = s + (viaFields?" - C7g=0 - C5a>0 & C5b>0 & C15b<>no/sv":" - dk words  & 'no' words & ‘no’ or ‘sv’ tld");
	    			
	        	} else if ( code==323) {
	    			s = s + (viaFields?" - C7g>0 - resten":" - bynavn - resten");
	        	} else if ( code==324) {
	    			s = s + (viaFields?" - C7g=0 - resten":" - resten");
	        	} 
	        } else if (code==Codes.cat_ERROR_dk) {
	        	s = "error";
	        } else if (code==Codes.cat_ignored_dk) {
	        	s = "ignored";
	        } else if (code==Codes.cat_likely_dk) {
	        	s = "likely danish";
	        } else if (code==Codes.cat_maybes_dk) {
	        	s = "maybes";
	        } else if (code==Codes.cat_not_likely_dk) {
	        	s = "NOT likely danish";
	        } else if (code==Codes.cat_unknown_dk) {
	        	s = "undecided";
	    	} else if (code<0)  { //  codes for which fields are set (in xls doc)	=> calcDanishCode <0
	    		s = "";
	    		String txt = "";
	        	String separator = "++";
	        	for (int b=1; b<=maxbit; b++) {
	        		txt = (BitUtils.getBit((short)b, code)==1 ? getBitCalcDkCodeText((short)b, codesOut, viaFields) : "");
	        		s = s + (s.isEmpty() || txt.isEmpty() ? txt : separator  + txt);
	        	}
	    		s = "Not decided - but has: " + s;
	    	} else {
	        	s="UNKNOWN CODE " + code;
	        }
	    }
	    
	    if (codesOut==Display.onlyCodes) s = String.valueOf(code) ; //text before count
	    else if (codesOut==Display.inText) s = s +  "(" + code + ")"; //text before count
	    else if (codesOut==Display.separateText) s = code +  row_delim  + s; //(number , text) before count
	    else if (codesOut==Display.allDisplays) s = code +  row_delim  + s +  "(" + code + ")" + row_delim  + s; //(number , text(number), text) before count
	    //else if (codesOut==Display.noCodes) s = s;
	    
	    return s;
	}
	
	public static boolean checkForDanishCode4(SingleCriteriaResult res, String languagesFound) {
		List<Language> languages = Language.findLanguages(languagesFound);
		for (Language l: languages) {
			if (l.getCode().equals("da") && l.getConfidence() > 0.90F) {
				res.intDanish = 1;
				res.calcDanishCode = 4;
				return true;
			}
		}
        return false;
    }
}
