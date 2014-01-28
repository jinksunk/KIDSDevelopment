package net.strasnet.kids;

/**
 * Indicates that data obtained from the ontology is inconsistent with respect to the assumptions made in the KIDS framework or client code.
 * NOTE: this does not necessarily indicate an inconsistent ontology from the reasoner perspective.
 * @author chrisstrasburg
 *
 */
public class KIDSOntologyException extends KIDSException {

	public KIDSOntologyException(String string) {
		super(string);
	}

	public KIDSOntologyException() {
		super();
	}

}
