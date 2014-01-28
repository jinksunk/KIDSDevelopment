/**
 * 
 */
package net.strasnet.kids.datasources;

import net.strasnet.kids.KIDSDataSignal;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 * @author chrisstrasburg
 *
 */
public class KIDSSnortURIContentDefinition extends KIDSDataSignal {

	public KIDSSnortURIContentDefinition(OWLOntology o, IRI oIRI,
			OWLDataFactory f, OWLReasoner r) {
		super(o, oIRI, f, r);
	}

}
