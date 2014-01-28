package net.strasnet.kids;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

public class KIDSHTTPURIFeature extends KIDSAbstractFeature implements KIDSFeature {
	
	public final static String parentDomain = "#HTTPSignalDomain";
	public final static String name = "#HTTPURISignalDomain";
	public final static String iName = "#HTTPURI";
	public KIDSContext myC = null;
	
	public KIDSHTTPURIFeature(OWLOntology o, IRI oIRI, OWLDataFactory f,
			OWLReasoner r) {
		super(o, oIRI, f, r);
	}

    /**
     * Return a set of add axioms for this feature, including:
     * - Assertion that this feature is a subclass of HTTPSignalDomain
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
	public IRI getIRI() {
		return IRI.create(name);
	}


	@Override
	public KIDSContext getContext() {
		// TODO Auto-generated method stub
		return myC;
	}
	

	@Override
	public boolean setContext(KIDSContext c) {
		// TODO Auto-generated method stub
		myC = c;
		return true;
	}

	@Override
	public IRI getInstanceIRI() {
		// TODO Auto-generated method stub
		return IRI.create(iName);
	}
	
}
