package net.strasnet.kids;

import java.util.Collection;

import org.semanticweb.owlapi.model.AddAxiom;

public interface KIDSAxiom {

	/**
	 * @return An axiom suitable for addition into the ontology.
	 */
	public Collection<AddAxiom> getAddAxioms();
	
}
