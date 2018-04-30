package dk.kb.webdanica.core.datamodel.criteria;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import dk.kb.webdanica.core.criteria.FrequentWords;
import dk.kb.webdanica.core.datamodel.DanicaStatus;
import dk.kb.webdanica.core.datamodel.Seed;
import dk.kb.webdanica.core.datamodel.Status;
import dk.kb.webdanica.core.utils.SystemUtils;
import dk.kb.webdanica.core.utils.TextUtils;

public class Classification {
    
    /**
     * Decide Danicastatus for seed based on SingleCriteriaResult.
     * This method sets the danicastatus to YES for the seed if the IsLikelyDanica criteria holds, and Status to DONE.
     * This method sets the danicastatus to NO for the seed if the IsLikelyNotDanica criteria holds, and Status to DONE.
     * Otherwise this method sets the danicastatus to UNDECIDED for the seed, and Status to AWAITS_CURATOR_DECISION. 
     *  
     * @param res SingleCriteriaResult for the given Seed 
     * @param s The Seed object for which the given SingleCriteriaResult is calculated   
     * @param rejectIfNotExplicitlyDanica Should seed be rejected if not explicitly found to be danica
     */
    public static void decideDanicaStatusFromResult(SingleCriteriaResult res, Seed s, boolean rejectIfNotExplicitlyDanica) {
        String statusReasonPrefix = "Harvested by harvest '"
                + res.harvestName
                + "' and now successfully analyzed. ";
        int danishcode = res.calcDanishCode;
        if (Codes.IsLikelyDanica(danishcode)) {
            s.setDanicaStatus(DanicaStatus.YES);
            s.setDanicaStatusReason("Set to DanicaStatus YES, because of danishcode '"
                    + danishcode
                    + "' belongs to the likelydk category");
            s.setStatus(Status.DONE);
            s.setStatusReason(statusReasonPrefix
                    + "Processing finished as we now consider this seed Danica");
        } else if (Codes.IsLikelyNotDanica(danishcode)) {
            s.setDanicaStatus(DanicaStatus.NO);
            s.setDanicaStatusReason("Set to DanicaStatus NO, because of danishcode '"
                    + danishcode
                    + "' belongs to the notlikelydk category");
            s.setStatus(Status.DONE);
            s.setStatusReason(statusReasonPrefix
                    + "Processing finished as we now consider this seed not-Danica");
        } else {
            if (rejectIfNotExplicitlyDanica) {
                s.setDanicaStatus(DanicaStatus.NO);
                s.setDanicaStatusReason("Rejected, as the danishcode '"
                        + danishcode
                        + "'doesn't give us any safe indications of danicastatus");
                s.setStatus(Status.DONE);
                s.setStatusReason(statusReasonPrefix
                        + "Processing finished as we now consider this seed not-Danica, because it is not explicitly found to be danica");
            } else {
                s.setDanicaStatus(DanicaStatus.UNDECIDED);
                s.setDanicaStatusReason("Still UNDECIDED, as the danishcode '"
                        + danishcode
                        + "'doesn't give us any safe indications of danicastatus");
                s.setStatus(Status.AWAITS_CURATOR_DECISION);
                s.setStatusReason(statusReasonPrefix
                        + "Now awaiting a curatorial decision");
            }
        }
    }
    
    /**
     * Some extra processing on the SingleCriteriaResult.
     * 
     * @param res a given SingleCriteriaResult
     * @deprecated "Not needed anymore, I think"
     */
    private static void someUpdateCode(SingleCriteriaResult res) {
        // update 3g
        update3g(res);
        // update 8c foreninger
        update8c(res);

        // update 9e firmaer on the basis on
        update9e(res);

        // update 10c
        String C10c = res.C.get("C10c");
        if (C10c != null && (!C10c.isEmpty() && !C10c.startsWith("0"))) {
            res.C.put("C10c", CriteriaUtils.findC10cval(C10c));
        }

        Set<Integer> codeSet = Codes.getCodesForNOTDanishResults();
        codeSet.addAll(Codes.getCodesForMaybees());
        /*** calculate C2b phone numbers ***/
        if (res.calcDanishCode <= 0 || codeSet.contains(res.calcDanishCode)) {
            CodesResult cr = CodesResult.setcodes_newPhone(res.C.get("C2b"),
                    res.C.get("C5a"), res.C.get("C5b"), res.C.get("C15b"));
            if (cr.calcDanishCode > 0) {
                res.calcDanishCode = cr.calcDanishCode;
                res.intDanish = cr.intDanish;
            }
        }

        /** town names */
        if (res.calcDanishCode <= 0 || codeSet.contains(res.calcDanishCode)) {
            String C7g = res.C.get("C7g");
            if (!((C7g == null) || (C7g.isEmpty()) || (C7g.startsWith("0")))) {
                CodesResult cr = CodesResult.setcodes_mail(res.C.get("C1a"),
                        res.C.get("C5a"), res.C.get("C5b"), res.C.get("C15b"),
                        C7g);
                if (cr.calcDanishCode > 0) {
                    res.calcDanishCode = cr.calcDanishCode;
                    res.intDanish = cr.intDanish;
                } else {
                    res.calcDanishCode = 230;
                    res.intDanish = 75 / 100;
                }
            }
        }

        for (CodesResult.NotDkExceptions ex : CodesResult.NotDkExceptions
                .values()) {
            if (res.calcDanishCode <= 0 || codeSet.contains(res.calcDanishCode)) {
                // get calcDanishCode and IntDanish
                // and check in depth
                CodesResult cr = CodesResult
                        .setcodes_notDkLanguageVeryLikelyNewFields(res, ex); 
                if (cr.calcDanishCode > 0) {
                    res.calcDanishCode = cr.calcDanishCode;
                    res.intDanish = cr.intDanish;
                }
            }
        }

        // update bits if already negativeBitmapCalcCode
        if (res.calcDanishCode <= 0) {
            res.calcDanishCode = CodesResult.findNegativBitmapCalcCode(res);
        }

    }
    
    /**
     * Update the value of criteria C9e using existing values of criteria C9b and C9e.   
     * @param res the given SingleCriteriaResult to update
     */
    private static void update9e(SingleCriteriaResult res) {
        String C9b = res.C.get("C9b");
        String C9e = res.C.get("C9e");
        if (C9b != null && (!C9b.isEmpty() && !C9b.startsWith("0"))) {
            res.C.put("C9e", CriteriaUtils.findC9eval(C9b, C9e));
        }

    }

    /**
     * Update the value of criteria C8c using existing values of criteria C8a and C8c.   
     * @param res the given SingleCriteriaResult to update
     */
    private static void update8c(SingleCriteriaResult res) {
        String C8a = res.C.get("C8a");
        String C8c = res.C.get("C8c");
        if (C8a != null && (!C8a.isEmpty() && !C8a.startsWith("0"))) {
            String oldC8c = C8c;
            C8c = CriteriaUtils.findC8cval(C8a, C8c);
            SystemUtils.log("Updating criteria C8c. Changed from '" + oldC8c + "' to '"
                    + C8c + "' using the C8a value '" + C8a + "'", Level.INFO, false);
            res.C.put("C8c", C8c);
        }

    }

    /**
     * Update the value of criteria C3g using existing values of criteria C3g.   
     * @param res the given SingleCriteriaResult to update
     */
    private static void update3g(SingleCriteriaResult res) {
        String C3g = res.C.get("C3g");
        if (C3g != null && (!C3g.isEmpty() && !C3g.startsWith("0"))) {
            String oldC3g = C3g;
            Set<String> tokens = TextUtils
                    .tokenizeText(C3g.substring(1).trim());
            List<String> words = Arrays
                    .asList(FrequentWords.frequentwordsWithDanishLettersCodedNew);
            tokens.retainAll(words);
            C3g = tokens.size() + " " + TextUtils.conjoin("#", tokens);
            SystemUtils.log("Updating criteria C3g. Changed from '" + oldC3g + "' to '"
                    + C3g + "'", Level.INFO, false);
            res.C.put("C3g", C3g);
        }

    }
    
    /**
     * This sets some extra criteria not included in the CombinedCombo.
     * See CalcDanishCode.getCalcDkCodeText for explanations.
     * @param res a SingleCriteriaResult
     * @param source The source of the result (currently not used)
     */
    protected static void analyzeRessource(SingleCriteriaResult res, DataSource source) {
        /*** set DataSource ***/
        res.source = source;
        
        /*** pre-calculate calcDanishCode and other fields. ***/
        res.calcDanishCode = 0;
        // res.calcDanishCode = 1 size=0
        if (res.Cext1 == 0) {
            res.calcDanishCode = 1; // no text
            return; // we stop the analysis now, as we believe the rest of the fields are empty
        }

        /*******************************************/
        /*** Update missing fields if necessary ***/
        /*******************************************/

        /*** calculate C15b ***/
        String tld = CriteriaUtils.findTLD(res.url);
        if (!tld.isEmpty()) {
            res.C.put("C15b", tld);
        } else {
            res.C.put("C15b", "-");
        }

        /*** calculate C8b if equal to 8a ***/
        if ((res.C.get("C8a") != null)) {
            if (res.C.get("C8b").equals(res.C.get("C8a"))) {
                res.C.put("C8b", CriteriaUtils.find8bVal(res.url));
            }
        }

        /*** calculate C3e restricted o/oe,ae, aa in html ***/
        if (res.C.get("C3b") != null) { // added 9/9
            res.C.put("C3e", CriteriaUtils.findNew3ValToken(res.C.get("C3b")));
        }

        /*** calculate C3f restricted o/oe,ae, aa in url ***/
        if (res.C.get("C3d") != null) { // added 9/9
            res.C.put("C3f", CriteriaUtils.findNew3ValToken(res.C.get("C3d")));
        }

        /*** END: Update missing fields ***/
        /**************************************/

        // Find a danishCode for the result

        if (res.C.get("C1a") != null) {
            CodesResult coderes = CodesResult.setcodes_mail(res.C.get("C1a"),
                    res.C.get("C5a"), res.C.get("C5b"), res.C.get("C15b"),
                    res.C.get("C7g"));
            if (coderes.calcDanishCode > 0) {
                res.calcDanishCode = coderes.calcDanishCode;
                res.intDanish = coderes.intDanish;
                return;
            }
        }

        // test c4a og c4b
        if (res.C.get("C4a") != null && res.C.get("C4a").equals("da")) {
            // look at the percentage in C4b
            String languagesFound = res.C.get("C4b");
            if (CalcDanishCode.checkForDanishCode4(res, languagesFound)) {
                return;
            }
        }
        // Rules supplied by Curators:
        checkCuratorSuppliedRules(res); // Check if curator-rules is enough to classify it as Danish
        if (res.calcDanishCode > 0) { 
            return;
        }
        // res.calcDanishCode =20-27, 40-47 - many dk indications
        if (res.C.get("C15a") != null && res.C.get("C16a") != null
                && res.C.get("C17a") != null && res.Cext1 > 200
                && res.C.get("C3a") != null && res.C.get("C4a") != null
                && res.C.get("C5a") != null && res.C.get("C5b") != null
                && res.C.get("C6a") != null) {
            CodesResult.setcodes_dkLanguageVeryLikely(res);
            if (res.calcDanishCode > 0) {
                return;
            }
        }

        if (res.C.get("C3a") != null) {
            CodesResult coderes = CodesResult.setcodes_languageDklettersNew(
                    res.C.get("C3a"), res.C.get("C5a"), res.C.get("C5b"),
                    res.C.get("C15b"));
            if (coderes.calcDanishCode > 0) {
                res.calcDanishCode = coderes.calcDanishCode;
                res.intDanish = coderes.intDanish;
                return;
            }
        }

        // res.calcDanishCode = 76-77 likely dk language (not norwegian)
        if (res.C.get("C4a") != null && res.C.get("C5a") != null) {
            CodesResult coderes = CodesResult.setcodes_languageDkNew(
                    res.C.get("C4a"), res.C.get("C5a"), res.C.get("C5b"),
                    res.C.get("C15b"));
            if (coderes.calcDanishCode > 0) {
                res.calcDanishCode = coderes.calcDanishCode;
                res.intDanish = coderes.intDanish;
                return;
            }
        }

        // find tlf an +45 to 315, 316, 317");
        if (res.C.get("C2a") != null) {
            CodesResult cr = CodesResult.setcodes_oldPhone(res.C.get("C2a"),
                    res.C.get("C5a"), res.C.get("C5b"), res.C.get("C15b"));
            if (cr.calcDanishCode > 0) {
                res.calcDanishCode = cr.calcDanishCode;
                res.intDanish = cr.intDanish;
                return;
            }
        }

        // res.calcDanishCode =100-107 small sizes
        if (res.Cext1 <= 200) {
            CodesResult coderes = CodesResult.setcodes_smallSize(
                    res.C.get("C4a"), res.C.get("C3a"), res.C.get("C3b"),
                    res.C.get("C3c"), res.C.get("C3d"), res.C.get("C6a"),
                    res.C.get("C6b"), res.C.get("C6c"));
            if (coderes.calcDanishCode > 0) {
                res.calcDanishCode = coderes.calcDanishCode;
                res.intDanish = coderes.intDanish;
                return;
            }
        }

        // res.calcDanishCode =10-12 asian/arabic languages
        if (res.C.get("C4a") != null) {
            CodesResult coderes = CodesResult
                    .setcodes_otherLanguagesChars(res.C.get("C4a"));
            if (coderes.calcDanishCode > 0) {
                res.calcDanishCode = coderes.calcDanishCode;
                res.intDanish = coderes.intDanish;
                return;
            }
        }

        // See CalcDanishCode.getCalcDkCodeText for explanations
        // get callcode and IntDanish and check in depth
        CodesResult cr = CodesResult.setcodes_notDkLanguageVeryLikely(res); 
        if (cr.calcDanishCode > 0) {
            res.calcDanishCode = cr.calcDanishCode;
            res.intDanish = cr.intDanish;
            return;
        }

        // res.calcDanishCode = 2: double-char (Cext2 >= 200)
        if (res.Cext2 >= 200) {
            res.calcDanishCode = 2; // lots of doublechars
            return;
        }
        // res.calcDanishCode = 220: double-char( 130 <= Cext2 < 200)
        if (res.Cext2 >= 130) {
            res.calcDanishCode = 220; // lots of doublechars
            return;
        }

        // res.calcDanishCode = 3
        if (res.C.get("C15b").equals("dk")) { // TLD-check
            res.calcDanishCode = 3; // I think we should test in more depth
            return;
        }
        
        
        
        // /////////////////////////////////
        // set calcDanishCode-codes for which fields are set
        res.calcDanishCode = CodesResult.findNegativBitmapCalcCode(res);
        return;
    }

    /**
     * Check the given Criteriaresult, if they match any of the curator given rules for Danica.
     * If any of the 15 rules match then
     * res.intDanish = 1;
     * res.calcDanishCode = SOMECODE; 
     * And the rest of the rules will not be checked
     * 
     * Note: rules for criteria C7b, C7c, C10a, and C10c are currently disabled 
     * 
     * @param res a given CriteriaResult
     * 
     * 400 C1A> 0 DK mail-adresses found   
     * 401 C2A> 0 DK tlf numbers found
     * 402 C6a: >20    hyppige danske ord
     * 403 C6b: >1 typiske danske ord 
     * (disabled) 404 C7b: >0 danske bynavne i URLen
     * (disabled) 405 C7c: >0 danske stednavne i teksten  Finder stadig delord
     * 406 C7e: >0 fremmedsprog af ordene København og Danmark
     * 407 C7g: >0 større danske byer i teksten
     * 408 C7h: >0 fremmedsprog af ordene København og Danmark
     * 409 C9e: >0 danske virksomheder
     * 410 C9d: >0 cvr      
     * 411 C9a: >0 a/s, aps
     * (disabled) 412 C10a: >2 finder -sen-navne i teksten.    Finder andre -sen-ord men den holder
     * (disabled) 413 C10c: >2 hyppige for- og efternavne  Finder stadig delord 
     * 414 C17a: >0 outlinks peger på websider i .dk
    */
    private static void checkCuratorSuppliedRules(SingleCriteriaResult res) {
        //C1a: >0 danske mail-adresser
        if (valueGreaterThan(res.C.get("C1a"), 0)) {
            res.intDanish = 1;
            res.calcDanishCode = 400;
            return;
        }
        //C2a: >0 tlf.                
        if (valueGreaterThan(res.C.get("C2a"), 0)) {
            res.intDanish = 1;
            res.calcDanishCode = 401;
            return;
        }
        //C6a: >20    hyppige danske ord
        if (valueGreaterThan(res.C.get("C6a"), 20)) {
            res.intDanish = 1;
            res.calcDanishCode = 402;
            return;
        }
        //C6b: >1 typiske danske ord  
        if (valueGreaterThan(res.C.get("C6b"), 1)) {
            res.intDanish = 1;
            res.calcDanishCode = 403;
            return;
        }
        //C7b: >0 danske bynavne i URLen
        /*if (valueGreaterThan(res.C.get("C7b"), 0)) {
            res.intDanish = 1;
            res.calcDanishCode = 404;
            return;
        }
        */
        
        //C7c: >0 danske stednavne i teksten  Finder stadig delord
        /*
        if (valueGreaterThan(res.C.get("C7c"), 0)) {
            res.intDanish = 1;
            res.calcDanishCode = 405;
            return;
        }
        */
        
        //C7e: >0 fremmedsprog af ordene København og Danmark
        if (valueGreaterThan(res.C.get("C7e"), 0)) {
            res.intDanish = 1;
            res.calcDanishCode = 406;
            return;
        }
        //C7g: >0 større danske byer i teksten
        if (valueGreaterThan(res.C.get("C7g"), 0)) {
            res.intDanish = 1;
            res.calcDanishCode = 407;
            return;
        }
        //C7h: >0 fremmedsprog af ordene København og Danmark
        if (valueGreaterThan(res.C.get("C7h"), 0)) {
            res.intDanish = 1;
            res.calcDanishCode = 408;
            return;
        }
        //C9e: >0 danske virksomheder 
        if (valueGreaterThan(res.C.get("C9e"), 0)) {
            res.intDanish = 1;
            res.calcDanishCode = 409;
            return;
        }
        //C9d: >0 cvr             
        if (valueGreaterThan(res.C.get("C9d"), 0)) {
            res.intDanish = 1;
            res.calcDanishCode = 410;
            return;
        }
        //C9a: >0 a/s, aps            
        if (valueGreaterThan(res.C.get("C9a"), 0)) {
            res.intDanish = 1;
            res.calcDanishCode = 411;
            return;
        }
        
        //C10a: >2    finder -sen-navne i teksten.    Finder andre -sen-ord men den holder
        /*
        if (valueGreaterThan(res.C.get("C10a"), 2)) {
            res.intDanish = 1;
            res.calcDanishCode = 412;
            return;
        } 
        */
        
        //C10c: >2    hyppige for- og efternavne  Finder stadig delord
        /*
        if (valueGreaterThan(res.C.get("C10c"), 2)) {
            res.intDanish = 1;
            res.calcDanishCode = 413;
            return;
        }
        */
        
        //C17a: >0    outlinks peger på websider i .dk
        if (valueGreaterThan(res.C.get("C17a"), 0)) {
            res.intDanish = 1;
            res.calcDanishCode = 414;
            return;
        }
    }
    /**
     * Helper method to test the resultValue of a given criteria.
     * 
     * @param criteriaValue The String value of some criteria
     * @param i a given integer
     * @return true, if the intvalue of the criteriaValue String is greater than argument i. Returns false, if criteriaValue is null, emptyvalue or its intvalue is not greater than argument i
     */
    private static boolean valueGreaterThan(String criteriaValue, int i) {
        if (criteriaValue == null || criteriaValue.isEmpty()) {
            return false;
        }
        int intValue=i;
        try {
            intValue = Integer.parseInt(criteriaValue);
        } catch (NumberFormatException e) {
            // Ignore for the time being
        }
        return intValue > i;
    }
}
