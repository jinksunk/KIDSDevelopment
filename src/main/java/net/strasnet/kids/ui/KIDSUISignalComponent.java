/**
 * 
 */
package net.strasnet.kids.ui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;

import net.strasnet.kids.ui.gui.KIDSGUIOracle;

/**
 * @author cstras
 * 
 * This class represents an Event for UI purposes in KIDS.
 */
public class KIDSUISignalComponent extends KIDSUIAbstractComponent implements KIDSUIComponent {
	
	public static final org.apache.logging.log4j.Logger logme = LogManager.getLogger(KIDSUISignalComponent.class.getName());

	private static final Map<String, String> reqProps = new HashMap<String, String>();
	static {
		reqProps.put("isProducerOf","Signal");
	};

	private static final Map<String, String> infProps = new HashMap<String, String>();
	static {
		infProps.put("isEvaluatedBy","Dataset");
		infProps.put("isRepresentedInDataset","Dataset");
		infProps.put("isAffectedBy","Response");
		infProps.put("isIncludedInLabel","DatasetLabel");
	};

	private static final Map<String, String> datProps = new HashMap<String, String>();
	static {
		infProps.put("isEvaluatedBy","Dataset");
		infProps.put("isRepresentedInDataset","Dataset");
		infProps.put("isAffectedBy","Response");
		infProps.put("isIncludedInLabel","DatasetLabel");
	};
	
	public KIDSUISignalComponent(IRI myID, KIDSGUIOracle o){
		super(myID, o);

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
					IRI.create(TBOXIRI + datProps.get(p))
					));

		}
	}

}