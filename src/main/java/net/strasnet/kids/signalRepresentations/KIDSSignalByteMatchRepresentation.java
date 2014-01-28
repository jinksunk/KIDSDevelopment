package net.strasnet.kids.signalRepresentations;

import net.strasnet.kids.KIDSCanonicalRepresentation;

/**
 * This class represents a simple Byte match.  Bytes are canonically represented as a Hex pair.
 * 
 * Acceptable input forms include:
 *  - A Hex pair (00 - FF)
 *  - A base-10 number between 0 and 255
 *  - An 8-character string of '0' and '1' characters
 *  
 * Acceptable comparison value forms include:
 *  - A Hex pair (00 - FF)
 *  - A base-10 number between 0 and 255
 *  - An 8-character string of '0' and '1' characters
 *  
 * A "match" for this representations is true of the comparison value == the canonical representation,
 * false otherwise.
 * 
 * @author chrisstrasburg
 *
 */
public class KIDSSignalByteMatchRepresentation implements
		KIDSCanonicalRepresentation {

	private String myForm;	// "00" - "FF"
	
	public KIDSSignalByteMatchRepresentation(){
		super();
	}
	
	/**
	 * Sets the internal representation to 's'
	 * @param s
	 * @throws KIDSRepresentationIncompatibleValueException 
	 */
	public KIDSSignalByteMatchRepresentation (String s) throws KIDSRepresentationInvalidRepresentationValueException{
		super();
		setValue(s);
	}
	
	/**
	 * Given a byte representation as one of:
	 *  - A decimal number between 0 and 255
	 *  - An 8-bit string
	 *  
	 * Will set the current value to a hexidecimal representation between 00 and FF.
	 * @throws KIDSRepresentationIncompatibleValueException 
	 */
	public String convertToCanonicalForm(String otherForm) throws KIDSRepresentationInvalidRepresentationValueException {
		String retValue;
		
		if (isDecimalForm(otherForm)){
			retValue = decToHex(otherForm);
		} else if (isBinaryForm(otherForm)){
			retValue = binToHex(otherForm);
		} else if (isInCanonicalForm(otherForm)){
			retValue = otherForm;
		} else {
			throw new KIDSRepresentationInvalidRepresentationValueException();
		}
		return retValue;
	}

	/**
	 * Returns a hexidecimal representation of the given binary string:
	 * @param retValue
	 * @return
	 */
	private String binToHex(String retValue) {
		int decimalValue = 0;
		for (int i = 0; i < 8; i++){
			decimalValue += Integer.parseInt(retValue.substring(i, i)) * java.lang.Math.pow(2,7 - i);
		}
		return decToHex("" + decimalValue);
	}

	private boolean isBinaryForm(String otherForm) {
		// Check to ensure the entire string is either '0' or '1':
		if (otherForm.length() == 8){
			for (int i = 0; i < 8; i++){
			if (otherForm.charAt(i) != '0' && 
				otherForm.charAt(i) != '1'){
				return false;
			}
			return true;
			}
		}
		return false;
	}

	/**
	 * Convert the given decimal value to a hexidecimal value
	 * @param retValue
	 * @return
	 */
	private String decToHex(String retValue) {
		String hexValue = Integer.toString(Integer.parseInt(retValue),16);
		
		return hexValue;
	}

	private boolean isDecimalForm(String otherForm) {
		try {
			int intermediate = Integer.parseInt(otherForm);
			if (intermediate >= 0 && intermediate < 256){
				return true;
			}
		} catch (NumberFormatException e){
			return false;
		}
		return false;
	}

	/**
	 * A byte match is in canonical form if it is a hexidecimal number between 00 and FF.
	 */
	public boolean isInCanonicalForm(String c) {
		if (c.length() == 2){
			if (c.matches("[A-F0-9]{2}")){
				return true;
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
	}

	/**
	 * Each representation needs to generate a form suitable for naming individuals in the knowledge base.
	 */
	public String getNameForm() {
		return myForm;
	}

	/**
	 * In this case, if it can be converted to canonical form, it is comparable.
	 * @param c - The string to check for comparability
	 */
	@Override
	public boolean isValueComparable(String c) {
		try {
			convertToCanonicalForm(c);
			return true;
		} catch (KIDSRepresentationInvalidRepresentationValueException e){
			return false;
		}
		
	}

	@Override
	/**
	 * In this case, the comparable form and the canonical signal definition form are the same.
	 * @param c - The signal domain value to convert (if necessary).
	 * @throws KIDSRepresentationIncompatibleValueException - if c is not on a comparable form.
	 */
	public String getComparableForm(String c)
			throws KIDSRepresentationIncompatibleValueException {
		try {
			return convertToCanonicalForm(c);
		} catch (KIDSRepresentationInvalidRepresentationValueException e) {
			throw new KIDSRepresentationIncompatibleValueException();
		}
	}

	/**
	 * True if c and stored signal value are equal.
	 * @param c - The signal domain value to check.
	 * @throws KIDSRepresentationIncompatibleValueException - if 'c' is not in a comparable form
	 */
	@Override
	public boolean matches(String c)
			throws KIDSRepresentationIncompatibleValueException {
		return getComparableForm(c).equals(myForm);
	}
}
