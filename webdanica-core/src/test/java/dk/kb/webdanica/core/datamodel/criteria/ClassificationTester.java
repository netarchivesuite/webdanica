package dk.kb.webdanica.core.datamodel.criteria;

import static org.junit.Assert.*;

import org.junit.Test;

import dk.kb.webdanica.core.datamodel.DanicaStatus;
import dk.kb.webdanica.core.datamodel.Seed;

public class ClassificationTester {

    public static int LIKELY_DANISH_CODE = 400;
    public static int LIKELY_NOT_DANISH_CODE = 317;
    public static int MAYBE_DANISH_CODE = 230;
    
    @Test
    public void testRejectIfNotExplicitlyDanica() {
       SingleCriteriaResult res = new SingleCriteriaResult();
       Seed s = new Seed("http://netarkivet.dk");
       // test LIKELY_DANISH_CODE case
       res.calcDanishCode = LIKELY_DANISH_CODE;
       boolean rejectIfNotExplicitlyDanica = true;
       Classification.decideDanicaStatusFromResult(res, s,  rejectIfNotExplicitlyDanica);
       assertTrue("Should be danicastatus YES, but was " + s.getDanicaStatus(), 
               s.getDanicaStatus().equals(DanicaStatus.YES));
       rejectIfNotExplicitlyDanica = false;
       Classification.decideDanicaStatusFromResult(res, s,  rejectIfNotExplicitlyDanica);
       assertTrue("Should be danicastatus YES, but was " + s.getDanicaStatus(), 
               s.getDanicaStatus().equals(DanicaStatus.YES));
     
       // Test LIKELY_NOT_DANISH_CODE case
       res.calcDanishCode = LIKELY_NOT_DANISH_CODE;
       rejectIfNotExplicitlyDanica = true;
       Classification.decideDanicaStatusFromResult(res, s,  rejectIfNotExplicitlyDanica);
       assertTrue("Should be danicastatus NO, but was " + s.getDanicaStatus(), 
               s.getDanicaStatus().equals(DanicaStatus.NO));
       rejectIfNotExplicitlyDanica = false;
       Classification.decideDanicaStatusFromResult(res, s,  rejectIfNotExplicitlyDanica);
       assertTrue("Should be danicastatus NO, but was " + s.getDanicaStatus(), 
               s.getDanicaStatus().equals(DanicaStatus.NO));
     
       // Test  MAYBE_DANISH_CODE case
       res.calcDanishCode = MAYBE_DANISH_CODE;
       rejectIfNotExplicitlyDanica = true;
       Classification.decideDanicaStatusFromResult(res, s,  rejectIfNotExplicitlyDanica);
       assertTrue("Should be danicastatus NO, but was " + s.getDanicaStatus(), 
               s.getDanicaStatus().equals(DanicaStatus.NO));
       rejectIfNotExplicitlyDanica = false;
       Classification.decideDanicaStatusFromResult(res, s,  rejectIfNotExplicitlyDanica);
       assertTrue("Should be danicastatus UNDECIDED but was " + s.getDanicaStatus(), 
               s.getDanicaStatus().equals(DanicaStatus.UNDECIDED));
       
    }

}
