package net.strasnet.kids.measurement;

/**
 * The Label class represents information about a particular DataInstance, such as whether it is
 * actually related to a specific event, and (if so), which.
 * @author chrisstrasburg
 *
 */
public class Label {

	private boolean labelValue; // Whether we are associated with the event or not.
	private EventOccurrence associatedEventOccurrence; // The event occurrence we are tied to.
	
	public Label (EventOccurrence ourE, boolean value){
		labelValue = value;
		associatedEventOccurrence = ourE;
	}
	
	public boolean isEvent(){
		return labelValue;
	}
	
	public EventOccurrence getEventOccurrence(){
		return associatedEventOccurrence;
	}
	
}
