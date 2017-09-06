package dk.kb.webdanica.core.datamodel.dao;

import static org.junit.Assert.*;

import org.junit.Test;

public class HbaseSettingsTester {

    @Test
    public void test() {
        String dummyConnectionString="jdbc:localhost";
        HbaseSettings hb = new HbaseSettings(dummyConnectionString);
        assertTrue(hb.getConnectionString().equals(dummyConnectionString));
        String defaultConnectionString = "jdbc:phoenix:localhost:2181:/hbase";
        hb = HbaseSettings.getDefaultSettings();
        assertTrue(hb.getConnectionString().equals(defaultConnectionString));
    }

}
