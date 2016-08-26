import java.io.IOException;

import org.jwat.common.Base64;

import dk.kb.webdanica.datamodel.criteria.CriteriaUtils;


public class TestBase64Conversion {

	public static void main(String[] args) throws IOException {
		String d = "Dette er en streng";
		String d1 = CriteriaUtils.toBase64(d);
		String d2 = CriteriaUtils.fromBase64(d1);
		System.out.println(d1);
		System.out.println(d2);
		
		System.out.println(Base64.decodeToString(d1, false));
		System.out.println(Base64.decodeToString(d1, true));
		System.out.println(Base64.encodeString(d));
	}

}
