package dk.kb.webdanica.oldtools;

public class HeritrixConfTester {

    // regexp fetched from HeritrixTemplate (should be equal to the one i Heritrix 1.14.4)
    public static final String ACCEPTABLE_FROM = "\\S+@\\S+\\.\\S+";
    // regexp fetched from HeritrixTemplate (should be equal to the one i Heritrix 1.14.4)
    private static final String USER_AGENT_REGEXP = 
            "\\S+.*\\(.*\\+http(s)?://\\S+\\.\\S+.*\\).*";
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        String ua = "Mozilla/5.0 (compatible; heritrix/1.14.4 +http://netarkivet.dk/webcrawler)";
        String from = "info@netarkivet.dk";
        //<string name="user-agent">Mozilla/5.0 (compatible; heritrix/1.14.4 +http://netarkivet.dk/webcrawler)</string>
        //<string name="from">info@netarkivet.dk</string>
        System.out.println(isFromOk(from));
        System.out.println(isUseragentOk(ua));
        
    }
    
 
    public static boolean isFromOk(String from) {
        return from.matches(ACCEPTABLE_FROM);
    }
    
    public static boolean isUseragentOk(String useragent) {
        return useragent.matches(USER_AGENT_REGEXP);
    }
}
