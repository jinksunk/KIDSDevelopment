package net.strasnet.kids;

/**
 * Indicates that an individual in the ontology contains too many or too few object values.
 * @author chrisstrasburg
 *
 */
public class KIDSOntologyObjectValuesException extends KIDSOntologyException {

	public KIDSOntologyObjectValuesException(String string) {
		super(string);
	}
}
