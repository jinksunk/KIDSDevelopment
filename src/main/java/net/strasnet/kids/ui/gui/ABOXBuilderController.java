/**
 * 
 */
package net.strasnet.kids.ui.gui;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import net.strasnet.kids.measurement.KIDSMeasurementOracle;
import net.strasnet.kids.ui.RunStreamingKIDSDetector;
import net.strasnet.kids.ui.gui.alerts.KIDSGUIAlert;
import net.strasnet.kids.ui.gui.alerts.KIDSGUIAlertInfo;

import org.apache.logging.log4j.LogManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

/**
 * @author Chris Strasburg
 *
 * This class contains the methods that interact with the various KIDS components to build a valid ABOX.
 * It functions as the controller component in a standard MVC pattern UI framework.
 */
public class ABOXBuilderController {
	
	/* Enable Logging */
	public static final org.apache.logging.log4j.Logger logme = LogManager.getLogger(ABOXBuilderController.class.getName());

	KIDSGUIOracle o = null;
	BlockingQueue <KIDSGUIAlert> ourLog = null;
	List <KIDSGUIStatus> statusList = null;
	
	public ABOXBuilderController(BlockingQueue<KIDSGUIAlert> logMessages){
		super();
		ourLog = logMessages;

		// Load the status list:
		statusList = new LinkedList<KIDSGUIStatus>();
		statusList.add((KIDSGUIStatus) this.new OntologyLoadedGUIStatus());

		logappend(new KIDSGUIAlertInfo("KIDS Controller initialized."));
	}
	
	/**
	 * Initializes the controller with the given TBOXLocation, TBOXIRI, and ABOXIRI. 
	 * @param TBOXLocation The physical location of the TBOX to load.
	 * @param TBOXIRI The IRI of the TBOX we are loading (may, but need not, match the physical location)
	 * @param ABOXIRI The IRI of the ABOX we will create
	 * @return true if the initialization was successful, false if something went wrong
	 * @throws OWLOntologyCreationException - If the ontology could not be created / loaded for some reason.
	 */
	public boolean init(String TBOXLocation, String TBOXIRI, 
						String ABOXLocation, String ABOXIRI) throws OWLOntologyCreationException{
		// Ensure the IRIs are valid:
		IRI TBIRI = IRI.create(TBOXIRI);
		IRI ABIRI = IRI.create(ABOXIRI);
		
		// Create a new oracle
		o = new KIDSGUIOracle();
		List<SimpleIRIMapper> m = new LinkedList<SimpleIRIMapper>();
		m.add(new SimpleIRIMapper(TBIRI, IRI.create(TBOXLocation)));
		m.add(new SimpleIRIMapper(ABIRI, IRI.create(ABOXLocation)));
		
		o.createKIDS(ABIRI, m);
		
		return true;
	}
	
	/**
	 * Add a new event to the ontology / model:
	 * @return
	 * @throws OWLOntologyStorageException
	 */
	public void addEvent(String eventIRI){
		o.addEvent(IRI.create(eventIRI));
	}

	public boolean save() throws OWLOntologyStorageException{
		o.getOntologyManager().saveOntology(o.getOntology());
		return true;
	}
	
	public List<KIDSGUIStatus> getStatus(){
		return this.statusList;
	}
	
	/*
	 * Append a message to the log, removing oldest entries as necessary.
	 */
	private void logappend(KIDSGUIAlert m){
		while (!this.ourLog.offer(m)){
			this.ourLog.remove();
		}
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
	private class OntologyLoadedGUIStatus extends KIDSGUIStatusAbstractClass {
		public OntologyLoadedGUIStatus(){
			message = "Ontology not loaded.";
			myColor = Color.RED;
		}

		public boolean checkStatus(){
			return ((o != null) && o.getOntology() != null);
		}

	}

}
