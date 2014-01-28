package net.strasnet.kids;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;


/**
 * Represents a protocol in snort.  Known protocols are:
 *   - ip
 *     - tcp
 *     - udp
 *     - icmp
 *   
 * A protocol is represented by a packet signal context in the ontology, and a snort rule which 
 *  specifies it also generates a protocolPacket as well as an instance of a signal (e.g. TCPProtocolValueSignal).
 * @author chrisstrasburg
 *
 */
public class KIDSProtocolAxiom extends KIDSAbstractAxiom implements KIDSAxiom {

	Collection<AddAxiom> axioms;
	
	public KIDSProtocolAxiom(OWLOntology o, IRI oIRI, OWLDataFactory f, OWLReasoner r, String protocol) {
		super(o, oIRI, f, r);
		
		axioms = new LinkedList<AddAxiom>();
		
		// Check to see if the IPPacketContext is present as an IPSignalDomainContext; if not, create an AddAxiom for it.
		OWLClass IPContext = myF.getOWLClass(IRI.create(myOIri + "#IPSignalDomainContext")); 
		OWLNamedIndividual IPPacketContext = myF.getOWLNamedIndividual(IRI.create(myOIri + "#IPPacketContext"));
//		Collection<OWLIndividual> individuals = IPContext.getIndividuals(myO);
	//	NodeSet<OWLNamedIndividual> ns = myR.getInstances(IPContext, true);
//		Iterator<OWLIndividual> i = individuals.iterator();
//		boolean found = false;
//		while (i.hasNext() && !found){
//			if (((OWLNamedIndividual)i.next()).equals(IPPacketContext)){
//			  found = true;
//			}
//		}
//		if (!found){
		axioms.addAll(getIPFeatureAxioms());
		axioms.add(new AddAxiom(myO, myF.getOWLClassAssertionAxiom(IPContext, IPPacketContext)));
//		}
		
		// If our protocol is one of tcp/udp/icmp, do the same for them.
		if (protocol.equalsIgnoreCase("tcp")){
			OWLNamedIndividual TCPPacketContext = myF.getOWLNamedIndividual(IRI.create(myOIri + "#TCPPacketContext"));
			OWLClass TCPContext = myF.getOWLClass(IRI.create(myOIri + "#TCPSignalDomainContext")); 

			// Add the class assertion
			axioms.add(new AddAxiom(myO, myF.getOWLClassAssertionAxiom(TCPContext, TCPPacketContext)));

			// Add the encapsulation assertion
			axioms.add(new AddAxiom(myO, myF.getOWLObjectPropertyAssertionAxiom(myF.getOWLObjectProperty(IRI.create(myOIri + "#hasEncapsulatedContext")), IPPacketContext, TCPPacketContext)));
			
			// Add the known features (signal domains)			
			axioms.addAll(getTCPFeatureAxioms());
			
		} else if (protocol.equalsIgnoreCase("udp")) {
			OWLNamedIndividual UDPPacketContext = myF.getOWLNamedIndividual(IRI.create(myOIri + "#UDPPacketContext"));
			OWLClass UDPContext = myF.getOWLClass(IRI.create(myOIri + "#UDPSignalDomainContext")); 

			axioms.add(new AddAxiom(myO, myF.getOWLClassAssertionAxiom(UDPContext, UDPPacketContext)));
			axioms.add(new AddAxiom(myO, myF.getOWLObjectPropertyAssertionAxiom(myF.getOWLObjectProperty(IRI.create(myOIri + "#hasEncapsulatedContext")), IPPacketContext, UDPPacketContext)));
			axioms.addAll(getUDPFeatureAxioms());
		} else if (protocol.equalsIgnoreCase("icmp")) {
			OWLNamedIndividual ICMPPacketContext = myF.getOWLNamedIndividual(IRI.create(myOIri + "#ICMPPacketContext"));
			OWLClass ICMPContext = myF.getOWLClass(IRI.create(myOIri + "#ICMPSignalDomainContext")); 

			axioms.add(new AddAxiom(myO, myF.getOWLClassAssertionAxiom(ICMPContext, ICMPPacketContext)));
			axioms.add(new AddAxiom(myO, myF.getOWLObjectPropertyAssertionAxiom(myF.getOWLObjectProperty(IRI.create(myOIri + "#hasEncapsulatedContext")), IPPacketContext, ICMPPacketContext)));
			axioms.addAll(getICMPFeatureAxioms());
		}
	}

	/**
	 * TCP Features include:
	 * <UL>
	 *   <LI> Source and Destination port numbers
	 *   <LI> Flags
	 *   <LI> Window
	 *   <LI> Sequence number
	 * </UL>
	 * @return
	 */
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
	
	/**
	 * UDP Features include:
	 * <UL>
	 *   <LI> Source and Destination port numbers
	 *   <LI> Flags
	 *   <LI> Window
	 *   <LI> Sequence number
	 * </UL>
	 * @return
	 */
	private Collection <AddAxiom> getUDPFeatureAxioms() {
		LinkedList<AddAxiom> axioms = new LinkedList<AddAxiom>();
		
		axioms.addAll(getFeature("#UDPSignalDomain","#UDPSourcePort"));
		axioms.addAll(getFeature("#UDPSignalDomain","#UDPDestinationPort"));
		axioms.addAll(getFeature("#UDPSignalDomain","#UDPPacketLength"));
		axioms.addAll(getFeature("#UDPSignalDomain","#UDPChecksum"));
		
		return axioms;
	}

	/**
	 * ICMP Features include:
	 * <UL>
	 *   <LI> Source and Destination port numbers
	 *   <LI> Flags
	 *   <LI> Window
	 *   <LI> Sequence number
	 * </UL>
	 * @return
	 */
	private Collection <AddAxiom> getICMPFeatureAxioms() {
		LinkedList<AddAxiom> axioms = new LinkedList<AddAxiom>();
		
		axioms.addAll(getFeature("#ICMPSignalDomain","#ICMPType"));
		axioms.addAll(getFeature("#ICMPSignalDomain","#ICMPCode"));
		axioms.addAll(getFeature("#ICMPSignalDomain","#ICMPChecksum"));
		
		return axioms;	}
	
	/**
	 * IP Features include:
	 * <UL>
	 *   <LI> Source and Destination port numbers
	 *   <LI> Flags
	 *   <LI> Window
	 *   <LI> Sequence number
	 * </UL>
	 * @return
	 */
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
	
	/**
	 * Given the name of an individual and the class it belongs to, check
	 * to see if they exist in the KB; if not, return the axioms to add them.
	 * @param individualName
	 * @param memberOfClass
	 * @return
	 */
	private Collection <AddAxiom> getFeature(String memberOfClass, String individualName){
		LinkedList<AddAxiom> axioms = new LinkedList<AddAxiom>();
		
		OWLNamedIndividual fInd = myF.getOWLNamedIndividual(IRI.create(myOIri + individualName));
		OWLClass fClass = myF.getOWLClass(IRI.create(myOIri + memberOfClass));
		
		axioms.add(new AddAxiom(myO, myF.getOWLClassAssertionAxiom(fClass,fInd)));
		
		return axioms;
	}
	
	@Override
	public Collection <AddAxiom> getAddAxioms() {
		return axioms;
	}

}
