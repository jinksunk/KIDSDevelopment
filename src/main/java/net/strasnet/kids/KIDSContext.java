package net.strasnet.kids;

import java.util.Collection;
import java.util.List;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

/**
 * 
 * @author chrisstrasburg
 *
 * Defines methods required for a KIDSContext.  These include:
 * 
 *  - public void addSubContext(KIDSContext k);
 */

public interface KIDSContext {
	
	/**
	 * Returns a list of all AddAxioms known for this context.
	 *  - Add for class in ontology
	 *  - Add for encapsulating context in ontology
	 *  - Also add axioms from parent context (if any)
	 */
	public Collection <AddAxiom> getAddAxioms(); // TODO: make static
	
	//TODO: Add method getIRI()
	/**
	 * @return the IRI of the context object:
	 */
	public IRI getIRI();
}
