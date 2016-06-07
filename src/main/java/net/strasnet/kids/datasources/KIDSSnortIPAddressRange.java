/**
 * 
 */
package net.strasnet.kids.datasources;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author chrisstrasburg
 * This class represents an IPv4 address range. The class 
 * converts between specifications of different formats and supports comparisons.
 */
public class KIDSSnortIPAddressRange {
	private long start;
	private long end;
	private InetAddress netmask;
	
	/**
	 * Initialize the range as a single dotted quad address.
	 * @param dottedQuadIP - The dotted quad notation of an IP address.
	 */
	public KIDSSnortIPAddressRange (InetAddress dottedQuadIP){
		// Start and end are the same IP
		start = byte2long(dottedQuadIP.getAddress());
		end = start;
		try {
			netmask = InetAddress.getByName("255.255.255.255");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	/**
	public KIDSSnortIPAddressRange (InetAddress startIP, InetAddress endIP){
		start = byte2long(startIP.getAddress());
		end = byte2long(endIP.getAddress());
	} */
	
	public KIDSSnortIPAddressRange (InetAddress startDottedQuad, String subnetMask) throws UnknownHostException{
		// Convert subnet mask into a bitfield:
		byte[] b = InetAddress.getByName(subnetMask).getAddress();
		rangeFromBitmask(startDottedQuad, b);
		netmask = InetAddress.getByName(subnetMask);

	}
	
	public KIDSSnortIPAddressRange (InetAddress startDottedQuad, int maskSpec){
		int bits = 0x80000000 >> (maskSpec - 1);
		byte[] b = new byte[4];
		b[0] = (byte) ((bits & 0xff000000) >> 24);
		b[1] = (byte) ((bits & 0x00ff0000) >> 16);
		b[2] = (byte) ((bits & 0x0000ff00) >> 8);
		b[3] = (byte) (bits & 0x000000ff);
		try {
			netmask = InetAddress.getByAddress(b);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		rangeFromBitmask(startDottedQuad, b);
	}
	
	private void rangeFromBitmask (InetAddress startDottedQuad, byte[] b){
		byte[] ip = startDottedQuad.getAddress();
		long curbyte;
		long startb = 0;
		long endb = 0;
		
		// Starting from the low-order bits, generate start and end values:
		for (int i = 3; i >= 0; i--){
			// For this byte, if we have hit our first '1' already, and see another '0', throw and error:
			curbyte = (((long)(b[i] & ip[i])) & 0x000000ff) * ((long)Math.pow(2,(8*(3-i))));
			startb += curbyte;
			curbyte = (((long)(~b[i] | ip[i])) & 0x000000ff) * ((long)Math.pow(2,(8*(3-i))));
			endb += curbyte;
		}
		start = startb;
		end = endb;
	}
	
	public static long byte2long (byte[] b){
		int bi[] = new int[4];
		for (int i = 0; i < 4; i++){
			bi[i] = ((0x000000ff) & ((int)b[i]));
		}
		return (long) (bi[0] * Math.pow(2,24) + bi[1] * Math.pow(2,16) + bi[2] * Math.pow(2, 8) + bi[3]);
	}
	
	public String toString(){
		// Print as a range of dotted-quads:
		return longIPToString(start) + netmask.toString(); 
	}
	
	public static String longIPToString(long IP){
		int[] b = new int[4];
		for (int i = 0; i < 4; i++){
  		  b[i] = (int) ((IP >>> (((3-i) * 8))) & 0x000000ff);
		}
		return b[0] + "." + b[1] + "." + b[2] + "." + b[3];
	}
	/**
	 * Given a netmask value as a string, return the CIDR version of the mask.  We simply count 1's from the left
	 * until we see a zero.
	 * @param string
	 * @return
	 */
	public static int netmaskAsCidr(String string) {
		int cidr = 0;
		InetAddress nm;
		try {
            String addr = string;
            if (string.startsWith("/")){
                addr = string.substring(1);
            }
			nm = InetAddress.getByName(addr);
		
			int nm_long = (int)byte2long(nm.getAddress());
		
			// While 0x80 & byte[i] == 0x80, increment and keep counting:
			for (int i = 0; i < 32; i++){
				int test = (nm_long << i);
				if ((test & 0x80000000) == 0x80000000){
					cidr++;
				} else {
					i = 32;
				}
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		return cidr;
	}
	
	/**
	 * Return true if the given IP address lies in the range represented here:
	 * @param c
	 * @throws UnknownHostException 
	 */
	public boolean inRange(String c) throws UnknownHostException{
		long cl = byte2long(InetAddress.getByName(c).getAddress());
		return (cl >= start && cl <= end);
	}
	
	// Method to test the class:
	public static void main(String[] args){
		InetAddress ip1;
		try {
		ip1 = InetAddress.getByName("147.155.12.1");
		InetAddress ip2 = InetAddress.getByName("150.0.0.255");
		String nm1 = "255.255.255.0";
		String nm2 = "255.255.255.192";
		String nm4 = "255.255.255.255";
		String nm5 = "128.0.0.0";
		int nm3 = 16;
		
		KIDSSnortIPAddressRange kipr1 = new KIDSSnortIPAddressRange(ip1);
		System.out.println("[KIDSSnortIPAddressRange Test]: " + kipr1);
		//KIDSSnortIPAddressRange kipr2 = new KIDSSnortIPAddressRange(ip1, ip2);
		//System.out.println("[KIDSSnortIPAddressRange Test]: " + kipr2);
		KIDSSnortIPAddressRange kipr3 = new KIDSSnortIPAddressRange(ip1, nm1);
		System.out.println("[KIDSSnortIPAddressRange Test]: " + kipr3);
		KIDSSnortIPAddressRange kipr4 = new KIDSSnortIPAddressRange(ip1, nm2);
		System.out.println("[KIDSSnortIPAddressRange Test]: " + kipr4);
		KIDSSnortIPAddressRange kipr5 = new KIDSSnortIPAddressRange(ip1, nm3);
		System.out.println("[KIDSSnortIPAddressRange Test]: " + kipr5);
		System.out.println("[KIDSSnortIPAddressRange Test]: " + nm1 + "=" + netmaskAsCidr(nm1));
		System.out.println("[KIDSSnortIPAddressRange Test]: " + nm2 + " = " + netmaskAsCidr(nm2));
		System.out.println("[KIDSSnortIPAddressRange Test]: " + nm4 + " = " + netmaskAsCidr(nm4));
		System.out.println("[KIDSSnortIPAddressRange Test]: " + nm5 + " = " + netmaskAsCidr(nm5));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
