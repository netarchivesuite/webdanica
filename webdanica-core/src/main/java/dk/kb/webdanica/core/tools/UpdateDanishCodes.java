package dk.kb.webdanica.core.tools;

import java.util.List;

import dk.kb.webdanica.core.datamodel.criteria.CalcDanishCode;
import dk.kb.webdanica.core.datamodel.criteria.SingleCriteriaResult;
import dk.kb.webdanica.core.datamodel.dao.CriteriaResultsDAO;
import dk.kb.webdanica.core.datamodel.dao.DAOFactory;
import dk.kb.webdanica.core.utils.DatabaseUtils;

/**
 * Tool to check, if the C4b criteria makes 
 * @deprecated "Not useful as-is"
 */
public class UpdateDanishCodes {

    private static DAOFactory daoFactory;

    public static void main(String[] args) throws Exception {
    	daoFactory = DatabaseUtils.getDao();
    	CriteriaResultsDAO cdao = daoFactory.getCriteriaResultsDAO();
    	List<SingleCriteriaResult> results = cdao.getResults(); // FIXME add limit
    	for (SingleCriteriaResult s: results) {
    		if (s.calcDanishCode < 0){
    			boolean matched = CalcDanishCode.checkForDanishCode4(s, s.C.get("C4b"));
    			if (matched) {
    				System.out.println("Url '" + s.url + "', harvest '" +  s.harvestName + "' matches new danish code: " +  s.calcDanishCode + "(" + s.intDanish + ")");
    			}
    			//cdao.updateRecord(s);
    		}
    	}
        daoFactory.close();
    }
}
