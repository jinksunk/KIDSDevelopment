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
 * A feature is an observable characteristic of a context, interesting from a
 * detection standpoint.  Signals, defined as constraints on possible feature
 * values, require that a feature be able to express the signals defined for it in a
 * canonical form.  
 * 
 * A feature may have multiple constraints placed on it; the feature is responsible for
 * determining how to combine those constraints.
 * 
 * Each feature may have a different canonical form.  
 * 
 * Defines methods required for a KIDSFeature.  These include:
 * - addSignal    -- Adds the signal passed in to this feature; 
 * - getAddAxioms -- Should include the add axioms for each signal defined over
 * 					 this feature, as well as the objectProperty that each signal is
 * 					 associated with this feature.
 */

public interface KIDSFeature {
	
	/**
	 *
	 * @param signalDefinition - A feature-specific specification of a signal
	 * @return true if the signal is added, false otherwise
	 */
	public boolean setContext(KIDSContext c);

	/**
	 * 
	 * @return The context this feature is associated with.
	 */
	public KIDSContext getContext();
	
	/**
	 * @return a list of all AddAxioms for this feature.
	 * - Name of this feature concept
	 * - Context this feature is associated with (getIRI() ?)
	 */
	public Collection <AddAxiom> getAddAxioms();
	
	/**
	 * @return the IRI for this feature:
	 */
    public IRI getIRI();	
    
    /**
     * @return the instance IRI for this feature:
     */
    public IRI getInstanceIRI();
}
