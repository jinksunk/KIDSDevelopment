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
	 * @param m - a list of SimpleIRI mappers that includes a mapping from the abox IRI to the document location IRI, and
	 *            the TBOX IRI to the document location IRI.
	 * @throws OWLOntologyCreationException
	 * @throws OWLOntologyStorageException 
	 */
	public void createKIDS(IRI kidskb, 
			List<SimpleIRIMapper> m) throws OWLOntologyCreationException, OWLOntologyStorageException {
		this.createKIDS(kidskb, null, m);
	}

	/**
	 * Create a new ABOX from scratch. Load the TBOX from the given IRI.
	 * 
	 * @param kidskb - the IRI of the ABOX Ontology
	 * @param kidsDOCkb - the IRI of the ABOX Ontology Document (physical location)
	 * @param kidsTBOXIRI - the IRI of the TBOX Ontology
	 * @param kidsTBOXDOCIRI - the IRI of the TBOX Ontology Document (physical location)
	 * @param m - a list of SimpleIRI mappers that includes a mapping from the abox IRI to the document location IRI, and
	 *            the TBOX IRI to the document location IRI.
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

		//p = new DefaultPrefixManager();
		setOntologyManager(OWLManager.createOWLOntologyManager());
		IRI ABOXFile = kidskb;
		IRI TBOXFile = getTBOXIRI();

		if (m != null){
			for (SimpleIRIMapper imap : m){
			    manager.getIRIMappers().add(imap);
			    if (imap.getDocumentIRI(kidskb) != null){
			    	ABOXFile = imap.getDocumentIRI(kidskb);
			    	logme.debug(String.format("Mapping %s -> %s...", 
			    			kidskb.toString(),
			    			ABOXFile.toString()));
			    } else if (imap.getDocumentIRI(getTBOXIRI()) != null){
			    	TBOXFile = imap.getDocumentIRI(getTBOXIRI());
			    	logme.debug(String.format("Mapping %s -> %s...", 
			    			getTBOXIRI().toString(),
			    			TBOXFile.toString()));
			    }
			}
		}

		setOwlDataFactory(manager.getOWLDataFactory());

		try {

			setOntology(manager.createOntology(kidskb));
			OWLImportsDeclaration importDeclaration=manager.getOWLDataFactory().getOWLImportsDeclaration(getTBOXIRI());
			manager.applyChange(new AddImport(o, importDeclaration));
			
			OWLOntologyID nuid = new OWLOntologyID(Optional.of(kidskb), Optional.of(ABOXFile));
			SetOntologyID setid = new SetOntologyID(this.o, nuid);
			manager.applyChange(setid);

			// We should not need this, unless we are doing a 'save as' type of thing.
			//manager.setOntologyDocumentIRI(this.o, kidsDOCkb);

					/*
			// Save and re-load the ontology:
			for (OWLOntologyIRIMapper mpr : manager.getIRIMappers()){
				if (mpr.getDocumentIRI(getTBOXIRI()) != null){
					ontologyFile = mpr.getDocumentIRI(getTBOXIRI());
					try{
						File t = new File(ontologyFile.toString());
						this.logme.debug(String.format("1a. IRI: %s",ontologyFile.toString()));
						this.logme.debug(String.format("1b. IRI-URI: %s",ontologyFile.toURI()));
						this.logme.debug(String.format("1c. IRI-Parts: Scheme: %s ; Namespace: %s ; Fragment: %s",ontologyFile.getScheme(), ontologyFile.getNamespace(), ontologyFile.getRemainder()));
						this.logme.debug(String.format("2. File Path: %s",t.getPath()));
						this.logme.debug(String.format("3. File ABS Path: %s",t.getAbsolutePath()));
						this.logme.debug(String.format("4. File Can Path: %s",t.getCanonicalPath()));
						this.logme.debug(String.format("5. File URI: %s",t.toURI()));
						this.logme.debug(String.format("6. File URL: %s",t.toURI().toURL()));
						this.logme.debug(String.format("7. File URL XForm: %s",t.toURI().toURL().toExternalForm()));
						this.logme.debug(String.format("8. File IRI: %s",IRI.create(t.toURI()).toString()));
						this.logme.debug(String.format("9. Valid URI: %s",this.getValidFileURI(t)));
						this.logme.debug(String.format("Loading %s from location %s.", getTBOXIRI(), new File(ontologyFile.toString()).toURI().toURL()));
					} catch (IOException | URISyntaxException e){
						logme.error(String.format("Cannot load ontology %s from location %s:",getTBOXIRI(), ontologyFile.toString()), e);
					}
				}
			}
					*/
			//try {
			//manager.loadOntology(ontologyFile);
//				manager.loadOntologyFromOntologyDocument(IRI.create(getValidFileURI(new File(ontologyFile.toString()))));
			//} catch (URISyntaxException e){
						//logme.error(String.format("Cannot load ontology %s from location %s:",getTBOXIRI(), ontologyFile.toString()), e);
			//}
			manager.saveOntology(getOntology(), new OWLXMLDocumentFormat());
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
			    manager.getIRIMappers().add(imap);
			    if (imap.getDocumentIRI(kidskb) != null){
			    	fileIRI = imap.getDocumentIRI(kidskb);
			    }
			}
		}
		
		if (fileIRI == null){
			System.err.println("Could not identify file IRI for " + kidskb + "!");
			System.exit(1);
		} 
		//else {
			//try {
		//		fileIRI = IRI.create(this.getValidFileURI(new File(fileIRI.toString())));
			//} catch (URISyntaxException e) {
				//logme.error(String.format("Could not load ontology from %s:", fileIRI.toString()),e);
			//}
		logme.info(String.format("Loading from file %s", fileIRI.toString()));
		//}
		
		setOwlDataFactory(manager.getOWLDataFactory());
		try {
			setOntology(manager.loadOntologyFromOntologyDocument(fileIRI));
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
	 */
	public URI getValidFileURI(File target) throws URISyntaxException{
		return new URI(String.format("file://%s", target.getAbsolutePath()));
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
