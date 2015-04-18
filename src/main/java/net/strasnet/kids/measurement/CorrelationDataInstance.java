/**
 * 
 */
package net.strasnet.kids.measurement;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.semanticweb.owlapi.model.IRI;

/**
 * @author cstras
 * Represents a 'complex' instance, that is, a DataInstance which is a composition of other DataInstances.
 * The label of the CDI is determined by the number of benign and (any) event-related instances included in 
 * this CDI.
 */
public class CorrelationDataInstance {
	
	private final static Logger logme = LogManager.getLogger(CorrelationDataInstance.class.getName());
	
	private Set<DataInstance> components; // The set of all instances (union of the below two sets)
	private Map<Integer, Set<DataInstance>> eInstances; // Set of event-related instances, indexed by event ID
    private Set<DataInstance> benignInstances; // The set of non-event-related instances
    private String correlationFeature; // The feature used to create this CDI
    private String correlationFeatureValue; // The value that this particular CDI has for that feature
	
	public CorrelationDataInstance(Set<DataInstance> initialComponents, String cFeature, String cValue){
		components = new HashSet<DataInstance>();
		benignInstances = new HashSet<DataInstance>();
		eInstances = new HashMap<Integer, Set<DataInstance>>();
		correlationFeature =  cFeature;
		correlationFeatureValue =  cValue;

		int bi = 0; int ei = 0;
		for (DataInstance i : initialComponents){
			components.add(i);
			// Unlabeled is assumed to be benign
			// TODO: Should set these when requested?
			if (i.getLabel() != null && i.getLabel().isEvent()){
				Integer labelID = i.getLabel().getEventOccurrence().getID();
				Set<DataInstance> addTo;
				if (eInstances.containsKey(labelID)){
					addTo = eInstances.get(labelID);
				} else {
					addTo = new HashSet<DataInstance>();
					eInstances.put(labelID,addTo);
				}
				ei++;
				addTo.add(i);
			} else {
				bi++;
				benignInstances.add(i);
			}
		}
		logme.debug("Created benign CDI [" + bi + "," + ei + "] (including base instance " + this.getInstances().iterator().next().getID() + " )");
		
	}

	/**
	 * 
	 * @return A map from resource IDs to sets of resource values, constituting the set union of resources in this 
	 * Correlation Data Instance.
	 */
	public Map<IRI, Set<String>> getResourceSets() {
		Map<IRI, Set<String>> resourceMap = new HashMap<IRI, Set<String>>();
		
		for (DataInstance i : components){
			Map<IRI, String> iResources = i.getResources();
			for (IRI r : iResources.keySet()){
				Set<String> toAdd;
				if (resourceMap.containsKey(r)){
					toAdd = resourceMap.get(r);
				} else {
					toAdd = new HashSet<String>();
					resourceMap.put(r, toAdd);
				}
				toAdd.add(iResources.get(r));
			}
		}
		return resourceMap;
	}
	
	/**
	 * 
	 * @return - A map from event ID to the set of data instances related to that event ID in this
	 * correlated data instance.  NOTE: This is not a copy, it is a reference.
	 */
	public Map<Integer,Set<DataInstance>> getEventInstances(){
		return eInstances;
	}

	/**
	 * @return - The set of all data instances in this correlated instance 
	 */
	public Set<DataInstance> getInstances() {
		return this.components;
	}

	/**
	 * 
	 * @return True if this CDI is considered event related, false otherwise
	 */
	public boolean isEventRelated() {
		return getEventInstances().keySet().size() > 0;
	}
	
	/**
	 * Return a string listing out the instances, as well as the overall classification of the CDI
	 * 
	 * @override
	 */
	public String toString(){
		StringBuilder sb = new StringBuilder();

		// First print out the ID of this CDI, the event(s) it is associated with, and the number of instances
		sb.append(String.format("CDI ID: %s = %s ; Object ID: %s ; Events: \n", this.correlationFeature, this.correlationFeatureValue, System.identityHashCode(this)));
		
		// List out the events associated with this CDI:
		StringBuilder eventIDs = new StringBuilder();
		
		for (Integer eid : this.eInstances.keySet()){
			eventIDs.append(String.format("\tEID: %d ; ",eid));
			for (DataInstance di : this.eInstances.get(eid)){
				eventIDs.append(String.format("%s,",di.getID()));
			}
			eventIDs.deleteCharAt(eventIDs.length() -1);
		}
		
		sb.append(String.format("\n %s \nCDI Instances:",eventIDs));
		
		// Next, iterate over the instances, adding each
		for (DataInstance di : this.components){
			sb.append(String.format("\t%s\t%d\n",di.getID(),di.getLabel().getEventOccurrence().getID()));
		}
		
		return sb.toString();
	}
	
	//Provide a method to obtain the number of benign sub-instances in this instance. -- Maybe?
	
	//Provide a method to add a sub-instance to this instance -- Maybe?

}
