package dk.kb.webdanica.oldtools;

import static org.grep4j.core.Grep4j.constantExpression;
import static org.grep4j.core.Grep4j.grep;
import static org.grep4j.core.fluent.Dictionary.on;
//import static org.grep4j.core.options.Option.onlyMatching;
//import static org.grep4j.core.options.Option.ignoreCase;
//import static org.grep4j.core.fluent.Dictionary.options;
//import static org.grep4j.core.fluent.Dictionary.with;


import java.sql.SQLException;
import java.util.*;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Random;

import org.grep4j.core.model.Profile;
import org.grep4j.core.model.ProfileBuilder;
import org.grep4j.core.result.GrepResults;

import dk.kb.webdanica.criteria.Words;
import dk.kb.webdanica.oldtools.MysqlRes.CodesResult;
import dk.kb.webdanica.oldtools.MysqlWorkFlow.HadoopResItem;
import dk.kb.webdanica.utils.TextUtils;


/* functions and structures share by Mysql classes */

public class MysqlX {

    /**
     * @param args None
     * @throws ClassNotFoundException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     * @throws SQLException 
     * @throws IOException 
     */
    
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
    	MysqlRes.CodesResult cr = new MysqlRes.CodesResult();
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

    public static MysqlRes.CodesResult setcodes_languageDkNew(String C4a, String C5a, String C5b, String C15b)  {
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
    
	public static int findNegativBitmapCalcCode(MysqlRes.SingleCriteriaResult res) {
    	int code = 0;
    	boolean setbit = false;
    	
        setbit = ((res.C1a!=null) && (res.C1a.length()>2)); // include country’s TLD email address 
        setbit = setbit || ((res.C2a!=null) &&  (res.C2a.length()>2)); // include national phone number 
    	if (setbit) code = setBit(1, code); //ph or mail

        setbit = ((res.C3a!=null) && (res.C3a.length()>2));  
        setbit = setbit || ((res.C3c!=null) &&  (res.C3c.length()>2)); 
    	if (setbit) code = setBit(2, code); //æøå in html or url

        if (res.C4a!=null) {
        	setbit = (res.C4a.equals("da") || res.C4a.equals("no") || res.C4a.equals("sv"));
        	if (setbit) code = setBit(3, code); //language like danish
        }

    	setbit = ((res.C6b!=null) && (res.C6b.length()>2));  
        setbit = setbit || ((res.C6c!=null) &&  (res.C6c.length()>2)); 
    	if (setbit) code = setBit(4, code); //frequently used selected Danish words

    	setbit = ((res.C7a!=null) && (res.C7a.length()>2));  
        setbit = setbit || ((res.C7b!=null) &&  (res.C7b.length()>2)); 
        setbit = setbit || ((res.C7e!=null) &&  (res.C7e.length()>2)); 
        setbit = setbit || ((res.C7f!=null) &&  (res.C7f.length()>2)); 
    	if (setbit) code =setBit(5, code); //largest Danish towns

        setbit = ((res.C9a!=null) &&  (res.C9a.length()>2)); 
        setbit = setbit || ((res.C9d!=null) &&  (res.C9d.length()>2)); 
    	if (setbit) code = setBit(6, code); //A/S… or CVR
        if (res.C15a!=null) {
        	setbit = (res.C15a.equals("y"));
        	if (setbit) code = setBit(7, code); //neighboring countries
        }
        setbit = ((res.C16a!=null) && (!res.C16a.isEmpty()) && (Long.parseLong(res.C16a) > 0L)); 
        setbit =  setbit || ((res.C17a!=null) && (!res.C17a.isEmpty()) &&  (Long.parseLong(res.C17a) > 0L)); 
    	if (setbit) code = setBit(8, code); //links

        setbit = ((res.C3b!=null) &&  (res.C3b.length()>2)); 
        setbit = setbit || ((res.C3d!=null) &&  (res.C3d.length()>2)); 
    	if (setbit) code = setBit(9, code); //words including ae, oe, aa in html or url

        setbit = (res.C6d!=null) && ( (!res.C6d.startsWith("0")) && (!res.C6d.isEmpty()) ); //setbit = ((res.C6a!=null) &&  (res.C6a.length()>2));
    	if (setbit) code = setBit(10, code); //frequently used Danish words NEW

        setbit = ((res.C7c!=null) &&  (res.C7c.length()>2)); 
        setbit = setbit || ((res.C7d!=null) &&  (res.C7d.length()>2)); 
    	if (setbit) code = setBit(11, code); //suffixes in town in html and url

        setbit = ((res.C8a!=null) &&  (res.C8a.length()>2)); 
        setbit = setbit || ((res.C8b!=null) &&  (res.C8b.length()>2)); 
    	if (setbit) code = setBit(12, code); //union and asscociation in htm and url

        setbit = ((res.C9b!=null) &&  (res.C9b.length()>2)); 
        setbit = setbit || ((res.C9c!=null) &&  (res.C9c.length()>2)); 
    	if (setbit) code = setBit(13, code); //company names in htm and url

        setbit = ((res.C10c!=null) &&  (!res.C10c.startsWith("0")) && (!res.C10c.isEmpty())); //((res.C10a!=null) &&  (res.C10a.length()>2)) || ((res.C10b!=null) &&  (res.C10b.length()>2)); 
    	if (setbit) code = setBit(14, code); //danish surnames NEW

        setbit = ((res.Cext1!=null) &&  (res.Cext1>250)); //size is considrable for language check
        if (setbit) code = setBit(15, code);  //

    	// changed from 1/8 2014 now code is int     	
        setbit = ((res.C10a!=null) &&  (res.C10a.length()>2)); 
        if (setbit) code = setBit(16, code);  //
        
        //NEW  new C8c[1]<>0 | C9e[1]<>0 (as 8a,9a)" : "new limmited union or companies
        setbit = (res.C8c!=null) && ( (!res.C8c.startsWith("0")) && (!res.C8c.isEmpty()) ); 
        setbit = setbit || (res.C9e!=null) && ( (!res.C9e.startsWith("0")) && (!res.C9e.isEmpty()) ); 
        if (setbit) code = setBit(17, code);
        
        //NEW  C7g[1]<>0 | C7h[1]<>0 (as 7e,7a)" : "new limmited largest dk towns (incl. translations)"); //Reset 
        setbit = (res.C7g!=null) && ( (!res.C7g.startsWith("0")) && (!res.C7g.isEmpty()) ); 
        setbit = setbit || (res.C7h!=null) && ( (!res.C7h.startsWith("0")) && (!res.C7h.isEmpty()) ); 
        if (setbit) code = setBit(18, code);  //

        //Cext2>=150 (<200), likely chinese or the like         
        setbit = ((res.Cext2!=null) &&  (res.Cext2>=150)); 
        if (setbit) code = setBit(19, code);  //

        setbit = (res.C3e!=null) && ( (!res.C3e.startsWith("0")) && (!res.C3e.isEmpty()) ); 
        setbit = setbit || ( (res.C3f!=null) && ( (!res.C3f.startsWith("0")) && (!res.C3f.isEmpty()) ) ); 
        if (setbit) code = setBit(20, code);  //
        
        setbit = (res.C3g!=null) && ( (!res.C3g.startsWith("0")) && (!res.C3g.isEmpty()) ); 
        if (setbit) code = setBit(21, code);  //
        
        setbit = (res.C2b!=null) && ( res.C2b.equals("y") ); 
        setbit = setbit || (res.C9f!=null) && ( res.C9f.equals("y") ); 
        if (setbit) code = setBit(22, code);  //

        return code;
    }
    
    public static boolean setcodes_dkLanguageVeryLikely(MysqlRes.SingleCriteriaResult res)  {
    	boolean ok = true;
    	
    	boolean bigSize = (res.Cext1>250); //40 for size 200-250 -
	    int interval = (bigSize ? 20 : 40);

    	boolean inclTld = (res.C15a!=null);
    	inclTld = inclTld && (res.C15a.equals("y"));  // The URL belongs to a TLD often used by Danes
    	boolean inclToLinks = (res.C16a!=null);
    	inclToLinks = inclToLinks && (Long.parseLong(res.C16a)>0L);  // There are .dk sites that points to the webpage
    	boolean inclFromLinks = (res.C17a!=null);
    	inclFromLinks = inclFromLinks && (Long.parseLong(res.C17a)>0L);  // <The webpage points to other .dk sites>
    	int calcCode = interval + (inclTld ? 4 : 0) + (inclToLinks ? 2 : 0) + (inclFromLinks ? 1 : 0);
        int startsize = (interval == 20 ? 250 : 200);
        int endsize = (interval == 20 ?   0 : 250);

        ok = (res.C3a!=null && res.C4a!=null && res.C5a!=null && res.C5b!=null && res.C6a!=null); 
        if (ok) {
        	ok = res.Cext1 > startsize //only consider when there are lots of text for n-gram
	    		&& (endsize==0 ? true : (res.Cext1 <= endsize) )
				&& res.C3a.length()>2 //includes æ,ø or å
				&& res.C4a.equals("da")
				&& res.C5a.length() >2 //includes typical and distingisable Danish words
				&& res.C5b.startsWith("0") //do not include typical Norwegain words
				&& res.C6a.length()>2 //includes frequently used Danish words"
				&& res.C15a.equals(inclTld ? "y":"n") //The URL belongs to a TLD often used by Danes
				&& (inclToLinks? Long.parseLong(res.C16a)>0 : Long.parseLong(res.C16a)==0) //There are .dk sites that points to the webpage 
				&& (inclFromLinks? Long.parseLong(res.C17a)>0 : Long.parseLong(res.C17a)==0); 
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
            String[] c3aParts = res.C3a.split(" ");
            float c3aFactor=1;
            if (c3aParts.length != 2) {
            	System.out.println("WARNING: wrong C3a value: " + res.C3a);
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
            String[] c6aParts = res.C6a.split(" ");
            float c6aFactor=1;
            if (c6aParts.length != 2) {
            	System.out.println("WARNING: wrong C6a value: " + res.C6a);
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
    
    enum LookupLevel {
    	all,l1, l2, l1_l2, l3
    }

	public static boolean skipLevel (LookupLevel pLevel, int level) {
    	boolean skip = (!pLevel.equals(LookupLevel.all));
    	if ( skip && (pLevel.equals(LookupLevel.l1) && level==1) ) skip = false;
    	if ( skip && (pLevel.equals(LookupLevel.l2) && level==2) ) skip = false;
    	if ( skip && (pLevel.equals(LookupLevel.l3) && level==3) ) skip = false;
    	if ( skip && (pLevel.equals(LookupLevel.l1_l2) && (level==1 || level==2) ) ) skip = false;
		return skip;
	}
	public static boolean inclExtract (LookupLevel pLevel, int level) {
    	boolean incl = (level<=MysqlX.noDomainLevels) && pLevel.equals(LookupLevel.all);
    	incl = incl || ( pLevel.equals(LookupLevel.l1) && level==1 );
    	incl = incl || ( pLevel.equals(LookupLevel.l2) && (level==1 || level==2) );
    	incl = incl || ( pLevel.equals(LookupLevel.l1_l2) && (level==1 || level==2) );
    	incl = incl || ( pLevel.equals(LookupLevel.l3) && (level==1 || level==2 || level==3) );
		return incl;
	}

	enum NotDkExceptions{
    	noException,
    	unions,
    	companies,
    }

    public static CodesResult setcodes_notDkLanguageVeryLikelyNewFields(MysqlRes.SingleCriteriaResult res, NotDkExceptions e)  {
    	CodesResult cr = new CodesResult(); 
    	
        boolean bigSize = (res.Cext1>250); // 50 for size 200-250
    	boolean inclTld = (res.C15a!=null);
    	inclTld = inclTld && (res.C15a.equals("y"));     // The URL belongs to a TLD often used by Danes

    	boolean incl7g = (res.C7g!=null);
    	incl7g = incl7g && !res.C7g.startsWith("0");
    	incl7g = incl7g && !res.C7g.isEmpty();
    	
    	boolean incl7h = (res.C7g!=null);
    	incl7h = incl7h && !res.C7g.startsWith("0");
    	incl7h = incl7h && !res.C7g.isEmpty();

    	boolean ok = true;
        ok = ok && ((res.C1a==null) || (res.C1a.isEmpty()) || (res.C1a.startsWith("0"))); //Do NOT include country’s TLD email address 
        ok = ok && ((res.C2b==null) || (res.C2b.isEmpty()) || (res.C2b.startsWith("n"))); //Do NOT include national phone number 
        ok = ok && ((res.C3a==null) || (res.C3a.isEmpty()) || (res.C3a.startsWith("0"))); //Do NOT include æ,ø or å
        ok = ok && ((res.C3g==null) || (res.C3g.isEmpty()) || (res.C3g.startsWith("0"))); //Do NOT include frequently used Danish words with coded æ, ø, å on form ae, oe/o, aa 
        ok = ok && ((res.C4a==null) || (res.C4a.isEmpty()) || ((!res.C4a.equals("da")) && (!res.C4a.equals("no")) && (!res.C4a.equals("sv"))));	//n-gram does NOT points at Scandinavian language
        ok = ok && ((res.C5a==null) || (res.C5a.isEmpty()) || (res.C5a.startsWith("0"))); //do NOT includes typical and distingisable Danish words
        ok = ok && ((res.C7g==null) || (res.C7g.isEmpty()) || (res.C7g.startsWith("0"))); // NOT list of 45 largest Danish towns (http://wwwC4a=="dnavneudvalget.ku.dk/) 
        ok = ok && ((res.C7h==null) || (res.C7h.isEmpty()) || (res.C7h.startsWith("0"))); // NOT København (Copenhagen) and Danmark (Denmark) translated to English, German, French and other European languages as well as Turkish, Somali and Romanian.
        if (e!=NotDkExceptions.unions) {
	        ok = ok && ((res.C8c==null) || (res.C8c.isEmpty()) || (res.C8c.startsWith("0"))); // unions
        }
        if (e!=NotDkExceptions.companies) {
	        ok = ok && ((res.C9e==null) || (res.C9e.isEmpty()) || (res.C9e.startsWith("0"))); // companies
	        ok = ok && ((res.C9f==null) || (res.C9f.isEmpty()) || (res.C9f.startsWith("n"))); // companies
        }
        ok = ok && ((res.C10c==null) || (res.C10c.isEmpty()) || (res.C10c.startsWith("0"))); // NOT look for typical patterns in Danish surnames like names ending in 'sen' (for son)
        ok = ok && ((res.C16a==null) || (Long.parseLong(res.C16a)==0));   //The URL does NOT belong to a TLD often used by Danes
        ok = ok && ((res.C17a==null) || (Long.parseLong(res.C17a)==0));   //The URL does NOT belong to a TLD often used by Danes

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

    
    public static CodesResult setcodes_notDkLanguageVeryLikely(MysqlRes.SingleCriteriaResult res)  {
    	CodesResult cr = new CodesResult(); 

    	boolean bigSize = (res.Cext1>250); // 50 for size 200-250
	    int interval = (bigSize ? 30 : 50);
	    
    	boolean inclTld = (res.C15a!=null);
    	inclTld = inclTld && (res.C15a.equals("y"));     // The URL belongs to a TLD often used by Danes

    	boolean incl6a = (res.C6a!=null);
    	incl6a = incl6a && !res.C6a.startsWith("0");
    	incl6a = incl6a && !res.C6a.isEmpty();
    	//System.out.println("incl6a: " + incl6a + " - 6a: " + res.C6a);
    	
    	boolean incl7c = (res.C7c!=null);
    	incl7c = incl7c && !res.C7c.startsWith("0");
    	incl7c = incl7c && !res.C7c.isEmpty();
    	
    	boolean incl7d = (res.C7d!=null);
    	incl7d = incl7d && !res.C7d.startsWith("0");
    	incl7d = incl7d && !res.C7d.isEmpty();
    	
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
        ok = ok && ((res.C1a==null) || (res.C1a.isEmpty()) || (res.C1a.startsWith("0"))); //Do NOT include country’s TLD email address 
        ok = ok && ((res.C2a==null) || (res.C2a.isEmpty()) || (res.C2a.startsWith("0"))); //Do NOT include national phone number 
        ok = ok && ((res.C3a==null) || (res.C3a.isEmpty()) || (res.C3a.startsWith("0"))); //Do NOT include æ,ø or å
        ok = ok && ((res.C3b==null) || (res.C3b.isEmpty()) || (res.C3b.startsWith("0"))); //Do NOT include frequently used Danish words with coded æ, ø, å on form ae, oe/o, aa 
        ok = ok && ((res.C3c==null) || (res.C3c.isEmpty()) || (res.C3c.startsWith("0"))); //Do NOT include same as C3a, but on the URL in uft8 URL encoding
        ok = ok && ((res.C3d==null) || (res.C3d.isEmpty()) || (res.C3d.startsWith("0"))); //Do NOT include same as C3c, but on the URL in uft8 URL encoding
        ok = ok && ((res.C4a==null) || (res.C4a.isEmpty()) || ((!res.C4a.equals("da")) && (!res.C4a.equals("no")) && (!res.C4a.equals("sv"))));	//n-gram does NOT points at Scandinavian language
        ok = ok && ((res.C5a==null) || (res.C5a.isEmpty()) || (res.C5a.startsWith("0"))); //do NOT includes typical and distingisable Danish words
        ok = ok && ((res.C6b==null) || (res.C6b.isEmpty()) || (res.C6b.startsWith("0"))); // NOT typical Danish words like 'dansk', 'Danmark' and 'forening'
        ok = ok && ((res.C6c==null) || (res.C6c.isEmpty()) || (res.C6c.startsWith("0"))); // NOT same as C6b, but on the URL, plus typical Danish notions '/dk/' or '/da/' 
        ok = ok && ((res.C7a==null) || (res.C7a.isEmpty()) || (res.C7a.startsWith("0"))); // NOT list of 45 largest Danish towns (http://wwwC4a=="dnavneudvalget.ku.dk/) 
        ok = ok && ((res.C7b==null) || (res.C7b.isEmpty()) || (res.C7b.startsWith("0"))); // NOT same as C7a, but on the URL 
        ok = ok && ((res.C7e==null) || (res.C7e.isEmpty()) || (res.C7e.startsWith("0"))); // NOT København (Copenhagen) and Danmark (Denmark) translated to English, German, French and other European languages as well as Turkish, Somali and Romanian.
        ok = ok && ((res.C7f==null) || (res.C7f.isEmpty()) || (res.C7f.startsWith("0"))); // NOT same as C7e, but on the URL 
        ok = ok && ((res.C8a==null) || (res.C8a.isEmpty()) || (res.C8a.startsWith("0"))); // unions
        ok = ok && ((res.C8b==null) || (res.C8b.isEmpty()) || (res.C8b.startsWith("0"))); // unions
        ok = ok && ((res.C9a==null) || (res.C9a.isEmpty()) || (res.C9a.startsWith("0"))); // NOT same as C9b, but on the URL
        ok = ok && ((res.C9b==null) || (res.C9b.isEmpty()) || (res.C9b.startsWith("0"))); // companies
        ok = ok && ((res.C9c==null) || (res.C9c.isEmpty()) || (res.C9c.startsWith("0"))); // companies
        ok = ok && ((res.C9d==null) || (res.C9d.isEmpty()) || (res.C9d.startsWith("0"))); // NOT search for CVR + 8 digits for registered Danish company number 
        ok = ok && ((res.C10a==null) || (res.C10a.isEmpty()) || (res.C10a.startsWith("0"))); // NOT look for typical patterns in Danish surnames like names ending in 'sen' (for son)
        ok = ok && ((res.C10b==null) || (res.C10b.isEmpty()) || (res.C10b.startsWith("0"))); // NOT look in list of 150 frequently used Danish first names and surnames
        ok = ok && ((res.C15a==null) || (res.C15a.isEmpty()) || (res.C15a.equals(inclTld ? "y":"n")));   //The URL does NOT belong to a TLD often used by Danes
        ok = ok && ((res.C16a==null) || (Long.parseLong(res.C16a)==0));   //The URL does NOT belong to a TLD often used by Danes
        ok = ok && ((res.C17a==null) || (Long.parseLong(res.C17a)==0));   //The URL does NOT belong to a TLD often used by Danes

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
    
    public static MysqlRes.CodesResult setcodes_WRONGphone(String c2a)  {
    	MysqlRes.CodesResult coderes = new MysqlRes.CodesResult();
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

    public static String findC9eval(String c9b, String c9e)  {
    	String s = "";
    	if (c9b!=null && (!c9b.isEmpty() && !c9b.startsWith("0"))) {
            String[] comps9b = c9b.substring(1).trim().split(",");
            Set<String> compsLst9b = new HashSet<String>();
            compsLst9b.addAll(java.util.Arrays.asList(comps9b));
            List<String> words2 = Arrays.asList(Words.virksomheder_lowercased_2_words_Nov2);
            compsLst9b.retainAll(words2);
	        Set<String>  compsLst9e = new HashSet<String>();
        	if (c9e!=null && (!c9e.isEmpty() && !c9e.startsWith("0"))) {
                String[] comps9e = c9e.substring(1).trim().split(",");
                compsLst9e.addAll(java.util.Arrays.asList(comps9e));
                List<String> words1 = Arrays.asList(Words.virksomheder_lowercased_1_word_Nov3);
                compsLst9e.retainAll(words1);
        	}
        	compsLst9e.addAll(compsLst9b);
        	s = compsLst9e.size() + " " + TextUtils.conjoin("#", compsLst9e);
    	}
    	if (s.isEmpty() && !c9e.isEmpty()) s=c9e;
    	return s;
    }

    public static String findC8cval(String c8a, String c8c)  {
    	String s = "";
    	if (c8a!=null && (!c8a.isEmpty() && !c8a.startsWith("0"))) {
            String[] comps8a = c8a.substring(1).trim().split(",");
            Set<String> compsLst8a = new HashSet<String>();
            compsLst8a.addAll(java.util.Arrays.asList(comps8a));
            List<String> words2 = Arrays.asList(Words.foreninger_lowercased_2_words_Nov2);
            compsLst8a.retainAll(words2);
	        Set<String>  compsLst8c = new HashSet<String>();
        	if (c8c!=null && (!c8c.isEmpty() && !c8c.startsWith("0"))) {
                String[] comps8c = c8c.substring(1).trim().split(",");
                compsLst8c.addAll(java.util.Arrays.asList(comps8c));
                List<String> words1 = Arrays.asList(Words.foreninger_lowercased_1_word_Nov2);
                compsLst8c.retainAll(words1);
        	}
        	compsLst8c.addAll(compsLst8a);
        	s = compsLst8c.size() + " " + TextUtils.conjoin("#", compsLst8c);
    	}
    	if (s.isEmpty() && !c8c.isEmpty()) s=c8c;
    	return s;
    }

    public static String findC10cval(String c10c)  {
    	String s = "";
    	if (c10c!=null && (!c10c.isEmpty())) {
    		if (!c10c.startsWith("0")) {
	            Set<String> tokens = TextUtils.tokenizeText(c10c.substring(1).trim());
	            List<String> words = Arrays.asList(Words.DanishNamesNov3);
	            tokens.retainAll(words);
	            s = tokens.size() + " " + TextUtils.conjoin("#", tokens);
    		} else {
    			s=c10c;
    		}
        }
    	return s;
    }

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

    public static MysqlRes.CodesResult setcodes_smallSize(String c4a, String c3a, String c3b, String c3c, String c3d, String c6a, String c6b, String c6c)  {
		//check with codes from http://www.loc.gov/standards/iso639-2/php/code_list.php
    	MysqlRes.CodesResult coderes = new MysqlRes.CodesResult();
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
    
    public static String find8bVal(String url) {
        Set<String> foundMatches = computeC8b(url);
        String val = (foundMatches.size() > 0 
    		    		? (foundMatches.size() + " " + TextUtils.conjoin("#", foundMatches))
    		    		: "0");
        return val;
    }
    
    /*public static String findNew3Val(String previous) {
    	String val = "";
    	if (previous.startsWith("0")) {
    		val = "0";
    	} else {
            Set<String> foundMatches = computeNewC3x(previous);
            val = (foundMatches.size() > 0 
       		    		? (foundMatches.size() + " " + TextUtils.conjoin("#", foundMatches))
       		    		: "0");
    	}
        return val;
    }*/
    
    public static String findNew3ValToken(String previous) {
    	String val = "";
    	if (previous.startsWith("0")) {
    		val = "0";
    	} else {
	        Set<String> tokens = TextUtils.tokenizeText(previous);
	        Set<String> foundMatches = new HashSet<String>();
	        for (String word: tokens) {
	        	if(TextUtils.findMatches(word, Words.frequentwordsWithDanishLettersCodedNew).size()>0) {
	                foundMatches.add(word);
	            }
	        }
        	val = (foundMatches.size() > 0 
    		    		? (foundMatches.size() + " " + TextUtils.conjoin("#", foundMatches))
    		    		: "0");
    	}
        return val;
    }
    
    public static String findTLD(String url) {
        String[] parts0 = url.split(":");
        // Check http://www.medicasur.com.mx:8090/ - becomes .mx
        
        if (parts0.length <2) {
            //System.out.println("Error --- no ':' in url " + url); 
            return "";
        }
        
        String[] parts1 = parts0[1].split("/");
        String[] parts2 = parts0[1].split( "\\\\");
        String[] parts = (parts1.length > parts2.length ? parts1 : parts2);
        
        if (parts.length == 0) {
            //System.out.println("Error --- no parts in url " + url); 
            return "";
        }
        
        int i = -1;
        boolean found = false;
        boolean stop = false;
        while (!stop && !found) {
        	i++;
        	found = !parts[i].isEmpty();
        	stop = (i==parts.length-1);	
        }
        
        if (stop && !found) {
        	//System.out.println("stopped --- : " + url); 
            return "";
        }
        
        String s = parts[i];
        
        int tldbegin = s.lastIndexOf('.');
        if (tldbegin == -1) {
            //System.out.println("No TLD found: " + url );
            return "";
        }
        String tld = s.substring(tldbegin+1, s.length());
        
        return tld;
    }

    public   static Set<String> computeC8b(String text) {
        return TextUtils.SearchPattern(text, 
                Words.foreninger_lowercased);
    }    

    /*public   static Set<String> computeNewC3x(String text) {
        return TextUtils.SearchPattern(text, 
                Words.frequentwordsWithDanishLettersCodedNew);
    } */   

    public  static boolean getBoleanSetting(String string) {
        String[] parts = string.split("=");
        if (parts[1].equalsIgnoreCase("true")) {
            return true;
        }
        return false;
    }
    
    public  static String getStringSetting(String string) {
        String[] parts = string.split("=");
        if (parts.length>1) return parts[1];
        else return "";
    }

    public  static int unsetBit(int x, int calcDanishCode) {
        BigInteger v = new BigInteger( Integer.toString((int) (-1 * calcDanishCode)));
        v = v.clearBit(x-1);
        return (-1*v.intValue());
    } 

    public static int getBit(short x, int calcDanishCode) {
        BigInteger v = new BigInteger( Integer.toString((int) (-1 * calcDanishCode)));
        return (v.testBit(x-1)?1:0);
    }

    public static int setBit(int x, int calcDanishCode) {
        int v = -1* (int)calcDanishCode;
        v |= (1 << (x-1) );
        return (-1*v);
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

    public static String getStringSequence(Collection<String> strSet, String delim) {
    	String s = "";
        for (String str: strSet) {
        	s = (s.isEmpty() ? str : s + delim + str);
        }
    	return s;
    }

    public static String getIntegerSequence(Collection<Integer> intSet, String delim) {
    	String s = "";
        for (int i: intSet) {
        	s = (s.isEmpty() ? ""+i : s + delim + i);
        }
    	return s;
    }

    public static String getBooleanSequence(Collection<Boolean> boolSet, String delim) {
    	String s = "";
        for (Boolean b: boolSet) {
        	s = (s.isEmpty() ? "" : s + delim) + (b?"true":"false");
        }
    	return s;
    }

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
	        else if (bit==10) s = (viaFields? "new C6d[1]<>0 (as 6a)" : "new limmited freq. used dk word"); //(viaFields? "C6a[1]<>0" : "freq. used dk word");
	        else if (bit==11) s = (viaFields? "C7c[1]<>0 or C7d[1]<>0" : "suffixes in town");
	        else if (bit==12) s = (viaFields? "C8a[1]<>0 or C8b[1]<>0" : "union and asscociation");
	        else if (bit==13) s = (viaFields? "C9b[1]<>0 or C9c[1]<>0" : "company names");
	        else if (bit==14) s = (viaFields? "new C10c[1]<>0 (as 10b)" : "new limmited danish names"); //(viaFields? "C10a[1]<>0 or C10b[1]<>0" : "danish surnames & endings");
	        else if (bit==15) s = (viaFields? "Cext1>250" : "size>250");
	        else if (bit==16) s = (viaFields? "C10a[1]<>0" : "danish names (no endings)");
	        else if (bit==17) s = (viaFields? "new C8c[1]<>0 | C9e[1]<>0 (as 8a,9a)" : "new limmited union or companies"); //Reset
	        else if (bit==18) s = (viaFields? "new C7g[1]<>0 | C7h[1]<>0 (as 7e,7a)" : "new limmited largest dk towns (incl. translations)"); //Reset 
	        else if (bit==19) s = (viaFields? "Cext2>=120 (<200)" : "maybe chinese (Charsize>150)");
	        else if (bit==20) s = (viaFields? "C3e[1]<>0 (from 3b) and C3f[1]<>0  (from 3d)" : "limmited wordlist o,oe,ae,aa");
	        else if (bit==21) s = (viaFields? "new C3g[1]<>0 (as 3b)" : "new limmited wordlist o,oe,ae,aa");
	        else if (bit==22) s = (viaFields? "new C2b[1]='y' | C9f[1]='y'" : "ph. or cvr from reg. exp");
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
	        else if (code==5) s = "UDGÅET"; //(viaFields ? "C1a>0" : "dk mail address");
	        else if (code==6) s = "UDGÅET - WRONG ph.";
	        else if (code==7) s = "UDGÅET"; // (viaFields ? "C2a incl. +45 & tlf. + C5a>0 C5b=0" : "ph. and dk + NOT no. words");
	        else if (code==8) s = "UDGÅET"; // (viaFields ? "C2a incl. +45 & tlf. + C5a>0 C5b>0" : "ph. and dk + no.swords");
	        else if (code>=10 && code<=12) { //asian/arabic languages
	        	if (level==Level.intervals) s = (viaFields ? "C4a is arabic/asian language code" : "chinese/arabic");
	        	else if (code==10) s = (viaFields ? "C4a='zh'/'ja'/'ko'" : "chinese like languages");
	            else if (code==11) s = (viaFields ? "C4a = 'bo'/'hi'/'mn'/'my'/'ne'/'ta'/'th'/'vi'" : "asian languages");
	            else if (code==12) s = (viaFields ? "C4a='he'/'fa'/'ur'/'yi'/'ar'" : "arabic languages");
	        } else  if ((code>=20 && code<=27) || (code>=40 && code<=47)) { //many dk indications 
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
	            s = "dk language tikka";  //replaces 71, 72 - only html not URL
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
		            s = s + " - " + (inclc4a ? (viaFields?"C4a='da'/'no'/'sv'":"scandi language") : (viaFields?"NOT C4a='da'/'no'/'sv'":"NOT scandi language" ));
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
	        } else if (code==cat_ERROR_dk) {
	        	s = "error";
	        } else if (code==cat_ignored_dk) {
	        	s = "ignored";
	        } else if (code==cat_likely_dk) {
	        	s = "likely danish";
	        } else if (code==cat_maybes_dk) {
	        	s = "maybes";
	        } else if (code==cat_not_likely_dk) {
	        	s = "NOT likely danish";
	        } else if (code==cat_unknown_dk) {
	        	s = "undecided";
	    	} else if (code<0)  { //  codes for which fields are set (in xls doc)	=> calcDanishCode <0
	    		s = "";
	    		String txt = "";
	        	String seperator = "++";
	        	for (int b=1; b<=maxbit; b++) {
	        		txt = (MysqlX.getBit((short)b, code)==1 ? getBitCalcDkCodeText((short)b, codesOut, viaFields) : "");
	        		s = s + (s.isEmpty() || txt.isEmpty() ? txt : seperator  + txt);
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
    
    enum Level {
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

    enum Display {
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

    public static boolean isPartfile(String fn) {
		//names on form part_m_00132
		//              012345678901
    	return fn.startsWith(partfile_prefix);
	}

    public static int getPartno(String fn) {
		//names on form part_m_00132
		//              012345678901
    	if(fn.startsWith(partfile_prefix)) {
			String s = fn.substring(partfile_prefix.length());
    		if(fn.endsWith(".gz")) {
    			s = s.substring(0, s.length()-3);
    		}
			return Integer.parseInt(s);
    	} else {
			return 0;
    	}
	}

	public static File checkDir(String dirname) {
        File statDir = new File(dirname);
        if (!statDir.isDirectory()) {
            System.err.println("ERROR: Cannot find dir'" + statDir.getAbsolutePath() + "' as a proper directory");
            System.exit(1);
        }
        return statDir;
	}

	public static String NAS_infix = "NAS_";
	public static String IA_infix = "IA_";

	public static String getStatFileSuffix(HadoopResItem item, String seqno) {
    	String s = "M" + item.dbmachine 
    			+ "_V" + seqno 
    			+ "_T" + item.getname("_");
        return s;
    }

    public static boolean isNumeric(String s) {
     	return s.matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+");
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
		
	public enum DataSource{
    	source_IA,
    	source_NAS,
    	source_none
    }
    
    public enum Source{
    	IA,
    	NAS
    }
    
	public static int getSourceIndex(Source src) {
		int i = -1;
		switch (src) {
			case IA: i=0; break;
			case NAS: i=1; break;
		}
		return i;
	}
    
    
    public static String getSourceInfix(DataSource src) {
		String s = "";
		switch (src) {
			case source_IA: s=IA_infix; break;
			case source_NAS: s=NAS_infix; break;
			case source_none: s=""; break;
		}
		return s;
	}

    public static String getSourceInfix(Source src) {
		String s = "";
		switch (src) {
			case IA: s=IA_infix; break;
			case NAS: s=NAS_infix; break;
		}
		return s;
	}

    public static DataSource convertSource(Source src) {
    	DataSource s = DataSource.source_none;
		switch (src) {
			case IA: s = DataSource.source_IA; break;
			case NAS: s = DataSource.source_NAS; break;
		}
		return s;
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

	
	public static boolean isIAtablename(String tablename) {
		boolean ok = false;
		if (tablename.startsWith(MysqlRes.wf_table_prefix)) {
			HadoopResItem item = MysqlWorkFlow.readItemFromTablename(tablename, "", "", "");
			ok = item.machine_no < 100;
		}
		return ok;
	}

	public static boolean isNAStablename(String tablename) {
		boolean ok = false;
		if (tablename.startsWith(MysqlRes.wf_table_prefix)) {
			HadoopResItem item = MysqlWorkFlow.readItemFromTablename(tablename, "", "", "");
			ok = !isNumeric(item.diskTld_no) || (item.machine_no == 100);
		}
		return ok;
	}

    public static int  cat_ERROR_dk = 9000; //getCodesForUdgaaede
    public static int  cat_ignored_dk = 9100; //getCodesForFrasorterede
    public static int  cat_not_likely_dk = 9101; //getCodesForNOTDanishResults
    public static int  cat_unknown_dk = 9200;	//Not decided 0 and negative
    public static int  cat_maybes_dk = 9500;	//getCodesForMaybees
    public static int  cat_likely_dk = 9999; //getCodesForDanishResults

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

    public static String txtfile_suffix = ".txt";
    public static String domaintable_machine_infix = "_M";

    public static int findGreplinesInFile(File f, String nm, String domain) {
    	Profile allurls = ProfileBuilder.newBuilder()
                .name(nm)
                .filePath(f.getAbsolutePath())
                .onLocalhost()
                .build();
		System.out.println("*** searching domain " + domain + " in " + f.getName());
		GrepResults res = grep(constantExpression(domain), on(allurls));
		return res.totalLines();
	}
        
   /* too slow 
    * public static int findCaseSensitiveGreplinesInFile(File f, String nm, String domain) {
    	Profile allurls = ProfileBuilder.newBuilder()
                .name(nm)
                .filePath(f.getAbsolutePath())
                .onLocalhost()
                .build();
		System.out.println("*** searching domain " + domain + " in " + f.getName());
		GrepResults res = 
				grep(constantExpression(domain), 
						on(allurls), 
						with(
			                options(
			                        onlyMatching(),
			                        ignoreCase()
			                        )
			                )
                );
		return res.totalLines();
	}
    */
}
