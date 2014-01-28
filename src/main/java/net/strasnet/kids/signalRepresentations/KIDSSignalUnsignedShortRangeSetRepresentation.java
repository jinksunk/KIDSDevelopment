package net.strasnet.kids.signalRepresentations;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.strasnet.kids.KIDSCanonicalRepresentation;
import net.strasnet.kids.lib.Range;
import net.strasnet.kids.signalRepresentations.KIDSSignalIPNetmaskMatchRepresentation;

/**
 * This class implements the canonical representation of a set of ranges of unsigned short integers.
 * This representation is used, e.g. for specifying signals involving port numbers.
 * 
 * The canonical representation is a comma separated list of colon-delimited values, surrounded by
 * square brackets (as with other sets of ranges).  E.g.:
 * 
 * [100:200,1000:2000,...]
 * 
 * If not already in canonical form, single values will be converted to this form, e.g.:
 *  100 -> [100:100]
 * 
 * Internally, a sorted list of ranges is maintained for efficient comparisons.
 * 
 * @author chrisstrasburg
 *
 */

public class KIDSSignalUnsignedShortRangeSetRepresentation implements
		KIDSCanonicalRepresentation {

	private String myForm;
	private List<Range<Short>> nativeForm; // A list of 2-element arrays
	private static Pattern validRepresentation = Pattern.compile("\\[\\d{1,5}:\\d{1,5}(,\\d{1,5}:\\d{1,5})*\\]");
	private static Pattern rangePattern = Pattern.compile("((\\d{1,5}):(\\d{1,5}))");

	
	public KIDSSignalUnsignedShortRangeSetRepresentation(){
		super();
		nativeForm = new ArrayList();
	}
	
	public KIDSSignalUnsignedShortRangeSetRepresentation (String s) throws KIDSRepresentationInvalidRepresentationValueException{
		super();
		nativeForm = new ArrayList();
		setValue(s);
	}
	
	/**
	 * Given a byte representation as one of:
	 *  - A decimal number between 0 and 255
	 *  - An 8-bit string
	 *  
	 * Will set the current value to a hexidecimal representation between 00 and FF.
	 */
	public String convertToCanonicalForm(String otherForm) {
		String retValue;
		
		if (isInSingleValueForm(otherForm)){
			retValue = singleValueToCF(otherForm);
		} else {
			retValue = null;
		}
		return retValue;
	}

	/**
	 * Given a single value (short), will produce the canonical form.
	 * @param otherForm
	 * @return
	 */
	private String singleValueToCF(String otherForm) {
		return "[" + otherForm + ":" + otherForm + "]";
	}

	public boolean isInSingleValueForm(String otherForm) {
		// Check to see if the string can be represented as a short in the correct range:
		try {
			int testVal = Integer.parseInt(otherForm);
			return (testVal >= 0 && testVal < 65535);
		} catch (NumberFormatException e){
			return false;
		}
	}


	/**
	 * Assuming that 'c' is in canonical form, attempt to parse it into a list of range objects.
	 * Throw an invalid value exception if parsing fails.
	 * @param c
	 * @throws KIDSRepresentationInvalidRepresentationValueException 
	 */
	private ArrayList<Range<Short>> nativeFromCanonicalForm(String c) throws KIDSRepresentationInvalidRepresentationValueException{
		ArrayList<Range<Short>> toReturn = new ArrayList<Range<Short>>();
		
		Matcher m = rangePattern.matcher(c);
		try {
			while (m.find()){
				Range<Short> p = new Range<Short>(Short.parseShort(m.group(2)),
									  Short.parseShort(m.group(3)));
				toReturn.add(p);
			}
		} catch (NumberFormatException e){
			throw new KIDSRepresentationInvalidRepresentationValueException();
		}

		return toReturn;
	}
	
	/**
	 * Determine if the given string argument is a valid canonical form.
	 * TODO: Check actual values to ensure they are within range (0 through 2^16 - 1)
	 */
	public boolean isInCanonicalForm(String c) {
		return validRepresentation.matcher(c).matches();
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
		nativeForm = nativeFromCanonicalForm(myForm);
		
	}

	/**
	 * Each representation needs to generate a form suitable for naming individuals in the knowledge base.
	 */
	public String getNameForm() {
		return myForm;
	}

	/**
	 * @return true if the given value is a valid "short"
	 */
	@Override
	public boolean isValueComparable(String c) {
		try {
			Short test = Short.parseShort(c);
		} catch (NumberFormatException e){
			return false;
		}
		return true;
	}

	/**
	 * @param c - A valid (but possibly non-canonical) form of this representation.  Currently the only valid form for this
	 *   representation is a string of digits representing a valid Unsigned "Short" value.
	 * @return The canonical form version of 'c'
	 * @throws KIDSRepresentationIncompatibleValueException if 'c' is not in a valid form.
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
	 * @param c - A valid representation to check.
	 * @return true if the value specified is in one of the ranges in this set, false otherwise.
	 */
	@Override
	public boolean matches(String c)
			throws KIDSRepresentationIncompatibleValueException {
		if (! isValueComparable(c)){
			throw new KIDSRepresentationIncompatibleValueException();
		}
		Iterator<Range<Short>> i = nativeForm.iterator();
		while (i.hasNext()){
			Range<Short> test = i.next();
			if (test.contains(Short.parseShort(c))){
				return true;
			}
		}
		return false;
	}
}
