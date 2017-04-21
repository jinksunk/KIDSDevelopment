package net.strasnet.kids.snort;

import java.util.Set;

import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOracle;
import net.strasnet.kids.gui.KIDSAddEventOracle;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

public class SnortRuleSourceIPComponent extends SnortRuleIPComponent {

	public SnortRuleSourceIPComponent(KIDSAddEventOracle ko, Set<IRI> currentSignalSet) throws KIDSOntologyDatatypeValuesException {
		super(ko, ko.getTBOXIRI() + "#IPSourceAddress_IPRangeSet",  currentSignalSet);
	}

}
