package dk.kb.webdanica.datamodel.criteria;

import java.math.BigInteger;

public class BitUtils {
	public static int unsetBit(int x, int calcDanishCode) {
	    BigInteger v = new BigInteger( Integer.toString((int) (-1 * calcDanishCode)));
	    v = v.clearBit(x-1);
	    return (-1*v.intValue());
	} 

	public static int getBit(short x, int calcDanishCode) {
	    BigInteger v = new BigInteger( Integer.toString((int) (-1 * calcDanishCode)));
	    return (v.testBit(x-1)?1:0);
	}

	public static int setBit(int x, int calcDanishCode) {
	    int v = -1* (int)calcDanishCode;
	    v |= (1 << (x-1) );
	    return (-1*v);
	}

}
