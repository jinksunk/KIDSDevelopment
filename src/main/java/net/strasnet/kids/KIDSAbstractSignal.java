/**
 * 
 */
package net.strasnet.kids;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLObjectPropertyAtom;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLVariable;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 * @author chrisstrasburg
 *
 */
public abstract class KIDSAbstractSignal extends KIDSAbstractAxiom implements KIDSSignal {
	public static final String signalValueDataProperty = "#canonicalSignalValue";
	public static final String featureValueDataProperty = "#isValueInSignalDomain";
	KIDSFeature myFeature = null;
	String name = null;
	String representation = null;
	
	public KIDSAbstractSignal(OWLOntology o, IRI oIRI, OWLDataFactory f,
			OWLReasoner r) {
		super(o, oIRI, f, r);
	}

	/* (non-Javadoc)
	 * @see net.strasnet.kids.KIDSSignal#addDefinition(java.lang.Object)
	 */
	@Override
	public abstract void addDefinition(Object signalDefinition);

	/* (non-Javadoc)
	 * @see net.strasnet.kids.KIDSSignal#getAddAxioms()
	 */
	@Override
	public abstract Collection<AddAxiom> getAddAxioms();

	@Override
	public IRI getIRI() {
		return IRI.create(name);
	}

	public IRI getInstanceIRI(){
		return IRI.create(name + "_" + toString().hashCode());
	}

	@Override
	public void setFeature(KIDSFeature f) {
		myFeature = f;
		
	}

	@Override
	public KIDSFeature getFeature() {
		return myFeature;
	}
	
	AddAxiom canonicalRepresentationAxiom(){
		return new AddAxiom(myO,
				   myF.getOWLDataPropertyAssertionAxiom(
						   myF.getOWLDataProperty(IRI.create(signalValueDataProperty)), 
						   myF.getOWLNamedIndividual(getInstanceIRI()),
						   toString()
						   )
				    );
	}
	
	AddAxiom classMembershipAxiom(){
		return new AddAxiom(myO, 
				   myF.getOWLClassAssertionAxiom(
						   myF.getOWLClass(getIRI()), 
						   myF.getOWLNamedIndividual(getInstanceIRI())
						   )
					);

	}
	
	AddAxiom representationTypeAxiom(){
		return new AddAxiom(myO, 
				   myF.getOWLClassAssertionAxiom(
						   myF.getOWLClass(IRI.create(representation)), 
						   myF.getOWLNamedIndividual(getInstanceIRI())
						   )
					);

	}
	
	AddAxiom featureValuesAssociationRules(){
		OWLNamedIndividual me = myF.getOWLNamedIndividual(getInstanceIRI());
		OWLObjectProperty oop = myF.getOWLObjectProperty(IRI.create(featureValueDataProperty));
		
		return new AddAxiom(myO, myF.getOWLObjectPropertyAssertionAxiom(
						oop,
						me,
						myF.getOWLNamedIndividual(myFeature.getInstanceIRI())
						)
				);
				
		/*
		SWRLVariable var = myF.getSWRLVariable(IRI.create(myOIri + "#x"));
		SWRLVariable var2 = myF.getSWRLVariable(IRI.create(myOIri + "#y"));
		
		// Build the SWRL antecedent
		Set<SWRLAtom> antecedent = new HashSet<SWRLAtom>();
		
		antecedent.add(myF.getSWRLClassAtom(signalClass, var));
		antecedent.add(myF.getSWRLClassAtom(featureClass, var2));
		
		// Now, add the SWRL rule for this set of signals
		SWRLObjectPropertyAtom hasSigDomProperty = myF.getSWRLObjectPropertyAtom(
				myF.getOWLObjectProperty(IRI.create(myOIri + "#isValueInSignalDomain")), 
				var, 
				var2);
			
		SWRLRule sigFeatureRule = myF.getSWRLRule(antecedent, Collections.singleton(hasSigDomProperty));
		return new AddAxiom(myO, sigFeatureRule);
		*/
	}

}
