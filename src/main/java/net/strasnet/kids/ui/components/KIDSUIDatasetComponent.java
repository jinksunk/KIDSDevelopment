/**
 * 
 */
package net.strasnet.kids.ui.components;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.log4j.LogManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;

import net.strasnet.kids.KIDSOntologyDatatypeValuesException;
import net.strasnet.kids.KIDSOracle;
import net.strasnet.kids.ui.KIDSUIInferredProperty;
import net.strasnet.kids.ui.KIDSUIRequiredDataProperty;
import net.strasnet.kids.ui.KIDSUIRequiredProperty;
import net.strasnet.kids.ui.gui.KIDSGUIOracle;

/**
 * @author cstras
 * 
 * This class represents an Event for UI purposes in KIDS.
 */
public class KIDSUIDatasetComponent extends KIDSUIAbstractComponent implements KIDSUIComponent {
	
	public static final org.apache.log4j.Logger logme = LogManager.getLogger(KIDSUIDatasetComponent.class.getName());

	private static final Map<String, String> reqProps = new HashMap<String, String>();

	private static final Map<String, String> infProps = new HashMap<String, String>();
	static {
		infProps.put("#includesTimePeriod","#TimePeriod");
		infProps.put("#isCompatibleDatasetForSignal","#Signal");
		infProps.put("#hasEventIncluded","#Event");
		infProps.put("#isEvaluationOf","#Event");
		infProps.put("#isContainerOf","#SignalDomainContext");
		infProps.put("#isViewableAs","#DatasetView");
	};

	private static final Map<String, KIDSDatatypeClass> datProps = new HashMap<String, KIDSDatatypeClass>();
	static {
		datProps.put("#datasetLocation", KIDSDatatypeClass.FILEPATH);
		datProps.put("#datasetParserImplementation",KIDSDatatypeClass.JAVA);
	};
	
	public KIDSUIDatasetComponent(IRI myID, KIDSGUIOracle o){
		super(myID, o);
		this.deflocation = KIDSComponentDefinition.ABOX;

		for (String p : reqProps.keySet()){
			myReqProps.add(new KIDSUIRequiredProperty(
					IRI.create(TBOXIRI + p), 
					IRI.create(TBOXIRI + reqProps.get(p))
					));

		}
		for (String p : infProps.keySet()){
			myInfProps.add(new KIDSUIInferredProperty(
					IRI.create(TBOXIRI + p), 
					IRI.create(TBOXIRI + infProps.get(p))
					));

		}
		for (String p : datProps.keySet()){
			myDataProps.add(new KIDSUIRequiredDataProperty(
					IRI.create(TBOXIRI + p), 
					datProps.get(p)
					));

		}
		requiredSubclassOf = IRI.create(TBOXIRI + "#Dataset");
	}
	
	@Override
	public DefaultMutableTreeNode getComponentDetails(DefaultMutableTreeNode root){
		super.getComponentDetails(root);
		
		DefaultMutableTreeNode datasetStats = new DefaultMutableTreeNode("Dataset Details:");
		root.add(datasetStats);

		// We will need to instantiate the dataset for this, if possible:
		String location;
		try {
			location = o.getDatasetLocation(myIRI);
			File dsFile = new File(location);
			datasetStats.add(new DefaultMutableTreeNode(String.format("Canonical filename: %s", dsFile.getCanonicalFile())));
			datasetStats.add(new DefaultMutableTreeNode(String.format("Can read/write/execute: %s/%s/%s", 
					dsFile.canRead(), dsFile.canWrite(), dsFile.canExecute())));
			datasetStats.add(new DefaultMutableTreeNode(String.format("Size (bytes): %s", dsFile.length())));
		} catch (KIDSOntologyDatatypeValuesException e) {
			logme.info("Could not load file details about dataset: %s", e);
		} catch (IOException e) {
			logme.info("Could not access file details: %s", e);
		}
		
		return root;
		

	}

}
