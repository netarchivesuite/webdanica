package dk.kb.webdanica.datamodel;

import com.datastax.driver.core.Session;

public interface Database {
    boolean isClosed();
    void close();
    // TODO find out how to support both hbase and cassandra with the same Database interface
    // probably getSession should be replaced something else
    Session getSession();  
}
