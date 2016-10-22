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
import net.strasnet.kids.ui.KIDSUIComponent;
import net.strasnet.kids.ui.KIDSUIEventComponent;
import net.strasnet.kids.ui.KIDSUIProblem;
import net.strasnet.kids.ui.RunStreamingKIDSDetector;
import net.strasnet.kids.ui.gui.alerts.KIDSGUIAlert;
import net.strasnet.kids.ui.gui.alerts.KIDSGUIAlertError;
import net.strasnet.kids.ui.gui.alerts.KIDSGUIAlertInfo;

import org.apache.logging.log4j.LogManager;
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

	/**
	 * 
	 */
	private static final List <IRI> supportedClasses = new ArrayList<IRI>();
	private static final Map <IRI, Class<? extends KIDSAddIndividualJDialog>> dialogDispatcher = new HashMap<IRI, Class<? extends KIDSAddIndividualJDialog>>();
	private static final Map <IRI, Class<? extends KIDSUIComponent>> iriToComponentMap = new HashMap<IRI, Class<? extends KIDSUIComponent>>();
	
	static {
		dialogDispatcher.put(EVENTCLASSIRI, 
							 net.strasnet.kids.ui.gui.AddEventJDialog.class);
		iriToComponentMap.put(EVENTCLASSIRI, 
							 net.strasnet.kids.ui.KIDSUIEventComponent.class);
		supportedClasses.add(EVENTCLASSIRI);

		dialogDispatcher.put(SIGNALCLASSIRI, 
							 net.strasnet.kids.ui.gui.AddSignalJDialog.class);
		iriToComponentMap.put(SIGNALCLASSIRI, 
							 net.strasnet.kids.ui.KIDSUISignalComponent.class);
		supportedClasses.add(SIGNALCLASSIRI);

		dialogDispatcher.put(DATASETCLASSIRI, 
							 net.strasnet.kids.ui.gui.AddDatasetJDialog.class);
		iriToComponentMap.put(DATASETCLASSIRI, 
							 net.strasnet.kids.ui.KIDSUIDatasetComponent.class);
		supportedClasses.add(DATASETCLASSIRI);
	};
	
	/* Enable Logging */
	public static final org.apache.logging.log4j.Logger logme = LogManager.getLogger(ABOXBuilderController.class.getName());
	
	KIDSGUIOracle o = null;
	BlockingQueue <KIDSGUIAlert> ourLog = null;
	List <KIDSGUIStatus> statusList = null;
	
	private List<AddEventListener> addEventListeners = new LinkedList<AddEventListener>();
	private List<OntologyLoadedListener> ontologyLoadedListeners = new LinkedList<OntologyLoadedListener>();
	private Map<IRI, KIDSUIComponent> loadedComponents = new HashMap<IRI, KIDSUIComponent>();
	
	public ABOXBuilderController(BlockingQueue<KIDSGUIAlert> logMessages){
		super();
		ourLog = logMessages;

		// Load the status list:
		statusList = new LinkedList<KIDSGUIStatus>();
		statusList.add((KIDSGUIStatus) this.new OntologyLoadedGUIStatus());

		logappend(new KIDSGUIAlertInfo("KIDS Controller initialized."));
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
		
		fireOntologyLoadedEvent(o);
		
		return true;
	}
	
	public synchronized void addOntologyLoadedListener(OntologyLoadedListener l){
		ontologyLoadedListeners.add(l);
	}

	public synchronized void removeOntologyLoadedListener(OntologyLoadedListener l){
		ontologyLoadedListeners.remove(l);
	}
	
	public synchronized void fireOntologyLoadedEvent(KIDSGUIOracle o){
		for (OntologyLoadedListener l : ontologyLoadedListeners){
			l.ontologyLoaded(o);
		}
	}

	/**
	 * Add a new event to the ontology / model:
	 * @return
	 * @throws OWLOntologyStorageException
	 */
	public void addEvent(IRI evIRI){
		int preAddAxiomCount = o.getOntology().getAxiomCount();
		o.addEvent(evIRI);
		logme.debug(String.format("Added event %s to the ontology (Pre-add count %d, post-add count %d).",
				evIRI, preAddAxiomCount, o.getOntology().getAxiomCount()));
		addComponentToList(evIRI, new KIDSUIEventComponent(evIRI, o));

		this.fireIndividualAddedEvent(evIRI);
	}
	
	public synchronized void addIndividualAddedListener(AddEventListener l){
		addEventListeners.add(l);
	}

	public synchronized void removeIndividualAddedListener(AddEventListener l){
		addEventListeners.remove(l);
	}
	
	public synchronized void fireIndividualAddedEvent(IRI newEvent){
		for (AddEventListener l : addEventListeners){
			l.newEventReceived(newEvent);
		}
	}

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
	
	/**
	 *  Will query the ontology for the current list of all known individuals of the given class; returns a list of IRIs.
	 * @param indClass - The IRI of the class we want individuals of
	 * @return The list of all known event; an empty list is possible.
	 * @throws InstantiationException 
	 */
	public Set<IRI> getKnownIndividuals(IRI indClass){
		Set<IRI> toReturn = new HashSet<IRI>();

		Iterator<OWLNamedIndividual> individuals = o.getIndividuals(o.getOwlDataFactory().getOWLClass(indClass));
		
		IRI i = null;
		while (individuals.hasNext()){
			i = individuals.next().getIRI();
			if (!this.loadedComponents.containsKey(i)){
				logme.warn(String.format("Individual %s from ontology not known to controller; adding", i.getFragment()));
				try {
					addComponentToList(i, getUIComponentForClass(indClass, i));
				} catch (InstantiationException e){
					logme.error(String.format("Could not instantiate component for %s (%s): %s", i, indClass, e.getMessage()));
				}
			}
			toReturn.add(i);
		}

		logme.debug(String.format("Found %d individuals from class %s.", toReturn.size(), indClass));
		return toReturn;
	}
	
	protected KIDSUIComponent getUIComponentForClass(IRI indClass, IRI i) throws InstantiationException{
		Class<? extends KIDSUIComponent> cclass = iriToComponentMap.get(indClass);
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
	
	public List<KIDSGUIStatus> getStatus(){
		return this.statusList;
	}
	
	private void addComponentToList(IRI newObjIRI, KIDSUIComponent newUIC){
		if (loadedComponents.containsKey(newObjIRI)){
			logme.warn(String.format("Object %s already loaded; replacing.", 
				newObjIRI.getFragment()));
			}
		loadedComponents.put(newObjIRI, new KIDSUIEventComponent(newObjIRI, o));
	}
	
	/**
	 * 
	 */
	public List<KIDSUIProblem> getProblems(IRI KIDSComponentIRI){
		List<KIDSUIProblem> toReturn = new LinkedList<KIDSUIProblem>();
		
		if (loadedComponents.containsKey(KIDSComponentIRI)){
			KIDSUIComponent kuc = loadedComponents.get(KIDSComponentIRI);
			toReturn.addAll(kuc.getComponentProblems());
		}
		
		logme.debug(String.format("Identified %d problems for %s.", toReturn.size(), KIDSComponentIRI.getFragment()));

		return toReturn;
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
		if (ABOXBuilderController.dialogDispatcher.containsKey(ourClass)){
			Class<? extends KIDSAddIndividualJDialog> dialog = ABOXBuilderController.dialogDispatcher.get(ourClass);
			Constructor<? extends KIDSAddIndividualJDialog> ctor;
			try {
				ctor = dialog.getConstructor(JFrame.class, IRI.class, this.getClass());
				logme.debug(String.format("Adding %s as the parent of %s...", frame, dialog));
				KIDSAddIndividualJDialog ourDialog = (KIDSAddIndividualJDialog)ctor.newInstance(frame, o.getABOXIRI(), this);
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
	 * Return an absolute ABOX IRI:
	 */
	public IRI getAbsoluteIRI(IRI docIRI, IRI source){
		
		if (!docIRI.equals(this.getABOXPrefix()) && 
		    !docIRI.equals(this.getTBOXPrefix())){
			logme.warn("Given document IRI %s doesn't match the loaded ABOX (%s) or TBOX(%s)",
					docIRI, getABOXPrefix(), getTBOXPrefix());
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
	
		logme.debug(String.format("%s Start: %s", prefix, toReturn.getStart()));
		logme.debug(String.format("%s Scheme: %s", prefix, toReturn.getScheme()));
		logme.debug(String.format("%s Fragment: %s", prefix, toReturn.getFragment()));
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

}
