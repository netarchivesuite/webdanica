package dk.kb.webdanica.core.tools;

import dk.kb.webdanica.core.datamodel.dao.DAOFactory;
import dk.kb.webdanica.core.utils.DatabaseUtils;

public class ComputeStats {

    private static DAOFactory daoFactory;

    public static void main(String[] args) throws Exception {
    	daoFactory = DatabaseUtils.getDao();
        DatabaseUtils.printDatabaseStats(daoFactory);
        daoFactory.close();
    }
}
