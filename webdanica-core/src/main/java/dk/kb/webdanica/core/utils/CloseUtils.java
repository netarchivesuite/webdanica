 package dk.kb.webdanica.core.utils;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by abr on 8/3/17.
 */
public class CloseUtils {

    private static final Logger logger = LoggerFactory.getLogger(CloseUtils.class);

    public static void closeQuietly(AutoCloseable autoCloseable){
        if (autoCloseable != null) {
            try {
                autoCloseable.close();
            } catch (Exception e) {
                logger.warn("Caught Exception when closing {}", autoCloseable, e);
            }
        }
    }

    public static void freeQuietly(java.sql.Array sqlArr) {
    	if (sqlArr != null) {
    		try {
    			sqlArr.free();
    		} catch (SQLException e) {
                logger.warn("Caught Exception when freeing {}", sqlArr, e);
    		}
    	}
    }

}
