/**
 * 
 */
package net.strasnet.kids.measurement;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;

/**
 * @author cstras
 * Represents a 'complex' instance, that is, a DataInstance which is a composition of other DataInstances.
 */
public class CorrelationDataInstance {
	
	private Set<DataInstance> components; // The set of all instances (union of the below two sets)
	private Map<Integer, Set<DataInstance>> eInstances; // Set of event-related instances, indexed by event ID
    private Set<DataInstance> benignInstances; // The set of non-event-related instances
	
	public CorrelationDataInstance(Set<DataInstance> initialComponents){
		components = new HashSet<DataInstance>();
		benignInstances = new HashSet<DataInstance>();
		eInstances = new HashMap<Integer, Set<DataInstance>>();
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
				addTo.add(i);
			} else {
				benignInstances.add(i);
			}
		}
		
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
	
	//Provide a method to obtain the number of benign sub-instances in this instance. -- Maybe?
	
	//Provide a method to add a sub-instance to this instance -- Maybe?

}
