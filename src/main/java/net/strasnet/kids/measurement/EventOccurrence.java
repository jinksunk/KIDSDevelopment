package net.strasnet.kids.measurement;

import org.semanticweb.owlapi.model.IRI;

/**
 * An event occurrence represents a specific event happening.  For instance, a particular CodeRed attack.
 * Each instance must have its own unique ID, representing a unique instance.
 * 
 * @author diel8r
 *
 */
public class EventOccurrence implements Comparable<EventOccurrence> {
	public static final EventOccurrence NONEVENT = new EventOccurrence(null);
	public static int currentEventID = 0;
	private int eventID;
	private IRI myEventOntologyInstance;
	
	public EventOccurrence (IRI eventIRepresent){
		eventID = currentEventID++;
		myEventOntologyInstance = eventIRepresent;
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
}
