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
public class KIDSUIEventComponent extends KIDSUIAbstractComponent implements KIDSUIComponent {
	
	public static final org.apache.log4j.Logger logme = LogManager.getLogger(KIDSUIEventComponent.class.getName());

	private static final Map<String, String> reqProps = new HashMap<String, String>();
	static {
		reqProps.put("#isProducerOf","#Signal");
	};

	private static final Map<String, String> infProps = new HashMap<String, String>();
	static {
		infProps.put("#isEvaluatedBy","#Dataset");
		infProps.put("#isRepresentedInDataset","#Dataset");
		infProps.put("#isAffectedBy","#Response");
		infProps.put("#isIncludedInLabel","#DatasetLabel");
	};

	private static final Map<String, KIDSDatatypeClass> datProps = new HashMap<String, KIDSDatatypeClass>();
	static {
	};
	
	public KIDSUIEventComponent(IRI myID, KIDSGUIOracle o){
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

		logme.debug(String.format("Initialized Event UI component for %s with: ReqPropChecks: %d, InfPropChecks: %d, ReqDataChecks: %d.", 
				myID,
				reqProps.size(),
				infProps.size(),
				datProps.size()));
	}

	/**
	 * To be eligable as an evaluation time period, the time period must.
	 * 
	 * @return - A set (possibly empty) of time periods over which this event can be evaluated in the current setup.
	 */
	public Set<KIDSUITimePeriodComponent> getAvailableTimePeriods() {
		Set<IRI> compatibleTimePeriods = o.getTimePeriodsForEvent(getIRI());
		Set<KIDSUITimePeriodComponent> toReturn = new HashSet<KIDSUITimePeriodComponent>();
		
		for (IRI t : compatibleTimePeriods){
			try {
				toReturn.add((KIDSUITimePeriodComponent) KIDSUIComponentFactory.getUIComponent(t, KIDSUITimePeriodComponent.class, o));
			} catch (InstantiationException e) {
				logme.warn(String.format("Could not instantiate component: %s (%s) ... skipping.", t, e.getMessage()));
			}
		}

		return toReturn;
	}

	/**
	 * Return the list of dataset views by which this event can be evaluated. - Note: there may be multiple views for a single
	 * dataset.
	 * @return - A set of dataset view components.
	 */
	public Set<KIDSUIDatasetViewComponent> getAvailableDatasetViews() {
		Set<IRI> dvSet = o.getDatasetViewsForEvent(getIRI());
		Set<KIDSUIDatasetViewComponent> toReturn = new HashSet<KIDSUIDatasetViewComponent>();

		
		for (IRI dv : dvSet){
			try {
				toReturn.add((KIDSUIDatasetViewComponent) KIDSUIComponentFactory.getUIComponent(dv, KIDSUIDatasetViewComponent.class, o));
			} catch (InstantiationException e) {
				logme.warn(String.format("Could not instantiate component for %s (class %s): %s", dv, KIDSUIDatasetViewComponent.class, e.getMessage()));
				e.printStackTrace();
			}
		}

		return toReturn;
	}

}
