package net.strasnet.kids;

import java.util.Collection;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

public abstract class KIDSAbstractAxiom implements KIDSAxiom {

	protected OWLOntology myO;
	protected IRI myOIri;
	protected OWLDataFactory myF;
	protected OWLReasoner myR;
	
	/**
	 * 
	 * @param o - The OWLOntology from which this Axiom is formed.
	 * @param oIRI - The IRI reference to the ontology
	 * @param f - The OWLDataFactory for this Ontology.
	 */
	public KIDSAbstractAxiom (OWLOntology o, IRI oIRI, OWLDataFactory f, OWLReasoner r){
		myO = o;
		myOIri = oIRI;
		myF = f;
		myR = r;
	}
	
	/**
	 * Returns an axiom which represents this IDS.
	 */
	public abstract Collection<AddAxiom> getAddAxioms();

	public IRI getIRI() {
		// TODO Auto-generated method stub
		return null;
	}

}
