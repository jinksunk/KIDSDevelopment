package net.strasnet.kids;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 * 
 * @author chrisstrasburg
 *
 * This class represents an IPPacket context in the knowledge base.
 * 
 * 
	private Collection <AddAxiom> getIPFeatureAxioms() {
		LinkedList<AddAxiom> axioms = new LinkedList<AddAxiom>();
		
		axioms.addAll(getFeature("#IPSignalDomain","#IPVersion"));
		axioms.addAll(getFeature("#IPSignalDomain","#IPHeaderLength"));
		axioms.addAll(getFeature("#IPSignalDomain","#IPDSCP"));
		axioms.addAll(getFeature("#IPSignalDomain","#IPECN"));
		axioms.addAll(getFeature("#IPSignalDomain","#IPTotalLength"));
		axioms.addAll(getFeature("#IPSignalDomain","#IPIdentification"));
		axioms.addAll(getFeature("#IPSignalDomain","#IPFlags"));
		axioms.addAll(getFeature("#IPSignalDomain","#IPFragmentOffset"));
		axioms.addAll(getFeature("#IPSignalDomain","#IPTimeToLive"));
		axioms.addAll(getFeature("#IPSignalDomain","#IPProtocolValue"));
		axioms.addAll(getFeature("#IPSignalDomain","#IPHeaderChecksum"));
		axioms.addAll(getFeature("#IPSignalDomain","#IPSourceAddress"));
		axioms.addAll(getFeature("#IPSignalDomain","#IPDestinationAddress"));
		axioms.addAll(getFeature("#IPSignalDomain","#IPOptions"));
		
		return axioms;
	}
 * 
 */

public class KIDSIPPacketContext extends KIDSAbstractContext {

	//TODO: Create feature classes for each of these features:
	/** Identified features for IPPackets */
	public final static String Version = "#IPVersion";
	public final static String SourceAddress = "#IPSourceAddress";
	public final static String DestinationAddress = "#IPDestinationAddress";
	public final static String Protocol = "#IPProtocolValue";
	
	/** Identified "meta" features for IPPackets */
	public final static String Within = "#IPPacketDataWithin";
	public final static String Nocase = "#IPPacketDataNocase";
	public final static String Distance = "#IPPacketDataDistance";
	public final static String Depth = "#IPPacketDataDepth";
	public final static String Offset = "#IPPacketDataOffset";
	
	/** Identified subContexts for IPPackets */
	public final static String TCPPacket = "#TCPPacketContext";
	public final static String UDPPacket = "#UDPPacketContext";
	public final static String ICMPPacket = "#ICMPPacketContext";
	
	public KIDSIPPacketContext(OWLOntology o, IRI oIRI, OWLDataFactory f,
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
		return IRI.create(myOIri + "#IPPacketContext");
	}
	
}
