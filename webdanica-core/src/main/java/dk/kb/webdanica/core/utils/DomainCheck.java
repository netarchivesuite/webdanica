package dk.kb.webdanica.core.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class DomainCheck {

    public static void main(String[] args) {
        System.out.println(isDomainAlive("mortensen.vg"));
        System.out.println(isDomainAlive("campingpladser.name"));
        System.out.println(isDomainAlive("burchardt.name"));
        System.out.println(isDomainAlive("kb.dk"));
        System.out.println(isDomainAlive("familien-carlsen.dk"));
        System.out.println(isDomainAlive("novasol.co.uk"));
    }
    
    public static boolean isDomainAlive(String domain) {

            try {
                InetAddress.getByName(domain);
                //System.out.println(hostAddress.getHostAddress());
                return true;
            }
            catch (UnknownHostException uhe) {
                //System.err.println("Unknown host/domain: " + domain);
                return false;
            }
        }
    }

