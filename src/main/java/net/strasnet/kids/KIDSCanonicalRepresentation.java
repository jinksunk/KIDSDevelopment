package net.strasnet.kids;

import net.strasnet.kids.signalRepresentations.KIDSRepresentationIncompatibleValueException;
import net.strasnet.kids.signalRepresentations.KIDSRepresentationInvalidRepresentationValueException;

/**
 * The interface through which to work with canonical signal representations.  Each distinct signal representation
 * must define a class which implements this interface.  The purpose is to provide:
 * 
 * 1) The capability to consume information in a one of several possible syntactic forms.
 * 2) The ability to compare values from the associated signal domain with signals using this representation.
 * 
 * @author chrisstrasburg
 *
 */
public interface KIDSCanonicalRepresentation {

	/**
	 * 
	 * @param otherForm - One of other supported forms. Implementing classes should specify supported forms.
	 * @return - The canonical form of the representation.
	 * @throws KIDSRepresentationInvalidRepresentationValueException 
	 */
	String convertToCanonicalForm(String otherForm) throws KIDSRepresentationIncompatibleValueException, KIDSRepresentationInvalidRepresentationValueException;
	
	/**
	 * 	 * @param c - The "query" form, to be evaluated.
	 * @return - True if 'c' is in canonical form, 
	 */
	boolean isInCanonicalForm(String c);
	
	/**
	 * Returns the canonical form currently stored in the representation object.
	 * @return - The currently stored canonical form.
	 */
	String getCanonicalForm();
	
	/**
	 * Modifies the currently stored canonical form by setting it to validForm.  If validForm is not valid,
	 * the currently stored form will remain unchanged.
	 * @param validForm
	 * @throws KIDSRepresentationInvalidRepresentationValueException 
	 */
	void setValue(String validForm) throws KIDSRepresentationInvalidRepresentationValueException;
	
	/**
	 * 
	 * @return - A version of the canonical form suitable for use as part of the signal IRI in the ontology.
	 */
	String getNameForm();
	
	/**
	 * 
	 * @param c - A signal domain *value* (not signal).
	 * @return - True if c can be compared with this signal representation, false otherwise.
	 */
	boolean isValueComparable(String c);
	
	/**
	 * 
	 * @param c - A signal domain *value* (not signal).
	 * @return - c, but in a form which can be compared with signals in this representation.
	 */
	String getComparableForm(String c) throws KIDSRepresentationIncompatibleValueException;
	
	/**
	 * 
	 * @param c - A comparable signal domain *value* (not signal)
	 * @return - true if the value is "matched" by this signal, false otherwise.
	 */
	boolean matches(String c) throws KIDSRepresentationIncompatibleValueException;
}
