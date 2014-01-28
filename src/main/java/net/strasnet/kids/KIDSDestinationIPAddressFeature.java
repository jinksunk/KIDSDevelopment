package net.strasnet.kids;

import java.util.Collection;
import java.util.LinkedList;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

public class KIDSDestinationIPAddressFeature extends KIDSAbstractAxiom implements KIDSFeature {

	public static final String name = "#KIDSDestinationIPAddressSignalDomain";
	public static final String iName = "#KIDSDestinationIPAddress";
	public static final String parentDomain = "#KIDSIPAddressSignalDomain";
	public KIDSContext myC = null;
	
	public KIDSDestinationIPAddressFeature(OWLOntology o, IRI oIRI,
			OWLDataFactory f, OWLReasoner r) {
		super(o, oIRI, f, r);
	}

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
				myF.getOWLObjectAllValuesFrom(
						myF.getOWLObjectProperty(IRI.create("#isContextOfSignalDomain")),
						myF.getOWLClass(myC.getIRI())
						)
				)
				)
				);
		
		return c;
	}

	@Override
	public boolean setContext(KIDSContext c) {
		myC = c;
		return true;
	}

	@Override
	public IRI getIRI() {
		return IRI.create(name);
	}

	@Override
	public KIDSContext getContext() {
		return myC;
	}

	@Override
	public IRI getInstanceIRI() {
		return IRI.create(iName);
	}

}
