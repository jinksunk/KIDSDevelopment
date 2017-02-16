/**
 * 
 */
package net.strasnet.kids.responses;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.strasnet.kids.streaming.StreamingAlertHandler;
import net.strasnet.kids.streaming.StreamingAlertHandlerFactory;
import net.strasnet.kids.streaming.StreamingEvent;

/**
 * @author Chris Strasburg
 *
 */
public class hostFWBlock implements StreamingAlertHandler, KIDSResponse {
	
	Logger logme = LogManager.getLogger(hostFWBlock.class.getName());
	
	// TODO: Define required resources

	/* (non-Javadoc)
	 * @see net.strasnet.kids.streaming.StreamingAlertHandler#handleEvent(net.strasnet.kids.streaming.StreamingEvent)
	 */
	@Override
	public void handleEvent(StreamingEvent e) {
		this.logme.info(String.format("Host filewall block implemented for event %s.",e.getEventIRI()));
		// TODO: Actually interface with the host firewall - demonstrate a block
	}

}
