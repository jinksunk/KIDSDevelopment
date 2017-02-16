/**
 * 
 */
package net.strasnet.kids.gui;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.ComboBoxModel;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;

import net.strasnet.kids.KIDSCanonicalRepresentation;
import net.strasnet.kids.KIDSCanonicalRepresentationFactory;
import net.strasnet.kids.KIDSOntologyObjectValuesException;
import net.strasnet.kids.KIDSOracle;
import net.strasnet.kids.KIDSSyntacticFormGenerator;
import net.strasnet.kids.KIDSSyntacticFormGeneratorFactory;

/**
 * @author chrisstrasburg
 *
 * Add methods to interface with the knowledge base that support describing events.  These include:
 *  1) createNewEvent - get an IRI reference to a new event individual
 *  2) addSignalToEvent - add a signal to the event individual
 *  3) addDomainToSignal - associates a signal with a new signal domain
 *  4) addContextToDomain - associates a signal domain to a new context
 *  
 */
public class KIDSAddEventOracle extends KIDSOracle {

	private IRI currentEventIRI;
	
	/**
	 * Assume that the text string is a simple name of an event.  
	 * @param text
	 */
	public void setEventIRI(String text) {
		currentEventIRI = IRI.create("#" + text);

		manager.addAxiom(o,
				odf.getOWLClassAssertionAxiom(
						odf.getOWLClass(IRI.create(ourIRI.toString() + "#Event")), 
						odf.getOWLNamedIndividual(IRI.create(ourIRI.toString() + currentEventIRI))
				)				
		);
		
		// Update the reasoner
		updateReasoner();
	}

	/**
	 * Returns a list of signals associated with this event in the current knowledge base.
	 * @return
	 */
	public java.util.List<OWLNamedIndividual> getEventSignals() {
		List<OWLNamedIndividual> toReturn = new LinkedList<OWLNamedIndividual>();
		
		// Make sure any pending reasoning queries are processed:
		r.flush();
		r.precomputeInferences();
		
		// Query the knowledge base for all signals produced by this event:
		toReturn.addAll(r.getObjectPropertyValues(
				odf.getOWLNamedIndividual(IRI.create(ourIRI.toString() + currentEventIRI.toString())),
				odf.getOWLObjectProperty(IRI.create(ourIRI.toString() + "#isProducerOf"))).getFlattened());
		return toReturn;
	}

	/**
	 * Returns a list of all signal subclasses in the knowledge base:
	 * @return
	 */
	public List<OWLClass> getSignalSubClasses() {
		List<OWLClass> toReturn = new LinkedList<OWLClass>();
		OWLClass parentC = odf.getOWLClass(IRI.create(ourIRI.toString() + "#Signal"));
		toReturn.addAll(
				r.getSubClasses(
						parentC, 
						false).getFlattened()
		);
		return toReturn;
	}
	
	/**
	 * Returns a list of all signal subclassess in the knowledge base:
	 * @return
	 */
	public List<OWLNamedIndividual> getSignalRepresentations() {
		List<OWLNamedIndividual> toReturn = new LinkedList<OWLNamedIndividual>();
		toReturn.addAll(
				r.getInstances(
						odf.getOWLClass(IRI.create(ourIRI.toString() + "#SignalCanonicalRepresentation")), 
						false).getFlattened()
		);
		return toReturn;
	}

	/**
	 * Return a list of all individuals which are members of (a subclass of) signalDomain
	 * @return
	 */
	public List<OWLNamedIndividual> getSignalDomains() {
		r.flush();
		List<OWLNamedIndividual> toReturn = new LinkedList<OWLNamedIndividual>();
		toReturn.addAll(
				r.getInstances(
						odf.getOWLClass(IRI.create(ourIRI.toString() + "#SignalDomain")), 
						false).getFlattened()
		);
		return toReturn;
	}
	
	/** Given the signal canonical representation, signal domain, and
	 * the canonical form, add it to the KB.
	 * @param ourRep 
	 * @param sd 
	 * @param scr 
	 */
	public void addSignalToEvent(OWLNamedIndividual ourRep,
			OWLNamedIndividual sd, KIDSCanonicalRepresentation scr) {

		// Get the property isProducedBy for the signal:
		OWLObjectProperty producedBy = odf.getOWLObjectProperty(IRI.create(ourIRI.toString() + evtSignalRelation));
		
		// Get the object property hasCanonicalRepresentation
		OWLObjectProperty hasRep = odf.getOWLObjectProperty(IRI.create(ourIRI.toString() + signalRepRelation));
		
		// Get the asserted class of this representation
	    OWLClass repClass = null; 
	    for (OWLClass c : r.getTypes(ourRep, true).getFlattened()) { 
	        repClass = c;
	    }
	    
	    // Generate the signal class:
		String sClassName = sd.getIRI().getFragment() + "_" + repClass.getIRI().getFragment();
		OWLClass ourClass = odf.getOWLClass(IRI.create(ourIRI.toString() + '#' + sClassName));
		
		// Generate the equivalent class: Signal ^ hasCanonicalRepresentaton some repClass ^ isValueInSignalDomain sd:
		OWLClass sigClass = odf.getOWLClass(IRI.create(ourIRI.toString() + signalClass.toString()));
		OWLClassExpression isValueClass = odf.getOWLObjectHasValue(odf.getOWLObjectProperty(signalDomainObjProp), sd);
		
		// Define the class equivalence:
		OWLEquivalentClassesAxiom eqClass = odf.getOWLEquivalentClassesAxiom(ourClass, 
										odf.getOWLObjectIntersectionOf(
												sigClass, 
												odf.getOWLObjectSomeValuesFrom(odf.getOWLObjectProperty(signalRepRelation), repClass),
												isValueClass)
											);
		
		// Get the signal individual:
		OWLNamedIndividual sigIndividual = odf.getOWLNamedIndividual(IRI.create(ourClass.getIRI().toString() + "_" + scr.getNameForm()));
		
		// Add the signal to the event:
		manager.addAxiom(o, odf.getOWLClassAssertionAxiom(ourClass, sigIndividual));
		
		// Add relationship between signal and representation:
		manager.addAxiom(o, odf.getOWLObjectPropertyAssertionAxiom(hasRep, sigIndividual, ourRep));
		
		// Add value of signal
		manager.addAxiom(o, odf.getOWLDataPropertyAssertionAxiom(odf.getOWLDataProperty(IRI.create(ourIRI.toString() + signalValueDataProp)), sigIndividual, odf.getOWLLiteral(scr.getCanonicalForm())));
		
		// Add signal to event
		manager.addAxiom(o, odf.getOWLObjectPropertyAssertionAxiom(producedBy, sigIndividual, odf.getOWLNamedIndividual(IRI.create(ourIRI.toString() + currentEventIRI))));
		
		// Add signal to the signal domain
		manager.addAxiom(o, odf.getOWLObjectPropertyAssertionAxiom(odf.getOWLObjectProperty(IRI.create(ourIRI.toString() + signalDomainObjProp)), sigIndividual, sd));
		
		// Add equivalent class definition:
		manager.addAxiom(o, eqClass);
		
		// Finally, apply changes:
		//manager.applyChanges(ocl);
	}

	/**
	 * 
	 * @param featureName - the name of the feature to add; will convert to an IRI and add it.
	 * @param selectedContexts - A List of OWLNamedIndividuals which are associated with the feature.
	 */
	public void addFeatureToKB(OWLNamedIndividual feature,
				OWLClass fClass,
			List<OWLNamedIndividual> selectedContexts) {
		
		OWLObjectProperty fcop = odf.getOWLObjectProperty(IRI.create(ourIRI.toString() + featureContextObjProp));
		
		// Add the feature as a member of its class:
		manager.addAxiom(o, odf.getOWLClassAssertionAxiom(fClass, feature));

		// Add the object property assertions:
		for (int i = 0; i < selectedContexts.size(); i++){
			manager.addAxiom(o, odf.getOWLObjectPropertyAssertionAxiom(fcop, feature, selectedContexts.get(i)));
		}
		
		//manager.applyChanges(ocl);
	}

	public void addContextToKB(OWLNamedIndividual owlNamedIndividual,
			OWLClass selectedContextClass) {
		OWLClassAssertionAxiom toAdd = odf.getOWLClassAssertionAxiom(selectedContextClass, owlNamedIndividual);
		manager.addAxiom(o, toAdd);
	}

	public List<OWLNamedIndividual> getSignalDomainContexts() {
		r.flush();
		List<OWLNamedIndividual> toReturn = new LinkedList<OWLNamedIndividual>();
		toReturn.addAll(
				r.getInstances(
						odf.getOWLClass(IRI.create(ourIRI.toString() + "#SignalDomainContext")), 
						false).getFlattened()
		);
		return toReturn;
	}

	/**
	 * 
	 * @return A list of <OWLClass>es for all of the known contexts.
	 */
	public List<OWLClass> getSignalDomainContextClasses() {
		r.flush();
		List<OWLClass> toReturn = new LinkedList<OWLClass>();
		OWLClass parentC = odf.getOWLClass(IRI.create(ourIRI.toString() + "#SignalDomainContext"));
		toReturn.addAll(
				r.getSubClasses(
						parentC, 
						false).getFlattened()
		);
		return toReturn;
	}

	/**
	 * Get the IDS individuals in the KB:
	 * @return
	 */
	public List<OWLNamedIndividual> getIDSes() {
		r.flush();
		List<OWLNamedIndividual> toReturn = new LinkedList<OWLNamedIndividual>();
		toReturn.addAll(
				r.getInstances(
						odf.getOWLClass(IRI.create(ourIRI.toString() + "#IDS")), 
						false).getFlattened()
		);
		return toReturn;	}

	/**
	 * Given an IDS, return the list of detectors associated with it:
	 */
	public List<OWLNamedIndividual> getDetectors(OWLNamedIndividual ids){
		NodeSet<OWLNamedIndividual> sForms = r.getObjectPropertyValues(ids, odf.getOWLObjectProperty(IRI.create(ourIRI.toString() + idsDetectorObjProp)));
		return new LinkedList<OWLNamedIndividual>(sForms.getFlattened());
	}
	
	/**
	 * Given an IDS, return the list of syntactic forms associated with it:
	 */
	public List<OWLNamedIndividual> getSynforms(OWLNamedIndividual ids){
		NodeSet<OWLNamedIndividual> sForms = r.getObjectPropertyValues(ids, odf.getOWLObjectProperty(IRI.create(ourIRI.toString() + idsSynformObjProp)));
		return new LinkedList<OWLNamedIndividual>(sForms.getFlattened());
	}
	
	/**
	 * Given a detector, return the syntactic form of the event under consideration.
	 * @param cf - the current form requested.
	 * @return The syntactic form of the current event, projected onto the contexts the Detector can monitor.
	 */
	/*public String getSyntacticForm(OWLNamedIndividual cf) {
		// Get the syntactic form for the given Detector:
		
		// Get the eventSyntacticFormGeneratorImplementation property for this form
		Set<OWLLiteral> sfClass = r.getDataPropertyValues(cf, odf.getOWLDataProperty(IRI.create(ourIRI.toString() + syntacticFormDataProperty)));
		String sfGenerator = sfClass.iterator().next().getLiteral();
		
		// Instantiate the generator, and produce the syntactic form for this event:
		 KIDSSyntacticFormGenerator g = KIDSSyntacticFormGeneratorFactory.createGenerator(sfGenerator);
		 g.setOracle(this);
		 //g.setCurrentEvent(odf.getOWLNamedIndividual(currentEventIRI));

		 return g.getSyntacticForm();
	}*/

	public OWLNamedIndividual[] getEvents() {
		List<OWLNamedIndividual> toReturn = new LinkedList<OWLNamedIndividual>();

		NodeSet<OWLNamedIndividual> sForms = r.getInstances(odf.getOWLClass(IRI.create(ourIRI.toString() + eventClass)), false);
		toReturn.addAll(sForms.getFlattened());
		OWLNamedIndividual[] toRAy = new OWLNamedIndividual[toReturn.size()];
		
		return toReturn.toArray(toRAy);
	}

	/**
	 * Given an array of individuals, return the short form (that following the '#', and without the enclosing '<..>') for the individuals. 
	 * @param individuals
	 * @return String[] with the same length as the input array
	 */
	public String[] getShortNames(OWLNamedIndividual[] individuals) {
		String[] toReturn = new String[individuals.length];
		for (int i = 0; i < individuals.length; i++){
			toReturn[i] = getShortName(individuals[i]);
		}
		return toReturn;
	}
	
	/**
	 * Return the short form of the name (that following the '#', and without the enclosing '<..>') of the given individual 
	 * @param i
	 */
	public String getShortName(OWLNamedIndividual i){
		String iri = i.getIRI().getFragment();
		return iri;
	}

	/**
	 * Given a short name, return an OWLNamedIndividual from the current namespace.
	 * @param selectedValue
	 * @return
	 */
	public OWLNamedIndividual getOWLNamedIndividualFromShortname(
			String selectedValue) {
		return odf.getOWLNamedIndividual(IRI.create(ourIRI.toString() + "#" + selectedValue));
	}

	/**
	 * Return the set of signals which are both produced by the Event 'e' and which 'isAppliedByDetector' detector.
	 * @param event
	 * @param detector
	 * @return
	 */
	public Set<IRI> getSignalsFromEventAndDetector(
			OWLNamedIndividual event, IRI detector) {
		r.getInstances(
				odf.getOWLObjectIntersectionOf(
						odf.getOWLObjectHasValue(
								odf.getOWLObjectProperty(IRI.create(ourIRI.toString() + KIDSOracle.evtSignalRelation)), 
								event
							),
						odf.getOWLObjectHasValue(
								odf.getOWLObjectProperty(IRI.create(ourIRI.toString() + KIDSOracle.detectorSignalRelation)), 
								event
							)
							),
				false);
		
		return null;
	}

	/**
	 * Returns the set of OWLNamedIndividuals which are both represented in the currentSigSet as well as a member of 
	 * provided class.
	 * @param currentSigSet
	 * @param create
	 * @return
	 */
	public Set<IRI> getIndividualsFromSetInClass(
			Set<IRI> individualsToSelect, IRI memberClass) {
		// For each element of the set, see if it is a member of the class:
		Iterator<IRI> i = individualsToSelect.iterator();
		OWLClass memberC = odf.getOWLClass(memberClass);
		Set<IRI> returnSet = new HashSet<IRI>();
		while (i.hasNext()){
			IRI candidate = i.next();
			NodeSet<OWLClass> classes = r.getTypes(odf.getOWLNamedIndividual(candidate), false);
			if (classes.containsEntity(memberC)){
				returnSet.add(candidate);
			}
		}
		
		return returnSet;
	}

	/**
	 * 
	 * @param svclass - The class of signal values considered compatible by the requester.
	 * @return A SignalValue object which is compatible, e.g. is a member of the given class
	 */
	public OWLNamedIndividual getCompatibleSignalValue(IRI svclass) {
	    OWLNamedIndividual returnValue = null;
	    Set<OWLNamedIndividual> ourBoys = r.getInstances(odf.getOWLClass(svclass), false).getFlattened();
	    Iterator<OWLNamedIndividual> obi = ourBoys.iterator();
	    while (obi.hasNext()){
	    	return obi.next();
	    }
		return null;
	}

	/**
	 *  This method assumes that a signal belongs to a strict hierarchy of classes
	 * @param iri - The signal for which we are getting the class heirarchy
	 * @return - A list of the signal classes, most specific to least specific, that this individual belongs to.
	 * @throws KIDSOntologyObjectValuesException 
	 */
	public List<IRI> getSignalClasses(IRI iri) throws KIDSOntologyObjectValuesException {
		List<IRI> toReturn = new LinkedList<IRI>();
		
		// First, get the base class (explicitly declared) this signal is a member of, then get all parent classes in order
		Set<OWLClass> baseClassSet = r.getTypes(odf.getOWLNamedIndividual(iri), true).getFlattened();
		if (baseClassSet.size() != 1){
			// This shouldn't happen - more than one declared class?
			throw new KIDSOntologyObjectValuesException("More than one base class declared for signal " + iri);
		}
		Iterator<OWLClass> classIter = baseClassSet.iterator();
		OWLClass currentClass = classIter.next();
		toReturn.add(currentClass.getIRI());
		
		Set<OWLClass> parentClassSet = r.getSuperClasses(currentClass, true).getFlattened();
		while(!parentClassSet.isEmpty()){
			if (parentClassSet.size() != 1){
				throw new KIDSOntologyObjectValuesException("More than one base class declared for signal " + iri);
			}
			classIter = parentClassSet.iterator();
			currentClass = classIter.next();
			toReturn.add(currentClass.getIRI());
			parentClassSet = r.getSuperClasses(currentClass, true).getFlattened();
		}
		
		return toReturn;
	}


}
