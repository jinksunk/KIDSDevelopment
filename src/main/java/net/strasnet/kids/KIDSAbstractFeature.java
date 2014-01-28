package net.strasnet.kids;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 * 
 * @author chrisstrasburg
 *
 * Represents a signal context within the knowledge base.  A context has
 * the following properties:
 *   - Can be nested inside other contexts
 *   - Can produce axioms to add to the knowledge base
 */

public abstract class KIDSAbstractFeature extends KIDSAbstractAxiom implements KIDSAxiom, KIDSFeature {
	
	public String name = null;
	public String iName = null;
	private KIDSContext myC = null;
	
	public KIDSAbstractFeature(OWLOntology o, IRI oIRI, OWLDataFactory f,
			OWLReasoner r) {
		super(o, oIRI, f, r);
	}

	@Override
	public abstract IRI getIRI();

	@Override
	public abstract IRI getInstanceIRI();

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
	public abstract Collection<AddAxiom> getAddAxioms();
	
}
