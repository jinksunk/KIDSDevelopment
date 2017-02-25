/**
 * 
 */
package net.strasnet.kids.ui.gui;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.reasoner.NodeSet;

import net.strasnet.kids.KIDSOracle;

/**
 * @author Chris Strasburg
 * 
 * This class extends the KIDSOracle with methods to support the use of a GUI for 
 * ontology modification.
 *
 */
public class KIDSGUIOracle extends KIDSOracle {

	public static final org.apache.log4j.Logger logme = LogManager.getLogger(KIDSGUIOracle.class.getName());
	final String tboxpref = "kids:";
	final String aboxpref = "abox:";
	
	Map<String, String> prefMap = null;
	
	/**
	 * 
	 * @return - The set of IRIs representing all known events in the ontology.
	 */
	public Set<IRI> getKnownEvents() {
		OWLClass evtClass = this.odf.getOWLClass(eventClass);
		Set<OWLNamedIndividual> events = this.getIndividualSet(evtClass);
		logme.debug(String.format("Oracle returned %d elements of class %s", events.size(), eventClass.toString()));
		return getIRISetFromNamedIndividualSet(events);
	}

	/**
	 *  Returns a set of IRIs indicating the individuals which are objects of the given
	 *  subject and predicate, and which also belong to the given class.
	 * @param myIRI The subject under discussion.
	 * @param property The predicate being evaluated
	 * @param objectClass The class by which to filter candidate objects.
	 * @return The set of individuals that are objects of the given property for the given subject, and
	 *         that belong to the given objectClass.
	 */
	public Set<IRI> getPropertyIndividualsOfClass(IRI myIRI, IRI property, IRI objectClass) {
		Set<IRI> toReturn = new HashSet<IRI>();

		// First, get all the individuals that satisfy the property:
		OWLObjectProperty rproperty = odf.getOWLObjectProperty(property);
		Set<OWLNamedIndividual> valueSet = r.getObjectPropertyValues(odf.getOWLNamedIndividual(myIRI), rproperty).getFlattened();
		
		// Then, make sure at least one is a member of the given object class:
		OWLClass ooc = odf.getOWLClass(objectClass);
		Set<OWLNamedIndividual> classMemberSet = r.getInstances(ooc, false).getFlattened();
		
		for (OWLNamedIndividual candidate : valueSet){
			if (classMemberSet.contains(candidate)){
				toReturn.add(candidate.getIRI());
			}
		}

		return toReturn;
	}

	/**
	 * The set of data property values for the given subject and property.
	 * @param myIRI The subject under discussion.
	 * @param property The data property.
	 * @return A set of String representations of the data property values.
	 */
	public Set<String> getDataPropertyValues(IRI myIRI, IRI property) {
		Set<String> toReturn = new HashSet<String>();
		
		OWLDataProperty dprop = odf.getOWLDataProperty(property);
		Set<OWLLiteral> dvals = r.getDataPropertyValues(odf.getOWLNamedIndividual(myIRI), dprop);
		toReturn.addAll(getStringSetFromOWLLiterals(dvals));
		
		return toReturn;
	}

	/**
	 * A utility function to return a set of strings representing the values given by this set of literals.
	 * @param dvals The set of literal values to convert to strings.
	 * @return - A set of string representations of the literal values
	 */
	public Set <String> getStringSetFromOWLLiterals(Set<OWLLiteral> dvals) {
		Set<String> toReturn = new HashSet<String>();
		for (OWLLiteral l : dvals){
			toReturn.add(l.getLiteral());
		}
		return toReturn;
	}

	/**
	 * Simply adds a triple to the ontology.
	 * 
	 * @param subject
	 * @param predicate
	 * @param object
	 */
	public void addRelation(IRI subject, IRI predicate, IRI object) {
		
		logme.debug(String.format("Adding statement (%s, %s, %s).", subject, predicate, object));
		OWLObjectProperty ourprop = this.odf.getOWLObjectProperty(predicate);
		OWLNamedIndividual oursubject = this.odf.getOWLNamedIndividual(subject);
		OWLNamedIndividual ourobject = this.odf.getOWLNamedIndividual(object);
		OWLObjectPropertyAssertionAxiom axiom = this.odf.getOWLObjectPropertyAssertionAxiom(ourprop, oursubject, ourobject);
		
		this.manager.addAxiom(o, axiom);

		logme.debug(String.format("Before flush, reasoner has %d pending changes.", this.r.getPendingChanges().size()));
		this.r.precomputeInferences();
		this.r.flush();
		logme.debug(String.format("AFter flush, reasoner has %d pending changes.", this.r.getPendingChanges().size()));
	}

	/**
	 * 
	 * @param subject
	 * @return True if the individual is known to the ontology, false otherwise.
	 */
	public boolean containsIndividual(IRI individual) {
		OWLNamedIndividual itest = odf.getOWLNamedIndividual(individual);
		Set<OWLNamedIndividual> samset = r.getSameIndividuals(itest).getEntities();
		logme.debug(String.format("Ontology contains %d same individuals from %s",samset.size(),individual));
		return (samset.size() > 0);
	}

	/**
	 * 
	 * @param individual
	 * @return The set of known types for the individual in question:
	 */
	public Set<IRI> getTypesForIndividual(IRI individual){
		OWLNamedIndividual oni = odf.getOWLNamedIndividual(individual);
		Set<OWLClass> c = r.getTypes(oni, false).getFlattened();
		Set<IRI> classSet = new HashSet<IRI>();
		for (OWLClass oce : c){
			classSet.add(oce.getClassesInSignature().iterator().next().getIRI());
		}
		return classSet;
	}

	/**
	 * Will return * true if the given individual is a member of a strict subclass of the given class,
	 * false otherwise.
	 * 
	 * @param parentClass - The class to use as the base parent class
	 * @param individualToCheck - The individual to check
	 * 
	 * @return true if the given individual is a member of a strict subclass of the given class,
	 * false otherwise.
	 */
	public boolean isMemberOfStrictSubclass(IRI parentClass, IRI individualToCheck) {
		OWLNamedIndividual ind = odf.getOWLNamedIndividual(individualToCheck);
		OWLClass cls = odf.getOWLClass(parentClass);
		
		Set<OWLClass> membership = r.getTypes(ind, false).getFlattened();
		Set<OWLClass> subclasses = r.getSubClasses(cls, false).getFlattened();
		
		logme.debug(String.format("Found %d subclasses of %s, and %d class memberships for individual %s.",
				subclasses.size(), parentClass, membership.size(), individualToCheck));
		
		for (OWLClass c : membership){
			if (subclasses.contains(c)){
				logme.debug(String.format("Found subclass %s for individual %s", c.getIRI(), individualToCheck));
				return true;
			}
		}
		
		return false;
		
	}

	/**
	 * Returns the set of classes which are identified as sublcasses of the given parent class
	 * @param parentClass - the root from which to get subclasses
	 * @return the set of classes which are identified as sublcasses of the given parent class
	 */
	public Set<IRI> getStrictSubclasses(IRI parentClass) {
		OWLClass cls = odf.getOWLClass(parentClass);
		
		Set<OWLClass> subclasses = r.getSubClasses(cls, false).getFlattened();
		
		logme.debug(String.format("Found %d subclasses of parent class %s.", subclasses.size(), parentClass));

		return this.getIRISetFromNamedIndividualSet(subclasses);
	}

	/**
	 * Adds an event to the ontology, to the correct subclass
	 * @param eventIRI
	 */
	public void addIndividual(IRI indIRI, IRI classIRI) {
		OWLClass indClass = this.odf.getOWLClass(classIRI);
		OWLNamedIndividual ind = this.odf.getOWLNamedIndividual(indIRI);
		OWLAxiom toAdd = this.odf.getOWLClassAssertionAxiom(indClass, ind);
		
		this.manager.addAxiom(this.o, toAdd);
		r.flush();
		
		logme.debug(String.format("Added individual %s to class %s.", indIRI, classIRI));
	}

	public void addDataPropertyToIndividual(IRI subjectIRI, IRI relation, String value) {
		OWLNamedIndividual oni = odf.getOWLNamedIndividual(subjectIRI);
		OWLDataProperty odp = odf.getOWLDataProperty(relation);
		
		this.manager.addAxiom(o, 
				this.odf.getOWLDataPropertyAssertionAxiom(
						odp, 
						oni, 
						value));

		logme.debug(String.format("Added tuple (%s, %s, %s)", subjectIRI, relation, value));
		r.flush();
		
	}

	/**
	 * 
	 * @param myIRI - The IRI of a Named Component for which we would like a
	 *                short form (if possible).
	 * @return A string of the form <prefix>:<fragment> if a prefix exists, or
	 *         IRI.toString() if not.
	 */
	public String getShortIRIString(IRI myIRI) {
		if (this.prefMap == null){
			this.prefMap = new HashMap<String, String>();
			this.prefMap.put(this.TBOXIRI.toString() + "#", this.tboxpref);
			this.prefMap.put(this.getABOXIRI().toString() + "#", this.aboxpref);
		}
		if (this.prefMap.containsKey(myIRI.getNamespace())){
			logme.debug(String.format("Found prefix match for namespace: %s",myIRI.getNamespace()));
			return this.prefMap.get(myIRI.getNamespace()) + myIRI.getShortForm();
		} else {
			logme.debug(String.format("No prefix match found for namespace: %s",myIRI.getNamespace()));
			return myIRI.toString();
		}
	}
}
