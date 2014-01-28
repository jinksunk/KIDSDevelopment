package net.strasnet.kids.datasources;

import java.util.Collection;
import java.util.LinkedList;

import net.strasnet.kids.KIDSAbstractFeature;
import net.strasnet.kids.KIDSFeature;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

public class KIDSSourceIPAddressFeature extends KIDSAbstractFeature implements KIDSFeature {

	public static final String name = "#IPPacketSourceAddressSignalDomain";
	public static final String iName = "#IPPacketSourceAddress";
	public static final String parentDomain = "#IPSignalDomain";
	
	public KIDSSourceIPAddressFeature(OWLOntology o, IRI oIRI,
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
				myF.getOWLObjectSomeValuesFrom(
						myF.getOWLObjectProperty(IRI.create("#hasSignalDomainContext")),
						myF.getOWLClass(IRI.create("#IPPacketContext"))
						)
				)
				)
				);
		
		return c;
	}

	@Override
	public IRI getIRI() {
		// TODO Auto-generated method stub
		return IRI.create(name);
	}

	@Override
	public IRI getInstanceIRI() {
		return IRI.create(iName);
	}

}
