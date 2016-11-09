package dk.kb.webdanica.core.datamodel.criteria;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static dk.kb.webdanica.core.datamodel.criteria.BitUtils.setBit;

public class CodesResult {
	public float intDanish;
	public int calcDanishCode;

	public CodesResult() {
		intDanish = 0F;
		calcDanishCode = 0;
	}

	// Static classes producing 
	
        
	/*public static boolean containsDkAndNotNw(String C5a, String C5b) {
	boolean found = false;
	if ((C5a!=null) && (!C5a.isEmpty()) && (!C5a.startsWith("0"))) {
		if (C5a.startsWith("1")) 
			found = !(C5a.contains("nu") || C5a.contains("af"));
		else if (C5a.startsWith("2"))
			found = !(C5a.contains("nu") && C5a.contains("af"));
		found = found && (C5b==null) || (C5b.isEmpty()) || (C5b.startsWith("0"));
	}
	return found;
}*/

public static boolean containsDk(String C5a) {
	boolean found = false;
	if ((C5a!=null) && (!C5a.isEmpty()) && (!C5a.startsWith("0"))) {
		if (C5a.startsWith("1")) 
			found = !(C5a.contains("nu") || C5a.contains("af"));
		else if (C5a.startsWith("2"))
			found = !(C5a.contains("nu") && C5a.contains("af"));
	}
	return found;
}

public static boolean containsNoNw(String C5b) {
	return (C5b==null) || (C5b.isEmpty()) || (C5b.startsWith("0"));
}

public static boolean containsNwAndTldNwOrSe(String C5b, String C15b) {
	boolean found = false;
	if ((C5b!=null) && (!C5b.isEmpty()) && (!C5b.startsWith("0"))) {
		if (C15b!=null && (C15b.equals("no") || C15b.equals("se"))) {
			found=true;
		}
	}
	return found;
}

public static CodesResult setcodes_mail(String C1a, String C5a, String C5b, String C15b, String C7g)  {
	CodesResult cr = new CodesResult();
	if ( (C1a!=null) && (!C1a.isEmpty()) && (!C1a.startsWith("0")) ) { //has phone number
		if (containsDk(C5a) && containsNoNw(C5b)) {
			cr.calcDanishCode = 320; //C1a mails dk ord – ingen norske ord
			cr.intDanish = 95/100;
		} else if (containsNwAndTldNwOrSe(C5b, C15b) && !containsDk(C5a)) {
			if ( (C7g!=null) && (!C7g.isEmpty()) && (!C7g.startsWith("0")) ) { 
				cr.calcDanishCode = 326; // probably no & bynavn
				cr.intDanish = 11/100;
			} else {
				cr.calcDanishCode = 327; //probably no 
				cr.intDanish = 10/100;
			}
		} else if (containsNwAndTldNwOrSe(C5b, C15b) && containsDk(C5a)) {
			if ( (C7g!=null) && (!C7g.isEmpty()) && (!C7g.startsWith("0")) ) { 
				cr.calcDanishCode = 321; // probably no & bynavn
				cr.intDanish = 50/100;
			} else {
				cr.calcDanishCode = 322; //probably no 
				cr.intDanish = 50/100;
			}
		} else {
			if ( (C7g!=null) && (!C7g.isEmpty()) && (!C7g.startsWith("0")) ) { 
				cr.calcDanishCode = 323; //mails resten & bynavn
				cr.intDanish = 80/100;
			} else {
				cr.calcDanishCode = 324; //mails resten
				cr.intDanish = 80/100;
			}
		}
	}
	return cr;
}

public static CodesResult setcodes_languageDklettersNew(String C3a, String C5a, String C5b, String C15b)  {
	CodesResult cr = new CodesResult(); 
	if (C3a!=null && (!C3a.isEmpty()) && (!C3a.startsWith("0")))  { //has æøå
		if (containsDk(C5a) && containsNoNw(C5b)) {
			cr.calcDanishCode = 110;
			cr.intDanish = 95/100;
		} else if (containsNwAndTldNwOrSe(C5b, C15b) && !containsDk(C5a)) {
			cr.calcDanishCode = 112;
			cr.intDanish = 10/100;
		} else if (containsNwAndTldNwOrSe(C5b, C15b) && containsDk(C5a)) {
			cr.calcDanishCode = 111;
			cr.intDanish = 10/100;
		}
	}
	return cr;
}

public static CodesResult setcodes_languageDkNew(String C4a, String C5a, String C5b, String C15b)  {
	//res.calcDanishCode = 76-79  likely dk language (not norwegain)
	CodesResult cr = new CodesResult(); 
	if ( (C4a!=null) && (!C4a.isEmpty()) ) { //has language code
		int code = 0;
		if (C4a.equals("da")) code = 120; 
		else if (C4a.equals("no")) code = 123; 
		else if (C4a.equals("sv")) code = 126; 
    	if (code>0) {
			if (containsDk(C5a) && containsNoNw(C5b)) {
				cr.calcDanishCode = code;
				cr.intDanish = 90/100;
			} else if (containsNwAndTldNwOrSe(C5b, C15b) && !containsDk(C5a)) {
				cr.calcDanishCode = code + 2;
				cr.intDanish = 10/100;
			} else if (containsNwAndTldNwOrSe(C5b, C15b) && containsDk(C5a)) {
				cr.calcDanishCode = code + 1;
				cr.intDanish = 50/100;
			}
		}
	}
	return cr;
}

public static CodesResult setcodes_newPhone(String C2b, String C5a, String C5b, String C15b)  {
	CodesResult cr = new CodesResult(); 
	if ( (C2b!=null) && (!C2b.isEmpty()) && (!C2b.startsWith("n")) ) { //has phone number
		if (containsDk(C5a) && containsNoNw(C5b)) {
			cr.calcDanishCode = 310;
			cr.intDanish = 90/100;
		} else if (containsNwAndTldNwOrSe(C5b, C15b) && !containsDk(C5a)) {
			cr.calcDanishCode = 312;
			cr.intDanish = 10/100;
		} else if (containsNwAndTldNwOrSe(C5b, C15b) && containsDk(C5a)) {
			cr.calcDanishCode = 311;
			cr.intDanish = 50/100;
		} else {
			cr.calcDanishCode = 313;
			cr.intDanish = 75/100;
		}
	}
	return cr;
}

public static CodesResult setcodes_oldPhone(String C2a, String C5a, String C5b, String C15b)  {
	CodesResult cr = new CodesResult(); 
	if ( (C2a!=null) && (!C2a.isEmpty()) ) {
		if (C2a.contains("tlf") && C2a.contains("+45")) { //has phone number
			if (containsDk(C5a) && containsNoNw(C5b)) {
				cr.calcDanishCode = 315;
				cr.intDanish = 85/100;
			} else if (containsNwAndTldNwOrSe(C5b, C15b) && !containsDk(C5a)) {
				cr.calcDanishCode = 317;
				cr.intDanish = 10/100;
			} else if (containsNwAndTldNwOrSe(C5b, C15b) && containsDk(C5a)) {
				cr.calcDanishCode = 316;
				cr.intDanish = 50/100;
			} else {
				cr.calcDanishCode = 318;
				cr.intDanish = 70/100;
			}
		}
	}
	return cr;
}

public static int findNegativBitmapCalcCode(SingleCriteriaResult res) {
	int code = 0;
	boolean setbit = false;
	String C1a = res.C.get("C1a");
	String C2a = res.C.get("C2a");
	
    setbit = ((C1a!=null) && (C1a.length()>2)); // include country’s TLD email address 
    setbit = setbit || ((C2a!=null) && (C2a.length()>2)); // include national phone number 
	if (setbit) code = setBit(1, code); //ph or mail

    setbit = ((res.C.get("C3a") !=null) && (res.C.get("C3a").length()>2));  
    setbit = setbit || ((res.C.get("C3c") !=null) && (res.C.get("C3c").length()>2)); 
	if (setbit) code = setBit(2, code); //æøå in html or url

	String C4a = res.C.get("c4a");
    if (C4a!=null) {
    	setbit = (C4a.equals("da") || C4a.equals("no") || C4a.equals("sv"));
    	if (setbit) code = setBit(3, code); //language like danish
    }

	setbit = ((res.C.get("C6b")!=null) && (res.C.get("C6b").length()>2));  
    setbit = setbit || ((res.C.get("C6c")!=null) &&  (res.C.get("C6c").length()>2)); 
	if (setbit) code = setBit(4, code); //frequently used selected Danish words

	setbit = ((res.C.get("C7a") !=null) && (res.C.get("C7a").length()>2));  
    setbit = setbit || ((res.C.get("C7b") !=null) &&  (res.C.get("C7b").length()>2)); 
    setbit = setbit || ((res.C.get("C7e") !=null) &&  (res.C.get("C7e").length()>2)); 
    setbit = setbit || ((res.C.get("C7f") !=null) &&  (res.C.get("C7f").length()>2)); 
	if (setbit) code = setBit(5, code); //largest Danish towns

    setbit = ((res.C.get("C9a")!=null) &&  (res.C.get("C9a").length()>2)); 
    setbit = setbit || ((res.C.get("C9d")!=null) &&  (res.C.get("C9d").length()>2)); 
	if (setbit) code = setBit(6, code); //A/S… or CVR
    if (res.C.get("C15a")!=null) {
    	setbit = (res.C.get("C15a").equals("y"));
    	if (setbit) code = setBit(7, code); //neighboring countries
    }
    setbit = ((res.C.get("C16a")!=null) && (!res.C.get("C16a").isEmpty()) && (Long.parseLong(res.C.get("C16a")) > 0L)); 
    setbit =  setbit || ((res.C.get("C17a")!=null) && (!res.C.get("C17a").isEmpty()) && (Long.parseLong(res.C.get("C17a")) > 0L)); 
	if (setbit) code = setBit(8, code); //links

    setbit = ((res.C.get("C3b")!=null) &&  (res.C.get("C3b").length()>2)); 
    setbit = setbit || ((res.C.get("C3d")!=null) &&  (res.C.get("C3d").length()>2)); 
	if (setbit) code = setBit(9, code); //words including ae, oe, aa in html or url

    setbit = (res.C.get("C6d")!=null) && ( (!res.C.get("C6d").startsWith("0")) && (!res.C.get("C6d").isEmpty()) ); //setbit = ((res.C6a!=null) &&  (res.C6a.length()>2));
	if (setbit) code = setBit(10, code); //frequently used Danish words NEW

    setbit = ((res.C.get("C7c")!=null) &&  (res.C.get("C7c").length()>2)); 
    setbit = setbit || ((res.C.get("C7d")!=null) &&  (res.C.get("C7d").length()>2)); 
	if (setbit) code = setBit(11, code); //suffixes in town in html and url

    setbit = ((res.C.get("C8a")!=null) &&  (res.C.get("C8a").length()>2)); 
    setbit = setbit || ((res.C.get("C8b")!=null) &&  (res.C.get("C8b").length()>2)); 
	if (setbit) code = setBit(12, code); //union and asscociation in htm and url

    setbit = ((res.C.get("C9b")!=null) &&  (res.C.get("C9b").length()>2)); 
    setbit = setbit || ((res.C.get("C9c")!=null) &&  (res.C.get("C9c").length()>2)); 
	if (setbit) code = setBit(13, code); //company names in htm and url

    setbit = ((res.C.get("C10c")!=null) &&  (!res.C.get("C10c").startsWith("0")) 
    		&& (!res.C.get("C10c").isEmpty())); //((res.C10a!=null) &&  (res.C10a.length()>2)) || ((res.C10b!=null) &&  (res.C10b.length()>2)); 
	if (setbit) code = setBit(14, code); //danish surnames NEW

    setbit = ((res.Cext1!=null) &&  (res.Cext1>250)); //size is considrable for language check
    if (setbit) code = setBit(15, code);  //

	// changed from 1/8 2014 now code is int     	
    setbit = ((res.C.get("C10a")!=null) &&  (res.C.get("C10a").length()>2)); 
    if (setbit) code = setBit(16, code);  //
    
    //NEW  new C8c[1]<>0 | C9e[1]<>0 (as 8a,9a)" : "new limmited union or companies
    setbit = (res.C.get("C8c")!=null) && ( (!res.C.get("C8c").startsWith("0")) && (!res.C.get("C8c").isEmpty()) ); 
    setbit = setbit || (res.C.get("C9e")!=null) && ( (!res.C.get("C9e").startsWith("0")) && (!res.C.get("C9e").isEmpty()) ); 
    if (setbit) code = setBit(17, code);
    
    //NEW  C7g[1]<>0 | C7h[1]<>0 (as 7e,7a)" : "new limited largest dk towns (incl. translations)"); //Reset 
    setbit = (res.C.get("C7g")!=null) && ( (!res.C.get("C7g").startsWith("0")) && (!res.C.get("C7g").isEmpty()) ); 
    setbit = setbit || (res.C.get("C7h")!=null) && ( (!res.C.get("C7h").startsWith("0")) && (!res.C.get("C7h").isEmpty()) ); 
    if (setbit) code = setBit(18, code);  //

    //Cext2>=150 (<200), likely chinese or the like         
    setbit = ((res.Cext2!=null) &&  (res.Cext2>=150)); 
    if (setbit) code = setBit(19, code);  //

    setbit = (res.C.get("C3e")!=null) && ( (!res.C.get("C3e").startsWith("0")) && (!res.C.get("C3e").isEmpty()) ); 
    setbit = setbit || ( (res.C.get("C3f")!=null) && ( (!res.C.get("C3f").startsWith("0")) && (!res.C.get("C3f").isEmpty()) ) ); 
    if (setbit) code = setBit(20, code);  //
    
    setbit = (res.C.get("C3g") !=null) && ( (!res.C.get("C3g").startsWith("0")) && (!res.C.get("C3g").isEmpty()) ); 
    if (setbit) code = setBit(21, code);  //
    
    setbit = (res.C.get("C2b")!=null) && ( res.C.get("C2b").equals("y") ); 
    setbit = setbit || (res.C.get("C9f")!=null) && ( res.C.get("C9f").equals("y") ); 
    if (setbit) code = setBit(22, code);  //

    return code;
}

public static boolean setcodes_dkLanguageVeryLikely(SingleCriteriaResult res)  {
	boolean ok = true;
	
	boolean bigSize = (res.Cext1>250); //40 for size 200-250 -
    int interval = (bigSize ? 20 : 40);

	boolean inclTld = (res.C.get("C15a") != null);
	inclTld = inclTld && (res.C.get("C15a").equals("y"));  // The URL belongs to a TLD often used by Danes
	boolean inclToLinks = (res.C.get("C16a")!=null);
	inclToLinks = inclToLinks && (Long.parseLong(res.C.get("C16a"))>0L);  // There are .dk sites that points to the webpage
	boolean inclFromLinks = (res.C.get("C17a")!=null);
	inclFromLinks = inclFromLinks && (Long.parseLong(res.C.get("C17a"))>0L);  // <The webpage points to other .dk sites>
	int calcCode = interval + (inclTld ? 4 : 0) + (inclToLinks ? 2 : 0) + (inclFromLinks ? 1 : 0);
    int startsize = (interval == 20 ? 250 : 200);
    int endsize = (interval == 20 ?   0 : 250);

    ok = (res.C.get("C3a")!=null && res.C.get("C4a")!=null && res.C.get("C5a")!=null 
    		&& res.C.get("C5b")!=null && res.C.get("C6a")!=null); 
    if (ok) {
    	ok = res.Cext1 > startsize //only consider when there are lots of text for n-gram
    		&& (endsize==0 ? true : (res.Cext1 <= endsize) )
			&& res.C.get("C3a").length()>2 //includes æ,ø or å
			&& res.C.get("C4a").equals("da")
			&& res.C.get("C5a").length() >2 //includes typical and distingisable Danish words
			&& res.C.get("C5b").startsWith("0") //do not include typical Norwegain words
			&& res.C.get("C6a").length()>2 //includes frequently used Danish words"
			&& res.C.get("C15a").equals(inclTld ? "y":"n") //The URL belongs to a TLD often used by Danes
			&& (inclToLinks? Long.parseLong(res.C.get("C16a"))>0 : Long.parseLong(res.C.get("C16a"))==0) //There are .dk sites that points to the webpage 
			&& (inclFromLinks? Long.parseLong(res.C.get("C17a"))>0 : Long.parseLong(res.C.get("C17a"))==0); 
    }
	if (ok) { //set code and calculate intDanish 
		float sizeFactor=1;
        float intDanish=1;
		
        // Size factor 0.90-1.0 for size > (size:200 or 250)
        if (res.Cext1  < 5000) { 
            //exp(5) = 148.41315910258 close to 150
        	sizeFactor= (float) (90 + Math.exp(res.Cext1/1000)/15); 
        } // else it is already set
        intDanish = intDanish * sizeFactor;
        
    	/* *********************** */
    	/* includes æ,ø or å  	   */
    	/* *********************** */
        String[] c3aParts = res.C.get("C3a").split(" ");
        float c3aFactor=1;
        if (c3aParts.length != 2) {
        	System.out.println("WARNING: wrong C3a value: " + res.C.get("C3a"));
        	c3aFactor=0;
        } else {
        	int num = Integer.parseInt(c3aParts[0]);
        	if (num<3) {
        		c3aFactor =  num/3;
            } //else remain 1
        }
        intDanish = intDanish * c3aFactor;
        
		/* ******************************************************************** */
    	/* C4a, C5a and C5b DO NOT contribute with further refinement 			*/
    	/* ******************************************************************** */
        // nothing
        
		/* ******************************************** */
    	/* includes frequently used Danish words 		*/
    	/* ******************************************** */
        String[] c6aParts = res.C.get("C6a").split(" ");
        float c6aFactor=1;
        if (c6aParts.length != 2) {
        	System.out.println("WARNING: wrong C6a value: " + res.C.get("C6a"));
        	c6aFactor=0;
        } else {
            // C6a factor 0.90-1 where (<number of words>/<size>) > 8 is very good (based on calc. on 20 danish sites)
        	// e.g. http://www.fullnamedirectory.com/name1141.html gives 0,00079 and is NOT in Danish
        	int num = Integer.parseInt(c6aParts[0]);
        	double d = num/res.Cext1;
           	if (d<(1/2)) { //NB target for adjustment
            	System.out.println("WARNING: less than C6a factor 0,5 : " + d + " url: " + res.url);
            	c6aFactor=0;
           	} else if (d<8) {
            	c6aFactor = (float)(((d/8*10)+90)/100);
        	} //else remain 1
        }
        intDanish = intDanish * c6aFactor;
        
		/* ******************************************** */
    	/* The URL belongs to a TLD often used by Danes */
    	/* ******************************************** */
        if (!inclTld) {
            intDanish = intDanish * 98/100;  //only set if TLD is NOT often used by danes - just a guestimate
        }
        
		/* ******************************************** */
    	/* The webpage points to other .dk sites        */
    	/* ******************************************** */
        if (!inclFromLinks) {
            intDanish = intDanish * 98/100;  //only set if it is NOT have links- just a guestimate
        }

		/* ******************************************** */
    	/* The URL belongs to a TLD often used by Danes */
    	/* ******************************************** */
        if (!inclToLinks) {
            intDanish = intDanish * 99/100;  //only set if it is NOT have links- just a guestimate
        }

    	/* *********************** */
    	/* Update            	   */
    	/* *********************** */
        res.calcDanishCode = calcCode;
        res.intDanish = intDanish;
	}
    return ok;
}






enum NotDkExceptions{
	noException,
	unions,
	companies,
}

/**
 * 
 * @param res
 * @param e
 * @return a CodesResult
 */
public static CodesResult setcodes_notDkLanguageVeryLikelyNewFields(SingleCriteriaResult res, NotDkExceptions e)  {
	CodesResult cr = new CodesResult(); 
	
    boolean bigSize = (res.Cext1>250); // 50 for size 200-250
	boolean inclTld = (res.C.get("C15a")!=null);
	inclTld = inclTld && (res.C.get("C15a").equals("y"));     // The URL belongs to a TLD often used by Danes

	boolean incl7g = (res.C.get("C7g")!=null);
	incl7g = incl7g && !res.C.get("C7g").startsWith("0");
	incl7g = incl7g && !res.C.get("C7g").isEmpty();
	
	boolean incl7h = (res.C.get("C7h")!=null);
	incl7h = incl7h && !res.C.get("C7h").startsWith("0");
	incl7h = incl7h && !res.C.get("C7h").isEmpty();

	boolean ok = true;
	
    ok = ok && ((res.C.get("C1a")==null) || (res.C.get("C1a").isEmpty()) || (res.C.get("C1a").startsWith("0"))); //Do NOT include country’s TLD email address 
    ok = ok && ((res.C.get("C2b")==null) || (res.C.get("C2b").isEmpty()) || (res.C.get("C2b").startsWith("n"))); //Do NOT include national phone number 
    ok = ok && ((res.C.get("C3a")==null) || (res.C.get("C3a").isEmpty()) || (res.C.get("C3a").startsWith("0"))); //Do NOT include æ,ø or å
    ok = ok && ((res.C.get("C3g")==null) || (res.C.get("C3g").isEmpty()) || (res.C.get("C3g").startsWith("0"))); //Do NOT include frequently used Danish words with coded æ, ø, å on form ae, oe/o, aa 
    ok = ok && ((res.C.get("C4a")==null) || (res.C.get("C4a").isEmpty()) || ((!res.C.get("C4a").equals("da")) 
    		&& (!res.C.get("C4a").equals("no")) && (!res.C.get("C4a").equals("sv"))));	//n-gram does NOT points at Scandinavian language
    ok = ok && ((res.C.get("C5a")==null) || (res.C.get("C5a").isEmpty()) || (res.C.get("C5a").startsWith("0"))); //do NOT includes typical and distingisable Danish words
    ok = ok && ((res.C.get("C7g")==null) || (res.C.get("C7g").isEmpty()) || (res.C.get("C7g").startsWith("0"))); // NOT list of 45 largest Danish towns (http://wwwC4a=="dnavneudvalget.ku.dk/) 
    ok = ok && ((res.C.get("C7h")==null) || (res.C.get("C7h").isEmpty()) || (res.C.get("C7h").startsWith("0"))); // NOT København (Copenhagen) and Danmark (Denmark) translated to English, German, French and other European languages as well as Turkish, Somali and Romanian.
    if (e!=NotDkExceptions.unions) {
        ok = ok && ((res.C.get("C8c")==null) || (res.C.get("C8c").isEmpty()) || (res.C.get("C8c").startsWith("0"))); // unions
    }
    if (e!=NotDkExceptions.companies) {
        ok = ok && ((res.C.get("C9e")==null) || (res.C.get("C9e").isEmpty()) || (res.C.get("C9e").startsWith("0"))); // companies
        ok = ok && ((res.C.get("C9f")==null) || (res.C.get("C9f").isEmpty()) || (res.C.get("C9f").startsWith("n"))); // companies
    }
    ok = ok && ((res.C.get("C10c")==null) || (res.C.get("C10c").isEmpty()) || (res.C.get("C10c").startsWith("0"))); // NOT look for typical patterns in Danish surnames like names ending in 'sen' (for son)
    ok = ok && ((res.C.get("C16a")==null) || (Long.parseLong(res.C.get("C16a"))==0));   //The URL does NOT belong to a TLD often used by Danes
    ok = ok && ((res.C.get("C17a")==null) || (Long.parseLong(res.C.get("C17a"))==0));   //The URL does NOT belong to a TLD often used by Danes

    if (ok) { // Candidate
   	 	if (e==NotDkExceptions.noException) {
        	cr.calcDanishCode = (bigSize ? 38 : 58);
        	cr.intDanish = 2/100;
   	 	} else if (e==NotDkExceptions.unions) {
        	cr.calcDanishCode = 301;
        	cr.intDanish = 10/100;
   	 	} else if (e==NotDkExceptions.companies) {
        	cr.calcDanishCode = 302;
        	cr.intDanish = 10/100;
   	 	}
    } 
    return cr;
}


public static CodesResult setcodes_notDkLanguageVeryLikely(SingleCriteriaResult res)  {
	CodesResult cr = new CodesResult(); 

	boolean bigSize = (res.Cext1>250); // 50 for size 200-250
    int interval = (bigSize ? 30 : 50);
    
	boolean inclTld = (res.C.get("C15a")!=null);
	inclTld = inclTld && (res.C.get("C15a").equals("y"));     // The URL belongs to a TLD often used by Danes

	boolean incl6a = (res.C.get("C6a") !=null);
	incl6a = incl6a && !res.C.get("C6a").startsWith("0");
	incl6a = incl6a && !res.C.get("C6a").isEmpty();
	//System.out.println("incl6a: " + incl6a + " - 6a: " + res.C6a);
	
	boolean incl7c = (res.C.get("C7c")!=null);
	incl7c = incl7c && !res.C.get("C7c").startsWith("0");
	incl7c = incl7c && !res.C.get("C7c").isEmpty();
	
	boolean incl7d = (res.C.get("C7d")!=null);
	incl7d = incl7d && !res.C.get("C7d").startsWith("0");
	incl7d = incl7d && !res.C.get("C7d").isEmpty();
	
	/*boolean incl8a = (res.C8a!=null);
	incl8a = incl8a && !res.C8a.startsWith("0");
	
	boolean incl8b = (res.C8b!=null);
	incl8b = incl8b && !res.C8b.startsWith("0");
	
	boolean incl9b = (res.C9b!=null);
	incl9b = incl9b && !res.C9b.startsWith("0");
	
	boolean incl9c = (res.C9c!=null);
	incl9c = incl9c && !res.C9c.startsWith("0"); */     

	//int startsize = (interval == 30 ? 250 : 200);
    //int endsize = (interval == 30 ?   0 : 250);
    
    boolean ok = true;
    // look at res.C1a
    ok = ok && ((res.C.get("C1a")==null) || (res.C.get("C1a").isEmpty()) || (res.C.get("C1a").startsWith("0"))); //Do NOT include country’s TLD email address 
    ok = ok && ((res.C.get("C2a")==null) || (res.C.get("C2a").isEmpty()) || (res.C.get("C2a").startsWith("0"))); //Do NOT include national phone number 
    ok = ok && ((res.C.get("C3a")==null) || (res.C.get("C3a").isEmpty()) || (res.C.get("C3a").startsWith("0"))); //Do NOT include æ,ø or å
    ok = ok && ((res.C.get("C3b")==null) || (res.C.get("C3b").isEmpty()) || (res.C.get("C3b").startsWith("0"))); //Do NOT include frequently used Danish words with coded æ, ø, å on form ae, oe/o, aa 
    ok = ok && ((res.C.get("C3c")==null) || (res.C.get("C3c").isEmpty()) || (res.C.get("C3c").startsWith("0"))); //Do NOT include same as C3a, but on the URL in uft8 URL encoding
    ok = ok && ((res.C.get("C3d")==null) || (res.C.get("C3d").isEmpty()) || (res.C.get("C3d").startsWith("0"))); //Do NOT include same as C3c, but on the URL in uft8 URL encoding
    ok = ok && ((res.C.get("C4a")==null) || (res.C.get("C4a").isEmpty()) || ((!res.C.get("C4a").equals("da"))
    			&& (!res.C.get("C4a").equals("no")) && (!res.C.get("C4a").equals("sv"))));	//n-gram does NOT points at Scandinavian language
    ok = ok && ((res.C.get("C5a")==null) || (res.C.get("C5a").isEmpty()) || (res.C.get("C5a").startsWith("0"))); //do NOT includes typical and distingisable Danish words
    ok = ok && ((res.C.get("C6b")==null) || (res.C.get("C6b").isEmpty()) || (res.C.get("C6b").startsWith("0"))); // NOT typical Danish words like 'dansk', 'Danmark' and 'forening'
    ok = ok && ((res.C.get("C6c")==null) || (res.C.get("C6c").isEmpty()) || (res.C.get("C6c").startsWith("0"))); // NOT same as C6b, but on the URL, plus typical Danish notions '/dk/' or '/da/' 
    ok = ok && ((res.C.get("C7a")==null) || (res.C.get("C7a").isEmpty()) || (res.C.get("C7a").startsWith("0"))); // NOT list of 45 largest Danish towns (http://wwwC4a=="dnavneudvalget.ku.dk/) 
    ok = ok && ((res.C.get("C7b")==null) || (res.C.get("C7b").isEmpty()) || (res.C.get("C7b").startsWith("0"))); // NOT same as C7a, but on the URL 
    ok = ok && ((res.C.get("C7e")==null) || (res.C.get("C7e").isEmpty()) || (res.C.get("C7e").startsWith("0"))); // NOT København (Copenhagen) and Danmark (Denmark) translated to English, German, French and other European languages as well as Turkish, Somali and Romanian.
    ok = ok && ((res.C.get("C7f")==null) || (res.C.get("C7f").isEmpty()) || (res.C.get("C7f").startsWith("0"))); // NOT same as C7e, but on the URL 
    ok = ok && ((res.C.get("C8a")==null) || (res.C.get("C8a").isEmpty()) || (res.C.get("C8a").startsWith("0"))); // unions
    ok = ok && ((res.C.get("C8b")==null) || (res.C.get("C8b").isEmpty()) || (res.C.get("C8b").startsWith("0"))); // unions
    ok = ok && ((res.C.get("C9a")==null) || (res.C.get("C9a").isEmpty()) || (res.C.get("C9a").startsWith("0"))); // NOT same as C9b, but on the URL
    ok = ok && ((res.C.get("C9b")==null) || (res.C.get("C9b").isEmpty()) || (res.C.get("C9b").startsWith("0"))); // companies
    ok = ok && ((res.C.get("C9c")==null) || (res.C.get("C9c").isEmpty()) || (res.C.get("C9c").startsWith("0"))); // companies
    ok = ok && ((res.C.get("C9d")==null) || (res.C.get("C9d").isEmpty()) || (res.C.get("C9d").startsWith("0"))); // NOT search for CVR + 8 digits for registered Danish company number 
    ok = ok && ((res.C.get("C10a")==null) || (res.C.get("C10a").isEmpty()) || (res.C.get("C10a").startsWith("0"))); // NOT look for typical patterns in Danish surnames like names ending in 'sen' (for son)
    ok = ok && ((res.C.get("C10b")==null) || (res.C.get("C10b").isEmpty()) || (res.C.get("C10b").startsWith("0"))); // NOT look in list of 150 frequently used Danish first names and surnames
    ok = ok && ((res.C.get("C15a")==null) || (res.C.get("C15a").isEmpty()) || (res.C.get("C15a").equals(inclTld ? "y":"n")));   //The URL does NOT belong to a TLD often used by Danes
    ok = ok && ((res.C.get("C16a")==null) || (Long.parseLong(res.C.get("C16a"))==0));   //The URL does NOT belong to a TLD often used by Danes
    ok = ok && ((res.C.get("C17a")==null) || (Long.parseLong(res.C.get("C17a"))==0));   //The URL does NOT belong to a TLD often used by Danes

    if (ok) { // Candidate
    	if (!incl6a && !incl7c && !incl7d) { // && !incl8a && !incl8b && !incl9b && !incl9c) {
            cr.calcDanishCode = (interval + (inclTld ? 1 : 0));
            cr.intDanish = 1/100;
        } else if (!incl7c && !incl7d) { // && !incl8a && !incl8b && !incl9b && !incl9c) { //ignore-6a 32,33,52,53 
        	cr.calcDanishCode = (interval + 2 + (inclTld ? 1 : 0));
        	cr.intDanish = 2/100;
        } else { // if (!incl8a && !incl8b && !incl9b && !incl9c) { //ignore-6a + 7cd 34,35,54,55
        	cr.calcDanishCode = (interval + 4 + (inclTld ? 1 : 0));
        	cr.intDanish = 4/100;
        } /*else if (incl8a || incl8b ) { //ignore-8ab +...
            cr.calcDanishCode = (200 + (inclTld ? 1 : 0));
            cr.intDanish = 10/100;
        } else if (incl9b || incl9c ) { //ignore-9ab +...
            cr.calcDanishCode = (202 + (inclTld ? 1 : 0));
            cr.intDanish = 10/100;
        } */
    } 
    return cr;
}

public static CodesResult setcodes_otherLanguagesChars(String c4a)  {
	//check with codes from http://www.loc.gov/standards/iso639-2/php/code_list.php
	CodesResult coderes = new CodesResult();
	
	if (c4a!=null) { 
    	int code = 0;
	    c4a = c4a.trim();
    	
    	if (c4a.equals("zh") || c4a.equals("ja") || c4a.equals("ko"))  { 
    		// Chinese, Japanese, Korean 
    		code = 10;
    	} else if (c4a.equals("bo") || c4a.equals("hi") || c4a.equals("mn") || c4a.equals("my") || c4a.equals("ne") || c4a.equals("ta") || c4a.equals("th") || c4a.equals("vi"))  { 
    		// asian : Tibetan, Hindi, Mongolian, Burmese, Nepali, Tamil, Thai, Vietnamese
    		code = 11;
    	} else if (c4a.equals("he") || c4a.equals("fa") || c4a.equals("ur") || c4a.equals("yi") || c4a.equals("ar"))  { // arabic : Hebrew, Persian, Urdu, Yiddish ALSO arabic NOT Turkish
    		code = 12;
		}
    	coderes.calcDanishCode = code;
	}
	if (coderes.calcDanishCode != 0) coderes.intDanish = 1/100; 
	return coderes;
}

/*public static MysqlRes.CodesResult setcodes_mail(String c1a)  {
	MysqlRes.CodesResult coderes = new MysqlRes.CodesResult();
	if (c1a!=null) { 
    	if (!c1a.trim().startsWith("0"))  { // Contains dk mail address 
    		coderes.calcDanishCode = 5;
    		coderes.intDanish = 85/100;
    	}
	}
	return coderes;
}*/

public static CodesResult setcodes_WRONGphone(String c2a)  {
	CodesResult coderes = new CodesResult();
	if (c2a!=null) { 
    	if (!c2a.contains("tlf"))  { // Contains tlf as minimum (45 not enough on its own) 
    		coderes.calcDanishCode = 6;
    		if (!c2a.contains("+45"))  {
    			coderes.intDanish = 98/100;
    		} else if (!c2a.contains("0045"))  {
    			coderes.intDanish = 94/100;
    		} else {
    			coderes.intDanish = 90/100;
    		}
    	}
	}
	return coderes;
}

/*public static MysqlRes.CodesResult setcodes_languageDkletters(String c3a, String c3c, String c5a, String c5b)  {
	MysqlRes.CodesResult coderes = new MysqlRes.CodesResult();
	if ((c3a!=null || c3c!=null) && c5a!=null) {
		if ((!c5a.isEmpty()) && (!c5a.startsWith("0")) && (!c5a.equals("1 af"))) {
			int x = 0;
			if (!((c5b==null) || (c5b.isEmpty()) || (c5b.startsWith("0")))) x++;
			if (c3a!=null && (!c3a.isEmpty()) && (!c3a.startsWith("0"))) { 
    			if (c3c!=null && (!c3c.isEmpty()) && (!c3c.startsWith("0"))) {
	    			coderes.calcDanishCode = 70 + x;
		    		coderes.intDanish = (95-2*x)/100;
    			} else {
	    			coderes.calcDanishCode = 72 + x;
		    		coderes.intDanish = (90-2*x)/100;
    			}
			} /*else {
    			if (c3c!=null && (!c3c.isEmpty()) && (!c3c.startsWith("0"))) {
	    			coderes.calcDanishCode = 74 + x;
		    		coderes.intDanish = (90-2*x)/100;
    			} 
    		} *
    	}
	}
	return coderes;
}*/


/*public static MysqlRes.CodesResult setcodes_languageDk(String c4a, String c5a, String c5b)  {
	//res.calcDanishCode = 76-79  likely dk language (not norwegain)
	MysqlRes.CodesResult coderes = new MysqlRes.CodesResult();
	if (c4a!=null && c5a!=null) {
    	if (c4a.equals("da") || c4a.equals("no") || c4a.equals("sv")) {  
    		if ((!c5a.isEmpty()) && (!c5a.startsWith("0")) && (!c5a.equals("1 af"))) {
    			int x = 0;
    			if (!((c5b==null) || (c5b.isEmpty()) || (c5b.startsWith("0")))) x++;
    			if (c4a.equals("sv")) { 
	    			coderes.calcDanishCode = 78 + x;
		    		coderes.intDanish = 90-2*x/100;
    			} else { // da or no
	    			coderes.calcDanishCode = 76 + x;
		    		coderes.intDanish = 95-2*x/100;
	    		}
    		}
    	}
	}
	return coderes;
} */

public static CodesResult setcodes_smallSize(String c4a, String c3a, String c3b, String c3c, String c3d, String c6a, String c6b, String c6c)  {
	//check with codes from http://www.loc.gov/standards/iso639-2/php/code_list.php
	CodesResult coderes = new CodesResult();
	int code = 100;

    boolean inclC4a = (c4a!=null);
	inclC4a = inclC4a  && (c4a.equals("da") || c4a.equals("no") || c4a.equals("sv"));     // scandinavian languge
    boolean inclC3abcd = (c3a==null ? false : (!c3a.startsWith("0"))); // æøå 
	inclC3abcd = inclC3abcd || (c3b==null ? false : (!c3b.startsWith("0"))) ;
	inclC3abcd = inclC3abcd || (c3c==null ? false : (!c3c.startsWith("0"))) ;
	inclC3abcd = inclC3abcd || (c3d==null ? false : (!c3d.startsWith("0"))) ;
	boolean inclC6abc = (c6a==null ? false : (!c6a.startsWith("0"))) ;	 // typical dk words
	inclC6abc = inclC6abc || (c6b==null ? false : (!c6b.startsWith("0"))) ;
	inclC6abc = inclC6abc || (c6c==null ? false : (!c6c.startsWith("0"))) ;

	code = code + (inclC4a ? 4 : 0);
	code = code + (inclC3abcd ? 2 : 0);
	code = code + (inclC6abc ? 1 : 0);
	coderes.calcDanishCode = code;
	coderes.intDanish = (1 + (inclC4a ? 1 : 0) + (inclC3abcd ? 1 : 0) + (inclC6abc ? 1 : 0) )/100;
	return coderes;
}

public static int randomFromInterval(int startInterval, int endInterval) {
    Random rn = new Random();
    int range = endInterval - startInterval + 1;
    return  startInterval + rn.nextInt(range);
}
public static int maxbit = 22;

public static String row_delim = "#";
public static String tablename_delim = ", ";
public static String statustext_delim = ",";



/////////////////////////////////////////////////////////////////////////////////////////////////
// All code beneath this line is probably to be ignored
/////////////////////////////////////////////////////////////////////////////////////////////////

public enum Level {
	intervals, //=0; //make lagkage where e.g. 20-27 is in 20 etc.
	positive,  //=1 make lagkage for all positive codes, and put 0 and negitive in o
	allcodes,  //=2 make lagkage for all codes
	danish, //=3;
	allStats, //=4; // make all above 
	none //=-1;// make none 
}

public static int getLevelId(Level level) {
	int id = -1;
	if(level==Level.allcodes) id = 2;
	else if(level==Level.positive) id = 1; 
	else if(level==Level.intervals) id = 0;
	else if(level==Level.danish) id = 3;
	else if(level==Level.allStats) id = 4; 
	else if(level==Level.none) id = -1;
	return id;
}

public static String getLevelName(Level level) {
	String s ="";
	if(level==Level.allcodes) s = "all calc-codes";
	else if(level==Level.positive) s = "positive calc-codes (rest 0)"; 
	else if(level==Level.intervals) s = "interval calcodes";
	else if(level==Level.allStats) s = "all types of statistics"; 
	else if(level==Level.danish) s = "danish/not-danish statistics"; 
	else if(level==Level.none) s = "none";
	return s;
}

public enum Display {
	onlyCodes,  //only display of codes
	noCodes,  //no display of codes
	inText,  //display codes as (<code>) in end of text
	separateText, //display codes and text separately
	allDisplays, // make all above 
	noDisplay // make none 
}

public static class Statistics{
	public Level level = Level.none;
	public boolean included = false;
	public boolean isumup = false;
	public boolean dksumup = false;
	public long totalurls = 0L;
	public long total0urls = 0L;
    Map <Integer,Long> countMap = new HashMap<Integer,Long>(); 
}

 	
public static class CheckResult { 
    long resfileLineCount=0L;
	Set<String>  missingSet = new HashSet<String>();
	Set<String>  warningSet = new HashSet<String>();
	Set<String>  checkSet = new HashSet<String>();
    long processedCount =0L;
    long skippedCount=0L;
    long ignoredOrUpdateCount=0L;
}

enum StatType{
	stat_kage,
	stat_freq,
	stat_status,
	stat_none,
}

public static int noDomainLevels =3; //3 senere

public static String tableimport_dir = "tableImportFinished";
public static String tableexport_dir = "tableExportFinished";
public static String stat_tableline_start = "Tables";
public static String partfile_prefix = "part-m-";
public static String statistics_dir = "stat";
public static String urlsfortest_dir = "urlsForTest";
public static String urlsfound_dir = "foundUrls";
public static String urlsberk_dir = "berk";
public static String urlsberkimport_dir = "import";
public static String urlsberkexport_dir = "export";
public static String urlsimport_dir = urlsfound_dir + "/"+ urlsberk_dir + "/"+ urlsberkimport_dir;
public static String urlsexport_dir = urlsfound_dir + "/"+ urlsberk_dir + "/"+ urlsberkexport_dir;
public static String seeds_dir = "seeds";
public static String seedfile1 = "allseeds-job1og2.txt";
public static String seedfile2 = "allseeds-job3.txt";
public static String seedfile_lc1 = "allseeds-job1og2_lowercase.txt";
public static String seedfile_lc2 = "allseeds-job3_lowercase.txt";
public static String lagkageextract_dir = "kage";
public static String domainextract_dir = "domains";
public static String merge_dir_suffix = "_merge";
public static String frequenceextract_dir = "freq";
public static String statusextract_dir = "status";
public static String addstatusextract_dir = "addstatus";
public static String lagkage_fileprefix = "lagkage_";
public static String frequence_fileprefix = "frequence_";
public static String domain_fileprefix = "domain_";
public static String status_fileprefix = "status_";
public static String urlextract_fileprefix = "urls_";
public static String urlfind_fileprefix = "urlfound_";
public static String urlextract_title_start = "Urls for test of calcDanishCode ";
public static String urlextract_tableline_start = "Tables";

public enum CodesSizeIntervals{
	int_000000_005000,
	int_005000_010000,
	int_010000_015000,
	int_015000_020000,
	int_020000_025000,
	int_025000_030000,
	int_030000_035000,
	int_035000_040000,
	int_040000_100000,
	int_100000_200000,
	int_200000_
}
public static int codesSizeIntervals_count = CodesSizeIntervals.int_200000_.ordinal();

public static CodesSizeIntervals getSetItemCodesSizeIntervals(Set<CodesSizeIntervals> intsSet, int interval) {
    int i = 1;
    CodesSizeIntervals res = CodesSizeIntervals.int_000000_005000; //dummy
    for (CodesSizeIntervals e : intsSet) {
    	if (i == interval) {
    		res = e;
    	} else {
    		i++;
    	}
    }
    return res;
}


public static class Interval{
	public int start = 0;
	public int end = 0;
}

public enum CodesSizeIntervalsDetailed98112{
	int_000000_001500,
	int_001500_003000,
	int_003000_005000,
	int_005000_007000,
	int_007000_010000,
	int_010000_015000,
	int_015000_020000,
	int_020000_025000,
	int_025000_030000,
	int_030000_035000,
	int_035000_040000,
	int_040000_100000,
	int_100000_200000,
	int_200000_
}

public enum CodesSizeIntervalsDetailed0{
	int_000000_001000,
	int_001000_002000,
	int_002000_003000,
	int_003000_004000,
	int_004000_005000,
	int_005000_007000,
	int_007000_008500,
	int_008500_010000,
	int_010000_015000,
	int_015000_020000,
	int_020000_025000,
	int_025000_030000,
	int_030000_035000,
	int_035000_040000,
	int_040000_100000,
	int_100000_200000,
	int_200000_
}

public static Interval getIntervalNormal(CodesSizeIntervals i) {
	Interval res = new Interval();
	switch (i) {
	  case int_000000_005000:
		  res.start = 0;
		  res.end = 5000;
		  break;
	  case int_005000_010000:
		  res.start = 5000;
		  res.end = 10000;
		  break;
	  case int_010000_015000:
		  res.start = 10000;
		  res.end = 15000;
		  break;
	  case int_015000_020000:
		  res.start = 15000;
		  res.end = 20000;
		  break;
	  case int_020000_025000:
		  res.start = 20000;
		  res.end = 25000;
		  break;
	  case int_025000_030000:
		  res.start = 25000;
		  res.end = 30000;
		  break;
	  case int_030000_035000:
		  res.start = 30000;
		  res.end = 35000;
		  break;
	  case int_035000_040000:
		  res.start = 35000;
		  res.end = 40000;
		  break;
	  case int_040000_100000:
		  res.start = 40000;
		  res.end = 100000;
		  break;
	  case int_100000_200000:
		  res.start = 100000;
		  res.end = 200000;
		  break;
	  case int_200000_:
		  res.start = 200000;
		  res.end = 0;
	}
	return res;
}

public static Interval getIntervalDetailed0(CodesSizeIntervalsDetailed0 i) {
	Interval res = new Interval();
	switch (i) {
	  case int_000000_001000:
		  res.start = 0;
		  res.end = 1000;
		  break;
	  case int_001000_002000:
		  res.start = 1000;
		  res.end = 3000;
		  break;
	  case int_002000_003000:
		  res.start = 1000;
		  res.end = 3000;
		  break;
	  case int_003000_004000:
		  res.start = 3000;
		  res.end = 4000;
		  break;
	  case int_004000_005000:
		  res.start = 4000;
		  res.end = 5000;
		  break;
	  case int_005000_007000:
		  res.start = 5000;
		  res.end = 7000;
		  break;
	  case int_007000_008500:
		  res.start = 7000;
		  res.end = 8500;
		  break;
	  case int_008500_010000:
		  res.start = 8500;
		  res.end = 10000;
		  break;
	  case int_010000_015000:
		  res.start = 10000;
		  res.end = 15000;
		  break;
	  case int_015000_020000:
		  res.start = 15000;
		  res.end = 20000;
		  break;
	  case int_020000_025000:
		  res.start = 20000;
		  res.end = 25000;
		  break;
	  case int_025000_030000:
		  res.start = 25000;
		  res.end = 30000;
		  break;
	  case int_030000_035000:
		  res.start = 30000;
		  res.end = 35000;
		  break;
	  case int_035000_040000:
		  res.start = 35000;
		  res.end = 40000;
		  break;
	  case int_040000_100000:
		  res.start = 40000;
		  res.end = 100000;
		  break;
	  case int_100000_200000:
		  res.start = 100000;
		  res.end = 200000;
		  break;
	  case int_200000_:
		  res.start = 200000;
		  res.end = 0;
	}
	return res;
}

public static Interval getIntervalDetailed98112(CodesSizeIntervalsDetailed98112 i) {
	Interval res = new Interval();
	switch (i) {
	  case int_000000_001500:
		  res.start = 0;
		  res.end = 1500;
		  break;
	  case int_001500_003000:
		  res.start = 1500;
		  res.end = 3000;
		  break;
	  case int_003000_005000:
		  res.start = 3000;
		  res.end = 5000;
		  break;
	  case int_005000_007000:
		  res.start = 5000;
		  res.end = 7000;
		  break;
	  case int_007000_010000:
		  res.start = 7000;
		  res.end = 8500;
		  break;
	  case int_010000_015000:
		  res.start = 10000;
		  res.end = 15000;
		  break;
	  case int_015000_020000:
		  res.start = 15000;
		  res.end = 20000;
		  break;
	  case int_020000_025000:
		  res.start = 20000;
		  res.end = 25000;
		  break;
	  case int_025000_030000:
		  res.start = 25000;
		  res.end = 30000;
		  break;
	  case int_030000_035000:
		  res.start = 30000;
		  res.end = 35000;
		  break;
	  case int_035000_040000:
		  res.start = 35000;
		  res.end = 40000;
		  break;
	  case int_040000_100000:
		  res.start = 40000;
		  res.end = 100000;
		  break;
	  case int_100000_200000:
		  res.start = 100000;
		  res.end = 200000;
		  break;
	  case int_200000_:
		  res.start = 200000;
		  res.end = 0;
	}
	return res;
}

public static String getWhereInterval(Interval i) {
	String w = "";
	if (i.start>0) {
		w = "extSize >= " + i.start;
	}
	if (i.end>0) {
		w = (!w.isEmpty()? w + " AND " : "") + "extSize < " + i.end;
	}
	return w;
}

}
