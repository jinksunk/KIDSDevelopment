/**
 * Represents a generic interface to a syntactic form.
 * TODO: Relate this to the classes under net.strasnet.kids.detectorsyntaxproducers
 */
package net.strasnet.kids;

import net.strasnet.kids.detectorsyntaxproducers.KIDSIncompatibleSyntaxException;
import net.strasnet.kids.gui.KIDSAddEventOracle;

import org.semanticweb.owlapi.model.OWLNamedIndividual;

/**
 * @author chrisstrasburg
 *
 */
public interface KIDSSyntacticFormGenerator {

	//public void setCurrentEvent(OWLNamedIndividual e);
	
	//public OWLNamedIndividual getCurrentEvent();
	
	public String getSyntacticForm() throws KIDSIncompatibleSyntaxException, KIDSOntologyObjectValuesException;
	
	public void setOracle(KIDSAddEventOracle k);
}
