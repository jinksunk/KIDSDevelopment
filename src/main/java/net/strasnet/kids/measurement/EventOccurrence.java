package net.strasnet.kids.measurement;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;

/**
 * An event occurrence represents a specific event happening.  For instance, a particular CodeRed attack.
 * Each instance must have its own unique ID, representing a unique instance.
 * 
 * @author diel8r
 *
 */
public class EventOccurrence implements Comparable<EventOccurrence> {
	public static final EventOccurrence NONEVENT = new EventOccurrence(null,0);
	public static int numberOfEvents = 0;
	public static Map<IRI, Map<Integer,EventOccurrence>> eventIDMap = new HashMap<IRI, Map<Integer, EventOccurrence>>();
	
	private int eventID;
	private IRI myEventOntologyInstance;

	private EventOccurrence (IRI eventIRepresent, Integer eventID){
		this.eventID = eventID;
		myEventOntologyInstance = eventIRepresent;
		numberOfEvents++;
	}
	
	public int getID(){
		return eventID;
	}
	
	public IRI getEventIRI(){
		return myEventOntologyInstance;
	}
	
	public boolean equals(EventOccurrence o){
		return (eventID == o.getID());
	}

	public int compareTo(EventOccurrence o) {
		Integer myID = new Integer(eventID);
		Integer oID = new Integer(o.getID());
		return myID.compareTo(oID);
	}

	public static EventOccurrence getEventOccurrence(IRI eventIRI, Integer eid) {
		if (!eventIDMap.containsKey(eventIRI)){
			Map<Integer,EventOccurrence> toPut = new HashMap<Integer,EventOccurrence>();
			eventIDMap.put(eventIRI, toPut);
		}
		if (!eventIDMap.get(eventIRI).containsKey(eid)){
			eventIDMap.get(eventIRI).put(eid, new EventOccurrence(eventIRI, eid));
		}

		return eventIDMap.get(eventIRI).get(eid);
	}
}
