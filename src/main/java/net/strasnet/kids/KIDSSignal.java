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
 * A signal is a constraint on a feature value.  While a signal may be specified in many
 * ways, a canonical form is required to ensure comparability with other signals.
 * 
 * A signal object is responsible for reading in specifications, generating its canonical form, 
 * and producing axioms for the knowledge base to capture its original specification and a
 * human-readable version of the canonical form.  A signal must also be able to produce a
 * unique identifier (based on its canonical form).
 *  
 * 
 * Defines methods required for a KIDSFeature.  These include:
 * - addDefinition -- Adds a definition component to this signal  
 * - getAddAxioms -- Should include the add axioms to specify the signal ID as well
 * 				     as annotation axioms with a human-readable version of the
 *                   original specification and the canonical form.
 * 
 */

public interface KIDSSignal {
	
	/**
	 * 
	 * @param signalDefinition - A specification of a signal definition component
	 */
	public void addDefinition(Object signalDefinition);
		
	/**
	 * @return a list of all AddAxioms for this signal, including:
	 * - objectProperty relating this signal to a feature
	 * - annotation describing the original specification as well as a
	 *   canonical form
	 */
	public Collection <AddAxiom> getAddAxioms();
	
	public IRI getIRI();
	
	public void setFeature(KIDSFeature f);
	
	public KIDSFeature getFeature();

	public IRI getInstanceIRI();
}
