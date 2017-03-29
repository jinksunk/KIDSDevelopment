/**
 * 
 */
package net.strasnet.kids.ui.components;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;

import net.strasnet.kids.ui.KIDSUIInferredProperty;
import net.strasnet.kids.ui.KIDSUIRequiredDataProperty;
import net.strasnet.kids.ui.KIDSUIRequiredProperty;
import net.strasnet.kids.ui.gui.KIDSGUIOracle;

/**
 * @author cstras
 * 
 * This class represents an Event for UI purposes in KIDS.
 */
public class KIDSUITimePeriodComponent extends KIDSUIAbstractComponent implements KIDSUIComponent {
	
	public static final org.apache.log4j.Logger logme = LogManager.getLogger(KIDSUITimePeriodComponent.class.getName());

	private static final Map<String, String> reqProps = new HashMap<String, String>();
	static {
		reqProps.put("#coincidesWithDatasetLabel","#DatasetLabel");
	};

	private static final Map<String, String> infProps = new HashMap<String, String>();
	static {
		infProps.put("#isIncludedInDataset","#Dataset");
	};

	private static final Map<String, KIDSDatatypeClass> datProps = new HashMap<String, KIDSDatatypeClass>();
	static {
	};
	
	public KIDSUITimePeriodComponent(IRI myID, KIDSGUIOracle o){
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
		
		logme.debug(String.format("Initialized Signal UI component for %s with: ReqPropChecks: %d, InfPropChecks: %d, ReqDataChecks: %d.", 
				myID,
				reqProps.size(),
				infProps.size(),
				datProps.size()));
	}

}
