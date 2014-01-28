/**
 * 
 */
package net.strasnet.kids.snort;

import java.util.Iterator;
import java.util.Set;

import net.strasnet.kids.gui.KIDSAddEventOracle;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 * @author chrisstrasburg
 *
 */
public class SnortRuleMessageComponent extends AbstractSnortRuleComponent {

	String compHead = "msg:";
	String value = "alert";
	String messageProperty = "#eventMessageText";
	
	/**
	 * @param o
	 * @param ontoIRI
	 * @param f
	 * @param r
	 * @param e
	 */
	public SnortRuleMessageComponent(KIDSAddEventOracle ko, Set<IRI> currentSignalSet) {
		super(ko, currentSignalSet);
		
		value = "Event detected!";

		/**
		Set<OWLLiteral> eLiterals = r.getDataPropertyValues(e, 
				myF.getOWLDataProperty(
						IRI.create(myOIri + messageProperty)
						)
						);
		// Get the message value for this event, if any:
		if (!eLiterals.isEmpty()){
			Iterator<OWLLiteral> i = eLiterals.iterator();
			value = i.next().getLiteral().toString();
		} */
		
	}

	public String toString(){
		return compHead + "\"" + value + "\";";
	}
	
}
