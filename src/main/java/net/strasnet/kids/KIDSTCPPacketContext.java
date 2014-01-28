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
 * This class represents a TCPPacket context in the knowledge base.
 * 
 *
	private Collection <AddAxiom> getTCPFeatureAxioms() {
		LinkedList<AddAxiom> axioms = new LinkedList<AddAxiom>();
		
		axioms.addAll(getFeature("#TCPSignalDomain","#TCPSourcePort"));
		axioms.addAll(getFeature("#TCPSignalDomain","#TCPDestinationPort"));
		axioms.addAll(getFeature("#TCPSignalDomain","#TCPSequenceNumber"));
		axioms.addAll(getFeature("#TCPSignalDomain","#TCPAcknowledgementNumber"));
		axioms.addAll(getFeature("#TCPSignalDomain","#TCPDataOffset"));
		axioms.addAll(getFeature("#TCPSignalDomain","#TCPReservedBits"));
		axioms.addAll(getFeature("#TCPSignalDomain","#TCPNS"));
		axioms.addAll(getFeature("#TCPSignalDomain","#TCPCWR"));
		axioms.addAll(getFeature("#TCPSignalDomain","#TCPECE"));
		axioms.addAll(getFeature("#TCPSignalDomain","#TCPURG"));
		axioms.addAll(getFeature("#TCPSignalDomain","#TCPACK"));
		axioms.addAll(getFeature("#TCPSignalDomain","#TCPPSH"));
		axioms.addAll(getFeature("#TCPSignalDomain","#TCPRST"));
		axioms.addAll(getFeature("#TCPSignalDomain","#TCPSYN"));
		axioms.addAll(getFeature("#TCPSignalDomain","#TCPFIN"));
		axioms.addAll(getFeature("#TCPSignalDomain","#TCPWindowSize"));
		axioms.addAll(getFeature("#TCPSignalDomain","#TCPChecksum"));
		axioms.addAll(getFeature("#TCPSignalDomain","#TCPUrgentPointer"));
		axioms.addAll(getFeature("#TCPSignalDomain","#TCPOptions"));
		
		return axioms;
	}
 */

public class KIDSTCPPacketContext extends KIDSAbstractContext {

	public KIDSTCPPacketContext(OWLOntology o, IRI oIRI, OWLDataFactory f,
			OWLReasoner r) {
		super(o, oIRI, f, r);
	}

	/**
	 * Overridden from Abstract class.  Includes axioms for:
	 * - Adding the IPPacketContext as a subclass of NetworkPacketContext
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
		return IRI.create(myOIri + "#TCPPacketContext");
	}	
}
