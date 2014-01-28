package net.strasnet.kids.signalRepresentations;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import net.strasnet.kids.KIDSCanonicalRepresentation;
import net.strasnet.kids.datasources.KIDSSnortIPAddressRange;

/**
 *  The canonical representation is, 
 * e.g. "[10,60]", which signifies 10 unique IP addresses seen in 60 seconds.  
 * @author chrisstrasburg
 *
 */
public class KIDSSignalUniqueSourceIPAddressCountThresholdRepresentation implements
		KIDSCanonicalRepresentation {

	private String myForm;
	private Integer ips;
	private Integer seconds;
	
	public KIDSSignalUniqueSourceIPAddressCountThresholdRepresentation(){
		super();
	}
	
	/**
	 * The only valid String based representations are:
	 *  - "[X,Y]" (canonical form) where X is the number of unique source IPs, and Y is the number of seconds
	 *  - "X,Y"
	 * @throws KIDSRepresentationIncompatibleValueException 
	 */
	public KIDSSignalUniqueSourceIPAddressCountThresholdRepresentation (String s) throws KIDSRepresentationInvalidRepresentationValueException{
		super();
		if (!isInCanonicalForm(s)){
			setValue(convertToCanonicalForm(s));
		} else {
			setValue(s);
		}
	}
	

	/**
	 * Creates the representation object from the given X,Y values.
	 * @param retValue
	 * @return
	 */
	public KIDSSignalUniqueSourceIPAddressCountThresholdRepresentation(int X, int Y){
		super();
		try {
			setValue(convertToCanonicalForm(X,Y));
		} catch (KIDSRepresentationInvalidRepresentationValueException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Given two integer values, X and Y, will return the canonical form of X unique source IP addresses
	 * seen in Y seconds.
	 */
	public String convertToCanonicalForm(int X, int Y) {
		String retValue;
		
		retValue = "[" + X + "," + Y + "]";
		
		return retValue;
	}

	/**
	 * A unique source IP address count threshold is in canonical form if it is of the form '[X,Y]', where X and Y are integers.
	 */
	public boolean isInCanonicalForm(String c) {
		if (c != null && c.length() > 0){
			if (c.matches("\\[(\\d+),(\\d+)\\]")){
				try {
					Integer[] t = getIntegerValues(c);
					if (t[0] >= 0 && t[1] >= 0){
						return true;
					}
				} catch (KIDSRepresentationInvalidRepresentationValueException e){
					return false;
				}
			}
		}
		return false;
	}

	public String getCanonicalForm(){
		return myForm;
	}
	
	public void setValue(String newValue) throws KIDSRepresentationInvalidRepresentationValueException{
		if (!isInCanonicalForm(newValue)){
			myForm = convertToCanonicalForm(newValue);
		} else {
			myForm = newValue;
		}
		Integer[] toSet = getIntegerValues(myForm);
		seconds = toSet[0];
		ips = toSet[1];
	}

	/**
	 * Each representation needs to generate a form suitable for naming individuals in the knowledge base.
	 */
	public String getNameForm() {
		return myForm;
	}

	/**
	 * Given a canonical form, return the count,seconds as integers.
	 * @param s
	 * @return
	 * @throws KIDSRepresentationIncompatibleValueException 
	 */
	public Integer[] getIntegerValues(String s) throws KIDSRepresentationInvalidRepresentationValueException{
		if (s.matches("\\[(\\d+),(\\d+)\\]")){
			Integer[] l = new Integer[2];
			l[0] = Integer.parseInt(s.substring(1, s.indexOf(",")));
			l[1] = Integer.parseInt(s.substring(s.indexOf(",") + 1,s.length() - 1));
			return l;
		} else {
			throw new KIDSRepresentationInvalidRepresentationValueException();
		}
	}
	
	/**
	 * The only String based conversion source valid for this representation is "X,Y"
	 * @throws KIDSRepresentationIncompatibleValueException 
	 */
	public String convertToCanonicalForm(String otherForm) throws KIDSRepresentationInvalidRepresentationValueException {
		if (otherForm.matches("\\[(\\d+),(\\d+)\\]")){
			int X = Integer.parseInt(otherForm.substring(1, otherForm.indexOf(",")));
			int Y = Integer.parseInt(otherForm.substring(otherForm.indexOf(",") + 1,otherForm.length() - 1));
			return convertToCanonicalForm(X,Y);
		} else {
			throw new KIDSRepresentationInvalidRepresentationValueException();
		}
	}

	/**
	 * Valid integer values are comparable.
	 */
	@Override
	public boolean isValueComparable(String c) {
		try {
			isInCanonicalForm(c);
		} catch (Exception e){
			return false;
		}
		return true;
	}

	/**
	 * Return a form of 'c' which is comparable.  Currently, only an Integer string is accepted.
	 * @throws KIDSRepresentationIncompatibleValueException if a string is passed which fails Integer.parseInt()
	 */
	@Override
	public String getComparableForm(String c)
			throws KIDSRepresentationIncompatibleValueException {
		if (! isValueComparable(c)){
			throw new KIDSRepresentationIncompatibleValueException();
		}
		return c;
	}

	@Override
	public boolean matches(String c)
			throws KIDSRepresentationIncompatibleValueException {
		try {
			String toComp = getComparableForm(c);
			Integer[] cVals = this.getIntegerValues(toComp);
			if (cVals[0] <= this.seconds &&
				cVals[1] >= this.ips){
				return true;
			} else {
				return false;
			}
		} catch (Exception e){
			throw new KIDSRepresentationIncompatibleValueException();
		}
	}
	
	
}
