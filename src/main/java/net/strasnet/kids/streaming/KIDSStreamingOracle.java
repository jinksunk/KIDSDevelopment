/**
 * 
 */
package net.strasnet.kids.streaming;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNode;

import net.strasnet.kids.measurement.KIDSMeasurementOracle;

/**
 * @author Chris Strasburg
 *
 */
public class KIDSStreamingOracle extends KIDSMeasurementOracle {
	
	/*
	 * Static properties and classes:
	 */
	public static final IRI responseRequiresResourceObjectProperty = IRI.create(KIDSMeasurementOracle.TBOXPrefix + "#requiresResource");
	public static final IRI responseImplementationDataProperty = IRI.create(KIDSMeasurementOracle.TBOXPrefix + "#responseImplementation");
	public static final IRI responseClass = IRI.create(KIDSMeasurementOracle.TBOXPrefix + "#Response");
	
	private Map<Set<IRI>, IRI> eventMap = null;
	private static final Logger logme = LogManager.getLogger(KIDSStreamingOracle.class.getName());
	
	/**
	 * TODO: This needs to be much more sophisticated.
	 * @param currentSignalSet - The set of signals to use to determine which events are likely to be occurring.
	 * @return - A list of possibly occurring events (could be empty).
	 */
	public Set<IRI> getMatchedEvents(Set<IRI> currentSignalSet){
		logme.debug("Evaluating signal set for matched events");
		HashSet<IRI> matchedEvents = new HashSet<IRI>();
		
		if (eventMap == null){
			logme.debug("Event map is uninitialized. Initializing...");
			eventMap = new HashMap<Set<IRI>, IRI>();
		    // The naive way to do this is probably to query a list of all the events known to the knowledge base
		    // and then determine the percentage of signals known to be produced by that event that are matched by 
		    // signals in the given set.
		
		    // First, get all instances of the 'Event' class
			OWLClassExpression evClass = odf.getOWLClass(eventClass);
			Set<OWLNamedIndividual> evMembers = r.getInstances(evClass, false).getFlattened();
			
		    // Next, query on the http://solomon.cs.iastate.edu/ontologies/KIDS.owl#isProducerOf property to associate signals with the events
		    // they could possibly produce; we want a map from signals -> events. 
			for (OWLNamedIndividual event : evMembers){
				OWLObjectPropertyExpression isProducerOfEx = odf.getOWLObjectProperty(IRI.create(this.eventSignalRelation));
				Set<OWLNamedIndividual> sigMembers = r.getObjectPropertyValues(event, isProducerOfEx).getFlattened();
				// Well, now we need to build a set of IRIs from the OWLNamedIndividuals
				Set<IRI> sigMemberIRIs = new HashSet<IRI>();

				for (OWLNamedIndividual sigMember : sigMembers){
					sigMemberIRIs.add(sigMember.getIRI());
				}
				eventMap.put(sigMemberIRIs, event.getIRI());
			}
			logme.debug(String.format("Event map initialized with %d known events...", eventMap.size()));
		} 
		
		// Check each map key against the set of current signals to see if we have a match:
		// The current policy is that all signals must exist in the currentSignalSet; only perfect matches.
		// TOOD: Add a probabilistic component?
		// TODO: Take into account what the detector can see
		for (Set<IRI> key : eventMap.keySet()){
			if (checkEvent(key, currentSignalSet)){
				matchedEvents.add(eventMap.get(key));
				logme.debug(String.format("Event %s matched %d signals...", eventMap.get(key).getFragment(), key.size()));
			}
		}
		
		return matchedEvents;
	}
	
	/*
	 * 
	 * @param eventSigSet - The signal set associated with the event.
	 * @param knownSigSet - The signal set we have observed.
	 * @return - True if we believe an event has occurred, false otherwise.
	 */
	private boolean checkEvent(Set<IRI> eventSigSet, Set<IRI> knownSigSet){
		StringBuilder dbug = new StringBuilder();
		dbug.append("Known signals include: (");
		for (IRI i1 : knownSigSet){
			dbug.append(String.format("%s, ", i1.getFragment()));
		}
		dbug.append(")");
		
		dbug.append("; Event signals include: (");
		for (IRI i2 : eventSigSet){
			dbug.append(String.format("%s, ", i2.getFragment()));
		}
		dbug.append(")");

		for (IRI i : eventSigSet){
			if (!knownSigSet.contains(i)){
				logme.debug(String.format("Event failed to match signal %s", i.getFragment()));
				logme.debug(dbug);
				return false;
			}
		}
		return true;
	}

	public Set<IRI> getSignalsForEvent(IRI event) {
		logme.debug(String.format("Getting signals for event %s", event.getFragment()));
		Set<IRI> toReturn = new HashSet<IRI>();
		Set<OWLNamedIndividual> oniSet = r.getObjectPropertyValues(odf.getOWLNamedIndividual(event), 
				                                                   odf.getOWLObjectProperty(IRI.create(KIDSStreamingOracle.eventSignalRelation))).getFlattened();
		logme.debug(String.format("Executing query (%s, %s, ?)", event, KIDSStreamingOracle.eventSignalRelation));
		for (OWLNamedIndividual i : oniSet){
			toReturn.add(i.getIRI());
		}
		logme.debug(String.format("Found %d signals.", toReturn.size()));
		return toReturn;
	}

	/**
	 * This needs to be implemented; given an eventIRI, get the description (annotation?) associated with
	 * it.
	 * @param event
	 * @return
	 */
	public String getEventDescription(IRI event) {
		// TODO Auto-generated method stub
		return "IMPLEMENT THE EVENT DESCRIPTION QUERY";
	}

	/**
	 * 
	 * @param handlerID - the IRI of the alert handler we need the class name for
	 * @return The canonical java class name for the given IRI.
	 */
	public String getAlertHandlerClass(IRI handlerID) {
		String toReturn = null;
		logme.debug(String.format("Getting response implementation for %s", handlerID));
		Set<OWLLiteral> vals = r.getDataPropertyValues(odf.getOWLNamedIndividual(handlerID), 
				odf.getOWLDataProperty(KIDSStreamingOracle.responseImplementationDataProperty));
		logme.debug(String.format("Found %d values for response id %s", vals.size(), handlerID));
		if (vals.size() > 0){
		    toReturn = vals.iterator().next().getLiteral();
		}
		logme.debug(String.format("Returning value %s", toReturn));
		return toReturn;
	}

	/**
	 * 
	 * @param keySet - The set of resources available
	 * @return - A list of possible responses to deploy
	 */
	public List<IRI> getViableResponseList(Set<IRI> keySet) {
		List<IRI> toReturn = new LinkedList<IRI>();
		
		// First, get a list of all available responses
		logme.debug(String.format("Getting list of response individuals in class %s",KIDSStreamingOracle.responseClass));
		Set<OWLNamedIndividual> availableResponses = r.getInstances(
				odf.getOWLClass(KIDSStreamingOracle.responseClass), 
				false).getFlattened();
		logme.debug(String.format("Returned list of %d individuals.",availableResponses.size()));
		
		// Next, for each response, get the set of resources it requires
		Map<IRI, Set<IRI>> responseResourceMap = new HashMap<IRI, Set<IRI>>();
		for (OWLNamedIndividual oni : availableResponses){
			logme.debug(String.format("Getting set of required resources for %s.",oni.getIRI()));
			Set<OWLNamedIndividual> requiredResources = r.getObjectPropertyValues(
					oni, 
					odf.getOWLObjectProperty(KIDSStreamingOracle.responseRequiresResourceObjectProperty)).getFlattened();
			logme.debug(String.format("Found %d required resources for response %s.",requiredResources.size(), oni.getIRI()));
			Set<IRI> resourceIRIs = new HashSet<IRI>();
			for (OWLNamedIndividual res : requiredResources){
				resourceIRIs.add(res.getIRI());
			}
			responseResourceMap.put(oni.getIRI(), resourceIRIs);
		}
		
		// Eliminate those responses that can't be deployed with the given resources.
		for (OWLNamedIndividual response : availableResponses){
			toReturn.add(response.getIRI());
			Set<IRI> requiredResources = responseResourceMap.get(response.getIRI());
			for (IRI myI : requiredResources){
				if (!keySet.contains(myI)){
					logme.debug(String.format("Eliminating response %s - does not provide resource %s",response.getIRI(), myI));
					toReturn.remove(response.getIRI());
				}
			}
		}
		
		// Finally, for each response which is still viable, add it to the list
		return toReturn;
	}

}
