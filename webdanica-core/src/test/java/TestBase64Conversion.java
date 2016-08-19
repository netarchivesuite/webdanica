import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.zip.Deflater;

import dk.kb.webdanica.datamodel.criteria.CriteriaUtils;


public class TestBase64Conversion {

	public static void main(String[] args) throws IOException {
		String d = "Dette er en streng";
		String d1 = CriteriaUtils.toBase64(d);
		String d2 = CriteriaUtils.fromBase64(d1);
		System.out.println(d1);
		System.out.println(d2);
		
		String d3 = CriteriaUtils.compress(d);
		String d4 = CriteriaUtils.decompress(d3);
		System.out.println(d3);
		System.out.println(d4);
		
		
		
	}
	
	
	

}
