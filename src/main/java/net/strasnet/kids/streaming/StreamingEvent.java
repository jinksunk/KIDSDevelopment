/**
 * Represents the data related to a detected event (e.g. a set of signals which match the knowledge base entry for a known event. 
 */
package net.strasnet.kids.streaming;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.strasnet.kids.measurement.DataInstance;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.IRI;

/**
 * @author Chris Strasburg
 *
 */
public class StreamingEvent {
	private Set<IRI> Signals = null;
	private String Description = null;
	private Set<DataInstance> DataInstances = null;
	private IRI Event = null;
	
	private static final Logger logme = LogManager.getLogger(StreamingEvent.class.getName());
	
	/**
	 * 
	 * @param eventIRI - The IRI of the detected event;
	 * @param sigSet - The set of signals detected in relation to this event;
	 * @param desc - The string description of the event (loaded from KB; possibly not necessary)
	 * @param instances - The set of data instances related to this event, as determined by the signals which matched the event
	 */
	public StreamingEvent (IRI eventIRI, Set<IRI> sigSet, String desc, Set<DataInstance> instances){
		StringBuilder consLogMsg = new StringBuilder();
		Event = eventIRI;
		consLogMsg.append(String.format("Event: %s\t", Event.getFragment().toString()));

		if (sigSet != null){
		    Signals = sigSet;
		} else {
			logme.warn("Null signal set provided, no signals will be recorded for event " + 
		               eventIRI.getFragment().toString());
			Signals = new HashSet<IRI>();
		}
		consLogMsg.append("Signals: ");
		for (IRI signal : Signals){
			consLogMsg.append(String.format("%s,",signal.getFragment().toString()));
		}
		consLogMsg.append("\t");

		Description = desc;
		consLogMsg.append("Description: " + Description);

		//TODO: Build overall resource value map from all data instances
		if (instances != null){
		    DataInstances = instances;
		} else {
			logme.warn("No data instances provided, no resources will be available for event " + 
		               eventIRI.getFragment().toString());
			DataInstances = new HashSet<DataInstance>();
		}
		consLogMsg.append(String.format("DataInstances: %d", DataInstances.size()));
		
		//TODO: Add debug logging
		logme.debug("Created streaming event: " + consLogMsg.toString());
	}
	
	/**
	 * Will look through the signals and related data instances to produce a set of available resources
	 * for this event. It will return the IRIs of the known available resources.
	 * @return A set of all the available resources for this event.
	 * 
	 * TODO: We need to handle the case when a resource has multiple values, since an event can span
	 * multiple data instances.
	 */
	public Map<IRI, String> getAvailableResources(){
		Map<IRI, String> toReturn = new HashMap<IRI, String>();
		
		for (DataInstance di : DataInstances){
			Map<IRI, String> tmap = di.getResources();
			for (IRI k : tmap.keySet()){
				toReturn.put(k, tmap.get(k)); // TODO: Broken - need to handle multiple values for a resource 
			}
		}
		
		logme.debug(String.format("Created a resource map with %d values.", toReturn.size()));
		return toReturn;
	}

	public IRI getEventIRI() {
		return Event;
	}

}
