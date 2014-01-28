/**
 * 
 */
package net.strasnet.kids;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 * @author chrisstrasburg
 *
 */
public class KIDSHTTPPacketContext extends KIDSAbstractContext implements
		KIDSAxiom {

	/** Identified features for HTTPPackets */

	public KIDSHTTPPacketContext(OWLOntology o, IRI oIRI, OWLDataFactory f,
			OWLReasoner r) {
		super(o, oIRI, f, r);
	}

	/**
	 * Overridden from Abstract class.  Includes axioms for:
	 * - Adding the HTTPPacketContext as a subclass of NetworkPacketContext
	 * 
	 */
	@Override
	public Collection<AddAxiom> getAddAxioms() {
		// Add an axiom to identify this context
		LinkedList<AddAxiom> axList = new LinkedList<AddAxiom>();
		axList.add(new AddAxiom( myO,
				myF.getOWLSubClassOfAxiom(
						myF.getOWLClass(
								IRI.create("#NetworkPacketContext")
								), myF.getOWLClass(getIRI()))
				)
				);
		return axList;
	}

	@Override
	public IRI getIRI() {
		// Auto-generated method stub
		return IRI.create(myOIri + "#HTTPPacketContext");
	}
}
