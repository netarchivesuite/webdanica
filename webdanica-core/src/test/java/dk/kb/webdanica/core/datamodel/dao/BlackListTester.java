package dk.kb.webdanica.core.datamodel.dao;

import static org.junit.Assert.*;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

import org.junit.Test;

import dk.kb.webdanica.core.datamodel.BlackList;

public class BlackListTester {

    @Test
    public void testConstructors() {
        String name = "TheName";
        String desc = "This contains some crawlertraps";
        String trapOne = "This is trapOne";
        String trapTwo = "This is trapTwo";
        List<String> traps = new ArrayList<String>();
        traps.add(trapOne);
        traps.add(trapTwo);
        boolean isactive = true;
        Long lastupdate = System.currentTimeMillis();
        BlackList b = new BlackList(name, desc, traps, isactive);
        assertTrue("blacklist b should have correct name", b.getName().equals(name));
        assertTrue("blacklist b should have correct description", b.getDescription().equals(desc));
        assertTrue("blacklist b should have correct isactive", b.isActive() == isactive);
        assertTrue("blacklist b should have correct list of traps", b.getList().containsAll(traps));
        
        
        UUID uid = UUID.randomUUID();
        BlackList b1 = new BlackList(uid, name, desc, traps, lastupdate, isactive);
        assertTrue("blacklist b1 should have correct uid", b1.getUid() == uid);
        assertTrue("blacklist b1 should have correct name", b1.getName().equals(name));
        assertTrue("blacklist b1 should have correct description", b1.getDescription().equals(desc));
        assertTrue("blacklist b1 should have correct lastupdate", b1.getLastUpdate() == lastupdate);
        assertTrue("blacklist b1 should have correct isactive", b1.isActive() == isactive);
        assertTrue("blacklist b1 should have correct list of traps", b1.getList().containsAll(traps));
        b1 = new BlackList(uid, name, desc, traps, lastupdate, false);
        assertFalse("blacklist b1 should have ISACTIVE=FALSE", b1.isActive());        
    }

}
