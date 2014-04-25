/**
 * 
 */
package net.strasnet.kids;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.strasnet.kids.signalRepresentations.KIDSRepresentationInvalidRepresentationValueException;

import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.reasoner.ConsoleProgressMonitor;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.reasoner.impl.DefaultNodeSet;
import org.semanticweb.owlapi.reasoner.impl.OWLNamedIndividualNodeSet;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

/**
 * @author chrisstrasburg
 *
 * Support Knowledge-based IDS by supporting complex ontology queries.
 */
public class KIDSOracle {

	protected IRI ourIRI = null;
	protected OWLReasoner r = null;
	protected OWLOntologyManager manager = null;
	protected OWLDataFactory odf = null;
	protected OWLOntology o = null;
	protected PrefixManager p = null;
	protected String TBOXIRI = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl";
	protected String ABOXIRI = null;
	protected List<SimpleIRIMapper> ourIRIMap = null;
	
	/** Static ontology nomenclature */
	public static final String TBOXPrefix = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl";
	public static final IRI evtSignalRelation = IRI.create(TBOXPrefix + "#isProducedBy");
	public static final IRI detectorSignalRelation = IRI.create(TBOXPrefix + "#isAppliedByDetector");
	public static final IRI signalRepRelation = IRI.create(TBOXPrefix + "#hasCanonicalRepresentation");
	public static final IRI repImplementationProp = IRI.create(TBOXPrefix + "#signalCanonicalRepresentationImplementation");
	public static final IRI signalValueObjectProperty = IRI.create(TBOXPrefix + "#hasSignalValue");
	public static final IRI signalValueDataProp = IRI.create(TBOXPrefix + "#hasValue");
	public static final IRI signalDomainObjProp = IRI.create(TBOXPrefix + "#hasDomain");
	public static final IRI featureContextObjProp = IRI.create(TBOXPrefix + "#hasSignalDomainContext");
	public static final IRI idsDetectorObjProp = IRI.create(TBOXPrefix + "#hasDetector");
	public static final IRI syntacticFormDataProperty = IRI.create(TBOXPrefix + "#eventSyntacticFormGeneratorImplementation");
	public static final IRI syntacticFormObjectProperty = IRI.create(TBOXPrefix + "#hasSyntacticForm");
	public static final IRI idsSynformObjProp = IRI.create(TBOXPrefix + "#hasSupportedSyntacticForm");
	public static final IRI eventClass = IRI.create(TBOXPrefix + "#Event");
	public static final IRI signalClass = IRI.create(TBOXPrefix + "#Signal");
	public static final IRI signalEventRelation = IRI.create(TBOXPrefix + "#isProducerOf");
	
	/**
	 * @param ourIRI the ourIRI to set
	 */
	private void setOurIRI(IRI ourIRI) {
		this.ourIRI = ourIRI;
	}

	/**
	 * @return the ourIRI
	 */
	public IRI getOurIRI() {
		return ourIRI;
	}

	/**
	 * @param r the r to set
	 */
	protected void setReasoner(OWLReasoner r) {
		this.r = r;
	}

	/**
	 * @return the r
	 */
	public OWLReasoner getReasoner() {
		return r;
	}

	/**
	 * @param manager the manager to set
	 */
	private void setOntologyManager(OWLOntologyManager manager) {
		this.manager = manager;
	}

	/**
	 * @return the manager
	 */
	public OWLOntologyManager getOntologyManager() {
		return manager;
	}

	/**
	 * @param odf the odf to set
	 */
	private void setOwlDataFactory(OWLDataFactory odf) {
		this.odf = odf;
	}

	/**
	 * @return the odf
	 */
	public OWLDataFactory getOwlDataFactory() {
		return odf;
	}

	/**
	 * @param o the o to set
	 */
	private void setOntology(OWLOntology o) {
		this.o = o;
	}

	/**
	 * @return the o
	 */
	public OWLOntology getOntology() {
		return o;
	}
	
	/**
	 * @return The IRI Mapper list as passed in to this oracle
	 * 
	 */
	public List<SimpleIRIMapper> getIRIMapperList(){
		return this.ourIRIMap;
	}

	/**
	 * Attempt to load the ontology; if no IRI is given, try our default location first.
	 * @param m A Simple IRI Mapper with the true location of the ontology (e.g. file:/// or something).
	 * @param IRI - The IRI of this ontology
	 * @throws Exception - If the ontology cannot be loaded.
	 */
	public void loadKIDS(IRI kidskb, List<SimpleIRIMapper> m) throws Exception{
		setOurIRI(kidskb);
		System.out.println("[loadKIDS] Loading from IRI " + getOurIRI());
		ourIRIMap = m;

		p = new DefaultPrefixManager("https://solomon.cs.iastate.edu/ontologies/KIDS.owl#");

		setOntologyManager(OWLManager.createOWLOntologyManager());
		if (m != null){
			for (SimpleIRIMapper imap : m){
			    manager.addIRIMapper(imap);
			}
		}
		setOwlDataFactory(manager.getOWLDataFactory());
		try {
			setOntology(manager.loadOntology(getOurIRI()));

			// Initialize a reasoner:
			ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
			OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
			OWLReasonerFactory rf = new PelletReasonerFactory();
//			OWLReasoner reasoner = rf.createReasoner(o, config);
			setReasoner(rf.createReasoner(o));
			r.precomputeInferences();
			r.isConsistent();
		} catch (Exception e) {
			System.out.println("Failed to load ontology: " + e);
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * @returns A java.util.List<IRI> of signals associated with malicious events.
	 */
	public List<IRI> getMaliciousSignals() {
		LinkedList<IRI> l = new LinkedList<IRI>();
		Set <OWLNamedIndividual> signalSet = new HashSet <OWLNamedIndividual> ();
		//Signal s;
		//Event ev;
		// Ask the reasoner for all Signals which are produced by Events which
		// are subclasses of Malicious.
		
		// For each Event which is a subclass of Malicious:
		OWLClass Event = odf.getOWLClass(":MaliciousActivityEvent",p);
		Iterator <OWLNamedIndividual> i = getIndividuals(Event);

		while (i.hasNext()){

			// Get Signals produced by the Event:
			//Iterator <Node<OWLNamedIndividual>> si = r.getInstances(odf.getOWLObjectHasValue(odf.getOWLObjectProperty(":isProducedBy", p), i.next()) , false).iterator();
			Iterator <Node<OWLNamedIndividual>> si = r.getObjectPropertyValues(i.next(), odf.getOWLObjectProperty(":isProducerOf",p)).iterator();
			
			// For each Signal produced by that Event:
			while (si.hasNext()){
				// Add the signal to the list if not already present:

				Node <OWLNamedIndividual> curN = si.next();
				System.out.println("Evaluating node...");
				
				Iterator <OWLNamedIndividual> iInd = curN.iterator();
				// For each Node element
				while (iInd.hasNext()){
					OWLNamedIndividual cur = iInd.next();
					System.out.println("\tProcessing Signal " + cur);
					// If it indicates the signal...
					if (!signalSet.contains(cur)){
						l.add(cur.getIRI());
						signalSet.add(cur);
					}
				}
			}
		}
		
		return l;
	}
	
	/**
	 * Get an iterator over all OWLNamedIndividuals indicated by this class expression.
	 * @param c - an OWL Class expression
	 * @return An iterator over all individuals
	 */
	public Iterator<OWLNamedIndividual> getIndividuals(OWLClassExpression c){
		NodeSet <OWLNamedIndividual> events = r.getInstances(c,false);
		Iterator <Node<OWLNamedIndividual>> i = events.iterator();
					
		Set<OWLNamedIndividual> ns = new HashSet<OWLNamedIndividual>();
		
		// For each Node
		while (i.hasNext()){
		
			Node <OWLNamedIndividual> curN = i.next();
			Iterator <OWLNamedIndividual> iInd = curN.iterator();
			
			// For each Node element
			while (iInd.hasNext()){
				OWLNamedIndividual cur = iInd.next();
				ns.add(cur);
			}
		}
		return ns.iterator();
	}
	
	private OWLClassExpression getIDSDetectableEvents(String idsName){
		
		OWLNamedIndividual ids = odf.getOWLNamedIndividual(idsName, p);
		
		// Need the intersection of events identifiable by this ids and
		// MaliciousEvent.
		OWLClassExpression allEvents = odf.getOWLObjectHasValue(odf.getOWLObjectProperty(":isIdentifiedByIDS",p),ids);
		return odf.getOWLObjectIntersectionOf(allEvents, odf.getOWLClass(":MaliciousActivityEvent", p));		
	}

	/** 
	 * Determine if the given response can affect the given event.
	 * @param res - The short name of the response.
	 * @param evt - The short name of the event.
	 * @return true if the event can be affected, false otherwise.
	 */
	private boolean canAffect(String res, String evt) {
		OWLNamedIndividual Ev = odf.getOWLNamedIndividual(evt, p);
		OWLNamedIndividual BSip = odf.getOWLNamedIndividual(res, p);
		
		// If res isResponseFor evt, return true; otherwise, return false.
		return r.getObjectPropertyValues(Ev, (odf.getOWLObjectProperty(":isAffectedBy",p))).containsEntity(BSip);
	}

	private boolean canDetect(String det, String evt) {
		OWLNamedIndividual Ev = odf.getOWLNamedIndividual(evt, p);
		OWLNamedIndividual BSip = odf.getOWLNamedIndividual(det, p);
		
		// If the event produces any signals which this detector can detect, return true; 
		// otherwise, return false.
		OWLObjectHasValue t = odf.getOWLObjectHasValue(odf.getOWLObjectProperty(":isProducedBy",p),Ev);
		//return r.isEntailed(odf.getOWLSubClassOfAxiom(t, odf.getOWLClass(":Signal",p)));
		OWLObjectHasValue s = odf.getOWLObjectHasValue(odf.getOWLObjectProperty(":isDetectedBy",p), BSip);
		System.out.println(s.toString());
		//return r.isEntailed(odf.getOWLSubClassOfAxiom(s, odf.getOWLClass(":Signal",p)));
		return (!r.getInstances(odf.getOWLObjectIntersectionOf(t,s), false).isEmpty());
		
//		return r.isSatisfiable(odf.getOWLObjectSomeValuesFrom(odf.getOWLObjectProperty(":isDetectedBy",p),t));
		//return r.getObjectPropertyValues(Ev, (odf.getOWLObjectProperty(":isDetectedBy",p))).containsEntity(BSip);
	} 
	
	private OWLNamedIndividualNodeSet getIndicatedEvents(String[] signals){
		int i = 0;
		OWLNamedIndividualNodeSet results = new OWLNamedIndividualNodeSet();
		OWLNamedIndividual obsSignal;
		
		for (i = 0; i < signals.length; i++){
			obsSignal = odf.getOWLNamedIndividual(signals[i],p);
			results.addDifferentEntities(r.getObjectPropertyValues(obsSignal, odf.getOWLObjectProperty(":isProducedBy",p)).getFlattened());
		}

		return results;
		
	}

	/**
	 * Update the reasoner.
	 */
	public void updateReasoner() {
		r.flush();
	}

	/**
	 * Attempts to instantiate the library associated with the given signal representation
	 * @param ourRep - The individual for this representation.
	 * @param signalInd - Optional: if the signal is already defined in the knowledge base, including this parameter will
	 * initialize the representation with the signal value.  Otherwise, leave it null and an uninitialized representation will be returned.
	 * @return
	 * @throws KIDSOntologyObjectValuesException - If the given signal has an invalid number (!= 1) of representation classes defined.
	 * @throws KIDSOntologyDatatypeValuesException - If the given signal has an invalid number of string representations defined.
	 * @throws KIDSRepresentationInvalidRepresentationValueException - If the representation specified for the signal cannot be used by
	 * the representation implementation for the signal.
	 */
	public KIDSCanonicalRepresentation getCanonicalRepresentation(
			OWLNamedIndividual ourRep, OWLNamedIndividual signalInd) throws KIDSOntologyObjectValuesException, KIDSOntologyDatatypeValuesException, KIDSRepresentationInvalidRepresentationValueException {
		// get the library string value for the given Class
		// The value is a data property of the individual:
		OWLDataProperty representationImpl = odf.getOWLDataProperty(IRI.create(ourIRI.toString() + repImplementationProp.toString()));
		//System.err.println("DEBUG: " + representationImpl);
		Set<OWLLiteral> oaSet = r.getDataPropertyValues(ourRep, representationImpl);
		if (oaSet.size() != 1){
			throw new KIDSOntologyObjectValuesException("Wrong set size for signal canonical representation: " + oaSet.size() + " ; " + ourRep.toString());
		} else {
			OWLLiteral oa = oaSet.iterator().next();
			// Now instantiate the class:
			KIDSCanonicalRepresentation ret = KIDSCanonicalRepresentationFactory.createRepresentation(oa.getLiteral().toString());			
			
			// Get the string value of the signal:
			if (signalInd != null){
			    String value = getSignalValue(signalInd);
			    ret.setValue(value);
			}
			return ret;
		}
	}
	
	/**
	 * Given a signal individual, look up and return the string data value.
	 * @param signalInd - The OWLNamedIndividual for this signalValue
	 * @return The string value stored in the knowledge base for this signal
	 * @throws KIDSOntologyDatatypeValuesException 
	 */
	public String getSignalValue (OWLNamedIndividual signalInd) throws KIDSOntologyDatatypeValuesException{
		// First, we need to get the SignalValue individual:
		Set<OWLNamedIndividual> sinds = r.getObjectPropertyValues(signalInd, odf.getOWLObjectProperty(KIDSOracle.signalValueObjectProperty)).getFlattened();
		if (sinds.size() != 1){
			throw new KIDSOntologyDatatypeValuesException();
		}
		
		Set<OWLLiteral> valueSet = r.getDataPropertyValues(
				sinds.iterator().next(), 
				odf.getOWLDataProperty(signalValueDataProp));
		if (valueSet.size() != 1){
			throw new KIDSOntologyDatatypeValuesException();
		}
		return valueSet.iterator().next().getLiteral();
	}
	
	/**
	 * Given a signal individual, instantiate and return the representation class:
	 * @param signalInd
	 * @return A KIDSCanonicalRepresentation for the signal representation of the given signal
	 * @throws KIDSOntologyObjectValuesException 
	 * @throws KIDSRepresentationInvalidRepresentationValueException 
	 * @throws KIDSOntologyDatatypeValuesException 
	 */
	public KIDSCanonicalRepresentation getSignalRepresentation(OWLNamedIndividual signalInd) throws KIDSOntologyObjectValuesException, KIDSOntologyDatatypeValuesException, KIDSRepresentationInvalidRepresentationValueException {
		// Just get the representation individual for the given signal, and return getCanonicalRepresentation
		OWLNamedIndividual repInd = null; 
		Set<OWLNamedIndividual> candidates = this.r.getObjectPropertyValues(signalInd, odf.getOWLObjectProperty(IRI.create(this.ourIRI.toString() + signalRepRelation))).getFlattened();
		
		// This property should always be functional, but check anyway:
		if (candidates.size() != 1){
			throw new KIDSOntologyObjectValuesException("Wrong size of signal representations: " + candidates.size() + " ; " + signalInd.toString());
		}
		return getCanonicalRepresentation(candidates.iterator().next(), signalInd);
	}

	/**
	 * Given a signal individual, get the associated signal domain as a NamedIndividual and return it
	 * @param signalInd
	 * @return associated signal domain
	 * @throws KIDSOntologyObjectValuesException 
	 */
	public OWLNamedIndividual getSignalDomain(OWLNamedIndividual signalInd) throws KIDSOntologyObjectValuesException {
		Set<OWLNamedIndividual> candidates = 
				r.getObjectPropertyValues(
					signalInd, 
					odf.getOWLObjectProperty(signalDomainObjProp)
				).getFlattened();
		
		if (candidates.size() != 1){
			throw new KIDSOntologyObjectValuesException("Wrong size of signal representations: " + candidates.size() + " ; " + signalInd.toString());
		}
		
		return candidates.iterator().next();
	}

}
