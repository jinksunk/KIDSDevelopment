package net.strasnet.kids;

import java.util.Collection;
import java.util.LinkedList;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 * 
 * @author chrisstrasburg
 * This feature represents an IP protocol number.  This is one byte of the packet header.
 */
public class KIDSIPProtocolNumberFeature extends KIDSAbstractFeature implements KIDSFeature {

	public final static String parentDomain = "#IPSignalDomain";
	public static final String name = "#IPPacketProtocolNumberFeature";
	public static final String iName = "#IPPacketProtocolNumber";
	public KIDSContext myC = null;
	
	public KIDSIPProtocolNumberFeature(OWLOntology o, IRI oIRI, OWLDataFactory f,
			OWLReasoner r) {
		super(o,oIRI,f,r);
	}
	/**
	 * The axioms to be added.
	 */
	@Override
	public Collection<AddAxiom> getAddAxioms() {
		LinkedList<AddAxiom> c = new LinkedList<AddAxiom>();
		// Add the class assertion
		c.add(
				new AddAxiom(
						myO,
						myF.getOWLSubClassOfAxiom(
								myF.getOWLClass(getIRI()), 
								myF.getOWLClass(IRI.create(parentDomain))
								)
							)
				);
		
		// Also add the assertion that the domain hasSignalDomainContext myC:
		c.add(new AddAxiom(myO, myF.getOWLSubClassOfAxiom(
				myF.getOWLClass(getIRI()),
				myF.getOWLObjectSomeValuesFrom(
						myF.getOWLObjectProperty(IRI.create("#hasSignalDomainContext")),
						myF.getOWLClass(IRI.create("#IPPacketContext"))
						)
				)
				)
				);
		
		// Finally, add an individual as a member of this class:
		c.add(
				new AddAxiom(
						myO,
						myF.getOWLClassAssertionAxiom(myF.getOWLClass(getIRI()), 
								myF.getOWLNamedIndividual(getInstanceIRI()))));
		
		return c;
	}

	@Override
	public boolean setContext(KIDSContext c) {
		myC = c;
		return true;
	}

	@Override
	public KIDSContext getContext() {
		return myC;
	}

	@Override
	public IRI getIRI() {
		return IRI.create(name);
	}
	@Override
	public IRI getInstanceIRI() {
		return IRI.create(iName);
	}

}
