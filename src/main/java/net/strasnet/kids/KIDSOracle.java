/**
 * 
 */
package net.strasnet.kids;

import java.io.File;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.strasnet.kids.measurement.KIDSMeasurementOracle;
import net.strasnet.kids.signalRepresentations.KIDSRepresentationInvalidRepresentationValueException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mindswap.pellet.utils.Namespaces;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.reasoner.BufferingMode;
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
import com.clarkparsia.pellet.owlapiv3.PelletReasoner;

/**
 * @author chrisstrasburg
 *
 * Support Knowledge-based IDS by supporting complex ontology queries.
 */
public class KIDSOracle {

	protected IRI ourIRI = null;
//	protected OWLReasoner r = null;
	protected PelletReasoner r = null;
	protected OWLOntologyManager manager = null;
	protected OWLDataFactory odf = null;
	protected OWLOntology o = null;
	protected PrefixManager p = null;
	public static final String DEFAULTTBOXIRI = "http://solomon.cs.iastate.edu/ontologies/KIDS.owl";
	protected IRI TBOXIRI = null;
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
	public static final IRI datasetClass = IRI.create(TBOXPrefix + "#DataSet");
	public static final IRI timeperiodClass = IRI.create(TBOXPrefix + "#TimePeriod");
	public static final IRI datasetViewClass = IRI.create(TBOXPrefix + "#DatasetView");
	public static final IRI datasetLabelClass = IRI.create(TBOXPrefix + "#DatasetLabel");
	public static final IRI detectorClass = IRI.create(TBOXPrefix + "#Detector");
	public static final IRI signalManifestationClass = IRI.create(TBOXPrefix + "#SignalManifestation");
	public static final IRI signalDomainClass = IRI.create(TBOXPrefix + "#SignalDomain");
	public static final IRI signalDomainRepresentationClass = IRI.create(TBOXPrefix + "#SignalDomainRepresentation");
	public static final IRI signalDomainContextClass = IRI.create(TBOXPrefix + "#SignalDomainContext");
	public static final IRI responseClass = IRI.create(TBOXPrefix + "#Response");
	public static final IRI signalValueClass = IRI.create(TBOXPrefix + "#SignalValue");
	public static final IRI resourceClass = IRI.create(TBOXPrefix + "#Resource");
	
	/** Logging */
	private static final Logger logme = LogManager.getLogger(KIDSOracle.class.getName());
	
	/**
	 * Static method to extract the IRI from an ontology given a file:
	 * @throws OWLOntologyCreationException 
	 */
	public static IRI getOntologyIRI(IRI ontoFile) throws OWLOntologyCreationException{
		OWLOntologyManager ourman = OWLManager.createOWLOntologyManager();
		OWLOntology localo = ourman.loadOntology(ontoFile);
		return localo.getOntologyID().getOntologyIRI();
	}
	
	/**
	 * @param ourIRI the ourIRI to set
	 */
	private void setABOXIRI(IRI ourIRI) {
		logme.debug(String.format("Set KIDSOracle ABOX IRI to: %s", ourIRI.toString()));
		this.ourIRI = ourIRI;
	}

	/**
	 * @param ourIRI the ourIRI to set
	 */
	private void setTBOXIRI(IRI ourIRI) {
		logme.debug(String.format("Set KIDSOracle ABOX IRI to: %s", ourIRI.toString()));
		if (ourIRI.equals(IRI.create(this.DEFAULTTBOXIRI))){
			logme.warn(String.format("Non-default TBOX IRI specified (%s) - this may cause problems...", ourIRI));
		}
		this.TBOXIRI = ourIRI;
	}

	/**
	 * @return the ourIRI
	 */
	public IRI getABOXIRI() {
		return ourIRI;
	}

	/**
	 * @return the TBOXIRI
	 */
	public IRI getTBOXIRI() {
		return TBOXIRI;
	}
	
	/**
	 * 
	 * @return The prefix manager used to initialize this oracle
	 */
	public PrefixManager getPrefixManager(){
		return p;
	}

	/**
	 * @param r the r to set
	 */
	protected void setReasoner(PelletReasoner r) {
		logme.debug(String.format("Set reasoner (BufferingMode: %s, Name: %s, Version: %s, Root Ontology: %s, isConsistent: %b)",
				r.getBufferingMode(), r.getReasonerName(), r.getReasonerVersion(),
				r.getRootOntology().getOntologyID().getOntologyIRI(), r.isConsistent()));
		this.r = r;
		this.getOntologyManager().addOntologyChangeListener(this.r);
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
	 * Create a new ABOX from scratch. Load the TBOX from the given IRI.
	 * 
	 * @param kidskb - ABOX Ontology IRI
	 * @param m
	 * @throws OWLOntologyCreationException
	 * @throws OWLOntologyStorageException 
	 */
	public void createKIDS(IRI kidskb, List<SimpleIRIMapper> m) throws OWLOntologyCreationException, OWLOntologyStorageException {
		this.createKIDS(kidskb, null, m);
	}

	/**
	 * Create a new ABOX from scratch. Load the TBOX from the given IRI.
	 * 
	 * @param kidskb
	 * @param kidsTBOXIRI - the IRI of the TBOX Ontology
	 * @param m
	 * @throws OWLOntologyCreationException - If the ontology cannot be created for some reason
	 * @throws OWLOntologyStorageException - If there is a problem writing to the location specified in the mapper m
	 */
	public void createKIDS(IRI kidskb, IRI kidsTBOXIRI, List<SimpleIRIMapper> m) throws OWLOntologyCreationException, OWLOntologyStorageException {
		setABOXIRI(kidskb);
		setTBOXIRI(kidsTBOXIRI);
		logme.debug(String.format("[createKIDS] Creating with ABOX [%s] (TBOX [%s])", getABOXIRI(), kidsTBOXIRI));

		if (this.TBOXIRI == null){
			logme.info("[createKIDS] No TBOX IRI given, using default " + KIDSOracle.DEFAULTTBOXIRI);
			setTBOXIRI(IRI.create(KIDSOracle.DEFAULTTBOXIRI));
		}

		p = new DefaultPrefixManager();
		
		setOntologyManager(OWLManager.createOWLOntologyManager());
		if (m != null){
			for (SimpleIRIMapper imap : m){
			    manager.addIRIMapper(imap);
			}
		}

		setOwlDataFactory(manager.getOWLDataFactory());

		try {

			setOntology(manager.createOntology(kidskb));
			OWLImportsDeclaration importDeclaration=manager.getOWLDataFactory().getOWLImportsDeclaration(getTBOXIRI());
			manager.applyChange(new AddImport(o, importDeclaration));
			// Save and re-load the ontology:
			manager.loadOntology(getTBOXIRI());
			manager.saveOntology(getOntology());
			this.loadKIDS(kidskb, this.TBOXIRI, m);
			
			/*
			if (! this.TBOXImported()){
				logme.error("TBOX is not correctly imported, aborting.");
			}

			// Initialize a reasoner:
			PelletReasonerFactory rf = new PelletReasonerFactory();
			setReasoner(rf.createNonBufferingReasoner(o));
			r.
			r.precomputeInferences();
			assert r.isConsistent();
			*/
		} catch (OWLOntologyCreationException e) {
			System.out.println("Failed to create ontology: " + e);
			e.printStackTrace();
			throw e;
		}
	}

	public void loadKIDS(IRI kidskb, List<SimpleIRIMapper> m) throws OWLOntologyCreationException {
		loadKIDS(kidskb, null, m);
	}
	/**
	 * Attempt to load the ontology using the provided IRI and IRI Mapper; if no IRI is given, try 
	 * our default location first. Note that both the ABOX and TBOX mappings should be provided.
	 * TODO: Need to figure out how to specify this so that no network connection is required for
	 * local loading.
	 * 
	 * Example usage:
	 * 
	 * <pre>
	 * {@code
	 * myGuy = new KIDSMeasurementOracle();
	 * List<SimpleIRIMapper> m = new LinkedList<SimpleIRIMapper>();
	 * m.add(new SimpleIRIMapper(IRI.create(ABOXIRI), IRI.create(ABOXFile)));
	 * m.add(new SimpleIRIMapper(IRI.create(TBOXIRI), IRI.create(TBOXFile)));
	 * myGuy.loadKIDS(IRI.create(ABOXIRI), m);
	 * }
	 * </pre>
	 * 
	 * @param kidskb - The IRI of this ontology
	 * @param m A Simple IRI Mapper with the true location of the ontology (e.g. file:/// or something).
	 * @throws OWLOntologyCreationException - If the ontology causes an error when loading 
	 * @throws Exception - If the ontology cannot be loaded.
	 */
	public void loadKIDS(IRI kidskb, IRI kidsTBOXkb, List<SimpleIRIMapper> m) throws OWLOntologyCreationException {
		setABOXIRI(kidskb);
		setTBOXIRI(kidsTBOXkb);

		if (getTBOXIRI() == null){
			setTBOXIRI(IRI.create(KIDSOracle.DEFAULTTBOXIRI));
		}

		logme.info("[loadKIDS] Loading from IRI " + getABOXIRI());
		ourIRIMap = m;
		IRI fileIRI = null;

//		p = new DefaultPrefixManager("https://solomon.cs.iastate.edu/ontologies/KIDS.owl#");
		p = new DefaultPrefixManager();

		setOntologyManager(OWLManager.createOWLOntologyManager());
		if (m != null){
			for (SimpleIRIMapper imap : m){
			    manager.addIRIMapper(imap);
			    if (imap.getDocumentIRI(kidskb) != null){
			    	fileIRI = imap.getDocumentIRI(kidskb);
			    }
			}
		}
		
		if (fileIRI == null){
			System.err.println("Could not identify file IRI for " + kidskb + "!");
			System.exit(1);
		} else {
			logme.info(String.format("Loading from file %s", fileIRI));
		}
		
		setOwlDataFactory(manager.getOWLDataFactory());
		try {
			setOntology(manager.loadOntology(fileIRI));
			if (! this.TBOXImported()){
				logme.warn("TBOX was not imported correctly; adding import.");
				OWLImportsDeclaration importDeclaration=manager.getOWLDataFactory().getOWLImportsDeclaration(getTBOXIRI());
					manager.applyChange(new AddImport(o, importDeclaration));
					manager.loadOntology(getTBOXIRI());
			}

			// Initialize a reasoner:
			PelletReasonerFactory rf = new PelletReasonerFactory();
			setReasoner(rf.createNonBufferingReasoner(o));
			r.precomputeInferences();
			assert r.isConsistent();
		} catch (OWLOntologyCreationException e) {
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
	 * Get the set of all OWLNamedIndividuals indicated by this class expression.
	 * @param c - an OWL Class expression
	 * @return An iterator over all individuals
	 */
	public Set<OWLNamedIndividual> getIndividualSet(OWLClassExpression c){
		NodeSet <OWLNamedIndividual> events = r.getInstances(c,false);
		Set <OWLNamedIndividual> eventSet = events.getFlattened();
		logme.debug(String.format("Reasoner query for members of class %s returned %d results.", 
				c.toString(), eventSet.size()));
		return eventSet;
	}

	/**
	 * Get an iterator over all OWLNamedIndividuals indicated by this class expression.
	 * @param c - an OWL Class expression
	 * @return An iterator over all individuals
	 */
	public Iterator<OWLNamedIndividual> getIndividuals(OWLClassExpression c){
		return r.getInstances(c,false).getFlattened().iterator();
		/*
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
		*/
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
	
	/**
	 * 
	 * @param source - A set of OWLNamedIndividuals
	 * @return - A corresponding set of IRIs
	 */
	public Set<IRI> getIRISetFromNamedIndividualSet(Set<OWLNamedIndividual> source){
		Set<IRI> toReturn = new HashSet<IRI>();
		for (OWLNamedIndividual i : source){
			toReturn.add(i.getIRI());
		}
		return toReturn;
	}
	
	/**
	 * Determine if the TBOX is correctly imported:
	 * @return true if the TBOX is imported and referenced correctly; false otherwise.
	 */
	public boolean TBOXImported(){
		boolean toReturn = false;
		
		Set<OWLOntology> directImportSet = o.getDirectImports();
		logme.debug(String.format("Found %d direct imports of %s.", directImportSet.size(), o.getOntologyID().getOntologyIRI()));
		for (OWLOntology ontoImport : directImportSet){
			if (ontoImport.getOntologyID().getOntologyIRI().equals(getTBOXIRI())){
				logme.debug("Found matching ontology in direct imports.");
				toReturn = true;
			}
		}
		Set<OWLOntology> totalImportSet = o.getImports();
		logme.debug(String.format("Found %d total imports of %s.", directImportSet.size(), o.getOntologyID().getOntologyIRI()));
		for (OWLOntology ontoImport : totalImportSet){
			if (ontoImport.getOntologyID().getOntologyIRI().equals(getTBOXIRI())){
				logme.debug("Found matching ontology in total imports.");
				toReturn = true;
			}
		}
		
		return toReturn;
	}

}
