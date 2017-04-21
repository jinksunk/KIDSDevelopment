/**
 * 
 */
package net.strasnet.kids.eventgenerator;

import java.util.List;

import net.strasnet.kids.eventgenerator.dataelement.KIDSDataElement;

/**
 * @author cstras
 * This represents an event, and defines the methods by which to get event data elements.
 */
public interface KIDSEvent {
	public List<KIDSDataElement> getDataElements();
}
