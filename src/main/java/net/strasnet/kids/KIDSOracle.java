/**
 * 
 */
package net.strasnet.kids;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.strasnet.kids.measurement.KIDSMeasurementOracle;
import net.strasnet.kids.signalRepresentations.KIDSRepresentationInvalidRepresentationValueException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.mindswap.pellet.utils.Namespaces;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.model.SetOntologyID;
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
import com.google.common.base.Optional;
import com.clarkparsia.pellet.owlapiv3.PelletReasoner;

/**
 * @author chrisstrasburg
 *
 * Support Knowledge-based IDS by supporting complex ontology queries.
 */
public class KIDSOracle {

	protected IRI ourIRI = null;
	protected IRI ourLocIRI = null;
	protected IRI tboxLocIRI = null;
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
	
	//TODO: This is horrible; it should be revamped to either load at runtime from the actual TBOX, or at least be
	// built and imported via a script?

	// Relation constants:
	public static final IRI contextDomainRelation = IRI.create(TBOXPrefix + "#isContextOfSignalDomain");
	public static final IRI correlationFunctionImplementationDataProperty = IRI.create(TBOXPrefix + "#hasCorrelationRelationImplementation");
	public static final IRI correlationRelationDatasetViewRelation = IRI.create(TBOXPrefix + "#isSupportedByDatasetView");
	public static final IRI datasetContextRelation = IRI.create(TBOXPrefix + "#isContainerOfContext");
	public static final IRI datasetDetectorRelation = IRI.create(TBOXPrefix + "#isExposedToDetector");
	public static final IRI datasetInstanceResourceProp = IRI.create(TBOXPrefix + "#datasetLocation");
	public static final IRI datasetLabelResourceProp = IRI.create(TBOXPrefix + "#datasetLabelLocation");
	public static final IRI datasetParserImplementationProp = IRI.create(TBOXPrefix + "#datasetParserImplementation");
	public static final IRI datasetSignalRelation = IRI.create(TBOXPrefix + "#isCompatibleDatasetForSignal");
	public static final IRI datasetTimeperiodRelation = IRI.create(TBOXPrefix + "#includesTimePeriod");
	public static final IRI datasetViewCorrelationRelation = IRI.create(TBOXPrefix + "#supportCorrelationRelation");
	public static final IRI datasetViewIsViewOfDatasetRelation = IRI.create(TBOXPrefix + "#providesViewOf");
	public static final IRI datasetViewRelation = IRI.create(TBOXPrefix + "#isViewableAs");
	public static final IRI datasetViewSignalManifestationRelation = IRI.create(TBOXPrefix + "#bringsIntoExistence");
	public static final IRI detectorDatasetRelation = IRI.create(TBOXPrefix + "#canMonitorDataset");
	public static final IRI detectorExecutionDataProperty = IRI.create(TBOXPrefix + "#detectorExecutionCommand");
	public static final IRI detectorImplementationDataProperty = IRI.create(TBOXPrefix + "#hasImplementationClass");
	public static final IRI detectorSignalRelation = IRI.create(TBOXPrefix + "#isAppliedByDetector");
	public static final IRI detectorSyntaxImplementationDataProperty = IRI.create(TBOXPrefix + "#hasSyntaxProductionImplementation");
	public static final IRI detectorSyntaxRelation = IRI.create(TBOXPrefix + "#hasSyntax");
	public static final IRI detectorSyntaxSignalRelation = IRI.create(TBOXPrefix + "#canRepresentFeatureWithConstraint");
	public static final IRI domainContextRelation = IRI.create(TBOXPrefix + "#isInContext");
	public static final IRI domainSignalRelation = IRI.create(TBOXPrefix + "#isDomainOfSignal");
	public static final IRI eventDatasetRelation = IRI.create(TBOXPrefix + "#isEvaluatedBy");
	public static final IRI eventLabelRelation = IRI.create(TBOXPrefix + "#isIncludedInLabel");
	public static final IRI eventSignalRelation = IRI.create(TBOXPrefix + "#isProducedBy");
	public static final IRI eventTimePeriodRelation = IRI.create(TBOXPrefix + "#isEvaluableDuringTimePeriod");
	public static final IRI featureContextObjProp = IRI.create(TBOXPrefix + "#hasSignalDomainContext");
	public static final IRI idsDetectorObjProp = IRI.create(TBOXPrefix + "#hasDetector");
	public static final IRI idsSynformObjProp = IRI.create(TBOXPrefix + "#hasSupportedSyntacticForm");
	public static final IRI labelClassDataProperty = IRI.create(TBOXPrefix + "#hasLabelFunction");
	public static final IRI labelLocationDataProperty = IRI.create(TBOXPrefix + "#hasLabelDataLocation");
	public static final IRI labelViewRelation = IRI.create(TBOXPrefix + "#isLabelerForDatasetView");
	public static final IRI manifestationSignalRelation = IRI.create(TBOXPrefix + "#SignalManifestationIncludesSignal");
	public static final IRI repImplementationProp = IRI.create(TBOXPrefix + "#signalCanonicalRepresentationImplementation");
	public static final IRI signalConstraintSignalRelation = IRI.create(TBOXPrefix + "#hasConstraint");
	public static final IRI signalDatasetRelation = IRI.create(TBOXPrefix + "#isEvaluableWithDataset");
	public static final IRI signalDetectorRelation = IRI.create(TBOXPrefix + "#isAppliedByDetector");
	public static final IRI signalDomainObjProp = IRI.create(TBOXPrefix + "#hasDomain");
	public static final IRI signalEventRelation = IRI.create(TBOXPrefix + "#isProducerOf");
	public static final IRI signalManifestationRelation = IRI.create(TBOXPrefix + "#SignalInManifestation");
	public static final IRI signalRepRelation = IRI.create(TBOXPrefix + "#hasCanonicalRepresentation");
	public static final IRI signalRepresentationRelation = IRI.create(TBOXPrefix + "#isRepresentedBy");
	public static final IRI signalValueDataProp = IRI.create(TBOXPrefix + "#hasValue");
	public static final IRI signalValueObjectProperty = IRI.create(TBOXPrefix + "#hasSignalValue");
	public static final IRI syntacticFormDataProperty = IRI.create(TBOXPrefix + "#eventSyntacticFormGeneratorImplementation");
	public static final IRI syntacticFormObjectProperty = IRI.create(TBOXPrefix + "#hasSyntacticForm");
	public static final IRI timePeriodDatasetRelation = IRI.create(TBOXPrefix + "#isIncludedInDataset");
	public static final IRI viewClassDataProperty = IRI.create(TBOXPrefix + "#viewProductionImplementation");
	public static final IRI viewDetectorRelation = IRI.create(TBOXPrefix + "#isMonitoredBy");
	public static final IRI viewLabelRelation = IRI.create(TBOXPrefix + "#hasDatasetLabel");
	
	
	// Class constants:
	public static final IRI datasetClass = IRI.create(TBOXPrefix + "#Dataset");
	public static final IRI datasetLabelClass = IRI.create(TBOXPrefix + "#DatasetLabel");
	public static final IRI datasetViewClass = IRI.create(TBOXPrefix + "#DatasetView");
	public static final IRI detectorClass = IRI.create(TBOXPrefix + "#Detector");
	public static final IRI detectorSyntaxClass = IRI.create(TBOXPrefix + "#DetectorSyntax");
	public static final IRI eventClass = IRI.create(TBOXPrefix + "#Event");
	public static final IRI HTTPClientUserAgentClass = IRI.create(TBOXPrefix + "#HTTPClientUserAgent");
	public static final IRI HTTPClientUsernameClass = IRI.create(TBOXPrefix + "#HTTPClientUsername");
	public static final IRI HTTPGetRequestClass = IRI.create(TBOXPrefix + "#HTTPGetRequest");
	public static final IRI HTTPGetRequestParameterClass = IRI.create(TBOXPrefix + "#HTTPGetRequestParameter");
	public static final IRI HTTPGetRequestQueryStringClass = IRI.create(TBOXPrefix + "#HTTPGetRequestQueryString");
	public static final IRI HTTPServerResponseCode = IRI.create(TBOXPrefix + "#HTTPServerResponseCode");
	public static final IRI MaliciousActivityEventClass = IRI.create(TBOXPrefix + "#MaliciousActivityEvent");
	public static final IRI resourceClass = IRI.create(TBOXPrefix + "#Resource");
	public static final IRI responseClass = IRI.create(TBOXPrefix + "#Response");
	public static final IRI signalClass = IRI.create(TBOXPrefix + "#Signal");
	public static final IRI signalConstraintClass = IRI.create(TBOXPrefix + "#SignalConstraint");
	public static final IRI signalDomainClass = IRI.create(TBOXPrefix + "#SignalDomain");
	public static final IRI signalDomainContextClass = IRI.create(TBOXPrefix + "#SignalDomainContext");
	public static final IRI signalDomainRepresentationClass = IRI.create(TBOXPrefix + "#SignalDomainRepresentation");
	public static final IRI signalManifestationClass = IRI.create(TBOXPrefix + "#SignalManifestation");
	public static final IRI signalValueClass = IRI.create(TBOXPrefix + "#SignalValue");
	public static final IRI TCPServerPort = IRI.create(TBOXPrefix + "#TCPServerPort");
	public static final IRI timeperiodClass = IRI.create(TBOXPrefix + "#TimePeriod");
	
	/** Logging */
	private static final Logger logme = LogManager.getLogger(KIDSOracle.class.getName());
	
	/**
	 * Static method to extract the IRI from an ontology given a file:
	 * @throws OWLOntologyCreationException 
	 */
	public static IRI getOntologyIRI(IRI ontoFile) throws OWLOntologyCreationException{
		OWLOntologyManager ourman = OWLManager.createOWLOntologyManager();
		OWLOntology localo = ourman.loadOntology(ontoFile);
		IRI toReturn = localo.getOntologyID().getOntologyIRI().get();
		return toReturn;
	}
	
	/**
	 * @param ourIRI the ourIRI to set
	 */
	private void setABOXLocationIRI(IRI ourLocIRI) {
		logme.debug(String.format("Set KIDSOracle ABOX Location to: %s", ourLocIRI.toString()));
		this.ourLocIRI = ourLocIRI;
	}

	/**
	 * @param tboxLocIRI the location of the TBOX we're using.
	 */
	private void setTBOXLocationIRI(IRI tboxLocIRI) {
		logme.debug(String.format("Set KIDSOracle TBOX Location to: %s", tboxLocIRI.toString()));
		this.tboxLocIRI = tboxLocIRI;
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
		if (ourIRI == null){
			logme.info(String.format("Received null TBOX IRI; setting to %s", this.DEFAULTTBOXIRI));
			ourIRI = IRI.create(this.DEFAULTTBOXIRI);
		} else if (!ourIRI.equals(IRI.create(this.DEFAULTTBOXIRI))){
			logme.warn(String.format("Non-default TBOX IRI specified (%s) - this may cause problems...", ourIRI));
		}

		this.TBOXIRI = ourIRI;
		logme.debug(String.format("Set KIDSOracle TBOX IRI to: %s", ourIRI.toString()));
	}

	/**
	 * @return the ourIRI
	 */
	public IRI getABOXIRI() {
		return ourIRI;
	}
	
	/**
	 * 
	 * @return - The IRI of the location the ABOX was loaded from.
	 */
	public IRI getABOXLocIRI(){
		return ourLocIRI;
	}

	/**
	 * @return the TBOXIRI
	 */
	public IRI getTBOXIRI() {
		return TBOXIRI;
	}

	/**
	 * 
	 * @return - The IRI of the location the TBOX was loaded from.
	 */
	public IRI getTBOXLocIRI(){
		return tboxLocIRI;
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
	 * @param kidsABOXkb - ABOX Ontology IRI
	 * @param m - a list of SimpleIRI mappers that includes a mapping from the abox IRI to the document location IRI, and
	 *            the TBOX IRI to the document location IRI.
	 * @throws OWLOntologyCreationException
	 * @throws OWLOntologyStorageException 
	 */
	public void createKIDS(IRI kidsABOXkb, 
			List<SimpleIRIMapper> m) throws OWLOntologyCreationException, OWLOntologyStorageException {
		this.createKIDS(kidsABOXkb, null, m);
	}

	/**
	 * Create a new ABOX from scratch. Load the TBOX from the given IRI.
	 * 
	 * @param kidsABOXkb - the IRI of the ABOX Ontology
	 * @param kidsDOCkb - the IRI of the ABOX Ontology Document (physical location)
	 * @param kidsTBOXIRI - the IRI of the TBOX Ontology
	 * @param kidsTBOXDOCIRI - the IRI of the TBOX Ontology Document (physical location)
	 * @param m - a list of SimpleIRI mappers that includes a mapping from the abox IRI to the document location IRI, and
	 *            the TBOX IRI to the document location IRI.
	 * @throws OWLOntologyCreationException - If the ontology cannot be created for some reason
	 * @throws OWLOntologyStorageException - If there is a problem writing to the location specified in the mapper m
	 */
	public void createKIDS(IRI kidsABOXkb, IRI kidsTBOXIRI, List<SimpleIRIMapper> m) throws OWLOntologyCreationException, OWLOntologyStorageException {
		try {
			setOntologyManager(OWLManager.createOWLOntologyManager());
			setOwlDataFactory(manager.getOWLDataFactory());
			setOntology(manager.createOntology(kidsABOXkb));

			setABOXIRI(kidsABOXkb);
			setTBOXIRI(kidsTBOXIRI);

			if (this.TBOXIRI == null){
				logme.info("[createKIDS] No TBOX IRI given, using default " + KIDSOracle.DEFAULTTBOXIRI);
				setTBOXIRI(IRI.create(KIDSOracle.DEFAULTTBOXIRI));
			}
			logme.debug(String.format("[createKIDS] Creating with ABOX [%s] (TBOX [%s])", getABOXIRI(), kidsTBOXIRI));

			//p = new DefaultPrefixManager();
			IRI ABOXFile = kidsABOXkb;
			IRI TBOXFile = getTBOXIRI();

			// If the locations are defined in the prefix mapper, set the location values accordingly
			if (m != null){
				for (SimpleIRIMapper imap : m){
			    	manager.getIRIMappers().add(imap);
			    	if (imap.getDocumentIRI(kidsABOXkb) != null){
			    		ABOXFile = imap.getDocumentIRI(kidsABOXkb);
			    		logme.debug(String.format("Mapping %s -> %s...", 
			    			kidsABOXkb.toString(),
			    			ABOXFile.toString()));
			    	} else if (imap.getDocumentIRI(getTBOXIRI()) != null){
			    		TBOXFile = imap.getDocumentIRI(getTBOXIRI());
			    		logme.debug(String.format("Mapping %s -> %s...", 
			    			getTBOXIRI().toString(),
			    			TBOXFile.toString()));
			    	}
				}
			}
			this.setTBOXLocationIRI(TBOXFile);
			this.setABOXLocationIRI(ABOXFile);

			// Ensure that the ABOX actually imports the TBOX ontology:
			OWLImportsDeclaration importDeclaration=manager.getOWLDataFactory().getOWLImportsDeclaration(getTBOXIRI());
			manager.applyChange(new AddImport(o, importDeclaration));
			
			OWLOntologyID nuid = new OWLOntologyID(Optional.of(kidsABOXkb), Optional.of(ABOXFile));
			SetOntologyID setid = new SetOntologyID(this.o, nuid);
			manager.applyChange(setid);

			// Save and load to make sure all the prefixes and mappings are set correctly in the manager:
			saveKIDS();
			this.loadKIDS(kidsABOXkb, getTBOXIRI(), m);
			
		} catch (OWLOntologyCreationException e) {
			System.out.println("Failed to create ontology: " + e);
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * A convenience method when the default TBOX should just be used. Note that the mapper can still be
	 * used to specify the document location.
	 * @param kidsABOXkb
	 * @param m
	 * @throws OWLOntologyCreationException
	 */
	public void loadKIDS(IRI kidsABOXkb, List<SimpleIRIMapper> m) throws OWLOntologyCreationException {
		loadKIDS(kidsABOXkb, null, m);
	}

	/**
	 * Attempt to load the ontology using the provided IRI and IRI Mapper; if no IRI is given, try 
	 * our default location first. Note that both the ABOX and TBOX mappings should be provided.
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
	 * @param kidsABOXkb - The IRI of the ABOX to load
	 * @param kidsTBOXkb - The IRI of the TBOX to use
	 * @param m A Simple IRI Mapper with the true location of the ontology (e.g. file:/// or something);
	 *          if not entry exists for either the ABOX or TBOX, they will be loaded from the location their IRI suggests.
	 * @throws OWLOntologyCreationException - If the ontology causes an error when loading 
	 * @throws Exception - If the ontology cannot be loaded.
	 */
	public void loadKIDS(IRI kidsABOXkb, IRI kidsTBOXkb, List<SimpleIRIMapper> m) throws OWLOntologyCreationException {
		setABOXIRI(kidsABOXkb);
		setTBOXIRI(kidsTBOXkb);

		setOntologyManager(OWLManager.createOWLOntologyManager());

		// If the TBOX is null, use the default
		if (getTBOXIRI() == null){
			// Load ABOX and get TBOX location / IRI
			setTBOXIRI(IRI.create(KIDSOracle.DEFAULTTBOXIRI));
		}
		
		// Set location IRIs based on the simple mapping:
		IRI ABOXLocIRI = kidsABOXkb;
		IRI TBOXLocIRI = getTBOXIRI();
		if (m != null){
			for (SimpleIRIMapper imap : m){
				manager.getIRIMappers().add(imap);
				if (imap.getDocumentIRI(getTBOXIRI()) != null){
					TBOXLocIRI = imap.getDocumentIRI(getTBOXIRI());
			    }
				if (imap.getDocumentIRI(kidsABOXkb) != null){
					ABOXLocIRI = imap.getDocumentIRI(kidsABOXkb);
				}
			}
		}
		
		this.setABOXLocationIRI(ABOXLocIRI);
		this.setTBOXLocationIRI(TBOXLocIRI);

		try {

			logme.info(String.format("[loadKIDS] Loading ontology %s from location %s with TBOX %s from location %s ",
					getABOXIRI(), getABOXLocIRI(), getTBOXIRI(), getTBOXLocIRI()));

			setOntology(manager.loadOntologyFromOntologyDocument(getABOXLocIRI()));
			ourIRIMap = m;

			p = new DefaultPrefixManager();
			setOwlDataFactory(manager.getOWLDataFactory());

			if (! this.TBOXImported()){
				logme.warn(String.format("TBOX was not imported correctly; adding import for %s.",getTBOXIRI()));
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
	 * Write the ABOX back to storage location (if possible), using the OWLXMLDocumentFormat. Note that 
	 * all new / modified statements will be written to the ABOX location; the TBOX is never modified.
	 * 
	 * @throws OWLOntologyStorageException - If it cannot be saved for some reason.
	 */
	public void saveKIDS() throws OWLOntologyStorageException{
		OWLDocumentFormat format = manager.getOntologyFormat(getOntology());
		
		logme.debug(String.format("Saving ontology %s to file %s in format %s",getOntology().getOntologyID(), this.getABOXLocIRI(), format.toString()));
		manager.saveOntology(getOntology(), format, this.getABOXLocIRI());
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
		OWLClass Event = odf.getOWLClass(MaliciousActivityEventClass);
		Iterator <OWLNamedIndividual> i = getIndividuals(Event);

		while (i.hasNext()){

			// Get Signals produced by the Event:
			//Iterator <Node<OWLNamedIndividual>> si = r.getInstances(odf.getOWLObjectHasValue(odf.getOWLObjectProperty(":isProducedBy", p), i.next()) , false).iterator();
			Iterator <Node<OWLNamedIndividual>> si = r.getObjectPropertyValues(i.next(), odf.getOWLObjectProperty(signalEventRelation)).iterator();
			
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

	/**
	 * Update the reasoner.
	 */
	public void updateReasoner() {
		r.flush();
	}

	/**
	 * Given a signal individual, look up and return the string data value.
	 * @param signalInd - The OWLNamedIndividual for this signalValue
	 * @return The string value stored in the knowledge base for this signal
	 * @throws KIDSOntologyDatatypeValuesException 
	 */
	public String getSignalValue (IRI signalIri) throws KIDSOntologyDatatypeValuesException{
		// First, we need to get the SignalValue individual:
		OWLNamedIndividual signalInd = odf.getOWLNamedIndividual(signalIri);
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
	 * Given a signal individual, get the associated signal domain as a NamedIndividual and return it
	 * @param mySig
	 * @return associated signal domain
	 * @throws KIDSOntologyObjectValuesException 
	 */
	public IRI getSignalDomain(IRI mySig) throws KIDSOntologyObjectValuesException {
		Set<OWLNamedIndividual> candidates = 
				r.getObjectPropertyValues(
					odf.getOWLNamedIndividual(mySig), 
					odf.getOWLObjectProperty(signalDomainObjProp)
				).getFlattened();
		
		if (candidates.size() != 1){
			throw new KIDSOntologyObjectValuesException("Wrong size of signal representations: " + candidates.size() + " ; " + mySig.toString());
		}
		
		return candidates.iterator().next().getIRI();
	}
	
	/**
	 * 
	 * @param source - A set of OWLNamedIndividuals
	 * @return - A corresponding set of IRIs
	 */
	public Set<IRI> getIRISetFromNamedIndividualSet(Set<? extends OWLEntity> source){
		Set<IRI> toReturn = new HashSet<IRI>();
		for (OWLEntity i : source){
			toReturn.add(i.getIRI());
		}
		return toReturn;
	}
	
	/**
	 * Given a local file, will evaluate the file path and ensure that a valid URI is returned, e.g.:
	 *  ./test.txt -> file:///home/cstras/test.txt
	 *  file:/tmp/test.ico -> file:///tmp/test.ico
	 * @param target
	 * @return
	 * @throws URISyntaxException 
	 * @throws IOException 
	 */
	public URI getValidFileURI(File target) throws URISyntaxException, IOException{
		if (!target.toString().startsWith("file:")){
			return target.getCanonicalFile().toURI();
		} else {
			return URI.create(target.toString());
		}
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
			if (ontoImport.getOntologyID().getOntologyIRI().get().equals(getTBOXIRI())){
				logme.debug("Found matching ontology in direct imports.");
				toReturn = true;
			}
		}
		Set<OWLOntology> totalImportSet = o.getImports();
		logme.debug(String.format("Found %d total imports of %s.", directImportSet.size(), o.getOntologyID().getOntologyIRI()));
		for (OWLOntology ontoImport : totalImportSet){
			if (ontoImport.getOntologyID().getOntologyIRI().get().equals(getTBOXIRI())){
				logme.debug("Found matching ontology in total imports.");
				toReturn = true;
			}
		}
		
		return toReturn;
	}

	/**
	 * Returns the set of detectors that can be evaluated given the event and time period. To qualify, a detector must:
	 * * Be able to detect at least one signal produced by the event;
	 * * Be able to monitor at least one dataset evaluable by event and time period;
	 * 
	 * @param eventUIC - The event component we are querying
	 * @param timeUIC - The time period component we are querying
	 * @return - a set of Detectors that will be included in an evaluation of the event in the time period.
	 */
	public Set<IRI> getEvaluableDetectorsForEventTimePeriod(IRI eventIRI, IRI timePeriodIRI) {
		Set<OWLNamedIndividual> toReturn = new HashSet<OWLNamedIndividual>();
		
		// First, get the set of signals evaluable over the event and time period;
		Set<IRI> evalSigSet = getEvaluableSignalsForEventTimePeriod(eventIRI, timePeriodIRI);
		
		// Next, get the set of datasets evaluable for the event and time period;
		Set<IRI> evalDataSet = getEvaluableDatasetsForEventTimePeriod(eventIRI, timePeriodIRI);
		
		// Finally, extract the set of detectors related to both:
		// - isAppliedByDetector // canApplySignal ;
		Set<OWLNamedIndividual> detSetForSignals = new HashSet<OWLNamedIndividual>();
		for (IRI s : evalSigSet){
			Set<OWLNamedIndividual> detectorsForSignal = r.getObjectPropertyValues(
					odf.getOWLNamedIndividual(s), 
					odf.getOWLObjectProperty(KIDSOracle.signalDetectorRelation)).getFlattened();
			detSetForSignals.addAll(detectorsForSignal);
		}
		logme.debug(String.format("Found %d detectors able to evaluate signals",detSetForSignals.size()));

		// - canMonitor o providesViewOf
		
		Set<OWLNamedIndividual> detSetForDatasets = new HashSet<OWLNamedIndividual>();
		
		for (IRI d : evalDataSet){
			Set<OWLNamedIndividual> detectorsForDataset = r.getObjectPropertyValues(
					odf.getOWLNamedIndividual(d),
					odf.getOWLObjectProperty(KIDSOracle.datasetDetectorRelation)).getFlattened();
			detSetForDatasets.addAll(detectorsForDataset);
		}
		logme.debug(String.format("Found %d detectors able to evaluate datasets",detSetForDatasets.size()));

		// Find the set intersecton:
		for (OWLNamedIndividual ds: detSetForSignals){
			if (detSetForDatasets.contains(ds)){
				toReturn.add(ds);
				logme.debug(String.format("Found dataset %s for both %s and %s.", ds.getIRI().getShortForm(), 
					eventIRI.getShortForm(), timePeriodIRI.getShortForm()));
			}
		}
		
		logme.debug(String.format("Found %d detectors compatible with time period %s and produced by event %s.", 
				toReturn.size(), timePeriodIRI.getShortForm(), eventIRI.getShortForm()));

		return this.getIRISetFromNamedIndividualSet(toReturn);
	}
	/**
	 * Returns the set of datasets which can be evaluated given the event and time period. To qualify, a dataset must:
	 * * Contain at least one signal produced by the event;
	 * * Coincide with the time-period under evaluation
	 * 
	 * Because the time period is included in the inference defining the property isEvaluationOf, we only need to look at
	 * values for that property.
	 * 
	 * @param eventUIC - The event component we are querying
	 * @param timeUIC - The time period component we are querying
	 * @return - a set of Dataset IRIs that will be included in an evaluation of the event in the time period.
	 */
	public Set<IRI> getEvaluableDatasetsForEventTimePeriod(IRI eventIRI, IRI timePeriodIRI) {
		Set<OWLNamedIndividual> toReturn = r.getObjectPropertyValues(
				odf.getOWLNamedIndividual(eventIRI), 
				odf.getOWLObjectProperty(KIDSOracle.eventDatasetRelation)).getFlattened();

		logme.debug(String.format("Found %d datasets compatible with time period %s and produced by event %s.", 
				toReturn.size(), timePeriodIRI.getShortForm(), eventIRI.getShortForm()));

		return this.getIRISetFromNamedIndividualSet(toReturn);
	}
	/**
	 * Returns the set of signals which can be evaluated given the event and time period. To qualify, a signal must:
	 * * Be produced by the event;
	 * * Be present in at least one dataset which includes the time period.
	 * 
	 * @param eventUIC - The event component we are querying
	 * @param timeUIC - The time period component we are querying
	 * @return - a set of KIDSUISignalComponents that will be included of an evaluation of the event in the time period.
	 */
	public Set<IRI> getEvaluableSignalsForEventTimePeriod(IRI eventIRI, IRI timePeriodIRI) {
		Set<OWLNamedIndividual> toReturn = new HashSet<OWLNamedIndividual>();

		OWLObjectHasValue signalsProducedByEvent = odf.getOWLObjectHasValue(odf.getOWLObjectProperty(KIDSOracle.eventSignalRelation), odf.getOWLNamedIndividual(eventIRI));
		OWLObjectHasValue datasetsIncludingTimePeriod = odf.getOWLObjectHasValue(odf.getOWLObjectProperty(KIDSOracle.datasetTimeperiodRelation), odf.getOWLNamedIndividual(timePeriodIRI));
		
		Set<OWLNamedIndividual> eventSigSet = r.getInstances(signalsProducedByEvent, false).getFlattened();
		Set<OWLNamedIndividual> debugDatSet = r.getInstances(datasetsIncludingTimePeriod, false).getFlattened();
		
		// TODO: This is terrible; need to revisit and find a more OWL-ish way to derive this set:
		for (OWLNamedIndividual ds: debugDatSet){
			Set<OWLNamedIndividual>datSigSet = r.getObjectPropertyValues(ds, odf.getOWLObjectProperty(KIDSOracle.datasetSignalRelation)).getFlattened();
			for (OWLNamedIndividual candidateSig : datSigSet){
				if (eventSigSet.contains(candidateSig)){
					toReturn.add(candidateSig);
					logme.debug(String.format("Found signal %s compatible with data set %s and produced by event.", candidateSig.getIRI(), 
							ds.getIRI().getShortForm()));
				}
			}
		}
		
		logme.debug(String.format("Found %d signals compatible with time period %s and produced by event %s.", 
				toReturn.size(), timePeriodIRI.getShortForm(), eventIRI.getShortForm()));

		return this.getIRISetFromNamedIndividualSet(toReturn);
	}

}
