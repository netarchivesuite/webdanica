package dk.kb.webdanica.batch;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import dk.netarkivet.common.utils.FileUtils;
import dk.netarkivet.common.utils.archive.ArchiveHeaderBase;


/** 
 * This code extract links in html code pointing outside of the .dk TLD domain.
 */
public class ExtractLinks { 

    private final String document;
    
    public ExtractLinks(String document) {
        this.document = document;
    }
    
    public ExtractLinks(File documentFile) throws IOException {
        this.document = FileUtils.readFile(documentFile);
    }
    
    /**
     * Decide whether the link points inside or outside the DK domain.
     * Relative urls are deemed inside.
     * Links to .dk are deeemed inside.
     * All other links are deeemed outside.
     *     
     * @param url A given url
     * @return true, if the url is deemed outside, otherwise false
     */
    private static boolean pointsOutsideDK(String url) {
        String urlLower = url.toLowerCase();
        if (!urlLower.startsWith("http")) {
            return false;
        }
        URL netUrl;
        String host = null;
        try {
            netUrl = new URL(urlLower);
            host = netUrl.getHost();
        } catch (MalformedURLException e) {
            return false;
        }
        if (host == null) {
            return false;
        }
        if (host.endsWith(".dk")) {
            return false;
        } else {
            return true;
        }
    }
    
    /**
     * Collect any links in document referring outside the .DK.
     * @return a set of urls referring outside the .DK
     * @throws ParserException 
     */
    public Set<String> getLinksPointingOutsideDK() throws ParserException {
        NodeFilter filter;
        NodeList list;
        Set<String> links = new HashSet<String>();
        Parser p = new Parser();
        filter = new NodeClassFilter (LinkTag.class);
        
        try {
            p.setInputHTML(document);
            list = p.extractAllNodesThatMatch (filter);
            for (int i = 0; i < list.size (); i++) {
                LinkTag n = (LinkTag) list.elementAt(i);
                // look for attributes href and HREF 
                String url = n.getAttribute("href");
                if (url == null) {
                    url = n.getAttribute("HREF");
                }
                // add url, if not url and outside .dk
                if (url != null && pointsOutsideDK(url)) {
                    links.add(url);
                }
            } 
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return links;
    }
    
    /**
     * Static method to test whether the url bundled with the metadata is a .dk URL.
     * @param metadata The header of a ArchiveRecord (ARC/WARC)
     * @return true if the URL of the record is a .dk url
     */
    public static boolean urlIsDK(ArchiveHeaderBase metadata) {
        String url = metadata.getUrl();
        return !pointsOutsideDK(url);
    }
}
