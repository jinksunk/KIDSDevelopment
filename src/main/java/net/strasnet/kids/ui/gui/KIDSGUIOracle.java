/**
 * 
 */
package net.strasnet.kids.ui.gui;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import net.strasnet.kids.KIDSOracle;

/**
 * @author Chris Strasburg
 * 
 * This class extends the KIDSOracle with methods to support the use of a GUI for 
 * ontology modification.
 *
 */
public class KIDSGUIOracle extends KIDSOracle {
	
	/**
	 * Adds an event to the ontology, to the correct subclass
	 * @param eventIRI
	 */
	public void addEvent(IRI eventIRI){
		OWLClass evtClass = this.odf.getOWLClass(eventClass);
		OWLNamedIndividual event = this.odf.getOWLNamedIndividual(eventIRI);
		OWLAxiom toAdd = this.odf.getOWLClassAssertionAxiom(evtClass, event);
		
		this.manager.applyChanges(
		    this.manager.addAxiom(this.o, toAdd)
		);
		r.flush();
	}

}
