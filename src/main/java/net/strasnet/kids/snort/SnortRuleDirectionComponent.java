/**
 * 
 */
package net.strasnet.kids.snort;

import java.util.Set;

import net.strasnet.kids.gui.KIDSAddEventOracle;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 * @author chrisstrasburg
 *
 */
public class SnortRuleDirectionComponent extends AbstractSnortRuleComponent {

	String value = "<>";
	
	public SnortRuleDirectionComponent(KIDSAddEventOracle ko, Set<IRI> currentSignalSet) {
		super(ko, currentSignalSet);
		
		//TODO: First, check the KB for a specification; set 'value' accordingly
	}

	public String toString(){
		return value;
	}
	
}
