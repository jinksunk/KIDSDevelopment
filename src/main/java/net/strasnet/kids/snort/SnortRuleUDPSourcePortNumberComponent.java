package net.strasnet.kids.snort;

import java.util.Set;

import net.strasnet.kids.detectorsyntaxproducers.KIDSIncompatibleSyntaxException;
import net.strasnet.kids.gui.KIDSAddEventOracle;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

public class SnortRuleUDPSourcePortNumberComponent extends SnortRulePortNumberComponent {

	public SnortRuleUDPSourcePortNumberComponent(KIDSAddEventOracle ko, Set<IRI> currentSignalSet) throws KIDSIncompatibleSyntaxException {
		super(ko, currentSignalSet, "#UDPSourcePort_IntegerRangeSet");
	}

}
