package net.strasnet.kids;

import java.util.Collection;
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
 * 
 * @author chrisstrasburg
 *
 * This class represents an IPPacket context in the knowledge base.
 * 
 *
	private Collection <AddAxiom> getICMPFeatureAxioms() {
		LinkedList<AddAxiom> axioms = new LinkedList<AddAxiom>();
		
		axioms.addAll(getFeature("#ICMPSignalDomain","#ICMPType"));
		axioms.addAll(getFeature("#ICMPSignalDomain","#ICMPCode"));
		axioms.addAll(getFeature("#ICMPSignalDomain","#ICMPChecksum"));
		
		return axioms;	}
	
 */

public class KIDSICMPPacketContext extends KIDSAbstractContext {
	
	public KIDSICMPPacketContext(OWLOntology o, IRI oIRI, OWLDataFactory f,
			OWLReasoner r) {
		super(o, oIRI, f, r);
	}

	/**
	 * Overridden from Abstract class.  Includes axioms for:
	 * - Adding the ICMPPacketContext as a subclass of NetworkPacketContext
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
		return IRI.create(myOIri + "#ICMPPacketContext");
	}
}
