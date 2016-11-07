package dk.kb.webdanica.core.tools;

import dk.kb.webdanica.core.Constants;
import dk.kb.webdanica.core.WebdanicaSettings;
import dk.kb.webdanica.core.datamodel.dao.CassandraDAOFactory;
import dk.kb.webdanica.core.datamodel.dao.DAOFactory;
import dk.kb.webdanica.core.datamodel.dao.HBasePhoenixDAOFactory;
import dk.kb.webdanica.core.utils.DatabaseUtils;
import dk.kb.webdanica.core.utils.SettingsUtilities;

public class ComputeStats {

    private static DAOFactory daoFactory;

    public static void main(String[] args) throws Exception {
        String databaseSystem = SettingsUtilities.getStringSetting(WebdanicaSettings.DATABASE_SYSTEM, Constants.DEFAULT_DATABASE_SYSTEM);
        if ("cassandra".equalsIgnoreCase(databaseSystem)) {
            daoFactory = new CassandraDAOFactory();
        } else if ("hbase-phoenix".equalsIgnoreCase(databaseSystem)) {
            daoFactory = new HBasePhoenixDAOFactory();
        }
        DatabaseUtils.printDatabaseStats(daoFactory);
        
    }

}
