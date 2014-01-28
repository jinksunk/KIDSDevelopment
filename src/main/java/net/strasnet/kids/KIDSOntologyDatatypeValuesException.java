package net.strasnet.kids;

/**
 * Indicates that an individual in the ontology contains too many or too few datatype values.
 * @author chrisstrasburg
 *
 */
public class KIDSOntologyDatatypeValuesException extends KIDSOntologyException {

	public KIDSOntologyDatatypeValuesException(String string) {
		super(string);
	}

	public KIDSOntologyDatatypeValuesException() {
		super();
	}

}
