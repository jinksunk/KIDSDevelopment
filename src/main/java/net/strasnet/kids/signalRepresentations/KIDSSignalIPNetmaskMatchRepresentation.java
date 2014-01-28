package net.strasnet.kids.signalRepresentations;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.*;

import net.strasnet.kids.KIDSCanonicalRepresentation;
import net.strasnet.kids.datasources.KIDSSnortIPAddressRange;
import net.strasnet.kids.lib.IPv4Address;

/**
 *  The canonical representation is, 
 * e.g. "[10.0.0.1/255.255.255.0,10.0.1.1/255.255.255.0,...]".  
 * @author chrisstrasburg
 *
 */
public class KIDSSignalIPNetmaskMatchRepresentation implements
		KIDSCanonicalRepresentation {

	private String myForm;
	private static Pattern validRepresentation = Pattern.compile("\\[((\\d{1,3}\\.){3}\\d{1,3}/(\\d{1,3}\\.){3}\\d{1,3})(,(\\d{1,3}\\.){3}\\d{1,3}/(\\d{1,3}\\.){3}\\d{1,3})*\\]");
	private static Pattern ipnmPattern = Pattern.compile("((\\d{1,3}\\.){3}\\d{1,3})/((\\d{1,3}\\.){3}\\d{1,3})");
	private List<KIDSSnortIPAddressRange> ipnmPairs;
	
	public KIDSSignalIPNetmaskMatchRepresentation(){
		super();
		ipnmPairs = new LinkedList<KIDSSnortIPAddressRange>();
	}
	
	public KIDSSignalIPNetmaskMatchRepresentation (String s) throws KIDSRepresentationInvalidRepresentationValueException{
		super();
		ipnmPairs = new LinkedList<KIDSSnortIPAddressRange>();
		setValue(s);
	}
	
	/**
	 * Given an IP representation as a dotted-quad or an integer, 
	 * will set the current value to a IP/netmask representation.
	 */
	public String convertToCanonicalForm(String otherForm) {
		String retValue;
		
		if (isLongForm(otherForm)){
			retValue = longToIPNM(otherForm);
		} else if (isSingleIPForm(otherForm)){
			retValue = singleIPToIPNM(otherForm);
		} else {
			retValue = null;
		}
		return retValue;
	}

	/**
	 * Returns a hexidecimal representation of the given binary string:
	 * @param retValue
	 * @return
	 */
	private String longToIPNM(String retValue) {
		return KIDSSnortIPAddressRange.longIPToString(Long.parseLong(retValue));
	}

	public boolean isLongForm(String otherForm) {
		// Check to ensure the entire string is either '0' or '1':
	    try {
	        long t = Long.parseLong( otherForm );
	        return (t > 0 && t < 4294967295L);
	    }
	    catch( Exception e ) {
	        return false;
	    }

	}

	/**
	 * Convert the given decimal value to a hexidecimal value
	 * @param retValue
	 * @return
	 */
	private String singleIPToIPNM(String retValue) {
		KIDSSnortIPAddressRange cRep;
		try {
			cRep = new KIDSSnortIPAddressRange(InetAddress.getByName(retValue));
			return cRep.toString();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
	}

	public boolean isSingleIPForm(String c) {
		if (c != null && c.length() > 0){
			if (c.matches("(\\d{1,3}\\.){3}\\d{1,3}")){
				return true;
			}
		}
		return false;
	}

	/**
	 * A byte match is in canonical form if it is a hexadecimal number between 00 and FF.
	 */
	public boolean isInCanonicalForm(String c) {
		if (c != null && c.length() > 0){
			if (c.matches("\\[((\\d{1,3}\\.){3}\\d{1,3}/(\\d{1,3}\\.){3}\\d{1,3})(,(\\d{1,3}\\.){3}\\d{1,3}/(\\d{1,3}\\.){3}\\d{1,3})*\\]")){
				return true;
			}
		}
		return false;
	}

	public String getCanonicalForm(){
		return myForm;
	}
	
	/**
	 * 
	 * @return A list of ipnmPairs, each representing one IP/Netmask dotted-quad pair from our
	 * representation.
	 * @throws UnknownHostException 
	 */
	public List<ipnmPair> getRangePairs() throws UnknownHostException{
		List<ipnmPair> retList = new LinkedList<ipnmPair>();
		
		Matcher m = ipnmPattern.matcher(myForm);
		while (m.find()){
			ipnmPair p = new ipnmPair(InetAddress.getByName(m.group(1)),
									  InetAddress.getByName(m.group(3)));
			retList.add(p);
		}
		
		return retList;
	}
	
	public void setValue(String newValue) throws KIDSRepresentationInvalidRepresentationValueException{
		if (!isInCanonicalForm(newValue)){
			myForm = "[" + convertToCanonicalForm(newValue) + "]";
		} else {
			myForm = newValue;
		}
		try {
			List<ipnmPair> ipnms = getRangePairs();
			Iterator<ipnmPair> i = ipnms.iterator();
			while (i.hasNext()){
				ipnmPair t = i.next();
				ipnmPairs.add(new KIDSSnortIPAddressRange(t.getIP(), t.getNM().getHostAddress()));
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
			throw new KIDSRepresentationInvalidRepresentationValueException();
		}
	}

	/**
	 * Each representation needs to generate a form suitable for naming individuals in the knowledge base.
	 */
	public String getNameForm() {
		return myForm;
	}

	/**
	 * In this case, the value is comparable if it is a valid IP address.
	 * @param c - The signal domain value to check
	 * @return true if c is a valid IP address
	 */
	@Override
	public boolean isValueComparable(String c) {
		// Determine if this is a valid IP:
		return IPv4Address.isDottedQuad(c);
	}

	/**
	 * Comparable values must be valid IP addresses.  This method will convert 
	 * to a comparable form from one of a number of equivalent forms. 
	 * In this case, we support:
	 *  - 'www.xxx.yyy.zzz/32'
	 *  - 'www.xxx.yyy.zzz/255.255.255.255' 
	 * 
	 * @param c - The signal domain value (in a compatible form) to convert
	 * @return A value in the form 'www.xxx.yyy.zzz'
	 */
	@Override
	public String getComparableForm(String c)
			throws KIDSRepresentationIncompatibleValueException {
		if (! isValueComparable(c)){
			throw new KIDSRepresentationIncompatibleValueException();
		}
		return c;
	}

	/**
	 * An IP address matches if it falls within the range specified by this signal.
	 * @param c - The IP address to check membership of.
	 * @return true if 'c' falls within the range specified
	 * @throws KIDSRepresentationIncompatibleValueException - If 'c' is not in comparable form.
	 * @throws  
	 */
	@Override
	public boolean matches(String c)
			throws KIDSRepresentationIncompatibleValueException {
		
		// Return true if our IP falls in the range of any of our pairs:
		Iterator<KIDSSnortIPAddressRange> myRanges = ipnmPairs.iterator();

		while (myRanges.hasNext()){
			KIDSSnortIPAddressRange ipr = myRanges.next();
			try {
				if (ipr.inRange(c)){
					return true;
				}
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				throw new KIDSRepresentationIncompatibleValueException();
			}
		}
		
		return false;
	}
	
	private class ipnmPair {
		private InetAddress ip;
		private InetAddress netmask;
		public ipnmPair (InetAddress i, InetAddress inetAddress) throws UnknownHostException{
			ip = i;
			netmask = inetAddress;
		}
		
		public InetAddress getIP() {
			return ip;
		}
		
		public InetAddress getNM() {
			return netmask;
		}
	}
	
}
