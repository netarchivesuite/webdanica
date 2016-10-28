package dk.kb.webdanica.tools;

import dk.kb.webdanica.WebdanicaSettings;
import dk.kb.webdanica.datamodel.dao.CassandraDAOFactory;
import dk.kb.webdanica.datamodel.dao.DAOFactory;
import dk.kb.webdanica.datamodel.dao.HBasePhoenixDAOFactory;
import dk.kb.webdanica.utils.DatabaseUtils;
import dk.kb.webdanica.utils.SettingsUtilities;

public class ComputeStats {

    private static DAOFactory daoFactory;

    public static void main(String[] args) throws Exception {
        final String DEFAULT_DATABASE_SYSTEM = "cassandra";
        String databaseSystem = SettingsUtilities.getStringSetting(WebdanicaSettings.DATABASE_SYSTEM, DEFAULT_DATABASE_SYSTEM);
        if ("cassandra".equalsIgnoreCase(databaseSystem)) {
            daoFactory = new CassandraDAOFactory();
        } else if ("hbase-phoenix".equalsIgnoreCase(databaseSystem)) {
            daoFactory = new HBasePhoenixDAOFactory();
        }
        DatabaseUtils.printDatabaseStats(daoFactory);
        
    }

}
