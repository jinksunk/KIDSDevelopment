/**
 * 
 */
package net.strasnet.kids.ui.gui;

import java.awt.Color;
import java.awt.Component;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import javax.swing.JDialog;
import javax.swing.JFrame;

import net.strasnet.kids.KIDSOracle;
import net.strasnet.kids.measurement.KIDSMeasurementOracle;
import net.strasnet.kids.ui.RunStreamingKIDSDetector;
import net.strasnet.kids.ui.components.KIDSUIComponent;
import net.strasnet.kids.ui.components.KIDSUIEventComponent;
import net.strasnet.kids.ui.components.KIDSUIAbstractComponent.KIDSDatatypeClass;
import net.strasnet.kids.ui.gui.alerts.KIDSGUIAlert;
import net.strasnet.kids.ui.gui.alerts.KIDSGUIAlertError;
import net.strasnet.kids.ui.gui.alerts.KIDSGUIAlertInfo;
import net.strasnet.kids.ui.problems.KIDSUIProblem;

import org.apache.log4j.LogManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

/**
 * @author Chris Strasburg
 *
 * This class contains the methods that interact with the various KIDS components to build a valid ABOX.
 * It functions as the controller component in a standard MVC pattern UI framework.
 */
public class ABOXBuilderController {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8955009728682394035L;
	
	public static final IRI EVENTCLASSIRI = KIDSGUIOracle.eventClass;
	public static final IRI SIGNALCLASSIRI = KIDSGUIOracle.signalClass;
	public static final IRI DATASETCLASSIRI = KIDSGUIOracle.datasetClass;
	public static final IRI TIMEPERIODCLASSIRI = KIDSGUIOracle.timeperiodClass;
	public static final IRI DATASETVIEWCLASSIRI = KIDSGUIOracle.datasetViewClass;
	public static final IRI DATASETLABELCLASSIRI = KIDSGUIOracle.datasetLabelClass;
	public static final IRI DETECTORCLASSIRI = KIDSGUIOracle.detectorClass;
	public static final IRI SIGNALMANIFESTATIONCLASSIRI = KIDSGUIOracle.signalManifestationClass;
	public static final IRI SIGNALDOMAINCLASSIRI = KIDSGUIOracle.signalDomainClass;
	public static final IRI SIGNALDOMAINREPRESENTATIONCLASSIRI = KIDSGUIOracle.signalDomainRepresentationClass;
	public static final IRI SIGNALDOMAINCONTEXTCLASSIRI = KIDSGUIOracle.signalDomainContextClass;
	public static final IRI RESPONSECLASSIRI = KIDSGUIOracle.responseClass;
	public static final IRI SIGNALVALUECLASSIRI = KIDSGUIOracle.signalValueClass;
	public static final IRI RESOURCECLASSIRI = KIDSGUIOracle.resourceClass;
	public static final IRI DETECTORSYNTAXCLASSIRI = KIDSGUIOracle.detectorSyntaxClass;
	public static final IRI SIGNALCONSTRAINTCLASSIRI = KIDSGUIOracle.signalConstraintClass;

	/**
	 * 
	 */
	private static final List <IRI> supportedClasses = new ArrayList<IRI>();
	private static final Map <IRI, Class<? extends KIDSAddIndividualJDialog>> dialogDispatcher = new HashMap<IRI, Class<? extends KIDSAddIndividualJDialog>>();
	private static final Map <IRI, Class<? extends KIDSUIComponent>> iriToComponentMap = new HashMap<IRI, Class<? extends KIDSUIComponent>>();
	private static final Map <KIDSDatatypeClass, Class<? extends KIDSAddDataJDialog>> datatypeClassDispatcher = new HashMap<KIDSDatatypeClass, Class<? extends KIDSAddDataJDialog>>();
	
	static {
		datatypeClassDispatcher.put(KIDSDatatypeClass.JAVA, KIDSAddJavaClassJDialog.class);
		datatypeClassDispatcher.put(KIDSDatatypeClass.FILEPATH, KIDSAddFilepathDataJDialog.class);
		datatypeClassDispatcher.put(KIDSDatatypeClass.STRING, KIDSAddStringDataJDialog.class);
		
		dialogDispatcher.put(EVENTCLASSIRI, 
							 net.strasnet.kids.ui.gui.KIDSAddIndividualJDialog.class);
		iriToComponentMap.put(EVENTCLASSIRI, 
							 net.strasnet.kids.ui.components.KIDSUIEventComponent.class);
		supportedClasses.add(EVENTCLASSIRI);

		dialogDispatcher.put(SIGNALCLASSIRI, 
							 net.strasnet.kids.ui.gui.KIDSAddIndividualJDialog.class);
		iriToComponentMap.put(SIGNALCLASSIRI, 
							 net.strasnet.kids.ui.components.KIDSUISignalComponent.class);
		supportedClasses.add(SIGNALCLASSIRI);

		dialogDispatcher.put(DATASETCLASSIRI, 
							 net.strasnet.kids.ui.gui.KIDSAddIndividualJDialog.class);
		iriToComponentMap.put(DATASETCLASSIRI, 
							 net.strasnet.kids.ui.components.KIDSUIDatasetComponent.class);
		supportedClasses.add(DATASETCLASSIRI);
		
		dialogDispatcher.put(SIGNALVALUECLASSIRI, 
							 net.strasnet.kids.ui.gui.KIDSAddIndividualJDialog.class);
		iriToComponentMap.put(SIGNALVALUECLASSIRI, 
							 net.strasnet.kids.ui.components.KIDSUISignalValueComponent.class);
		supportedClasses.add(SIGNALVALUECLASSIRI);
		
		dialogDispatcher.put(TIMEPERIODCLASSIRI, 
							 net.strasnet.kids.ui.gui.KIDSAddIndividualJDialog.class);
		iriToComponentMap.put(TIMEPERIODCLASSIRI, 
							 net.strasnet.kids.ui.components.KIDSUITimePeriodComponent.class);
		supportedClasses.add(TIMEPERIODCLASSIRI);

		dialogDispatcher.put(SIGNALMANIFESTATIONCLASSIRI, 
							 net.strasnet.kids.ui.gui.KIDSAddIndividualJDialog.class);
		iriToComponentMap.put(SIGNALMANIFESTATIONCLASSIRI, 
							 net.strasnet.kids.ui.components.KIDSUISignalManifestationComponent.class);
		supportedClasses.add(SIGNALMANIFESTATIONCLASSIRI);

		dialogDispatcher.put(SIGNALDOMAINREPRESENTATIONCLASSIRI, 
							 net.strasnet.kids.ui.gui.KIDSAddIndividualJDialog.class);
		iriToComponentMap.put(SIGNALDOMAINREPRESENTATIONCLASSIRI, 
							 net.strasnet.kids.ui.components.KIDSUISignalDomainRepresentationComponent.class);
		supportedClasses.add(SIGNALDOMAINREPRESENTATIONCLASSIRI);

		dialogDispatcher.put(RESOURCECLASSIRI, 
							 net.strasnet.kids.ui.gui.KIDSAddIndividualJDialog.class);
		iriToComponentMap.put(RESOURCECLASSIRI, 
							 net.strasnet.kids.ui.components.KIDSUIResourceComponent.class);
		supportedClasses.add(RESOURCECLASSIRI);

		dialogDispatcher.put(RESPONSECLASSIRI, 
							 net.strasnet.kids.ui.gui.KIDSAddIndividualJDialog.class);
		iriToComponentMap.put(RESPONSECLASSIRI, 
							 net.strasnet.kids.ui.components.KIDSUIResponseComponent.class);
		supportedClasses.add(RESPONSECLASSIRI);

		dialogDispatcher.put(SIGNALDOMAINCLASSIRI, 
							 net.strasnet.kids.ui.gui.KIDSAddIndividualJDialog.class);
		iriToComponentMap.put(SIGNALDOMAINCLASSIRI, 
							 net.strasnet.kids.ui.components.KIDSUISignalDomainComponent.class);
		supportedClasses.add(SIGNALDOMAINCLASSIRI);

		dialogDispatcher.put(SIGNALDOMAINCONTEXTCLASSIRI, 
							 net.strasnet.kids.ui.gui.KIDSAddIndividualJDialog.class);
		iriToComponentMap.put(SIGNALDOMAINCONTEXTCLASSIRI, 
							 net.strasnet.kids.ui.components.KIDSUISignalDomainContextComponent.class);
		supportedClasses.add(SIGNALDOMAINCONTEXTCLASSIRI);

		dialogDispatcher.put(DATASETVIEWCLASSIRI, 
							 net.strasnet.kids.ui.gui.KIDSAddIndividualJDialog.class);
		iriToComponentMap.put(DATASETVIEWCLASSIRI, 
							 net.strasnet.kids.ui.components.KIDSUIDatasetViewComponent.class);
		supportedClasses.add(DATASETVIEWCLASSIRI);

		dialogDispatcher.put(DATASETLABELCLASSIRI, 
							 net.strasnet.kids.ui.gui.KIDSAddIndividualJDialog.class);
		iriToComponentMap.put(DATASETLABELCLASSIRI, 
							 net.strasnet.kids.ui.components.KIDSUIDatasetLabelComponent.class);
		supportedClasses.add(DATASETLABELCLASSIRI);

		dialogDispatcher.put(DETECTORCLASSIRI, 
							 net.strasnet.kids.ui.gui.KIDSAddIndividualJDialog.class);
		iriToComponentMap.put(DETECTORCLASSIRI, 
							 net.strasnet.kids.ui.components.KIDSUIDetectorComponent.class);
		supportedClasses.add(DETECTORCLASSIRI);
	};
	
	/* Enable Logging */
	public static final org.apache.log4j.Logger logme = LogManager.getLogger(ABOXBuilderController.class.getName());
	
	KIDSGUIOracle o = null;
	BlockingQueue <KIDSGUIAlert> ourLog = null;
	List <KIDSGUIStatus> statusList = null;
	
	private List<AddEventListener> addEventListeners = new LinkedList<AddEventListener>();
	private List<OntologyModifiedListener> ontologyModifiedListeners = new LinkedList<OntologyModifiedListener>();
	private Map<IRI, KIDSUIComponent> loadedComponents = new HashMap<IRI, KIDSUIComponent>();
	
	public ABOXBuilderController(BlockingQueue<KIDSGUIAlert> logMessages){
		super();
		ourLog = logMessages;

		// Load the status list:
		statusList = new LinkedList<KIDSGUIStatus>();
		statusList.add((KIDSGUIStatus) this.new OntologyLoadedGUIStatus());

		logappend(new KIDSGUIAlertInfo("KIDS Controller initialized."));
	}
	
	/** ************************************************************************************************
	 *  Methods that manage the ontology itself (saving, loading, initializing, etc...
	 ** ***********************************************************************************************/


	/**
	 * Will write the ontology to a file.
	 * @return true
	 * @throws OWLOntologyStorageException
	 */
	public boolean save() throws OWLOntologyStorageException{
		OWLOntologyManager om = o.getOntologyManager();
		OWLOntology onto = o.getOntology();
		logme.debug(String.format("Saving ontology to %s in format %s (%d axioms)", 
				om.getOntologyDocumentIRI(onto),
				om.getOntologyFormat(onto),
				onto.getAxiomCount()));
		BufferedOutputStream outfile;
		try {
			File ofile = new File(om.getOntologyDocumentIRI(onto).toURI().getPath());
			outfile = new BufferedOutputStream(new FileOutputStream(ofile));
			om.saveOntology(onto, 
				om.getOntologyFormat(onto),
				outfile);
		} catch (FileNotFoundException e) {
			logme.warn(String.format("Could not save file %s: %s", om.getOntologyDocumentIRI(onto), e.getMessage()));
			return false;
		}

		return true;
	}
	
	/*
	 * Common initialization code
	 * @return true if the initialization was successful, false if something went wrong
	 * @throws OWLOntologyCreationException - If the ontology could not be created / loaded for some reason.
	 */
	protected List<SimpleIRIMapper> init(String TBOXLocation, IRI TBIRI, 
						String ABOXLocation, IRI ABIRI) throws OWLOntologyCreationException{
		
		// Create a new oracle
		o = new KIDSGUIOracle();
		List<SimpleIRIMapper> m = new LinkedList<SimpleIRIMapper>();
		
		logme.debug(String.format("Adding mapping [%s] -> [%s]", TBIRI.toString(), TBOXLocation));
		m.add(new SimpleIRIMapper(TBIRI, IRI.create(TBOXLocation)));
		logme.debug(String.format("Adding mapping [%s] -> [%s]", ABIRI.toString(), ABOXLocation));
		m.add(new SimpleIRIMapper(ABIRI, IRI.create(ABOXLocation)));
		
		return m;
	}
	/**
	 * Initializes the controller with the given TBOXLocation, TBOXIRI, and ABOXIRI. 
	 * @param TBOXLocation The physical location of the TBOX to load.
	 * @param TBOXIRI The IRI of the TBOX we are loading (may, but need not, match the physical location)
	 * @param ABOXIRI The IRI of the ABOX we will create
	 * @return true if the initialization was successful, false if something went wrong
	 * @throws OWLOntologyCreationException - If the ontology could not be created / loaded for some reason.
	 * @throws OWLOntologyStorageException 
	 */
	public boolean initNew(String TBOXLocation, String TBOXIRI, 
						String ABOXLocation, String ABOXIRI) throws OWLOntologyCreationException, OWLOntologyStorageException{
		// Ensure the IRIs are valid:
		IRI TBIRI = IRI.create(TBOXIRI);
		IRI ABIRI = IRI.create(ABOXIRI);

		List<SimpleIRIMapper> m = init(TBOXLocation, TBIRI, ABOXLocation, ABIRI);

		o.createKIDS(ABIRI, TBIRI, m);
		
		logappend(new KIDSGUIAlertInfo(String.format("Created new ontology %s [TBOX: %s] with %d assertions (Consistant: %b)",
													  ABIRI.toString(), TBIRI.toString(), 
													  o.getOntology().getAxiomCount(),
													  o.getReasoner().isConsistent())));
		return true;
	}

	/**
	 * Initializes the controller with the given TBOXLocation, TBOXIRI, and ABOXIRI. 
	 * @param TBOXLocation The physical location of the TBOX to load.
	 * @param TBOXIRI The IRI of the TBOX we are loading (may, but need not, match the physical location)
	 * @param ABOXIRI The IRI of the ABOX we will create
	 * @return true if the initialization was successful, false if something went wrong
	 * @throws OWLOntologyCreationException - If the ontology could not be created / loaded for some reason.
	 */
	public boolean initExisting(String TBOXLocation, String TBOXIRI, 
						String ABOXLocation, String ABOXIRI) throws OWLOntologyCreationException{
		// Ensure the IRIs are valid:
		IRI TBIRI = IRI.create(TBOXIRI);
		IRI ABIRI = IRI.create(ABOXIRI);
		
		List<SimpleIRIMapper> m = init(TBOXLocation, TBIRI, ABOXLocation, ABIRI);

		// Create a new oracle
		o.loadKIDS(ABIRI, m);
		
		logappend(new KIDSGUIAlertInfo(String.format("Loaded existing ontology %s [TBOX: %s] with %d assertions (Consistant: %b)",
													  ABIRI.toString(), TBIRI.toString(), 
													  o.getOntology().getAxiomCount(),
													  o.getReasoner().isConsistent())));
		
		// TODO: Update all component class lists from KB:
		//getKnownIndividuals();
		//getKnownDatasets();
		//getKnownSignals();
		// ...
		for (IRI supportedClass : ABOXBuilderController.supportedClasses){
			getKnownIndividuals(supportedClass);
		}
		
		fireOntologyModifiedEvent(o);
		
		return true;
	}
	
	/** ************************************************************************************************
	 *  Methods that handle controller events / listeners
	 ** ***********************************************************************************************/

	/* Ontology modified event handling */
	public synchronized void addOntologyModifiedListener(OntologyModifiedListener l){
		ontologyModifiedListeners.add(l);
	}

	public synchronized void removeOntologyModifiedListener(OntologyModifiedListener l){
		ontologyModifiedListeners.remove(l);
	}
	
	public synchronized void fireOntologyModifiedEvent(KIDSGUIOracle o){
		for (OntologyModifiedListener l : ontologyModifiedListeners){
			l.ontologyModified(o);
		}
	}

	/* Individual added event handling */
	public synchronized void addIndividualAddedListener(AddEventListener l){
		addEventListeners.add(l);
	}

	public synchronized void removeIndividualAddedListener(AddEventListener l){
		addEventListeners.remove(l);
	}
	
	public synchronized void fireIndividualAddedEvent(IRI newIndividual){
		for (AddEventListener l : addEventListeners){
			l.newEventReceived(newIndividual);
		}
	}

	/** ************************************************************************************************
	 *  Methods that query the knowledge base:
	 ** ***********************************************************************************************/

	/**
	 *  Will query the ontology for the current list of all known individuals of the given class; returns a list of IRIs.
	 * @param indClass - The IRI of the class we want individuals of
	 * @return The list of all known event; an empty list is possible.
	 * @throws InstantiationException 
	 */
	public Set<KIDSUIComponent> getKnownIndividuals(IRI indClass){
		Set<KIDSUIComponent> toReturn = new HashSet<KIDSUIComponent>();

		Iterator<OWLNamedIndividual> individuals = o.getIndividuals(o.getOwlDataFactory().getOWLClass(indClass));
		
		
		IRI i = null;
		while (individuals.hasNext()){
			i = individuals.next().getIRI();
			try {
				// TODO: Is the second argument necessary?
				toReturn.add(this.getUIComponentForClass(indClass, i));
			} catch (InstantiationException e){
				logme.error(String.format("Could not instantiate component for %s (%s): %s", i, indClass, e.getMessage()));
			}

			/*
			if (!this.loadedComponents.containsKey(i)){
				logme.warn(String.format("Individual %s from ontology not known to controller; adding", i.getFragment()));
				try {
					addComponentToList(i, getUIComponentForClass(indClass, i));
				} catch (InstantiationException e){
					logme.error(String.format("Could not instantiate component for %s (%s): %s", i, indClass, e.getMessage()));
				}
			}
			toReturn.add(i);
			*/
		}

		logme.debug(String.format("Found %d individuals from class %s.", toReturn.size(), indClass));
		return toReturn;
	}
	
	/** ************************************************************************************************
	 *  Methods that manage dynamic UI components
	 ** ***********************************************************************************************/

	protected KIDSUIComponent getUIComponentForClass(IRI indClass, IRI i) throws InstantiationException{
		Class<? extends KIDSUIComponent> cclass = iriToComponentMap.get(indClass);

		if (cclass == null){
			logme.warn(String.format("No constructor defined in IRI->Component map for class %s", indClass));
			return null;
		}
		
		Constructor<? extends KIDSUIComponent> classCon;
		logme.debug(String.format("Getting UI component for %s (%s)", i, indClass));
		
		if (loadedComponents.containsKey(i)){
			logme.debug(String.format("Reusing component for individual %s", i));
			return loadedComponents.get(i);
		}

		try {
			classCon = cclass.getConstructor(IRI.class, KIDSGUIOracle.class);
			return classCon.newInstance(i, o);
		} catch (NoSuchMethodException e){
			logme.error(String.format("No constructor available for class %s: ", indClass, e.getMessage()));
			throw new InstantiationException(e.getMessage());
		} catch (SecurityException e) {
			logme.error(String.format("Security exception for class %s: %s", indClass, e.getMessage()));
			throw new InstantiationException(e.getMessage());
		} catch (IllegalArgumentException e) {
			logme.error(String.format("Illegal argument exception for class %s: %s", indClass, e.getMessage()));
			throw new InstantiationException(e.getMessage());
		} catch (InvocationTargetException e) {
			logme.error(String.format("Invocation target exception for class %s: %s", indClass, e.getCause().getMessage()));
			e.getCause().printStackTrace();
		} catch (IllegalAccessException e) {
			logme.error(String.format("Illegal access exception for class %s: %s", indClass, e.getMessage()));
			throw new InstantiationException(e.getMessage());
		}
		
		return null;
	}

	/**
	 * Given the KIDSdatatype, will return an instance of JDialog to create an instance of it.
	 * @param KIDSDatatype - the datatype to instantiate
	 * @param frame - the GUI parent
	 * @param subjectIRI - the IRI of the subject
	 * @param propertyIRI - the IRI of the property we are adding to
	 * @return - an instance of KIDSAddDataJDialog to add this data element
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public KIDSAddDataJDialog getAddDataValueDialogForClass(KIDSDatatypeClass ourClass, JFrame frame,
			IRI subjectIRI, IRI propertyIRI) throws InstantiationException, IllegalAccessException {
		logme.debug(String.format("Getting add data value dialog for data value class %s", ourClass));
		if (ABOXBuilderController.datatypeClassDispatcher.containsKey(ourClass)){
			Class<? extends KIDSAddDataJDialog> dialog = ABOXBuilderController.datatypeClassDispatcher.get(ourClass);
			Constructor<? extends KIDSAddDataJDialog> ctor;
			try {
				ctor = dialog.getConstructor(JFrame.class, 
						IRI.class, // ABOXIRI
						IRI.class, // SubjectIRI
						IRI.class, // PropertyIRI
						this.getClass());
				KIDSAddDataJDialog ourDialog = (KIDSAddDataJDialog)ctor.newInstance(frame, 
						o.getABOXIRI(), 
						subjectIRI,
						propertyIRI,
						this);
				return ourDialog;
			} catch (NoSuchMethodException e) {
				logme.error(String.format("No constructor available with JFrame argument: %s", e.getMessage()));
				throw new InstantiationException(e.getMessage());
			} catch (SecurityException e) {
				logme.error(String.format("Security exception: %s", e.getMessage()));
				throw new InstantiationException(e.getMessage());
			} catch (IllegalArgumentException e) {
				logme.error(String.format("Illegal argument exception: %s", e.getMessage()));
				throw new InstantiationException(e.getMessage());
			} catch (InvocationTargetException e) {
				logme.error(String.format("Invocation target exception: %s", e.getCause().getMessage()));
				e.getCause().printStackTrace();
				throw new InstantiationException(e.getMessage());
			}
		} else {
			logme.warn(String.format("No class mapping for class %s", ourClass));
		    return null;
		}
	}
	
	/**
	 * Given the class IRI, will return an instance of JDialog to create an instance of it.
	 * @param ourClass
	 * @param frame
	 * @return
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public KIDSAddIndividualJDialog getAddInstanceDialogForClass(IRI ourClass, JFrame frame) throws InstantiationException, IllegalAccessException {
		logme.debug(String.format("Getting add individual dialog for class %s", ourClass));
		if (ABOXBuilderController.dialogDispatcher.containsKey(ourClass)){
			Class<? extends KIDSAddIndividualJDialog> dialog = ABOXBuilderController.dialogDispatcher.get(ourClass);
			Constructor<? extends KIDSAddIndividualJDialog> ctor;
			try {
				ctor = dialog.getConstructor(JFrame.class, IRI.class, IRI.class, this.getClass());
				logme.debug(String.format("Adding %s as the parent of %s...", frame, dialog));
				KIDSAddIndividualJDialog ourDialog = (KIDSAddIndividualJDialog)ctor.newInstance(
						frame, 
						o.getABOXIRI(), 
						ourClass,
						this);
				return ourDialog;
			} catch (NoSuchMethodException e) {
				logme.error(String.format("No constructor available with JFrame argument: %s", e.getMessage()));
				throw new InstantiationException(e.getMessage());
			} catch (SecurityException e) {
				logme.error(String.format("Security exception: %s", e.getMessage()));
				throw new InstantiationException(e.getMessage());
			} catch (IllegalArgumentException e) {
				logme.error(String.format("Illegal argument exception: %s", e.getMessage()));
				throw new InstantiationException(e.getMessage());
			} catch (InvocationTargetException e) {
				logme.error(String.format("Invocation target exception: %s", e.getCause().getMessage()));
				e.getCause().printStackTrace();
				throw new InstantiationException(e.getMessage());
			}
		} else {
			logme.warn(String.format("No class mapping for class %s", ourClass));
		    return null;
		}
	}
	
	
	
	/** ************************************************************************************************
	 *  Methods that handle logging and status management
	 ** ***********************************************************************************************/

	public List<KIDSGUIStatus> getStatus(){
		return this.statusList;
	}
	
	/**
	 * Append a message to the log, removing oldest entries as necessary.
	 */
	public void logappend(KIDSGUIAlert m){
		while (!this.ourLog.offer(m)){
			this.ourLog.remove();
		}
	}
	
	/**
	 * A convenience method to add an informational alert to the log queue:
	 * @param m
	 */
	public void logappendInfo(String msg){
		KIDSGUIAlert m = new KIDSGUIAlertInfo(msg);
		logappend(m);
	}

	/**
	 * A convenience method to add an informational alert to the log queue:
	 * @param m
	 */
	public void logappendError(String msg){
		KIDSGUIAlert m = new KIDSGUIAlertError(msg);
		logappend(m);
	}
	
	
	/**
	 * Return the prefix string for the current ontology:
	 */
	public IRI getABOXPrefix(){
		return o.getABOXIRI();
	}

	/**
	 * Return the prefix string for the imported TBOX:
	 */
	public IRI getTBOXPrefix(){
		return o.getTBOXIRI();
	}

	/**
	 * For each status check defined, see if it is true; if so, add it to the current status.
	 */
	protected void checkStatus(){
		//TODO: Add status classes / checks

	}
	
	/*
	 * Here's where we define various status checks to perform:
	 * @author Chris Strasburg
	 *
	 */
	interface KIDSGUIStatus {
		public boolean checkStatus();
		public String toString();
		public Color getColor();
	}
	
	private abstract class KIDSGUIStatusAbstractClass {
		String message = null;
		Color myColor = null;
		public String toString(){
			return message;
		}
		
		public Color getColor(){
			return myColor;
		}
	}
	
	/* Check to see if the ontology has been loaded: */
	private class OntologyLoadedGUIStatus extends KIDSGUIStatusAbstractClass implements KIDSGUIStatus {
		public OntologyLoadedGUIStatus(){
			message = "Ontology not loaded.";
			myColor = Color.RED;
		}

		public boolean checkStatus(){
			return ((o != null) && o.getOntology() != null);
		}

	}

	/**
	 * Return an absolute ABOX IRI:
	 */
	public IRI getAbsoluteIRI(IRI docIRI, IRI source){
		
		if (!docIRI.equals(this.getABOXPrefix()) && 
		    !docIRI.equals(this.getTBOXPrefix())){
			logme.warn(String.format("Given document IRI %s doesn't match the loaded ABOX (%s) or TBOX(%s)",
					docIRI, getABOXPrefix(), getTBOXPrefix()));
		}
		
		IRI toReturn = source;
		
		if (!toReturn.isAbsolute()){
			StringBuilder toReturnString = new StringBuilder(docIRI.toString());

			if (!source.toString().startsWith("#")){
				toReturnString.append("#");
			}
			toReturnString.append(source.toString());
			toReturn = IRI.create(toReturnString.toString());
		}

		/* No longer necessary?
		if (toReturn.getStart() != null && !toReturn.getStart().endsWith("#")){
			toReturn = IRI.create(String.format(
				"%s#%s", 
					toReturn.getStart(),
					toReturn.getFragment()));
		}
		*/
		
		String prefix = String.format("%s ->", toReturn);
		logme.debug(String.format("Properties of IRI %s:", toReturn));
	
		logme.debug(String.format("%s Start: %s", prefix, toReturn.getNamespace()));
		logme.debug(String.format("%s Scheme: %s", prefix, toReturn.getScheme()));
		logme.debug(String.format("%s Fragment: %s", prefix, toReturn.getShortForm()));
		logme.debug(String.format("%s Source: %s", prefix, source));
		logme.debug(String.format("%s Is absolute: %s", prefix, toReturn.isAbsolute()));
		logme.debug(String.format("%s length: %d", prefix, toReturn.length()));
		
		return toReturn;
		
	}
	
	/**
	 * Return a set of all named individuals currently known to the ontology:
	 */
	public Set<IRI> getKnownIndividuals(){
		Set<IRI> toReturn = new HashSet<IRI>();
		Set<OWLNamedIndividual> oniSet = o.getIndividualSet(o.getOwlDataFactory().getOWLThing());
		for (OWLNamedIndividual oni : oniSet){
			toReturn.add(oni.getIRI());
			Set<IRI> classes = o.getTypesForIndividual(oni.getIRI());
			StringBuilder classesString = new StringBuilder();
			for (IRI classiri : classes){
				classesString.append(String.format("%s,", classiri));
			}
			logme.debug(String.format("Individual %s belongs to classes %s.", oni.getIRI(), classesString.toString()));
		}
		return toReturn;
	}
	
	/** ************************************************************************************************
	 *  Methods that modify the Ontology go here - each should fire events to ensure that listeners are
	 *  notified when the ontology changes.
	 ** ***********************************************************************************************/

	/**
	 * Adds the provided data value to the given data property for the given individual
	 * @param relation - the Data property to add the value for
	 * @param subjectIRI - the subject individual we are adding data to
	 * @param value - the value to add
	 */
	public void addDataValueForIndividual(IRI relation, IRI subjectIRI, String value) {
		logme.debug(String.format("Adding tuple (%s, %s, %s) to kb.", subjectIRI, relation, value));
		o.addDataPropertyToIndividual(subjectIRI, relation, value);
	}

	/**
	 * Add a new event to the ontology / model:
	 * @return
	 * @throws OWLOntologyStorageException
	 */
	public void addIndividual(IRI indIRI, IRI classIRI){
		int preAddAxiomCount = o.getOntology().getAxiomCount();
		o.addIndividual(indIRI, classIRI);
		logme.debug(String.format("Added individual %s, class %S (Pre-add count %d, post-add count %d).",
				indIRI, classIRI, preAddAxiomCount, o.getOntology().getAxiomCount()));

		this.fireIndividualAddedEvent(indIRI);
	}
	
	/**
	 * A convenience method to hide the necessity of working with IRIs from the GUI.
	 * @param evIRI - the IRI of the event to add.
	 */
	public void addEvent(IRI evIRI){
		addIndividual(evIRI, KIDSOracle.eventClass);
	}
	
	/**
	 * Add the specified relation to the ontology (using the oracle), and fire an 
	 * 'ontologyModified' event (?)
	 * @param thisEvent
	 * @param relation
	 * @param object
	 */
	public void addRelation(IRI subject, IRI predicate, IRI object) {
		boolean subexists = o.containsIndividual(subject);
		boolean objexists = o.containsIndividual(object);
		o.addRelation(subject, predicate, object);

		// If either the subject or object are not known, fire an individualAdded event:
		if (! subexists){
			logme.debug(String.format("New individual in relation: adding %s...", subject));
			this.fireIndividualAddedEvent(subject);
		}
		
		if (! objexists){
			logme.debug(String.format("New individual in relation: adding %s...", object));
			this.fireIndividualAddedEvent(object);
		}
		
		this.fireOntologyModifiedEvent(o);
	}


}
