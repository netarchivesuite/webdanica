package dk.kb.webdanica.datamodel.criteria;

public class CriteriaKeyAndTable {
	public String tablename;
	public String url;
	public String urlOrig; //only set if != url
	public java.sql.Timestamp Cext3;
	public String Cext3Orig; //date
	public SingleCriteriaResult allres = new SingleCriteriaResult();
}

